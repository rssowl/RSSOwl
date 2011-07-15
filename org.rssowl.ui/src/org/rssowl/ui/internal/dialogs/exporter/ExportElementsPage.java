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

package org.rssowl.ui.internal.dialogs.exporter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.FolderChildCheckboxTree;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.List;

/**
 * A {@link WizardPage} to select which {@link IFolderChild} to include in the
 * export.
 *
 * @author bpasero
 */
public class ExportElementsPage extends WizardPage {
  private FolderChildCheckboxTree fFolderChildTree;
  private Button fSelectAll;
  private Button fDeselectAll;

  /**
   * @param pageName
   */
  protected ExportElementsPage(String pageName) {
    super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/export_wiz.png")); //$NON-NLS-1$
    setMessage(Messages.ExportElementsPage_EXPORT_ELEMENTS);
  }

  /* Return the Checked Elements */
  List<IFolderChild> getElementsToExport() {
    return fFolderChildTree.getCheckedElements();
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* Viewer for Folder Child Selection */
    fFolderChildTree = new FolderChildCheckboxTree(container);
    fFolderChildTree.getViewer().setInput(CoreUtils.loadRootFolders());
    ((GridData) fFolderChildTree.getViewer().getTree().getLayoutData()).heightHint = 190;
    fFolderChildTree.setAllChecked(true);

    /* Select All / Deselect All */
    Composite buttonContainer = new Composite(container, SWT.NONE);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fSelectAll = new Button(buttonContainer, SWT.PUSH);
    fSelectAll.setText(Messages.ExportElementsPage_SELECT_ALL);
    Dialog.applyDialogFont(fSelectAll);
    setButtonLayoutData(fSelectAll);
    fSelectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setAllChecked(true);
      }
    });

    fDeselectAll = new Button(buttonContainer, SWT.PUSH);
    fDeselectAll.setText(Messages.ExportElementsPage_DESELECT_ALL);
    Dialog.applyDialogFont(fDeselectAll);
    setButtonLayoutData(fDeselectAll);
    fDeselectAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fFolderChildTree.setAllChecked(false);
      }
    });

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    fFolderChildTree.getViewer().getTree().setFocus();
  }

  /*
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete() {
    return true;
  }
}