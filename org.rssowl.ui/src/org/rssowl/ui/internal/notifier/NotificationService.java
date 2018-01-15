/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2009 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.notifier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.BookMarkAdapter;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.SearchMarkAdapter;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.BatchedBuffer;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.ApplicationWorkbenchAdvisor;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.preferences.NotifierPreferencesPage;
import org.rssowl.ui.internal.util.JobRunner;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The <code>NotificationService</code> listens on News being downloaded and
 * opens the <code>NotificationPopup</code> to show them in case the preferences
 * are set to show notifications.
 *
 * @author bpasero
 */
public class NotificationService {

  /* Batch News-Events for every 5 seconds */
  private static final int BATCH_INTERVAL = 5000;

  private final NewsListener fNewsListener;
  private final SearchMarkListener fSearchMarkListener;
  private final BookMarkListener fBookMarkListener;
  private final FolderListener fFolderListener;
  private final IPreferenceScope fGlobalPreferences;
  private final BatchedBuffer<NotificationItem> fBatchedBuffer;
  private final Map<String /* Feed Link */, Boolean /* Enablement State */> fNotifierEnablementCache = new ConcurrentHashMap<String, Boolean>();

  /* Singleton instance */
  private static NotificationPopup fgNotificationPopup;

  /* Remember Last Closing Time */
  private static long fgLastNotificationPopupCloseTime;

  /** Supported Modes of the Notifier */
  public enum Mode {

    /** Automatic Notification of Incoming News (default) */
    INCOMING_AUTOMATIC,

    /** Manual Notification of Incoming News */
    INCOMING_MANUAL,

    /** Notification of Recent News */
    RECENT
  }

  /** Creates a new Notification Service */
  public NotificationService() {

    /* Process Events batched */
    BatchedBuffer.Receiver<NotificationItem> receiver = new BatchedBuffer.Receiver<NotificationItem>() {
      @Override
      public IStatus receive(Collection<NotificationItem> items, Job job, IProgressMonitor monitor) {
        if (!monitor.isCanceled())
          showItems(items, Mode.INCOMING_AUTOMATIC, monitor);

        return Status.OK_STATUS;
      }
    };

    fBatchedBuffer = new BatchedBuffer<NotificationItem>(receiver, BATCH_INTERVAL);
    fGlobalPreferences = Owl.getPreferenceService().getGlobalScope();
    fNewsListener = registerNewsListener();
    fSearchMarkListener = registerSearchMarkListener();
    fBookMarkListener = registerBookMarkListener();
    fFolderListener = registerFolderListener();
  }

  /** Shutdown this Service */
  public void stopService() {
    fBatchedBuffer.cancel(false);
    DynamicDAO.removeEntityListener(INews.class, fNewsListener);
    DynamicDAO.removeEntityListener(ISearchMark.class, fSearchMarkListener);
    DynamicDAO.removeEntityListener(IBookMark.class, fBookMarkListener);
    DynamicDAO.removeEntityListener(IFolder.class, fFolderListener);
  }

  /**
   * Called whenever settings on the {@link NotifierPreferencesPage} have
   * changed. In our case, we will invalidate the cache of enabled bookmarks.
   */
  public void notifySettingsChanged() {
    synchronized (fNotifierEnablementCache) {
      fNotifierEnablementCache.clear();
    }
  }

  /**
   * @param news the list of {@link INews} to show in the notifier.
   * @param color the color to use for the news or <code>null</code> if none.
   * @param mode the {@link Mode} of the notification
   */
  public void show(List<INews> news, RGB color, Mode mode) {
    show(news, color, mode, false);
  }

  /**
   * @param news the list of {@link INews} to show in the notifier.
   * @param color the color to use for the news or <code>null</code> if none.
   * @param mode the {@link Mode} of the notification
   * @param direct if <code>true</code> directly show the news in the notifier
   * without going through the delayed buffer, <code>false</code> otherwise.
   */
  public void show(List<INews> news, RGB color, Mode mode, boolean direct) {

    /* Create Notification Items */
    Set<NotificationItem> items = new TreeSet<NotificationItem>();
    for (INews newsitem : news)
      items.add(new NewsNotificationItem(newsitem, color));

    /* Add into Buffer for automatic */
    if (!direct && !isPopupVisible() && mode == Mode.INCOMING_AUTOMATIC)
      fBatchedBuffer.addAll(items);

    /* Show Directly otherwise */
    else
      showItems(items, mode, null);
  }

  /**
   * @return <code>true</code> if the popup notification is currently visible
   * and <code>false</code> otherwise.
   */
  public synchronized boolean isPopupVisible() {
    return fgNotificationPopup != null;
  }

  /**
   * @return <code>true</code> if the notification popup was recently closed and
   * <code>false</code> otherwise.
   */
  public boolean wasPopupRecentlyClosed() {
    return System.currentTimeMillis() - fgLastNotificationPopupCloseTime < 300;
  }

