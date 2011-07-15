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

package org.rssowl.core.persist.reference;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.service.PersistenceException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A <code>FeedLinkReference</code> is a lightweight representation of IFeed.
 * The actual IFeed can be retrieved by calling the resolve() method.
 */
public final class FeedLinkReference {
  private final String fLinkText;

  /**
   * Creates an instance of this object for a Feed with link <code>link</code>.
   *
   * @param link The link of the Feed that this object references. This cannot
   * be null.
   */
  public FeedLinkReference(URI link) {
    Assert.isNotNull(link, "link"); //$NON-NLS-1$
    fLinkText = link.toString();
  }

  /**
   * @return the link of the feed this object references.
   */
  public final URI getLink() {
    try {
      return new URI(fLinkText);
    } catch (URISyntaxException e) {
      /* Cannot happen */
      throw new IllegalStateException(e);
    }
  }

  /**
   * Convenience method that returns the link of the feed this object references
   * as text.
   *
   * @return text of the referenced feed's link.
   */
  public String getLinkAsText() {
    return fLinkText;
  }

  /**
   * Loads the Feed that this reference points to from the persistence layer and
   * returns it. It may return <code>null</code> if the feed has been deleted
   * from the persistence layer.
   *
   * @return the IFeed this object references.
   * @throws PersistenceException In case an error occurs while accessing the
   * persistence layer.
   */
  public final IFeed resolve() throws PersistenceException {
    return Owl.getPersistenceService().getDAOService().getFeedDAO().load(getLink());
  }

  /**
   * Returns <code>true</code> if calling {@link #resolve()} on this reference
   * will return an entity equal to <code>feed</code>.
   *
   * @param feed The IFeed to compare to.
   * @return <code>true</code> if this object references <code>feed</code> or
   * <code>false</code> otherwise.
   */
  public boolean references(IFeed feed) {
    Assert.isNotNull(feed);

    String entityLinkText = feed.getLink().toString();
    return entityLinkText == null ? false : fLinkText.equals(entityLinkText);
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if ((obj == null) || (obj.getClass() != getClass()))
      return false;

    FeedLinkReference other = (FeedLinkReference) obj;
    return fLinkText.equals(other.fLinkText);
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return fLinkText.hashCode();
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String name = super.toString();
    int index = name.lastIndexOf('.');
    if (index != -1)
      name = name.substring(index + 1, name.length());

    return name + " (Link = " + fLinkText + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }
}