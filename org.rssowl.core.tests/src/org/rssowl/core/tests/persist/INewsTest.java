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
package org.rssowl.core.tests.persist;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.rssowl.core.internal.persist.DefaultModelFactory;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;

/**
 * Unit tests for INews.
 */
public class INewsTest {

  private IModelFactory fFactory = new DefaultModelFactory();

  /**
   * See:
   * <li>bug #558 : Consider not using GUID if isPermaLink is false.</li>
   * <li>bug 958: Consider GUID if equal regardless of isPermaLink.</li>
   *
   * <p>
   * Tests that isPermalink is ignored in positive matches, but is considered
   * for negative matches. In the latter case, if the guid does not match and
   * isPermalink == false then the guid is ignored (as if it was null).
   * </p>
   * @throws URISyntaxException
   */
  @Test
  public void testIsEquivalentWithGuidIsPermaLinkFalse() throws URISyntaxException    {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    String link = "www.link.com";
    fFactory.createGuid(news1, link, false);
    news1.setLink(new URI(link));

    INews news2 = fFactory.createNews(null, feed, new Date());
    fFactory.createGuid(news2, link, false);
    news2.setLink(new URI(link));

    INews news3 = fFactory.createNews(null, feed, new Date());
    fFactory.createGuid(news3, "http://www.anotherlink.com", false);
    news3.setLink(new URI(link));

    INews news4 = fFactory.createNews(null, feed, new Date());
    fFactory.createGuid(news4, link, false);
    news4.setLink(new URI("www.anotherlink2.com"));

    INews news5 = fFactory.createNews(null, feed, new Date());
    fFactory.createGuid(news5, "http://www.anotherlink.com", false);
    news5.setLink(new URI("www.anotherlink2.com"));

    assertTrue(news1.isEquivalent(news2));
    assertTrue(news1.isEquivalent(news3));
    assertTrue(news1.isEquivalent(news4));

    assertTrue(news2.isEquivalent(news3));
    assertTrue(news2.isEquivalent(news4));

    assertFalse(news3.isEquivalent(news4));
    assertTrue(news4.isEquivalent(news5));
  }

  /**
   * Tests that two INews with the same guid value and link are equivalent if
   * one of them has isPermaLink == true and the other has isPermaLink == false.
   * This is a backwards compatibility test. We may decide to remove it after
   * M7.
   *
   * @throws URISyntaxException
   */
  @Test
  public void testIsEquivalentWithGuidPermaLinkTrueAndPermaLinkFalse() throws URISyntaxException {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    String link = "www.link.com";
    fFactory.createGuid(news1, link, true);
    news1.setLink(new URI(link));

    INews news2 = fFactory.createNews(null, feed, new Date());
    fFactory.createGuid(news2, link, false);
    news2.setLink(new URI(link));

    assertTrue(news1.isEquivalent(news2));
  }

  /**
   * Tests isEquivalent when one of the Guids is null and the other has
   * permaLink == true.
   * @throws URISyntaxException
   */
  @Test
  public void testIsEquivalentWithNullGuid() throws URISyntaxException {
    IFeed feed = fFactory.createFeed(null, new URI("http://www.feed.com"));

    INews news1 = fFactory.createNews(null, feed, new Date());
    String link = "www.link.com";
    fFactory.createGuid(news1, link, true);
    news1.setLink(new URI(link));

    INews news2 = fFactory.createNews(null, feed, new Date());
    news2.setLink(new URI(link));

    assertFalse(news1.isEquivalent(news2));
  }

  /**
   * Tests that calling INews#merge merges the permalink of the Guid correctly.
   * @throws Exception
   */
  @Test
  public void testMergeGuidPermalink() throws Exception {
    IFeed parent = fFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fFactory.createNews(null, parent, new Date());
    news.setId(1L);
    fFactory.createGuid(news, "www.news.com", true);

    URI newsLink = new URI("http://www.news.com");
    news.setLink(newsLink);
    INews otherNews = fFactory.createNews(null, parent, new Date());
    otherNews.setLink(newsLink);
    fFactory.createGuid(otherNews, "www.news.com", false);

    //TODO Because description is now lazy-loaded from the db, this specific
    //method fails when the test is executed as a unit test. Fix it if possible,
    //or move it to the integration tests
    news.merge(otherNews);
    assertEquals(false, news.getGuid().isPermaLink());

    /* This test is specific to our News implementation */
    assertEquals(false, getNewsFGuidIsPermaLink(news));
  }

  private boolean getNewsFGuidIsPermaLink(INews news) throws Exception  {
    for (Field field : news.getClass().getDeclaredFields()) {
      if (field.getName().equals("fGuidIsPermaLink")) {
        field.setAccessible(true);
        return (Boolean) field.get(news);
      }
    }
    throw new IllegalStateException();
  }

  /**
   * Tests that adding a label to a news copy works correctly.
   * @throws Exception
   */
  @Test
  public void testAddLabelToNewsCopy() throws Exception {
    IFeed parent = fFactory.createFeed(null, new URI("http://www.feed.com"));
    INews news = fFactory.createNews(null, parent, new Date());
    IFolder folder = fFactory.createFolder(null, null, "folder");
    INewsBin newsBin = fFactory.createNewsBin(1L, folder, "newsbin");
    INews newsCopy = fFactory.createNews(news, newsBin);
    newsCopy.addLabel(fFactory.createLabel(null, "label"));
    assertEquals(1, newsCopy.getLabels().size());
    Set<ILabel> labels = news.getLabels();
    labels.add(fFactory.createLabel(null, "Another label"));
    assertEquals(1, labels.size());
  }

  /**
   * Tests that calling INews#merge merges the permalink of the Guid correctly.
   * @throws Exception
   */
  @Test
  public void testMergeSyncedNews() throws Exception {
    IFeed feed = fFactory.createFeed(null, new URI("reader://www.feed.com"));

    INews news = fFactory.createNews(null, feed, new Date());
    news.setTitle("Hello World");
    news.setId(1L);

    INews otherNews = fFactory.createNews(null, feed, new Date());
    otherNews.setTitle("Hello World");
    otherNews.setFlagged(true);
    otherNews.setComments("Comments");
    otherNews.setId(1L);

    news.merge(otherNews);
    assertTrue(news.isFlagged());
    assertNull(news.getComments());

    otherNews.setTitle("Hello World *Updated*");
    news.merge(otherNews);
    assertTrue(news.isFlagged());
    assertEquals("Comments", news.getComments());

    otherNews.setPublishDate(new Date(1000));
    otherNews.setComments("Updated Comments");

    news.merge(otherNews);
    assertTrue(news.isFlagged());
    assertEquals("Updated Comments", news.getComments());

    otherNews.setModifiedDate(new Date(1000));
    otherNews.setComments("Updated Comments *Update*");

    news.merge(otherNews);
    assertTrue(news.isFlagged());
    assertEquals("Updated Comments *Update*", news.getComments());
  }
}