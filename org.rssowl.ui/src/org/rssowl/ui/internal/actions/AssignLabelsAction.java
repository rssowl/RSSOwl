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
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.persist.INews;
import org.rssowl.ui.internal.dialogs.AssignLabelsDialog;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class AssignLabelsAction extends Action {
  private IStructuredSelection fSelection;
  private final Shell fShell;

  /**
   * @param shell
   * @param selection
   */
  public AssignLabelsAction(Shell shell, IStructuredSelection selection) {
    fShell = shell;
    fSelection = selection;
  }

  /*
   * @see org.eclipse.jface.action.Action#getActionDefinitionId()
   */
  @Override
  public String getActionDefinitionId() {
    return "org.rssowl.ui.AssignLabels"; //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    return Messages.AssignLabelsAction_ASSIGN_LABELS;
  }

  /*
   * @see org.eclipse.jface.action.Action#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    return !fSelection.isEmpty();
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    List<INews> entities = ModelUtils.getEntities(fSelection, INews.class);
    Set<INews> news = new HashSet<INews>(entities);
    if (!news.isEmpty())
      new AssignLabelsDialog(fShell, news).open();
  }
}