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

package org.rssowl.core.util;

import org.apache.commons.httpclient.URIException;
import org.rssowl.core.internal.Activator;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Utility Class for working with Links.
 *
 * @author bpasero
 */
public class URIUtils {

  /** URL of Blank Website */
  public static final String ABOUT_BLANK = "about:blank"; //$NON-NLS-1$

  /* Default Encoding */
  private static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

  /** Common Newsfeed Extensions */
  private static final String[] FEED_EXTENSIONS = new String[] { "rss", "rdf", "xml", "atom", "feed" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

  /* Used when encoding a URL in a fast way */
  private static final String[] CHARS_TO_ENCODE = new String[] { " ", "[", "]", "{", "}", "|", "^", "\\", "<", ">" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
  private static final String[] ENCODED_CHARS = new String[] { "%20", "%5B", "%5D", "%7B", "%7D", "%7C", "%5E", "%5C", "%3C", "%3E" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

  /** The HTTP Protocol */
  public static final String HTTP = "http://"; //$NON-NLS-1$

  /** The HTTPS Protocol */
  public static final String HTTPS = "https://"; //$NON-NLS-1$

  /** The FEED Protocol */
  public static final String FEED = "feed://"; //$NON-NLS-1$

  /** The FEED Identifier */
  public static final String FEED_IDENTIFIER = "feed:"; //$NON-NLS-1$

  /** The NEWS Identifier */
  public static final String NEWS_IDENTIFIER = "news:"; //$NON-NLS-1$

  /** The NNTP Identifier */
  public static final String NNTP_IDENTIFIER = "nntp:"; //$NON-NLS-1$

  /** Identifier for a Protocol */
  public static final String PROTOCOL_IDENTIFIER = "://"; //$NON-NLS-1$

  /** Some URI Schemes */
  public static final String HTTP_SCHEME = "http"; //$NON-NLS-1$
  public static final String HTTPS_SCHEME = "https"; //$NON-NLS-1$
  public static final String FEED_SCHEME = "feed"; //$NON-NLS-1$
  public static final String FILE_SCHEME = "file"; //$NON-NLS-1$

  /** The JavaScript Identifier */
  public static final String JS_IDENTIFIER = "javascript:"; //$NON-NLS-1$

  /** Identifies a managed Link to be treated specially */
  private static final String MANAGED_LINK_SEPARATOR = "#"; //$NON-NLS-1$
  private static final String MANAGED_LINK_ANCHOR = "rssowlmlink"; //$NON-NLS-1$
  public static final String MANAGED_LINK_IDENTIFIER = MANAGED_LINK_SEPARATOR + MANAGED_LINK_ANCHOR;

  /* This utility class constructor is hidden */
  private URIUtils() {
    // Protect default constructor
  }

  /**
   * Will create a new {@link URI} out of the given one that only contains the
   * Scheme and Host part.
   *
   * @param link The link to normalize.
   * @return the normalized link.
   */
  public static URI normalizeUri(URI link) {
    return normalizeUri(link, false);
  }

  /**
   * Will create a new {@link URI} out of the given one that only contains the
   * Scheme and Host part. If <code>withPort</code> is set to TRUE, the port
   * will be part of the normalized URI too.
   *
   * @param link The link to normalize.
   * @param withPort If set to <code>TRUE</code>, include the port in the
   * normalized URI.
   * @return the normalized link.
   */
  public static URI normalizeUri(URI link, boolean withPort) {
    try {
      if (withPort)
        return new URI(link.getScheme(), null, safeGetHost(link), link.getPort(), null, null, null);
      return new URI(link.getScheme(), safeGetHost(link), null, null);
    } catch (URISyntaxException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

    return link;
  }

  /**
   * @param base the base {@link URI} to resolve against.
   * @param relative the relative {@link URI} to resolve.
   * @return a resolved {@link URI} that is absolute.
   * @throws URISyntaxException in case of an error while resolving.
   */
  public static URI resolve(URI base, URI relative) throws URISyntaxException {
    if (relative.isAbsolute())
      return relative;

    /* Resolve against Host */
    if (relative.toString().startsWith("/")) { //$NON-NLS-1$
      base = normalizeUri(base, true);
      return base.resolve(relative);
    }

    /* Resolve against Given Base */
    if (base.toString().endsWith("/")) //$NON-NLS-1$
      return base.resolve(relative);

    /* Resolve against Given Base By Appending Leading Slash */
    return new URI(base.toString() + "/").resolve(relative.toString()); //$NON-NLS-1$
  }

  /**
   * Return TRUE in case the given String looks like a Link to a Feed.
   *
   * @param str The String to check
   * @return TRUE in case the String looks like a Link to a Feed.
   */
  public static boolean looksLikeFeedLink(String str) {
    return looksLikeFeedLink(str, true);
  }

  /**
   * Return TRUE in case the given String looks like a Link to a Feed.
   *
   * @param str The String to check
   * @param strict if <code>true</code> require the given String to contain one
   * of the feed extensions with a leading ".", <code>false</code> otherwise.
   * @return TRUE in case the String looks like a Link to a Feed.
   */
  public static boolean looksLikeFeedLink(String str, boolean strict) {
    if (!looksLikeLink(str))
      return false;

    if (str.startsWith(FEED))
      return true;

    for (String extension : FEED_EXTENSIONS) {
      if (strict && str.contains("." + extension)) //$NON-NLS-1$
        return true;
      else if (!strict && str.contains(extension))
        return true;
    }

    return false;
  }

  /**
   * Return TRUE in case the given String looks like a Link.
   *
   * @param str The String to check
   * @return TRUE in case the String looks like a Link.
   */
  public static boolean looksLikeLink(String str) {
    return looksLikeLink(str, true);
  }

  /**
   * Return TRUE in case the given String looks like a Link.
   *
   * @param str The String to check
   * @param allowNewsGroup <code>true</code> to allow links of the form
   * "news://" and <code>false</code> otherwise.
   * @return TRUE in case the String looks like a Link.
   */
  public static boolean looksLikeLink(String str, boolean allowNewsGroup) {

    /* Is empty or null? */
    if (!StringUtils.isSet(str))
      return false;

    /* Contains whitespaces ? */
    if (str.indexOf(' ') >= 0)
      return false;

    /* Check Protocol for Newsgroup if set */
    if (!allowNewsGroup && (str.startsWith(NEWS_IDENTIFIER) || str.startsWith(NNTP_IDENTIFIER)))
      return false;

    /* RegEx Link check */
    if (RegExUtils.isValidURL(str))
      return true;

    /* Try creating an URL object */
    try {
      new URL(str);
    } catch (MalformedURLException e) {
      return false;
    }

    /* String is an URL */
    return true;
  }

  /**
   * URLEncode the given String. Note that URLEncoder uses "+" to display any
   * spaces. But we need "%20", so we'll replace all "+" with "%20". This method
   * is used to create a "mailto:" URL that is handled by a mail application.
   * The String is HTML Encoded if the user has set so.
   *
   * @param str String to encode
   * @return String encoded String
   */
  public static String mailToUrllEncode(String str) {
    return urlEncode(str).replaceAll("\\+", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This is a simple wrapper method for the encode() Method of the URLEncoder.
   * UTF-8 is used for encoding.
   *
   * @param str String to encode
   * @return the URL Encoded String
   */
  public static String urlEncode(String str) {

    /* Try Default encoding */
    try {
      return URLEncoder.encode(str, DEFAULT_ENCODING);
    }

    /* Return in this case */
    catch (UnsupportedEncodingException e1) {
      return str;
    }
  }

  /**
   * This is a simple wrapper method for the decode() Method of the URLDecoder.
   * UTF-8 is used for encoding.
   *
   * @param str String to decode
   * @return the URL Decoded String
   */
  public static String urlDecode(String str) {

    /* Try Default encoding */
    try {
      return URLDecoder.decode(str, DEFAULT_ENCODING);
    }

    /* Return in this case */
    catch (UnsupportedEncodingException e1) {
      return str;
    }
  }

  /**
   * Try to create an URI from the given String. The String is preprocessed to
   * work around some bugs in the implementation of Java's equals() for URIs:
   * <p>
   * <li>remove leading and trailing whitespaces</li>
   * <li>encode invalid URI Characters</li>
   * </p>
   *
   * @param str The String to interpret as URI.
   * @return The URI or NULL in case of the String does not match the URI
   * Syntax.
   */
  public static URI createURI(String str) {
    if (str == null)
      return null;

    try {

      /* Remove surrounding whitespaces */
      str = str.trim();

      /* Encode invalid URI Characters */
      str = fastEncode(str);

      return new URI(str);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Returns a new <code>URI</code> from the given one, that potentially points
   * to the favicon.ico.
   *
   * @param link The Link to look for a favicon.
   * @param rewriteHost If <code>TRUE</code>, change the host for a better
   * result.
   * @return Returns the <code>URI</code> from the given one, that potentially
   * points to the favicon.ico.
   * @throws URISyntaxException In case of a malformed URI.
   */
  public static URI toFaviconUrl(URI link, boolean rewriteHost) throws URISyntaxException {
    String host = safeGetHost(link);

    if (!StringUtils.isSet(host))
      return null;

    /* Strip all but the last two segments from the Host */
    if (rewriteHost) {
      String[] hostSegments = host.split("\\."); //$NON-NLS-1$
      int len = hostSegments.length;

      /* Rewrite if conditions match */
      if (len > 2 && !"www".equals(hostSegments[0])) //$NON-NLS-1$
        host = hostSegments[len - 2] + "." + hostSegments[len - 1]; //$NON-NLS-1$

      /* Rewrite failed, avoid reloading by throwing an exception */
      else
        throw new URISyntaxException("", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    StringBuilder buf = new StringBuilder();
    buf.append(HTTP);
    buf.append(host);
    buf.append("/favicon.ico"); //$NON-NLS-1$

    return new URI(fastEncode(buf.toString()));
  }

  /**
   * @param link the absolute link to convert to a top level URI (e.g.
   * http://www.rssowl.org/feed.xml becomes http://www.rssowl.org).
   * @return the top level URL or <code>null</code> if the link is not
   * convertable.
   * @throws URISyntaxException in case of any error converting the link to a
   * top level link.
   */
  public static URI toTopLevel(URI link) throws URISyntaxException {
    if (link == null)
      return null;

    String host = safeGetHost(link);
    if (!StringUtils.isSet(host))
      return null;

    return new URI(HTTP + host);
  }

  /**
   * Try to get the File Name of the given URI.
   *
   * @param uri The URI to parse the File from.
   * @param extension the file extension or <code>null</code> if unknown.
   * @return String The File Name or the URI in external Form.
   */
  public static String getFile(URI uri, String extension) {

    /* Fallback if Extension not set */
    if (!StringUtils.isSet(extension))
      return getFile(uri);

    /* Prefix Extension if necessary */
    if (!extension.startsWith(".")) //$NON-NLS-1$
      extension = "." + extension; //$NON-NLS-1$

    /* Obtain Filename Candidates from Query and Path */
    String fileQuerySegment = getFileSegmentFromQuery(uri.getQuery(), extension);
    String lastPathSegment = getLastSegmentFromPath(uri.getPath());

    /* Favour Query over Path if Extension part of it */
    if (StringUtils.isSet(fileQuerySegment) && fileQuerySegment.contains(extension))
      return urlDecode(fileQuerySegment);

    /* Use Path if Extension part of it */
    if (StringUtils.isSet(lastPathSegment) && lastPathSegment.contains(extension))
      return urlDecode(lastPathSegment);

    /* Favour Path over Query otherwise */
    if (StringUtils.isSet(lastPathSegment))
      return urlDecode(lastPathSegment);

    /* Use Query as Fallback */
    if (StringUtils.isSet(fileQuerySegment))
      return urlDecode(fileQuerySegment);

    return uri.toASCIIString();
  }

  private static String getLastSegmentFromPath(String path) {
    if (StringUtils.isSet(path)) {
      String parts[] = path.split("/"); //$NON-NLS-1$
      if (parts.length > 0 && StringUtils.isSet(parts[parts.length - 1]))
        return parts[parts.length - 1];
    }

    return null;
  }

  private static String getFileSegmentFromQuery(String query, String extension) {
    if (StringUtils.isSet(query)) {
      StringTokenizer tokenizer = new StringTokenizer(query, "&?=/"); //$NON-NLS-1$
      List<String> tokens = new ArrayList<String>();
      while (tokenizer.hasMoreTokens())
        tokens.add(tokenizer.nextToken());

      Collections.reverse(tokens);

      for (String token : tokens) {
        if (token.contains(extension))
          return token;
      }
    }

    return null;
  }

  private static String getFile(URI uri) {
    String file = uri.getPath();
    if (StringUtils.isSet(file)) {
      String parts[] = file.split("/"); //$NON-NLS-1$
      if (parts.length > 0 && StringUtils.isSet(parts[parts.length - 1]))
        return urlDecode(parts[parts.length - 1]);
    }
    return uri.toASCIIString();
  }

  /**
   * @param url the link to encode.
   * @return the encoded link.
   */
  public static String fastEncode(String url) {
    for (int i = 0; i < CHARS_TO_ENCODE.length; i++) {
      if (url.contains(CHARS_TO_ENCODE[i]))
        url = StringUtils.replaceAll(url, CHARS_TO_ENCODE[i], ENCODED_CHARS[i]);
    }

    return url;
  }

  /**
   * @param url the link to decode.
   * @return the decoded link.
   */
  public static String fastDecode(String url) {
    for (int i = 0; i < ENCODED_CHARS.length; i++) {
      if (url.contains(ENCODED_CHARS[i]))
        url = StringUtils.replaceAll(url, ENCODED_CHARS[i], CHARS_TO_ENCODE[i]);
    }

    return url;
  }

  /**
   * @param value the input value (either a link or phrase).
   * @return the value as is if it is a link or a search url for the phrase.
   */
  public static String getLink(String value) {
    if (!StringUtils.isSet(value))
      return value;

    if (value.contains(":") || value.contains("/")) //$NON-NLS-1$ //$NON-NLS-2$
      return value;

    if (value.contains(" ") || !value.contains(".")) { //$NON-NLS-1$ //$NON-NLS-2$
      StringBuilder searchUrl = new StringBuilder();
      searchUrl.append("http://www.google.com/search?q="); //$NON-NLS-1$
      searchUrl.append(urlEncode(value));
      searchUrl.append("&safe=active"); //$NON-NLS-1$

      Locale locale = Locale.getDefault();
      if (locale != null) {
        String language = locale.getLanguage();
        if (StringUtils.isSet(language))
          searchUrl.append("&hl=").append(language); //$NON-NLS-1$
      }

      return searchUrl.toString();
    }

    return value;
  }

  /**
   * @param link the String to ensure that it begins with a protocol.
   * @return the same String if it begins with a protocol, or a String where the
   * http-protocol was appended to the beginning.
   */
  public static String ensureProtocol(String link) {
    if (link != null && !link.contains(PROTOCOL_IDENTIFIER))
      return HTTP + link;

    return link;
  }

  /**
   * @param link the link to convert to a managed link.
   * @return the same link identified as managed link.
   */
  public static String toManaged(String link) {
    if (StringUtils.isSet(link))
      return link + MANAGED_LINK_IDENTIFIER;

    return link;
  }

  /**
   * @param link the link to convert to a unmanaged link.
   * @return the same link without managed identifier.
   */
  public static String toUnManaged(String link) {
    if (isManaged(link)) {

      /* Link Ends With "#rssowlmlink" */
      if (link.endsWith(MANAGED_LINK_IDENTIFIER))
        return link.substring(0, link.length() - MANAGED_LINK_IDENTIFIER.length());

      /*
       * Bug on Windows with IE: Link Ends With "rssowlmlink". This can happen
       * if the original link was already using a hash mark in its URL.
       */
      else if (link.endsWith(MANAGED_LINK_ANCHOR))
        return link.substring(0, link.length() - MANAGED_LINK_ANCHOR.length());
    }

    return link;
  }

  /**
   * @param link the link to check for being managed
   * @return <code>true</code> if the link is managed and <code>false</code>
   * otherwise.
   */
  public static boolean isManaged(String link) {
    return StringUtils.isSet(link) && link.endsWith(MANAGED_LINK_ANCHOR);
  }

  /**
   * The JDK implementation of {@link URI} will return <code>null</code> for
   * urls that contain an underscore. This method will fall back to Apache
   * Commons version of {@link org.apache.commons.httpclient.URI} to get the
   * host information in this case.
   *
   * @param uri the {@link URI} to retrieve the host from.
   * @return the host of the given {@link URI} or <code>null</code> if none.
   */
  public static String safeGetHost(URI uri) {

    /* Try JDK URI */
    String host = uri.getHost();
    if (host != null)
      return host;

    /* Fallback to Apache Commons URI */
    try {
      org.apache.commons.httpclient.URI altUri = new org.apache.commons.httpclient.URI(uri.toString(), false);
      return altUri.getHost();
    } catch (URIException e) {
      /* Ignore */
    }

    return null;
  }

  /**
   * A helper to convert custom schemes (like feed://) to the HTTP counterpart.
   *
   * @param uri the uri to get as HTTP/HTTPS {@link URI}.
   * @return the converted {@link URI} if necessary.
   */
  public static URI toHTTP(URI uri) {
    if (uri == null)
      return uri;

    String scheme = uri.getScheme();
    if (HTTP_SCHEME.equals(scheme) || HTTPS_SCHEME.equals(scheme))
      return uri;

    String newScheme = HTTP_SCHEME;
    if (SyncUtils.READER_HTTPS_SCHEME.equals(scheme))
      newScheme = HTTPS_SCHEME;

    try {
      return new URI(newScheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
    } catch (URISyntaxException e) {
      return uri;
    }
  }

  /**
   * A helper to convert custom schemes (like feed://) to the HTTP counterpart.
   *
   * @param str the uri to get as HTTP/HTTPS {@link URI}.
   * @return the converted {@link String} if necessary.
   */
  public static String toHTTP(String str) {
    if (!StringUtils.isSet(str))
      return str;

    if (str.startsWith(HTTP) || str.startsWith(HTTPS))
      return str;

    try {
      return toHTTP(new URI(str)).toString();
    } catch (URISyntaxException e) {
      return str;
    }
  }
}