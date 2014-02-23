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

package org.rssowl.ui.internal.dialogs.cleanup.tasks;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.cleanup.PostProcessingWork;
import org.rssowl.ui.internal.dialogs.cleanup.pages.SummaryModelTaskGroup;

/**
 * An instance of <code>CleanUpTask</code> to delete a {@link IBookMark}.
 *
 * @author bpasero
 */
public class BookMarkDeleteTask extends AbstractCleanUpTask {
  private String fLabel;
  private ImageDescriptor fImage;
  private final IBookMark fMark;

  public BookMarkDeleteTask(SummaryModelTaskGroup group, IBookMark bookmark) {
    super(group);
    Assert.isNotNull(bookmark);
    fMark = bookmark;
    init();
  }

  private void init() {
    /* Label */
    fLabel = NLS.bind(Messages.TASK_LABEL_DELETE_BOOKMARK, fMark.getName());

    /* Image */
    fImage = OwlUI.getFavicon(fMark);
    if (fImage == null)
      fImage = OwlUI.BOOKMARK;
  }

  @Override
  public void perform(PostProcessingWork work) {
    work.deleteBokmark(fMark);
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getImage()
   */
  @Override
  public ImageDescriptor getImage() {
    return fImage;
  }

  /*
   * @see org.rssowl.ui.internal.dialogs.cleanup.CleanUpTask#getLabel()
   */
  @Override
  public String getLabel() {
    return fLabel;
  }

  /**
   * @return Returns the Bookmark that is to be deleted.
   */
  public IBookMark getBookmarkMark() {
    return fMark;
  }

  @Override
  public int getActualWorkUnits() {
    return 0;
  }
}