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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * A Dialog that is shown to the user in case RSSOwl failed to start or during a
 * fatal error like an Out of Memory exception.
 *
 * @author bpasero
 */
public class FatalOutOfMemoryErrorDialog extends TitleAreaDialog {
  private LocalResourceManager fResources;
  private final IStatus fErrorStatus;

  /**
   * @param errorStatus
   */
  public FatalOutOfMemoryErrorDialog(IStatus errorStatus) {
    super(null);
    fErrorStatus = errorStatus;
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

    newShell.setText(Messages.StartupErrorDialog_RSSOWL_CRASH_REPORTER);

    /* Images no longer initialized */
    Image img_16x16 = OwlUI.getImage(fResources, "icons/product/16x16.png"); //$NON-NLS-1$
    Image img_32x32 = OwlUI.getImage(fResources, "icons/product/32x32.png"); //$NON-NLS-1$
    Image img_48x48 = OwlUI.getImage(fResources, "icons/product/48x48.png"); //$NON-NLS-1$
    Image img_64x64 = OwlUI.getImage(fResources, "icons/product/64x64.png"); //$NON-NLS-1$
    Image img_128x128 = OwlUI.getImage(fResources, "icons/product/128x128.png"); //$NON-NLS-1$
    newShell.setImages(new Image[] { img_16x16, img_32x32, img_48x48, img_64x64, img_128x128 });
  }

  /*
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Composite to hold all components */
    Composite composite = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 5, 10, 15, 5, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Title */
    setTitle(Messages.StartupErrorDialog_WE_SORRY);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, "icons/wizban/welcome_wiz.gif")); //$NON-NLS-1$

    /* Title Message */
    setMessage(Messages.FatalOutOfMemoryErrorDialog_RSSOWL_CRASHED_OOM, IMessageProvider.WARNING);

    /* Recovery Label */
    Link recoveryMessageLabel = new Link(composite, SWT.WRAP);
    recoveryMessageLabel.setText(Messages.FatalOutOfMemoryErrorDialog_CRASH_ADVISE_OOM);
    recoveryMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    recoveryMessageLabel.addSelectionListener(new SelectionAdapter() {
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

    /* Crash Report Label */
    Link dialogMessageLabel = new Link(composite, SWT.WRAP);
    dialogMessageLabel.setText(Messages.FatalOutOfMemoryErrorDialog_CRASH_DIAGNOSE_OOM);
    dialogMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    dialogMessageLabel.addSelectionListener(new SelectionAdapter() {
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

    /* Error Details Label */
    Label reasonLabel = new Label(composite, SWT.NONE);
    reasonLabel.setText(Messages.StartupErrorDialog_ERROR_DETAILS);
    reasonLabel.setFont(OwlUI.getBold(JFaceResources.DIALOG_FONT));
    reasonLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    Label errorDetailsLabel = new Label(composite, SWT.WRAP);
    if (StringUtils.isSet(fErrorStatus.getMessage()))
      errorDetailsLabel.setText(NLS.bind(Messages.FatalOutOfMemoryErrorDialog_OOM_ERROR_N, fErrorStatus.getMessage()));
    else
      errorDetailsLabel.setText(Messages.FatalOutOfMemoryErrorDialog_OOM_ERROR);
    errorDetailsLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

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
   * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
   */
  @Override
  protected void buttonPressed(int buttonId) {
    setReturnCode(buttonId);
    close();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.HELP_ID, Messages.StartupErrorDialog_HELP, true);
    getButton(IDialogConstants.HELP_ID).setFocus();
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