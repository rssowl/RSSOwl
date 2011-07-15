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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * @author bpasero
 */
public class AggregateNewsDialog extends TitleAreaDialog {
  private Button fRememberDecisionCheck;
  private LocalResourceManager fResources;
  private IPreferenceScope fPreferences;
  private final String fFolderName;

  /**
   * @param parentShell
   * @param folderName
   */
  public AggregateNewsDialog(Shell parentShell, String folderName) {
    super(parentShell);
    fFolderName = folderName;
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
   * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
   */
  @Override
  protected void buttonPressed(int buttonId) {
    switch (buttonId) {
      case IDialogConstants.YES_ID:
        fPreferences.putBoolean(DefaultPreferences.REMEMBER_AGGREGATE_NEWS_OPTION, fRememberDecisionCheck.getSelection());
        if (fRememberDecisionCheck.getSelection())
          fPreferences.putBoolean(DefaultPreferences.AGGREGATE_NEWS_AS_SEARCH, true);
        setReturnCode(buttonId);
        close();
        break;

      case IDialogConstants.NO_ID:
        fPreferences.putBoolean(DefaultPreferences.REMEMBER_AGGREGATE_NEWS_OPTION, fRememberDecisionCheck.getSelection());
        if (fRememberDecisionCheck.getSelection())
          fPreferences.putBoolean(DefaultPreferences.AGGREGATE_NEWS_AS_SEARCH, false);
        setReturnCode(buttonId);
        close();
        break;
    }

    super.buttonPressed(buttonId);
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);

    newShell.setText(Messages.AggregateNewsDialog_AGGREGATE_NEWS);
  }

  /**
   * @return the path to the title image to use.
   */
  protected String getTitleImage() {
    return "/icons/wizban/aggfolder_wiz.png"; //$NON-NLS-1$
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
    setTitle(Messages.AggregateNewsDialog_AGGREGATE_NEWS);

    /* Title Image */
    setTitleImage(OwlUI.getImage(fResources, getTitleImage()));

    /* Title Message */
    setMessage(NLS.bind(Messages.AggregateNewsDialog_AGGREGATE_NEWS_OF_N, fFolderName));

    /* Dialog Message */
    Label dialogMessageLabel = new Label(composite, SWT.WRAP);
    dialogMessageLabel.setText(Messages.AggregateNewsDialog_AGGREGATION_RECOMMENDATION);
    dialogMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Spacer */
    new Label(composite, SWT.NONE);

    /* Checkbox to disable confirm dialog */
    fRememberDecisionCheck = new Button(composite, SWT.CHECK);
    fRememberDecisionCheck.setText(Messages.AggregateNewsDialog_REMEMBER_DECISION);

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
    createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL, true);
    createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
    getButton(IDialogConstants.YES_ID).setFocus();
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