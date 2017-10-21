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

package org.rssowl.core.tests.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.AttachmentAdapter;
import org.rssowl.core.persist.event.AttachmentEvent;
import org.rssowl.core.persist.event.AttachmentListener;
import org.rssowl.core.persist.event.BookMarkAdapter;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.BookMarkListener;
import org.rssowl.core.persist.event.CategoryAdapter;
import org.rssowl.core.persist.event.CategoryEvent;
import org.rssowl.core.persist.event.CategoryListener;
import org.rssowl.core.persist.event.FeedAdapter;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FeedListener;
import org.rssowl.core.persist.event.FolderAdapter;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.FolderListener;
import org.rssowl.core.persist.event.NewsAdapter;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.NewsListener;
import org.rssowl.core.persist.event.PersonAdapter;
import org.rssowl.core.persist.event.PersonEvent;
import org.rssowl.core.persist.event.PersonListener;
import org.rssowl.core.persist.event.SearchConditionAdapter;
import org.rssowl.core.persist.event.SearchConditionEvent;
import org.rssowl.core.persist.event.SearchConditionListener;
import org.rssowl.core.persist.event.SearchMarkAdapter;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.event.SearchMarkListener;
import org.rssowl.core.persist.reference.AttachmentReference;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.CategoryReference;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.reference.PersonReference;
import org.rssowl.core.persist.reference.SearchConditionReference;
import org.rssowl.core.persist.reference.SearchMarkReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.tests.TestUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;

/**
 * This TestCase is for testing the Model Plugin (2 of 4).
 *
 * @author bpasero
 */
