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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IActionFilter;
import org.rssowl.core.persist.IEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of <code>EntityGroup</code> are the parent of a List of
 * <code>EntityGroupItem</code>s. They can be used in Viewer to categorize
 * Model-References. Thereby the Group offers a human readable Name to Display
 * and an optional Image.
 *
 * @author bpasero
 */
public class EntityGroup implements IActionFilter {
  private final long fId;
  private final String fCategoryId;
  private final String fName;
  private ImageDescriptor fImage;
  List<EntityGroupItem> fItems;
  private final Integer fSortKey;
  private final RGB fColorHint;
  private int fSizeHint;

  /**
   * Creates a new EntityGroup with the given ID. Note that inside one Viewer
   * there should <em>not</em> be two different ViewerGroups with the same ID.
   *
   * @param id The unique ID of the EntityGroup.
   * @param groupId A unique Identifier describing the Category of this Group.
   * The Category is used to decide wether contributed Actions should enable.
   */
  public EntityGroup(long id, String groupId) {
    this(id, groupId, null);
  }

  /**
   * Creates a new EntityGroup with the given ID and Name. Note that inside one
   * Viewer there should <em>not</em> be two different ViewerGroups with the
   * same ID.
   *
   * @param id The unique ID of the EntityGroup.
   * @param groupId A unique Identifier describing the Category of this Group.
   * The Category is used to decide wether contributed Actions should enable.
   * @param name The Name of this EntityGroup.
   */
  public EntityGroup(long id, String groupId, String name) {
    this(id, groupId, name, null, null);
  }

  /**
   * Creates a new EntityGroup with the given ID and Name. Note that inside one
   * Viewer there should <em>not</em> be two different ViewerGroups with the
   * same ID.
   *
   * @param id The unique ID of the EntityGroup.
   * @param groupId A unique Identifier describing the Category of this Group.
   * The Category is used to decide wether contributed Actions should enable.
   * @param sortKey a key for sorting or <code>null</code> if none.
   * @param name The Name of this EntityGroup.
   * @param colorHint a hint for the color that should be used for this group or
   * <code>null</code> if none.
   */
  public EntityGroup(long id, String groupId, String name, Integer sortKey, RGB colorHint) {
    Assert.isNotNull(groupId);
    fId = id;
    fCategoryId = groupId;
    fName = name;
    fItems = new ArrayList<EntityGroupItem>();
    fSortKey = sortKey;
    fColorHint = colorHint;
  }

  /**
   * @param item
   */
  void add(EntityGroupItem item) {
    fItems.add(item);
  }

  /**
   * @return Returns the Name of this EntityGroup.
   */
  public String getName() {
    return fName != null ? fName : NLS.bind(Messages.EntityGroup_GROUP, fId);
  }

  /**
   * @return Returns the ID of this Viewergroup.
   */
  public long getId() {
    return fId;
  }

  /**
   * @return Returns the ID of the Category of this group.
   */
  public String getCategory() {
    return fCategoryId;
  }

  /**
   * @return Returns the List of <code>EntityGroupItem</code>s contained in this
   * Group.
   */
  public List<EntityGroupItem> getItems() {
    return fItems;
  }

  /**
   * @return Returns a List of <code>IEntity</code> collected from the
   * EntityGroupItems of this Group.
   */
  public List<IEntity> getEntities() {
    List<IEntity> entities = new ArrayList<IEntity>();
    for (EntityGroupItem groupItem : fItems)
      entities.add(groupItem.getEntity());

    return entities;
  }

  /**
   * @param image The image to display this group in the UI.
   */
  public void setImage(ImageDescriptor image) {
    fImage = image;
  }

  /**
   * @return the image to display this group in the UI.
   */
  public ImageDescriptor getImage() {
    return fImage;
  }

  /**
   * @return Returns the Number of <code>EntityGroupItem</code>s contained in
   * this Group.
   */
  public int size() {
    return fItems.size();
  }

  /**
   * @return the sort key or <code>null</code> if none.
   */
  public Integer getSortKey() {
    return fSortKey;
  }

  /**
   * @return a hint for the color that should be used for this group or
   * <code>null</code> if none.
   */
  public RGB getColorHint() {
    return fColorHint;
  }

  /**
   * @param sizeHint a hint on the number of elements this group shows, which
   * can be different from the items inside if a filter is enabled by the user.
   */
  public void setSizeHint(int sizeHint) {
    fSizeHint = sizeHint;
  }

  /**
   * @return a hint on the number of elements this group shows, which can be
   * different from the items inside if a filter is enabled by the user.
   */
  public int getSizeHint() {
    return fSizeHint;
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

    EntityGroup group = (EntityGroup) obj;
    return fId == group.fId;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + (int) (fId ^ (fId >>> 32));
    return result;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

  /*
   * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object,
   * java.lang.String, java.lang.String)
   */
  @Override
  public boolean testAttribute(Object target, String name, String value) {
    return (this == target) && fCategoryId.equals(value);
  }
}