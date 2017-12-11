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

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.util.MergeUtils;

import java.net.URI;

/**
 * Simple Implementation of this Type. Data is kept in fields and all Methods
 * are functional to set/get this Data.
 *
 * @author bpasero
 */
public class Image extends Persistable implements IImage {
  private String fLink;
  private String fTitle;
  private String fHomepage;
  private int fWidth;
  private int fHeight;
  private String fDescription;

  /**
   * Constructor used by <code>DefaultModelFactory</code>
   */
  public Image() {}

  /**
   * Creates an instance of this object with the provided <code>id</code> and
   * <code>feedReference</code>.
   *
   * @param link The Link of the Image to display it.
   */
  public Image(URI link) {
    if (link != null)
      fLink = link.toString();
  }

  /*
   * @see org.rssowl.core.model.types.IImage#setLink(java.net.URI)
   */
  @Override
  public synchronized void setLink(URI link) {
    if (link != null)
      fLink = link.toString();
  }

  /*
   * @see org.rssowl.core.model.types.IImage#setTitle(java.lang.String)
   */
  @Override
  public synchronized void setTitle(String title) {
    fTitle = title;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#setHomepage(java.net.URI)
   */
  @Override
  public synchronized void setHomepage(URI homepage) {
    fHomepage = getURIText(homepage);
  }

  /*
   * @see org.rssowl.core.model.types.IImage#setWidth(int)
   */
  @Override
  public synchronized void setWidth(int width) {
    fWidth = width;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#setHeight(int)
   */
  @Override
  public synchronized void setHeight(int height) {
    fHeight = height;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#setDescription(java.lang.String)
   */
  @Override
  public synchronized void setDescription(String description) {
    fDescription = description;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#getDescription()
   */
  @Override
  public synchronized String getDescription() {
    return fDescription;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#getHeight()
   */
  @Override
  public synchronized int getHeight() {
    return fHeight;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#getHomepage()
   */
  @Override
  public synchronized URI getHomepage() {
    return createURI(fHomepage);
  }

  /*
   * @see org.rssowl.core.model.types.IImage#getTitle()
   */
  @Override
  public synchronized String getTitle() {
    return fTitle;
  }

  /*
   * @see org.rssowl.core.model.types.IImage#getLink()
   */
  @Override
  public synchronized URI getLink() {
    return createURI(fLink);
  }

  /*
   * @see org.rssowl.core.model.types.IImage#getWidth()
   */
  @Override
  public synchronized int getWidth() {
    return fWidth;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public synchronized int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((fDescription == null) ? 0 : fDescription.hashCode());
    result = PRIME * result + fHeight;
    result = PRIME * result + ((fHomepage == null) ? 0 : fHomepage.hashCode());
    result = PRIME * result + ((fTitle == null) ? 0 : fTitle.hashCode());
    result = PRIME * result + ((fLink == null) ? 0 : fLink.hashCode());
    result = PRIME * result + fWidth;
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public synchronized boolean equals(Object image) {
    if (this == image)
      return true;

    if (!(image instanceof Image))
      return false;

    synchronized (image) {
      Image i = (Image) image;

      return (fLink == null ? i.fLink == null : fLink.equals(i.fLink)) &&
          (fDescription == null ? i.fDescription == null : fDescription.equals(i.fDescription)) &&
          fHeight == i.fHeight && fWidth == i.fWidth && (fHomepage == null ? i.fHomepage == null : fHomepage.equals(i.fHomepage)) &&
          (fTitle == null ? i.fTitle == null : fTitle.equals(i.fTitle));
    }
  }

  /*
   * @see org.rssowl.core.model.types.MergeCapable#merge(java.lang.Object)
   */
  @Override
  public synchronized MergeResult merge(IImage objectToMerge) {
    Assert.isNotNull(objectToMerge);
    synchronized (objectToMerge) {
      boolean updated = !simpleFieldsEqual(objectToMerge);
      fHeight = objectToMerge.getHeight();
      setHomepage(objectToMerge.getHomepage());
      fTitle = objectToMerge.getTitle();
      setLink(objectToMerge.getLink());
      fWidth = objectToMerge.getWidth();
      fDescription = objectToMerge.getDescription();
      MergeResult mergeResult = new MergeResult();
      if (updated)
        mergeResult.addUpdatedObject(this);

      return mergeResult;
    }
  }

  private boolean simpleFieldsEqual(IImage image) {
    return fHeight == image.getHeight() &&
        MergeUtils.equals(getHomepage(), image.getHomepage()) &&
        MergeUtils.equals(fTitle, image.getTitle()) &&
        MergeUtils.equals(getLink(), image.getLink()) &&
        MergeUtils.equals(fWidth, image.getWidth()) &&
        MergeUtils.equals(fDescription, image.getDescription());
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public synchronized String toString() {
    return super.toString() + "Link = " + fLink + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns a String describing the state of this Entity.
   *
   * @return A String describing the state of this Entity.
   */
  public synchronized String toLongString() {
    return super.toString() + "Link = " + fLink + ", Title = " + fTitle + ", Homepage = " + fHomepage + ", Width = " + fWidth + ", Height = " + fHeight + ", Description = " + fDescription + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
  }
}