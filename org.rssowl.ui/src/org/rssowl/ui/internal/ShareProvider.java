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

package org.rssowl.ui.internal;

import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

/**
 * Instances of {@link ShareProvider} are used to allow for sharing of news
 * items with services. They can be contributed using the
 * <cod>ShareProvider</code> extension point.
 *
 * @author bpasero
 */
public class ShareProvider {
  private static final String URL_INPUT_TOKEN = "[L]"; //$NON-NLS-1$
  private static final String TITLE_INPUT_TOKEN = "[T]"; //$NON-NLS-1$

  private final String fId;
  private final String fPluginId;
  private final int fIndex;
  private final String fName;
  private final String fIconPath;
  private final String fUrl;
  private final int fMaxTitleLength;
  private boolean fEnabled;

  /**
   * @param id the unique id of the contributed provider.
   * @param pluginId the id of the plugin that contributes this provider.
   * @param index the index of the provider for sorting.
   * @param name the name of the provider.
   * @param iconPath the path to an icon of the provider.
   * @param url the templated URL to share with.
   * @param maxTitleLength a limit for the title.
   * @param enabled <code>true</code> if this provider is enabled and
   * <code>false</code> otherwise.
   */
  public ShareProvider(String id, String pluginId, int index, String name, String iconPath, String url, String maxTitleLength, boolean enabled) {
    fId = id;
    fPluginId = pluginId;
    fIndex = index;
    fName = name;
    fIconPath = iconPath;
    fUrl = url;
    fEnabled = enabled;

    if (maxTitleLength != null)
      fMaxTitleLength = Integer.parseInt(maxTitleLength);
    else
      fMaxTitleLength = Integer.MAX_VALUE;
  }

  /**
   * @return the unique id of the contributed provider.
   */
  public String getId() {
    return fId;
  }

  /**
   * @return the id of the plugin that contributes this provider.
   */
  public String getPluginId() {
    return fPluginId;
  }

  /**
   * @return the index of the provider used for sorting.
   */
  public int getIndex() {
    return fIndex;
  }

  /**
   * @return the name of the provider.
   */
  public String getName() {
    return fName;
  }

  /**
   * @return the path to an icon of the provider.
   */
  public String getIconPath() {
    return fIconPath;
  }

  /**
   * @param enabled <code>true</code> if this provider is enabled and
   * <code>false</code> otherwise.
   */
  public void setEnabled(boolean enabled) {
    fEnabled = enabled;
  }

  /**
   * @return <code>true</code> if this provider is enabled and
   * <code>false</code> otherwise.
   */
  public boolean isEnabled() {
    return fEnabled;
  }

  /**
   * @param news the news to share.
   * @return a link that can be used to share the news with this provider.
   */
  public String toShareUrl(INews news) {
    String link = CoreUtils.getLink(news);
    String title = CoreUtils.getHeadline(news, true);

    return toShareUrl(link, title);
  }

  /**
   * @param mark the bookmark to share.
   * @return a link that can be used to share the bookmark with this provider.
   */
  public String toShareUrl(IBookMark mark) {
    String link = URIUtils.toHTTP(mark.getFeedLinkReference().getLinkAsText());
    String title = mark.getName();

    return toShareUrl(link, title);
  }

  /**
   * @param link the link to share.
   * @param title a title for the link to share.
   * @return a link that can be used to share the link with this provider.
   */
  public String toShareUrl(String link, String title) {
    if (!StringUtils.isSet(link))
      link = ""; //$NON-NLS-1$

    if (!StringUtils.isSet(title))
      title = ""; //$NON-NLS-1$

    if (title.length() > fMaxTitleLength)
      title = StringUtils.smartTrim(title, fMaxTitleLength);

    link = URIUtils.urlEncode(link);
    title = URIUtils.urlEncode(title);

    String shareUrl = fUrl;

    int linkIndex = fUrl.indexOf(URL_INPUT_TOKEN);
    int titleIndex = fUrl.indexOf(TITLE_INPUT_TOKEN);

    if (linkIndex >= 0)
      shareUrl = StringUtils.replaceAll(shareUrl, URL_INPUT_TOKEN, link);

    if (titleIndex >= 0)
      shareUrl = StringUtils.replaceAll(shareUrl, TITLE_INPUT_TOKEN, title);

    return shareUrl;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fId == null) ? 0 : fId.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    ShareProvider other = (ShareProvider) obj;
    if (fId == null) {
      if (other.fId != null)
        return false;
    } else if (!fId.equals(other.fId))
      return false;

    return true;
  }
}