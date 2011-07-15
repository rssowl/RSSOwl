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
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.reference.AttachmentReference;
import org.rssowl.core.util.MergeUtils;

import java.net.URI;

/**
 * An attachment to the News. This for example could be a News having a Podcast
 * attached.
 *
 * @author bpasero
 */
public class Attachment extends AbstractEntity implements IAttachment {
  private String fLink;
  private int fLength;
  private String fType;
  private INews fNews;

  private transient URI fLinkURI;

  /**
   * Default constructor provided for deserialization
   */
  protected Attachment() {
  // As per javadoc
  }

  /**
   * Constructor used by <code>DefaultModelFactory</code>
   *
   * @param news The News this attachment is belonging to.
   */
  public Attachment(INews news) {
    super(null);
    Assert.isNotNull(news, "The type Attachment requires a News that is not NULL"); //$NON-NLS-1$
    fNews = news;
  }

  /**
   * Creates a new Element of type Attachment.
   *
   * @param id The unique ID of this Type.
   * @param news The News this attachment is belonging to.
   */
  public Attachment(Long id, INews news) {
    super(id);
    Assert.isNotNull(news, "The type Attachment requires a News that is not NULL"); //$NON-NLS-1$
    fNews = news;
  }

  public Attachment(IAttachment attachment, INews news) {
    synchronized (attachment) {
      setLength(attachment.getLength());
      setType(attachment.getType());
      setLink(attachment.getLink());
    }
    setParent(news);
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#getLength()
   */
  public synchronized int getLength() {
    return fLength;
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#setLength(int)
   */
  public synchronized void setLength(int length) {
    fLength = length;
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#getType()
   */
  public synchronized String getType() {
    return fType;
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#setType(java.lang.String)
   */
  public synchronized void setType(String type) {
    fType = type;
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#getNews()
   */
  public synchronized INews getNews() {
    return fNews;
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#setLink(java.net.URI)
   */
  public synchronized void setLink(URI link) {
    if (link == null) {
      fLinkURI = null;
      fLink = null;
    }
    else {
      fLinkURI = link;
      fLink = link.toString();
    }
  }

  /*
   * @see org.rssowl.core.model.types.IAttachment#getLink()
   */
  public synchronized URI getLink() {
    if (fLinkURI == null && fLink != null)
      fLinkURI = createURI(fLink);

    return fLinkURI;
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param attachment to be compared.
   * @return whether this object and <code>attachment</code> are identical. It
   * compares all the fields.
   */
  public synchronized boolean isIdentical(IAttachment attachment) {
    if (this == attachment)
      return true;

    if (attachment instanceof Attachment == false)
      return false;

    synchronized (attachment) {
      Attachment a = (Attachment) attachment;

      return (getId() == null ? a.getId() == null : getId().equals(a.getId())) &&
              fNews.equals(a.fNews) && (fLink == null ? a.fLink == null : fLink.equals(a.fLink)) &&
              (fType == null ? a.fType == null : fType.equals(a.fType)) && fLength == a.fLength &&
              (getProperties() == null ? a.getProperties() == null : getProperties().equals(a.getProperties()));
    }
  }

  /*
   * @see org.rssowl.core.model.types.MergeCapable#merge(java.lang.Object)
   */
  public synchronized MergeResult merge(IAttachment objectToMerge) {
    Assert.isNotNull(objectToMerge, "objectToMerge"); //$NON-NLS-1$
    synchronized (objectToMerge) {
      boolean updated = false;
      updated = fLength != objectToMerge.getLength();
      fLength = objectToMerge.getLength();
      updated = !MergeUtils.equals(fType, objectToMerge.getType());
      fType = objectToMerge.getType();
      ComplexMergeResult<?> result = MergeUtils.mergeProperties(this, objectToMerge);
      if (updated || result.isStructuralChange())
        result.addUpdatedObject(this);

      return result;
    }
  }

  /*
   * @see org.rssowl.core.model.types.Reparentable#setParent(java.lang.Object)
   */
  public synchronized void setParent(INews newParent) {
    Assert.isNotNull(newParent, "newParent"); //$NON-NLS-1$
    fNews = newParent;
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  public AttachmentReference toReference() {
    return new AttachmentReference(getIdAsPrimitive());
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#toString()
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
    return super.toString() + "Link = " + fLink + ", Type = " + fType + ", Length = " + fLength + ", Belongs to News = " + fNews.getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  }
}