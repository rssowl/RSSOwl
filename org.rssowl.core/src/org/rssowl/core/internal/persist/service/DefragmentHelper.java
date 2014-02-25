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

package org.rssowl.core.internal.persist.service;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.persist.ConditionalGet;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.Label;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.internal.persist.Preference;
import org.rssowl.core.internal.persist.SearchFilter;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.NewsCounterItem;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.LongOperationMonitor;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.query.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefragmentHelper {

  /* Some constants used when defragmenting to a larger block size */
  private static final String DEFRAGMENT_MARKER = "defragment"; //$NON-NLS-1$

  /* Defrag Tasks Work Ticks */
  private static final int DEFRAG_TOTAL_WORK = 10000000; //100% (but don't fill to 100% to leave room for backup)
  private static final int DEFRAG_SUB_WORK_LABELS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_FOLDERS = 500000; //5%
  private static final int DEFRAG_SUB_WORK_BINS = 1000000; //10%
  private static final int DEFRAG_SUB_WORK_FEEDS = 3000000; //30%
  private static final int DEFRAG_SUB_WORK_DESCRIPTIONS = 3000000; //30%
  private static final int DEFRAG_SUB_WORK_PREFERENCES = 100000; //1%
  private static final int DEFRAG_SUB_WORK_FILTERS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_CONDITIONAL_GET = 100000; //1%
  private static final int DEFRAG_SUB_WORK_COUNTERS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_EVENTS = 100000; //1%
  private static final int DEFRAG_SUB_WORK_COMMITT_DESTINATION = 300000; //3%
  private static final int DEFRAG_SUB_WORK_CLOSE_DESTINATION = 100000; //1%
  private static final int DEFRAG_SUB_WORK_CLOSE_SOURCE = 100000; //1%
  private static final int DEFRAG_SUB_WORK_FINISH = 100000; //1%

  private static boolean moveToLargeBlockSize;
  private static ObjectContainer sourceDb;
  private static ObjectContainer destinationDb;
  private static IProgressMonitor fMonitor;

  /**
   * Close databases on defragmentation finish on cancelling
   */
  private static void closeDb() {
    if (sourceDb != null) {
      sourceDb.close();
    }
    if (destinationDb != null) {
      destinationDb.close();
    }
  }

  private static boolean isCanceled() {
    if (!fMonitor.isCanceled()) {
      return false;
    }
    if (moveToLargeBlockSize) { //Must not allow cancellation when migrating from small DB to 2 GB DB
      fMonitor.setTaskName(Messages.DBManager_WAIT_TASK_COMPLETION);
      return false;
    }

    /* Otherwise close Object Containers */
    closeDb();
    return true;
  }

  /**
   * @return the File indicating whether defragment should be run or not.
   */
  private static File getDefragmentFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, DEFRAGMENT_MARKER);
  }

  private static void init() {
    moveToLargeBlockSize = false;
    sourceDb = null;
    destinationDb = null;
    fMonitor = null;
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.deframentation.IDefragmentHelper#defragment
   * (org.rssowl.core.internal.persist.migration.MigrationResult,
   * org.rssowl.core.util.LongOperationMonitor)
   */
  public static void defragmentIfNecessary(LongOperationMonitor progressMonitor) {

    init();
    boolean shouldDefrag = false;

    moveToLargeBlockSize = ConfigurationHelper.moveToLargerBlockSize();
    File defragmentFile = getDefragmentFile();

    /* First: Check if a defragmentation to a larger block size is required */
    if (moveToLargeBlockSize) {
      shouldDefrag = true;
    }
    /* Check if the user asked for defragmentation */
    else if (defragmentFile.exists()) {
      shouldDefrag = true;
    }

    if (defragmentFile.exists() && !defragmentFile.delete()) {
      Activator.getDefault().logError("Failed to delete defragment file", null); //$NON-NLS-1$
    }

    if (shouldDefrag) {
      defragment(progressMonitor);
    }
  }

  private static void defragment(LongOperationMonitor progressMonitor) {

    progressMonitor.beginLongOperation(true);
    String monitorText = Messages.DBManager_PROGRESS_WAIT;
    SubMonitor subMonitor = SubMonitor.convert(progressMonitor, monitorText, DEFRAG_TOTAL_WORK);
    fMonitor = subMonitor.newChild(DEFRAG_TOTAL_WORK);

    /*
     * This should not be needed, but things don't work properly when it's not
     * called.
     */
    fMonitor.beginTask(monitorText, DEFRAG_TOTAL_WORK);
    Activator.safeLogInfo("Start: Database Defragmentation"); //$NON-NLS-1$

    File database = new File(ProfileFileManager.getDBFilePath());
    File defragmentedDatabase = new File(database.getParentFile(), ProfileFileManager.getTempFile());

    if (defragmentedDatabase.exists() && !defragmentedDatabase.delete()) {
      throw new PersistenceException("Failed to delete file: " + defragmentedDatabase); //$NON-NLS-1$
    }

    /* User might have cancelled the operation */
    if (!moveToLargeBlockSize && isCanceled()) {
      Activator.safeLogInfo("Cancelled: Database Defragmentation"); //$NON-NLS-1$
      return;
    }

    /* Defrag */
    fMonitor.subTask(Messages.DEFRAGMENT_IMPROVING_APP_PERFORMANCE);
    copyDatabase(database, defragmentedDatabase);

    if (fMonitor.isCanceled()) {
      Activator.safeLogInfo("Cancelled: Database Defragmentation"); //$NON-NLS-1$
      defragmentedDatabase.delete();
      return;
    }

    /* Create one time backup service */
    fMonitor.subTask(Messages.DBManager_CREATING_DB_BACKUP);
    BackupService backupService = BackupHelper.createScheduledBackupService(null);
    backupService.backup(true, fMonitor);

    if (fMonitor.isCanceled()) {
      Activator.safeLogInfo("Cancelled: Database Defragmentation"); //$NON-NLS-1$
      defragmentedDatabase.delete();
      return;
    }

    /* Rename Defragmented DB to real DB */
    DBHelper.rename(defragmentedDatabase, database);

    /*
     * Create the marker file in case the DB has been migrated to a larger block
     * size
     */
    if (moveToLargeBlockSize) {
      ConfigurationHelper.createLargeBlockSizeMarker();
    }

    /* Finished */
    fMonitor.done();
    fMonitor = null;
    Activator.safeLogInfo("Finished: Database Defragmentation"); //$NON-NLS-1$
  }

  /**
   * Internal method. Made public for testing. Creates a copy of the database
   * that has all essential data structures. At the moment, this means not
   * copying NewsCounter and IConditionalGets since they will be re-populated
   * eventually.
   *
   * @param sourceDbFile source database file
   * @param destinationDbFile destination database file
   */
  private final static void copyDatabase(File sourceDbFile, File destinationDbFile) {
    try {

      /* Open Source DB */
      sourceDb = Db4o.openFile(ConfigurationHelper.createConfiguration(true, false), sourceDbFile.getAbsolutePath());

      /* Open Destination DB */
      Configuration destinationDbConfiguration = ConfigurationHelper.createConfiguration(true, moveToLargeBlockSize);
      destinationDb = Db4o.openFile(destinationDbConfiguration, destinationDbFile.getAbsolutePath());

      /* Copy (Defragment) */
      internalCopyDatabase();
    } finally {
      closeDb();
    }
  }

  private final static void internalCopyDatabase() {

    /* User might have cancelled the operation */
    if (isCanceled()) {
      return;
    }

    // processing labels
    List<Label> labels = new ArrayList<Label>();
    if (!processLabels(labels)) {
      return;
    }

    // processing folders
    if (!processFolders()) {
      return;
    }

    // processing news bins
    if (!processNewsBins()) {
      return;
    }

    // processing feeds
    NewsCounter newsCounter = new NewsCounter();
    if (!processFeeds(newsCounter)) {
      return;
    }

    // updating news counter
    if (!processNewsCounter(newsCounter)) {
      return;
    }

    /* Description */
    if (!processDescriptions()) {
      return;
    }

    /* Preferences */
    if (!processPreferences()) {
      return;
    }

    /* Filter */
    if (!processNewsFilters()) {
      return;
    }

    /* Counter */
    if (!processCounters()) {
      return;
    }

    /* Entity Id By Event Type */
    if (!processEvents()) {
      return;
    }

    /* Conditional Get */
    if (!processConditionalGet()) {
      return;
    }

    sourceDb.close();
    fMonitor.worked(DEFRAG_SUB_WORK_CLOSE_SOURCE);

    if (fMonitor.isCanceled()) {
      // TODO commit ?
      destinationDb.close();
      return;
    }

    fMonitor.subTask("Commit new DB"); //$NON-NLS-1$
    destinationDb.commit();
    fMonitor.worked(DEFRAG_SUB_WORK_COMMITT_DESTINATION);

    /* User might have cancelled the operation */
    if (fMonitor.isCanceled()) {
      destinationDb.close();
      return;
    }

    fMonitor.subTask("Close new DB"); //$NON-NLS-1$
    destinationDb.close();
    fMonitor.worked(DEFRAG_SUB_WORK_CLOSE_DESTINATION);

    if (fMonitor.isCanceled()) {
      return;
    }

    fMonitor.subTask("Garbage Collection"); //$NON-NLS-1$
    System.gc();
    fMonitor.worked(DEFRAG_SUB_WORK_FINISH);
  }

  /**
   * Processing Labels entities (keep in memory to avoid duplicate copies when
   * cascading feed)
   *
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param labels
   * @param monitor
   */
  private static boolean processLabels(List<Label> labels) {
    int available = DEFRAG_SUB_WORK_LABELS;

    ObjectSet<Label> allLabels = sourceDb.query(Label.class);

    if (allLabels.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    // deleting labels with duplicate ids
    Set<Long> ids = new HashSet<Long>();
    for (Label label : allLabels) {
      Long id = label.getId();
      if (ids.contains(id)) {
        continue; // duplicate label id
      }
      ids.add(id);
      labels.add(label);
    }
    int count = allLabels.size() - labels.size();
    if (count > 0) {
      Activator.safeLogInfo(String.format("Found %1 labels with duplicate ids", count)); //$NON-NLS-1$
    }

    int chunk = available / labels.size();
    int i = 1;
    for (Label label : labels) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_LABELS, i, labels.size()));
      i++;
      sourceDb.activate(label, Integer.MAX_VALUE);
      destinationDb.ext().set(label, Integer.MAX_VALUE);
      fMonitor.worked(chunk);
    }

    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processFolders() {
    int available;
    ObjectSet<Folder> allFolders = sourceDb.query(Folder.class);
    available = DEFRAG_SUB_WORK_FOLDERS;
    if (allFolders.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    int chunk = available / allFolders.size();
    int i = 1;
    for (Folder folder : allFolders) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_FOLDERS, i, allFolders.size()));
      i++;

      sourceDb.activate(folder, Integer.MAX_VALUE);
      if (folder.getParent() == null) {
        // adding root folder
        destinationDb.ext().set(folder, Integer.MAX_VALUE);
      }

      fMonitor.worked(chunk);
    }

    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processNewsBins() {
    int available;

    /*
     * We use destinationDb for the query here because we have already copied
     * the NewsBins at this stage and we may need to fix the NewsBin in case it
     * contains stale news refs.
     */
    ObjectSet<NewsBin> allBins = destinationDb.query(NewsBin.class);
    available = DEFRAG_SUB_WORK_BINS;
    if (allBins.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    int chunk = available / allBins.size();
    int i = 1;
    int count = 0;
    for (NewsBin newsBin : allBins) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_NEWSBINS, i, allBins.size()));
      i++;

      destinationDb.activate(newsBin, Integer.MAX_VALUE);
      List<NewsReference> deleteNewsRefs = new ArrayList<NewsReference>(0);

      // check NewsBin for missing and deleted news
      for (NewsReference newsRef : newsBin.getNewsRefs()) {
        if (isCanceled()) {
          return false;
        }

        Query query = sourceDb.query();
        query.constrain(News.class);
        query.descend("fId").constrain(newsRef.getId()); //$NON-NLS-1$
        Iterator<?> newsIt = query.execute().iterator();
        if (!newsIt.hasNext()) {
          Activator.safeLogError("NewsBin " + newsBin + " has reference to news with id: " + newsRef.getId() + ", but that news does not exist.", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          deleteNewsRefs.add(newsRef);
          continue;
        }

        // actually delete news marked as DELETED or hidden
        INews news = (INews) newsIt.next();
        if (!news.isVisible()) {
          deleteNewsRefs.add(newsRef);
          continue;
        }

        // activate news from news bins
        sourceDb.activate(news, Integer.MAX_VALUE);
        destinationDb.ext().set(news, Integer.MAX_VALUE);
      }

      if (!deleteNewsRefs.isEmpty()) {
        if (isCanceled()) {
          return false;
        }

        newsBin.removeNewsRefs(deleteNewsRefs);
        destinationDb.ext().set(newsBin, Integer.MAX_VALUE);

        count += deleteNewsRefs.size();
      }

      fMonitor.worked(chunk);
    }

    if (count > 0) {
      Activator.safeLogInfo("Deleted " + count + " deleted or staled news from NewsBins"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   * @param newsCounter
   */
  private static boolean processFeeds(NewsCounter newsCounter) {
    int available;
    available = DEFRAG_SUB_WORK_FEEDS;
    int feedCounter = 0;
    ObjectSet<Feed> allFeeds = sourceDb.query(Feed.class);
    if (allFeeds.isEmpty()) {
      fMonitor.worked(available);
    }

    int allFeedsSize = allFeeds.size();
    int chunk = available / allFeedsSize;
    int i = 1;

    int count = 0;
    for (Feed feed : allFeeds) {
      if (isCanceled()) {
        return false;
      }

      /* Introduce own label as feed copying can be very time consuming */
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_NEWSFEEDS, i, allFeedsSize));
      i++;

      sourceDb.activate(feed, Integer.MAX_VALUE);
      addNewsCounterItem(newsCounter, feed);

      // check feed before copying
      List<INews> deleteNews = feed.getNewsByStates(EnumSet.of(INews.State.DELETED, INews.State.HIDDEN));
      if (!deleteNews.isEmpty()) {
        if (isCanceled()) {
          return false;
        }

        for (INews news : deleteNews) {
          // TODO correctly delete News and it's persistable fields from DB ????
          sourceDb.delete(news);
          feed.removeNews(news);
        }
        count += deleteNews.size();
      }

      // recreate fields base on news (Category, Author??

      // copy feed to new db
      destinationDb.ext().set(feed, Integer.MAX_VALUE);
      ++feedCounter;
      if (feedCounter % 40 == 0) {
        destinationDb.commit();
        System.gc();
      }

      fMonitor.worked(chunk);
    }
    destinationDb.commit();
    System.gc();

    if (count > 0) {
      Activator.safeLogInfo("Deleted " + count + " HIDDEN or DELETED news from feeds"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return !isCanceled();
  }

  private static void addNewsCounterItem(NewsCounter newsCounter, Feed feed) {
    Map<State, Integer> stateToCountMap = feed.getNewsCount();
    int unreadCount = getCount(stateToCountMap, EnumSet.of(State.NEW, State.UNREAD, State.UPDATED));
    Integer newCount = stateToCountMap.get(INews.State.NEW);
    newsCounter.put(feed.getLink().toString(), new NewsCounterItem(newCount, unreadCount, feed.getStickyCount()));
  }

  private static int getCount(Map<State, Integer> stateToCountMap, Set<State> states) {
    int count = 0;
    for (State state : states) {
      count += stateToCountMap.get(state);
    }
    return count;
  }

  /**
   * @param destinationDb
   * @param monitor
   * @param newsCounter
   */
  private static boolean processNewsCounter(NewsCounter newsCounter) {
    fMonitor.subTask(Messages.DBManager_UPDATING_NEWS_COUNTERS);
    destinationDb.ext().set(newsCounter, Integer.MAX_VALUE);
    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processDescriptions() {
    int available = DEFRAG_SUB_WORK_DESCRIPTIONS;
    int descriptionCounter = 0;
    ObjectSet<Description> allDescriptions = sourceDb.query(Description.class);
    if (allDescriptions.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    int chunk = Math.max(1, available / allDescriptions.size());
    int i = 1;
    int count = 0;
    for (Description description : allDescriptions) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_DESCRIPTIONS, i, allDescriptions.size()));
      i++;

      sourceDb.activate(description, Integer.MAX_VALUE);

      // check for deleted news
      // TODO: first check in for static deleted news id variable
      NewsReference newsRef = description.getNews();

      Query query = sourceDb.query();
      query.constrain(News.class);
      query.descend("fId").constrain(newsRef.getId()); //$NON-NLS-1$
      Iterator<?> newsIt = query.execute().iterator();
      if (!newsIt.hasNext()) {
        // skip this description
        count++;
        continue;
      }

      destinationDb.ext().set(description, Integer.MAX_VALUE);

      ++descriptionCounter;
      if (descriptionCounter % 10000 == 0) {
        destinationDb.commit();
        System.gc();
      }

      fMonitor.worked(chunk);
    }
    destinationDb.commit();
    System.gc();

    if (count > 0) {
      Activator.safeLogInfo("Deleted " + count + " desciptions with missing news entries"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processPreferences() {
    int available;
    available = DEFRAG_SUB_WORK_PREFERENCES;
    ObjectSet<Preference> allPreferences = sourceDb.query(Preference.class);
    if (allPreferences.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    int chunk = available / allPreferences.size();
    int i = 1;
    for (Preference pref : allPreferences) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_PREFERENCES, i, allPreferences.size()));
      i++;

      sourceDb.activate(pref, Integer.MAX_VALUE);
      destinationDb.ext().set(pref, Integer.MAX_VALUE);

      fMonitor.worked(chunk);
    }
    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processNewsFilters() {
    int available = DEFRAG_SUB_WORK_FILTERS;
    ObjectSet<SearchFilter> allFilters = sourceDb.query(SearchFilter.class);
    if (allFilters.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    int chunk = available / allFilters.size();
    int i = 1;
    for (ISearchFilter filter : allFilters) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_NEWSFILTERS, i, allFilters.size()));
      i++;

      sourceDb.activate(filter, Integer.MAX_VALUE);
      destinationDb.ext().set(filter, Integer.MAX_VALUE);
      fMonitor.worked(chunk);
    }
    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processCounters() {
    fMonitor.subTask("Processing Counter"); //$NON-NLS-1$

    List<Counter> counterSet = sourceDb.query(Counter.class);
    Counter counter = counterSet.iterator().next();
    sourceDb.activate(counter, Integer.MAX_VALUE);
    destinationDb.ext().set(counter, Integer.MAX_VALUE);

    fMonitor.worked(DEFRAG_SUB_WORK_COUNTERS);

    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processEvents() {
    fMonitor.subTask("Processing Events"); //$NON-NLS-1$

    EntityIdsByEventType entityIdsByEventType = sourceDb.query(EntityIdsByEventType.class).iterator().next();
    sourceDb.activate(entityIdsByEventType, Integer.MAX_VALUE);
    destinationDb.ext().set(entityIdsByEventType, Integer.MAX_VALUE);

    fMonitor.worked(DEFRAG_SUB_WORK_EVENTS);

    return !isCanceled();
  }

  /**
   * @param sourceDb
   * @param destinationDb
   * @param moveToLargeBlockSize
   * @param monitor
   */
  private static boolean processConditionalGet() {
    int available = DEFRAG_SUB_WORK_CONDITIONAL_GET;
    ObjectSet<ConditionalGet> allConditionalGets = sourceDb.query(ConditionalGet.class);
    if (allConditionalGets.isEmpty()) {
      fMonitor.worked(available);
      return true;
    }

    int chunk = available / allConditionalGets.size();
    int i = 1;
    for (ConditionalGet conditionalGet : allConditionalGets) {
      if (isCanceled()) {
        return false;
      }
      fMonitor.subTask(NLS.bind(Messages.DBManager_OPTIMIZING_CONDITIONAL_GETS, i, allConditionalGets.size()));
      i++;

      sourceDb.activate(conditionalGet, Integer.MAX_VALUE);
      destinationDb.ext().set(conditionalGet, Integer.MAX_VALUE);
      fMonitor.worked(chunk);
    }
    allConditionalGets = null;

    return !isCanceled();
  }

  public static void defragmentOnNextStartup() {
    File defragmentFile = getDefragmentFile();
    if (!defragmentFile.exists()) {
      ProfileFileManager.safeCreate(defragmentFile);
    }
  }

}
