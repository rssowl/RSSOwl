/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse Public License v1.0 which accompanies this    **
 **   distribution, and is available at:                                     **
 **   http://www.rssowl.org/legal/epl-v10.html                               **
 **                                                                          **
 **   A copy is found in the file epl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **                                                                          **
 **   This copyright notice MUST APPEAR in all copies of the file!           **
 **                                                                          **
 **   Contributors:                                                          **
 **     RSSOwl Development Team - initial API and implementation             **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.ui.internal.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.AuthenticationRequiredException;
import org.rssowl.core.connection.ConnectionException;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.SearchFilterAdapter;
import org.rssowl.core.util.BatchedBuffer;
import org.rssowl.core.util.BatchedBuffer.Receiver;
import org.rssowl.core.util.SyncItem;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

/**
 * A service that listens to changes of {@link INews} and then synchronizes with
 * an online server to notify about changes.
 *
 * @author bpasero
 */
public class SyncService implements Receiver<SyncItem> {

  /* Delay in Millies before syncing */
  private static final int SYNC_DELAY = 15000; // 15 seconds

  /* Sync scheduler interval in Millies */
  private static final int SYNC_SCHEDULER = 300000; // 5 minutes

  /* Maximum sync items per call to google reader API */
  private static final int SYNC_PAGE_SIZE = 150;

  /* HTTP Constants */
  private static final String REQUEST_HEADER_CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
  private static final String REQUEST_HEADER_AUTHORIZATION = "Authorization"; //$NON-NLS-1$
  private static final String CONTENT_TYPE_FORM_ENCODED = "application/x-www-form-urlencoded"; //$NON-NLS-1$

  private final BatchedBuffer<SyncItem> fSynchronizer;
  private final SyncItemsManager fSyncItemsManager;
  private final AtomicInteger fTotalSyncItemCount = new AtomicInteger();
  private Job fSyncScheduler;
  private NewsListener fNewsListener;
  private SearchFilterAdapter fSearchFilterListener;
  private SyncStatus fStatus;

  /** Status holder used for reporting Sync Status */
  public static class SyncStatus extends Status {
    private final long fTime = System.currentTimeMillis();
    private final int fItemCount;
    private final int fTotalItemCount;

    public SyncStatus(int itemCount, int totalItemCount) {
      super(IStatus.OK, Activator.PLUGIN_ID, null, null);
      fItemCount = itemCount;
      fTotalItemCount = totalItemCount;
    }

    public SyncStatus(String message, Throwable exception) {
      super(IStatus.ERROR, Activator.PLUGIN_ID, message, exception);
      fItemCount = 0;
      fTotalItemCount = 0;
    }

    public long getTime() {
      return fTime;
    }

    public int getItemCount() {
      return fItemCount;
    }

    public int getTotalItemCount() {
      return fTotalItemCount;
    }
  }

  /**
   * Starts the synchronizer by listening to news events.
   */
  public SyncService() {
    fSynchronizer = new BatchedBuffer<SyncItem>(this, SYNC_DELAY);
    fSyncItemsManager = new SyncItemsManager();
    init();
  }

  private void init() {

    /* Listen to Events for Syncing */
    registerListeners();

    /* Deserialize uncommitted items from previous session */
    try {
      fSyncItemsManager.startup();
    } catch (IOException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    } catch (ClassNotFoundException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

    /* Schedule Sync of previously uncommitted items as needed */
    if (fSyncItemsManager.hasUncommittedItems())
      addAllAsync(fSyncItemsManager.getUncommittedItems().values()); //Must add async because the buffer is blocking while running

    /* Start a Job that periodically tries to sync uncommitted items */
    fSyncScheduler = new Job("") { //$NON-NLS-1$
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        if (!Controller.getDefault().isShuttingDown() && !monitor.isCanceled()) {

          /* Only trigger synchronization if the synchronizer is not already running and if we have uncommitted items */
          if (fSyncItemsManager.hasUncommittedItems() && !fSynchronizer.isScheduled())
            fSynchronizer.addAll(fSyncItemsManager.getUncommittedItems().values());

          /* Re-Schedule */
          schedule(SYNC_SCHEDULER);
        }

        return Status.OK_STATUS;
      }
    };
    fSyncScheduler.setSystem(true);
    fSyncScheduler.setUser(false);
    fSyncScheduler.schedule(SYNC_SCHEDULER);
  }

