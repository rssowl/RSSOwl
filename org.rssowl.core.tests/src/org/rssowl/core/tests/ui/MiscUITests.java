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

package org.rssowl.core.tests.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.ApplicationServer;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.LinkTransformer;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.ShareProvider;
import org.rssowl.ui.internal.actions.LabelAction;
import org.rssowl.ui.internal.actions.MakeNewsStickyAction;
import org.rssowl.ui.internal.actions.MoveCopyNewsToBinAction;
import org.rssowl.ui.internal.actions.ToggleReadStateAction;
import org.rssowl.ui.internal.editors.feed.NewsColumn;
import org.rssowl.ui.internal.editors.feed.NewsColumnViewModel;
import org.rssowl.ui.internal.editors.feed.NewsComparator;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Testing the <code>RSSOwlUI</code> facade.
 *
 * @author bpasero
 */
public class MiscUITests {
  private IModelFactory fFactory = Owl.getModelFactory();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFavicon() throws Exception {

    /* Delete previously stored favicons */
    for (int i = 0; i < 5; i++)
      OwlUI.deleteImage(i);

    IFeed feed = new Feed(new URI("http://www.rssowl.org/node/feed"));
    IFolder root = new Folder(null, null, "Root");
    IBookMark bookmark = new BookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    root.addMark(bookmark, null, false);

    feed = DynamicDAO.save(feed);
    DynamicDAO.save(root);

    assertEquals(null, OwlUI.getFavicon(bookmark));

    Controller.getDefault().reload(bookmark, null, new NullProgressMonitor());

    assertNotNull(OwlUI.getFavicon(bookmark));

    DynamicDAO.delete(bookmark);

    assertEquals(null, OwlUI.getFavicon(bookmark));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testShareProviders() throws Exception {
    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");

    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news = Owl.getModelFactory().createNews(null, feed, new Date());
    news.setLink(new URI("link"));
    news.setTitle("My News");
    DynamicDAO.save(feed);

    IBookMark bm1 = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "My Bookmark");
    IBookMark bm2 = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(feed.getLink()), "My <other> ? & Bo_öokmark");
    folder = DynamicDAO.save(folder);

