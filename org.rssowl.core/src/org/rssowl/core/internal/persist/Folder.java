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
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.reference.FolderReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Folders store a number of Marks in an hierachical order. The hierachical
 * order is achieved by allowing to store Folders inside Folders.
 * <p>
 * In case a Blogroll Link is set for the Folder, it is to be interpreted as
 * root-folder of a "Synchronized Blogroll". This special kind of Folder allows
 * to synchronize its contents from a remote OPML file that contains a number of
 * Feeds.
 * </p>
 *
 * @author bpasero
 */
public class Folder extends AbstractEntity implements IFolder {
  private String fName;
  private String fBlogrollLink;
  private IFolder fParent;
  private List<IFolderChild> fChildren;

  /**
   * Creates a new Folder with the given ID and Name as a Child of the given
   * FolderReference. In case the FolderReference is <code>NULL</code>, this
   * Folder is root-leveld.
   *
   * @param id The unique ID of this Folder.
   * @param parent The parent Folder this Folder belongs to, or
   * @param name The Name of this Folder. <code>NULL</code>, if this Folder
   * is a Root-Folder.
   */
  public Folder(Long id, IFolder parent, String name) {
    super(id);
    Assert.isNotNull(name, "The type Folder requires a Name that is not NULL"); //$NON-NLS-1$
    fParent = parent;
    fName = name;
    fChildren = new ArrayList<IFolderChild>(5);
  }

  /**
   * Default constructor for deserialization
   */
  protected Folder() {
  // As per javadoc
  }

  /*
   * @see org.rssowl.core.persist.IFolder#sort()
   */
  public void sort() {
    Collections.sort(fChildren, new Comparator<IFolderChild>() {
      public int compare(IFolderChild child1, IFolderChild child2) {

        /* Sort by Name if classes equal */
        if (child1.getClass().equals(child2.getClass()))
          return child1.getName().toLowerCase().compareTo(child2.getName().toLowerCase());

        /* Sort Marks to Bottom */
        if (child1 instanceof IMark)
          return 1;

        /* Sort Folders to Top */
        return -1;
      }
    });
  }

