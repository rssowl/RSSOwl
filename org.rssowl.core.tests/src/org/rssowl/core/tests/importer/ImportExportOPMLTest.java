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

package org.rssowl.core.tests.importer;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.newsaction.LabelNewsAction;
import org.rssowl.core.internal.newsaction.MoveNewsAction;
import org.rssowl.core.internal.persist.pref.GlobalScope;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.interpreter.ITypeExporter.Options;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.Preference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.util.ImportUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Tests a full export and import using OPML including Folders, Bookmarks, Saved
 * Searches and News Bins. For saved searches most combinations of Field,
 * Specifier and Value are used to make sure everything works as expected. Also
 * tested is import export of a OPML file from a backup containing Labels and
 * Filters.
 *
 * @author bpasero
 */
public class ImportExportOPMLTest {
  private File fTmpFile;
  private File fTmpFileOnlyMarks;
  private File fTmpFileInvalidLocations;
  private File fTmpBackupFile;
  private File fTmpFileHierarchy;
  private IModelFactory fFactory;
  private IFolder fDefaultSet;
  private IFolder fCustomSet;
  private IFolder fDefaultFolder1;
  private IBookMark fBookMark1;
  private IFolder fDefaultFolder2;
  private IFolder fCustomFolder2;
  private INewsBin fNewsBin;
  private ILabel fImportantLabel;
  private ISearchMark fSearchmark;
  private ISearchMark fSearchmarkWithLocation;
  private IBookMark fBookMark2;
  private IBookMark fBookMark3;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();

    fFactory = Owl.getModelFactory();
    fTmpFile = File.createTempFile("rssowl", ".opml2"); //Test the fallback to OPML format too
    fTmpFile.deleteOnExit();

    fTmpFileOnlyMarks = File.createTempFile("rssowl_onlymarks", ".opml");
    fTmpFileOnlyMarks.deleteOnExit();

    fTmpFileHierarchy = File.createTempFile("rssowl_hierarchy", ".opml");
    fTmpFileHierarchy.deleteOnExit();

    fTmpFileInvalidLocations = File.createTempFile("rssowl_invalidlocations", ".opml");
    fTmpFileInvalidLocations.deleteOnExit();

    fTmpBackupFile = File.createTempFile("rssowl_backup", ".opml");
    fTmpBackupFile.deleteOnExit();

    /* Fill Defaults */
    fillDefaults();
    DynamicDAO.getDAO(IFolderDAO.class).save(fDefaultSet);
    DynamicDAO.getDAO(IFolderDAO.class).save(fCustomSet);

    /* Export */
    Set<IFolder> rootFolders = new HashSet<IFolder>();
    rootFolders.add(fDefaultSet);
    rootFolders.add(fCustomSet);

    List<IMark> marks = new ArrayList<IMark>(fDefaultSet.getMarks());
    marks.addAll(fCustomSet.getMarks());
    Iterator<IMark> iterator = marks.iterator();
    while (iterator.hasNext()) {
      if (iterator.next() instanceof ISearchMark) //Remove Saved Searches since they might depend on a not existing location
        iterator.remove();
    }

    Owl.getInterpreter().exportTo(fTmpFile, rootFolders, null);
    Owl.getInterpreter().exportTo(fTmpFileOnlyMarks, marks, null);
    Owl.getInterpreter().exportTo(fTmpFileHierarchy, Arrays.asList(new IFolderChild[] { fBookMark2, fBookMark3 }), null);
    Owl.getInterpreter().exportTo(fTmpFileInvalidLocations, Collections.singleton(fSearchmarkWithLocation), EnumSet.of(Options.EXPORT_FILTERS, Options.EXPORT_LABELS, Options.EXPORT_PREFERENCES));
    Owl.getInterpreter().exportTo(fTmpBackupFile, rootFolders, EnumSet.of(Options.EXPORT_FILTERS, Options.EXPORT_LABELS, Options.EXPORT_PREFERENCES));

    /* Clear */
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();

