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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.BookMarkAdapter;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.OwlUI.FeedViewOpenMode;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.JobRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Service managing automatic reload of Feeds in RSSOwl based on the user
 * preferences.
 *
 * @author bpasero
 */
public class FeedReloadService {

  /* The delay-threshold in millis (5 Minutes) */
  private static final int DELAY_THRESHOLD = 5 * 60 * 1000;

  /* The delay-value in millis (30 Seconds) */
  private static final int DELAY_VALUE = 30 * 1000;

  /* Listen to Bookmark Updates */
  private BookMarkAdapter fBookMarkListener;

  /* Map IBookMark to Update-Intervals */
  private final Map<IBookMark, Long> fMapBookMarkToInterval;

  /*
   * This subclass of a Job is making sure to delay the operation for <code>WAKEUP_DELAY</code>
   * millis in case it is detecting that the last run of the Job was some amount
   * of time (<code>DELAY_THRESHOLD</code>) after it was meant to be run due
   * to the given Update-Interval. This fixes a problem, where all Update-Jobs
   * would immediately run after waking up from an OS hibernate (e.g. on
   * Windows). Since all Jobs are scheduled based on a time-dif, once waking up
   * from hibernate, the dif is usually telling the Jobs to schedule
   * immediately, even before network interfaces had any chance to start. Thus,
   * all BookMarks will show errors.
   */
  private class ReloadJob extends Job {
    private final IBookMark fBookMark;
    private long fLastRunInMillis;

    ReloadJob(IBookMark bookMark, String name) {
      super(name);
      fBookMark = bookMark;
      fLastRunInMillis = System.currentTimeMillis();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {

      /* Update Interval of this BookMark */
      Long updateIntervalInSeconds = fMapBookMarkToInterval.get(fBookMark);

      /* Delay execution if required */
      if (delay(updateIntervalInSeconds) && !monitor.isCanceled()) {
        try {
          Thread.sleep(DELAY_VALUE);
        } catch (InterruptedException e) {
          /* Ignore */
        }
      }

      /* Update field */
      fLastRunInMillis = System.currentTimeMillis();

      /* Reload */
      if (!monitor.isCanceled() && !Controller.getDefault().isShuttingDown())
        Controller.getDefault().reloadQueued(fBookMark, null, null);

      /* Re-Schedule */
      if (!monitor.isCanceled() && !Controller.getDefault().isShuttingDown() && updateIntervalInSeconds != null)
        schedule(updateIntervalInSeconds * 1000);

      return Status.OK_STATUS;
    }

    @Override
    public boolean belongsTo(Object family) {
      return family.equals(fBookMark) || family.equals(FeedReloadService.this);
    }

    private boolean delay(Long updateIntervalInSeconds) {
      if (fLastRunInMillis == 0 || updateIntervalInSeconds == null)
        return false;

      long dif = System.currentTimeMillis() - fLastRunInMillis;
      return dif > ((updateIntervalInSeconds * 1000) + DELAY_THRESHOLD);
    }
  }

  /**
   * Instantiates the Feed Reload Service.
   */
  public FeedReloadService() {
    fMapBookMarkToInterval = new ConcurrentHashMap<IBookMark, Long>();

    /* Register Listeners */
    registerListeners();

    /* Init from a Background Thread */
    JobRunner.runInBackgroundThread(new Runnable() {
      @Override
      public void run() {
        init();
      }
    });
  }

  /** Unregister from Listeners and cancel all Jobs */
  public void stopService() {
    unregisterListeners();
    Job.getJobManager().cancel(this);
  }

  private void init() {

    /* Query Update Intervals and reload/open state */
    Collection<IBookMark> bookmarks = DynamicDAO.loadAll(IBookMark.class);
    Collection<INewsBin> newsbins = DynamicDAO.loadAll(INewsBin.class);

    final Set<IBookMark> bookmarksToReloadOnStartup = new HashSet<IBookMark>();
    final List<INewsMark> newsmarksToOpenOnStartup = new ArrayList<INewsMark>();

    /* For each Bookmark */
    for (IBookMark bookMark : bookmarks) {
      IPreferenceScope entityPreferences = Owl.getPreferenceService().getEntityScope(bookMark);

      /* BookMark is to reload in a certain Interval */
      if (entityPreferences.getBoolean(DefaultPreferences.BM_UPDATE_INTERVAL_STATE)) {
        long updateInterval = entityPreferences.getLong(DefaultPreferences.BM_UPDATE_INTERVAL);
        fMapBookMarkToInterval.put(bookMark, updateInterval);

        /* BookMark is to reload on startup */
        if (entityPreferences.getBoolean(DefaultPreferences.BM_RELOAD_ON_STARTUP))
          bookmarksToReloadOnStartup.add(bookMark);
      }

      /* BookMark is to open on startup */
      if (entityPreferences.getBoolean(DefaultPreferences.BM_OPEN_ON_STARTUP))
        newsmarksToOpenOnStartup.add(bookMark);
    }

    /* For each Newsbin */
    for (INewsBin bin : newsbins) {
      IPreferenceScope entityPreferences = Owl.getPreferenceService().getEntityScope(bin);

      /* Newsbin is to open on startup */
      if (entityPreferences.getBoolean(DefaultPreferences.BM_OPEN_ON_STARTUP))
        newsmarksToOpenOnStartup.add(bin);
    }

    /* Reload the ones that reload on startup */
    if (!bookmarksToReloadOnStartup.isEmpty()) {
      JobRunner.runInUIThread(null, new Runnable() {
        @Override
        public void run() {
          Controller.getDefault().reloadQueued(bookmarksToReloadOnStartup, null, null);
        }
      });
    }

    /* Initialize the Jobs that manages Updates */
    Set<Entry<IBookMark, Long>> entries = fMapBookMarkToInterval.entrySet();
    for (Entry<IBookMark, Long> entry : entries) {
      IBookMark bookMark = entry.getKey();
      scheduleUpdate(bookMark, entry.getValue());
    }

    /* Open BookMarks which are to open on startup */
    if (!newsmarksToOpenOnStartup.isEmpty()) {
      JobRunner.runInUIThread(null, new Runnable() {
        @Override
        public void run() {
          IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
          IWorkbenchPage page = (window != null) ? window.getActivePage() : null;
          if (page != null) {
            int maxOpenEditors = EditorUtils.getOpenEditorLimit();
            int openFeedViewCount = OwlUI.getOpenFeedViewCount();

            /* Do not open any Feed if already showing max number of Feeds */
            if (openFeedViewCount >= maxOpenEditors)
              return;

            /* Open in Feedview */
            OwlUI.openInFeedView(page, new StructuredSelection(newsmarksToOpenOnStartup), EnumSet.of(FeedViewOpenMode.IGNORE_ALREADY_OPENED, FeedViewOpenMode.IGNORE_REUSE));
          }
        }
      });
    }
  }

