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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.rssowl.core.persist.service.ProfileLockedException;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.CustomWizardDialog;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Part of the {@link FatalErrorWizard} to give information on the fatal error.
 *
 * @author bpasero
 */
public class ErrorInfoPage extends WizardPage {
  private final IStatus fErrorStatus;
  private Menu fCopyMenu;
  private final boolean fHasNextPage;

  ErrorInfoPage(String pageName, IStatus errorStatus, boolean hasNextPage) {
    super(pageName, pageName, null);
    fErrorStatus = errorStatus;
    fHasNextPage = hasNextPage;
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {

    /* Title Image and Message */
    setImageDescriptor(OwlUI.getImageDescriptor("icons/wizban/welcome_wiz.gif")); //$NON-NLS-1$
    setMessage(Messages.ErrorInfoPage_RSSOWL_CRASH, IMessageProvider.WARNING);

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(LayoutUtils.createGridLayout(1, 5, 5));
    if (!fHasNextPage)
      ((GridLayout) container.getLayout()).marginBottom = 5;

    /* Error Details */
    Label errorDetailsLabel = new Label(container, SWT.NONE);
    errorDetailsLabel.setText(Messages.ErrorInfoPage_ERROR_DETAILS);
    errorDetailsLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
    errorDetailsLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    String msg = null;

    /* Out of memory error */
    if (fErrorStatus.getException() instanceof OutOfMemoryError) {
      if (StringUtils.isSet(fErrorStatus.getMessage()))
        msg = NLS.bind(Messages.ErrorInfoPage_OOM_ERROR_N, fErrorStatus.getMessage());
      else
        msg = Messages.ErrorInfoPage_OOM_ERROR;
    }

    /* Profile Locked by another Instance */
    else if (fErrorStatus.getException() instanceof ProfileLockedException) {
      if (StringUtils.isSet(fErrorStatus.getMessage()))
        msg = NLS.bind(Messages.ErrorInfoPage_LOCKED_ERROR_N, fErrorStatus.getMessage());
      else
        msg = Messages.ErrorInfoPage_LOCKED_ERROR;
    }

    /* Any other error */
    else {
      if (StringUtils.isSet(fErrorStatus.getMessage()))
        msg = NLS.bind(Messages.ErrorInfoPage_STARTUP_ERROR_N, fErrorStatus.getMessage());
      else
        msg = Messages.ErrorInfoPage_STARTUP_ERROR;
    }

    final Label errorDetailsTextLabel = new Label(container, SWT.WRAP);
    errorDetailsTextLabel.setText(msg);
    errorDetailsTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    ((GridData) errorDetailsTextLabel.getLayoutData()).widthHint = 200;

    /* Context Menu to copy the error message */
    fCopyMenu = new Menu(errorDetailsTextLabel.getShell(), SWT.POP_UP);
    MenuItem copyItem = new MenuItem(fCopyMenu, SWT.PUSH);
    copyItem.setText(Messages.ErrorInfoPage_COPY);
    copyItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        OwlUI.getClipboard(fCopyMenu.getDisplay()).setContents(new Object[] { errorDetailsTextLabel.getText() }, new Transfer[] { TextTransfer.getInstance() });
      }
    });
    errorDetailsTextLabel.setMenu(fCopyMenu);

    /* Report Crash (not for OutOfMemory and ProfileLockedException  */
    if (!(fErrorStatus.getException() instanceof ProfileLockedException) && !(fErrorStatus.getException() instanceof OutOfMemoryError)) {
      Label crashReportLabel = new Label(container, SWT.NONE);
      crashReportLabel.setText(Messages.ErrorInfoPage_LET_US_KNOW);
      crashReportLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      crashReportLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) crashReportLabel.getLayoutData()).verticalIndent = 10;

      Link crashReportTextLabel = new Link(container, SWT.WRAP);
      crashReportTextLabel.setText(Messages.ErrorInfoPage_SEND_LOGS_ADVISE);
      crashReportTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      crashReportTextLabel.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          try {
            if ("save".equals(e.text)) //$NON-NLS-1$
              OwlUI.saveCrashReport(getShell());
            else
              BrowserUtils.sendErrorLog();
          } catch (Throwable t) {
            setMessage(t.getMessage(), IMessageProvider.ERROR);
          }
        }
      });
    }

    /* Further Steps */
    {
      Label furtherStepsLabel = new Label(container, SWT.NONE);
      furtherStepsLabel.setText(Messages.ErrorInfoPage_FURTHER_STEPS);
      furtherStepsLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
      furtherStepsLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) furtherStepsLabel.getLayoutData()).verticalIndent = 10;

      Link moreInfoLabel = new Link(container, SWT.WRAP);
      if (fErrorStatus.getException() instanceof ProfileLockedException)
        moreInfoLabel.setText(Messages.ErrorInfoPage_LOCKED_PROFILE_ADVISE);
      else
        moreInfoLabel.setText(fHasNextPage ? Messages.ErrorInfoPage_NEXT_PAGE_ADVISE : Messages.ErrorInfoPage_GENERAL_ERROR_ADVISE);
      moreInfoLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) moreInfoLabel.getLayoutData()).widthHint = 200;

      moreInfoLabel.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          try {
            if ("faq".equals(e.text)) //$NON-NLS-1$
              BrowserUtils.openFAQ(fErrorStatus);
            else if ("forum".equals(e.text)) //$NON-NLS-1$
              BrowserUtils.openHelpForum(fErrorStatus);
          } catch (Throwable t) {
            setMessage(t.getMessage(), IMessageProvider.ERROR);
          }
        }
      });
    }

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);

    /* Transfer Focus to Buttons, otherwise a link is focussed which looks weird */
    if (visible) {
      Button focusButton = ((CustomWizardDialog) getContainer()).getButton(IDialogConstants.NEXT_ID);
      if (focusButton == null)
        focusButton = ((CustomWizardDialog) getContainer()).getButton(IDialogConstants.FINISH_ID);

      if (focusButton != null)
        focusButton.setFocus();
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.DialogPage#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    if (fCopyMenu != null && !fCopyMenu.isDisposed())
      fCopyMenu.dispose();
  }
}