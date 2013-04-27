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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Generic Dialog for asking for confirmation when performing something.
 *
 * @author bpasero
 */
public class ConfirmDialog extends TitleAreaDialog {
  private String fDialogHeaderMessage;
  private String fDialogMessage;
  private Button fNeverAskAgainCheck;
  private String fTitle;
  private LocalResourceManager fResources;
  private String fConfirmPrefKey;
  private IPreferenceScope fPreferences;
  private String fButtonName;

  /**
   * Instantiate a new ConfirmDeleteDialog
   *
   * @param parentShell The parent shell
   * @param title The title of the dialog
   * @param dialogHeaderMessage The info message
   * @param dialogMessage The dialog message
   * @param confirmPrefKey The key to the boolean preference to receive the
   * setting for "Never ask again"
   */
  public ConfirmDialog(Shell parentShell, String title, String dialogHeaderMessage, String dialogMessage, String confirmPrefKey) {
    this(parentShell, title, dialogHeaderMessage, dialogMessage, Messages.ConfirmDialog_DELETE, confirmPrefKey);
  }

  /**
   * Instantiate a new ConfirmDeleteDialog
   *
   * @param parentShell The parent shell
   * @param title The title of the dialog
   * @param dialogHeaderMessage The info message
   * @param dialogMessage The dialog message
   * @param okButtonName
   * @param confirmPrefKey The key to the boolean preference to receive the
   * setting for "Never ask again"
   */
  public ConfirmDialog(Shell parentShell, String title, String dialogHeaderMessage, String dialogMessage, String okButtonName, String confirmPrefKey) {
    super(parentShell);

    fTitle = title;
    fDialogMessage = dialogMessage;
    fDialogHeaderMessage = dialogHeaderMessage;
    fButtonName = okButtonName;
    fConfirmPrefKey = confirmPrefKey;
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fPreferences = Owl.getPreferenceService().getGlobalScope();
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#close()
   */
  @Override
  public boolean close() {
    fResources.dispose();

    return super.close();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    if (fNeverAskAgainCheck != null && fNeverAskAgainCheck.getSelection())
      fPreferences.putBoolean(fConfirmPrefKey, false);

    super.okPressed();
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);

    newShell.setText(fTitle);
  }

  /**
   * @return the path to the title image to use.
   */
  protected String getTitleImage() {
    return "/icons/wizban/trash.gif"; //$NON-NLS-1$
  }

  /**
   * @return the title label to use
   */
  protected String getTitleLabel() {
    return Messages.ConfirmDialog_DELETE;
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Composite to hold all components */
    Composite composite = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(1, 5, 10));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Title */
    setTitle(this.getTitleLabel());

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, getTitleImage()));

    /* Title Message */
    setMessage(fDialogHeaderMessage, IMessageProvider.WARNING);

    /* Dialog Message */
    Label dialogMessageLabel = new Label(composite, SWT.WRAP);
    dialogMessageLabel.setText(fDialogMessage);
    dialogMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Spacer */
    new Label(composite, SWT.NONE);

    /* Checkbox to disable confirm dialog */
    if (fConfirmPrefKey != null) {
      fNeverAskAgainCheck = new Button(composite, SWT.CHECK);
      fNeverAskAgainCheck.setText(Messages.ConfirmDialog_NEVER_ASK_AGAIN);
    }

    /* Holder for the separator to the OK and Cancel buttons */
    Composite sepHolder = new Composite(parent, SWT.NONE);
    sepHolder.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    sepHolder.setLayout(LayoutUtils.createGridLayout(1, 0, 0));

    /* Separator */
    Label separator = new Label(sepHolder, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    applyDialogFont(composite);

    return composite;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, fButtonName, false);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
    getButton(IDialogConstants.CANCEL_ID).setFocus();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();
    Point bestSize = getShell().computeSize(convertHorizontalDLUsToPixels(OwlUI.MIN_DIALOG_WIDTH_DLU), SWT.DEFAULT);
    Point location = getInitialLocation(bestSize);
    getShell().setBounds(location.x, location.y, bestSize.x, bestSize.y);
  }
}