  private void scheduleUpdate(final IBookMark bookMark, Long intervalInSeconds) {
    Job updateJob = new ReloadJob(bookMark, ""); //$NON-NLS-1$
    updateJob.setSystem(true);
    updateJob.schedule(intervalInSeconds * 1000);
  }

  private void registerListeners() {
    fBookMarkListener = new BookMarkAdapter() {

      @Override
      public void entitiesAdded(Set<BookMarkEvent> events) {
        if (!Controller.getDefault().isShuttingDown())
          onBookMarksAdded(events);
      }

      @Override
      public void entitiesUpdated(Set<BookMarkEvent> events) {
        if (!Controller.getDefault().isShuttingDown())
          onBookMarksUpdated(events);
      }

      @Override
      public void entitiesDeleted(Set<BookMarkEvent> events) {
        if (!Controller.getDefault().isShuttingDown())
          onBookMarksDeleted(events);
      }
    };

    DynamicDAO.addEntityListener(IBookMark.class, fBookMarkListener);
  }

  private void unregisterListeners() {
    DynamicDAO.removeEntityListener(IBookMark.class, fBookMarkListener);
  }

  private void onBookMarksAdded(Set<BookMarkEvent> events) {
    for (BookMarkEvent event : events) {
      IBookMark addedBookMark = event.getEntity();
      IPreferenceScope entityPreferences = Owl.getPreferenceService().getEntityScope(addedBookMark);

      Long interval = entityPreferences.getLong(DefaultPreferences.BM_UPDATE_INTERVAL);
      boolean autoUpdateState = entityPreferences.getBoolean(DefaultPreferences.BM_UPDATE_INTERVAL_STATE);

      /* BookMark wants to Auto-Update */
      if (autoUpdateState)
        addUpdate(event.getEntity(), interval);
    }
  }

  private void onBookMarksUpdated(Set<BookMarkEvent> events) {
    for (BookMarkEvent event : events) {
      IBookMark updatedBookMark = event.getEntity();
      sync(updatedBookMark);
    }
  }

  private void onBookMarksDeleted(Set<BookMarkEvent> events) {
    for (BookMarkEvent event : events) {
      removeUpdate(event.getEntity());
    }
  }

  /**
   * Synchronizes the reload-service on the given BookMark. Performs no
   * operation in case the given bookmarks update-interval is matching the
   * stored one.
   *
   * @param updatedBookmark The Bookmark to synchronize with the reload-service.
   */
  public void sync(IBookMark updatedBookmark) {
    IPreferenceScope entityPreferences = Owl.getPreferenceService().getEntityScope(updatedBookmark);

    Long oldInterval = fMapBookMarkToInterval.get(updatedBookmark);
    Long newInterval = entityPreferences.getLong(DefaultPreferences.BM_UPDATE_INTERVAL);

    boolean autoUpdateState = entityPreferences.getBoolean(DefaultPreferences.BM_UPDATE_INTERVAL_STATE);

    /* BookMark known to the Service */
    if (oldInterval != null) {

      /* BookMark no longer Auto-Updating */
      if (!autoUpdateState)
        removeUpdate(updatedBookmark);

      /* New Interval different to Old Interval */
      else if (!newInterval.equals(oldInterval)) {
        Job.getJobManager().cancel(updatedBookmark);
        fMapBookMarkToInterval.put(updatedBookmark, newInterval);
        scheduleUpdate(updatedBookmark, newInterval);
      }
    }

    /* BookMark not yet known to the Service and wants to Auto-Update */
    else if (autoUpdateState) {
      addUpdate(updatedBookmark, newInterval);
    }
  }

  private void removeUpdate(IBookMark bookmark) {
    fMapBookMarkToInterval.remove(bookmark);
    Job.getJobManager().cancel(bookmark);
  }

  private void addUpdate(IBookMark bookmark, Long intervalInSeconds) {
    fMapBookMarkToInterval.put(bookmark, intervalInSeconds);
    scheduleUpdate(bookmark, intervalInSeconds);
  }
}