public class ModelTest2 extends LargeBlockSizeTest {
  private IModelFactory fFactory;

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
    fFactory = Owl.getModelFactory();
  }

  /**
   * Test getting Events for a Folder being added with sub-folders.
   *
   * @throws Exception
   */
  @Test
  public void testDeepFolderAddedEvents() throws Exception {
    FolderListener folderListener = null;
    BookMarkListener bookMarkListener = null;
    FeedListener feedListener = null;
    SearchMarkListener searchMarkListener = null;
    SearchConditionListener searchConditionListener = null;
    try {

      /* Check Folder Added */
      IFolder root = fFactory.createFolder(null, null, "Root");
      IFolder rootChild = fFactory.createFolder(null, root, "Root Child");
      IFolder rootChildChild1 = fFactory.createFolder(null, rootChild, "Root Child Child #1");
      IFolder rootChildChild2 = fFactory.createFolder(null, rootChild, "Root Child Child #2");
      IFolder rootChildChild1Child = fFactory.createFolder(null, rootChildChild1, "Root Child Child #1 Child");

      final boolean[] folderEventsIssued = new boolean[5];
      final Folder[] folders = new Folder[] { (Folder) root, (Folder) rootChild, (Folder) rootChildChild1, (Folder) rootChildChild2, (Folder) rootChildChild1Child };

      folderListener = new FolderAdapter() {
        @Override
        public void entitiesAdded(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            IFolder folder = event.getEntity();

            if ("Root".equals(folder.getName()))
              assertTrue("Expected this Event to be Root Event", event.isRoot());
            else
              assertFalse("Expected this Event to be no Root Event", event.isRoot());

            if ("Root".equals(folder.getName()))
              assertNull(folder.getParent());
            else if ("Root Child".equals(folder.getName()))
              assertEquals("Root", folder.getParent().getName());
            else if ("Root Child Child #1".equals(folder.getName()))
              assertEquals("Root Child", folder.getParent().getName());
            else if ("Root Child Child #2".equals(folder.getName()))
              assertEquals("Root Child", folder.getParent().getName());
            else if ("Root Child Child #1 Child".equals(folder.getName()))
              assertEquals("Root Child Child #1", folder.getParent().getName());

            for (int i = 0; i < folders.length; i++) {
              if (folders[i].getName().equals(folder.getName()))
                folderEventsIssued[i] = true;
            }
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);

      /* Check BookMark Added */
      final IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));

      final boolean feedEventsIssued[] = new boolean[1];
      feedListener = new FeedAdapter() {
        @Override
        public void entitiesAdded(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertFalse("Already received feed added event!", feedEventsIssued[0]);
            assertTrue("Expected this Event to be Root Event", event.isRoot());

            if (event.getEntity().getLink().toString().equals(feed.getLink().toString()))
              feedEventsIssued[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IFeed.class, feedListener);

      /* Save Feed since a IBookMark now doesn't contain a feed */
      DynamicDAO.save(feed);

      final IBookMark bookMark1 = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Root BookMark");
      final IBookMark bookMark2 = fFactory.createBookMark(null, rootChild, new FeedLinkReference(feed.getLink()), "Root Child BookMark");
      final IBookMark bookMark3 = fFactory.createBookMark(null, rootChildChild1Child, new FeedLinkReference(feed.getLink()), "Root Child Child #1 BookMark");

      final boolean bookMarkEventsIssued[] = new boolean[3];

      bookMarkListener = new BookMarkAdapter() {
        @Override
        public void entitiesAdded(Set<BookMarkEvent> events) {
          for (BookMarkEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IBookMark bookMark = event.getEntity();

            if ("Root BookMark".equals(bookMark.getName()))
              assertEquals("Root", bookMark.getParent().getName());
            else if ("Root Child BookMark".equals(bookMark.getName()))
              assertEquals("Root Child", bookMark.getParent().getName());
            else if ("Root Child Child #1 BookMark".equals(bookMark.getName()))
              assertEquals("Root Child Child #1 Child", bookMark.getParent().getName());

            if (bookMark.getName().equals(bookMark1.getName()))
              bookMarkEventsIssued[0] = true;

            else if (bookMark.getName().equals(bookMark2.getName()))
              bookMarkEventsIssued[1] = true;

            else if (bookMark.getName().equals(bookMark3.getName()))
              bookMarkEventsIssued[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IBookMark.class, bookMarkListener);

      /* Check SearchMark Added */
      final ISearchMark searchMark1 = fFactory.createSearchMark(null, root, "Root SearchMark");
      final ISearchMark searchMark2 = fFactory.createSearchMark(null, rootChild, "Root Child SearchMark");
      final ISearchMark searchMark3 = fFactory.createSearchMark(null, rootChildChild1Child, "Root Child Child #1 SearchMark");

      final boolean searchMarkEventsIssued[] = new boolean[3];

      searchMarkListener = new SearchMarkAdapter() {
        @Override
        public void entitiesAdded(Set<SearchMarkEvent> events) {
          for (SearchMarkEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            ISearchMark searchMark = event.getEntity();

            if ("Root SearchMark".equals(searchMark.getName()))
              assertEquals("Root", searchMark.getParent().getName());
            else if ("Root Child SearchMark".equals(searchMark.getName()))
              assertEquals("Root Child", searchMark.getParent().getName());
            else if ("Root Child Child #1 SearchMark".equals(searchMark.getName()))
              assertEquals("Root Child Child #1 Child", searchMark.getParent().getName());

            if (searchMark.getName().equals(searchMark1.getName()))
              searchMarkEventsIssued[0] = true;

            else if (searchMark.getName().equals(searchMark2.getName()))
              searchMarkEventsIssued[1] = true;

            else if (searchMark.getName().equals(searchMark3.getName()))
              searchMarkEventsIssued[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ISearchMark.class, searchMarkListener);

      /* Check SearchCondition Added */
      ISearchField field1 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());
      final ISearchCondition condition1 = fFactory.createSearchCondition(null, searchMark1, field1, SearchSpecifier.CONTAINS, "Foo");
      final ISearchCondition condition2 = fFactory.createSearchCondition(null, searchMark1, field1, SearchSpecifier.IS, "Bar");
      final ISearchCondition condition3 = fFactory.createSearchCondition(null, searchMark2, field1, SearchSpecifier.IS_NOT, "Foo Bar");

      final boolean searchConditionEventsIssued[] = new boolean[3];

      searchConditionListener = new SearchConditionAdapter() {
        @Override
        public void entitiesAdded(Set<SearchConditionEvent> events) {
          for (SearchConditionEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            ISearchCondition searchCondition = event.getEntity();

            if (searchCondition.getValue().equals(condition1.getValue()))
              searchConditionEventsIssued[0] = true;

            else if (searchCondition.getValue().equals(condition2.getValue()))
              searchConditionEventsIssued[1] = true;

            else if (searchCondition.getValue().equals(condition3.getValue()))
              searchConditionEventsIssued[2] = true;

          }
        }
      };
      DynamicDAO.addEntityListener(ISearchCondition.class, searchConditionListener);

      /* Save Folder */
      DynamicDAO.save(root);

      /* Asserts Follow */
      assertTrue("Expected one feedAdded event", feedEventsIssued[0]);

      for (int i = 0; i < folderEventsIssued.length; i++) {
        if (!folderEventsIssued[i])
          fail("Expected five folderAdded events!");
      }

      for (int i = 0; i < bookMarkEventsIssued.length; i++) {
        if (!bookMarkEventsIssued[i])
          fail("Expected three bookMarkAdded events!");
      }

      for (int i = 0; i < searchMarkEventsIssued.length; i++) {
        if (!searchMarkEventsIssued[i])
          fail("Expected three searchMarkAdded events!");
      }

      for (int i = 0; i < searchConditionEventsIssued.length; i++) {
        if (!searchConditionEventsIssued[i])
          fail("Expected three searchConditionAdded events!");
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    } finally {
      if (folderListener != null)
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      if (bookMarkListener != null)
        DynamicDAO.removeEntityListener(IBookMark.class, bookMarkListener);
      if (searchMarkListener != null)
        DynamicDAO.removeEntityListener(ISearchMark.class, searchMarkListener);
      if (feedListener != null)
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
      if (searchConditionListener != null)
        DynamicDAO.removeEntityListener(ISearchCondition.class, searchConditionListener);
    }
  }

  /**
   * Test getting Events for a Feed being added with all possible child-types
   * added.
   * <p>
   * <ul>
   * <li>Image Event not sent out</li>
   * <li>Saving News gives NullPE because getLink is not expected to be NULL</li>
   * <li>Resolving Person gives NULL</li>
   * </ul>
   * </p>
   *
   * @throws Exception
   */
  @Test
  public void testDeepFeedAddedEvents() throws Exception {
    FeedListener feedListener = null;
    NewsListener newsListener = null;
    AttachmentListener attachmentListener = null;
    PersonListener personListener = null;
    CategoryListener categoryListener = null;
    try {

      /* Check Feed Added and News received */
      final IFeed feed = fFactory.createFeed(null, new URI("http://www.foobar.com"));
      final boolean feedAdded[] = new boolean[1];
      final boolean newsReceivedFromFeed[] = new boolean[1];
      feedListener = new FeedAdapter() {
        @Override
        public void entitiesAdded(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertFalse("Already received feedAdded Event", feedAdded[0]);
            assertTrue("Expected this Event to be Root Event", event.isRoot());

            if (event.getEntity().getLink().toString().equals(feed.getLink().toString()))
              feedAdded[0] = true;
          }
        }
      };

      DynamicDAO.addEntityListener(IFeed.class, feedListener);

      /* Check News Added */
      final INews news1 = fFactory.createNews(null, feed, new Date());
      news1.setTitle("News1 Title");
      news1.setLink(new URI("http://www.news.com/news1.html"));
      final INews news2 = fFactory.createNews(null, feed, new Date());
      news2.setTitle("News2 Title");
      news2.setLink(new URI("http://www.news.com/news2.html"));
      final INews news3 = fFactory.createNews(null, feed, new Date());
      news3.setTitle("News3 Title");
      news3.setLink(new URI("http://www.news.com/news3.html"));
      final boolean newsReceived[] = new boolean[3];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            INews news = event.getEntity();

            if (news.getTitle().equals(news1.getTitle()))
              newsReceived[0] = true;

            else if (news.getTitle().equals(news2.getTitle()))
              newsReceived[1] = true;

            else if (news.getTitle().equals(news3.getTitle()))
              newsReceived[2] = true;

            try {
              assertEquals(new URI("http://www.foobar.com").toString(), news.getFeedReference().getLink().toString());
            } catch (URISyntaxException e) {
              fail(e.getMessage());
            }
          }
          if (events.size() == 3)
            newsReceivedFromFeed[0] = true;
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* Check Attachment Added */
      final IAttachment attachment1 = fFactory.createAttachment(null, news1);
      attachment1.setLink(new URI("http://www.attachment1.com"));
      final IAttachment attachment2 = fFactory.createAttachment(null, news2);
      attachment2.setLink(new URI("http://www.attachment2.com"));
      final IAttachment attachment3 = fFactory.createAttachment(null, news3);
      attachment3.setLink(new URI("http://www.attachment3.com"));
      final boolean attachmentAdded[] = new boolean[3];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesAdded(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IAttachment attachment = event.getEntity();

            if (attachment.getLink().equals(attachment1.getLink()))
              attachmentAdded[0] = true;

            else if (attachment.getLink().equals(attachment2.getLink()))
              attachmentAdded[1] = true;

            else if (attachment.getLink().equals(attachment3.getLink()))
              attachmentAdded[2] = true;

          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      /* Check Person Added */
      final IPerson person1 = fFactory.createPerson(null, feed);
      person1.setName("Person1");
      final IPerson person2 = fFactory.createPerson(null, news1);
      person2.setName("Person2");
      final boolean personAdded[] = new boolean[2];
      personListener = new PersonAdapter() {
        @Override
        public void entitiesAdded(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IPerson person = event.getEntity();

            if (person.getName().equals(person1.getName()))
              personAdded[0] = true;

            else if (person.getName().equals(person2.getName()))
              personAdded[1] = true;

          }
        }
      };
      DynamicDAO.addEntityListener(IPerson.class, personListener);

      /* Check Category Added */
      final ICategory category1 = fFactory.createCategory(null, news1);
      category1.setName("Category1");
      final ICategory category2 = fFactory.createCategory(null, news2);
      category2.setName("Category2");
      final boolean categoryAdded[] = new boolean[2];
      categoryListener = new CategoryAdapter() {
        @Override
        public void entitiesAdded(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            ICategory category = event.getEntity();

            if (category.getName().equals(category1.getName()))
              categoryAdded[0] = true;

            else if (category.getName().equals(category2.getName()))
              categoryAdded[1] = true;

          }
        }
      };
      DynamicDAO.addEntityListener(ICategory.class, categoryListener);

      /* Save Feed */
      DynamicDAO.save(feed);

      /* Asserts Follow */
      assertTrue("Missed feedAdded Event in FeedListener!", feedAdded[0]);
      assertTrue("Missed newsReceived Event in FeedListener!", newsReceivedFromFeed[0]);

      for (int i = 0; i < newsReceived.length; i++) {
        if (!newsReceived[i])
          fail("Missed newsReceived Event in NewsListener!");
      }

      for (int i = 0; i < attachmentAdded.length; i++) {
        if (!attachmentAdded[i])
          fail("Missed attachmentAdded Event in AttachmentListener!");
      }

      for (int i = 0; i < personAdded.length; i++) {
        if (!personAdded[i])
          fail("Missed personAdded Event in PersonListener!");
      }

      for (int i = 0; i < categoryAdded.length; i++) {
        if (!categoryAdded[i])
          fail("Missed categoryAdded Event in CategoryListener!");
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    } finally {
      if (feedListener != null)
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
      if (personListener != null)
        DynamicDAO.removeEntityListener(IPerson.class, personListener);
      if (categoryListener != null)
        DynamicDAO.removeEntityListener(ICategory.class, categoryListener);
    }
  }

  /**
   * Test getting Events for a News being added with Attachments and other
   * types.
   *
   * @throws Exception
   */
  @Test
  public void testDeepNewsAddedEvents() throws Exception {
    NewsListener newsListener = null;
    AttachmentListener attachmentListener = null;
    PersonListener personListener = null;
    CategoryListener categoryListener = null;
    try {
      IFeed feed = fFactory.createFeed(null, new URI("http://www.foobar.com"));
      FeedReference feedReference = new FeedReference(DynamicDAO.save(feed).getId());

      /* Check News Added */
      final INews news = fFactory.createNews(null, feedReference.resolve(), new Date());
      news.setTitle("News Title");
      final boolean newsAdded[] = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (event.getEntity().getTitle().equals(news.getTitle()))
              newsAdded[0] = true;

            try {
              assertEquals(new URI("http://www.foobar.com").toString(), event.getEntity().getFeedReference().getLink().toString());
            } catch (URISyntaxException e) {
              fail(e.getMessage());
            }
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* Check Author Added */
      final IPerson person = fFactory.createPerson(null, news);
      person.setName("Person Name");
      final boolean personAdded[] = new boolean[1];
      personListener = new PersonAdapter() {
        @Override
        public void entitiesAdded(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());
            if (event.getEntity().getName().equals(person.getName()))
              personAdded[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IPerson.class, personListener);

      /* Check Attachments Added */
      final IAttachment attachment1 = fFactory.createAttachment(null, news);
      attachment1.setLink(new URI("http://www.attachment1.com"));
      final IAttachment attachment2 = fFactory.createAttachment(null, news);
      attachment2.setLink(new URI("http://www.attachment2.com"));
      final IAttachment attachment3 = fFactory.createAttachment(null, news);
      attachment3.setLink(new URI("http://www.attachment3.com"));
      final boolean attachmentAdded[] = new boolean[3];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesAdded(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IAttachment attachment = event.getEntity();

            if (attachment.getLink().equals(attachment1.getLink()))
              attachmentAdded[0] = true;
            else if (attachment.getLink().equals(attachment2.getLink()))
              attachmentAdded[1] = true;
            else if (attachment.getLink().equals(attachment3.getLink()))
              attachmentAdded[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      /* Check Category Added */
      final ICategory category = fFactory.createCategory(null, news);
      category.setName("Category 1");
      final boolean categoryAdded[] = new boolean[1];
      categoryListener = new CategoryAdapter() {
        @Override
        public void entitiesAdded(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());
            if (event.getEntity().getName().equals(category.getName()))
              categoryAdded[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ICategory.class, categoryListener);

      DynamicDAO.save(news);

      /* Asserts Follow */
      assertTrue("Missed newsReceived Event in NewsListener!", newsAdded[0]);
      assertTrue("Missed personAdded Event in PersonListener!", personAdded[0]);
      assertTrue("Missed categoryAdded Event in CategoryListener!", categoryAdded[0]);

      for (int i = 0; i < attachmentAdded.length; i++) {
        if (!attachmentAdded[i])
          fail("Missed attachmentAdded Event in AttachmentListener!");
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
      if (personListener != null)
        DynamicDAO.removeEntityListener(IPerson.class, personListener);
      if (categoryListener != null)
        DynamicDAO.removeEntityListener(ICategory.class, categoryListener);
    }
  }

  /**
   * This test checks that deleting a feed, also deletes the news an attachments
   * associated with it.
   *
   * @throws Exception
   */
  @Test
  public void testDeleteFeedNewsAndAttachment() throws Exception {
    NewsListener newsListener = null;
    AttachmentListener attachmentListener = null;
    try {
      IFeed feed = fFactory.createFeed(null, new URI("http://www.foobar.com"));
      FeedReference feedReference = new FeedReference(DynamicDAO.save(feed).getId());

      /* Check News Added */
      final INews news = fFactory.createNews(null, feedReference.resolve(), new Date());
      news.setTitle("News Title");
      final IAttachment attachment0 = fFactory.createAttachment(null, news);
      attachment0.setLink(new URI("http://www.attachment1.com"));
      final IAttachment attachment1 = fFactory.createAttachment(null, news);
      attachment1.setLink(new URI("http://www.attachment1.com"));
      DynamicDAO.save(feed);
      NewsReference newsRef = new NewsReference(feed.getNews().get(0).getId());
      AttachmentReference attachmentRef0 = new AttachmentReference(news.getAttachments().get(0).getId());
      AttachmentReference attachmentRef1 = new AttachmentReference(news.getAttachments().get(1).getId());

      final boolean[] newsDeleted = new boolean[1];
      newsListener = new NewsListener() {
        @Override
        public void entitiesDeleted(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());
            if (event.getEntity().getTitle().equals(news.getTitle()))
              newsDeleted[0] = true;

            try {
              assertEquals(new URI("http://www.foobar.com").toString(), event.getEntity().getFeedReference().getLink().toString());
            } catch (URISyntaxException e) {
              fail(e.getMessage());
            }
          }
        }

        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          fail("Unexpected event");
        }

        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          fail("Unexpected event");
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* Check Attachments Added */
      final boolean attachmentDeleted[] = new boolean[2];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesDeleted(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IAttachment attachment = event.getEntity();

            if (attachment.getLink().equals(attachment0.getLink()))
              attachmentDeleted[0] = true;
            if (attachment.getLink().equals(attachment1.getLink()))
              attachmentDeleted[1] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      DynamicDAO.delete(feed);
      assertNull(DynamicDAO.load(IFeed.class, feed.getId()));
      assertNull(DynamicDAO.load(INews.class, newsRef.getId()));
      assertNull(DynamicDAO.load(IAttachment.class, attachmentRef0.getId()));
      assertNull(DynamicDAO.load(IAttachment.class, attachmentRef1.getId()));

      assertTrue("Missed newsDeleted Event in NewsListener!", newsDeleted[0]);
      assertTrue("Missed attachmentDeleted Event in PersonListener!", attachmentDeleted[0]);
      assertTrue("Missed attachmentDeleted Event in PersonListener!", attachmentDeleted[1]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testDeleteAttachmentFiresNewsUpdatedEvent() throws Exception {
    NewsListener newsListener = null;
    AttachmentListener attachmentListener = null;
    try {
      final URI feedURI = new URI("http://www.foobar.com");
      IFeed feed = fFactory.createFeed(null, feedURI);
      FeedReference feedReference = new FeedReference(DynamicDAO.save(feed).getId());

      /* Check News Added */
      final INews news = fFactory.createNews(null, feedReference.resolve(), new Date());
      news.setTitle("News Title");
      final IAttachment attachment1 = fFactory.createAttachment(null, news);
      attachment1.setLink(new URI("http://www.attachment1.com"));
      DynamicDAO.save(news);

      final boolean[] newsUpdated = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesUpdated(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());
            if (event.getEntity().getTitle().equals(news.getTitle()))
              newsUpdated[0] = true;

            assertEquals(feedURI.toString(), event.getEntity().getFeedReference().getLink().toString());
            assertNotNull(event.getOldNews());
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* Check Attachments Added */
      final boolean attachmentDeleted[] = new boolean[1];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesDeleted(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());

            IAttachment attachment = event.getEntity();

            if (attachment.getLink().equals(attachment1.getLink()))
              attachmentDeleted[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      IAttachment attachment = news.getAttachments().get(0);
      DynamicDAO.delete(attachment);

      assertNull(DynamicDAO.load(IAttachment.class, attachment.getId()));
      /* Asserts Follow */
      assertTrue("Missed newsUpdated Event in NewsListener!", newsUpdated[0]);
      assertTrue("Missed attachmentDeleted Event in PersonListener!", attachmentDeleted[0]);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
    }
  }

  /**
   * Test getting Events for a Folder being deleted with sub-folders and some
   * BookMarks and SearchMarks as Childs.
   *
   * @throws Exception
   */
  @Test
  public void testDeepFolderDeletedEvents() throws Exception {
    FolderListener folderListener = null;
    BookMarkListener bookMarkListener = null;
    FeedListener feedListener = null;
    SearchMarkListener searchMarkListener = null;
    SearchConditionListener searchConditionListener = null;
    try {
      /* Check Folder Deleted */
      IFolder root = fFactory.createFolder(null, null, "Root");
      root = DynamicDAO.save(root);
      final FolderReference rootRef = new FolderReference(root.getId());
      fFactory.createFolder(null, root, "Root Child");
      root = DynamicDAO.save(root);
      IFolder rootChild = root.getFolders().get(0);
      fFactory.createFolder(null, rootChild, "Root Child Child #1");
      rootChild = DynamicDAO.save(rootChild);
      FolderReference rootChildRef = new FolderReference(rootChild.getId());
      IFolder rootChildChild1 = rootChild.getFolders().get(0);
      FolderReference rootChildChild1Ref = new FolderReference(rootChildChild1.getId());
      fFactory.createFolder(null, rootChild, "Root Child Child #2");
      rootChild = DynamicDAO.save(rootChild);
      IFolder rootChildChild2 = rootChild.getFolders().get(1);
      FolderReference rootChildChild2Ref = new FolderReference(rootChildChild2.getId());
      fFactory.createFolder(null, rootChildChild1, "Root Child Child #1 Child");
      rootChildChild1 = DynamicDAO.save(rootChildChild1);
      IFolder rootChildChild1Child = rootChildChild1.getFolders().get(0);
      FolderReference rootChildChild1ChildRef = new FolderReference(rootChildChild1Child.getId());

      final boolean[] folderEventsIssued = new boolean[5];
      final FolderReference[] folderReferences = new FolderReference[] { rootRef, rootChildRef, rootChildChild1Ref, rootChildChild2Ref, rootChildChild1ChildRef };

      folderListener = new FolderAdapter() {
        @Override
        public void entitiesDeleted(Set<FolderEvent> events) {
          for (FolderEvent event : events) {
            IFolder folder = event.getEntity();

            if (rootRef.references(folder))
              assertEquals(true, event.isRoot());

            for (int i = 0; i < folderReferences.length; i++)
              if (folderReferences[i].references(folder))
                folderEventsIssued[i] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IFolder.class, folderListener);

      /* Check BookMark Deleted */
      final IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));
      final FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      IBookMark bookMark1 = fFactory.createBookMark(null, rootRef.resolve(), new FeedLinkReference(feed.getLink()), "Root BookMark");
      root = DynamicDAO.save(bookMark1.getParent());
      final BookMarkReference bookMarkRef1 = new BookMarkReference(root.getMarks().get(0).getId());

      IBookMark bookMark2 = fFactory.createBookMark(null, rootChildRef.resolve(), new FeedLinkReference(feed.getLink()), "Root Child BookMark");
      rootChild = DynamicDAO.save(bookMark2.getParent());
      final BookMarkReference bookMarkRef2 = new BookMarkReference(rootChild.getMarks().get(0).getId());

      IBookMark bookMark3 = fFactory.createBookMark(null, rootChildChild1Ref.resolve(), new FeedLinkReference(feed.getLink()), "Root Child Child #1 BookMark");
      rootChildChild1 = DynamicDAO.save(bookMark3.getParent());
      final BookMarkReference bookMarkRef3 = new BookMarkReference(rootChildChild1.getMarks().get(0).getId());

      final boolean bookMarkEventsIssued[] = new boolean[3];
      final boolean feedEventsIssued[] = new boolean[1];

      bookMarkListener = new BookMarkAdapter() {
        @Override
        public void entitiesDeleted(Set<BookMarkEvent> events) {
          for (BookMarkEvent event : events) {
            IBookMark bookMark = event.getEntity();

            if (bookMarkRef1.references(bookMark))
              bookMarkEventsIssued[0] = true;

            else if (bookMarkRef2.references(bookMark))
              bookMarkEventsIssued[1] = true;

            else if (bookMarkRef3.references(bookMark))
              bookMarkEventsIssued[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IBookMark.class, bookMarkListener);

      feedListener = new FeedAdapter() {
        @Override
        public void entitiesDeleted(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertFalse("Already received feed deleted event!", feedEventsIssued[0]);
            if (feedRef.references(event.getEntity()))
              feedEventsIssued[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IFeed.class, feedListener);

      /* Check SearchMark Deleted */
      ISearchMark searchMark1 = fFactory.createSearchMark(null, rootRef.resolve(), "Root SearchMark");
      root = DynamicDAO.save(searchMark1.getParent());
      searchMark1 = (ISearchMark) root.getMarks().get(root.getMarks().size() - 1);
      final SearchMarkReference searchMarkRef1 = new SearchMarkReference(searchMark1.getId());

      ISearchMark searchMark2 = fFactory.createSearchMark(null, rootChildRef.resolve(), "Root Child SearchMark");
      rootChild = DynamicDAO.save(searchMark2.getParent());
      searchMark2 = (ISearchMark) rootChild.getMarks().get(rootChild.getMarks().size() - 1);
      final SearchMarkReference searchMarkRef2 = new SearchMarkReference(searchMark2.getId());

      ISearchMark searchMark3 = fFactory.createSearchMark(null, rootChildChild1ChildRef.resolve(), "Root Child Child #1 Child SearchMark");
      rootChildChild1Child = DynamicDAO.save(searchMark3.getParent());
      searchMark3 = (ISearchMark) rootChildChild1Child.getMarks().get(rootChildChild1Child.getMarks().size() - 1);
      final SearchMarkReference searchMarkRef3 = new SearchMarkReference(searchMark3.getId());

      final boolean searchMarkEventsIssued[] = new boolean[3];

      searchMarkListener = new SearchMarkAdapter() {
        @Override
        public void entitiesDeleted(Set<SearchMarkEvent> events) {
          for (SearchMarkEvent event : events) {
            ISearchMark searchMark = event.getEntity();

            if (searchMarkRef1.references(searchMark))
              searchMarkEventsIssued[0] = true;

            else if (searchMarkRef2.references(searchMark))
              searchMarkEventsIssued[1] = true;

            else if (searchMarkRef3.references(searchMark))
              searchMarkEventsIssued[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ISearchMark.class, searchMarkListener);

      /* Check SearchCondition Deleted */
      ISearchField field1 = fFactory.createSearchField(IEntity.ALL_FIELDS, INews.class.getName());

      fFactory.createSearchCondition(null, searchMark1, field1, SearchSpecifier.CONTAINS, "Foo");
      searchMark1 = DynamicDAO.save(searchMark1);
      final SearchConditionReference conditionRef1 = new SearchConditionReference(searchMark1.getSearchConditions().get(0).getId());

      fFactory.createSearchCondition(null, searchMark2, field1, SearchSpecifier.IS, "Bar");
      searchMark2 = DynamicDAO.save(searchMark2);
      final SearchConditionReference conditionRef2 = new SearchConditionReference(searchMark2.getSearchConditions().get(0).getId());

      fFactory.createSearchCondition(null, searchMark3, field1, SearchSpecifier.IS_NOT, "Foo Bar");
      searchMark3 = DynamicDAO.save(searchMark3);
      final SearchConditionReference conditionRef3 = new SearchConditionReference(searchMark3.getSearchConditions().get(0).getId());

      final boolean searchConditionEventsIssued[] = new boolean[3];

      searchConditionListener = new SearchConditionAdapter() {
        @Override
        public void entitiesDeleted(Set<SearchConditionEvent> events) {
          for (SearchConditionEvent event : events) {
            ISearchCondition searchCondition = event.getEntity();

            if (conditionRef1.references(searchCondition))
              searchConditionEventsIssued[0] = true;

            else if (conditionRef2.references(searchCondition))
              searchConditionEventsIssued[1] = true;

            else if (conditionRef3.references(searchCondition))
              searchConditionEventsIssued[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ISearchCondition.class, searchConditionListener);

      /* Delete Root Folder */
      DynamicDAO.delete(rootRef.resolve());

      /* Asserts Follow */
      assertEquals(0, Owl.getPersistenceService().getDAOService().getFolderDAO().loadRoots().size());
      assertEquals(0, DynamicDAO.loadAll(IBookMark.class).size());
      assertTrue("Expected one feedDeleted event", feedEventsIssued[0]);
      for (int i = 0; i < folderEventsIssued.length; i++) {
        if (!folderEventsIssued[i])
          fail("Expected five folderDeleted events!");
      }

      for (int i = 0; i < bookMarkEventsIssued.length; i++) {
        if (!bookMarkEventsIssued[i])
          fail("Expected three bookMarkDeleted events!");
      }

      for (int i = 0; i < searchMarkEventsIssued.length; i++) {
        if (!searchMarkEventsIssued[i])
          fail("Expected three searchMarkDeleted events!");
      }

      for (int i = 0; i < searchConditionEventsIssued.length; i++) {
        if (!searchConditionEventsIssued[i])
          fail("Expected three searchConditionDeleted events!");
      }
    } finally {
      if (folderListener != null)
        DynamicDAO.removeEntityListener(IFolder.class, folderListener);
      if (bookMarkListener != null)
        DynamicDAO.removeEntityListener(IBookMark.class, bookMarkListener);
      if (searchMarkListener != null)
        DynamicDAO.removeEntityListener(ISearchMark.class, searchMarkListener);
      if (feedListener != null)
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
      if (searchConditionListener != null)
        DynamicDAO.removeEntityListener(ISearchCondition.class, searchConditionListener);
    }
  }

  /**
   * Test getting Events for a Feed being deleted with News and Attachments and
   * other types.
   *
   * @throws Exception
   */
  @Test
  public void testDeepFeedDeletedEvents() throws Exception {
    FeedListener feedListener = null;
    NewsListener newsListener = null;
    AttachmentListener attachmentListener = null;
    PersonListener personListener = null;
    CategoryListener categoryListener = null;
    NewsAdapter newsAdapter = null;
    try {

      /* Check Feed Deleted and News Deleted */
      final IFeed feed = fFactory.createFeed(null, new URI("http://www.foobar.com"));
      final IPerson person1 = fFactory.createPerson(null, feed);
      person1.setName("Person1");
      final FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());
      final PersonReference personRef1 = new PersonReference(feedRef.resolve().getAuthor().getId());
      final boolean feedDeleted[] = new boolean[1];
      final boolean newsDeletedFromFeed[] = new boolean[1];
      feedListener = new FeedAdapter() {
        @Override
        public void entitiesDeleted(Set<FeedEvent> events) {
          for (FeedEvent event : events) {
            assertFalse("Already received feedDeleted Event", feedDeleted[0]);
            assertTrue("Expected this Event to be Root Event", event.isRoot());

            if (feedRef.references(event.getEntity()))
              feedDeleted[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IFeed.class, feedListener);

      /* Check News Deleted */
      final INews news1 = fFactory.createNews(null, feedRef.resolve(), new Date());
      news1.setTitle("News1 Title");
      news1.setLink(new URI("http://www.news.com/news1.html"));
      final IPerson person2 = fFactory.createPerson(null, news1);
      person2.setName("Person2");
      fFactory.createSource(news1).setLink(new URI("http://www.source1.com"));
      fFactory.createGuid(news1, "Guid1", null);

      final NewsReference[] newsRef = new NewsReference[1];
      newsAdapter = new NewsAdapter() {
        @Override
        public void entitiesAdded(Set<NewsEvent> events) {
          assertEquals(1, events.size());
          newsRef[0] = new NewsReference(events.iterator().next().getEntity().getId());
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsAdapter);
      /* Must save parent because it gets changed during creation of news */
      DynamicDAO.save(feedRef.resolve());
      final NewsReference newsRef1 = newsRef[0];
      final PersonReference personRef2 = new PersonReference(newsRef1.resolve().getAuthor().getId());

      final INews news2 = fFactory.createNews(null, feedRef.resolve(), new Date());
      news2.setTitle("News2 Title");
      news2.setLink(new URI("http://www.news.com/news2.html"));
      fFactory.createSource(news2).setLink(new URI("http://www.source2.com"));
      fFactory.createGuid(news2, "Guid2", null);
      /* Must save parent because it gets changed during creation of news */
      DynamicDAO.save(feedRef.resolve());
      final NewsReference newsRef2 = newsRef[0];

      final INews news3 = fFactory.createNews(null, feedRef.resolve(), new Date());
      news3.setTitle("News3 Title");
      news3.setLink(new URI("http://www.news.com/news3.html"));
      fFactory.createSource(news3).setLink(new URI("http://www.source3.com"));
      fFactory.createGuid(news3, "Guid3", null);
      /* Must save parent because it gets changed during creation of news */
      DynamicDAO.save(feedRef.resolve());
      final NewsReference newsRef3 = newsRef[0];

      final boolean newsDeleted[] = new boolean[3];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesDeleted(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            NewsReference newsRef = new NewsReference(event.getEntity().getId());

            if (newsRef.equals(newsRef1))
              newsDeleted[0] = true;

            else if (newsRef.equals(newsRef2))
              newsDeleted[1] = true;

            else if (newsRef.equals(newsRef3))
              newsDeleted[2] = true;
          }

          if (events.size() == 3)
            newsDeletedFromFeed[0] = true;
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* Check Attachment Deleted */
      final IAttachment attachment1 = fFactory.createAttachment(null, newsRef1.resolve());
      final AttachmentReference attachmentRef1 = new AttachmentReference(DynamicDAO.save(attachment1).getId());
      final IAttachment attachment2 = fFactory.createAttachment(null, newsRef2.resolve());
      final AttachmentReference attachmentRef2 = new AttachmentReference(DynamicDAO.save(attachment2).getId());
      final IAttachment attachment3 = fFactory.createAttachment(null, newsRef3.resolve());
      final AttachmentReference attachmentRef3 = new AttachmentReference(DynamicDAO.save(attachment3).getId());

      final boolean attachmentDeleted[] = new boolean[3];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesDeleted(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IAttachment attachment = event.getEntity();

            if (attachmentRef1.references(attachment))
              attachmentDeleted[0] = true;

            else if (attachmentRef2.references(attachment))
              attachmentDeleted[1] = true;

            else if (attachmentRef3.references(attachment))
              attachmentDeleted[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      /* Check Person Deleted */
      final boolean personDeleted[] = new boolean[2];
      personListener = new PersonAdapter() {
        @Override
        public void entitiesDeleted(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IPerson person = event.getEntity();
            if (personRef1.references(person))
              personDeleted[0] = true;

            else if (personRef2.references(person))
              personDeleted[1] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IPerson.class, personListener);

      /* Check Category Deleted */
      final ICategory category1 = fFactory.createCategory(null, newsRef1.resolve());
      final CategoryReference categoryRef1 = new CategoryReference(DynamicDAO.save(category1).getId());
      final ICategory category2 = fFactory.createCategory(null, newsRef2.resolve());
      final CategoryReference categoryRef2 = new CategoryReference(DynamicDAO.save(category2).getId());

      final boolean categoryDeleted[] = new boolean[2];
      categoryListener = new CategoryAdapter() {
        @Override
        public void entitiesDeleted(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            ICategory category = event.getEntity();

            if (categoryRef1.references(category))
              categoryDeleted[0] = true;

            else if (categoryRef2.references(category))
              categoryDeleted[1] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ICategory.class, categoryListener);

      /* Delete Feed */
      DynamicDAO.delete(feedRef.resolve());

      /* Asserts Follow */
      assertTrue("Missed feedDeleted Event in FeedListener!", feedDeleted[0]);
      assertTrue("Set<NewsEvent> in feedDeleted of FeedListener did not contain 3 News-References!", newsDeletedFromFeed[0]);

      for (int i = 0; i < newsDeleted.length; i++) {
        if (!newsDeleted[i])
          fail("Missed newsDeleted Event in NewsListener!");
      }

      for (int i = 0; i < attachmentDeleted.length; i++) {
        if (!attachmentDeleted[i])
          fail("Missed attachmentDeleted Event in AttachmentListener!");
      }

      for (int i = 0; i < personDeleted.length; i++) {
        if (!personDeleted[i])
          fail("Missed personDeleted Event in PersonListener!");
      }

      for (int i = 0; i < categoryDeleted.length; i++) {
        if (!categoryDeleted[i])
          fail("Missed categoryDeleted Event in CategoryListener!");
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    } finally {
      if (newsAdapter != null)
        DynamicDAO.removeEntityListener(INews.class, newsAdapter);
      if (feedListener != null)
        DynamicDAO.removeEntityListener(IFeed.class, feedListener);
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
      if (personListener != null)
        DynamicDAO.removeEntityListener(IPerson.class, personListener);
      if (categoryListener != null)
        DynamicDAO.removeEntityListener(ICategory.class, categoryListener);
    }
  }

  /**
   * Test getting Events for a News being deleted with Attachments and other
   * types.
   *
   * @throws Exception
   */
  @Test
  public void testDeepNewsDeletedEvents() throws Exception {
    NewsListener newsListener = null;
    AttachmentListener attachmentListener = null;
    PersonListener personListener = null;
    CategoryListener categoryListener = null;
    try {

      /* Store a Feed */
      IFeed feed = fFactory.createFeed(null, new URI("http://www.foobar.com"));
      FeedReference feedRef = new FeedReference(DynamicDAO.save(feed).getId());

      /* Create a News */
      final INews news = fFactory.createNews(null, feedRef.resolve(), new Date());
      news.setTitle("News Title");
      final IPerson person = fFactory.createPerson(null, news);
      person.setName("Person Name");
      fFactory.createCategory(null, news);
      fFactory.createSource(news);
      fFactory.createGuid(news, "Guid Value", null);
      fFactory.createAttachment(null, news);
      fFactory.createAttachment(null, news);
      fFactory.createAttachment(null, news);

      /* Save News */
      final NewsReference newsRef = new NewsReference(DynamicDAO.save(news).getId());

      final PersonReference personRef = new PersonReference(DynamicDAO.load(INews.class, newsRef.getId()).getAuthor().getId());
      final AttachmentReference attachmentRef1 = new AttachmentReference(DynamicDAO.load(INews.class, newsRef.getId()).getAttachments().get(0).getId());
      final AttachmentReference attachmentRef2 = new AttachmentReference(DynamicDAO.load(INews.class, newsRef.getId()).getAttachments().get(1).getId());
      final AttachmentReference attachmentRef3 = new AttachmentReference(DynamicDAO.load(INews.class, newsRef.getId()).getAttachments().get(2).getId());
      final CategoryReference categoryRef = new CategoryReference(DynamicDAO.load(INews.class, newsRef.getId()).getCategories().get(0).getId());

      /* Check News Deleted */
      final boolean newsDeleted[] = new boolean[1];
      newsListener = new NewsAdapter() {
        @Override
        public void entitiesDeleted(Set<NewsEvent> events) {
          for (NewsEvent event : events) {
            assertTrue("Expected this Event to be Root Event", event.isRoot());
            if (newsRef.references(event.getEntity()))
              newsDeleted[0] = true;

            try {
              assertEquals(new URI("http://www.foobar.com").toString(), event.getEntity().getFeedReference().getLink().toString());
            } catch (URISyntaxException e) {
              fail(e.getMessage());
            }
          }
        }
      };
      DynamicDAO.addEntityListener(INews.class, newsListener);

      /* Check Author Deleted */
      final boolean personDeleted[] = new boolean[1];
      personListener = new PersonAdapter() {
        @Override
        public void entitiesDeleted(Set<PersonEvent> events) {
          for (PersonEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());
            if (personRef.references(event.getEntity()))
              personDeleted[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IPerson.class, personListener);

      /* Check Attachments Deleted */
      final boolean attachmentDeleted[] = new boolean[3];
      attachmentListener = new AttachmentAdapter() {
        @Override
        public void entitiesDeleted(Set<AttachmentEvent> events) {
          for (AttachmentEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());

            IAttachment attachment = event.getEntity();

            if (attachmentRef1.references(attachment))
              attachmentDeleted[0] = true;
            else if (attachmentRef2.references(attachment))
              attachmentDeleted[1] = true;
            else if (attachmentRef3.references(attachment))
              attachmentDeleted[2] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(IAttachment.class, attachmentListener);

      /* Check Category Deleted */
      final boolean categoryDeleted[] = new boolean[1];
      categoryListener = new CategoryAdapter() {
        @Override
        public void entitiesDeleted(Set<CategoryEvent> events) {
          for (CategoryEvent event : events) {
            assertFalse("Expected this Event to be no Root Event", event.isRoot());
            if (categoryRef.references(event.getEntity()))
              categoryDeleted[0] = true;
          }
        }
      };
      DynamicDAO.addEntityListener(ICategory.class, categoryListener);

      /* Delete News */
      DynamicDAO.delete(newsRef.resolve());

      /* Asserts Follow */
      assertTrue("Missed newsDeleted Event in NewsListener!", newsDeleted[0]);
      assertTrue("Missed personDeleted Event in PersonListener!", personDeleted[0]);
      assertTrue("Missed categoryDeleted Event in CategoryListener!", categoryDeleted[0]);

      for (int i = 0; i < attachmentDeleted.length; i++) {
        if (!attachmentDeleted[i])
          fail("Missed attachmentDeleted Event in AttachmentListener!");
      }
    } catch (PersistenceException e) {
      TestUtils.fail(e);
    } finally {
      if (newsListener != null)
        DynamicDAO.removeEntityListener(INews.class, newsListener);
      if (attachmentListener != null)
        DynamicDAO.removeEntityListener(IAttachment.class, attachmentListener);
      if (personListener != null)
        DynamicDAO.removeEntityListener(IPerson.class, personListener);
      if (categoryListener != null)
        DynamicDAO.removeEntityListener(ICategory.class, categoryListener);
    }
  }
}