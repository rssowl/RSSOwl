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

package org.rssowl.core.tests.connection;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.AuthenticationRequiredException;
import org.rssowl.core.connection.HttpConnectionInputStream;
import org.rssowl.core.connection.IConditionalGetCompatible;
import org.rssowl.core.connection.IConnectionPropertyConstants;
import org.rssowl.core.connection.IConnectionService;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.connection.IProtocolHandler;
import org.rssowl.core.connection.IProxyCredentials;
import org.rssowl.core.connection.NotModifiedException;
import org.rssowl.core.connection.PlatformCredentialsProvider;
import org.rssowl.core.internal.connection.DefaultProtocolHandler;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.interpreter.EncodingException;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.RegExUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.Triple;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.LinkTransformer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This TestCase covers use-cases for the Connection Plugin.
 *
 * @author bpasero
 */
public class ConnectionTests {

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl) Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * Test contribution of Credentials Provider.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testProxyCredentialProvider() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rdf.xml");
    IFeed feed = new Feed(feedUrl);

    IProxyCredentials proxyCredentials = conManager.getProxyCredentials(feed.getLink());

    assertEquals("", proxyCredentials.getDomain());
    assertEquals("bpasero", proxyCredentials.getUsername());
    assertEquals("admin", proxyCredentials.getPassword());
    assertEquals("127.0.0.1", proxyCredentials.getHost());
    assertEquals(0, proxyCredentials.getPort());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testGetLabel() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    // URI feedUrl = new URI("http://www.rssowl.org/node/feed");
    URI feedUrl = new URI("http://www.heise.de");
    String label = conManager.getLabel(feedUrl, new NullProgressMonitor());
    assertEquals("RSSOwl News", label);
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testGetFavicon() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    // URI feedUrl = new URI("http://www.rssowl.org/node/feed");
    URI feedUrl = new URI("http://www.heise.de");
    byte[] feedIcon = conManager.getFeedIcon(feedUrl, new NullProgressMonitor());
    assertNotNull(feedIcon);
    assertTrue(feedIcon.length != 0);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testHttpConnectionInputStream() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI url = new URI("http://www.rssowl.org/favicon.ico");
    IProtocolHandler handler = conManager.getHandler(url);
    InputStream stream = handler.openStream(url, null, null);
    if (stream instanceof HttpConnectionInputStream) {
      HttpConnectionInputStream inS = (HttpConnectionInputStream) stream;
      assertTrue(inS.getContentLength() > 0);
      assertNotNull(inS.getIfModifiedSince());
      assertNotNull(inS.getIfNoneMatch());
    }
  }

  /**
   * Test a protected Feed.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testProtectedFeed() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl1 = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rss.xml");
    URI feedUrl2 = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rss_copy.xml");
    ICredentialsProvider credProvider = conManager.getCredentialsProvider(feedUrl1);

    IFeed feed1 = new Feed(feedUrl1);
    IFeed feed2 = new Feed(feedUrl2);
    AuthenticationRequiredException e = null;

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);

    try {
      Owl.getConnectionService().getHandler(feed1.getLink()).openStream(feed1.getLink(), null, null);
    } catch (AuthenticationRequiredException e1) {
      e = e1;
    }

    assertNotNull(e);
    e = null;

    ICredentials credentials = new ICredentials() {
      @Override
      public String getDomain() {
        return null;
      }

      @Override
      public String getPassword() {
        return "admin";
      }

      @Override
      public String getUsername() {
        return "bpasero";
      }
    };

    credProvider.setAuthCredentials(credentials, feedUrl1, null);

    InputStream inS = Owl.getConnectionService().getHandler(feed1.getLink()).openStream(feed1.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed1, null);
    assertEquals("RSS 2.0", feed1.getFormat());

    /* Test authentication by other realm is not working */
    credProvider.setAuthCredentials(credentials, URIUtils.normalizeUri(feedUrl2, true), "Other Directory");

    try {
      Owl.getConnectionService().getHandler(feed2.getLink()).openStream(feed2.getLink(), null, null);
    } catch (AuthenticationRequiredException e1) {
      e = e1;
    }

    assertNotNull(e);

    /* Test authentication by realm is working */
    credProvider.setAuthCredentials(credentials, URIUtils.normalizeUri(feedUrl2, true), "Restricted Directory");

    inS = Owl.getConnectionService().getHandler(feed2.getLink()).openStream(feed2.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed2, null);
    assertEquals("RSS 2.0", feed2.getFormat());

    DynamicDAO.delete(feed1);
    DynamicDAO.delete(feed2);

    assertNull(conManager.getAuthCredentials(feed1.getLink(), null));
    assertNull(conManager.getAuthCredentials(feed2.getLink(), null));

    ((PlatformCredentialsProvider) conManager.getCredentialsProvider(feedUrl2)).clear();
  }

  /**
   * Test a protected Feed.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testProtectedFeedInMemory() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl1 = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rss.xml");
    URI feedUrl2 = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rss_copy.xml");
    ICredentialsProvider credProvider = conManager.getCredentialsProvider(feedUrl1);

    IFeed feed1 = new Feed(feedUrl1);
    IFeed feed2 = new Feed(feedUrl2);
    AuthenticationRequiredException e = null;

    DynamicDAO.save(feed1);
    DynamicDAO.save(feed2);

    try {
      Owl.getConnectionService().getHandler(feed1.getLink()).openStream(feed1.getLink(), null, null);
    } catch (AuthenticationRequiredException e1) {
      e = e1;
    }

    assertNotNull(e);
    e = null;

    ICredentials credentials = new ICredentials() {
      @Override
      public String getDomain() {
        return null;
      }

      @Override
      public String getPassword() {
        return "admin";
      }

      @Override
      public String getUsername() {
        return "bpasero";
      }
    };

    credProvider.setInMemoryAuthCredentials(credentials, feedUrl1, null);

    InputStream inS = Owl.getConnectionService().getHandler(feed1.getLink()).openStream(feed1.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed1, null);
    assertEquals("RSS 2.0", feed1.getFormat());

    /* Test authentication by other realm is not working */
    credProvider.setInMemoryAuthCredentials(credentials, URIUtils.normalizeUri(feedUrl2, true), "Other Directory");

    try {
      Owl.getConnectionService().getHandler(feed2.getLink()).openStream(feed2.getLink(), null, null);
    } catch (AuthenticationRequiredException e1) {
      e = e1;
    }

    assertNotNull(e);

    /* Test authentication by realm is working */
    credProvider.setInMemoryAuthCredentials(credentials, URIUtils.normalizeUri(feedUrl2, true), "Restricted Directory");

    inS = Owl.getConnectionService().getHandler(feed2.getLink()).openStream(feed2.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed2, null);
    assertEquals("RSS 2.0", feed2.getFormat());

    assertNull(conManager.getCredentialsProvider(feed1.getLink()).getPersistedAuthCredentials(feed1.getLink(), null));
    assertNull(conManager.getCredentialsProvider(feed2.getLink()).getPersistedAuthCredentials(feed2.getLink(), null));

    DynamicDAO.delete(feed1);
    DynamicDAO.delete(feed2);

    assertNull(conManager.getAuthCredentials(feed1.getLink(), null));
    assertNull(conManager.getAuthCredentials(feed2.getLink(), null));

    ((PlatformCredentialsProvider) conManager.getCredentialsProvider(feedUrl2)).clear();
  }

  /**
   * Test a normal Feed via HTTP Protocol.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testHTTPFeed() throws Exception {
    URI feedUrl = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/rss_2_0.xml");
    IFeed feed = new Feed(feedUrl);

    InputStream inS = Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed, null);
    assertEquals("RSS 2.0", feed.getFormat());
  }

  /**
   * Test a normal Feed via FEED Protocol.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testFEEDFeed() throws Exception {
    URI feedUrl = new URI("feed://www.rssowl.org/rssowl2dg/tests/connection/rss_2_0.xml");
    IFeed feed = new Feed(feedUrl);

    InputStream inS = Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed, null);
    assertEquals("RSS 2.0", feed.getFormat());
  }

  /**
   * Test a normal Feed via HTTP Protocol.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testHTTPSFeed() throws Exception {
    // URI feedUrl = new URI("https://sourceforge.net/export/rss2_projnews.php?group_id=141424&rss_fulltext=1");
    URI feedUrl = new URI("https://www.heise.de");
    IFeed feed = new Feed(feedUrl);

    InputStream inS = Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, null);
    assertNotNull(inS);

    Owl.getInterpreter().interpret(inS, feed, null);
    assertEquals("RSS 2.0", feed.getFormat());
  }

  /**
   * Test a normal Feed via FILE Protocol.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testFILEFeed() throws Exception {
    URL pluginLocation = FileLocator.toFileURL(Platform.getBundle("org.rssowl.core.tests").getEntry("/"));
    IConnectionService conManager = Owl.getConnectionService();
    URL feedUrl = pluginLocation.toURI().resolve("data/interpreter/feed_rss.xml").toURL();
    IFeed feed = new Feed(feedUrl.toURI());

    Triple<IFeed, IConditionalGet, URI> result = conManager.reload(feed.getLink(), null, null);

    assertEquals("RSS 2.0", result.getFirst().getFormat());
  }

  /**
   * Test Conditional GET with a compatible Feed.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testConditionalGet() throws Exception {
    URI feedUrl = new URI("http://rss.slashdot.org/Slashdot/slashdot/to");
    IFeed feed = new Feed(feedUrl);
    NotModifiedException e = null;

    InputStream inS = Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, null);
    assertNotNull(inS);

    String ifModifiedSince = null;
    String ifNoneMatch = null;
    if (inS instanceof IConditionalGetCompatible) {
      ifModifiedSince = ((IConditionalGetCompatible) inS).getIfModifiedSince();
      ifNoneMatch = ((IConditionalGetCompatible) inS).getIfNoneMatch();
    }
    IConditionalGet conditionalGet = Owl.getModelFactory().createConditionalGet(ifModifiedSince, feedUrl, ifNoneMatch);

    Map<Object, Object> conProperties = new HashMap<Object, Object>();
    ifModifiedSince = conditionalGet.getIfModifiedSince();
    if (ifModifiedSince != null)
      conProperties.put(IConnectionPropertyConstants.IF_MODIFIED_SINCE, ifModifiedSince);

    ifNoneMatch = conditionalGet.getIfNoneMatch();
    if (ifNoneMatch != null)
      conProperties.put(IConnectionPropertyConstants.IF_NONE_MATCH, ifNoneMatch);

    try {
      Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, conProperties);
    } catch (NotModifiedException e1) {
      e = e1;
    }

    assertNotNull(e);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testStoredCredentialsDeleted() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rdf.xml");
    IFeed feed = new Feed(feedUrl);

    DynamicDAO.save(feed);

    ICredentials authCreds = new ICredentials() {
      @Override
      public String getDomain() {
        return null;
      }

      @Override
      public String getPassword() {
        return "admin";
      }

      @Override
      public String getUsername() {
        return "bpasero";
      }
    };

    conManager.getCredentialsProvider(feedUrl).setAuthCredentials(authCreds, feedUrl, null);

    assertNotNull(conManager.getAuthCredentials(feedUrl, null));

    DynamicDAO.delete(new FeedLinkReference(feedUrl).resolve());

    assertNull(conManager.getAuthCredentials(feedUrl, null));
    assertNull(conManager.getCredentialsProvider(feedUrl).getPersistedAuthCredentials(feedUrl, null));

    ((PlatformCredentialsProvider) conManager.getCredentialsProvider(feedUrl)).clear();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testInMemoryCredentialsDeleted() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/authrequired/feed_rdf.xml");
    IFeed feed = new Feed(feedUrl);

    DynamicDAO.save(feed);

    ICredentials authCreds = new ICredentials() {
      @Override
      public String getDomain() {
        return null;
      }

      @Override
      public String getPassword() {
        return "admin";
      }

      @Override
      public String getUsername() {
        return "bpasero";
      }
    };

    conManager.getCredentialsProvider(feedUrl).setInMemoryAuthCredentials(authCreds, feedUrl, null);

    assertNotNull(conManager.getAuthCredentials(feedUrl, null));
    assertNull(conManager.getCredentialsProvider(feedUrl).getPersistedAuthCredentials(feedUrl, null));

    DynamicDAO.delete(new FeedLinkReference(feedUrl).resolve());

    assertNull(conManager.getAuthCredentials(feedUrl, null));
    assertNull(conManager.getCredentialsProvider(feedUrl).getPersistedAuthCredentials(feedUrl, null));

    ((PlatformCredentialsProvider) conManager.getCredentialsProvider(feedUrl)).clear();
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testLoadFeedFromWebsiteWithRedirect() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("http://www.planeteclipse.org");

    assertEquals("http://www.planeteclipse.org/planet/rss20.xml", conManager.getFeed(feedUrl, new NullProgressMonitor()).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testLoadFeedFromWebsiteWithoutRedirect() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("http://www.heise.de");

    assertEquals("https://www.heise.de/newsticker/heise-atom.xml", conManager.getFeed(feedUrl, new NullProgressMonitor()).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testLoadEntityEncodedFeedFromWebsite() throws Exception {
    IConnectionService conManager = Owl.getConnectionService();
    URI feedUrl = new URI("http://www.rssowl.org/rssowl2dg/tests/connection/homepage.html");

    assertEquals("http://www.rssowl.org/node/feed&help=true", conManager.getFeed(feedUrl, new NullProgressMonitor()).toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testKeywordFeeds() throws Exception {
    String keywords = "blog feed";
    String URL_INPUT_TOKEN = "[:]";
    String KEYWORD_FEED_EXTENSION_POINT = "org.rssowl.ui.KeywordFeed";

    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IConfigurationElement elements[] = reg.getConfigurationElementsFor(KEYWORD_FEED_EXTENSION_POINT);

    /* For each contributed property keyword feed */
    for (IConfigurationElement element : elements) {
      String id = element.getAttribute("id");
      if ("org.rssowl.ui.DiggKeywordFeed".equals(id))
        continue;

      String url = element.getAttribute("url");

      String feedUrlStr = StringUtils.replaceAll(url, URL_INPUT_TOKEN, URIUtils.urlEncode(keywords));
      URI feedUrl = new URI(feedUrlStr);
      IFeed feed = new Feed(feedUrl);

      try {
        InputStream inS = Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, null);
        assertNotNull(id, inS);

        assertNull(id, feed.getFormat());
        try {
          Owl.getInterpreter().interpret(inS, feed, null);
        } catch (EncodingException e) {
          inS = Owl.getConnectionService().getHandler(feed.getLink()).openStream(feed.getLink(), null, null);
          Owl.getInterpreter().interpret(inS, feed, Collections.singletonMap((Object) DefaultProtocolHandler.USE_PLATFORM_ENCODING, (Object) Boolean.TRUE));
        }
        assertNotNull(id, feed.getFormat());
      } catch (Exception e) {
        e.printStackTrace();
        fail(feedUrlStr);
      }
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFeedSearch_SingleLanguage() throws Exception {
    String link = Controller.getDefault().toFeedSearchLink("blog");

    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, 60000);
    properties.put(IConnectionPropertyConstants.ACCEPT_LANGUAGE, "de");

    InputStream inS = Owl.getConnectionService().getHandler(new URI(link)).openStream(new URI(link), null, properties);
    String content = StringUtils.readString(new BufferedReader(new InputStreamReader(inS)));
    assertNotNull(content);

    List<String> links = RegExUtils.extractLinksFromText(content, false);
    assertTrue(!links.isEmpty());
    assertTrue(links.size() > 40);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFeedSearch_DoubleLanguage() throws Exception {
    String link = Controller.getDefault().toFeedSearchLink("blog");

    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, 60000);
    properties.put(IConnectionPropertyConstants.ACCEPT_LANGUAGE, "en,de");

    InputStream inS = Owl.getConnectionService().getHandler(new URI(link)).openStream(new URI(link), null, properties);
    String content = StringUtils.readString(new BufferedReader(new InputStreamReader(inS)));
    assertNotNull(content);

    List<String> links = RegExUtils.extractLinksFromText(content, false);
    assertTrue(!links.isEmpty());
    assertTrue(links.size() > 40);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testFeedSearch_WrongLanguage() throws Exception {
    String link = Controller.getDefault().toFeedSearchLink("blog");

    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, 60000);
    properties.put(IConnectionPropertyConstants.ACCEPT_LANGUAGE, "en-us,de_de");

    InputStream inS = Owl.getConnectionService().getHandler(new URI(link)).openStream(new URI(link), null, properties);
    String content = StringUtils.readString(new BufferedReader(new InputStreamReader(inS)));
    assertNotNull(content);

    List<String> links = RegExUtils.extractLinksFromText(content, false);
    assertTrue(!links.isEmpty());
    assertTrue(links.size() > 40);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testWebsite() throws Exception {
    String link = "http://www.rssowl.org";

    Map<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(IConnectionPropertyConstants.CON_TIMEOUT, 60000);

    InputStream inS = Owl.getConnectionService().getHandler(new URI(link)).openStream(new URI(link), null, properties);
    String content = StringUtils.readString(new BufferedReader(new InputStreamReader(inS)));
    assertNotNull(content);

    List<String> links = RegExUtils.extractLinksFromText(content, false);
    assertTrue(!links.isEmpty());
    assertTrue(links.size() > 10);
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testGoogleReaderSync() throws Exception {
    String authToken = SyncUtils.getGoogleAuthToken("rssowl@mailinator.com", "rssowl.org", true, new NullProgressMonitor());
    assertNotNull(authToken);

    assertEquals(authToken, SyncUtils.getGoogleAuthToken("rssowl@mailinator.com", "rssowl.org", false, new NullProgressMonitor()));

    String newAuthToken = SyncUtils.getGoogleAuthToken("rssowl@mailinator.com", "rssowl.org", true, new NullProgressMonitor());
    assertFalse(authToken.equals(newAuthToken));

    authToken = newAuthToken;

    URI uri = URI.create(SyncUtils.GOOGLE_READER_OPML_URI);
    IProtocolHandler handler = Owl.getConnectionService().getHandler(uri);

    Map<Object, Object> properties = new HashMap<Object, Object>();

    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Authorization", SyncUtils.getGoogleAuthorizationHeader(authToken)); //$NON-NLS-1$
    properties.put(IConnectionPropertyConstants.HEADERS, headers);

    InputStream inS = handler.openStream(uri, new NullProgressMonitor(), properties);

    List<? extends IEntity> elements = Owl.getInterpreter().importFrom(inS);
    assertTrue(!elements.isEmpty());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testGetGoogleReaderAPIToken() throws Exception {
    String apiToken = SyncUtils.getGoogleApiToken("rssowl@mailinator.com", "rssowl.org", new NullProgressMonitor());
    assertNotNull(apiToken);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsTransformer() throws Exception {
    String link = "http://www.rssowl.org/node/258";

    List<LinkTransformer> transformers = Controller.getDefault().getLinkTransformers();
    for (LinkTransformer transformer : transformers) {
      String transformedUrl = transformer.toTransformedUrl(link);

      InputStream inS = Owl.getConnectionService().getHandler(new URI(transformedUrl)).openStream(new URI(transformedUrl), null, new HashMap<Object, Object>());
      String content = StringUtils.readString(new BufferedReader(new InputStreamReader(inS)));
      assertNotNull(content);

      List<String> links = RegExUtils.extractLinksFromText(content, false);
      assertTrue(!links.isEmpty());
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsTransformerEmbedded() throws Exception {
    String link = "http://www.rssowl.org/help";
    String transformedUrl = Controller.getDefault().getEmbeddedTransformedUrl(link);

    InputStream inS = Owl.getConnectionService().getHandler(new URI(transformedUrl)).openStream(new URI(transformedUrl), null, new HashMap<Object, Object>());
    String content = StringUtils.readString(new BufferedReader(new InputStreamReader(inS)));
    assertNotNull(content);

    List<String> links = RegExUtils.extractLinksFromText(content, false);
    assertTrue(!links.isEmpty());
  }
}