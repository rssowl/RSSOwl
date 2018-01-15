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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;

/**
 * @author bpasero
 */
public class FeedViewInput implements IEditorInput {

  /* Used to restore Editor if required */
  private static final String FACTORY_ID = "org.rssowl.ui.FeedViewFactory"; //$NON-NLS-1$
  private static final String RESTORE_QUALIFIER = "/instance/org.eclipse.ui.workbench/"; //$NON-NLS-1$
  private static final String RESTORE_KEY = "USE_IPERSISTABLE_EDITORS"; //$NON-NLS-1$
  static final String MARK_INPUT_CLASS = "org.rssowl.ui.internal.editors.feed.MarkInputClass"; //$NON-NLS-1$
  static final String MARK_INPUT_ID = "org.rssowl.ui.internal.editors.feed.MarkInputId"; //$NON-NLS-1$

  private final INewsMark fMark;
  private boolean fIsDeleted;
  private final PerformAfterInputSet fPerformOnInputSet;

  /**
   * @param mark
   */
  public FeedViewInput(INewsMark mark) {
    this(mark, null);
  }

  /**
   * @param mark
   * @param performOnInputSet
   */
  public FeedViewInput(INewsMark mark, PerformAfterInputSet performOnInputSet) {
    Assert.isNotNull(mark);
    fMark = mark;
    fPerformOnInputSet = performOnInputSet;
  }

  /*
   * @see org.eclipse.ui.IEditorInput#exists()
   */
  @Override
  public boolean exists() {
    return !fIsDeleted;
  }

  /** Marks this Input as Deleted (exists = false) */
  public void setDeleted() {
    fIsDeleted = true;
  }

  /**
   * @return Returns the action that is to be done automatically once the input
   * has been set.
   */
  public PerformAfterInputSet getPerformOnInputSet() {
    return fPerformOnInputSet;
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
   */
  @Override
  public ImageDescriptor getImageDescriptor() {

    /* Book Mark */
    if (fMark instanceof IBookMark) {
      IBookMark bookmark = (IBookMark) fMark;
      ImageDescriptor favicon = OwlUI.getFavicon(bookmark);
      if (favicon != null)
        return favicon;

      return OwlUI.BOOKMARK;
    }

    /* Search Mark */
    else if (fMark instanceof ISearchMark)
      return OwlUI.SEARCHMARK;

    /* News Bin */
    else if (fMark instanceof INewsBin) {
      if (fMark.getProperty(DefaultPreferences.ARCHIVE_BIN_MARKER) != null)
        return OwlUI.ARCHIVE;

      return OwlUI.NEWSBIN;
    }

    /* Folder */
    else if (fMark instanceof FolderNewsMark)
      return OwlUI.FOLDER;

    return OwlUI.UNKNOWN;
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getName()
   */
  @Override
  public String getName() {
    return fMark.getName();
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getPersistable()
   */
  @Override
  public IPersistableElement getPersistable() {
    boolean restore = Platform.getPreferencesService().getBoolean(RESTORE_QUALIFIER, RESTORE_KEY, true, null);
    if (!restore)
      return null;

    return new IPersistableElement() {
      @Override
      public String getFactoryId() {
        return FACTORY_ID;
      }

      @Override
      public void saveState(IMemento memento) {
        memento.putString(MARK_INPUT_CLASS, fMark.getClass().getName());
        memento.putString(MARK_INPUT_ID, String.valueOf(fMark.getId()));
      }
    };
  }

  /*
   * @see org.eclipse.ui.IEditorInput#getToolTipText()
   */
  @Override
  public String getToolTipText() {
    return fMark.getName();
  }

  /*
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

  /**
   * @return Returns the mark.
   */
  public INewsMark getMark() {
    return fMark;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + fMark.hashCode();
    return result;
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

    FeedViewInput type = (FeedViewInput) obj;
    return fMark.equals(type.fMark);
  }
}