  /**
   * Close the notification popup if it is currently showing.
   */
  public synchronized void closePopup() {
    NotificationPopup popup = fgNotificationPopup;
    if (popup != null)
      popup.doClose();
  }

  /* Listen on News Events */
  private NewsListener registerNewsListener() {
    NewsListener listener = new NewsAdapter() {
      @Override
      public void entitiesAdded(final Set<NewsEvent> events) {
        if (!Controller.getDefault().isShuttingDown())
          onNewsAdded(events);
      }
    };

    DynamicDAO.addEntityListener(INews.class, listener);
    return listener;
  }

  private BookMarkListener registerBookMarkListener() {
    BookMarkListener listener = new BookMarkAdapter() {
      @Override
      public void entitiesUpdated(Set<BookMarkEvent> events) {
        for (BookMarkEvent event : events) {
          if (event.isRoot() && event.getOldParent() != null) {
            notifySettingsChanged();
            break;
          }
        }
      }
    };
    DynamicDAO.addEntityListener(IBookMark.class, listener);

    return listener;
  }

  private FolderListener registerFolderListener() {
    FolderListener listener = new FolderAdapter() {
      @Override
      public void entitiesUpdated(Set<FolderEvent> events) {
        for (FolderEvent event : events) {
          if (event.isRoot() && event.getOldParent() != null) {
            notifySettingsChanged();
            break;
          }
        }
      }
    };
    DynamicDAO.addEntityListener(IFolder.class, listener);

    return listener;
  }

  /* Listen on Search Mark Events */
  private SearchMarkListener registerSearchMarkListener() {
    SearchMarkListener listener = new SearchMarkAdapter() {
      @Override
      public void newsChanged(Set<SearchMarkEvent> events) {
        if (!Controller.getDefault().isShuttingDown())
          onNewsChanged(events);
      }
    };

    DynamicDAO.addEntityListener(ISearchMark.class, listener);
    return listener;
  }

  private void onNewsChanged(Set<SearchMarkEvent> events) {

    /* Return if Notification is disabled */
    if (!fGlobalPreferences.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP))
      return;

    /* Filter Events if user decided to show Notifier only for selected Elements */
    Set<SearchMarkEvent> filteredEvents = new HashSet<SearchMarkEvent>(events.size());
    if (fGlobalPreferences.getBoolean(DefaultPreferences.LIMIT_NOTIFIER_TO_SELECTION)) {
      for (SearchMarkEvent event : events) {

        /* Check for new *new* News matching now */
        if (!event.isAddedNewNews())
          continue;

        /* Check for explicit selection */
        IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(event.getEntity());
        if (prefs.getBoolean(DefaultPreferences.ENABLE_NOTIFIER))
          filteredEvents.add(event);
      }
    }

    /* Filter Events based on other criterias otherwise */
    else {
      for (SearchMarkEvent event : events) {
        ISearchMark searchmark = event.getEntity();
        List<ISearchCondition> conditions = searchmark.getSearchConditions();

        /* Check for new *new* News matching now */
        if (!event.isAddedNewNews())
          continue;

        /* Look for a String search condition that is not Label */
        for (ISearchCondition condition : conditions) {
          if (condition.getValue() instanceof String && condition.getField().getId() != INews.LABEL) {
            filteredEvents.add(event);
            break;
          }
        }
      }
    }

