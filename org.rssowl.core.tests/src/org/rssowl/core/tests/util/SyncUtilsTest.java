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

package org.rssowl.core.tests.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.SyncUtils;

import java.net.URI;
import java.util.Date;

/**
 * Tests methods in {@link SyncUtils} that are not connecting to the internet.
 *
 * @author bpasero
 */
public class SyncUtilsTest {

  /**
   * @throws Exception
   */
  @Test
  public void testIsNewsSynchronized() throws Exception {
    IFeed feed = Owl.getModelFactory().createFeed(null, new URI("reader://rssowl.org"));

    INews news1 = Owl.getModelFactory().createNews(null, feed, new Date());
    news1.setLink(URI.create("http://www.rssowl.org"));
    INews news2 = Owl.getModelFactory().createNews(null, feed, new Date());
    news2.setLink(URI.create("reader://www.rssowl.org"));
    INews news3 = Owl.getModelFactory().createNews(null, feed, new Date());
    news3.setLink(URI.create("readers://www.rssowl.org"));

    assertTrue(SyncUtils.isSynchronized(news1));
    assertTrue(SyncUtils.isSynchronized(news2));
    assertTrue(SyncUtils.isSynchronized(news3));

    news2.setInReplyTo("Foo");
    assertTrue(SyncUtils.isSynchronized(news2));

    news2.setGuid(Owl.getModelFactory().createGuid(news2, "tag:google.com/foo", true));
    assertTrue(SyncUtils.isSynchronized(news2));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsBookmarkSynchronized() throws Exception {
    IFolder folder = Owl.getModelFactory().createFolder(null, null, "Root");

    IBookMark bm1 = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(URI.create("http://www.rssowl.org")), "A");
    IBookMark bm2 = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(URI.create("reader://www.rssowl.org")), "B");
    IBookMark bm3 = Owl.getModelFactory().createBookMark(null, folder, new FeedLinkReference(URI.create("readers://www.rssowl.org")), "C");

    assertFalse(SyncUtils.isSynchronized(bm1));
    assertTrue(SyncUtils.isSynchronized(bm2));
    assertTrue(SyncUtils.isSynchronized(bm3));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testIsURISynchronized() throws Exception {
    assertFalse(SyncUtils.isSynchronized((String) null));
    assertFalse(SyncUtils.isSynchronized(""));
    assertFalse(SyncUtils.isSynchronized("http://www.rssowl.org"));
    assertFalse(SyncUtils.isSynchronized("https://www.rssowl.org"));
    assertFalse(SyncUtils.isSynchronized("feed://www.rssowl.org"));

    assertTrue(SyncUtils.isSynchronized("reader://www.rssowl.org"));
    assertTrue(SyncUtils.isSynchronized("readers://www.rssowl.org"));
  }
}