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

package org.rssowl.ui.internal.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.event.BookMarkAdapter;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.NewsBinAdapter;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.persist.service.IndexListener;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.SearchHit;
import org.rssowl.ui.internal.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The <code>SavedSearchService</code> is responsible to listen for updates to
 * the search-index and updating all <code>ISearchMark</code>s as a result to
 * that event in order to reflect changing search results in the UI.
 *
 * @author bpasero
 */
/**
 * @author MuxaJIbI4
 */
public class SavedSearchService {

  /* Time in millies before updating the saved searches (long) */
  private static final int BATCH_INTERVAL_LONG = 10000;

  /* Time in millies before updating the saved searches (short) */
  private static final int BATCH_INTERVAL_SHORT = 1000;

  /* Number of updated documents before using the long batch interval */
  private static final int SHORT_THRESHOLD = 1;

  // TODO make this Job shown in progress view
  private final Job fBatchJob;

  // service running state
  private final AtomicBoolean fServiceRunning = new AtomicBoolean(false);

  // show if we update any saved searhes now. If we do it in 1 thread it's not work
  private final AtomicBoolean fSearchesUpdating = new AtomicBoolean(false);

  private final AtomicBoolean fBatchInProcess = new AtomicBoolean(false);
  private final AtomicBoolean fUpdatedOnce = new AtomicBoolean(false);
  private final AtomicBoolean fForceQuickUpdate = new AtomicBoolean(false);
  private IndexListener fIndexListener;
  private BookMarkAdapter fBookmarkListener;
  private NewsBinAdapter fNewsBinListener;
  private FolderAdapter fFolderListener;

  /** Creates and Starts this Service */
  public SavedSearchService() {
    fBatchJob = createBatchJob();
    createListeners();
    startService();
  }

  /** Starts this service by adding listeners */
  public void startService() {
    if (!fServiceRunning.getAndSet(true)) {
      registerListeners();
      fBatchJob.schedule(BATCH_INTERVAL_LONG);
    }
  }

  /**
   * Returns whether saved search service is running
   *
   * @return service running state
   */
  public boolean isRunning() {
    return fServiceRunning.get();
  }

  /** Stops this service and unregisters any listeners added. */
  public void stopService() {
    if (fServiceRunning.getAndSet(false)) {
      unregisterListeners();
      fBatchJob.cancel();
    }
  }

  private void createListeners() {

    /* Index Listener */
    fIndexListener = new IndexListener() {
      public void indexUpdated(int entitiesCount) {
        updateSavedSearchesFromEvent(entitiesCount);
      }
    };

    /* Bookmark Listener: Update on Reparent */
    fBookmarkListener = new BookMarkAdapter() {
      @Override
      public void entitiesUpdated(Set<BookMarkEvent> events) {
        for (BookMarkEvent event : events) {
          if (event.isRoot()) {
            IFolder oldParent = event.getOldParent();
            IFolder parent = event.getEntity().getParent();

            if (oldParent != null && !oldParent.equals(parent)) {
              updateSavedSearchesFromEvent(1);
              break;
            }
          }
        }
      }
    };

    /* News Bin Listener: Update on Reparent */
    fNewsBinListener = new NewsBinAdapter() {
      @Override
      public void entitiesUpdated(Set<NewsBinEvent> events) {
        for (NewsBinEvent event : events) {
          if (event.isRoot()) {
            IFolder oldParent = event.getOldParent();
            IFolder parent = event.getEntity().getParent();

            if (oldParent != null && !oldParent.equals(parent)) {
              updateSavedSearchesFromEvent(1);
              break;
            }
          }
        }
      }
    };

    /* Folder Listener: Update on Reparent */
    fFolderListener = new FolderAdapter() {
      @Override
      public void entitiesUpdated(Set<FolderEvent> events) {
        for (FolderEvent event : events) {
          if (event.isRoot()) {
            IFolder oldParent = event.getOldParent();
            IFolder parent = event.getEntity().getParent();

            if (oldParent != null && !oldParent.equals(parent)) {
              updateSavedSearchesFromEvent(1);
              break;
            }
          }
        }
      }
    };
  }

  private void registerListeners() {
    Owl.getPersistenceService().getModelSearch().addIndexListener(fIndexListener);
    DynamicDAO.addEntityListener(IBookMark.class, fBookmarkListener);
    DynamicDAO.addEntityListener(INewsBin.class, fNewsBinListener);
    DynamicDAO.addEntityListener(IFolder.class, fFolderListener);
  }

  private void unregisterListeners() {
    Owl.getPersistenceService().getModelSearch().removeIndexListener(fIndexListener);
    DynamicDAO.removeEntityListener(IBookMark.class, fBookmarkListener);
    DynamicDAO.removeEntityListener(INewsBin.class, fNewsBinListener);
    DynamicDAO.removeEntityListener(IFolder.class, fFolderListener);
  }

