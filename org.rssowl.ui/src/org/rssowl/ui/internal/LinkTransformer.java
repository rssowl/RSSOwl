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

import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

/**
 * Instances of {@link LinkTransformer} are used to allow for transforming a
 * link, e.g. to provide a better reading experience. They can be contributed
 * using the <cod>LinkTransformer</code> extension point.
 *
 * @author bpasero
 */
public class LinkTransformer {
  private static final String URL_INPUT_TOKEN = "[L]"; //$NON-NLS-1$

  private final String fId;
  private final String fName;
  private final String fUrl;

  /**
   * @param id the unique id of the contributed transformer.
   * @param name the name of the transformer.
   * @param url the templated URL for transformation.
   */
  public LinkTransformer(String id, String name, String url) {
    fId = id;
    fName = name;
    fUrl = url;
  }

  /**
   * @return the unique id of the contributed transformer.
   */
  public String getId() {
    return fId;
  }

  /**
   * @return the name of the transformer.
   */
  public String getName() {
    return fName;
  }

  /**
   * @param news the news to transform.
   * @return a link that will be transformed.
   */
  public String toTransformedUrl(INews news) {
    String link = CoreUtils.getLink(news);

    return toTransformedUrl(link);
  }

  /**
   * @param link the link to transform.
   * @return a link that will be transformed.
   */
  public String toTransformedUrl(String link) {
    if (!StringUtils.isSet(link))
      link = ""; //$NON-NLS-1$

    link = URIUtils.urlEncode(link);

    String transformedUrl = fUrl;

    int linkIndex = fUrl.indexOf(URL_INPUT_TOKEN);
    if (linkIndex >= 0)
      transformedUrl = StringUtils.replaceAll(transformedUrl, URL_INPUT_TOKEN, link);

    return transformedUrl;
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

    LinkTransformer other = (LinkTransformer) obj;
    if (fId == null) {
      if (other.fId != null)
        return false;
    } else if (!fId.equals(other.fId))
      return false;

    return true;
  }
}