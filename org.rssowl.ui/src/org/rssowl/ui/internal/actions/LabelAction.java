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
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.util.ModelUtils;

import java.util.List;
import java.util.Set;

/**
 * @author bpasero
 */
public class LabelAction extends Action {
  private final ILabel fLabel;
  private IStructuredSelection fSelection;

  /**
   * @param label
   * @param selection
   */
  public LabelAction(ILabel label, IStructuredSelection selection) {
    super("", label != null ? AS_CHECK_BOX : AS_UNSPECIFIED); //$NON-NLS-1$

    fLabel = label;
    fSelection = selection;
  }

  /*
   * @see org.eclipse.jface.action.Action#getActionDefinitionId()
   */
  @Override
  public String getActionDefinitionId() {
    if (fLabel != null)
      return Controller.LABEL_ACTION_PREFIX + fLabel.getOrder();

    return null;
  }

  /*
   * @see org.eclipse.jface.action.Action#getText()
   */
  @Override
  public String getText() {
    if (fLabel == null)
      return Messages.LabelAction_REMOVE_ALL_LABELS;

    IBindingService bs = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
    TriggerSequence binding = bs.getBestActiveBindingFor(Controller.LABEL_ACTION_PREFIX + fLabel.getId());

    return binding != null ? NLS.bind(Messages.LabelAction_LABEL_BINDING, fLabel.getName(), binding.format()) : fLabel.getName();
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
    List<INews> newsList = ModelUtils.getEntities(fSelection, INews.class);
    if (newsList.isEmpty())
      return;

    /* For each News */
    for (INews newsItem : newsList) {
      Set<ILabel> newsLabels = newsItem.getLabels();

      /* Add or Remove particular Label */
      if (fLabel != null) {

        /* Add Label */
        if (!newsLabels.contains(fLabel)) {
          newsItem.addLabel(fLabel);
        }

        /* Remove single Label */
        else
          newsItem.removeLabel(fLabel);
      }

      /* Remove all Labels */
      else {
        for (ILabel newsLabel : newsLabels)
          newsItem.removeLabel(newsLabel);
      }
    }

    /* Mark Saved Search Service as in need for a quick Update */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Save */
    DynamicDAO.saveAll(newsList);
  }
}