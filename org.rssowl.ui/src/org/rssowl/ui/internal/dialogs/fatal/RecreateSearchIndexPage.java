/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.ui.internal.dialogs.fatal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Part of the {@link FatalErrorWizard} to recreate the search index.
 *
 * @author bpasero
 */
public class RecreateSearchIndexPage extends WizardPage {
  RecreateSearchIndexPage(String pageName) {
    super(pageName, pageName, null);
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {

    /* Title Image and Message */
    setImageDescriptor(OwlUI.getImageDescriptor("icons/wizban/welcome_wiz.gif")); //$NON-NLS-1$
    setMessage(Messages.RecreateSearchIndexPage_RSSOWL_CRASH, IMessageProvider.WARNING);

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(1, 5, 5));
    ((GridLayout) container.getLayout()).marginBottom = 10;

    /* Recreate Information */
    {
      Label recoverInfoLabel = new Label(container, SWT.NONE);
      recoverInfoLabel.setText(Messages.RecreateSearchPage_RECREATING_SEARCH_INDEX);
      recoverInfoLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      recoverInfoLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

      Label recoverInfoTextLabel = new Label(container, SWT.WRAP);
      if (Application.IS_WINDOWS)
        recoverInfoTextLabel.setText(Messages.RecreateSearchPage_RECREATING_DETAILS_RESTART);
      else
        recoverInfoTextLabel.setText(Messages.RecreateSearchPage_RECREATING_DETAILS_QUIT);
      recoverInfoTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) recoverInfoTextLabel.getLayoutData()).widthHint = 200;
    }

    /* Recreate Advise */
    {
      Label adviseLabel = new Label(container, SWT.NONE);
      adviseLabel.setText(Messages.RecreateSearchPage_INFORMATION);
      adviseLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      adviseLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
      ((GridData) adviseLabel.getLayoutData()).verticalIndent = 10;

      Label adviseTextLabel = new Label(container, SWT.WRAP);
      adviseTextLabel.setText(Messages.RecreateSearchPage_RECREATING_INFORMATION);
      adviseTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) adviseTextLabel.getLayoutData()).widthHint = 200;
    }

    Dialog.applyDialogFont(container);

    setControl(container);
  }
}