    List<ShareProvider> providers = Controller.getDefault().getShareProviders();
    for (ShareProvider provider : providers) {
      try {
        if ("org.rssowl.ui.SendLinkAction".equals(provider.getId()))
          continue;

        assertTrue(URIUtils.looksLikeLink(provider.toShareUrl(bm1)));
        assertTrue(URIUtils.looksLikeLink(provider.toShareUrl(bm2)));
        assertTrue(URIUtils.looksLikeLink(provider.toShareUrl(news)));
      } catch (AssertionError e) {
        throw new Exception(provider.getName(), e.getCause());
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLinkTransformers() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news = Owl.getModelFactory().createNews(null, feed, new Date());
    news.setLink(new URI("http://www.link.org"));
    news.setTitle("My News");
    DynamicDAO.save(feed);

    List<LinkTransformer> transformers = Controller.getDefault().getLinkTransformers();
    for (LinkTransformer transformer : transformers) {
      try {
        assertTrue(URIUtils.looksLikeLink(transformer.toTransformedUrl(news)));
      } catch (AssertionError e) {
        throw new Exception(transformer.getName(), e.getCause());
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMoveCopyToBinAction() throws Exception {
    IFolder folder = fFactory.createFolder(null, null, "Root");
    INewsBin bin = fFactory.createNewsBin(null, folder, "Bin");
    folder = DynamicDAO.save(folder);
    bin = (INewsBin) folder.getMarks().get(0);

    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News 1");
    INews news2 = Owl.getModelFactory().createNews(null, feed, new Date());
    news2.setTitle("News 2");
    INews news3 = Owl.getModelFactory().createNews(null, feed, new Date());
    news3.setTitle("News 3");
    DynamicDAO.save(feed);

    MoveCopyNewsToBinAction action = new MoveCopyNewsToBinAction(new StructuredSelection(news1), bin, false);
    action.run();

    assertEquals(1, bin.getNews().size());
    assertEquals("News 1", bin.getNews().get(0).getTitle());

    action = new MoveCopyNewsToBinAction(new StructuredSelection(news2), bin, true);
    action.run();

    assertEquals(2, bin.getNews().size());
    assertEquals("News 1", bin.getNews().get(0).getTitle());
    assertEquals("News 2", bin.getNews().get(1).getTitle());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testToggleReadStateAction() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News 1");
    news1.setState(INews.State.READ);
    INews news2 = Owl.getModelFactory().createNews(null, feed, new Date());
    news2.setTitle("News 2");
    news2.setState(INews.State.READ);
    INews news3 = Owl.getModelFactory().createNews(null, feed, new Date());
    news3.setTitle("News 3");
    DynamicDAO.save(feed);

    ToggleReadStateAction action = new ToggleReadStateAction(new StructuredSelection(new Object[] { news1, news2 }));
    action.run();

    assertEquals(INews.State.UNREAD, news1.getState());
    assertEquals(INews.State.UNREAD, news2.getState());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMakeStickyAction() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News 1");
    INews news2 = Owl.getModelFactory().createNews(null, feed, new Date());
    news2.setTitle("News 2");
    INews news3 = Owl.getModelFactory().createNews(null, feed, new Date());
    news3.setTitle("News 3");
    DynamicDAO.save(feed);

    MakeNewsStickyAction action = new MakeNewsStickyAction(new StructuredSelection(new Object[] { news1, news2 }));
    action.run();

    assertTrue(news1.isFlagged());
    assertTrue(news2.isFlagged());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLabelAction() throws Exception {
    ILabel label1 = DynamicDAO.save(fFactory.createLabel(null, "Foo"));

    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setTitle("News 1");
    INews news2 = Owl.getModelFactory().createNews(null, feed, new Date());
    news2.setTitle("News 2");
    INews news3 = Owl.getModelFactory().createNews(null, feed, new Date());
    news3.setTitle("News 3");
    DynamicDAO.save(feed);

    LabelAction action = new LabelAction(label1, new StructuredSelection(new Object[] { news1, news2 }));
    action.setChecked(true);
    action.run();

    assertEquals(1, news1.getLabels().size());
    assertEquals(1, news2.getLabels().size());

    action = new LabelAction(label1, new StructuredSelection(new Object[] { news1, news2 }));
    action.run();

    assertEquals(0, news1.getLabels().size());
    assertEquals(0, news2.getLabels().size());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsColumnViewModel() throws Exception {
    IFolder root = DynamicDAO.save(fFactory.createFolder(null, null, "Root"));
    IPreferenceScope prefs = Owl.getPreferenceService().getEntityScope(root);

    NewsColumnViewModel model = NewsColumnViewModel.createFrom(new int[] { 5, 4, 3, 6, 1 }, 3, true);
    model.saveTo(prefs);

    NewsColumnViewModel loadedModel = NewsColumnViewModel.loadFrom(prefs);
    assertTrue(loadedModel.isAscending());
    assertEquals(3, loadedModel.getSortColumn().ordinal());
    List<NewsColumn> columns = loadedModel.getColumns();
    assertEquals(NewsColumn.values()[5], columns.get(0));
    assertEquals(NewsColumn.values()[4], columns.get(1));
    assertEquals(NewsColumn.values()[3], columns.get(2));
    assertEquals(NewsColumn.values()[6], columns.get(3));
    assertEquals(NewsColumn.values()[1], columns.get(4));
  }

  /**
   * Tests some modes of the {@link NewsComparator}.
   *
   * @throws Exception
   */
  @Test
  public void testNewsComparator() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date(0));
    news1.setTitle("A News");

    INews news2 = Owl.getModelFactory().createNews(null, feed, new Date(100));
    news2.setTitle("B News");

    INews news3 = Owl.getModelFactory().createNews(null, feed, new Date());
    news3.setTitle("C News");
    DynamicDAO.save(feed);

    NewsComparator comp = new NewsComparator();

    /* By Title */
    comp.setSortBy(NewsColumn.TITLE);
    comp.setAscending(true);
    Object[] elements = new Object[] { news1, news2, news3 };
    comp.sort(null, elements);

    assertEquals("A News", ((INews) elements[0]).getTitle());
    assertEquals("B News", ((INews) elements[1]).getTitle());
    assertEquals("C News", ((INews) elements[2]).getTitle());

    /* By Date */
    comp.setSortBy(NewsColumn.DATE);
    comp.setAscending(false);

    comp.sort(null, elements);

    assertEquals("C News", ((INews) elements[0]).getTitle());
    assertEquals("B News", ((INews) elements[1]).getTitle());
    assertEquals("A News", ((INews) elements[2]).getTitle());

    /* By Label */
    comp.setSortBy(NewsColumn.LABELS);

    comp.sort(null, elements);

    assertEquals("C News", ((INews) elements[0]).getTitle());
    assertEquals("B News", ((INews) elements[1]).getTitle());
    assertEquals("A News", ((INews) elements[2]).getTitle());

    ILabel label1 = Owl.getModelFactory().createLabel(null, "Label 1");
    label1.setOrder(0);

    ILabel label2 = Owl.getModelFactory().createLabel(null, "Label 2");
    label2.setOrder(1);

    ILabel label3 = Owl.getModelFactory().createLabel(null, "Label 3");
    label3.setOrder(2);

    news1.addLabel(label1);
    comp.setAscending(true);

    comp.sort(null, elements);

    /*
     * News 1: Label_1 News 3: - News 2: -
     */
    assertEquals("A News", ((INews) elements[0]).getTitle());
    assertEquals("C News", ((INews) elements[1]).getTitle());
    assertEquals("B News", ((INews) elements[2]).getTitle());

    news1.addLabel(label2);

    comp.sort(null, elements);

    /*
     * News 1: Label_1, Label_2 News 3: - News 2: -
     */
    assertEquals("A News", ((INews) elements[0]).getTitle());
    assertEquals("C News", ((INews) elements[1]).getTitle());
    assertEquals("B News", ((INews) elements[2]).getTitle());

    news2.addLabel(label1);

    comp.sort(null, elements);

    /*
     * News 1: Label_1, Label_2 News 2: Label_1 News 3: -
     */
    assertEquals("A News", ((INews) elements[0]).getTitle());
    assertEquals("B News", ((INews) elements[1]).getTitle());
    assertEquals("C News", ((INews) elements[2]).getTitle());

    comp.setAscending(false);
    comp.sort(null, elements);

    /*
     * News 3: - News 2: Label_1 News 1: Label_1, Label_2
     */
    assertEquals("C News", ((INews) elements[0]).getTitle());
    assertEquals("B News", ((INews) elements[1]).getTitle());
    assertEquals("A News", ((INews) elements[2]).getTitle());

    news3.addLabel(label2);

    comp.setAscending(true);
    comp.sort(null, elements);

    /*
     * News 1: Label_1, Label_2 News 2: Label_1 News 3: Label_2
     */
    assertEquals("A News", ((INews) elements[0]).getTitle());
    assertEquals("B News", ((INews) elements[1]).getTitle());
    assertEquals("C News", ((INews) elements[2]).getTitle());

    news3.addLabel(label1);

    comp.sort(null, elements);

    /*
     * News 3: Label_1, Label_2 News 1: Label_1, Label_2 News 2: Label_1
     */
    assertEquals("C News", ((INews) elements[0]).getTitle());
    assertEquals("A News", ((INews) elements[1]).getTitle());
    assertEquals("B News", ((INews) elements[2]).getTitle());

    news3.addLabel(label3);

    comp.sort(null, elements);

    /*
     * News 3: Label_1, Label_2, Label_3 News 1: Label_1, Label_2 News 2:
     * Label_1
     */
    assertEquals("C News", ((INews) elements[0]).getTitle());
    assertEquals("A News", ((INews) elements[1]).getTitle());
    assertEquals("B News", ((INews) elements[2]).getTitle());

    news2.addLabel(label3);

    comp.sort(null, elements);

    /*
     * News 3: Label_1, Label_2, Label_3 News 1: Label_1, Label_2 News 2:
     * Label_1, Label_3
     */
    assertEquals("C News", ((INews) elements[0]).getTitle());
    assertEquals("A News", ((INews) elements[1]).getTitle());
    assertEquals("B News", ((INews) elements[2]).getTitle());
  }

  /**
   * TODO Not complete because the ApplicationServer requires a
   * {@link NewsBrowserViewer} instance to function.
   *
   * @throws Exception
   */
  @Test
  public void testApplicationServer() throws Exception {
    ApplicationServer server = ApplicationServer.getDefault();
    server.startup();

    ILabel label = DynamicDAO.save(fFactory.createLabel(null, "Label"));

    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("feed"));
    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date(0));
    news1.setTitle("A News");
    news1.setDescription("Foo Bar");
    news1.setFlagged(true);
    news1.addLabel(label);

    IPerson author = fFactory.createPerson(null, news1);
    author.setName("Benjamin Pasero");
    author.setEmail(new URI("foo@bar.de"));
    news1.setAuthor(author);

    ICategory category = fFactory.createCategory(null, news1);
    category.setName("category");
    news1.addCategory(category);

    IAttachment attachment = fFactory.createAttachment(null, news1);
    attachment.setLink(new URI("attachment"));
    news1.addAttachment(attachment);

    feed = DynamicDAO.save(feed);

    IFolder root = fFactory.createFolder(null, null, "Root");
    IBookMark bm = fFactory.createBookMark(null, root, new FeedLinkReference(feed.getLink()), "Bookmark");
    DynamicDAO.save(root);

    String newsUrl = server.toUrl("foo", news1);
    assertTrue(URIUtils.looksLikeLink(newsUrl));

    String bmUrl = server.toUrl("foo", bm);
    assertTrue(URIUtils.looksLikeLink(bmUrl));
  }
}