    /* Create Items */
    Set<NotificationItem> items = new TreeSet<NotificationItem>();
    for (SearchMarkEvent event : filteredEvents)
      items.add(new SearchNotificationItem(event.getEntity(), event.getEntity().getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED))));

    /* Add into Buffer */
    if (!isPopupVisible())
      fBatchedBuffer.addAll(items);

    /* Show Directly */
    else
      showItems(items, Mode.INCOMING_AUTOMATIC, null);
  }

  private void onNewsAdded(final Set<NewsEvent> events) {

    /* Return if Notification is disabled */
    if (!fGlobalPreferences.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP))
      return;

    /* Return if events are not containing any NEW News */
    if (!CoreUtils.containsState(events, INews.State.NEW))
      return;

    /* Use a runnable for this piece of code as it might be executed from an async call or not */
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        Set<NewsEvent> eventsToShow = events;

        /* Filter Events if user decided to show Notifier only for selected Elements */
        if (fGlobalPreferences.getBoolean(DefaultPreferences.LIMIT_NOTIFIER_TO_SELECTION))
          eventsToShow = filterEvents(eventsToShow);

        /* Create Items */
        Set<NotificationItem> items = new TreeSet<NotificationItem>();
        for (NewsEvent event : eventsToShow) {
          INews news = event.getEntity();
          if (news.getState().equals(INews.State.NEW)) //Only show NEW news in Notifier
            items.add(new NewsNotificationItem(news));
        }

        /* Return if nothing to show */
        if (items.isEmpty())
          return;

        /* Add into Buffer */
        if (!isPopupVisible())
          fBatchedBuffer.addAll(items);

        /* Show Directly */
        else
          showItems(items, Mode.INCOMING_AUTOMATIC, null);
      }
    };

    /*
     * Optimization and Workaround for a Bug: It is quite useless to send notification items
     * to the buffer for showing in the notifier if the user has configured RSSOwl to only show
     * a notification when the application window is minimized and the window is currently not
     * minimized. This also fixes a timing issue where quickly minimizing the window after
     * receving news items would show them in the notifier (although they might have been read
     * before already). The fix is to check in the UI thread whether the application window
     * is minimized or not.
     */
    if (fGlobalPreferences.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED)) {
      Shell primaryShell = OwlUI.getPrimaryShell();
      if (primaryShell != null) {
        JobRunner.runInUIThread(primaryShell, new Runnable() { //MUST NOT RUN SYNCED IN UI THREAD FROM EVENT - DEADLOCK ALERT !!!
          @Override
          public void run() {
            if (Controller.getDefault().isShuttingDown())
              return;

            ApplicationWorkbenchWindowAdvisor advisor = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;
            if (advisor != null && !advisor.isMinimizedToTray() && !advisor.isMinimized())
              return;

            JobRunner.runInBackgroundThread(runnable);
          }
        });
      }
    }

    /* Directly Execute */
    else
      runnable.run();
  }

  /**
   * @param news the {@link INews} to check if it can show or not.
   * @return <code>true</code> if the news can be shown and <code>false</code>
   * if it should not show due to filter rules.
   */
  public boolean shouldShow(INews news) {
    if (!fGlobalPreferences.getBoolean(DefaultPreferences.LIMIT_NOTIFIER_TO_SELECTION))
      return true;

    return shouldShow(news.getFeedLinkAsText());
  }

  private boolean shouldShow(String feedLink) {
    synchronized (fNotifierEnablementCache) {
      if (!fNotifierEnablementCache.containsKey(feedLink))
        updateEnabledFeedCache();

      Boolean notifierEnabled = fNotifierEnablementCache.get(feedLink);
      if (notifierEnabled == null) {
        notifierEnabled = false;
        fNotifierEnablementCache.put(feedLink, notifierEnabled);
      }

      return notifierEnabled;
    }
  }

  private Set<NewsEvent> filterEvents(Set<NewsEvent> events) {
    Set<NewsEvent> filteredEvents = new HashSet<NewsEvent>();

    for (NewsEvent event : events) {
      if (!event.getEntity().isVisible())
        continue;

      if (shouldShow(event.getEntity().getFeedLinkAsText()))
        filteredEvents.add(event);
    }

    return filteredEvents;
  }

  private void updateEnabledFeedCache() {
    Collection<IBookMark> bookMarks = DynamicDAO.loadAll(IBookMark.class);
    for (IBookMark bookMark : bookMarks) {
      IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(bookMark);
      fNotifierEnablementCache.put(bookMark.getFeedLinkReference().getLinkAsText(), prefs.getBoolean(DefaultPreferences.ENABLE_NOTIFIER));
    }
  }

  /* Show Notification in UI Thread */
  private void showItems(final Collection<NotificationItem> items, final Mode mode, final IProgressMonitor monitor) {

    /* Ignore empty lists */
    if (items.isEmpty())
      return;

    /* Make sure to run in UI Thread */
    JobRunner.runInUIThread(OwlUI.getPrimaryShell(), new Runnable() {
      @Override
      public void run() {

        /* Return early if shutting down */
        if (Controller.getDefault().isShuttingDown() || (monitor != null && monitor.isCanceled()))
          return;

        /* Return if Notification should only show when minimized */
        ApplicationWorkbenchWindowAdvisor advisor = ApplicationWorkbenchAdvisor.fgPrimaryApplicationWorkbenchWindowAdvisor;
        boolean minimized = advisor != null && (advisor.isMinimizedToTray() || advisor.isMinimized());
        if (!minimized && fGlobalPreferences.getBoolean(DefaultPreferences.SHOW_NOTIFICATION_POPUP_ONLY_WHEN_MINIMIZED))
          return;

        /* Show News in Popup */
        synchronized (NotificationService.this) {

          /* Popup not yet visible, create new */
          if (fgNotificationPopup == null) {
            fgNotificationPopup = new NotificationPopup(items.size(), mode) {
              @Override
              public boolean doClose() {
                fgNotificationPopup = null;
                fgLastNotificationPopupCloseTime = System.currentTimeMillis();
                return super.doClose();
              }
            };

            try {
              fgNotificationPopup.open(items);
            }

            /*
             * For some reason a NPE is raised from Decorations.restoreFocus(Decorations.java:806)
             * as outlined in Bug 1389 (NullPointer while clicking on tray icon). This is actually
             * caused by a bug in Eclipse (https://bugs.eclipse.org/bugs/show_bug.cgi?id=212219)
             * that is only fixed in version 3.5 and newer.
             */
            catch (Exception e) {
              fgNotificationPopup = null;
            }
          }

          /* Notifier already opened - Show Items (only for automatic) */
          else if (mode == Mode.INCOMING_AUTOMATIC)
            fgNotificationPopup.makeVisible(items);
        }
      }
    });
  }
}