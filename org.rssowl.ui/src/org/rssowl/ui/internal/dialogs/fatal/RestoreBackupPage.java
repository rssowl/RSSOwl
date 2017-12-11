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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.util.Pair;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.List;

/**
 * Part of the {@link FatalErrorWizard} to restore from a backup.
 *
 * @author bpasero
 */
public class RestoreBackupPage extends WizardPage {
  private List<File> fBackups;
  private ComboViewer fBackupsViewer;
  private final DateFormat fDateFormat = OwlUI.getShortDateFormat();
  private Button fConfirmRestoreCheck;
  private final IStatus fErrorStatus;

  RestoreBackupPage(String pageName, IStatus errorStatus, List<File> backups) {
    super(pageName, pageName, null);
    fErrorStatus = errorStatus;
    fBackups = backups;
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {

    /* Title Image and Message */
    setImageDescriptor(OwlUI.getImageDescriptor("icons/wizban/welcome_wiz.gif")); //$NON-NLS-1$
    if (!fErrorStatus.isOK())
      setMessage(Messages.RestoreBackupPage_RSSOWL_CRASH, IMessageProvider.WARNING);
    else
      setMessage(Messages.RestoreBackupPage_RESTORE_TEXT_OK, IMessageProvider.INFORMATION);

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(2, 5, 5));
    ((GridLayout) container.getLayout()).marginBottom = 10;

    /* Restore Information */
    {
      Label backupInfoLabel = new Label(container, SWT.NONE);
      backupInfoLabel.setText(Messages.RestoreBackupPage_RESTORING_A_BACKUP);
      backupInfoLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      backupInfoLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

      Label backupTextLabel = new Label(container, SWT.WRAP);
      backupTextLabel.setText(Application.IS_WINDOWS && !fErrorStatus.isOK() ? Messages.RestoreBackupPage_BACKUP_INFO_RESTART : Messages.RestoreBackupPage_BACKUP_INFO_QUIT);
      backupTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
      ((GridData) backupTextLabel.getLayoutData()).widthHint = 200;
    }

    /* Current Profile Info */
    Pair<File, Long> pair = InternalOwl.getDefault().getProfile();
    File profile = pair.getFirst();
    if (profile != null && profile.exists()) {
      Label currentProfileLabel = new Label(container, SWT.NONE);
      currentProfileLabel.setText(Messages.RestoreBackupPage_CURRENT_PROFILE);
      currentProfileLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
      ((GridData) currentProfileLabel.getLayoutData()).verticalIndent = 5;

      Long timestamp = (pair.getSecond() != null) ? pair.getSecond() : profile.lastModified();

      Label currentProfileTextLabel = new Label(container, SWT.NONE);
      currentProfileTextLabel.setText(NLS.bind(Messages.RestoreBackupPage_LAST_MODIFIED, fDateFormat.format(timestamp), OwlUI.getSize((int) profile.length())));
      currentProfileTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) currentProfileTextLabel.getLayoutData()).verticalIndent = 5;
    }

    /* Restore Controls */
    {
      Label chooseBackupLabel = new Label(container, SWT.NONE);
      chooseBackupLabel.setText(Messages.RestoreBackupPage_CHOOSE_BACKUP);
      chooseBackupLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

      fBackupsViewer = new ComboViewer(container, SWT.BORDER | SWT.READ_ONLY);
      fBackupsViewer.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
      fBackupsViewer.getCombo().setVisibleItemCount(fBackups.size());
      fBackupsViewer.setContentProvider(new ArrayContentProvider());
      fBackupsViewer.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          File file = (File) element;
          return NLS.bind(Messages.RestoreBackupPage_BACKUP_LABEL, fDateFormat.format(file.lastModified()), OwlUI.getSize((int) file.length()));
        }
      });

      fBackupsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
          getContainer().updateButtons();
          fConfirmRestoreCheck.setEnabled(!event.getSelection().isEmpty());
        }
      });

      fBackupsViewer.setInput(fBackups);
    }

    /* Restore Advise */
    {
      Composite adviseContainer = new Composite(container, SWT.None);
      adviseContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
      adviseContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
      ((GridData) adviseContainer.getLayoutData()).verticalIndent = 5;

      Label adviseLabel = new Label(adviseContainer, SWT.NONE);
      adviseLabel.setText(Messages.RestoreBackupPage_CAUTION);
      adviseLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      adviseLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

      Label adviseTextLabel = new Label(adviseContainer, SWT.WRAP);
      adviseTextLabel.setText(Messages.RestoreBackupPage_RESTORE_WARNING);
      adviseTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) adviseTextLabel.getLayoutData()).widthHint = 200;

      fConfirmRestoreCheck = new Button(adviseContainer, SWT.CHECK);
      fConfirmRestoreCheck.setText(Messages.RestoreBackupPage_CONFIRM_RESTORE);
      fConfirmRestoreCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) fConfirmRestoreCheck.getLayoutData()).verticalIndent = 5;
      ((GridData) fConfirmRestoreCheck.getLayoutData()).horizontalIndent = 5;
      fConfirmRestoreCheck.setEnabled(false);
      fConfirmRestoreCheck.addSelectionListener(new SelectionAdapter() {
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
    return fBackupsViewer.getSelection().isEmpty() || fConfirmRestoreCheck.getSelection();
  }

  File getSelectedBackup() {
    IStructuredSelection selection = (IStructuredSelection) fBackupsViewer.getSelection();
    if (!selection.isEmpty())
      return (File) selection.getFirstElement();

    return null;
  }
}