    /* Add Default Set */
    DynamicDAO.getDAO(IFolderDAO.class).save(fFactory.createFolder(null, null, "My Bookmarks"));
  }

  private void fillDefaults() throws URISyntaxException {

    /* Set: Default */
    fillDefaultSet();

    /* Set: Custom */
    fillCustomSet();

    DynamicDAO.getDAO(IFolderDAO.class).save(fDefaultSet);
    DynamicDAO.getDAO(IFolderDAO.class).save(fCustomSet);

    /* Default > List of SearchMarks */
    fillSearchMarks(fDefaultSet);

    /* Default > Folder 2 > List of SearchMarks */
    fillSearchMarks(fDefaultFolder2);

    /* Custom > List of SearchMarks */
    fillSearchMarks(fCustomSet);

    /* Custom > Folder 2 > List of SearchMarks */
    fillSearchMarks(fCustomFolder2);

    /* Labels */
    fillLabels();

    /* Filters */
    fillFilters();

    /* Global / Eclipse Preferences */
    fillPreferences();
  }

  private void fillPreferences() {
    IPreferenceScope globalPreferences = Owl.getPreferenceService().getGlobalScope();
    IPreferenceScope eclipsePreferences = Owl.getPreferenceService().getGlobalScope();

    globalPreferences.putBoolean(Preference.MARK_READ_ON_TAB_CLOSE.id(), true);
    globalPreferences.putInteger(Preference.MARK_READ_IN_MILLIS.id(), 5);
    globalPreferences.putIntegers(Preference.BM_NEWS_COLUMNS.id(), new int[] { -1, 0, 1, 2, 3 });
    globalPreferences.putLong(Preference.BM_UPDATE_INTERVAL.id(), 8);
    globalPreferences.putLong(Preference.NM_SELECTED_NEWS.id(), 100);
    globalPreferences.putString(Preference.CUSTOM_BROWSER_PATH.id(), "hello world");
    globalPreferences.putStrings(Preference.DISABLE_JAVASCRIPT_EXCEPTIONS.id(), new String[] { "hello", "world", "foo", "bar" });

    eclipsePreferences.putBoolean(Preference.ECLIPSE_SINGLE_CLICK_OPEN.id(), true);
    eclipsePreferences.putInteger(Preference.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD.id(), 5);
    eclipsePreferences.putString(Preference.ECLIPSE_PROXY_HOST_HTTP.id(), "");
  }

  private void assertPreferences() {
    IPreferenceScope globalPreferences = new GlobalScope(Owl.getPreferenceService().getDefaultScope());
    IPreferenceScope eclipsePreferences = Owl.getPreferenceService().getGlobalScope();

    assertEquals(true, globalPreferences.getBoolean(Preference.MARK_READ_ON_TAB_CLOSE.id()));
    assertEquals(5, globalPreferences.getInteger(Preference.MARK_READ_IN_MILLIS.id()));
    assertTrue(Arrays.equals(new int[] { -1, 0, 1, 2, 3 }, globalPreferences.getIntegers(Preference.BM_NEWS_COLUMNS.id())));
    assertEquals(8, globalPreferences.getLong(Preference.BM_UPDATE_INTERVAL.id()));
    assertTrue(globalPreferences.getLong(Preference.NM_SELECTED_NEWS.id()) != 100);
    assertEquals("hello world", globalPreferences.getString(Preference.CUSTOM_BROWSER_PATH.id()));
    assertTrue(Arrays.equals(new String[] { "hello", "world", "foo", "bar" }, globalPreferences.getStrings(Preference.DISABLE_JAVASCRIPT_EXCEPTIONS.id())));

    assertEquals(true, eclipsePreferences.getBoolean(Preference.ECLIPSE_SINGLE_CLICK_OPEN.id()));
    assertEquals(5, eclipsePreferences.getInteger(Preference.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD.id()));
    assertEquals("", eclipsePreferences.getString(Preference.ECLIPSE_PROXY_HOST_HTTP.id()));
  }

  private void fillFilters() {

    /* 1) Match All News - Enabled - Mark Read */
    ISearchFilter filter = fFactory.createSearchFilter(null, null, "Filter 1");
    filter.setMatchAllNews(true);
    filter.setEnabled(true);
    filter.setOrder(5);
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkReadNewsAction"));
    DynamicDAO.save(filter);

    /* 2) Match All News - Disabled - Mark Read + Mark Sticky */
    filter = fFactory.createSearchFilter(null, null, "Filter 2");
    filter.setMatchAllNews(true);
    filter.setEnabled(false);
    filter.setOrder(0);
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkReadNewsAction"));
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkStickyNewsAction"));
    DynamicDAO.save(filter);

    /* 3) Entire News contains "Foo" - Enabled - Mark Read */
    ISearch search = fFactory.createSearch(null);
    ISearchField entireNewsField = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    search.addSearchCondition(fFactory.createSearchCondition(entireNewsField, SearchSpecifier.CONTAINS, "Foo"));
    filter = fFactory.createSearchFilter(null, search, "Filter 3");
    filter.setMatchAllNews(false);
    filter.setOrder(3);
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkReadNewsAction"));
    DynamicDAO.save(filter);

    /* 4) Entire News contains "Foo" or "Bar" - Enabled - Mark Read */
    search = fFactory.createSearch(null);
    search.setMatchAllConditions(true);
    search.addSearchCondition(fFactory.createSearchCondition(entireNewsField, SearchSpecifier.CONTAINS, "Foo"));
    search.addSearchCondition(fFactory.createSearchCondition(entireNewsField, SearchSpecifier.CONTAINS, "Bar"));
    filter = fFactory.createSearchFilter(null, search, "Filter 4");
    filter.setMatchAllNews(false);
    filter.setOrder(4);
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkReadNewsAction"));
    DynamicDAO.save(filter);

    /* 5) Location is "XY" - Enabled - Mark Read */
    search = fFactory.createSearch(null);
    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    search.addSearchCondition(fFactory.createSearchCondition(locationField, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fBookMark1, fNewsBin }))));
    filter = fFactory.createSearchFilter(null, search, "Filter 5");
    filter.setMatchAllNews(false);
    filter.setOrder(8);
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkReadNewsAction"));
    DynamicDAO.save(filter);

    /* 6) Match All News - Enabled - Label News */
    filter = fFactory.createSearchFilter(null, null, "Filter 6");
    filter.setMatchAllNews(true);
    filter.setOrder(5);
    IFilterAction action = fFactory.createFilterAction(LabelNewsAction.ID);
    action.setData(fImportantLabel.getId());
    filter.addAction(action);
    DynamicDAO.save(filter);

    /* 7) Match All News - Enabled - Label News + Move News + Play Sound */
    filter = fFactory.createSearchFilter(null, null, "Filter 7");
    filter.setMatchAllNews(true);
    filter.setOrder(5);
    action = fFactory.createFilterAction(LabelNewsAction.ID);
    action.setData(fImportantLabel.getId());
    filter.addAction(action);

    action = fFactory.createFilterAction(MoveNewsAction.ID);
    action.setData(new Long[] { fNewsBin.getId() });
    filter.addAction(action);

    action = fFactory.createFilterAction("org.rssowl.ui.PlaySoundAction");
    action.setData("C:\\ProgramData\\Microsoft\\Windows & Help\\Start Menu");
    filter.addAction(action);

    DynamicDAO.save(filter);

    /* 8) Filter with Properties as Data */
    filter = fFactory.createSearchFilter(null, null, "Filter 8");
    filter.setMatchAllNews(true);
    filter.setOrder(5);

    action = fFactory.createFilterAction("org.rssowl.ui.PlaySoundAction");
    Properties props = new Properties();
    props.setProperty("foo", "bar");
    props.setProperty("hello world", " world hello ");
    props.setProperty("<some xml>tags</a>", "foo & bar");
    action.setData(props);
    filter.addAction(action);

    DynamicDAO.save(filter);

    /* 9) Location is DELETED - Enabled - Mark Read */
    search = fFactory.createSearch(null);
    locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    Long[][] result = new Long[3][];
    result[0] = new Long[] { 99999l };
    search.addSearchCondition(fFactory.createSearchCondition(locationField, SearchSpecifier.SCOPE, result));
    filter = fFactory.createSearchFilter(null, search, "Filter 9");
    filter.setMatchAllNews(false);
    filter.setOrder(9);
    filter.addAction(fFactory.createFilterAction("org.rssowl.core.MarkReadNewsAction"));
    DynamicDAO.save(filter);
  }

  private void fillLabels() {
    ILabel label = fFactory.createLabel(null, "Later");
    label.setColor("113,21,88");
    label.setOrder(4);
    DynamicDAO.save(label);

    label = fFactory.createLabel(null, "Personal");
    label.setColor("105,130,73");
    label.setOrder(3);
    DynamicDAO.save(label);

    fImportantLabel = fFactory.createLabel(null, "Important");
    fImportantLabel.setColor("177,39,52");
    fImportantLabel.setOrder(2);
    DynamicDAO.save(fImportantLabel);

    label = fFactory.createLabel(null, "Work");
    label.setColor("234,152,79");
    label.setOrder(1);
    DynamicDAO.save(label);

    label = fFactory.createLabel(null, "To Do");
    label.setColor("113,160,168");
    label.setOrder(0);
    DynamicDAO.save(label);
  }

  private void fillDefaultSet() throws URISyntaxException {
    fDefaultSet = fFactory.createFolder(null, null, "My Bookmarks");

    fDefaultFolder1 = fFactory.createFolder(null, fDefaultSet, "Default Folder 1");
    addProperties(Owl.getPreferenceService().getEntityScope(fDefaultFolder1));

    fDefaultFolder2 = fFactory.createFolder(null, fDefaultSet, "Default Folder 2");

    /* Default > BookMark 1 */
    IFeed feed1 = fFactory.createFeed(null, new URI("feed1"));
    fBookMark1 = fFactory.createBookMark(null, fDefaultSet, new FeedLinkReference(feed1.getLink()), "Bookmark 1");
    addProperties(Owl.getPreferenceService().getEntityScope(fBookMark1));

    /* Default > Folder 1 > BookMark 3 */
    IFeed feed3 = fFactory.createFeed(null, new URI("feed3"));
    fBookMark3 = fFactory.createBookMark(null, fDefaultFolder1, new FeedLinkReference(feed3.getLink()), "Bookmark 3");

    /* Default > News Bin 1 */
    fNewsBin = fFactory.createNewsBin(null, fDefaultSet, "Bin 1");
    addProperties(Owl.getPreferenceService().getEntityScope(fNewsBin));
  }

  private void addProperties(IPreferenceScope prefs) {
    prefs.putBoolean("boolean", true);
    prefs.putInteger("integer", 5);
    prefs.putIntegers("integers", new int[] { -1, 0, 1, 2, 3 });
    prefs.putLong("long", 8);
    prefs.putLongs("longs", new long[] { -3, -2, -1, 0, 1, 2, 3 });
    prefs.putString("string", "hello world");
    prefs.putStrings("strings", new String[] { "hello", "world", "foo", "bar" });
  }

  private void assertProperties(IPreferenceScope prefs) {
    assertEquals(true, prefs.getBoolean("boolean"));
    assertEquals(5, prefs.getInteger("integer"));
    assertTrue(Arrays.equals(new int[] { -1, 0, 1, 2, 3 }, prefs.getIntegers("integers")));
    assertEquals(8, prefs.getLong("long"));
    assertTrue(Arrays.equals(new long[] { -3, -2, -1, 0, 1, 2, 3 }, prefs.getLongs("longs")));
    assertEquals("hello world", prefs.getString("string"));
    assertTrue(Arrays.equals(new String[] { "hello", "world", "foo", "bar" }, prefs.getStrings("strings")));
  }

  private void fillCustomSet() throws URISyntaxException {
    fCustomSet = fFactory.createFolder(null, null, "Custom");

    /* Custom > Folder 1 */
    IFolder folder1 = fFactory.createFolder(null, fCustomSet, "Custom Folder 1");

    fCustomFolder2 = fFactory.createFolder(null, fCustomSet, "Custom Folder 2");

    /* Custom > BookMark 2 */
    IFeed feed2 = fFactory.createFeed(null, new URI("feed2"));
    fBookMark2 = fFactory.createBookMark(null, fCustomSet, new FeedLinkReference(feed2.getLink()), "Bookmark 2");

    /* Custom > Folder 1 > BookMark 4 */
    IFeed feed4 = fFactory.createFeed(null, new URI("feed4"));
    fFactory.createBookMark(null, folder1, new FeedLinkReference(feed4.getLink()), "Bookmark 4");

  }

  private void fillSearchMarks(IFolder parent) {
    String newsName = INews.class.getName();

    /* 1) State IS new */
    {
      ISearchField field = fFactory.createSearchField(INews.STATE, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(State.NEW));

      fSearchmark = fFactory.createSearchMark(null, parent, "Search");
      fSearchmark.addSearchCondition(condition);
      addProperties(Owl.getPreferenceService().getEntityScope(fSearchmark));
    }

    /* 2) State IS new, unread, updated */
    {
      ISearchField field = fFactory.createSearchField(INews.STATE, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(State.NEW, State.UNREAD, State.UPDATED));

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 3) Entire News CONTAINS foo?bar */
    {
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo?bar");

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 4) Age in Days is > 5 */
    {
      ISearchField field = fFactory.createSearchField(INews.AGE_IN_DAYS, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_GREATER_THAN, 5);

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 5) Publish Date is 26.12.1981 */
    {
      Calendar cal = DateUtils.getToday();
      cal.set(Calendar.YEAR, 1981);
      cal.set(Calendar.MONTH, Calendar.DECEMBER);
      cal.set(Calendar.DATE, 26);

      ISearchField field = fFactory.createSearchField(INews.PUBLISH_DATE, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, cal.getTime());

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 6) Feed Links is not http://www.rssowl.org/node/feed */
    {
      ISearchField field = fFactory.createSearchField(INews.FEED, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS_NOT, "http://www.rssowl.org/node/feed");

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 7) Has Attachments is TRUE */
    {
      ISearchField field = fFactory.createSearchField(INews.HAS_ATTACHMENTS, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /*
     * 8) Entire News CONTAINS foo?bar AND State ISnew AND Has Attachments is
     * TRUE
     */
    {
      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");

      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS, "foo?bar");
      searchmark.addSearchCondition(condition);

      field = fFactory.createSearchField(INews.HAS_ATTACHMENTS, newsName);
      condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, true);
      searchmark.addSearchCondition(condition);

      field = fFactory.createSearchField(INews.STATE, newsName);
      condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(State.NEW));
      searchmark.addSearchCondition(condition);

      searchmark.setMatchAllConditions(true);
    }

    /* 9) Location is Default Set */
    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fDefaultSet })));

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 10) Location is Default Set OR Location is Custom Set */
    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fDefaultSet, fCustomSet })));

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 11) Location is Folder 1 */
    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fDefaultFolder1 })));

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 12) Location is BookMark 1 */
    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fBookMark1 })));

      fSearchmarkWithLocation = fFactory.createSearchMark(null, parent, "Search");
      fSearchmarkWithLocation.addSearchCondition(condition);
    }

    /*
     * 13) Location is Default Set OR Location is Custom Set OR Location is
     * BookMark1
     */
    {
      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");

      ISearchField field = fFactory.createSearchField(INews.LOCATION, newsName);

      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fDefaultSet })));
      searchmark.addSearchCondition(condition);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fCustomSet })));
      searchmark.addSearchCondition(condition);

      condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fBookMark1 })));
      searchmark.addSearchCondition(condition);
    }

    /* 14) Location is Bin 1 */
    {
      ISearchField field = fFactory.createSearchField(INews.LOCATION, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { fNewsBin })));

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 15) Entire News CONTAINS_ALL foo?bar */
    {
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_ALL, "foo?bar");

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }

    /* 16) Entire News CONTAINS_NOT foo?bar */
    {
      ISearchField field = fFactory.createSearchField(IEntity.ALL_FIELDS, newsName);
      ISearchCondition condition = fFactory.createSearchCondition(field, SearchSpecifier.CONTAINS_NOT, "foo?bar");

      ISearchMark searchmark = fFactory.createSearchMark(null, parent, "Search");
      searchmark.addSearchCondition(condition);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings( { "nls", "null" })
  public void testExportImportOnlyMarkOPML() throws Exception {

    /* Import */
    List<? extends IEntity> elements = Owl.getInterpreter().importFrom(new FileInputStream(fTmpFileOnlyMarks));
    ImportUtils.doImport(null, elements, true);

    /* Validate */
    Collection<IFolder> rootFolders = DynamicDAO.getDAO(IFolderDAO.class).loadRoots();

    assertEquals(2, rootFolders.size());

    IFolder defaultSet = null;
    IFolder customSet = null;
    for (IFolder rootFolder : rootFolders) {
      if (rootFolder.getName().equals("My Bookmarks"))
        defaultSet = rootFolder;
      else if (rootFolder.getName().equals("Custom"))
        customSet = rootFolder;
    }

    assertNotNull(defaultSet);
    assertNotNull(customSet);

    assertEquals(2, defaultSet.getChildren().size());
    assertEquals(1, customSet.getChildren().size());

    List<IFolderChild> children = defaultSet.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IBookMark)
        assertEquals("Bookmark 1", child.getName());
      else if (child instanceof INewsBin)
        assertEquals("Bin 1", child.getName());
      else
        fail();
    }

    children = customSet.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IBookMark)
        assertEquals("Bookmark 2", child.getName());
      else
        fail();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings( { "nls", "null" })
  public void testExportImportInvalidLocationsOPML() throws Exception {

    /* Import */
    List<? extends IEntity> elements = Owl.getInterpreter().importFrom(new FileInputStream(fTmpFileInvalidLocations));
    ImportUtils.doImport(null, elements, true);

    /* Validate */
    Collection<IFolder> rootFolders = DynamicDAO.getDAO(IFolderDAO.class).loadRoots();

    IFolder defaultSet = null;
    IFolder customSet = null;
    for (IFolder rootFolder : rootFolders) {
      if (rootFolder.getName().equals("My Bookmarks"))
        defaultSet = rootFolder;
      else if (rootFolder.getName().equals("Custom"))
        customSet = rootFolder;
    }

    assertNotNull(defaultSet);
    assertNotNull(customSet);

    List<IFolderChild> children = customSet.getChildren();

    assertEquals(1, children.size());
    assertTrue(children.get(0) instanceof IFolder);
    assertEquals("Custom Folder 2", children.get(0).getName());

    IFolder customFolder2 = (IFolder) children.get(0);
    children = customFolder2.getChildren();

    assertEquals(1, children.size());
    assertTrue(children.get(0) instanceof ISearchMark);
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings( { "nls", "null" })
  public void testExportImportCompleteOPML() throws Exception {
    exportImportCompleteOPML(false);
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings( { "nls", "null" })
  public void testExportImportCompleteBackupOPML() throws Exception {

    /* Pre-Create some Labels for Testing merge behavior */
    ILabel label = fFactory.createLabel(null, "Later");
    label.setColor("113,21,88");
    label.setOrder(0);
    DynamicDAO.save(label);

    label = fFactory.createLabel(null, "Personal");
    label.setColor("0,0,0");
    label.setOrder(1);
    DynamicDAO.save(label);

    exportImportCompleteOPML(true);
  }

  @SuppressWarnings( { "nls", "null" })
  private void exportImportCompleteOPML(boolean useBackup) throws Exception {

    /* Import */
    List<? extends IEntity> elements = Owl.getInterpreter().importFrom(new FileInputStream(useBackup ? fTmpBackupFile.getAbsolutePath() : fTmpFile.getAbsolutePath()));
    ImportUtils.doImport(null, elements, true);

    /* Validate */
    Collection<IFolder> rootFolders = DynamicDAO.getDAO(IFolderDAO.class).loadRoots();

    assertEquals(2, rootFolders.size());

    IFolder defaultSet = null;
    IFolder customSet = null;
    for (IFolder rootFolder : rootFolders) {
      if (rootFolder.getName().equals("My Bookmarks"))
        defaultSet = rootFolder;
      else if (rootFolder.getName().equals("Custom"))
        customSet = rootFolder;
    }

    assertNotNull(defaultSet);
    assertNotNull(customSet);

    List<IFolder> defaultFolders = defaultSet.getFolders();
    assertEquals(2, defaultFolders.size());

    IFolder defaultFolder1 = null;
    IFolder defaultFolder2 = null;

    for (IFolder defaultFolder : defaultFolders) {
      if (defaultFolder.getName().equals("Default Folder 1"))
        defaultFolder1 = defaultFolder;
      else if (defaultFolder.getName().equals("Default Folder 2"))
        defaultFolder2 = defaultFolder;
    }

    if (useBackup)
      assertProperties(Owl.getPreferenceService().getEntityScope(defaultFolder1));

    assertNotNull(defaultFolder1);
    assertNotNull(defaultFolder2);

    List<IFolder> customFolders = customSet.getFolders();
    assertEquals(2, customFolders.size());

    IFolder customFolder1 = null;
    IFolder customFolder2 = null;

    for (IFolder customFolder : customFolders) {
      if (customFolder.getName().equals("Custom Folder 1"))
        customFolder1 = customFolder;
      else if (customFolder.getName().equals("Custom Folder 2"))
        customFolder2 = customFolder;
    }

    assertNotNull(customFolder1);
    assertNotNull(customFolder2);

    List<IMark> defaultMarks = defaultSet.getMarks();
    assertEquals(18, defaultMarks.size());

    IBookMark bookmark1 = null;
    for (IMark mark : defaultMarks) {
      if (mark instanceof IBookMark && mark.getName().equals("Bookmark 1"))
        bookmark1 = (IBookMark) mark;
    }

    assertNotNull(bookmark1);
    assertEquals("http://feed1", bookmark1.getFeedLinkReference().getLink().toString());
    if (useBackup)
      assertProperties(Owl.getPreferenceService().getEntityScope(bookmark1));

    INewsBin bin = null;
    for (IMark mark : defaultMarks) {
      if (mark instanceof INewsBin && mark.getName().equals("Bin 1"))
        bin = (INewsBin) mark;
    }

    assertNotNull(bin);
    if (useBackup)
      assertProperties(Owl.getPreferenceService().getEntityScope(bin));

    List<IMark> customMarks = customSet.getMarks();
    assertEquals(17, customMarks.size());

    IBookMark bookmark2 = null;
    for (IMark mark : customMarks) {
      if (mark instanceof IBookMark && mark.getName().equals("Bookmark 2"))
        bookmark2 = (IBookMark) mark;
    }

    assertNotNull(bookmark2);
    assertEquals("http://feed2", bookmark2.getFeedLinkReference().getLink().toString());

    List<IMark> marks = defaultFolder1.getMarks();
    assertEquals(1, marks.size());

    IBookMark bookmark3 = null;
    for (IMark mark : marks) {
      if (mark instanceof IBookMark && mark.getName().equals("Bookmark 3"))
        bookmark3 = (IBookMark) mark;
    }

    assertNotNull(bookmark3);
    assertEquals("http://feed3", bookmark3.getFeedLinkReference().getLink().toString());

    marks = customFolder1.getMarks();
    assertEquals(1, marks.size());

    IBookMark bookmark4 = null;
    for (IMark mark : marks) {
      if (mark instanceof IBookMark && mark.getName().equals("Bookmark 4"))
        bookmark4 = (IBookMark) mark;
    }

    assertNotNull(bookmark4);
    assertEquals("http://feed4", bookmark4.getFeedLinkReference().getLink().toString());

    assertSearchMarks(defaultSet, useBackup);
    assertSearchMarks(customSet, useBackup);
    assertSearchMarks(defaultFolder2, useBackup);
    assertSearchMarks(customFolder2, useBackup);

    if (useBackup) {
      assertLabels();
      assertFilters();
      assertPreferences();
    }
  }

  private void assertFilters() {
    Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
    assertEquals(9, filters.size());

    for (ISearchFilter filter : filters) {
      if ("Filter 1".equals(filter.getName())) {
        assertEquals(true, filter.isEnabled());
        assertNull(filter.getSearch());
        assertTrue(filter.matchAllNews());
        assertEquals(5, filter.getOrder());
        assertEquals(1, filter.getActions().size());
        assertEquals("org.rssowl.core.MarkReadNewsAction", filter.getActions().get(0).getActionId());
        assertNull(filter.getActions().get(0).getData());
      }

      else if ("Filter 2".equals(filter.getName())) {
        assertEquals(false, filter.isEnabled());
        assertNull(filter.getSearch());
        assertTrue(filter.matchAllNews());
        assertEquals(0, filter.getOrder());
        assertEquals(2, filter.getActions().size());
        assertEquals("org.rssowl.core.MarkReadNewsAction", filter.getActions().get(0).getActionId());
        assertNull(filter.getActions().get(0).getData());
        assertEquals("org.rssowl.core.MarkStickyNewsAction", filter.getActions().get(1).getActionId());
        assertNull(filter.getActions().get(1).getData());
      }

      else if ("Filter 3".equals(filter.getName())) {
        assertNotNull(filter.getSearch());
        assertEquals(false, filter.getSearch().matchAllConditions());
        assertEquals(1, filter.getSearch().getSearchConditions().size());
        ISearchCondition cond = filter.getSearch().getSearchConditions().get(0);
        assertEquals(IEntity.ALL_FIELDS, cond.getField().getId());
        assertEquals(INews.class.getName(), cond.getField().getEntityName());
        assertEquals(SearchSpecifier.CONTAINS, cond.getSpecifier());
        assertEquals("Foo", cond.getValue());

        assertEquals(false, filter.matchAllNews());
        assertEquals(3, filter.getOrder());
        assertEquals(1, filter.getActions().size());
        assertEquals("org.rssowl.core.MarkReadNewsAction", filter.getActions().get(0).getActionId());
        assertNull(filter.getActions().get(0).getData());
      }

      else if ("Filter 4".equals(filter.getName())) {
        assertNotNull(filter.getSearch());
        assertEquals(true, filter.getSearch().matchAllConditions());
        assertEquals(2, filter.getSearch().getSearchConditions().size());
        ISearchCondition cond1 = filter.getSearch().getSearchConditions().get(0);
        assertEquals(IEntity.ALL_FIELDS, cond1.getField().getId());
        assertEquals(INews.class.getName(), cond1.getField().getEntityName());
        assertEquals(SearchSpecifier.CONTAINS, cond1.getSpecifier());
        assertEquals("Foo", cond1.getValue());

        ISearchCondition cond2 = filter.getSearch().getSearchConditions().get(1);
        assertEquals(IEntity.ALL_FIELDS, cond2.getField().getId());
        assertEquals(INews.class.getName(), cond2.getField().getEntityName());
        assertEquals(SearchSpecifier.CONTAINS, cond2.getSpecifier());
        assertEquals("Bar", cond2.getValue());

        assertEquals(false, filter.matchAllNews());
        assertEquals(4, filter.getOrder());
        assertEquals(1, filter.getActions().size());
        assertEquals("org.rssowl.core.MarkReadNewsAction", filter.getActions().get(0).getActionId());
        assertNull(filter.getActions().get(0).getData());
      }

      else if ("Filter 5".equals(filter.getName())) {
        assertNotNull(filter.getSearch());
        assertEquals(1, filter.getSearch().getSearchConditions().size());
        ISearchCondition cond = filter.getSearch().getSearchConditions().get(0);
        assertEquals(INews.LOCATION, cond.getField().getId());
        assertEquals(INews.class.getName(), cond.getField().getEntityName());
        assertEquals(SearchSpecifier.SCOPE, cond.getSpecifier());

        List<IFolderChild> locations = CoreUtils.toEntities((Long[][]) cond.getValue());
        assertEquals(2, locations.size());
        for (IFolderChild location : locations) {
          if (!fBookMark1.getName().equals(location.getName()) && !fNewsBin.getName().equals(location.getName()))
            fail("Unexpected location: " + location.getName());
        }
      }

      else if ("Filter 6".equals(filter.getName())) {
        assertEquals(1, filter.getActions().size());
        assertEquals(LabelNewsAction.ID, filter.getActions().get(0).getActionId());
        Object data = filter.getActions().get(0).getData();
        assertNotNull(data);
        assertEquals(true, data instanceof Long);
        ILabel label = DynamicDAO.getDAO(ILabelDAO.class).load(((Long) data).longValue());
        assertNotNull(label);
        assertEquals(fImportantLabel.getName(), label.getName());
      }

      else if ("Filter 7".equals(filter.getName())) {
        assertEquals(3, filter.getActions().size());
        assertEquals(LabelNewsAction.ID, filter.getActions().get(0).getActionId());
        Object data = filter.getActions().get(0).getData();
        assertNotNull(data);
        assertEquals(true, data instanceof Long);
        ILabel label = DynamicDAO.getDAO(ILabelDAO.class).load(((Long) data).longValue());
        assertNotNull(label);
        assertEquals(fImportantLabel.getName(), label.getName());

        assertEquals(MoveNewsAction.ID, filter.getActions().get(1).getActionId());
        data = filter.getActions().get(1).getData();
        assertNotNull(data);
        assertEquals(true, data instanceof Long[]);
        assertEquals(1, ((Long[]) data).length);
        INewsBin bin = DynamicDAO.getDAO(INewsBinDAO.class).load(((Long[]) data)[0].longValue());
        assertNotNull(bin);
        assertEquals(fNewsBin.getName(), bin.getName());

        assertEquals("org.rssowl.ui.PlaySoundAction", filter.getActions().get(2).getActionId());
        data = filter.getActions().get(2).getData();
        assertNotNull(data);
        assertEquals("C:\\ProgramData\\Microsoft\\Windows & Help\\Start Menu", data);
      }

      else if ("Filter 8".equals(filter.getName())) {
        assertEquals(1, filter.getActions().size());
        assertEquals("org.rssowl.ui.PlaySoundAction", filter.getActions().get(0).getActionId());
        Object data = filter.getActions().get(0).getData();
        assertNotNull(data);
        assertEquals(true, data instanceof Properties);
        Properties props = (Properties) data;
        assertEquals("bar", props.getProperty("foo"));
        assertEquals(" world hello ", props.getProperty("hello world"));
        assertEquals("foo & bar", props.getProperty("<some xml>tags</a>"));
      }

      else if ("Filter 9".equals(filter.getName())) {
        assertTrue(!filter.isEnabled());
      }

      else
        fail("Unexpected Filter found with name: " + filter.getName());
    }
  }

  private void assertLabels() {
    Collection<ILabel> labels = DynamicDAO.loadAll(ILabel.class);
    assertEquals(5, labels.size());
    for (ILabel label : labels) {
      if ("Later".equals(label.getName())) {
        assertEquals("113,21,88", label.getColor());
        assertEquals(4, label.getOrder());
      } else if ("Personal".equals(label.getName())) {
        assertEquals("105,130,73", label.getColor());
        assertEquals(3, label.getOrder());
      } else if ("Important".equals(label.getName())) {
        assertEquals("177,39,52", label.getColor());
        assertEquals(2, label.getOrder());
        label.setColor("177,39,52");
      } else if ("Work".equals(label.getName())) {
        assertEquals("234,152,79", label.getColor());
        assertEquals(1, label.getOrder());
      } else if ("To Do".equals(label.getName())) {
        assertEquals("113,160,168", label.getColor());
        assertEquals(0, label.getOrder());
      } else
        fail("Unexpected Label found with name: " + label.getName());
    }
  }

  private void assertSearchMarks(IFolder folder, boolean isBackup) {
    List<IMark> marks = folder.getMarks();
    List<ISearchMark> searchmarks = new ArrayList<ISearchMark>();
    for (IMark mark : marks) {
      if (mark instanceof ISearchMark)
        searchmarks.add((ISearchMark) mark);
    }

    /* 1) State ISnew */
    ISearchMark searchmark = searchmarks.get(0);
    assertEquals("Search", searchmark.getName());
    List<ISearchCondition> conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.STATE, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    assertEquals(EnumSet.of(INews.State.NEW), conditions.get(0).getValue());
    if (isBackup)
      assertProperties(Owl.getPreferenceService().getEntityScope(searchmark));

    /* 2) State ISnewunreadupdated */
    searchmark = searchmarks.get(1);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.STATE, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    assertEquals(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED), conditions.get(0).getValue());

    /* 3) Entire News CONTAINS foo?bar */
    searchmark = searchmarks.get(2);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(IEntity.ALL_FIELDS, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.CONTAINS, conditions.get(0).getSpecifier());
    assertEquals("foo?bar", conditions.get(0).getValue());

    /* 4) Age in Days is > 5 */
    searchmark = searchmarks.get(3);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.AGE_IN_DAYS, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS_GREATER_THAN, conditions.get(0).getSpecifier());
    assertEquals(5, conditions.get(0).getValue());

    /* 5) Publish Date is 26.12.1981 */
    Calendar cal = DateUtils.getToday();
    cal.set(Calendar.YEAR, 1981);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.DATE, 26);

    searchmark = searchmarks.get(4);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.PUBLISH_DATE, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    assertEquals(cal.getTime(), conditions.get(0).getValue());

    /* 6) Feed Links is not http://www.rssowl.org/node/feed */
    searchmark = searchmarks.get(5);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.FEED, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS_NOT, conditions.get(0).getSpecifier());
    assertEquals("http://www.rssowl.org/node/feed", conditions.get(0).getValue());

    /* 7) Has Attachments is TRUE */
    searchmark = searchmarks.get(6);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.HAS_ATTACHMENTS, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    assertEquals(true, conditions.get(0).getValue());

    /*
     * 8) Entire News CONTAINS foo?bar AND State ISnew AND Has Attachments is
     * TRUE
     */
    searchmark = searchmarks.get(7);
    conditions = searchmark.getSearchConditions();
    assertEquals(3, conditions.size());
    assertEquals(true, searchmark.matchAllConditions());

    for (ISearchCondition condition : conditions) {
      switch (condition.getField().getId()) {
        case IEntity.ALL_FIELDS:
          assertEquals(SearchSpecifier.CONTAINS, condition.getSpecifier());
          assertEquals("foo?bar", condition.getValue());
          break;

        case INews.STATE:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals(EnumSet.of(INews.State.NEW), condition.getValue());
          break;

        case INews.HAS_ATTACHMENTS:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals(true, condition.getValue());
          break;

        default:
          fail();
      }
    }

    /* 9) Location is Default Set */
    searchmark = searchmarks.get(8);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.LOCATION, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    assertEquals(Arrays.asList(new IFolderChild[] { fDefaultSet }), CoreUtils.toEntities((Long[][]) conditions.get(0).getValue()));

    /* 10) Location is Default Set OR Location is Custom Set */
    searchmark = searchmarks.get(9);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    List<IFolderChild> locations = CoreUtils.toEntities((Long[][]) conditions.get(0).getValue());
    assertEquals(INews.LOCATION, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    assertEquals(2, locations.size());
    assertContains("My Bookmarks", locations);
    assertContains("Custom", locations);

    /* 11) Location is Folder 1 */
    searchmark = searchmarks.get(10);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.LOCATION, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    locations = CoreUtils.toEntities((Long[][]) conditions.get(0).getValue());
    assertEquals(1, locations.size());
    assertEquals(true, locations.get(0) instanceof IFolder);
    assertEquals("Default Folder 1", locations.get(0).getName());

    /* 12) Location is BookMark 1 */
    searchmark = searchmarks.get(11);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.LOCATION, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    locations = CoreUtils.toEntities((Long[][]) conditions.get(0).getValue());
    assertEquals(1, locations.size());
    assertEquals(true, locations.get(0) instanceof IBookMark);
    assertEquals("Bookmark 1", locations.get(0).getName());

    /*
     * 13) Location is Default Set OR Location is Custom Set OR Location is
     * BookMark1
     */
    searchmark = searchmarks.get(12);
    conditions = searchmark.getSearchConditions();
    assertEquals(3, conditions.size());

    locations = new ArrayList<IFolderChild>();

    for (ISearchCondition condition : conditions) {
      assertEquals(INews.LOCATION, condition.getField().getId());
      assertEquals(SearchSpecifier.IS, condition.getSpecifier());

      locations.addAll(CoreUtils.toEntities((Long[][]) condition.getValue()));
    }

    assertEquals(3, locations.size());
    assertContains("My Bookmarks", locations);
    assertContains("Custom", locations);
    assertContains("Bookmark 1", locations);

    /* 14) Location is Bin 1 */
    searchmark = searchmarks.get(13);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(INews.LOCATION, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.IS, conditions.get(0).getSpecifier());
    locations = CoreUtils.toEntities((Long[][]) conditions.get(0).getValue());
    assertEquals(1, locations.size());
    assertEquals(true, locations.get(0) instanceof INewsBin);
    assertEquals(fNewsBin.getName(), locations.get(0).getName());

    /* 15) Entire News CONTAINS_ALL foo?bar */
    searchmark = searchmarks.get(14);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(IEntity.ALL_FIELDS, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.CONTAINS_ALL, conditions.get(0).getSpecifier());
    assertEquals("foo?bar", conditions.get(0).getValue());

    /* 16) Entire News CONTAINS_NOT foo?bar */
    searchmark = searchmarks.get(15);
    conditions = searchmark.getSearchConditions();
    assertEquals(1, conditions.size());
    assertEquals(IEntity.ALL_FIELDS, conditions.get(0).getField().getId());
    assertEquals(SearchSpecifier.CONTAINS_NOT, conditions.get(0).getSpecifier());
    assertEquals("foo?bar", conditions.get(0).getValue());
  }

  private void assertContains(String name, List<IFolderChild> childs) {
    boolean found = false;
    for (IFolderChild child : childs) {
      if (child.getName().equals(name)) {
        found = true;
        break;
      }
    }

    assertEquals(true, found);
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("null")
  @Test
  public void testExportHierarchyConsistent() throws Exception {

    /* Import */
    List<? extends IEntity> elements = Owl.getInterpreter().importFrom(new FileInputStream(fTmpFileHierarchy));

    assertEquals(2, elements.size());

    IFolder defaultSet = null;
    IFolder customSet = null;
    for (IEntity entity : elements) {
      assertTrue(entity instanceof IFolder);
      IFolder rootFolder = (IFolder) entity;
      if (rootFolder.getName().equals("My Bookmarks"))
        defaultSet = rootFolder;
      else if (rootFolder.getName().equals("Custom"))
        customSet = rootFolder;
    }

    assertNotNull(defaultSet);
    assertEquals(1, defaultSet.getChildren().size());
    assertTrue(defaultSet.getChildren().get(0) instanceof IFolder);
    IFolder folder = (IFolder) defaultSet.getChildren().get(0);
    assertEquals(fDefaultFolder1.getName(), folder.getName());
    assertEquals(1, folder.getChildren().size());
    assertTrue(folder.getChildren().get(0) instanceof IBookMark);
    assertEquals(fBookMark3.getName(), folder.getChildren().get(0).getName());

    assertNotNull(customSet);
    assertEquals(1, customSet.getChildren().size());
    assertTrue(customSet.getChildren().get(0) instanceof IBookMark);
    assertEquals(fBookMark2.getName(), customSet.getChildren().get(0).getName());
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("null")
  @Test
  public void testExportFormats() throws Exception {
    Collection<String> formats = Owl.getInterpreter().getExportFormats();
    assertTrue(formats.contains("opml"));
    assertTrue(formats.contains("xml"));
  }
}