  private void registerListeners() {

    /* News Listener */
    fNewsListener = new NewsAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsEvent> events) {
        Collection<SyncItem> items = filter(events);
        synchronize(items);
      }
    };
    DynamicDAO.addEntityListener(INews.class, fNewsListener);

    /* News Filter Listener */
    fSearchFilterListener = new SearchFilterAdapter() {
      @Override
      public void filterApplied(ISearchFilter filter, Collection<INews> news) {
        Collection<SyncItem> items = filter(filter, news);
        synchronize(items);
      }
    };
    DynamicDAO.addEntityListener(ISearchFilter.class, fSearchFilterListener);
  }

  private void addAllAsync(final Collection<SyncItem> items) {
    JobRunner.runInBackgroundThread(new Runnable() {
      public void run() {
        fSynchronizer.addAll(items);
      }
    });
  }

  private Collection<SyncItem> filter(ISearchFilter filter, Collection<INews> news) {
    List<SyncItem> filteredEvents = new ArrayList<SyncItem>();
    for (INews item : news) {
      if (!SyncUtils.isSynchronized(item))
        continue;

      SyncItem syncItem = SyncItem.toSyncItem(filter, item);
      if (syncItem != null)
        filteredEvents.add(syncItem);
    }

    return filteredEvents;
  }

  private Collection<SyncItem> filter(Set<NewsEvent> events) {
    List<SyncItem> filteredEvents = new ArrayList<SyncItem>();
    for (NewsEvent event : events) {
      if (event.isMerged() || event.getOldNews() == null || !SyncUtils.isSynchronized(event.getEntity()))
        continue;

      SyncItem syncItem = SyncItem.toSyncItem(event);
      if (syncItem != null)
        filteredEvents.add(syncItem);
    }

    return filteredEvents;
  }

  private void unregisterListeners() {
    DynamicDAO.removeEntityListener(INews.class, fNewsListener);
    DynamicDAO.removeEntityListener(ISearchFilter.class, fSearchFilterListener);
  }

  /**
   * Asks this service to synchronize all outstanding items.
   */
  public void synchronize() {
    JobRunner.runInBackgroundThread(new Runnable() {
      public void run() {
        if (!Controller.getDefault().isShuttingDown() && fSyncItemsManager.hasUncommittedItems() && !fSynchronizer.isScheduled())
          fSynchronizer.addAll(fSyncItemsManager.getUncommittedItems().values());
      }
    });
  }

  /**
   * @param items a {@link Collection} of {@link SyncItem} to process.
   */
  public void synchronize(Collection<SyncItem> items) {
    if (!items.isEmpty()) {
      fSyncItemsManager.addUncommitted(items);
      addAllAsync(items); //Must add async because the buffer is blocking while running
    }
  }

  /**
   * @return the {@link SyncStatus} of the last synchronization run.
   */
  public SyncStatus getStatus() {
    return fStatus;
  }

  /**
   * @return a {@link Map} of uncommitted {@link SyncItem} at this moment in
   * time.
   */
  public Map<String, SyncItem> getUncommittedItems() {
    return fSyncItemsManager.getUncommittedItems();
  }

  /**
   * Stops the Synchronizer.
   *
   * @param emergency if <code>true</code>, indicates that RSSOwl is shutting
   * down in an emergency situation where methods should return fast and
   * <code>false</code> otherwise.
   */
  public void stopService(boolean emergency) {

    /* Stop Listening and Scheduling */
    unregisterListeners();
    fSyncScheduler.cancel();

    /* Wait until the Synchronizer has finished synchronizing (if not in emergency shutdown) */
    fSynchronizer.cancel(!emergency);

    /* Serialize uncomitted synchronization items */
    if (!emergency) {
      try {
        fSyncItemsManager.shutdown();
      } catch (FileNotFoundException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (IOException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }
  }

  /*
   * @see org.rssowl.core.util.BatchedBuffer.Receiver#receive(java.util.Collection, org.eclipse.core.runtime.jobs.Job, org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus receive(Collection<SyncItem> items, Job job, IProgressMonitor monitor) {

    /* Synchronize */
    try {
      int itemCount = sync(fSyncItemsManager.getUncommittedItems().values(), monitor);
      int totalItemCount = fTotalSyncItemCount.addAndGet(itemCount);
      if (itemCount > 0)
        fStatus = new SyncStatus(itemCount, totalItemCount);
    }

    /* Authentication Required */
    catch (AuthenticationRequiredException e) {
      fStatus = new SyncStatus(e.getMessage(), e);
      handleAuthenticationRequired(monitor);
    }

    /* Any other Connection Exception */
    catch (ConnectionException e) {
      Activator.getDefault().logError(e.getMessage(), e);
      fStatus = new SyncStatus(e.getMessage(), e);
    }

    return Status.OK_STATUS; //Intentionally using OK here to not spam the activity dialog
  }

  private void handleAuthenticationRequired(final IProgressMonitor monitor) {
    if (!Controller.getDefault().isShuttingDown() && !monitor.isCanceled()) {
      JobRunner.runInBackgroundThread(new Runnable() { //Run in background thread to avoid lock contention in buffer due to UI lock
        public void run() {
          Lock loginLock = Controller.getDefault().getLoginDialogLock();
          if (!Controller.getDefault().isShuttingDown() && !monitor.isCanceled() && loginLock.tryLock()) { //Avoid multiple login dialogs if login dialog already showing
            try {
              JobRunner.runSyncedInUIThread(new Runnable() {
                public void run() {
                  if (!Controller.getDefault().isShuttingDown() && !monitor.isCanceled())
                    OwlUI.openSyncLogin(null);
                }
              });
            } finally {
              loginLock.unlock();
            }
          }
        }
      });
    }
  }

  private int sync(Collection<SyncItem> items, IProgressMonitor monitor) throws ConnectionException {

    /* Return on cancellation */
    if (isCanceled(monitor))
      return 0;

    /* Group Sync Items by Feed and Merge Duplictates */
    Map<String, Map<String, SyncItem>> mapFeedToSyncItems = groupByStream(items);

    /* Return on cancellation */
    if (isCanceled(monitor))
      return 0;

    /* Obtain API Token */
    String token = getGoogleApiToken(monitor);
    String authToken = SyncUtils.getGoogleAuthToken(null, null, false, monitor); //Already up to date from previous call to getGoogleApiToken()
    if (token == null || authToken == null)
      throw new ConnectionException(Activator.getDefault().createErrorStatus("Unable to obtain a token for Google API access.")); //$NON-NLS-1$

    /* Return on cancellation */
    if (isCanceled(monitor))
      return 0;

    /* Synchronize for each Stream */
    int itemCount = 0;
    Set<Entry<String, Map<String, SyncItem>>> entries = mapFeedToSyncItems.entrySet();
    for (Entry<String, Map<String, SyncItem>> entry : entries) {
      if (entry.getValue() == null)
        continue;

      Collection<SyncItem> syncItems = entry.getValue().values();

      /* Find Equivalent Items to Sync with 1 Connection */
      List<List<SyncItem>> equivalentItemLists = findEquivalents(syncItems);

      /* For each list of equivalent items */
      for (List<SyncItem> equivalentItems : equivalentItemLists) {
        if (equivalentItems.isEmpty())
          continue;

        for (int syncPage = 0; syncPage <= equivalentItems.size() / SYNC_PAGE_SIZE; syncPage++) {
          int fromIndex = syncPage * SYNC_PAGE_SIZE;
          int toIndex = Math.min((syncPage + 1) * SYNC_PAGE_SIZE, equivalentItems.size());
          List<SyncItem> equivalentItemsPage = equivalentItems.subList(fromIndex, toIndex);

          /* Connection Headers */
          Map<String, String> headers = new HashMap<String, String>();
          headers.put(REQUEST_HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_ENCODED);
          headers.put(REQUEST_HEADER_AUTHORIZATION, SyncUtils.getGoogleAuthorizationHeader(authToken));

          /* POST Parameters */
          Map<String, String[]> parameters = new HashMap<String, String[]>();
          parameters.put(SyncUtils.API_PARAM_TOKEN, new String[] { token });

          List<String> identifiers = new ArrayList<String>();
          List<String> streamIds = new ArrayList<String>();
          Set<String> tagsToAdd = new HashSet<String>();
          Set<String> tagsToRemove = new HashSet<String>();
          for (SyncItem item : equivalentItemsPage) {
            identifiers.add(item.getId());
            streamIds.add(item.getStreamId());

            if (item.isMarkedRead()) {
              tagsToAdd.add(SyncUtils.CATEGORY_READ);
              tagsToRemove.add(SyncUtils.CATEGORY_UNREAD);
            }

            if (item.isMarkedUnread()) {
              tagsToAdd.add(SyncUtils.CATEGORY_UNREAD);
              tagsToAdd.add(SyncUtils.CATEGORY_TRACKING_UNREAD);
              tagsToRemove.add(SyncUtils.CATEGORY_READ);
            }

            if (item.isStarred())
              tagsToAdd.add(SyncUtils.CATEGORY_STARRED);

            if (item.isUnStarred())
              tagsToRemove.add(SyncUtils.CATEGORY_STARRED);

            List<String> addedLabels = item.getAddedLabels();
            if (addedLabels != null) {
              for (String label : addedLabels) {
                tagsToAdd.add(SyncUtils.CATEGORY_LABEL_PREFIX + label);
              }
            }

            List<String> removedLabels = item.getRemovedLabels();
            if (removedLabels != null) {
              for (String label : removedLabels) {
                tagsToRemove.add(SyncUtils.CATEGORY_LABEL_PREFIX + label);
              }
            }
          }

          parameters.put(SyncUtils.API_PARAM_IDENTIFIER, identifiers.toArray(new String[identifiers.size()]));
          parameters.put(SyncUtils.API_PARAM_STREAM, streamIds.toArray(new String[streamIds.size()]));

          if (!tagsToAdd.isEmpty())
            parameters.put(SyncUtils.API_PARAM_TAG_TO_ADD, tagsToAdd.toArray(new String[tagsToAdd.size()]));

          if (!tagsToRemove.isEmpty())
            parameters.put(SyncUtils.API_PARAM_TAG_TO_REMOVE, tagsToRemove.toArray(new String[tagsToRemove.size()]));

          /* Connection Properties */
          Map<Object, Object> properties = new HashMap<Object, Object>();
          properties.put(IConnectionPropertyConstants.HEADERS, headers);
          properties.put(IConnectionPropertyConstants.POST, Boolean.TRUE);
          properties.put(IConnectionPropertyConstants.PARAMETERS, parameters);
          properties.put(IConnectionPropertyConstants.CON_TIMEOUT, getConnectionTimeout());

          /* Return on cancellation */
          if (isCanceled(monitor))
            return itemCount;

          /* Perform POST */
          URI uri = URI.create(SyncUtils.GOOGLE_EDIT_TAG_URL);
          IProtocolHandler handler = Owl.getConnectionService().getHandler(uri);
          InputStream inS = null;
          try {
            inS = handler.openStream(uri, new NullProgressMonitor(), properties); //Do not allow to cancel this outgoing request for transactional reasons
            fSyncItemsManager.removeUncommitted(equivalentItemsPage);
            itemCount += equivalentItemsPage.size();
          } finally {
            if (inS != null) {
              try {
                inS.close();
              } catch (IOException e) {
                throw new ConnectionException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
              }
            }
          }

          /* Return on cancellation */
          if (isCanceled(monitor))
            return itemCount;
        }
      }
    }

    return itemCount;
  }

  private Map<String, Map<String, SyncItem>> groupByStream(Collection<SyncItem> items) {
    Map<String, Map<String, SyncItem>> mapFeedToSyncItems = new HashMap<String, Map<String, SyncItem>>();

    for (SyncItem item : items) {
      Map<String, SyncItem> streamItems = mapFeedToSyncItems.get(item.getStreamId());
      if (streamItems == null) {
        streamItems = new HashMap<String, SyncItem>();
        mapFeedToSyncItems.put(item.getStreamId(), streamItems);
      }

      streamItems.put(item.getId(), item);
    }

    return mapFeedToSyncItems;
  }

  private List<List<SyncItem>> findEquivalents(Collection<SyncItem> syncItems) {
    List<List<SyncItem>> equivalentItemLists = new ArrayList<List<SyncItem>>();
    List<SyncItem> currentItemList = new ArrayList<SyncItem>();
    equivalentItemLists.add(currentItemList);
    for (SyncItem item : syncItems) {
      if (currentItemList.isEmpty())
        currentItemList.add(item);
      else if (item.isEquivalent(currentItemList.get(0)))
        currentItemList.add(item);
      else {
        currentItemList = new ArrayList<SyncItem>();
        currentItemList.add(item);
        equivalentItemLists.add(currentItemList);
      }
    }

    return equivalentItemLists;
  }

  private String getGoogleApiToken(IProgressMonitor monitor) throws ConnectionException {
    ICredentialsProvider provider = Owl.getConnectionService().getCredentialsProvider(URI.create(SyncUtils.GOOGLE_LOGIN_URL));
    ICredentials creds = provider.getAuthCredentials(URI.create(SyncUtils.GOOGLE_LOGIN_URL), null);
    if (creds == null)
      throw new AuthenticationRequiredException(null, Status.CANCEL_STATUS);

    return SyncUtils.getGoogleApiToken(creds.getUsername(), creds.getPassword(), monitor);
  }

  private boolean isCanceled(IProgressMonitor monitor) {
    return (monitor != null && monitor.isCanceled());
  }

  private int getConnectionTimeout() {
    return Controller.getDefault().isShuttingDown() ? SyncUtils.SHORT_CON_TIMEOUT : SyncUtils.DEFAULT_CON_TIMEOUT;
  }

  /* Only used for testing */
  public void testSync(Collection<SyncItem> items) throws ConnectionException {
    int itemCount = sync(items, new NullProgressMonitor());
    int totalItemCount = fTotalSyncItemCount.addAndGet(itemCount);
    if (itemCount > 0)
      fStatus = new SyncStatus(itemCount, totalItemCount);
  }
}