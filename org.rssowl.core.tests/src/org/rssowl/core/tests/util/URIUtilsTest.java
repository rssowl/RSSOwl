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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.rssowl.core.util.URIUtils;

import java.net.URI;

/**
 * Tests methods in {@link URIUtils}.
 *
 * @author bpasero
 */
public class URIUtilsTest {

  /**
   * @throws Exception
   */
  @Test
  public void testLooksLikeFeedLink() throws Exception {
    assertFalse(URIUtils.looksLikeFeedLink(""));
    assertFalse(URIUtils.looksLikeFeedLink(" "));
    assertFalse(URIUtils.looksLikeFeedLink("foo bar"));
    assertFalse(URIUtils.looksLikeFeedLink("www.domain.org"));
    assertFalse(URIUtils.looksLikeFeedLink("http://www.domain.org"));

    assertTrue(URIUtils.looksLikeFeedLink("http://www.domain.de/foobar.rss"));
    assertTrue(URIUtils.looksLikeFeedLink("http://www.domain.de/foobar.rdf"));
    assertTrue(URIUtils.looksLikeFeedLink("http://www.domain.de/foobar.xml"));
    assertTrue(URIUtils.looksLikeFeedLink("http://www.domain.de/foobar.atom"));

    assertTrue(URIUtils.looksLikeFeedLink("feed://www.domain.de/news.php"));
    assertTrue(URIUtils.looksLikeFeedLink("feed://www.domain.de/foobar.atom"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLooksLikeLink() throws Exception {
    assertFalse(URIUtils.looksLikeLink(""));
    assertFalse(URIUtils.looksLikeLink(" "));
    assertFalse(URIUtils.looksLikeLink("foo bar"));

    assertTrue(URIUtils.looksLikeLink("www.domain.org"));
    assertTrue(URIUtils.looksLikeLink("http://www.domain.org"));
    assertTrue(URIUtils.looksLikeLink("http://www.domain.de/foobar.rss"));
    assertTrue(URIUtils.looksLikeLink("http://domain.de/foobar.rss"));
    assertTrue(URIUtils.looksLikeLink("feed://www.domain.de/foobar.rss"));
    assertTrue(URIUtils.looksLikeLink("feed://domain.de/foobar.rss"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testToFaviconUri() throws Exception {
    assertEquals("http://www.domain.de/favicon.ico", URIUtils.toFaviconUrl(new URI("http://www.domain.de/path/index.html"), false).toString());
    assertEquals("http://www.domain.de/favicon.ico", URIUtils.toFaviconUrl(new URI("http://www.domain.de/path/"), false).toString());
    assertEquals("http://www.domain.de/favicon.ico", URIUtils.toFaviconUrl(new URI("http://www.domain.de"), false).toString());
    assertEquals("http://domain.de/favicon.ico", URIUtils.toFaviconUrl(new URI("http://test.domain.de/path/index.html"), true).toString());
    assertEquals("http://domain.de/favicon.ico", URIUtils.toFaviconUrl(new URI("http://test.domain.de/path/"), true).toString());
    assertEquals("http://domain.de/favicon.ico", URIUtils.toFaviconUrl(new URI("http://test.domain.de"), true).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetFile() throws Exception {
    assertEquals("foo", URIUtils.getFile(new URI("foo"), null));
    assertEquals("foo.bar", URIUtils.getFile(new URI("foo.bar"), null));
    assertEquals("bar", URIUtils.getFile(new URI("foo/bar"), null));
    assertEquals("bar", URIUtils.getFile(new URI("/foo/bar"), null));
    assertEquals("bar.txt", URIUtils.getFile(new URI("/foo/bar.txt"), null));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetFile2() throws Exception {
    assertEquals("foo", URIUtils.getFile(new URI("foo"), "foo"));
    assertEquals("foo.bar", URIUtils.getFile(new URI("foo.bar"), "bar"));
    assertEquals("bar", URIUtils.getFile(new URI("foo/bar"), "foo"));
    assertEquals("bar", URIUtils.getFile(new URI("/foo/bar"), "bar"));
    assertEquals("bar.txt", URIUtils.getFile(new URI("/foo/bar.txt"), "foo"));
    assertEquals("levin11102009.mp3", URIUtils.getFile(new URI("http://server.com/dloadTrack.mp3?prm=2069xhttp://other.server.com/levin11102009.mp3"), "mp3"));
    assertEquals("levin11102009.mp3", URIUtils.getFile(new URI("http://server.com/dloadTrack.mp3?http://other.server.com/levin11102009.mp3"), "mp3"));
    assertEquals("dloadTrack.mp3", URIUtils.getFile(new URI("http://server.com/dloadTrack.mp3?http://other.server.com/levin11102009.mp3"), "ogg"));
    assertEquals("baba.ogg", URIUtils.getFile(new URI("http://server.com/dloadTrack.mp3?baba.ogg"), "ogg"));
    assertEquals("dloadTrack.mp3_baba.ogg", URIUtils.getFile(new URI("http://server.com/dloadTrack.mp3_baba.ogg"), "ogg"));
    assertEquals("http://server.com", URIUtils.getFile(new URI("http://server.com"), "ogg"));
    assertEquals("podcast.mp3", URIUtils.getFile(new URI("http://server.com/download.php?file=podcast.mp3"), "mp3"));
    assertEquals("podcast.mp3", URIUtils.getFile(new URI("http://server.com/download.php?test=true&file=podcast.mp3&cached"), "mp3"));
    assertEquals("", URIUtils.getFile(new URI(""), "mp3"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNormalize() throws Exception {
    assertEquals("http://www.rssowl.org", URIUtils.normalizeUri(new URI("http://www.rssowl.org/path"), false).toString());
    assertEquals("http://www.rssowl.org", URIUtils.normalizeUri(new URI("http://www.rssowl.org/path"), true).toString());
    assertEquals("http://www.rssowl.org:80", URIUtils.normalizeUri(new URI("http://www.rssowl.org:80/path"), true).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testResolve() throws Exception {
    URI baseWithTrailingSlash = new URI("http://www.rssowl.org/");
    URI baseWithoutTrailingSlash = new URI("http://www.rssowl.org");

    URI base2WithTrailingSlash = new URI("http://www.rssowl.org/other/");
    URI base2WithoutTrailingSlash = new URI("http://www.rssowl.org/other");

    URI relativeWithLeadingSlash = new URI("/path/download.mp3");
    URI relativeWithoutLeadingSlash = new URI("path/download.mp3");

    assertEquals("http://www.rssowl.org/path/download.mp3", URIUtils.resolve(baseWithTrailingSlash, relativeWithLeadingSlash).toString());
    assertEquals("http://www.rssowl.org/path/download.mp3", URIUtils.resolve(baseWithTrailingSlash, relativeWithoutLeadingSlash).toString());

    assertEquals("http://www.rssowl.org/path/download.mp3", URIUtils.resolve(baseWithoutTrailingSlash, relativeWithLeadingSlash).toString());
    assertEquals("http://www.rssowl.org/path/download.mp3", URIUtils.resolve(baseWithoutTrailingSlash, relativeWithoutLeadingSlash).toString());

    assertEquals("http://www.rssowl.org/path/download.mp3", URIUtils.resolve(base2WithTrailingSlash, relativeWithLeadingSlash).toString());
    assertEquals("http://www.rssowl.org/other/path/download.mp3", URIUtils.resolve(base2WithTrailingSlash, relativeWithoutLeadingSlash).toString());

    assertEquals("http://www.rssowl.org/path/download.mp3", URIUtils.resolve(base2WithoutTrailingSlash, relativeWithLeadingSlash).toString());
    assertEquals("http://www.rssowl.org/other/path/download.mp3", URIUtils.resolve(base2WithoutTrailingSlash, relativeWithoutLeadingSlash).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testConvertToToplevel() throws Exception {
    assertEquals("http://www.rssowl.org", URIUtils.toTopLevel(new URI("http://www.rssowl.org")).toString());
    assertEquals("http://www.rssowl.org", URIUtils.toTopLevel(new URI("http://www.rssowl.org/")).toString());
    assertEquals("http://rssowl.org", URIUtils.toTopLevel(new URI("http://rssowl.org/")).toString());
    assertEquals("http://www.rssowl.org", URIUtils.toTopLevel(new URI("feed://www.rssowl.org/")).toString());
    assertEquals("http://www.rssowl.org", URIUtils.toTopLevel(new URI("http://www.rssowl.org/path/index.html")).toString());
    assertEquals(null, URIUtils.toTopLevel(new URI("/path/index.html")));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testManagedLinks() throws Exception {
    assertEquals("http://www.rssowl.org" + URIUtils.MANAGED_LINK_IDENTIFIER, URIUtils.toManaged("http://www.rssowl.org"));
    assertEquals("http://www.rssowl.org/foo" + URIUtils.MANAGED_LINK_IDENTIFIER, URIUtils.toManaged("http://www.rssowl.org/foo"));
    assertEquals(null, URIUtils.toManaged(null));
    assertEquals("", URIUtils.toManaged(""));

    assertEquals(true, URIUtils.isManaged("http://www.rssowl.org" + URIUtils.MANAGED_LINK_IDENTIFIER));
    assertEquals(false, URIUtils.isManaged("http://www.rssowl.org" + URIUtils.MANAGED_LINK_IDENTIFIER + "..."));
    assertEquals(false, URIUtils.isManaged("http://www.rssowl.org"));
    assertEquals(false, URIUtils.isManaged(""));
    assertEquals(false, URIUtils.isManaged(null));

    assertEquals("http://www.rssowl.org", URIUtils.toUnManaged("http://www.rssowl.org" + URIUtils.MANAGED_LINK_IDENTIFIER));
    assertEquals("http://www.rssowl.org/foo", URIUtils.toUnManaged(URIUtils.toManaged("http://www.rssowl.org/foo")));
    assertEquals(null, URIUtils.toUnManaged(null));
    assertEquals("", URIUtils.toUnManaged(""));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSafeGetHost() throws Exception {
    assertEquals("foo.de", URIUtils.safeGetHost(URI.create("http://foo.de")));
    assertEquals("foo_bar.de", URIUtils.safeGetHost(URI.create("http://foo_bar.de")));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testToHttp() throws Exception {
    assertEquals("http://foo.de", URIUtils.toHTTP(URI.create("http://foo.de")).toString());
    assertEquals("http://foo.de", URIUtils.toHTTP("http://foo.de").toString());
    assertEquals("https://foo.de", URIUtils.toHTTP(URI.create("https://foo.de")).toString());
    assertEquals("https://foo.de", URIUtils.toHTTP("https://foo.de").toString());
    assertEquals("http://foo.de", URIUtils.toHTTP(URI.create("feed://foo.de")).toString());
    assertEquals("http://foo.de", URIUtils.toHTTP("feed://foo.de").toString());
    assertEquals("http://foo.de", URIUtils.toHTTP(URI.create("reader://foo.de")).toString());
    assertEquals("http://foo.de", URIUtils.toHTTP("reader://foo.de").toString());
    assertEquals("https://foo.de", URIUtils.toHTTP(URI.create("readers://foo.de")).toString());
    assertEquals("https://foo.de", URIUtils.toHTTP("readers://foo.de").toString());

    assertEquals("https://foo.de/test/123.html/?help=true&foo=bar", URIUtils.toHTTP(URI.create("readers://foo.de/test/123.html/?help=true&foo=bar")).toString());
    assertEquals("https://foo.de/test/123.html/?help=true&foo=bar", URIUtils.toHTTP("readers://foo.de/test/123.html/?help=true&foo=bar").toString());
  }
}