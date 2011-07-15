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
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.reference.CategoryReference;
import org.rssowl.core.util.MergeUtils;

/**
 * Each Feed and News may be related to one or more Categories.
 *
 * @author bpasero
 */
public class Category extends AbstractEntity implements ICategory {
  private String fName;
  private String fDomain;

  /**
   * Constructor used by <code>DefaultModelFactory</code>
   */
  public Category() {
    super(null);
  }

  /**
   * Creates a new Category with the given ID.
   *
   * @param id The unique ID of this type.
   */
  public Category(Long id) {
    super(id);
  }

  public Category(ICategory category) {
    synchronized (category) {
      setName(category.getName());
      setDomain(category.getDomain());
    }
  }

  /*
   * @see org.rssowl.core.model.types.ICategory#setName(java.lang.String)
   */
  public synchronized void setName(String name) {
    fName = name;
  }

  /*
   * @see org.rssowl.core.model.types.ICategory#setDomain(java.lang.String)
   */
  public synchronized void setDomain(String domain) {
    fDomain = domain;
  }

  /*
   * @see org.rssowl.core.model.types.ICategory#getDomain()
   */
  public synchronized String getDomain() {
    return fDomain;
  }

  /*
   * @see org.rssowl.core.model.types.ICategory#getName()
   */
  public synchronized String getName() {
    return fName;
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param category to be compared.
   * @return whether this object and <code>category</code> are identical. It
   * compares all the fields.
   */
  public synchronized boolean isIdentical(ICategory category) {
    if (category == this)
      return true;

    if (!(category instanceof Category))
      return false;

    synchronized (category) {
      Category c = (Category) category;

      return (getId() == null ? c.getId() == null : getId().equals(c.getId()))
          && (fDomain == null ? c.fDomain == null : fDomain.equals(c.fDomain))
          && fName.equals(c.fName)
          && (getProperties() == null ? c.getProperties() == null :
              getProperties().equals(c.getProperties()));
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#toString()
   */
  @Override
  public synchronized String toString() {
    return super.toString() + "Name = " + fName + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns a String describing the state of this Entity.
   *
   * @return A String describing the state of this Entity.
   */
  public synchronized String toLongString() {
    return super.toString() + "Name = " + fName + ", Domain = " + fDomain + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /*
   * @see org.rssowl.core.persist.MergeCapable#merge(java.lang.Object)
   */
  public synchronized MergeResult merge(ICategory objectToMerge) {
    Assert.isNotNull(objectToMerge, "objectToMerge"); //$NON-NLS-1$
    synchronized (objectToMerge) {
      boolean updated = false;
      updated |= !MergeUtils.equals(fDomain, objectToMerge.getDomain());
      fDomain = objectToMerge.getDomain();
      updated |= !MergeUtils.equals(fName, objectToMerge.getName());
      fName = objectToMerge.getName();
      MergeUtils.mergeProperties(this, objectToMerge);
      ComplexMergeResult<?> result = MergeUtils.mergeProperties(this, objectToMerge);
      if (updated || result.isStructuralChange())
        result.addUpdatedObject(this);

      return result;
    }
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  public CategoryReference toReference() {
    return new CategoryReference(getIdAsPrimitive());
  }
}