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

package org.rssowl.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

/**
 * Supports "Find" in the current active bookmark explorer or feed view.
 *
 * @author bpasero
 */
public class FindAction extends Action {

  /** Find Action! */
  public FindAction() {
    setId("org.rssowl.ui.FindAction"); //$NON-NLS-1$
    setActionDefinitionId("org.rssowl.ui.FindAction"); //$NON-NLS-1$
    setImageDescriptor(OwlUI.getImageDescriptor("icons/etool16/find.gif")); //$NON-NLS-1$
    setText(Messages.FindAction_FIND);
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    IWorkbenchWindow window = OwlUI.getWindow();
    if (window != null) {
      IWorkbenchPage activePage = window.getActivePage();
      if (activePage != null) {
        IWorkbenchPart activePart = activePage.getActivePart();
        if (activePart != null) {

          /* Find in Bookmark Explorer */
          if (activePart instanceof BookMarkExplorer) {
            ((BookMarkExplorer) activePart).find();
          }

          /* Find in Feed View */
          else if (activePart instanceof FeedView) {
            ((FeedView) activePart).find();
          }
        }
      }
    }

    super.run();
  }
}