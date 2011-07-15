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

package org.rssowl.core.tests.performance;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.ICloud;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.persist.ITextInput;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.tests.Activator;
import org.rssowl.core.tests.TestUtils;
import org.rssowl.core.util.ITask;
import org.rssowl.core.util.TaskAdapter;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.services.SavedSearchService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author bpasero
 */
public class PerformanceTest {

  /* Define the number of Feeds here (1 - 216) */
  private static final int FEEDS = 200;

  /* Number of Jobs per JobQueue */
  private static final int JOBS = 10;

  private URI fPluginLocation;
  private Controller fController;
  private IModelSearch fModelSearch;

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Before
  public void setUp() throws Exception {
    InternalOwl.PERF_TESTING = true;
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fModelSearch = Owl.getPersistenceService().getModelSearch();
    fPluginLocation = FileLocator.toFileURL(Platform.getBundle("org.rssowl.core.tests").getEntry("/")).toURI();
    fController = Controller.getDefault();
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void realWorldReloadTestWithFilters() throws Exception {
    /*
     * We use 2 to simulate better how things happen. The queue for saving has a
     * single job and there are multiple jobs for loading the online feed and
     * interpreting. However, since the test infrastructure uses a single
     */
    int jobsCount = 2;
    File corpus1Folder = new File(fPluginLocation.resolve("data/performance/corpus_10-03-07"));
    File corpus2Folder = new File(fPluginLocation.resolve("data/performance/corpus_10-06-07"));
    File corpus3Folder = new File(fPluginLocation.resolve("data/performance/corpus_10-09-07"));
    File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
    File feedFolder = new File(tmpFolder.getAbsolutePath(), "rssowlfeeds");
    feedFolder.mkdir();
    feedFolder.deleteOnExit();

    /* Copy Feeds of corpus_10-03-07 to temp location */
    copyFeedFilesToTempLocation(corpus1Folder, feedFolder);

    /* Create Filter */
    {
      IModelFactory factory = Owl.getModelFactory();
      ISearch search = factory.createSearch(null);
      search.setMatchAllConditions(false);
      ISearchField field = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
      search.addSearchCondition(factory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo bar president"));

      field = factory.createSearchField(INews.AUTHOR, INews.class.getName());
      search.addSearchCondition(factory.createSearchCondition(field, SearchSpecifier.IS, "foobar"));

      field = factory.createSearchField(INews.TITLE, INews.class.getName());
      search.addSearchCondition(factory.createSearchCondition(field, SearchSpecifier.IS, "anything"));

      ISearchFilter filter = factory.createSearchFilter(null, search, "Filter 1");
      filter.setEnabled(true);
      filter.setOrder(0);

      filter.addAction(factory.createFilterAction("org.rssowl.core.StopFilterAction"));

      DynamicDAO.save(filter);
    }

    List<ITask> tasks = getRealWorldReloadTasks(feedFolder.getAbsolutePath());

    System.gc();
    /* Initial Reload */
    System.out.println("Reloading Real Word Feeds With Filters: " + FEEDS + " Feeds [Initial - " + jobsCount + " Jobs] took: " + TestUtils.executeAndWait(tasks, jobsCount) + "ms");

    /* Copy Feeds of corpus_10-06-07 to temp location */
    copyFeedFilesToTempLocation(corpus2Folder, feedFolder);
    System.gc();

    /* Second Reload */
    System.out.println("Reloading Real Word Feeds With Filters: " + FEEDS + " Feeds [Second - " + jobsCount + " Jobs] took: " + TestUtils.executeAndWait(tasks, jobsCount) + "ms");

    /* Copy Feeds of corpus_10-06-09 to temp location */
    copyFeedFilesToTempLocation(corpus3Folder, feedFolder);
    System.gc();

    /* Third Reload */
    System.out.println("Reloading Real Word Feeds With Filters: " + FEEDS + " Feeds [Third - " + jobsCount + " Jobs] took: " + TestUtils.executeAndWait(tasks, jobsCount) + "ms");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void realWorldReloadTest() throws Exception {
    /*
     * We use 2 to simulate better how things happen. The queue for saving has a
     * single job and there are multiple jobs for loading the online feed and
     * interpreting. However, since the test infrastructure uses a single
     */
    int jobsCount = 2;
    File corpus1Folder = new File(fPluginLocation.resolve("data/performance/corpus_10-03-07"));
    File corpus2Folder = new File(fPluginLocation.resolve("data/performance/corpus_10-06-07"));
    File corpus3Folder = new File(fPluginLocation.resolve("data/performance/corpus_10-09-07"));
    File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
    File feedFolder = new File(tmpFolder.getAbsolutePath(), "rssowlfeeds");
    feedFolder.mkdir();
    feedFolder.deleteOnExit();

    /* Copy Feeds of corpus_10-03-07 to temp location */
    copyFeedFilesToTempLocation(corpus1Folder, feedFolder);
    List<ITask> tasks = getRealWorldReloadTasks(feedFolder.getAbsolutePath());

    System.gc();
    /* Initial Reload */
    System.out.println("Reloading Real Word Feeds: " + FEEDS + " Feeds [Initial - " + jobsCount + " Jobs] took: " + TestUtils.executeAndWait(tasks, jobsCount) + "ms");

    /* Copy Feeds of corpus_10-06-07 to temp location */
    copyFeedFilesToTempLocation(corpus2Folder, feedFolder);
    System.gc();

    /* Second Reload */
    System.out.println("Reloading Real Word Feeds: " + FEEDS + " Feeds [Second - " + jobsCount + " Jobs] took: " + TestUtils.executeAndWait(tasks, jobsCount) + "ms");

    /* Copy Feeds of corpus_10-06-09 to temp location */
    copyFeedFilesToTempLocation(corpus3Folder, feedFolder);
    System.gc();

	/* Third Reload */
    System.out.println("Reloading Real Word Feeds: " + FEEDS + " Feeds [Third - " + jobsCount + " Jobs] took: " + TestUtils.executeAndWait(tasks, jobsCount) + "ms");
  }

  private void copyFeedFilesToTempLocation(File corpusFolder, File feedFolder) throws FileNotFoundException, IOException {
    File[] feedFiles = feedFolder.listFiles();
    for (File file : feedFiles) {
      file.delete();
    }

    FileFilter filter = new FileFilter() {
      public boolean accept(File pathname) {
       return pathname.getName().contains("zip");
      }
    };

    File zipFile = corpusFolder.listFiles(filter)[0];
    FileInputStream fis = new FileInputStream(zipFile);
    ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
    ZipEntry entry;
    while ((entry = zin.getNextEntry()) != null) {
      File outputFile = new File(feedFolder, entry.getName());
      outputFile.deleteOnExit();

      FileOutputStream outS = new FileOutputStream(outputFile);
      copy(zin, outS);
    }
  }

  private static void copy(ZipInputStream zis, OutputStream fos) {
    try {
      int count;
      final int BUFFER = 2048;
      byte data[] = new byte[BUFFER];

      BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
      while ((count = zis.read(data, 0, BUFFER)) != -1) {
        dest.write(data, 0, count);
      }

      dest.flush();
      dest.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<ITask> getRealWorldReloadTasks(String feedDirectory) {
    List<ITask> tasks = new ArrayList<ITask>();

    /* Create Folder with all Bookmarks */
    IFolder rootFolder = new Folder(null, null, "Root");
    for (int i = 1; i < FEEDS + 1; i++) {
      try {
        File file = new File(feedDirectory, i + ".xml");
        if (!file.exists())
          continue;

        URI feedLink = file.toURI();

        IFeed feed = new Feed(feedLink);

        feed = DynamicDAO.save(feed);

        IBookMark bookmark = new BookMark(null, rootFolder, new FeedLinkReference(feed.getLink()), "Bookmark");
        IPreferenceScope entityScope = Owl.getPreferenceService().getEntityScope(bookmark);
        entityScope.putInteger(DefaultPreferences.DEL_NEWS_BY_COUNT_VALUE, 50);
        rootFolder.addMark(bookmark, null, false);
        DynamicDAO.save(rootFolder);
      } catch (Exception e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    /* Create Tasks */
    List<IMark> marks = rootFolder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark) {
        final IBookMark bookmark = (IBookMark) mark;

        tasks.add(new TaskAdapter() {
          public IStatus run(IProgressMonitor monitor) {
            return fController.reload(bookmark, null, new NullProgressMonitor());
          }
        });
      }
    }

    return tasks;
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void savedSearchServiceTest() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();

    /* Cold-Start: Run Saved Search Service on 216 Feeds */
    List<ITask> tasks = getSavedSearchServiceTestTasks(ex);
    System.out.println("Running Saved Search Service on " + FEEDS + " Feeds [Cold - " + 1 + " Jobs] took: " + TestUtils.executeAndWait(tasks, 1) + "ms");

    /* Warm-Start: Run Saved Search Service on 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSavedSearchServiceTestTasks(ex);
    long l1 = TestUtils.executeAndWait(tasks, 1);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSavedSearchServiceTestTasks(ex);
    long l2 = TestUtils.executeAndWait(tasks, 1);

    System.out.println("Running Saved Search Service on " + FEEDS + " Feeds [Warm - " + 1 + " Jobs] took: " + (l1 + l2) / 2 + "ms");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  List<ITask> getSavedSearchServiceTestTasks(final List<Exception> ex) {
    List<ITask> tasks = new ArrayList<ITask>();

    /* Create Saved Searches */
    createSavedSearches();

    /* Save some Feeds first */
    saveFeedsHelper();

    final SavedSearchService smService = new SavedSearchService();

    tasks.add(new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        try {
          smService.updateSavedSearches(DynamicDAO.loadAll(ISearchMark.class));
          smService.stopService();
        } catch (Exception e) {
          ex.add(e);
        }
        return Status.OK_STATUS;
      }
    });

    return tasks;
  }

  @SuppressWarnings("unused")
  private void createSavedSearches() {
    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Default");
    IModelFactory factory = Owl.getModelFactory();

    /* Create Default SearchMarks */
    String newsEntityName = INews.class.getName();

    /* SearchCondition: New and Updated News */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "New and Updated News");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(State.NEW));
    }

    /* SearchCondition: Recent News */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Recent News");

      ISearchField field1 = factory.createSearchField(INews.AGE_IN_DAYS, newsEntityName);
      factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS_LESS_THAN, 2);
    }

    /* SearchCondition: News with Attachments */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "News with Attachments");

      ISearchField field = factory.createSearchField(INews.HAS_ATTACHMENTS, newsEntityName);
      factory.createSearchCondition(null, mark, field, SearchSpecifier.IS, true);
    }

    /* SearchCondition: Sticky News */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Sticky News");

      ISearchField field = factory.createSearchField(INews.IS_FLAGGED, newsEntityName);
      factory.createSearchCondition(null, mark, field, SearchSpecifier.IS, true);
    }

    /*
     * Condition : +(State IS *new* OR State is *unread* OR State IS *updated*)
     * AND +((Entire News contains "Foo") OR Author is "Benjamin Pasero")
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(IEntity.ALL_FIELDS, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.CONTAINS, "Foo");

      ISearchField field5 = factory.createSearchField(INews.AUTHOR, newsEntityName);
      ISearchCondition cond5 = factory.createSearchCondition(null, mark, field5, SearchSpecifier.IS, "Benjamin Pasero");
    }

    /*
     * Condition : +(State IS *new* OR State is *unread* OR State IS *updated*)
     * AND (Entire News contains "Foo") AND (Author is "Benjamin Pasero")
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(IEntity.ALL_FIELDS, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.CONTAINS, "Foo");

      ISearchField field5 = factory.createSearchField(INews.AUTHOR, newsEntityName);
      ISearchCondition cond5 = factory.createSearchCondition(null, mark, field5, SearchSpecifier.CONTAINS, "Benjamin Pasero");
    }

    /*
     * Condition : (Entire News contains "Foo") AND (Title contains "Bar") AND
     * (Author is not "Benjamin Pasero")
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(IEntity.ALL_FIELDS, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.CONTAINS, "fafa");

      ISearchField field2 = factory.createSearchField(INews.TITLE, newsEntityName);
      ISearchCondition cond2 = factory.createSearchCondition(null, mark, field2, SearchSpecifier.CONTAINS, "Bar");

      ISearchField field3 = factory.createSearchField(INews.AUTHOR, newsEntityName);
      ISearchCondition cond3 = factory.createSearchCondition(null, mark, field3, SearchSpecifier.IS_NOT, "Benjamin Pasero");
    }

    /*
     * Condition : +(State IS *new* OR State is *unread* OR State IS *updated*)
     * AND (Category IS Windows) AND (Category IS Apple)
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(INews.CATEGORIES, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.IS, "windows");

      ISearchField field5 = factory.createSearchField(INews.CATEGORIES, newsEntityName);
      ISearchCondition cond5 = factory.createSearchCondition(null, mark, field5, SearchSpecifier.IS, "apple");
    }

    /*
     * Condition : +(State IS *new* OR State is *unread* OR State IS *updated*)
     * AND (Category IS Windows) AND (Category IS Apple) AND (Category IS NOT
     * Slashdot)
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(INews.CATEGORIES, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.IS, "windows");

      ISearchField field5 = factory.createSearchField(INews.CATEGORIES, newsEntityName);
      ISearchCondition cond5 = factory.createSearchCondition(null, mark, field5, SearchSpecifier.IS, "apple");

      ISearchField field6 = factory.createSearchField(INews.CATEGORIES, newsEntityName);
      factory.createSearchCondition(null, mark, field6, SearchSpecifier.IS_NOT, "slashdot");
    }

    /*
     * Condition : +(State IS *new* OR State is *unread* OR State IS *updated*)
     * AND (Category IS NOT Windows)
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(INews.CATEGORIES, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.IS_NOT, "windows");
    }

    /*
     * Condition : +(State IS *new* OR State is *unread* OR State IS *updated*)
     * AND (Age is Less than 5 Days)
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(INews.AGE_IN_DAYS, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.IS_LESS_THAN, 5);
    }

    /*
     * Condition 5: (State IS *new* OR State is *unread* OR State IS *updated*)
     * AND All_Fields CONTAINS foo
     */
    {
      ISearchMark mark = factory.createSearchMark(null, folder, "Complex Search");

      ISearchField field1 = factory.createSearchField(INews.STATE, newsEntityName);
      ISearchCondition cond1 = factory.createSearchCondition(null, mark, field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED));

      ISearchField field4 = factory.createSearchField(IEntity.ALL_FIELDS, newsEntityName);
      ISearchCondition cond4 = factory.createSearchCondition(null, mark, field4, SearchSpecifier.CONTAINS, "pasero");
    }

    DynamicDAO.save(folder);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void saveAndIndexFeeds() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();

    /* Cold-Start: Save and Index 216 Feeds */
    List<ITask> tasks = getSaveAndIndexFeedsTasks(ex);
    System.out.println("Saving and Indexing " + FEEDS + " Feeds [Cold - " + 1 + " Jobs] took: " + TestUtils.executeAndWait(tasks, 1) + "ms");

    /* Warm-Start: Save and Index 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSaveAndIndexFeedsTasks(ex);
    long l1 = TestUtils.executeAndWait(tasks, 1);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSaveAndIndexFeedsTasks(ex);
    long l2 = TestUtils.executeAndWait(tasks, 1);

    System.out.println("Saving and Indexing " + FEEDS + " Feeds [Warm - " + 1 + " Jobs] took: " + (l1 + l2) / 2 + "ms");

    /* Warm-Start: Save 216 Feeds with search disabled (to calculate plain index time) */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    Owl.getPersistenceService().getModelSearch().shutdown(false);
    tasks = getSaveAndIndexFeedsTasks(ex);
    long l3 = TestUtils.executeAndWait(tasks, 1);

    Owl.getPersistenceService().getModelSearch().startup();
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    Owl.getPersistenceService().getModelSearch().shutdown(false);
    tasks = getSaveAndIndexFeedsTasks(ex);
    long l4 = TestUtils.executeAndWait(tasks, 1);

    /* Switch model search back on at the end */
    Owl.getPersistenceService().getModelSearch().startup();

    long indexL = (l1 + l2) / 2 - (l3 + l4) / 2;

    System.out.println("Indexing " + FEEDS + " Feeds [Warm - " + 1 + " Jobs] took: " + indexL + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  private List<ITask> getSaveAndIndexFeedsTasks(final List<Exception> ex) {
    List<ITask> tasks = new ArrayList<ITask>();
    List<IFeed> feeds = interpretFeedsHelper();

    for (final IFeed feed : feeds) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            DynamicDAO.save(feed);
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    return tasks;
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void searchNews() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();
    final int[] results = new int[] { 0 };
    final IModelFactory factory = Owl.getModelFactory();
    List<ITask> tasks = new ArrayList<ITask>();
    fModelSearch.startup();

    /* Save some Feeds first */
    saveFeedsHelper();

    /* Query 1: News is *new*, *unread*, *updated*, *read* */
    ITask task = new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

        ISearchField field1 = factory.createSearchField(INews.STATE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, State.UNREAD, State.UPDATED, State.READ)));

        results[0] = fModelSearch.searchNews(conditions, false).size();

        return Status.OK_STATUS;
      }
    };
    tasks.clear();
    tasks.add(task);
    long l1 = TestUtils.executeAndWait(tasks, 1);
    System.out.println("Searching [States (" + results[0] + " results, Occur.SHOULD, Cold)] in " + FEEDS + " Feeds took: " + l1 + "ms");

    /* Recreate */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    saveFeedsHelper();

    /* Query 2: Entire News contains 'news' */
    task = new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

        ISearchField field1 = factory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
        conditions.add(factory.createSearchCondition(field1, SearchSpecifier.CONTAINS, "news"));

        results[0] = fModelSearch.searchNews(conditions, false).size();

        return Status.OK_STATUS;
      }
    };
    tasks.clear();
    tasks.add(task);
    System.gc();
    long l2 = TestUtils.executeAndWait(tasks, 1);
    System.out.println("Searching [Entire News (" + results[0] + " results, Occur.SHOULD, Cold)] in " + FEEDS + " Feeds took: " + l2 + "ms");

    /* Recreate */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    saveFeedsHelper();

    /*
     * Query 3: Title contains 'news' OR Author contains 's*' OR Category begins
     * with 'e'
     */
    task = new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

        ISearchField field1 = factory.createSearchField(INews.TITLE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field1, SearchSpecifier.CONTAINS, "news"));

        ISearchField field2 = factory.createSearchField(INews.AUTHOR, INews.class.getName());
        conditions.add(factory.createSearchCondition(field2, SearchSpecifier.CONTAINS, "s*"));

        ISearchField field3 = factory.createSearchField(INews.CATEGORIES, INews.class.getName());
        conditions.add(factory.createSearchCondition(field3, SearchSpecifier.BEGINS_WITH, "e"));

        results[0] = fModelSearch.searchNews(conditions, false).size();

        return Status.OK_STATUS;
      }
    };
    tasks.clear();
    tasks.add(task);
    System.gc();
    long l3 = TestUtils.executeAndWait(tasks, 1);
    System.out.println("Searching [Title, Author, Categories (" + results[0] + " results, Occur.SHOULD, Cold)] in " + FEEDS + " Feeds took: " + l3 + "ms");

    /* Recreate */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    saveFeedsHelper();

    /*
     * Query 4: Title contains 'news' AND Author contains 's*' AND Category
     * begins with 'e'
     */
    task = new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

        ISearchField field1 = factory.createSearchField(INews.TITLE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field1, SearchSpecifier.CONTAINS, "news"));

        ISearchField field2 = factory.createSearchField(INews.AUTHOR, INews.class.getName());
        conditions.add(factory.createSearchCondition(field2, SearchSpecifier.CONTAINS, "s*"));

        ISearchField field3 = factory.createSearchField(INews.CATEGORIES, INews.class.getName());
        conditions.add(factory.createSearchCondition(field3, SearchSpecifier.BEGINS_WITH, "e"));

        results[0] = fModelSearch.searchNews(conditions, true).size();

        return Status.OK_STATUS;
      }
    };
    tasks.clear();
    tasks.add(task);
    System.gc();
    long l4 = TestUtils.executeAndWait(tasks, 1);
    System.out.println("Searching [Title, Author, Categories (" + results[0] + " results, Occur.MUST, Cold)] in " + FEEDS + " Feeds took: " + l4 + "ms");

    /* Recreate */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    saveFeedsHelper();

    /* Query 5: Publish Date before Now AND After 2000 */
    task = new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

        ISearchField field1 = factory.createSearchField(INews.PUBLISH_DATE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field1, SearchSpecifier.IS_BEFORE, new Date()));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);

        ISearchField field2 = factory.createSearchField(INews.PUBLISH_DATE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field2, SearchSpecifier.IS_AFTER, cal.getTime()));

        results[0] = fModelSearch.searchNews(conditions, true).size();

        return Status.OK_STATUS;
      }
    };
    tasks.clear();
    tasks.add(task);
    System.gc();
    long l5 = TestUtils.executeAndWait(tasks, 1);
    System.out.println("Searching [Date Range (" + results[0] + " results, Occur.MUST, Cold)] in " + FEEDS + " Feeds took: " + l5 + "ms");

    /* Recreate */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    saveFeedsHelper();

    /* Query 6: News is *new*, *unread*, *updated* and Has Attachments IS TRUE */
    task = new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

        ISearchField field1 = factory.createSearchField(INews.STATE, INews.class.getName());
        conditions.add(factory.createSearchCondition(field1, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)));

        ISearchField field4 = factory.createSearchField(INews.HAS_ATTACHMENTS, INews.class.getName());
        conditions.add(factory.createSearchCondition(field4, SearchSpecifier.IS, true));

        results[0] = fModelSearch.searchNews(conditions, false).size();

        return Status.OK_STATUS;
      }
    };
    tasks.clear();
    tasks.add(task);
    long l6 = TestUtils.executeAndWait(tasks, 1);
    System.out.println("Searching [States, Has Attachments (" + results[0] + " results, Occur.SHOULD, Cold)] in " + FEEDS + " Feeds took: " + l6 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void reloadFeedsNoRetention() throws Exception {
    List<ITask> tasks = getReloadFeedsTasks(false);

    /* Cold-Start: Interpret 216 Feeds */
    System.out.println("Reloading Feeds (No Retention): " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getReloadFeedsTasks(false);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getReloadFeedsTasks(false);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Reloading Feeds (No Retention): " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void reloadFeedsWithRetention() throws Exception {
    List<ITask> tasks = getReloadFeedsTasks(true);

    /* Cold-Start: Interpret 216 Feeds */
    System.out.println("Reloading Feeds (With Retention): " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getReloadFeedsTasks(true);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getReloadFeedsTasks(true);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Reloading Feeds (With Retention): " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");
  }

  private List<ITask> getReloadFeedsTasks(boolean withRetention) {
    List<ITask> tasks = new ArrayList<ITask>();
    Random rand = new Random();

    /* Create Folder with all Bookmarks */
    IFolder rootFolder = new Folder(null, null, "Root");
    for (int i = 1; i < FEEDS + 1; i++) {
      try {
        URI feedLink = fPluginLocation.resolve("data/performance/" + i + ".xml").toURL().toURI();

        IFeed feed = new Feed(feedLink);

        if (withRetention) {
          for (int j = 0; j < 10; j++) {
            INews news = new News(feed);
            news.setTitle("Random Title " + j);
            news.setDescription(getLongDecription());
            news.setLink(new URI("http://www." + System.currentTimeMillis() + ".com/" + rand.nextInt(10000) + j));
            news.setComments("Comments");
            news.setState(INews.State.READ);
            feed.addNews(news);
          }
        }

        feed = DynamicDAO.save(feed);

        IBookMark bookmark = new BookMark(null, rootFolder, new FeedLinkReference(feed.getLink()), "Bookmark");

        if (withRetention)
          Owl.getPreferenceService().getEntityScope(bookmark).putBoolean(DefaultPreferences.DEL_READ_NEWS_STATE, true);

        rootFolder.addMark(bookmark, null, false);
        DynamicDAO.save(rootFolder);
      } catch (Exception e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    /* Create Tasks */
    List<IMark> marks = rootFolder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark) {
        final IBookMark bookmark = (IBookMark) mark;

        tasks.add(new TaskAdapter() {
          public IStatus run(IProgressMonitor monitor) {
            return fController.reload(bookmark, null, new NullProgressMonitor());
          }
        });
      }
    }

    return tasks;
  }

  private String getLongDecription() {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < 10; i++)
      str.append("Hello World<br>");

    return str.toString();
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void setAllNewsStateWithEquiUpdateAndResolveFeed() throws Exception {
    List<ITask> tasks = getSetNewsStateTask(true, true, true);

    /* Cold-Start: Interpret 216 Feeds */
    String message = "Setting all news state by resolving feed (Update Equivalent): " + FEEDS + " Feeds ";
    System.out.println(message + "[Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Interpret 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, true, true);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, true, true);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println(message + "[Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms/n");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void setNewsStateWithEquiUpdateAndResolveFeed() throws Exception {
    List<ITask> tasks = getSetNewsStateTask(true, true, false);

    /* Cold-Start: Interpret 216 Feeds */
    String message = "Setting 1/5 news state by resolving feed (Update Equivalent): " + FEEDS + " Feeds ";
    System.out.println(message + "[Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Interpret 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, true, false);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, true, false);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println(message + "[Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms/n");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void setAllNewsStateWithEquiUpdate() throws Exception {
    List<ITask> tasks = getSetNewsStateTask(true, false, true);

    /* Cold-Start: Interpret 216 Feeds */
    String message = "Setting all news state (Update Equivalent): " + FEEDS + " Feeds ";
    System.out.println(message + "[Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Interpret 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, false, true);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, false, true);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println(message + "[Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms/n");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void setNewsStateWithEquiUpdate() throws Exception {
    List<ITask> tasks = getSetNewsStateTask(true, false, false);

    /* Cold-Start: Interpret 216 Feeds */
    String message = "Setting 1/5 news state (Update Equivalent): " + FEEDS + " Feeds ";
    System.out.println(message + "[Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Interpret 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, false, false);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(true, false, false);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println(message + "[Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms/n");
  }

  private List<ITask> getSetNewsStateTask(final boolean updateEquivalent, final boolean resolveFeed, boolean allNewsNew) {

    final EnumSet<INews.State> unreadStates = EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED);
    /* Save some Feeds first */
    List<IFeed> feeds = interpretFeedsHelper();
    final List<FeedLinkReference> feedRefs = new ArrayList<FeedLinkReference>();
    for (IFeed feed : feeds) {
      List<INews> news = feed.getNews();
      if (!allNewsNew && (!news.isEmpty())) {
        /* Make 4/5 of the INews READ */
        int endIndex = news.size() * 4 / 5;
        for (int i = 0; i < endIndex; ++i) {
          news.get(i).setState(INews.State.READ);
        }
      }
      feedRefs.add(new FeedLinkReference(DynamicDAO.save(feed).getLink()));
    }

    feeds = null;
    System.gc();

    final INewsDAO newsDAO = DynamicDAO.getDAO(INewsDAO.class);
    List<ITask> tasks = new ArrayList<ITask>();
    tasks.add(new TaskAdapter() {
      public IStatus run(IProgressMonitor monitor) {
        List<INews> news = new ArrayList<INews>();
        for (FeedLinkReference feedRef : feedRefs) {
          if (resolveFeed) {
            news.addAll(feedRef.resolve().getNewsByStates(unreadStates));
          } else {
            news.addAll(newsDAO.loadAll(feedRef, unreadStates));
          }
        }

        newsDAO.setState(news, INews.State.HIDDEN, updateEquivalent, false);

        return Status.OK_STATUS;
      }
    });

    return tasks;
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void setNewsStateWithoutEquiUpdate() throws Exception {
    List<ITask> tasks = getSetNewsStateTask(false, true, true);

    /* Cold-Start: Interpret 216 Feeds */
    System.out.println("Setting news state (Ignore Equivalent): " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Interpret 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(false, true, true);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSetNewsStateTask(false, true, true);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Setting news state (Ignore Equivalent): " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void interpretFeeds() throws Exception {
    List<ITask> tasks = new ArrayList<ITask>();
    final List<Exception> ex = new ArrayList<Exception>();

    /* Prepare Tasks */
    for (int i = 1; i < FEEDS + 1; i++) {
      final int c = i;
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            URI feedLink = fPluginLocation.resolve("data/performance/" + c + ".xml").toURL().toURI();
            IFeed feed = new Feed(feedLink);

            InputStream inS = loadFileProtocol(feed.getLink());
            Owl.getInterpreter().interpret(inS, feed, null);
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    /* Cold-Start: Interpret 216 Feeds */
    System.out.println("Interpreting " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Interpret 216 Feeds */
    long l1 = TestUtils.executeAndWait(tasks, JOBS);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Interpreting " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void saveFeeds() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();

    /* Cold-Start: Save 216 Feeds */
    List<ITask> tasks = getSaveFeedsTasks(ex);
    System.out.println("Saving " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Save 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSaveFeedsTasks(ex);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getSaveFeedsTasks(ex);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Saving " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  private List<ITask> getSaveFeedsTasks(final List<Exception> ex) {
    List<ITask> tasks = new ArrayList<ITask>();
    List<IFeed> feeds = interpretFeedsHelper();

    for (final IFeed feed : feeds) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            DynamicDAO.save(feed);
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    return tasks;
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void resolveFeeds() throws Exception {
    List<ITask> tasks = new ArrayList<ITask>();
    final List<Exception> ex = new ArrayList<Exception>();

    /* Prepare Tasks */
    List<FeedReference> feedRefs = saveFeedsHelper();
    for (final FeedReference feedRef : feedRefs) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            accessAllFields(feedRef.resolve());
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    /* Cold-Start: Resolve 216 Feeds */
    System.gc();
    System.out.println("Resolving " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Resolve 216 Feeds */
    long l1 = TestUtils.executeAndWait(tasks, JOBS);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Resolving " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void resolveFeedsAndNews() throws Exception {
    List<ITask> tasks = new ArrayList<ITask>();
    final List<Exception> ex = new ArrayList<Exception>();

    /* Prepare Tasks */
    List<FeedReference> feedRefs = saveFeedsHelper();
    for (final FeedReference feedRef : feedRefs) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            IFeed feed = feedRef.resolve();
            accessAllFields(feed);

            Collection<INews> news = feed.getNews();
            for (INews newsItem : news)
              accessAllFields(newsItem);
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    /* Cold-Start: Resolve 216 Feeds and all News */
    System.gc();
    System.out.println("Resolving " + FEEDS + " Feeds with all News [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Resolve 216 Feeds and all News */
    long l1 = TestUtils.executeAndWait(tasks, JOBS);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Resolving " + FEEDS + " Feeds with all News [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void resolveNewsStatesByResolving() throws Exception {
    List<ITask> tasks = new ArrayList<ITask>();
    final List<Exception> ex = new ArrayList<Exception>();

    /* Prepare Tasks */
    List<FeedReference> feedRefs = saveFeedsHelper();
    for (final FeedReference feedRef : feedRefs) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            IFeed feed = feedRef.resolve();

            Collection<INews> news = feed.getNews();
            for (INews newsItem : news)
              newsItem.getState();
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    /* Cold-Start: Resolve 216 Feeds and all News */
    System.gc();
    System.out.println("Resolving News-States of " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Resolve 216 Feeds and all News */
    long l1 = TestUtils.executeAndWait(tasks, JOBS);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Resolving News-States of " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void resolveFeedCompletly() throws Exception {
    List<ITask> tasks = new ArrayList<ITask>();
    final List<Exception> ex = new ArrayList<Exception>();

    /* Prepare Tasks */
    List<FeedReference> feedRefs = saveFeedsHelper();
    for (final FeedReference feedRef : feedRefs) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          accessAllFields(ex, feedRef);
          return Status.OK_STATUS;
        }
      });
    }

    /* Cold-Start: Resolve 216 Feeds and all News */
    System.gc();
    System.out.println("Resolving " + FEEDS + " Feeds with all News completly [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Resolve 216 Feeds and all News */
    long l1 = TestUtils.executeAndWait(tasks, JOBS);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Resolving " + FEEDS + " Feeds with all News completly [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  private void accessAllFields(final List<Exception> ex, final FeedReference feedRef) {
    try {
      IFeed feed = feedRef.resolve();

      if (feed.getAuthor() != null)
        accessAllFields(feed.getAuthor());

      List<ICategory> feedCats = feed.getCategories();
      for (ICategory category : feedCats)
        accessAllFields(category);

      if (feed.getImage() != null)
        accessAllFields(feed.getImage());

      accessAllFields(feed);

      Collection<INews> newsList = feed.getNews();
      for (INews news : newsList) {
        accessAllFields(news);

        List<IAttachment> attachments = news.getAttachments();
        for (IAttachment attachment : attachments)
          accessAllFields(attachment);

        if (news.getAuthor() != null)
          accessAllFields(news.getAuthor());

        List<ICategory> categories = news.getCategories();
        for (ICategory category : categories)
          accessAllFields(category);

        if (news.getGuid() != null)
          accessAllFields(news.getGuid());

        if (news.getSource() != null)
          accessAllFields(news.getSource());

        for (ILabel label : news.getLabels())
          accessAllFields(label);
      }
    } catch (Exception e) {
      ex.add(e);
    }
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void deleteFeeds() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();

    /* Cold-Start: Delete 216 Feeds */
    List<ITask> tasks = getDeleteFeedsTasks(ex);
    System.out.println("Deleting " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Delete 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getDeleteFeedsTasks(ex);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getDeleteFeedsTasks(ex);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Deleting " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  private List<ITask> getDeleteFeedsTasks(final List<Exception> ex) throws PersistenceException {
    List<ITask> tasks = new ArrayList<ITask>();

    List<FeedReference> feedRefs = saveFeedsHelper();
    for (final FeedReference feedRef : feedRefs) {
      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            DynamicDAO.delete(feedRef.resolve());
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    return tasks;
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void updateFeeds() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();

    /* Cold-Start: Update 216 Feeds */
    List<ITask> tasks = getUpdateFeedsTasks(ex);
    System.out.println("Updating " + FEEDS + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Update 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getUpdateFeedsTasks(ex);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getUpdateFeedsTasks(ex);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Updating " + FEEDS + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");
    if (ex.size() > 0)
      throw ex.get(0);
  }

  @SuppressWarnings("nls")
  private List<ITask> getUpdateFeedsTasks(final List<Exception> ex) throws PersistenceException {
    List<ITask> tasks = new ArrayList<ITask>();

    /* Save some Feeds first */
    List<IFeed> feeds = interpretFeedsHelper();
    List<IFeed> savedFeeds = new ArrayList<IFeed>(feeds.size());
    for (IFeed feed : feeds)
      savedFeeds.add(DynamicDAO.save(feed));

    /* Interpret them again for updates */
    List<IFeed> newFeeds = interpretFeedsHelper();

    // To test pure updates on pure detached entities, replace newFeeds
    // with savedFeeds and comment out the existingFeed[0].merge(feed) inside
    // the task
    for (final IFeed feed : newFeeds) {
      final IFeed[] existingFeed = new IFeed[1];
      for (IFeed savedFeed : savedFeeds) {
        if (savedFeed.getLink().toString().equals(feed.getLink().toString())) {
          existingFeed[0] = savedFeed;

          // Enable this if you want to eliminate the Feed#merge time from
          // the test, must also comment this same line inside the task
          // existingFeed[0].merge(feed);
          // break;
        }
      }

      tasks.add(new TaskAdapter() {
        public IStatus run(IProgressMonitor monitor) {
          try {
            // Uncomment this line and comment the following one to use load the
            // full feed before the merge
            // existingFeed[0] =
            // feedManager.loadFullEntity(existingFeed[0].getId());
            existingFeed[0] = DynamicDAO.load(IFeed.class, existingFeed[0].getId());
            feed.setCopyright("The new Copyright");
            feed.setDescription("The Description has changed as well");
            feed.setPublishDate(new Date());

            if (feed.getNews().size() > 0) {
              feed.getNews().get(0).setDescription("Some new news description");
            }

            Owl.getModelFactory().createNews(null, feed, new Date()).setTitle("The New News");

            // Uncomment this line and comment out the next two to test the
            // performance of setting the id on a new entity to force
            // an update
            // DynamicDAO.save(feed);

            existingFeed[0].merge(feed);
            DynamicDAO.save(existingFeed[0]);
          } catch (Exception e) {
            ex.add(e);
          }
          return Status.OK_STATUS;
        }
      });
    }

    return tasks;
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("nls")
  @Test
  public void resolveSaveFeeds() throws Exception {
    final List<Exception> ex = new ArrayList<Exception>();

    /* Cold-Start: Save and Resolve 216 Feeds */
    List<ITask> tasks = getResolveSaveFeedsTasks(ex);
    System.gc();
    System.out.println("Saving/Resolving " + FEEDS / 2 + " Feeds [Cold - " + JOBS + " Jobs] took: " + TestUtils.executeAndWait(tasks, JOBS) + "ms");

    /* Warm-Start: Save and Resolve 216 Feeds */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getResolveSaveFeedsTasks(ex);
    long l1 = TestUtils.executeAndWait(tasks, JOBS);

    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    tasks = getResolveSaveFeedsTasks(ex);
    long l2 = TestUtils.executeAndWait(tasks, JOBS);

    System.out.println("Saving/Resolving " + FEEDS / 2 + " Feeds [Warm - " + JOBS + " Jobs] took: " + (l1 + l2) / 2 + "ms\n");

    if (ex.size() > 0)
      throw ex.get(0);
  }

  private List<ITask> getResolveSaveFeedsTasks(final List<Exception> ex) throws PersistenceException {
    List<ITask> tasks = new ArrayList<ITask>();

    /* Save some Feeds first */
    final List<FeedReference> feedRefs = new ArrayList<FeedReference>();
    final List<IFeed> feeds = interpretFeedsHelper();
    final int limit = feeds.size() / 2;
    for (int i = 0; i < limit; i++)
      feedRefs.add(new FeedReference(DynamicDAO.save(feeds.get(i)).getId()));

    /* Tasks to resolve Feeds */
    for (int i = 0; i < limit; i++) {
      final int a = i;

      /* Add Task to save a Feed (88%) */
      if (a % 8 != 0) {
        tasks.add(new TaskAdapter() {
          public IStatus run(IProgressMonitor monitor) {
            try {
              DynamicDAO.save(feeds.get(a + limit));
            } catch (Exception e) {
              ex.add(e);
            }
            return Status.OK_STATUS;
          }
        });
      }

      /* Add Task to resolve a Feed (12%) */
      if (a % 8 == 0) {
        tasks.add(new TaskAdapter() {
          public IStatus run(IProgressMonitor monitor) {
            accessAllFields(ex, feedRefs.get(a));
            return Status.OK_STATUS;
          }
        });
      }
    }

    return tasks;
  }

  private List<FeedReference> saveFeedsHelper() throws PersistenceException {
    List<IFeed> feeds = interpretFeedsHelper();
    List<FeedReference> feedRefs = new ArrayList<FeedReference>();
    for (IFeed feed : feeds)
      feedRefs.add(new FeedReference(DynamicDAO.save(feed).getId()));

    return feedRefs;
  }

  @SuppressWarnings("nls")
  private List<IFeed> interpretFeedsHelper() {
    List<IFeed> feeds = new ArrayList<IFeed>();
    for (int i = 1; i < FEEDS + 1; i++) {
      try {
        URI feedLink = fPluginLocation.resolve("data/performance/" + i + ".xml").toURL().toURI();
        IFeed feed = new Feed(feedLink);

        InputStream inS = loadFileProtocol(feed.getLink());
        Owl.getInterpreter().interpret(inS, feed, null);

        feeds.add(feed);
      } catch (Exception e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    return feeds;
  }

  private void accessAllFields(IPersistable type) {
    if (type instanceof IEntity) {
      IEntity entity = (IEntity) type;
      entity.getId();
      Map<String, ?> properties = entity.getProperties();
      if (properties != null) {
        Set<String> keys = properties.keySet();
        for (String string : keys)
          properties.get(string);
      }
    }

    if (type instanceof IFeed) {
      IFeed feed = (IFeed) type;
      feed.getBase();
      feed.getCopyright();
      feed.getDaysToSkip();
      feed.getDescription();
      feed.getDocs();
      feed.getFormat();
      feed.getGenerator();
      feed.getHomepage();
      feed.getHoursToSkip();
      feed.getLanguage();
      feed.getLastBuildDate();
      feed.getLastModifiedDate();
      feed.getLink();
      feed.getProperties();
      feed.getPublishDate();
      feed.getRating();
      feed.getTitle();
      feed.getTTL();
      feed.getUpdateBase();
      feed.getUpdateFrequency();
      feed.getUpdatePeriod();
      feed.getWebmaster();
    }

    else if (type instanceof INews) {
      INews news = (INews) type;
      news.getBase();
      news.getComments();
      news.getDescription();
      news.getLink();
      news.getModifiedDate();
      news.getPublishDate();
      news.getRating();
      news.getReceiveDate();
      news.getState();
      news.getTitle();
    }

    else if (type instanceof IAttachment) {
      IAttachment attachment = (IAttachment) type;
      attachment.getLength();
      attachment.getType();
      attachment.getLink();
    }

    else if (type instanceof ICategory) {
      ICategory category = (ICategory) type;
      category.getDomain();
      category.getName();
    }

    else if (type instanceof ISource) {
      ISource source = (ISource) type;
      source.getName();
      source.getLink();
    }

    else if (type instanceof IPerson) {
      IPerson person = (IPerson) type;
      person.getEmail();
      person.getName();
      person.getUri();
    }

    else if (type instanceof IImage) {
      IImage image = (IImage) type;
      image.getDescription();
      image.getHeight();
      image.getWidth();
      image.getHomepage();
      image.getTitle();
      image.getLink();
    }

    else if (type instanceof ILabel) {
      ILabel label = (ILabel) type;
      label.getColor();
      label.getName();
    }

    else if (type instanceof IGuid) {
      IGuid guid = (IGuid) type;
      guid.getValue();
    }

    else if (type instanceof ICloud) {
      ICloud cloud = (ICloud) type;
      cloud.getDomain();
      cloud.getPath();
      cloud.getPort();
      cloud.getRegisterProcedure();
      cloud.getProtocol();
      cloud.getFeed();
    }

    else if (type instanceof ITextInput) {
      ITextInput input = (ITextInput) type;
      input.getDescription();
      input.getLink();
      input.getName();
      input.getTitle();
      input.getFeed();
    }
  }

  private InputStream loadFileProtocol(URI link) throws FileNotFoundException {
    File file = new File(link);
    return new BufferedInputStream(new FileInputStream(file));
  }
}