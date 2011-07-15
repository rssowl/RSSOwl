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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Part of the {@link FatalErrorWizard} to start with a clean profile again
 * importing just the backup OPML file if present.
 *
 * @author bpasero
 */
public class CleanProfilePage extends WizardPage {
  private final boolean fHasOPMLBackup;
  private Button fConfirmWarningCheck;
  private Button fDoRecoverCheck;
  private final IStatus fErrorStatus;

  CleanProfilePage(String pageName, IStatus errorStatus, boolean hasOPMLBackup) {
    super(pageName, pageName, null);
    fErrorStatus = errorStatus;
    fHasOPMLBackup = hasOPMLBackup;
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {

    /* Title Image and Message */
    setImageDescriptor(OwlUI.getImageDescriptor("icons/wizban/welcome_wiz.gif")); //$NON-NLS-1$
    if (!fErrorStatus.isOK())
      setMessage(Messages.CleanProfilePage_RSSOWL_CRASH, IMessageProvider.WARNING);
    else
      setMessage(fHasOPMLBackup ? Messages.CleanProfilePage_CLEAN_TEXT_OPML_OK : Messages.CleanProfilePage_CLEAN_TEXT_OK, IMessageProvider.INFORMATION);

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(1, 5, 5));
    ((GridLayout) container.getLayout()).marginBottom = 10;

    /* Recover Information */
    {
      Label recoverInfoLabel = new Label(container, SWT.NONE);
      recoverInfoLabel.setText(fHasOPMLBackup ? Messages.CleanProfilePage_RESTORING_SUBSCRIPTIONS_SETTINGS : Messages.CleanProfilePage_STARTING_OVER);
      recoverInfoLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      recoverInfoLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

      Label recoverInfoTextLabel = new Label(container, SWT.WRAP);
      if (fHasOPMLBackup)
        recoverInfoTextLabel.setText(Messages.CleanProfilePage_OPML_BACKUP_INFO);
      else
        recoverInfoTextLabel.setText(Messages.CleanProfilePage_NO_BACKUPS);
      recoverInfoTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) recoverInfoTextLabel.getLayoutData()).widthHint = 200;

      fDoRecoverCheck = new Button(container, SWT.CHECK);
      fDoRecoverCheck.setText(fHasOPMLBackup ? Messages.CleanProfilePage_RESTORE_SUBSCRIPTIONS_SETTINGS : Messages.CleanProfilePage_START_OVER);
      fDoRecoverCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) fDoRecoverCheck.getLayoutData()).verticalIndent = 5;
      ((GridData) fDoRecoverCheck.getLayoutData()).horizontalIndent = 5;
      fDoRecoverCheck.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          getContainer().updateButtons();
          fConfirmWarningCheck.setEnabled(fDoRecoverCheck.getSelection());
        }
      });
    }

    /* Recover Advise */
    {
      Label adviseLabel = new Label(container, SWT.NONE);
      adviseLabel.setText(Messages.CleanProfilePage_CAUTION);
      adviseLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      adviseLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
      ((GridData) adviseLabel.getLayoutData()).verticalIndent = 10;

      Label adviseTextLabel = new Label(container, SWT.WRAP);
      if (fHasOPMLBackup)
        adviseTextLabel.setText(Messages.CleanProfilePage_CAUTION_TEXT_RESTORE);
      else
        adviseTextLabel.setText(Messages.CleanProfilePage_CAUTION_TEXT_START_OVER);
      adviseTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) adviseTextLabel.getLayoutData()).widthHint = 200;

      fConfirmWarningCheck = new Button(container, SWT.CHECK);
      fConfirmWarningCheck.setText(Messages.CleanProfilePage_CONFIRM_TEXT);
      fConfirmWarningCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) fConfirmWarningCheck.getLayoutData()).verticalIndent = 5;
      ((GridData) fConfirmWarningCheck.getLayoutData()).horizontalIndent = 5;
      fConfirmWarningCheck.setEnabled(false);
      fConfirmWarningCheck.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          getContainer().updateButtons();
        }
      });
    }

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  /*
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete() {
    return !fDoRecoverCheck.getSelection() || fConfirmWarningCheck.getSelection();
  }

  boolean doCleanProfile() {
    return fDoRecoverCheck.getSelection();
  }
}