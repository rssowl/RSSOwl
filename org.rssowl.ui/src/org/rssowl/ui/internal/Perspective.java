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

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

/**
 * @author bpasero
 */
public class Perspective implements IPerspectiveFactory {
  private static final float WIDE_SCREEN_RATIO = 0.2f;
  private static final float NORMAL_SCREEN_RATIO = 0.25f;

  /*
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
   */
  public void createInitialLayout(IPageLayout layout) {

    /* Show View */
    layout.addShowViewShortcut(BookMarkExplorer.VIEW_ID);

    /* Bookmark Explorer */
    layout.addView(BookMarkExplorer.VIEW_ID, IPageLayout.LEFT, getRatio(), layout.getEditorArea());
    layout.getViewLayout(BookMarkExplorer.VIEW_ID).setCloseable(false);

    /* Add RSSOwl Action Sets (Eclipe Integration) */
    layout.addActionSet("org.rssowl.ui.EditSet"); //$NON-NLS-1$
    layout.addActionSet("org.rssowl.ui.GoSet"); //$NON-NLS-1$
    layout.addActionSet("org.rssowl.ui.NewsSet"); //$NON-NLS-1$

    /* Add RSSOwl Wizards (Eclipe Integration) */
    layout.addNewWizardShortcut("org.rssowl.ui.BookmarkWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.rssowl.ui.SearchMarkWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.rssowl.ui.NewsBinWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.rssowl.ui.FolderWizard"); //$NON-NLS-1$
  }

  private float getRatio() {
    Point size = OwlUI.getFirstMonitorSize();
    if (size != null && size.y != 0) {
      float screenRatio = (float) size.x / (float) size.y;
      return screenRatio > 1.5 ? WIDE_SCREEN_RATIO : NORMAL_SCREEN_RATIO;
    }

    return NORMAL_SCREEN_RATIO;
  }
}