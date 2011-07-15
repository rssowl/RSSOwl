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

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.ui.internal.EntityGroup;

import java.util.Date;

/**
 * @author bpasero
 */
public class BookMarkSorter extends ViewerComparator {

  /* Return this is the sort should be skipped for two elements */
  private static final int SKIP_SORT = 0;

  /** Sort Type */
  public enum Type {

    /** Apply Default Sorting */
    DEFAULT_SORTING,

    /** Sort by Name */
    SORT_BY_NAME,

    /** Sort by Popularity */
    SORT_BY_POPULARITY,

    /** Sort by Last Visit Date */
    SORT_BY_LAST_VISIT_DATE
  }

  /* The current Sorter Type */
  private Type fType = Type.DEFAULT_SORTING;

  /**
   * @param type the type of sorting as described by {@link BookMarkSorter.Type}
   */
  public void setType(BookMarkSorter.Type type) {
    fType = type;
  }

  BookMarkSorter.Type getType() {
    return fType;
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {

    /* Skip Sort for two Folder Childs if default sorting */
    if (fType == Type.DEFAULT_SORTING && e1 instanceof IFolderChild && e2 instanceof IFolderChild)
      return SKIP_SORT;

    /* Compare two Folders */
    else if (e1 instanceof IFolder && e2 instanceof IFolder)
      return compareFolders((IFolder) e1, (IFolder) e2);

    /* Compare two Marks */
    else if (e1 instanceof IMark && e2 instanceof IMark)
      return compareMarks((IMark) e1, (IMark) e2);

    /* Compare two EntityGroups */
    else if (e1 instanceof EntityGroup && e2 instanceof EntityGroup)
      return compareGroups((EntityGroup) e1, (EntityGroup) e2);

    return super.compare(viewer, e1, e2);
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
   */
  @Override
  public int category(Object element) {

    /* Sort Folders to the Top */
    if (element instanceof IFolder)
      return 0;

    return 1;
  }

  private int compareFolders(IFolder folder1, IFolder folder2) {

    /* Sort by Name */
    if (fType == Type.SORT_BY_NAME)
      return folder1.getName().toLowerCase().compareTo(folder2.getName().toLowerCase());

    return SKIP_SORT;
  }

  private int compareMarks(IMark mark1, IMark mark2) {

    /* Sort by Name */
    if (fType == Type.SORT_BY_NAME)
      return mark1.getName().toLowerCase().compareTo(mark2.getName().toLowerCase());

    /* Sort by Last Visit Date (descending) */
    else if (fType == Type.SORT_BY_LAST_VISIT_DATE) {
      Date d1 = mark1.getLastVisitDate();
      Date d2 = mark2.getLastVisitDate();

      if (d1 != null && d2 != null)
        return d2.compareTo(d1);
      else if (d2 != null)
        return -1;
      else if (d1 != null)
        return 1;
    }

    /* Sort by Popularity (descending) */
    else if (fType == Type.SORT_BY_POPULARITY) {
      int p1 = mark1.getPopularity();
      int p2 = mark2.getPopularity();

      if (p1 != p2)
        return p2 > p1 ? -1 : 1;
    }

    return SKIP_SORT;
  }

  private int compareGroups(EntityGroup g1, EntityGroup g2) {
    return g1.getId() < g2.getId() ? -1 : 1;
  }
}