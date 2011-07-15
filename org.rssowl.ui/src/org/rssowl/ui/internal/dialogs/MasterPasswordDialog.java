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
 **   The algorithm to measure the strength of the master password was       **
 **   taken from the Firefox sourcecode and is licensed under the Mozilla    **
 **   Public License (MPL).                                                  **
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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.equinox.security.storage.provider.PasswordProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Dialog to ask for the master password used to encrypt credentials. Supports a
 * mode to create a new master password and to ask for an existing one.
 *
 * @author bpasero
 */
@SuppressWarnings("restriction")
public class MasterPasswordDialog extends TitleAreaDialog {
  private final LocalResourceManager fResources;
  private Text fPassword;
  private Text fPasswordConfirmed;
  private String fPasswordValue;
  private final int fStyle;
  private ProgressBar fQualityBar;

  /**
   * @param parentShell
   * @param style
   */
  public MasterPasswordDialog(Shell parentShell, int style) {
    super(parentShell);
    fStyle = style;
    fResources = new LocalResourceManager(JFaceResources.getResources());
  }

  /**
   * @return the master password or <code>null</code> if none.
   */
  public String getMasterPassword() {
    return fPasswordValue;
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#close()
   */
  @Override
  public boolean close() {
    fResources.dispose();
    return super.close();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
   */
  @Override
  protected void buttonPressed(int buttonId) {

    /* User pressed OK Button */
    if (buttonId == IDialogConstants.OK_ID)
      fPasswordValue = fPassword.getText();

    super.buttonPressed(buttonId);
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);

    if ((fStyle & PasswordProvider.CREATE_NEW_PASSWORD) != 0)
      shell.setText(Messages.MasterPasswordDialog_ENTER_MASTER_PASSWORD_TITLE);
    else
      shell.setText(Messages.MasterPasswordDialog_ENTER_MASTER_PASSWORD);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#create()
   */
  @Override
  public void create() {
    super.create();

    if ((fStyle & PasswordProvider.CREATE_NEW_PASSWORD) != 0)
      getButton(IDialogConstants.OK_ID).setEnabled(false);
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Separator */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Composite to hold all components */
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 5, 10));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

    /* Title */
    setTitle(Messages.MasterPasswordDialog_MASTER_PASSWORD);

    /* Title Image */
    if ((fStyle & PasswordProvider.CREATE_NEW_PASSWORD) != 0) {
      setTitleImage(OwlUI.getImage(fResources, "icons/wizban/new_value_wiz.png")); //$NON-NLS-1$
      setMessage(Messages.MasterPasswordDialog_REMEMBER_PASSWORD, IMessageProvider.WARNING);
    } else {
      setTitleImage(OwlUI.getImage(fResources, "icons/wizban/login_wiz.png")); //$NON-NLS-1$
      setMessage(Messages.MasterPasswordDialog_MASTER_PASSWORD_INFO, IMessageProvider.INFORMATION);
    }

    /* Username Label */
    Label passwordLabel = new Label(composite, SWT.NONE);
    passwordLabel.setText(Messages.MasterPasswordDialog_PASSWORD);
    passwordLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    /* Password input field */
    fPassword = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
    fPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    fPassword.setFocus();

    if ((fStyle & PasswordProvider.CREATE_NEW_PASSWORD) != 0) {
      fPassword.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          boolean enabled = fPassword.getText().equals(fPasswordConfirmed.getText()) && fPassword.getText().length() > 0;
          getButton(IDialogConstants.OK_ID).setEnabled(enabled);
          updateQualityBar();
        }
      });
    }

    /* Confirm Password Label */
    if ((fStyle & PasswordProvider.CREATE_NEW_PASSWORD) != 0) {
      Label confirmPasswordLabel = new Label(composite, SWT.NONE);
      confirmPasswordLabel.setText(Messages.MasterPasswordDialog_CONFIRM_PASSWORD);
      confirmPasswordLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

      /* Confirm Password input field */
      fPasswordConfirmed = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
      fPasswordConfirmed.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      fPasswordConfirmed.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          boolean enabled = fPassword.getText().equals(fPasswordConfirmed.getText()) && fPassword.getText().length() > 0;
          getButton(IDialogConstants.OK_ID).setEnabled(enabled);
        }
      });

      /* Spacer */
      new Label(composite, SWT.NONE);
      new Label(composite, SWT.NONE);

      /* Password Quality Meter */
      Label passwordQuality = new Label(composite, SWT.NONE);
      passwordQuality.setText(Messages.MasterPasswordDialog_PASSWORD_QUALITY);
      passwordQuality.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

      fQualityBar = new ProgressBar(composite, SWT.HORIZONTAL);
      fQualityBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      fQualityBar.setMinimum(0);
      fQualityBar.setMaximum(100);
    }

    /* Separator */
    Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));

    applyDialogFont(composite);

    return composite;
  }

  private void updateQualityBar() {
    String pw = fPassword.getText();

    int score = 0;

    /* 1.) Length: password-length*10 - 20 (Max 20) */
    score += Math.min(20, (pw.length() * 10) - 20);

    /* 2.) Numbers: no-of-numerics * 10 (Max 30) */
    int numericsCount = 0;
    for (int i = 0; i < pw.length(); i++) {
      if (Character.isDigit(pw.charAt(i)))
        numericsCount++;
    }

    score += Math.min(30, numericsCount * 10);

    /* 3.) Symbols: no-of-symbols * 15 (Max 45) */
    int symbolCount = 0;
    for (int i = 0; i < pw.length(); i++) {
      if (!Character.isLetterOrDigit(pw.charAt(i)))
        symbolCount++;
    }

    score += Math.min(45, symbolCount * 15);

    /* 4.) Uppercase: no-of-Uppercase * 10 (Max 30) */
    int upperCaseCount = 0;
    for (int i = 0; i < pw.length(); i++) {
      if (Character.isUpperCase(pw.charAt(i)))
        upperCaseCount++;
    }

    score += Math.min(30, upperCaseCount * 10);

    fQualityBar.setSelection(score);
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.CLOSE | getDefaultOrientation();

    return style;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();

    Shell shell = getShell();

    /* Minimum Size */
    int minWidth = convertHorizontalDLUsToPixels(OwlUI.MIN_DIALOG_WIDTH_DLU);
    int minHeight = shell.computeSize(minWidth, SWT.DEFAULT).y;

    /* Required Size */
    Point requiredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    shell.setSize(Math.max(minWidth, requiredSize.x), Math.max(minHeight, requiredSize.y));
    LayoutUtils.positionShell(shell);
  }
}