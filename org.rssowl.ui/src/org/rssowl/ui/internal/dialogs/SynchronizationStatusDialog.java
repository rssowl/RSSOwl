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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.connection.SyncConnectionException;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.actions.ImportAction;
import org.rssowl.ui.internal.services.SyncService;
import org.rssowl.ui.internal.services.SyncService.SyncStatus;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.text.DateFormat;
import java.util.Date;

/**
 * Dialog to show Synchronization Status from the {@link SyncService}.
 *
 * @author bpasero
 */
public class SynchronizationStatusDialog extends TitleAreaDialog {
  private LocalResourceManager fResources;
  private final SyncStatus fStatus;
  private final DateFormat fDateFormat = OwlUI.getShortDateFormat();
  private final DateFormat fTimeFormat = OwlUI.getShortTimeFormat();

  /**
   * @param parentShell the parent shell
   * @param status the synchronization status to show
   */
  public SynchronizationStatusDialog(Shell parentShell, SyncStatus status) {
    super(parentShell);
    fStatus = status;
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
    newShell.setText(Messages.SynchronizationStatusDialog_SYNC_STATUS);
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
    setTitle(Messages.SynchronizationStatusDialog_SYNC_STATUS);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, "/icons/wizban/reader_wiz.png")); //$NON-NLS-1$

    /* Title Message */
    if (fStatus == null)
      setMessage(Messages.SynchronizationStatusDialog_NO_STATUS_AVAILABLE, IMessageProvider.INFORMATION);
    else if (fStatus.isOK())
      setMessage(Messages.SynchronizationStatusDialog_LAST_SYNC_OK, IMessageProvider.INFORMATION);
    else
      setMessage(Messages.SynchronizationStatusDialog_LAST_SYNC_ERROR, IMessageProvider.ERROR);

    /* Dialog Message Link */
    Link dialogMessageLink = new Link(composite, SWT.WRAP);
    dialogMessageLink.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    boolean isToday = fStatus != null ? DateUtils.isAfterIncludingToday(new Date(fStatus.getTime()),  DateUtils.getToday().getTimeInMillis()) : false;
    DateFormat format = isToday ? fTimeFormat : fDateFormat;

    /* a) Never Synchronized */
    if (fStatus == null) {
      dialogMessageLink.setText(Messages.SynchronizationStatusDialog_NO_STATUS_MSG);
      dialogMessageLink.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          close();
          ImportAction action = new ImportAction();
          action.openWizardDefault(getParentShell());
        }
      });
    }

    /* b) Synchronized OK */
    else if (fStatus.isOK()) {
      if (fStatus.getItemCount() == 1)
        dialogMessageLink.setText(NLS.bind(Messages.SynchronizationStatusDialog_LAST_SYNC_OK_MSG, format.format(fStatus.getTime()), fStatus.getTotalItemCount()));
      else
        dialogMessageLink.setText(NLS.bind(Messages.SynchronizationStatusDialog_LAST_SYNC_OK_MSG_N, new Object[] { fStatus.getItemCount(), format.format(fStatus.getTime()), fStatus.getTotalItemCount() }));
    }

    /* c) Synchronization ERROR */
    else {
      final String userUrl = (fStatus.getException() instanceof SyncConnectionException) ? ((SyncConnectionException) fStatus.getException()).getUserUrl() : null;

      /* Google provided link to solve the issue */
      if (StringUtils.isSet(userUrl)) {
        dialogMessageLink.setText(NLS.bind(Messages.SynchronizationStatusDialog_LAST_SYNC_ERROR_MSG_LINK, format.format(fStatus.getTime()), fStatus.getException().getMessage()));
        dialogMessageLink.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            BrowserUtils.openLinkExternal(userUrl);
          }
        });
      }

      /* Other general connection issue */
      else {
        dialogMessageLink.setText(NLS.bind(Messages.SynchronizationStatusDialog_LAST_SYNC_ERROR_MSG, format.format(fStatus.getTime()), CoreUtils.toMessage(fStatus.getException())));
      }
    }

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