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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility Class for working with <code>Regular Expressions</code>.
 *
 * @author bpasero
 */
public class RegExUtils {

  /* The reg. expression for an URL */
  private static final String URL_REGEX = "(www([\\wv\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?)|(http|ftp|https|feed):\\/\\/[\\w]+(.[\\w]+)([\\wv\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?"; //$NON-NLS-1$

  /* The compiled pattern to match an URL */
  private static final Pattern URL_REGEX_PATTERN = Pattern.compile(URL_REGEX);

  /* The reg. expression for a strict URL (requires protocol) */
  private static final String STRICT_URL_REGEX = "(http|ftp|https|feed):\\/\\/[\\w]+(.[\\w]+)([\\wv\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?"; //$NON-NLS-1$

  /* The compiled pattern to match a strict URL (requires protocol) */
  private static final Pattern STRICT_URL_REGEX_PATTERN = Pattern.compile(STRICT_URL_REGEX);

  /* This utility class constructor is hidden */
  private RegExUtils() {
  // Protect default constructor
  }

  /**
   * Check if the given URL is valid
   *
   * @param url The URL to check
   * @return boolean TRUE if the link is valid
   */
  public static boolean isValidURL(String url) {
    return URL_REGEX_PATTERN.matcher(url).matches();
  }

  /**
   * Extract all links from the given String and returns it. This method will
   * NOT consider relative links. Only use this method when you are searching
   * for absolute links in a text (which may also be HTML).
   *
   * @param text The String to search for links
   * @param strict If <code>TRUE</code>, require a protocol for any URL in
   * the Text
   * @return A List of Strings matching the criteria for absolute URLs, or an
   * empty List if none.
   */
  public static List<String> extractLinksFromText(String text, boolean strict) {
    List<String> urls = new ArrayList<String>();
    Matcher match = strict ? STRICT_URL_REGEX_PATTERN.matcher(text) : URL_REGEX_PATTERN.matcher(text);

    while (match.find()) {
      String str = match.group(0);
      if (StringUtils.isSet(str) && !urls.contains(str))
        urls.add(str);
    }

    return urls;
  }
}