  /*
   * @see org.rssowl.core.persist.IFolder#addMark(org.rssowl.core.persist.IMark,
   * org.rssowl.core.persist.IFolderChild, boolean)
   */
  public synchronized void addMark(IMark mark, IFolderChild position, Boolean after) {
    Assert.isNotNull(mark, "Exception adding NULL as Mark into Folder"); //$NON-NLS-1$
    Assert.isTrue(equals(mark.getParent()), "The Mark has a different Folder set!"); //$NON-NLS-1$
    addChild(mark, position, after);
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#getMarks()
   */
  public synchronized List<IMark> getMarks() {
    return extractTypes(IMark.class, fChildren);
  }

  /*
   * @see org.rssowl.core.persist.IFolder#addFolder(org.rssowl.core.persist.IFolder,
   * org.rssowl.core.persist.IFolderChild, boolean)
   */
  public synchronized void addFolder(IFolder folder, IFolderChild position, Boolean after) {
    Assert.isNotNull(folder, "Exception adding NULL as Child Folder into Parent Folder"); //$NON-NLS-1$
    Assert.isTrue(equals(folder.getParent()), "The Folder has a different Parent Folder set!"); //$NON-NLS-1$
    addChild(folder, position, after);
  }

  /*
   * @see org.rssowl.core.persist.IFolder#isEmpty()
   */
  public synchronized boolean isEmpty() {
    return fChildren.isEmpty();
  }

  /*
   * @see org.rssowl.core.persist.IFolder#containsChild(org.rssowl.core.persist.IFolderChild)
   */
  public synchronized boolean containsChild(IFolderChild child) {
    return fChildren.contains(child);
  }

  /*
   * @see org.rssowl.core.persist.IFolder#getChildren()
   */
  public synchronized List<IFolderChild> getChildren() {
    return new ArrayList<IFolderChild>(fChildren);
  }

  /*
   * @see org.rssowl.core.persist.IFolder#removeChild(org.rssowl.core.persist.IFolderChild)
   */
  public synchronized boolean removeChild(IFolderChild child) {
    return fChildren.remove(child);
  }

  private void addChild(IFolderChild child, IFolderChild position, Boolean after) {

    /* Mask Null */
    after = (after == null) ? Boolean.FALSE : after;

    /* Add to end of List if Position is unknown */
    if (position == null)
      fChildren.add(child);

    /* Position is provided */
    else {
      int index = fChildren.indexOf(position);

      /* Insert to end of List */
      if (index < 0 || (index == fChildren.size() && after))
        fChildren.add(child);

      /* Insert after Position */
      else if (after)
        fChildren.add(index + 1, child);

      /* Insert before Position */
      else
        fChildren.add(index, child);
    }
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#getFolders()
   */
  public synchronized List<IFolder> getFolders() {
    return extractTypes(IFolder.class, fChildren);
  }

  private <T> List<T> extractTypes(Class<T> type, List<? super T> list) {
    List<T> types = new ArrayList<T>(list.size());
    for (Object object : list) {
      if (type.isInstance(object))
        types.add(type.cast(object));
    }
    return types;
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#setBlogrollLink(java.net.URI)
   */
  public synchronized void setBlogrollLink(URI blogrollLink) {
    fBlogrollLink = getURIText(blogrollLink);
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#getName()
   */
  public synchronized String getName() {
    return fName;
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#setName(java.lang.String)
   */
  public synchronized void setName(String name) {
    Assert.isNotNull(name, "The type Folder requires a Name that is not NULL"); //$NON-NLS-1$
    fName = name;
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#getParent()
   */
  public synchronized IFolder getParent() {
    return fParent;
  }

  /*
   * @see org.rssowl.core.model.types.Reparentable#setParent(java.lang.Object)
   */
  public synchronized void setParent(IFolder newParent) {
    fParent = newParent;
  }

  /*
   * @see org.rssowl.core.model.types.IFolder#getBlogrollLink()
   */
  public synchronized URI getBlogrollLink() {
    return createURI(fBlogrollLink);
  }

  /*
   * @see org.rssowl.core.persist.IFolder#reorderChildren(java.util.List,
   * org.rssowl.core.persist.IFolderChild, boolean)
   */
  public synchronized void reorderChildren(List<? extends IFolderChild> children, IFolderChild position, Boolean after) {
    Assert.isTrue(fChildren.contains(position));
    Assert.isTrue(fChildren.containsAll(children));

    /* Mask Null */
    after = (after == null) ? Boolean.FALSE : after;

    /* First, remove the given Marks */
    fChildren.removeAll(children);

    int index = fChildren.indexOf(position);

    /* Insert to end of List */
    if (index == fChildren.size() && after)
      fChildren.addAll(children);

    /* Insert after Position */
    else if (after)
      fChildren.addAll(index + 1, children);

    /* Insert before Position */
    else
      fChildren.addAll(index, children);
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param folder to be compared.
   * @return whether this object and <code>folder</code> are identical. It
   * compares all the fields.
   */
  public synchronized boolean isIdentical(IFolder folder) {
    if (this == folder)
      return true;

    if (!(folder instanceof Folder))
      return false;

    synchronized (folder) {
      Folder f = (Folder) folder;

      return  (getId() == null ? f.getId() == null : getId().equals(f.getId())) &&
          (fParent == null ? f.fParent == null : fParent.equals(f.fParent)) &&
          (fName == null ? f.fName == null : fName.equals(f.fName)) &&
          (getBlogrollLink() == null ? f.getBlogrollLink() == null : getBlogrollLink().equals(f.getBlogrollLink())) &&
          (fChildren == null ? f.fChildren == null : fChildren.equals(f.fChildren)) &&
          (getProperties() == null ? f.getProperties() == null : getProperties().equals(f.getProperties()));
    }
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  public FolderReference toReference() {
    return new FolderReference(getIdAsPrimitive());
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
    return super.toString() + "Name = " + fName + ", Blogroll Link = " + fBlogrollLink + ", Children = " + fChildren.toString() + ", Parent Folder = " + (fParent != null ? fParent.getId() : "none") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
  }
}