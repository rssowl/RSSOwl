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

package org.rssowl.ui.internal.dialogs.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * Container for all Preferences that have not yet been categorized.
 *
 * @author bpasero
 */
public class MiscPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  /** ID of this Preferences Page */
  public static final String ID = "org.rssowl.ui.MiscPreferences"; //$NON-NLS-1$

  private IPreferenceScope fGlobalScope;
  private IPreferenceScope fEclipseScope;
  private Button fMinimizeToTray;
  private Button fMoveToTrayOnStart;
  private Button fMoveToTrayOnExit;
  private Spinner fAutoCloseTabsSpinner;
  private Button fAutoCloseTabsCheck;
  private Button fUseMultipleTabsCheck;
  private Button fReopenFeedsOnStartupCheck;
  private Button fAlwaysReuseFeedView;
  private Button fOpenOnSingleClick;
  private Button fUpdateOnStartup;
  private Button fSingleClickRestore;
  private Button fDoubleClickRestore;

  /** Leave for reflection */
  public MiscPreferencePage() {
    fGlobalScope = Owl.getPreferenceService().getGlobalScope();
    fEclipseScope = Owl.getPreferenceService().getEclipseScope();
    setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/view.gif")); //$NON-NLS-1$
  }

  /**
   * @param title
   */
  public MiscPreferencePage(String title) {
    super(title);
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  @Override
  public void init(IWorkbench workbench) {}

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    updateApplyEnablement(false);
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = createComposite(parent);

    /* Tab Options */
    createTabOptions(container);

    /* System Tray Options */
    if (!Application.IS_ECLIPSE)
      createTrayOptions(container);

    /* Misc Options */
    createMiscOptions(container);

    applyDialogFont(container);

    /* Enable Apply Button on Selection Changes */
    OwlUI.runOnSelection(new Runnable() {
      @Override
      public void run() {
        updateApplyEnablement(true);
      }
    }, container);

    return container;
  }

  private void createTabOptions(Composite container) {
    Label label = new Label(container, SWT.NONE);
    label.setText(Messages.MiscPreferencePage_TABBED_BROWSING);
    label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

    /* Group */
    Composite group = new Composite(container, SWT.None);
    group.setLayout(LayoutUtils.createGridLayout(1, 7, 3));
    ((GridLayout) group.getLayout()).marginBottom = 5;
    group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fAlwaysReuseFeedView = new Button(group, SWT.CHECK);
    fAlwaysReuseFeedView.setText(Messages.MiscPreferencePage_SAME_TAB);
    fAlwaysReuseFeedView.setSelection(fGlobalScope.getBoolean(DefaultPreferences.ALWAYS_REUSE_FEEDVIEW));

    fUseMultipleTabsCheck = new Button(group, SWT.CHECK);
    fUseMultipleTabsCheck.setText(Messages.MiscPreferencePage_MULTIPLE_TABS);
    fUseMultipleTabsCheck.setSelection(fEclipseScope.getBoolean(DefaultPreferences.ECLIPSE_MULTIPLE_TABS));

    Composite autoCloseTabsContainer = new Composite(group, SWT.None);
    autoCloseTabsContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0, 0, 2, false));

    fAutoCloseTabsCheck = new Button(autoCloseTabsContainer, SWT.CHECK);
    fAutoCloseTabsCheck.setText(Messages.MiscPreferencePage_TAB_LIMIT);
    fAutoCloseTabsCheck.setSelection(fEclipseScope.getBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS));
    fAutoCloseTabsCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fAutoCloseTabsSpinner.setEnabled(fAutoCloseTabsCheck.getSelection());
        fAlwaysReuseFeedView.setEnabled(!fAutoCloseTabsCheck.getSelection() || fAutoCloseTabsSpinner.getSelection() > 1);
      }
    });

    fAutoCloseTabsSpinner = new Spinner(autoCloseTabsContainer, SWT.BORDER);
    fAutoCloseTabsSpinner.setMinimum(1);
    fAutoCloseTabsSpinner.setMaximum(100);
    fAutoCloseTabsSpinner.setSelection(fEclipseScope.getInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD));
    fAutoCloseTabsSpinner.setEnabled(fAutoCloseTabsCheck.getSelection());
    fAutoCloseTabsSpinner.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fAlwaysReuseFeedView.setEnabled(!fAutoCloseTabsCheck.getSelection() || fAutoCloseTabsSpinner.getSelection() > 1);
      }
    });

    label = new Label(autoCloseTabsContainer, SWT.None);
    label.setText(Messages.MiscPreferencePage_TABS);

    fAlwaysReuseFeedView.setEnabled(!fAutoCloseTabsCheck.getSelection() || fAutoCloseTabsSpinner.getSelection() > 1);
  }

  private void createMiscOptions(Composite container) {
    Label label = new Label(container, SWT.NONE);
    label.setText(Messages.MiscPreferencePage_MISC);
    label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

    /* Group */
    Composite miscGroup = new Composite(container, SWT.None);
    miscGroup.setLayout(LayoutUtils.createGridLayout(1, 7, 3));
    miscGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    fReopenFeedsOnStartupCheck = new Button(miscGroup, SWT.CHECK);
    fReopenFeedsOnStartupCheck.setText(Messages.MiscPreferencePage_REOPEN_LAST_OPENED);
    fReopenFeedsOnStartupCheck.setSelection(fEclipseScope.getBoolean(DefaultPreferences.ECLIPSE_RESTORE_TABS));

    fOpenOnSingleClick = new Button(miscGroup, SWT.CHECK);
    fOpenOnSingleClick.setText(Messages.MiscPreferencePage_SINGLE_CLICK);
    fOpenOnSingleClick.setSelection(fEclipseScope.getBoolean(DefaultPreferences.ECLIPSE_SINGLE_CLICK_OPEN));

    if (!Application.IS_ECLIPSE) {
      fUpdateOnStartup = new Button(miscGroup, SWT.CHECK);
      fUpdateOnStartup.setText(Messages.MiscPreferencePage_UPDATE_ON_STARTUP);
      fUpdateOnStartup.setSelection(fGlobalScope.getBoolean(DefaultPreferences.UPDATE_ON_STARTUP));
    }
  }

  private void createTrayOptions(Composite container) {
    Label label = new Label(container, SWT.NONE);
    label.setText(Messages.MiscPreferencePage_SYSTEM_TRAY);
    label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

    /* Group */
    Composite group = new Composite(container, SWT.None);
    group.setLayout(LayoutUtils.createGridLayout(1, 7, 3));
    ((GridLayout) group.getLayout()).marginBottom = 5;
    group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    label = new Label(group, SWT.NONE);
    label.setText(Messages.MiscPreferencePage_MOVE_TO_TRAY);

    /* Enable / Disable Tray */
    fMinimizeToTray = new Button(group, SWT.CHECK);
    fMinimizeToTray.setText(Messages.MiscPreferencePage_ON_MINIMIZE);
    fMinimizeToTray.setSelection(fGlobalScope.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE));
    fMinimizeToTray.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateRestoreEnablement();
      }
    });

    /* Move to Tray on Start */
    fMoveToTrayOnStart = new Button(group, SWT.CHECK);
    fMoveToTrayOnStart.setText(Messages.MiscPreferencePage_ON_START);
    fMoveToTrayOnStart.setSelection(fGlobalScope.getBoolean(DefaultPreferences.TRAY_ON_START));
    fMoveToTrayOnStart.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateRestoreEnablement();
      }
    });

    /* Move to Tray on Close */
    fMoveToTrayOnExit = new Button(group, SWT.CHECK);
    fMoveToTrayOnExit.setText(Messages.MiscPreferencePage_ON_CLOSE);
    fMoveToTrayOnExit.setSelection(fGlobalScope.getBoolean(DefaultPreferences.TRAY_ON_CLOSE));
    fMoveToTrayOnExit.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        updateRestoreEnablement();
      }
    });

    label = new Label(group, SWT.NONE);
    label.setText(Messages.MiscPreferencePage_RESTORE_FROM_TRAY);
    label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    ((GridData) label.getLayoutData()).verticalIndent = 5;

    Composite buttonContainer = new Composite(group, SWT.None);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));

    fSingleClickRestore = new Button(buttonContainer, SWT.RADIO);
    fSingleClickRestore.setText(Messages.MiscPreferencePage_SINGLE_CLICK_RESTORE);
    fSingleClickRestore.setSelection(!fGlobalScope.getBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK));

    fDoubleClickRestore = new Button(buttonContainer, SWT.RADIO);
    fDoubleClickRestore.setText(Messages.MiscPreferencePage_DOUBLE_CLICK_RESTORE);
    fDoubleClickRestore.setSelection(fGlobalScope.getBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK));

    updateRestoreEnablement();
  }

  private void updateRestoreEnablement() {
    boolean enable = fMinimizeToTray.getSelection() || fMoveToTrayOnStart.getSelection() || fMoveToTrayOnExit.getSelection();
    fSingleClickRestore.setEnabled(enable);
    fDoubleClickRestore.setEnabled(enable);
  }

  private Composite createComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
    composite.setFont(parent.getFont());
    return composite;
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    if (!Application.IS_ECLIPSE)
      fGlobalScope.putBoolean(DefaultPreferences.UPDATE_ON_STARTUP, fUpdateOnStartup.getSelection());

    fEclipseScope.putBoolean(DefaultPreferences.ECLIPSE_SINGLE_CLICK_OPEN, fOpenOnSingleClick.getSelection());
    if (!Application.IS_ECLIPSE)
      OpenStrategy.setOpenMethod(fOpenOnSingleClick.getSelection() ? OpenStrategy.SINGLE_CLICK | OpenStrategy.ARROW_KEYS_OPEN : OpenStrategy.DOUBLE_CLICK);
    fEclipseScope.putBoolean(DefaultPreferences.ECLIPSE_RESTORE_TABS, fReopenFeedsOnStartupCheck.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.ALWAYS_REUSE_FEEDVIEW, fAlwaysReuseFeedView.getSelection());
    fEclipseScope.putBoolean(DefaultPreferences.ECLIPSE_MULTIPLE_TABS, fUseMultipleTabsCheck.getSelection());
    fEclipseScope.putBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS, fAutoCloseTabsCheck.getSelection());
    fEclipseScope.putInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD, fAutoCloseTabsSpinner.getSelection());

    if (!Application.IS_ECLIPSE) {
      fGlobalScope.putBoolean(DefaultPreferences.TRAY_ON_MINIMIZE, fMinimizeToTray.getSelection());
      fGlobalScope.putBoolean(DefaultPreferences.TRAY_ON_START, fMoveToTrayOnStart.getSelection());
      fGlobalScope.putBoolean(DefaultPreferences.TRAY_ON_CLOSE, fMoveToTrayOnExit.getSelection());
      fGlobalScope.putBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK, fDoubleClickRestore.getSelection());
    }

    return super.performOk();
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performApply()
   */
  @Override
  protected void performApply() {
    super.performApply();
    updateApplyEnablement(false);
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults() {
    super.performDefaults();

    IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();

    if (!Application.IS_ECLIPSE)
      fUpdateOnStartup.setSelection(defaultScope.getBoolean(DefaultPreferences.UPDATE_ON_STARTUP));

    fOpenOnSingleClick.setSelection(defaultScope.getBoolean(DefaultPreferences.ECLIPSE_SINGLE_CLICK_OPEN));
    if (!Application.IS_ECLIPSE)
      OpenStrategy.setOpenMethod(fOpenOnSingleClick.getSelection() ? OpenStrategy.SINGLE_CLICK | OpenStrategy.ARROW_KEYS_OPEN : OpenStrategy.DOUBLE_CLICK);
    fReopenFeedsOnStartupCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.ECLIPSE_RESTORE_TABS));
    fAlwaysReuseFeedView.setSelection(true); //Forced for performance reasons
    fUseMultipleTabsCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.ECLIPSE_MULTIPLE_TABS));
    fAutoCloseTabsCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS));
    fAutoCloseTabsSpinner.setSelection(defaultScope.getInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD));
    fAutoCloseTabsSpinner.setEnabled(fAutoCloseTabsCheck.getSelection());

    if (!Application.IS_ECLIPSE) {
      fMinimizeToTray.setSelection(defaultScope.getBoolean(DefaultPreferences.TRAY_ON_MINIMIZE));
      fMoveToTrayOnStart.setSelection(defaultScope.getBoolean(DefaultPreferences.TRAY_ON_START));
      fMoveToTrayOnExit.setSelection(defaultScope.getBoolean(DefaultPreferences.TRAY_ON_CLOSE));
      fSingleClickRestore.setSelection(!defaultScope.getBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK));
      fDoubleClickRestore.setSelection(defaultScope.getBoolean(DefaultPreferences.RESTORE_TRAY_DOUBLECLICK));
      updateRestoreEnablement();
    }

    fAlwaysReuseFeedView.setEnabled(!fAutoCloseTabsCheck.getSelection() || fAutoCloseTabsSpinner.getSelection() > 1);

    updateApplyEnablement(true);
  }

  private void updateApplyEnablement(boolean enable) {
    Button applyButton = getApplyButton();
    if (applyButton != null && !applyButton.isDisposed() && applyButton.isEnabled() != enable)
      applyButton.setEnabled(enable);
  }
}