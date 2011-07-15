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
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * A dialog informing the user about the Archive feature to store selected news.
 *
 * @author bpasero
 */
public class ArchiveNewsDialog extends TitleAreaDialog {
  private LocalResourceManager fResources;

  /**
   * @param parentShell
   */
  public ArchiveNewsDialog(Shell parentShell) {
    super(parentShell);
    fResources = new LocalResourceManager(JFaceResources.getResources());
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
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);

    newShell.setText(Messages.ArchiveNewsDialog_ARCHIVE_NEWS);
  }

  /**
   * @return the path to the title image to use.
   */
  protected String getTitleImage() {
    return "/icons/wizban/archive_wiz.png"; //$NON-NLS-1$
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
    setTitle(Messages.ArchiveNewsDialog_ARCHIVE_NEWS);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, getTitleImage()));

    /* Title Message */
    setMessage(Messages.ArchiveNewsDialog_ARCHIVE_NEWS_MSG);

    /* Dialog Message */
    Label dialogMessageLabel = new Label(composite, SWT.WRAP);
    dialogMessageLabel.setText(Messages.ArchiveNewsDialog_ARCHIVE_NEWS_DESCRIPTION);
    dialogMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Spacer */
    new Label(composite, SWT.NONE);

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
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    getButton(IDialogConstants.OK_ID).setFocus();
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