  /**
   * shutting down or SavedSearchServce is stopped
   *
   * @return whether to break all ongoing searches
   */
  private boolean stopSearching() {
    return Controller.getDefault().isShuttingDown() || !isRunning();
  }

  private void updateSavedSearchesFromEvent(int entitiesCount) {
    if (!stopSearching()) {
      return;
    }

    if (!InternalOwl.TESTING) {
      onIndexUpdated(entitiesCount);
    } else {
      updateSavedSearches(true);
    }
  }

  /**
   * Tells this Service to rapidly update all saved searches when the next
   * indexing is done. This can be called after an atomic operation (e.g.
   * Marking some News as read) to force a quick update on all saved searches.
   */
  public void forceQuickUpdate() {
    fForceQuickUpdate.set(true);
  }

  private void onIndexUpdated(int entitiesCount) {

    /* Start a new Job if one is not in progress */
    if (!fBatchInProcess.getAndSet(true)) {
      fBatchJob.schedule((entitiesCount <= SHORT_THRESHOLD || fForceQuickUpdate.get()) ? BATCH_INTERVAL_SHORT : BATCH_INTERVAL_LONG);
      return;
    }
  }

  private Job createBatchJob() {
    Job job = new Job("") { //$NON-NLS-1$
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        fBatchInProcess.set(false);
        fForceQuickUpdate.set(false);

        /* Update all saved searches */
        SafeRunner.run(new LoggingSafeRunnable() {
          public void run() throws Exception {
            if (!stopSearching()) {
              updateSavedSearches(true);
            }
          }
        });

        return Status.OK_STATUS;
      }
    };

    job.setSystem(true);
    job.setUser(false);

    return job;
  }

  /**
   * Update the results of all <code>ISearchMark</code>s stored in RSSOwl.
   *
   * @param force If set to <code>TRUE</code>, update saved searches even if
   * done before.
   */
  public void updateSavedSearches(boolean force) {
    if (!force && fUpdatedOnce.get()) {
      return;
    }

    Collection<ISearchMark> searchMarks = DynamicDAO.loadAll(ISearchMark.class);
    updateSavedSearches(searchMarks);
  }

  /**
   * @param searchMarks The Set of <code>ISearchMark</code> to update the
   * results in.
   */
  public void updateSavedSearches(Collection<ISearchMark> searchMarks) {
    updateSavedSearches(searchMarks, false);
  }

  /**
   * @param searchMarks The Set of <code>ISearchMark</code> to update the
   * results in. TODO: make cancelable at any stage: make Queue similar to
   * JobQueue for reloading Boormarks
   * @param fromUserEvent Indicates whether to update the saved searches due to
   * a user initiated event or an automatic one.
   */
  public void updateSavedSearches(Collection<ISearchMark> searchMarks, boolean fromUserEvent) {
    boolean firstUpdate = !fUpdatedOnce.get();

    fUpdatedOnce.set(true);
    IModelSearch modelSearch = Owl.getPersistenceService().getModelSearch();
    Set<SearchMarkEvent> events = new HashSet<SearchMarkEvent>(searchMarks.size());

    /* For each Search Mark */
    for (ISearchMark searchMark : searchMarks) {
      if (stopSearching()) {
        return;
      }

      /* Execute the search */
      List<SearchHit<NewsReference>> results = modelSearch.searchNews(searchMark.getSearchConditions(), searchMark.matchAllConditions());

      /* Fill Result into Map Buckets */
      Map<INews.State, List<NewsReference>> resultsMap = new EnumMap<INews.State, List<NewsReference>>(INews.State.class);

      Set<State> visibleStates = INews.State.getVisible();
      for (SearchHit<NewsReference> searchHit : results) {

        /* Return early if shutting down */
        if (stopSearching()) {
          return;
        }

        INews.State state = (State) searchHit.getData(INews.STATE);
        if (visibleStates.contains(state)) {
          List<NewsReference> newsRefs = resultsMap.get(state);
          if (newsRefs == null) {
            newsRefs = new ArrayList<NewsReference>(results.size() / 3);
            resultsMap.put(state, newsRefs);
          }
          newsRefs.add(searchHit.getResult());
        }
      }

      /* Set Result */
      Pair<Boolean, Boolean> result = searchMark.setNewsRefs(resultsMap);
      boolean changed = result.getFirst();
      boolean newNewsAdded = result.getSecond();

      /* Create Event to indicate changed results if any */
      if (changed) {
        events.add(new SearchMarkEvent(searchMark, null, true, !firstUpdate && !fromUserEvent && newNewsAdded));
      }
    }

    /* Notify Listeners */
    if (!events.isEmpty() && !stopSearching()) {
      DynamicDAO.getDAO(ISearchMarkDAO.class).fireNewsChanged(events);
    }
  }
}