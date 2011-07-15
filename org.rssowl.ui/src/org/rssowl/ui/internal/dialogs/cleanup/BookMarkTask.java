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

package org.rssowl.ui.internal.dialogs.cleanup;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.ui.internal.OwlUI;

/**
 * An instance of <code>CleanUpTask</code> to delete a {@link IBookMark}.
 *
 * @author bpasero
 */
public class BookMarkTask extends CleanUpTask {
  private String fLabel;
  private ImageDescriptor fImage;
  private final IBookMark fMark;

  BookMarkTask(CleanUpGroup group, IBookMark mark) {
    super(group);
    Assert.isNotNull(mark);
    fMark = mark;

    init();
  }

  private void init() {

    /* Label */
    fLabel = NLS.bind(Messages.BookMarkTask_DELETE_N, fMark.getName());

    /* Image */
    fImage = OwlUI.getFavicon(fMark);
    if (fImage == null)
      fImage = OwlUI.BOOKMARK;
  }

  /**
   * @return Returns the Bookmark that is to be deleted.
   */
  public IBookMark getMark() {
    return fMark;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  ImageDescriptor getImage() {
    return fImage;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  String getLabel() {
    return fLabel;
  }
}