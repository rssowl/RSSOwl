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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.util.BrowserUtils;

import java.util.List;

/**
 * @author bpasero
 */
public class OpenInExternalBrowserAction extends Action {
  private static final String ID = "org.rssowl.ui.OpenInExternalBrowser"; //$NON-NLS-1$
  private IStructuredSelection fSelection;

  /**
   * @param selection
   */
  public OpenInExternalBrowserAction(IStructuredSelection selection) {
    fSelection = selection;

    setText(Messages.OpenInExternalBrowserAction_OPEN_IN_EXTERNAL_BROWSER);
    setId(ID);
    setActionDefinitionId(ID);
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    List<?> selection = fSelection.toList();
    for (Object object : selection) {
      if (object instanceof INews) {
        INews news = (INews) object;
        String link = CoreUtils.getLink(news);
        if (link != null)
          BrowserUtils.openLinkExternal(link);
      }
    }
  }
}