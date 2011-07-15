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

package org.rssowl.core.tests.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.dao.CachingDAO;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IBookMarkDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.dao.ILabelDAO;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.LongOperationMonitor;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests specifically for the CachingDAOs in RSSOwl.
 *
 * @author bpasero
 */
@SuppressWarnings("unchecked")
public class CachingDAOTest extends LargeBlockSizeTest {
  private IModelFactory fFactory;

  private static class NullOperationMonitor extends LongOperationMonitor {
    public NullOperationMonitor() {
      super(new NullProgressMonitor());
    }
  }

  private static final AtomicInteger fgCounter = new AtomicInteger();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    System.setProperty("rssowl.reindex", "false"); //Clear any set reindex marker
    DBManager.getDefault().getReIndexFile().delete();
    DBManager.getDefault().getDefragmentFile().delete();
    DBManager.getDefault().getCleanUpIndexFile().delete();
  }

  private void setProperties(IEntity entity) {
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(entity);
    prefs.putBoolean("booleanKey", true);
    prefs.putInteger("integerKey", 10);
    prefs.putIntegers("integersKey", new int[] { 1, 2, 3, 4, 5 });
    prefs.putLong("longKey", 5l);
    prefs.putLongs("longsKey", new long[] { 10l, 20l, 30l, 40l, 50l });
    prefs.putString("stringKey", "Hello World");
    prefs.putStrings("stringsKey", new String[] { "Hello", "World", "foo", "bar" });

    DynamicDAO.save(entity);
  }

  private void setProperties(IBookMark entity) {
    setProperties((IEntity) entity);
    entity.setCreationDate(new Date(10000));
    entity.setErrorLoading(true);
    entity.setLastVisitDate(new Date(100));
    entity.setPopularity(1000);

    DynamicDAO.save(entity);
  }

  private void setProperties(ISearchMark entity) {
    setProperties((IEntity) entity);

    entity.setCreationDate(new Date(10000));
    entity.setLastVisitDate(new Date(100));
    entity.setPopularity(1000);
    entity.setMatchAllConditions(true);

    ISearchField allField = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(allField, SearchSpecifier.CONTAINS, "foo bar")));

    ISearchField ageInDaysField = fFactory.createSearchField(INews.AGE_IN_DAYS, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(ageInDaysField, SearchSpecifier.IS, 5)));

    ISearchField attachmentContentField = fFactory.createSearchField(INews.ATTACHMENTS_CONTENT, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(attachmentContentField, SearchSpecifier.CONTAINS, "hello world")));

    ISearchField authorField = fFactory.createSearchField(INews.AUTHOR, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(authorField, SearchSpecifier.CONTAINS_NOT, "author help")));

    ISearchField categoriesField = fFactory.createSearchField(INews.CATEGORIES, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(categoriesField, SearchSpecifier.IS_NOT, "categories horror")));

    ISearchField descriptionField = fFactory.createSearchField(INews.DESCRIPTION, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(descriptionField, SearchSpecifier.CONTAINS_ALL, "lorem ipsum dolor sit...")));

    ISearchField feedField = fFactory.createSearchField(INews.FEED, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(feedField, SearchSpecifier.IS, "http://www.rssowl.org")));

    ISearchField hasAttachmentsField = fFactory.createSearchField(INews.HAS_ATTACHMENTS, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(hasAttachmentsField, SearchSpecifier.IS, true)));

    ISearchField labelField = fFactory.createSearchField(INews.LABEL, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(labelField, SearchSpecifier.IS_NOT, "myLabel")));

    ISearchField linkField = fFactory.createSearchField(INews.LINK, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(linkField, SearchSpecifier.IS, "http://www.rssowl.de")));

    ISearchField locationField = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(locationField, SearchSpecifier.SCOPE, new Long[][] { { 1l, 2l, 3l }, { 4l, 5l, 6l }, { 6l, 7l, 8l } })));
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(locationField, SearchSpecifier.IS, new Long[][] { { 1l, 2l, 3l }, { 0l, 0l, 0l }, { 6l, 7l, 8l } })));
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(locationField, SearchSpecifier.IS_AFTER, new Long[][] { { 0l, 0l, 0l }, { 4l, 5l, 6l }, { 6l, 7l, 8l } })));
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(locationField, SearchSpecifier.IS_BEFORE, new Long[][] { { 1l, 2l, 3l }, { 4l, 5l, 6l }, { 0l, 0l, 0l } })));
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(locationField, SearchSpecifier.IS_GREATER_THAN, new Long[][] { { 0l, 0l, 0l }, { 0l, 0l, 0l }, { 0l, 0l, 0l } })));

    ISearchField modifiedDateField = fFactory.createSearchField(INews.MODIFIED_DATE, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(modifiedDateField, SearchSpecifier.IS_AFTER, new Date(1000))));

    ISearchField stateField = fFactory.createSearchField(INews.STATE, INews.class.getName());
    entity.addSearchCondition(DynamicDAO.save(fFactory.createSearchCondition(stateField, SearchSpecifier.IS, EnumSet.of(State.NEW, State.UNREAD, State.UPDATED, State.READ))));

    DynamicDAO.save(entity);
  }

  private void setProperties(INewsBin entity) {
    setProperties((IEntity) entity);

    entity.setCreationDate(new Date(10000));
    entity.setLastVisitDate(new Date(100));
    entity.setPopularity(1000);

    IFeed feed = DynamicDAO.save(fFactory.createFeed(null, URI.create(String.valueOf(fgCounter.incrementAndGet()))));
    INews news1 = fFactory.createNews(null, feed, new Date());
    news1.setState(INews.State.NEW);
    DynamicDAO.save(news1);

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setState(INews.State.UNREAD);
    DynamicDAO.save(news2);

    INews news3 = fFactory.createNews(null, feed, new Date());
    news3.setState(INews.State.READ);
    DynamicDAO.save(news3);

    DynamicDAO.save(feed);

    DynamicDAO.save(fFactory.createNews(news1, entity));
    DynamicDAO.save(fFactory.createNews(news2, entity));
    DynamicDAO.save(fFactory.createNews(news3, entity));

    DynamicDAO.save(entity);
  }

  private void assertProperties(IEntity entity) {
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(entity);
    assertTrue(prefs.getBoolean("booleanKey"));
    assertEquals(10, prefs.getInteger("integerKey"));
    assertTrue(Arrays.equals(new int[] { 1, 2, 3, 4, 5 }, prefs.getIntegers("integersKey")));
    assertEquals(5l, prefs.getLong("longKey"));
    assertTrue(Arrays.equals(new long[] { 10l, 20l, 30l, 40l, 50l }, prefs.getLongs("longsKey")));
    assertEquals("Hello World", prefs.getString("stringKey"));
    assertTrue(Arrays.equals(new String[] { "Hello", "World", "foo", "bar" }, prefs.getStrings("stringsKey")));
  }

  private void assertProperties(IBookMark entity) {
    assertProperties((IEntity) entity);
    assertEquals(new Date(10000), entity.getCreationDate());
    assertEquals(new Date(100), entity.getLastVisitDate());
    assertEquals(true, entity.isErrorLoading());
    assertEquals(1000, entity.getPopularity());
  }

  private void assertProperties(ISearchMark entity) {
    assertProperties((IEntity) entity);

    assertEquals(new Date(10000), entity.getCreationDate());
    assertEquals(new Date(100), entity.getLastVisitDate());
    assertEquals(1000, entity.getPopularity());
    assertEquals(true, entity.matchAllConditions());

    List<ISearchCondition> conditions = entity.getSearchConditions();
    assertEquals(17, conditions.size());
    for (ISearchCondition condition : conditions) {
      switch (condition.getField().getId()) {
        case IEntity.ALL_FIELDS:
          assertEquals(SearchSpecifier.CONTAINS, condition.getSpecifier());
          assertEquals("foo bar", condition.getValue());
          break;

        case INews.AGE_IN_DAYS:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals(5, condition.getValue());
          break;

        case INews.ATTACHMENTS_CONTENT:
          assertEquals(SearchSpecifier.CONTAINS, condition.getSpecifier());
          assertEquals("hello world", condition.getValue());
          break;

        case INews.AUTHOR:
          assertEquals(SearchSpecifier.CONTAINS_NOT, condition.getSpecifier());
          assertEquals("author help", condition.getValue());
          break;

        case INews.CATEGORIES:
          assertEquals(SearchSpecifier.IS_NOT, condition.getSpecifier());
          assertEquals("categories horror", condition.getValue());
          break;

        case INews.DESCRIPTION:
          assertEquals(SearchSpecifier.CONTAINS_ALL, condition.getSpecifier());
          assertEquals("lorem ipsum dolor sit...", condition.getValue());
          break;

        case INews.FEED:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals("http://www.rssowl.org", condition.getValue());
          break;

        case INews.HAS_ATTACHMENTS:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals(true, condition.getValue());
          break;

        case INews.LABEL:
          assertEquals(SearchSpecifier.IS_NOT, condition.getSpecifier());
          assertEquals("myLabel", condition.getValue());
          break;

        case INews.LINK:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals("http://www.rssowl.de", condition.getValue());
          break;

        case INews.LOCATION:
          switch (condition.getSpecifier()) {
            case SCOPE:
              Long[][] value = (Long[][]) condition.getValue();
              assertEquals(3, value.length);
              assertTrue(Arrays.equals(new Long[] { 1l, 2l, 3l }, value[0]));
              assertTrue(Arrays.equals(new Long[] { 4l, 5l, 6l }, value[1]));
              assertTrue(Arrays.equals(new Long[] { 6l, 7l, 8l }, value[2]));

              break;

            case IS:
              value = (Long[][]) condition.getValue();
              assertEquals(3, value.length);
              assertTrue(Arrays.equals(new Long[] { 1l, 2l, 3l }, value[0]));
              assertTrue(Arrays.equals(new Long[] { 0l, 0l, 0l }, value[1]));
              assertTrue(Arrays.equals(new Long[] { 6l, 7l, 8l }, value[2]));

              break;

            case IS_AFTER:
              value = (Long[][]) condition.getValue();
              assertEquals(3, value.length);
              assertTrue(Arrays.equals(new Long[] { 0l, 0l, 0l }, value[0]));
              assertTrue(Arrays.equals(new Long[] { 4l, 5l, 6l }, value[1]));
              assertTrue(Arrays.equals(new Long[] { 6l, 7l, 8l }, value[2]));

              break;

            case IS_BEFORE:
              value = (Long[][]) condition.getValue();
              assertEquals(3, value.length);
              assertTrue(Arrays.equals(new Long[] { 1l, 2l, 3l }, value[0]));
              assertTrue(Arrays.equals(new Long[] { 4l, 5l, 6l }, value[1]));
              assertTrue(Arrays.equals(new Long[] { 0l, 0l, 0l }, value[2]));

              break;

            case IS_GREATER_THAN:
              value = (Long[][]) condition.getValue();
              assertEquals(3, value.length);
              assertTrue(Arrays.equals(new Long[] { 0l, 0l, 0l }, value[0]));
              assertTrue(Arrays.equals(new Long[] { 0l, 0l, 0l }, value[1]));
              assertTrue(Arrays.equals(new Long[] { 0l, 0l, 0l }, value[2]));

              break;

            default:
              fail();
          }
          break;

        case INews.MODIFIED_DATE:
          assertEquals(SearchSpecifier.IS_AFTER, condition.getSpecifier());
          assertEquals(new Date(1000), condition.getValue());
          break;

        case INews.STATE:
          assertEquals(SearchSpecifier.IS, condition.getSpecifier());
          assertEquals(INews.State.getVisible(), condition.getValue());
          break;

        default:
          fail();
      }
    }
  }

  private void assertProperties(INewsBin entity) {
    assertProperties((IEntity) entity);
    assertEquals(new Date(10000), entity.getCreationDate());
    assertEquals(new Date(100), entity.getLastVisitDate());
    assertEquals(1000, entity.getPopularity());

    assertEquals(1, entity.getNewsCount(EnumSet.of(INews.State.NEW)));
    assertEquals(1, entity.getNewsCount(EnumSet.of(INews.State.UNREAD)));
    assertEquals(1, entity.getNewsCount(EnumSet.of(INews.State.READ)));

    assertEquals(1, entity.getNews(EnumSet.of(INews.State.NEW)).size());
    assertEquals(1, entity.getNews(EnumSet.of(INews.State.UNREAD)).size());
    assertEquals(1, entity.getNews(EnumSet.of(INews.State.READ)).size());

    Set<NewsReference> set = new HashSet<NewsReference>();
    set.addAll(entity.getNewsRefs());
    assertEquals(3, set.size());
    for (NewsReference newsRef : set) {
      assertNotNull(newsRef.resolve());
    }
  }

  /**
   * Tests Bookmarks, News Bins, Searchmarks and Folders in a deeply nested
   * hierarchy are fully resolvable from the caching DAOs.
   *
   * @throws Exception
   */
  @Test
  public void testDeepHierarchyResolvedWithCachingDAOSingleRoot() throws Exception {

    /* Folders */
    IFolder root = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));
    Long rootFolderId = root.getId();
    setProperties(root);

    IFolder subRoot1 = DynamicDAO.save(fFactory.createFolder(null, root, "Sub Root 1"));
    Long subRootFolder1Id = subRoot1.getId();
    setProperties(subRoot1);

    IFolder subRoot2 = DynamicDAO.save(fFactory.createFolder(null, root, "Sub Root 2"));
    Long subRootFolder2Id = subRoot2.getId();
    setProperties(subRoot2);

    IFolder subRoot3 = DynamicDAO.save(fFactory.createFolder(null, root, "Sub Root 3"));
    Long subRootFolder3Id = subRoot3.getId();
    setProperties(subRoot3);

    IFolder subSubRoot1 = DynamicDAO.save(fFactory.createFolder(null, subRoot3, "Sub Sub Root 1"));
    Long subSubRootFolder1Id = subSubRoot1.getId();
    setProperties(subSubRoot1);

    IFolder subSubSubRoot1 = DynamicDAO.save(fFactory.createFolder(null, subSubRoot1, "Sub Sub Sub Root 1"));
    Long subSubSubRootFolder1Id = subSubSubRoot1.getId();
    setProperties(subSubSubRoot1);

    /* Book Marks */
    IFeed feed1 = fFactory.createFeed(null, URI.create("1"));
    IBookMark rootBM1 = DynamicDAO.save(fFactory.createBookMark(null, root, new FeedLinkReference(feed1.getLink()), "Root Bookmark 1"));
    Long rootBM1Id = rootBM1.getId();
    setProperties(rootBM1);

    IFeed feed2 = fFactory.createFeed(null, URI.create("2"));
    IBookMark rootBM2 = DynamicDAO.save(fFactory.createBookMark(null, root, new FeedLinkReference(feed2.getLink()), "Root Bookmark 2"));
    Long rootBM2Id = rootBM2.getId();
    setProperties(rootBM2);

    IFeed feed3 = fFactory.createFeed(null, URI.create("3"));
    IBookMark subRoot1BM1 = DynamicDAO.save(fFactory.createBookMark(null, subRoot1, new FeedLinkReference(feed3.getLink()), "Sub Root 1 Bookmark 1"));
    Long subRoot1BM1Id = subRoot1BM1.getId();
    setProperties(subRoot1BM1);

    IFeed feed4 = fFactory.createFeed(null, URI.create("4"));
    IBookMark subRoot1BM2 = DynamicDAO.save(fFactory.createBookMark(null, subRoot1, new FeedLinkReference(feed4.getLink()), "Sub Root 1 Bookmark 2"));
    Long subRoot1BM2Id = subRoot1BM2.getId();
    setProperties(subRoot1BM2);

    IFeed feed5 = fFactory.createFeed(null, URI.create("5"));
    IBookMark subRoot2BM1 = DynamicDAO.save(fFactory.createBookMark(null, subRoot2, new FeedLinkReference(feed5.getLink()), "Sub Root 2 Bookmark 1"));
    Long subRoot2BM1Id = subRoot2BM1.getId();
    setProperties(subRoot2BM1);

    IFeed feed6 = fFactory.createFeed(null, URI.create("6"));
    IBookMark subRoot3BM1 = DynamicDAO.save(fFactory.createBookMark(null, subRoot3, new FeedLinkReference(feed6.getLink()), "Sub Root 3 Bookmark 1"));
    Long subRoot3BM1Id = subRoot3BM1.getId();
    setProperties(subRoot3BM1);

    IFeed feed7 = fFactory.createFeed(null, URI.create("7"));
    IBookMark subSubRoot1BM1 = DynamicDAO.save(fFactory.createBookMark(null, subSubRoot1, new FeedLinkReference(feed7.getLink()), "Sub Sub Root 1 Bookmark 1"));
    Long subSubRoot1BM1Id = subSubRoot1BM1.getId();
    setProperties(subSubRoot1BM1);

    IFeed feed8 = fFactory.createFeed(null, URI.create("8"));
    IBookMark subSubSubRoot1BM1 = DynamicDAO.save(fFactory.createBookMark(null, subSubSubRoot1, new FeedLinkReference(feed8.getLink()), "Sub Sub Sub Root 1 Bookmark 1"));
    Long subSubSubRoot1BM1Id = subSubSubRoot1BM1.getId();
    setProperties(subSubSubRoot1BM1);

    /* News Bins */
    INewsBin rootNB1 = DynamicDAO.save(fFactory.createNewsBin(null, root, "Root Newsbin 1"));
    Long rootNB1Id = rootNB1.getId();
    setProperties(rootNB1);

    INewsBin rootNB2 = DynamicDAO.save(fFactory.createNewsBin(null, root, "Root Newsbin 2"));
    Long rootNB2Id = rootNB2.getId();
    setProperties(rootNB2);

    INewsBin subRoot1NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot1, "Sub Root 1 Newsbin 1"));
    Long subRoot1NB1Id = subRoot1NB1.getId();
    setProperties(subRoot1NB1);

    INewsBin subRoot1NB2 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot1, "Sub Root 1 Newsbin 2"));
    Long subRoot1NB2Id = subRoot1NB2.getId();
    setProperties(subRoot1NB2);

    INewsBin subRoot2NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot2, "Sub Root 2 Newsbin 1"));
    Long subRoot2NB1Id = subRoot2NB1.getId();
    setProperties(subRoot2NB1);

    INewsBin subRoot3NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot3, "Sub Root 3 Newsbin 1"));
    Long subRoot3NB1Id = subRoot3NB1.getId();
    setProperties(subRoot3NB1);

    INewsBin subSubRoot1NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subSubRoot1, "Sub Sub Root 1 Newsbin 1"));
    Long subSubRoot1NB1Id = subSubRoot1NB1.getId();
    setProperties(subSubRoot1NB1);

    INewsBin subSubSubRoot1NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subSubSubRoot1, "Sub Sub Sub Root 1 Newsbin 1"));
    Long subSubSubRoot1NB1Id = subSubSubRoot1NB1.getId();
    setProperties(subSubSubRoot1NB1);

    /* Search Marks */
    ISearchMark rootSM1 = DynamicDAO.save(fFactory.createSearchMark(null, root, "Root Searchmark 1"));
    Long rootSM1Id = rootSM1.getId();
    setProperties(rootSM1);

    ISearchMark rootSM2 = DynamicDAO.save(fFactory.createSearchMark(null, root, "Root Searchmark 2"));
    Long rootSM2Id = rootSM2.getId();
    setProperties(rootSM2);

    ISearchMark subRoot1SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot1, "Sub Root 1 Searchmark 1"));
    Long subRoot1SM1Id = subRoot1SM1.getId();
    setProperties(subRoot1SM1);

    ISearchMark subRoot1SM2 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot1, "Sub Root 1 Searchmark 2"));
    Long subRoot1SM2Id = subRoot1SM2.getId();
    setProperties(subRoot1SM2);

    ISearchMark subRoot2SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot2, "Sub Root 2 Searchmark 1"));
    Long subRoot2SM1Id = subRoot2SM1.getId();
    setProperties(subRoot2SM1);

    ISearchMark subRoot3SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot3, "Sub Root 3 Searchmark 1"));
    Long subRoot3SM1Id = subRoot3SM1.getId();
    setProperties(subRoot3SM1);

    ISearchMark subSubRoot1SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subSubRoot1, "Sub Sub Root 1 Searchmark 1"));
    Long subSubRoot1SM1Id = subSubRoot1SM1.getId();
    setProperties(subSubRoot1SM1);

    ISearchMark subSubSubRoot1SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subSubSubRoot1, "Sub Sub Sub Root 1 Searchmark 1"));
    Long subSubSubRoot1SM1Id = subSubSubRoot1SM1.getId();
    setProperties(subSubSubRoot1SM1);

    DynamicDAO.save(root);

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);

    root = null;
    subRoot1 = null;
    subRoot2 = null;
    subRoot3 = null;
    subSubRoot1 = null;
    subSubSubRoot1 = null;

    rootBM1 = null;
    rootBM2 = null;
    subRoot1BM1 = null;
    subRoot1BM2 = null;
    subRoot2BM1 = null;
    subRoot3BM1 = null;
    subSubRoot1BM1 = null;
    subSubSubRoot1BM1 = null;

    rootNB1 = null;
    rootNB2 = null;
    subRoot1NB1 = null;
    subRoot1NB2 = null;
    subRoot2NB1 = null;
    subRoot3NB1 = null;
    subSubRoot1NB1 = null;
    subSubSubRoot1NB1 = null;

    rootSM1 = null;
    rootSM2 = null;
    subRoot1SM1 = null;
    subRoot1SM2 = null;
    subRoot2SM1 = null;
    subRoot3SM1 = null;
    subSubRoot1SM1 = null;
    subSubSubRoot1SM1 = null;

    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), false, false);

    /* Assert Folders */
    CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(IFolderDAO.class);
    root = (IFolder) dao.load(rootFolderId);
    assertNotNull(root);
    assertEquals("Root", root.getName());
    assertProperties(root);

    subRoot1 = (IFolder) dao.load(subRootFolder1Id);
    assertNotNull(subRoot1);
    assertEquals("Sub Root 1", subRoot1.getName());
    assertProperties(subRoot1);

    subRoot2 = (IFolder) dao.load(subRootFolder2Id);
    assertNotNull(subRoot2);
    assertEquals("Sub Root 2", subRoot2.getName());
    assertProperties(subRoot2);

    subRoot3 = (IFolder) dao.load(subRootFolder3Id);
    assertNotNull(subRoot3);
    assertEquals("Sub Root 3", subRoot3.getName());
    assertProperties(subRoot3);

    subSubRoot1 = (IFolder) dao.load(subSubRootFolder1Id);
    assertNotNull(subSubRoot1);
    assertEquals("Sub Sub Root 1", subSubRoot1.getName());
    assertProperties(subSubRoot1);

    subSubSubRoot1 = (IFolder) dao.load(subSubSubRootFolder1Id);
    assertNotNull(subSubSubRoot1);
    assertEquals("Sub Sub Sub Root 1", subSubSubRoot1.getName());
    assertProperties(subSubSubRoot1);

    /* Assert Bookmarks */
    dao = (CachingDAO) DynamicDAO.getDAO(IBookMarkDAO.class);

    rootBM1 = (IBookMark) dao.load(rootBM1Id);
    assertNotNull(rootBM1);
    assertEquals("Root Bookmark 1", rootBM1.getName());
    assertProperties(rootBM1);

    rootBM2 = (IBookMark) dao.load(rootBM2Id);
    assertNotNull(rootBM2);
    assertEquals("Root Bookmark 2", rootBM2.getName());
    assertProperties(rootBM2);

    subRoot1BM1 = (IBookMark) dao.load(subRoot1BM1Id);
    assertNotNull(subRoot1BM1);
    assertEquals("Sub Root 1 Bookmark 1", subRoot1BM1.getName());
    assertProperties(subRoot1BM1);

    subRoot1BM2 = (IBookMark) dao.load(subRoot1BM2Id);
    assertNotNull(subRoot1BM2);
    assertEquals("Sub Root 1 Bookmark 2", subRoot1BM2.getName());
    assertProperties(subRoot1BM2);

    subRoot2BM1 = (IBookMark) dao.load(subRoot2BM1Id);
    assertNotNull(subRoot2BM1);
    assertEquals("Sub Root 2 Bookmark 1", subRoot2BM1.getName());
    assertProperties(subRoot2BM1);

    subRoot3BM1 = (IBookMark) dao.load(subRoot3BM1Id);
    assertNotNull(subRoot3BM1);
    assertEquals("Sub Root 3 Bookmark 1", subRoot3BM1.getName());
    assertProperties(subRoot3BM1);

    subSubRoot1BM1 = (IBookMark) dao.load(subSubRoot1BM1Id);
    assertNotNull(subSubRoot1BM1);
    assertEquals("Sub Sub Root 1 Bookmark 1", subSubRoot1BM1.getName());
    assertProperties(subSubRoot1BM1);

    subSubSubRoot1BM1 = (IBookMark) dao.load(subSubSubRoot1BM1Id);
    assertNotNull(subSubSubRoot1BM1);
    assertEquals("Sub Sub Sub Root 1 Bookmark 1", subSubSubRoot1BM1.getName());
    assertProperties(subSubSubRoot1BM1);

    /* Assert News Bins */
    dao = (CachingDAO) DynamicDAO.getDAO(INewsBinDAO.class);

    rootNB1 = (INewsBin) dao.load(rootNB1Id);
    assertNotNull(rootNB1);
    assertEquals("Root Newsbin 1", rootNB1.getName());
    assertProperties(rootNB1);

    rootNB2 = (INewsBin) dao.load(rootNB2Id);
    assertNotNull(rootNB2);
    assertEquals("Root Newsbin 2", rootNB2.getName());
    assertProperties(rootNB2);

    subRoot1NB1 = (INewsBin) dao.load(subRoot1NB1Id);
    assertNotNull(subRoot1NB1);
    assertEquals("Sub Root 1 Newsbin 1", subRoot1NB1.getName());
    assertProperties(subRoot1NB1);

    subRoot1NB2 = (INewsBin) dao.load(subRoot1NB2Id);
    assertNotNull(subRoot1NB2);
    assertEquals("Sub Root 1 Newsbin 2", subRoot1NB2.getName());
    assertProperties(subRoot1NB2);

    subRoot2NB1 = (INewsBin) dao.load(subRoot2NB1Id);
    assertNotNull(subRoot2NB1);
    assertEquals("Sub Root 2 Newsbin 1", subRoot2NB1.getName());
    assertProperties(subRoot2NB1);

    subRoot3NB1 = (INewsBin) dao.load(subRoot3NB1Id);
    assertNotNull(subRoot3NB1);
    assertEquals("Sub Root 3 Newsbin 1", subRoot3NB1.getName());
    assertProperties(subRoot3NB1);

    subSubRoot1NB1 = (INewsBin) dao.load(subSubRoot1NB1Id);
    assertNotNull(subSubRoot1NB1);
    assertEquals("Sub Sub Root 1 Newsbin 1", subSubRoot1NB1.getName());
    assertProperties(subSubRoot1NB1);

    subSubSubRoot1NB1 = (INewsBin) dao.load(subSubSubRoot1NB1Id);
    assertNotNull(subSubSubRoot1NB1);
    assertEquals("Sub Sub Sub Root 1 Newsbin 1", subSubSubRoot1NB1.getName());
    assertProperties(subSubSubRoot1NB1);

    /* Assert Search Marks */
    dao = (CachingDAO) DynamicDAO.getDAO(ISearchMarkDAO.class);

    rootSM1 = (ISearchMark) dao.load(rootSM1Id);
    assertNotNull(rootSM1);
    assertEquals("Root Searchmark 1", rootSM1.getName());
    assertProperties(rootSM1);

    rootSM2 = (ISearchMark) dao.load(rootSM2Id);
    assertNotNull(rootSM2);
    assertEquals("Root Searchmark 2", rootSM2.getName());
    assertProperties(rootSM2);

    subRoot1SM1 = (ISearchMark) dao.load(subRoot1SM1Id);
    assertNotNull(subRoot1SM1);
    assertEquals("Sub Root 1 Searchmark 1", subRoot1SM1.getName());
    assertProperties(subRoot1SM1);

    subRoot1SM2 = (ISearchMark) dao.load(subRoot1SM2Id);
    assertNotNull(subRoot1SM2);
    assertEquals("Sub Root 1 Searchmark 2", subRoot1SM2.getName());
    assertProperties(subRoot1SM2);

    subRoot2SM1 = (ISearchMark) dao.load(subRoot2SM1Id);
    assertNotNull(subRoot2SM1);
    assertEquals("Sub Root 2 Searchmark 1", subRoot2SM1.getName());
    assertProperties(subRoot2SM1);

    subRoot3SM1 = (ISearchMark) dao.load(subRoot3SM1Id);
    assertNotNull(subRoot3SM1);
    assertEquals("Sub Root 3 Searchmark 1", subRoot3SM1.getName());
    assertProperties(subRoot3SM1);

    subSubRoot1SM1 = (ISearchMark) dao.load(subSubRoot1SM1Id);
    assertNotNull(subSubRoot1SM1);
    assertEquals("Sub Sub Root 1 Searchmark 1", subSubRoot1SM1.getName());
    assertProperties(subSubRoot1SM1);

    subSubSubRoot1SM1 = (ISearchMark) dao.load(subSubSubRoot1SM1Id);
    assertNotNull(subSubSubRoot1SM1);
    assertEquals("Sub Sub Sub Root 1 Searchmark 1", subSubSubRoot1SM1.getName());
    assertProperties(subSubSubRoot1SM1);
  }

  /**
   * Tests Bookmarks, News Bins, Searchmarks and Folders in a deeply nested
   * hierarchy of multiple root folders are fully resolvable from the caching
   * DAOs.
   *
   * @throws Exception
   */
  @Test
  public void testDeepHierarchyResolvedWithCachingDAOMultiRoot() throws Exception {

    /* Folders */
    IFolder root1 = DynamicDAO.save(fFactory.createFolder(null, null, "Root 1"));
    Long rootFolder1Id = root1.getId();
    setProperties(root1);

    IFolder root2 = DynamicDAO.save(fFactory.createFolder(null, null, "Root 2"));
    Long rootFolder2Id = root2.getId();
    setProperties(root2);

    IFolder root3 = DynamicDAO.save(fFactory.createFolder(null, null, "Root 3"));
    Long rootFolder3Id = root3.getId();
    setProperties(root3);

    IFolder subRoot1 = DynamicDAO.save(fFactory.createFolder(null, root1, "Sub Root 1"));
    Long subRootFolder1Id = subRoot1.getId();
    setProperties(subRoot1);

    IFolder subRoot2 = DynamicDAO.save(fFactory.createFolder(null, root2, "Sub Root 2"));
    Long subRootFolder2Id = subRoot2.getId();
    setProperties(subRoot2);

    IFolder subRoot3 = DynamicDAO.save(fFactory.createFolder(null, root3, "Sub Root 3"));
    Long subRootFolder3Id = subRoot3.getId();
    setProperties(subRoot3);

    IFolder subSubRoot1 = DynamicDAO.save(fFactory.createFolder(null, subRoot3, "Sub Sub Root 1"));
    Long subSubRootFolder1Id = subSubRoot1.getId();
    setProperties(subSubRoot1);

    IFolder subSubSubRoot1 = DynamicDAO.save(fFactory.createFolder(null, subSubRoot1, "Sub Sub Sub Root 1"));
    Long subSubSubRootFolder1Id = subSubSubRoot1.getId();
    setProperties(subSubSubRoot1);

    /* Book Marks */
    IFeed feed1 = fFactory.createFeed(null, URI.create("1"));
    IBookMark rootBM1 = DynamicDAO.save(fFactory.createBookMark(null, root1, new FeedLinkReference(feed1.getLink()), "Root Bookmark 1"));
    Long rootBM1Id = rootBM1.getId();
    setProperties(rootBM1);

    IFeed feed2 = fFactory.createFeed(null, URI.create("2"));
    IBookMark rootBM2 = DynamicDAO.save(fFactory.createBookMark(null, root1, new FeedLinkReference(feed2.getLink()), "Root Bookmark 2"));
    Long rootBM2Id = rootBM2.getId();
    setProperties(rootBM2);

    IFeed feed3 = fFactory.createFeed(null, URI.create("3"));
    IBookMark subRoot1BM1 = DynamicDAO.save(fFactory.createBookMark(null, subRoot1, new FeedLinkReference(feed3.getLink()), "Sub Root 1 Bookmark 1"));
    Long subRoot1BM1Id = subRoot1BM1.getId();
    setProperties(subRoot1BM1);

    IFeed feed4 = fFactory.createFeed(null, URI.create("4"));
    IBookMark subRoot1BM2 = DynamicDAO.save(fFactory.createBookMark(null, subRoot1, new FeedLinkReference(feed4.getLink()), "Sub Root 1 Bookmark 2"));
    Long subRoot1BM2Id = subRoot1BM2.getId();
    setProperties(subRoot1BM2);

    IFeed feed5 = fFactory.createFeed(null, URI.create("5"));
    IBookMark subRoot2BM1 = DynamicDAO.save(fFactory.createBookMark(null, subRoot2, new FeedLinkReference(feed5.getLink()), "Sub Root 2 Bookmark 1"));
    Long subRoot2BM1Id = subRoot2BM1.getId();
    setProperties(subRoot2BM1);

    IFeed feed6 = fFactory.createFeed(null, URI.create("6"));
    IBookMark subRoot3BM1 = DynamicDAO.save(fFactory.createBookMark(null, subRoot3, new FeedLinkReference(feed6.getLink()), "Sub Root 3 Bookmark 1"));
    Long subRoot3BM1Id = subRoot3BM1.getId();
    setProperties(subRoot3BM1);

    IFeed feed7 = fFactory.createFeed(null, URI.create("7"));
    IBookMark subSubRoot1BM1 = DynamicDAO.save(fFactory.createBookMark(null, subSubRoot1, new FeedLinkReference(feed7.getLink()), "Sub Sub Root 1 Bookmark 1"));
    Long subSubRoot1BM1Id = subSubRoot1BM1.getId();
    setProperties(subSubRoot1BM1);

    IFeed feed8 = fFactory.createFeed(null, URI.create("8"));
    IBookMark subSubSubRoot1BM1 = DynamicDAO.save(fFactory.createBookMark(null, subSubSubRoot1, new FeedLinkReference(feed8.getLink()), "Sub Sub Sub Root 1 Bookmark 1"));
    Long subSubSubRoot1BM1Id = subSubSubRoot1BM1.getId();
    setProperties(subSubSubRoot1BM1);

    /* News Bins */
    INewsBin rootNB1 = DynamicDAO.save(fFactory.createNewsBin(null, root1, "Root Newsbin 1"));
    Long rootNB1Id = rootNB1.getId();
    setProperties(rootNB1);

    INewsBin rootNB2 = DynamicDAO.save(fFactory.createNewsBin(null, root1, "Root Newsbin 2"));
    Long rootNB2Id = rootNB2.getId();
    setProperties(rootNB2);

    INewsBin subRoot1NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot1, "Sub Root 1 Newsbin 1"));
    Long subRoot1NB1Id = subRoot1NB1.getId();
    setProperties(subRoot1NB1);

    INewsBin subRoot1NB2 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot1, "Sub Root 1 Newsbin 2"));
    Long subRoot1NB2Id = subRoot1NB2.getId();
    setProperties(subRoot1NB2);

    INewsBin subRoot2NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot2, "Sub Root 2 Newsbin 1"));
    Long subRoot2NB1Id = subRoot2NB1.getId();
    setProperties(subRoot2NB1);

    INewsBin subRoot3NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subRoot3, "Sub Root 3 Newsbin 1"));
    Long subRoot3NB1Id = subRoot3NB1.getId();
    setProperties(subRoot3NB1);

    INewsBin subSubRoot1NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subSubRoot1, "Sub Sub Root 1 Newsbin 1"));
    Long subSubRoot1NB1Id = subSubRoot1NB1.getId();
    setProperties(subSubRoot1NB1);

    INewsBin subSubSubRoot1NB1 = DynamicDAO.save(fFactory.createNewsBin(null, subSubSubRoot1, "Sub Sub Sub Root 1 Newsbin 1"));
    Long subSubSubRoot1NB1Id = subSubSubRoot1NB1.getId();
    setProperties(subSubSubRoot1NB1);

    /* Search Marks */
    ISearchMark rootSM1 = DynamicDAO.save(fFactory.createSearchMark(null, root1, "Root Searchmark 1"));
    Long rootSM1Id = rootSM1.getId();
    setProperties(rootSM1);

    ISearchMark rootSM2 = DynamicDAO.save(fFactory.createSearchMark(null, root1, "Root Searchmark 2"));
    Long rootSM2Id = rootSM2.getId();
    setProperties(rootSM2);

    ISearchMark subRoot1SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot1, "Sub Root 1 Searchmark 1"));
    Long subRoot1SM1Id = subRoot1SM1.getId();
    setProperties(subRoot1SM1);

    ISearchMark subRoot1SM2 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot1, "Sub Root 1 Searchmark 2"));
    Long subRoot1SM2Id = subRoot1SM2.getId();
    setProperties(subRoot1SM2);

    ISearchMark subRoot2SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot2, "Sub Root 2 Searchmark 1"));
    Long subRoot2SM1Id = subRoot2SM1.getId();
    setProperties(subRoot2SM1);

    ISearchMark subRoot3SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subRoot3, "Sub Root 3 Searchmark 1"));
    Long subRoot3SM1Id = subRoot3SM1.getId();
    setProperties(subRoot3SM1);

    ISearchMark subSubRoot1SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subSubRoot1, "Sub Sub Root 1 Searchmark 1"));
    Long subSubRoot1SM1Id = subSubRoot1SM1.getId();
    setProperties(subSubRoot1SM1);

    ISearchMark subSubSubRoot1SM1 = DynamicDAO.save(fFactory.createSearchMark(null, subSubSubRoot1, "Sub Sub Sub Root 1 Searchmark 1"));
    Long subSubSubRoot1SM1Id = subSubSubRoot1SM1.getId();
    setProperties(subSubSubRoot1SM1);

    DynamicDAO.save(root1);

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);

    root1 = null;
    root2 = null;
    root3 = null;
    subRoot1 = null;
    subRoot2 = null;
    subRoot3 = null;
    subSubRoot1 = null;
    subSubSubRoot1 = null;

    rootBM1 = null;
    rootBM2 = null;
    subRoot1BM1 = null;
    subRoot1BM2 = null;
    subRoot2BM1 = null;
    subRoot3BM1 = null;
    subSubRoot1BM1 = null;
    subSubSubRoot1BM1 = null;

    rootNB1 = null;
    rootNB2 = null;
    subRoot1NB1 = null;
    subRoot1NB2 = null;
    subRoot2NB1 = null;
    subRoot3NB1 = null;
    subSubRoot1NB1 = null;
    subSubSubRoot1NB1 = null;

    rootSM1 = null;
    rootSM2 = null;
    subRoot1SM1 = null;
    subRoot1SM2 = null;
    subRoot2SM1 = null;
    subRoot3SM1 = null;
    subSubRoot1SM1 = null;
    subSubSubRoot1SM1 = null;

    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), false, false);

    /* Assert Folders */
    CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(IFolderDAO.class);
    root1 = (IFolder) dao.load(rootFolder1Id);
    assertNotNull(root1);
    assertEquals("Root 1", root1.getName());
    assertProperties(root1);

    root2 = (IFolder) dao.load(rootFolder2Id);
    assertNotNull(root2);
    assertEquals("Root 2", root2.getName());
    assertProperties(root2);

    root3 = (IFolder) dao.load(rootFolder3Id);
    assertNotNull(root3);
    assertEquals("Root 3", root3.getName());
    assertProperties(root3);

    subRoot1 = (IFolder) dao.load(subRootFolder1Id);
    assertNotNull(subRoot1);
    assertEquals("Sub Root 1", subRoot1.getName());
    assertProperties(subRoot1);

    subRoot2 = (IFolder) dao.load(subRootFolder2Id);
    assertNotNull(subRoot2);
    assertEquals("Sub Root 2", subRoot2.getName());
    assertProperties(subRoot2);

    subRoot3 = (IFolder) dao.load(subRootFolder3Id);
    assertNotNull(subRoot3);
    assertEquals("Sub Root 3", subRoot3.getName());
    assertProperties(subRoot3);

    subSubRoot1 = (IFolder) dao.load(subSubRootFolder1Id);
    assertNotNull(subSubRoot1);
    assertEquals("Sub Sub Root 1", subSubRoot1.getName());
    assertProperties(subSubRoot1);

    subSubSubRoot1 = (IFolder) dao.load(subSubSubRootFolder1Id);
    assertNotNull(subSubSubRoot1);
    assertEquals("Sub Sub Sub Root 1", subSubSubRoot1.getName());
    assertProperties(subSubSubRoot1);

    /* Assert Bookmarks */
    dao = (CachingDAO) DynamicDAO.getDAO(IBookMarkDAO.class);

    rootBM1 = (IBookMark) dao.load(rootBM1Id);
    assertNotNull(rootBM1);
    assertEquals("Root Bookmark 1", rootBM1.getName());
    assertProperties(rootBM1);

    rootBM2 = (IBookMark) dao.load(rootBM2Id);
    assertNotNull(rootBM2);
    assertEquals("Root Bookmark 2", rootBM2.getName());
    assertProperties(rootBM2);

    subRoot1BM1 = (IBookMark) dao.load(subRoot1BM1Id);
    assertNotNull(subRoot1BM1);
    assertEquals("Sub Root 1 Bookmark 1", subRoot1BM1.getName());
    assertProperties(subRoot1BM1);

    subRoot1BM2 = (IBookMark) dao.load(subRoot1BM2Id);
    assertNotNull(subRoot1BM2);
    assertEquals("Sub Root 1 Bookmark 2", subRoot1BM2.getName());
    assertProperties(subRoot1BM2);

    subRoot2BM1 = (IBookMark) dao.load(subRoot2BM1Id);
    assertNotNull(subRoot2BM1);
    assertEquals("Sub Root 2 Bookmark 1", subRoot2BM1.getName());
    assertProperties(subRoot2BM1);

    subRoot3BM1 = (IBookMark) dao.load(subRoot3BM1Id);
    assertNotNull(subRoot3BM1);
    assertEquals("Sub Root 3 Bookmark 1", subRoot3BM1.getName());
    assertProperties(subRoot3BM1);

    subSubRoot1BM1 = (IBookMark) dao.load(subSubRoot1BM1Id);
    assertNotNull(subSubRoot1BM1);
    assertEquals("Sub Sub Root 1 Bookmark 1", subSubRoot1BM1.getName());
    assertProperties(subSubRoot1BM1);

    subSubSubRoot1BM1 = (IBookMark) dao.load(subSubSubRoot1BM1Id);
    assertNotNull(subSubSubRoot1BM1);
    assertEquals("Sub Sub Sub Root 1 Bookmark 1", subSubSubRoot1BM1.getName());
    assertProperties(subSubSubRoot1BM1);

    /* Assert News Bins */
    dao = (CachingDAO) DynamicDAO.getDAO(INewsBinDAO.class);

    rootNB1 = (INewsBin) dao.load(rootNB1Id);
    assertNotNull(rootNB1);
    assertEquals("Root Newsbin 1", rootNB1.getName());
    assertProperties(rootNB1);

    rootNB2 = (INewsBin) dao.load(rootNB2Id);
    assertNotNull(rootNB2);
    assertEquals("Root Newsbin 2", rootNB2.getName());
    assertProperties(rootNB2);

    subRoot1NB1 = (INewsBin) dao.load(subRoot1NB1Id);
    assertNotNull(subRoot1NB1);
    assertEquals("Sub Root 1 Newsbin 1", subRoot1NB1.getName());
    assertProperties(subRoot1NB1);

    subRoot1NB2 = (INewsBin) dao.load(subRoot1NB2Id);
    assertNotNull(subRoot1NB2);
    assertEquals("Sub Root 1 Newsbin 2", subRoot1NB2.getName());
    assertProperties(subRoot1NB2);

    subRoot2NB1 = (INewsBin) dao.load(subRoot2NB1Id);
    assertNotNull(subRoot2NB1);
    assertEquals("Sub Root 2 Newsbin 1", subRoot2NB1.getName());
    assertProperties(subRoot2NB1);

    subRoot3NB1 = (INewsBin) dao.load(subRoot3NB1Id);
    assertNotNull(subRoot3NB1);
    assertEquals("Sub Root 3 Newsbin 1", subRoot3NB1.getName());
    assertProperties(subRoot3NB1);

    subSubRoot1NB1 = (INewsBin) dao.load(subSubRoot1NB1Id);
    assertNotNull(subSubRoot1NB1);
    assertEquals("Sub Sub Root 1 Newsbin 1", subSubRoot1NB1.getName());
    assertProperties(subSubRoot1NB1);

    subSubSubRoot1NB1 = (INewsBin) dao.load(subSubSubRoot1NB1Id);
    assertNotNull(subSubSubRoot1NB1);
    assertEquals("Sub Sub Sub Root 1 Newsbin 1", subSubSubRoot1NB1.getName());
    assertProperties(subSubSubRoot1NB1);

    /* Assert Search Marks */
    dao = (CachingDAO) DynamicDAO.getDAO(ISearchMarkDAO.class);

    rootSM1 = (ISearchMark) dao.load(rootSM1Id);
    assertNotNull(rootSM1);
    assertEquals("Root Searchmark 1", rootSM1.getName());
    assertProperties(rootSM1);

    rootSM2 = (ISearchMark) dao.load(rootSM2Id);
    assertNotNull(rootSM2);
    assertEquals("Root Searchmark 2", rootSM2.getName());
    assertProperties(rootSM2);

    subRoot1SM1 = (ISearchMark) dao.load(subRoot1SM1Id);
    assertNotNull(subRoot1SM1);
    assertEquals("Sub Root 1 Searchmark 1", subRoot1SM1.getName());
    assertProperties(subRoot1SM1);

    subRoot1SM2 = (ISearchMark) dao.load(subRoot1SM2Id);
    assertNotNull(subRoot1SM2);
    assertEquals("Sub Root 1 Searchmark 2", subRoot1SM2.getName());
    assertProperties(subRoot1SM2);

    subRoot2SM1 = (ISearchMark) dao.load(subRoot2SM1Id);
    assertNotNull(subRoot2SM1);
    assertEquals("Sub Root 2 Searchmark 1", subRoot2SM1.getName());
    assertProperties(subRoot2SM1);

    subRoot3SM1 = (ISearchMark) dao.load(subRoot3SM1Id);
    assertNotNull(subRoot3SM1);
    assertEquals("Sub Root 3 Searchmark 1", subRoot3SM1.getName());
    assertProperties(subRoot3SM1);

    subSubRoot1SM1 = (ISearchMark) dao.load(subSubRoot1SM1Id);
    assertNotNull(subSubRoot1SM1);
    assertEquals("Sub Sub Root 1 Searchmark 1", subSubRoot1SM1.getName());
    assertProperties(subSubRoot1SM1);

    subSubSubRoot1SM1 = (ISearchMark) dao.load(subSubSubRoot1SM1Id);
    assertNotNull(subSubSubRoot1SM1);
    assertEquals("Sub Sub Sub Root 1 Searchmark 1", subSubSubRoot1SM1.getName());
    assertProperties(subSubSubRoot1SM1);
  }

  /**
   * Tests Bookmarks, News Bins, Searchmarks and Folders in a crazy deeply
   * nested hierarchy are fully resolvable from the caching DAOs.
   *
   * @throws Exception
   */
  @Test
  public void testCrazyDeepHierarchyResolvedWithCachingDAOSingleRoot() throws Exception {
    IFolder folder = null;
    for (int i = 0; i < 20; i++) {
      folder = DynamicDAO.save(fFactory.createFolder(null, folder, "Folder " + i));
      setProperties(folder);

      IFeed feed = fFactory.createFeed(null, URI.create("1"));
      IBookMark bm = DynamicDAO.save(fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark " + i));
      setProperties(bm);

      INewsBin nb = DynamicDAO.save(fFactory.createNewsBin(null, folder, "Newsbin " + i));
      setProperties(nb);

      ISearchMark sm = DynamicDAO.save(fFactory.createSearchMark(null, folder, "Searchmark " + i));
      setProperties(sm);

      DynamicDAO.save(folder);
    }

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);
    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), false, false);

    /* Assert Folders */
    CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(IFolderDAO.class);
    Collection all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof IFolder)
        assertProperties((IFolder) object);
      else
        fail();
    }

    /* Assert Bookmarks */
    dao = (CachingDAO) DynamicDAO.getDAO(IBookMarkDAO.class);
    all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof IBookMark)
        assertProperties((IBookMark) object);
      else
        fail();
    }

    /* Assert News Bins */
    dao = (CachingDAO) DynamicDAO.getDAO(INewsBinDAO.class);
    all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof INewsBin)
        assertProperties((INewsBin) object);
      else
        fail();
    }

    /* Assert Search Marks */
    dao = (CachingDAO) DynamicDAO.getDAO(ISearchMarkDAO.class);
    all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof ISearchMark)
        assertProperties((ISearchMark) object);
      else
        fail();
    }
  }

  /**
   * Tests Bookmarks, News Bins, Searchmarks and Folders in a crazy deeply
   * nested hierarchy are fully resolvable from the caching DAOs.
   *
   * @throws Exception
   */
  @Test
  public void testCrazyDeepHierarchyResolvedWithCachingDAOSingleRootEmergencyStartup() throws Exception {
    IFolder folder = null;
    for (int i = 0; i < 20; i++) {
      folder = DynamicDAO.save(fFactory.createFolder(null, folder, "Folder " + i));
      setProperties(folder);

      IFeed feed = fFactory.createFeed(null, URI.create("1"));
      IBookMark bm = DynamicDAO.save(fFactory.createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "Bookmark " + i));
      setProperties(bm);

      INewsBin nb = DynamicDAO.save(fFactory.createNewsBin(null, folder, "Newsbin " + i));
      setProperties(nb);

      ISearchMark sm = DynamicDAO.save(fFactory.createSearchMark(null, folder, "Searchmark " + i));
      setProperties(sm);

      DynamicDAO.save(folder);
    }

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);
    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), true, false);

    /* Assert Folders */
    {
      CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(IFolderDAO.class);
      Collection all = dao.loadAll();
      assertEquals(20, all.size());
      for (Object object : all) {
        if (object instanceof IFolder)
          assertProperties((IFolder) object);
        else
          fail();
      }

      /* Assert Bookmarks */
      dao = (CachingDAO) DynamicDAO.getDAO(IBookMarkDAO.class);
      all = dao.loadAll();
      assertEquals(20, all.size());
      for (Object object : all) {
        if (object instanceof IBookMark)
          assertProperties((IBookMark) object);
        else
          fail();
      }

      /* Assert News Bins */
      dao = (CachingDAO) DynamicDAO.getDAO(INewsBinDAO.class);
      all = dao.loadAll();
      assertEquals(20, all.size());
      for (Object object : all) {
        if (object instanceof INewsBin)
          assertProperties((INewsBin) object);
        else
          fail();
      }

      /* Assert Search Marks */
      dao = (CachingDAO) DynamicDAO.getDAO(ISearchMarkDAO.class);
      all = dao.loadAll();
      assertEquals(20, all.size());
      for (Object object : all) {
        if (object instanceof ISearchMark)
          assertProperties((ISearchMark) object);
        else
          fail();
      }
    }

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);
    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), false, false);

    /* Assert Folders */
    CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(IFolderDAO.class);
    Collection all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof IFolder)
        assertProperties((IFolder) object);
      else
        fail();
    }

    /* Assert Bookmarks */
    dao = (CachingDAO) DynamicDAO.getDAO(IBookMarkDAO.class);
    all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof IBookMark)
        assertProperties((IBookMark) object);
      else
        fail();
    }

    /* Assert News Bins */
    dao = (CachingDAO) DynamicDAO.getDAO(INewsBinDAO.class);
    all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof INewsBin)
        assertProperties((INewsBin) object);
      else
        fail();
    }

    /* Assert Search Marks */
    dao = (CachingDAO) DynamicDAO.getDAO(ISearchMarkDAO.class);
    all = dao.loadAll();
    assertEquals(20, all.size());
    for (Object object : all) {
      if (object instanceof ISearchMark)
        assertProperties((ISearchMark) object);
      else
        fail();
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCachingLabelDAO() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Hello");
    label1.setOrder(5);
    label1 = DynamicDAO.save(label1);
    Long label1Id = label1.getId();

    ILabel label2 = fFactory.createLabel(null, "Hello World");
    label2.setColor("255,255,0");
    label2 = DynamicDAO.save(label2);
    Long label2Id = label2.getId();

    ILabel label3 = fFactory.createLabel(null, "Foo Bar");
    label3.setProperty("key", "value");
    label3 = DynamicDAO.save(label3);
    Long label3Id = label3.getId();

    DynamicDAO.save(label1);
    DynamicDAO.save(label2);
    DynamicDAO.save(label3);

    label1 = null;
    label2 = null;
    label3 = null;

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);
    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), false, false);

    /* Assert Folders */
    CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(ILabelDAO.class);

    label1 = (ILabel) dao.load(label1Id);
    assertEquals("Hello", label1.getName());
    assertEquals(5, label1.getOrder());

    label2 = (ILabel) dao.load(label2Id);
    assertEquals("Hello World", label2.getName());
    assertEquals("255,255,0", label2.getColor());

    label3 = (ILabel) dao.load(label3Id);
    assertEquals("Foo Bar", label3.getName());
    assertEquals("value", label3.getProperty("key"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCachingLabelDAOEmergencyStartup() throws Exception {
    ILabel label1 = fFactory.createLabel(null, "Hello");
    label1.setOrder(5);
    label1 = DynamicDAO.save(label1);
    Long label1Id = label1.getId();

    ILabel label2 = fFactory.createLabel(null, "Hello World");
    label2.setColor("255,255,0");
    label2 = DynamicDAO.save(label2);
    Long label2Id = label2.getId();

    ILabel label3 = fFactory.createLabel(null, "Foo Bar");
    label3.setProperty("key", "value");
    label3 = DynamicDAO.save(label3);
    Long label3Id = label3.getId();

    DynamicDAO.save(label1);
    DynamicDAO.save(label2);
    DynamicDAO.save(label3);

    label1 = null;
    label2 = null;
    label3 = null;

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);
    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), true, false);

    /* Assert Folders */
    CachingDAO dao = (CachingDAO) DynamicDAO.getDAO(ILabelDAO.class);

    label1 = (ILabel) dao.load(label1Id);
    assertEquals("Hello", label1.getName());
    assertEquals(5, label1.getOrder());

    label2 = (ILabel) dao.load(label2Id);
    assertEquals("Hello World", label2.getName());
    assertEquals("255,255,0", label2.getColor());

    label3 = (ILabel) dao.load(label3Id);
    assertEquals("Foo Bar", label3.getName());
    assertEquals("value", label3.getProperty("key"));

    /* Reopen Database */
    Owl.getPersistenceService().shutdown(false);
    System.gc();
    Owl.getPersistenceService().startup(new NullOperationMonitor(), false, false);

    label1 = (ILabel) dao.load(label1Id);
    assertEquals("Hello", label1.getName());
    assertEquals(5, label1.getOrder());

    label2 = (ILabel) dao.load(label2Id);
    assertEquals("Hello World", label2.getName());
    assertEquals("255,255,0", label2.getColor());

    label3 = (ILabel) dao.load(label3Id);
    assertEquals("Foo Bar", label3.getName());
    assertEquals("value", label3.getProperty("key"));
  }
}