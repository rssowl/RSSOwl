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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.WebsiteListDialog;
import org.rssowl.ui.internal.util.CBrowser;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * @author bpasero
 */
public class BrowserPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

  /** ID of the Page */
  public static String ID = "org.rssowl.ui.BrowserPreferencesPage"; //$NON-NLS-1$

  private IPreferenceScope fGlobalScope;
  private Text fCustomBrowserInput;
  private Button fUseCustomExternalBrowser;
  private Button fUseDefaultExternalBrowser;
  private Button fUseInternalBrowser;
  private Button fCustomBrowserSearchButton;
  private Button fReOpenBrowserTabs;
  private Button fOpenLinksInNewTab;
  private Button fLoadBrowserTabInBackground;
  private Button fAlwaysReuseBrowser;
  private Button fEnablePopupBlockerCheck;
  private Button fDisableJavaScriptCheck;
  private Button fDisableJavaScriptExceptionsButton;
  private IPreferenceScope fEclipseScope;
  private LocalResourceManager fResources = new LocalResourceManager(JFaceResources.getResources());

  /** Leave for reflection */
  public BrowserPreferencePage() {
    fGlobalScope = Owl.getPreferenceService().getGlobalScope();
    fEclipseScope = Owl.getPreferenceService().getEclipseScope();
    setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/browser.gif")); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
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
   * @see org.eclipse.jface.dialogs.DialogPage#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    fResources.dispose();
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = createComposite(parent);

    /* Browser Options */
    createBrowserOptions(container);

    applyDialogFont(container);

    return container;
  }

  private void createBrowserOptions(Composite container) {
    Composite browserGroup = new Composite(container, SWT.None);
    browserGroup.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    browserGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Tabbed Browsing */
    {
      Label label = new Label(browserGroup, SWT.NONE);
      label.setText(Messages.BrowserPreferencePage_BROWSER_SELECTION);
      label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

      /* Group */
      Composite group = new Composite(browserGroup, SWT.None);
      group.setLayout(LayoutUtils.createGridLayout(2, 7, 3));
      ((GridLayout) group.getLayout()).marginBottom = 5;
      group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      /* Use internal Browser */
      fUseInternalBrowser = new Button(group, SWT.RADIO);
      fUseInternalBrowser.setText(Messages.BrowserPreferencePage_USE_EMBEDDED_BROWSER);
      fUseInternalBrowser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
      fUseInternalBrowser.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          updateEnablement();
        }
      });

      /* Use default external Browser */
      fUseDefaultExternalBrowser = new Button(group, SWT.RADIO);
      String name = getDefaultBrowserName();
      if (StringUtils.isSet(name))
        fUseDefaultExternalBrowser.setText(NLS.bind(Messages.BrowserPreferencePage_USE_STANDARD_BROWSER_N, name));
      else
        fUseDefaultExternalBrowser.setText(Messages.BrowserPreferencePage_USE_STANDARD_BROWSER);

      fUseDefaultExternalBrowser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
      fUseDefaultExternalBrowser.setSelection(fGlobalScope.getBoolean(DefaultPreferences.USE_DEFAULT_EXTERNAL_BROWSER));

      /* Use custom external Browser */
      fUseCustomExternalBrowser = new Button(group, SWT.RADIO);
      fUseCustomExternalBrowser.setText(Messages.BrowserPreferencePage_USE_EXTERNAL_BROWSER);
      fUseCustomExternalBrowser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
      fUseCustomExternalBrowser.setSelection(fGlobalScope.getBoolean(DefaultPreferences.USE_CUSTOM_EXTERNAL_BROWSER));
      fUseCustomExternalBrowser.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          fCustomBrowserInput.setEnabled(fUseCustomExternalBrowser.getSelection());
          fCustomBrowserSearchButton.setEnabled(fUseCustomExternalBrowser.getSelection());
        }
      });

      fUseInternalBrowser.setSelection(!fUseDefaultExternalBrowser.getSelection() && !fUseCustomExternalBrowser.getSelection());

      fCustomBrowserInput = new Text(group, SWT.BORDER);
      OwlUI.makeAccessible(fCustomBrowserInput, fUseCustomExternalBrowser);
      fCustomBrowserInput.setEnabled(fUseCustomExternalBrowser.getSelection());
      fCustomBrowserInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

      ControlDecoration controlDeco = new ControlDecoration(fCustomBrowserInput, SWT.LEFT | SWT.TOP);
      controlDeco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
      controlDeco.setDescriptionText(Messages.BrowserPreferencePage_BROWSER_PARAMETER_SUPPORT);
      controlDeco.setShowOnlyOnFocus(true);

      String customBrowserValue = fGlobalScope.getString(DefaultPreferences.CUSTOM_BROWSER_PATH);
      if (customBrowserValue != null)
        fCustomBrowserInput.setText(customBrowserValue);

      fCustomBrowserSearchButton = new Button(group, SWT.PUSH);
      fCustomBrowserSearchButton.setText(Messages.BrowserPreferencePage_BROWSE);
      Dialog.applyDialogFont(fCustomBrowserSearchButton);
      setButtonLayoutData(fCustomBrowserSearchButton);
      fCustomBrowserSearchButton.setEnabled(fUseCustomExternalBrowser.getSelection());
      fCustomBrowserSearchButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
          dialog.setFileName(fCustomBrowserInput.getText());
          String path = dialog.open();
          if (path != null)
            fCustomBrowserInput.setText(path);
        }
      });
    }

    /* Tabbed Browsing */
    {
      Label label = new Label(browserGroup, SWT.NONE);
      label.setText(Messages.BrowserPreferencePage_TABBED_BROWSING);
      label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

      /* Group */
      Composite group = new Composite(browserGroup, SWT.None);
      group.setLayout(LayoutUtils.createGridLayout(1, 7, 3));
      ((GridLayout) group.getLayout()).marginBottom = 5;
      group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      fOpenLinksInNewTab = new Button(group, SWT.CHECK);
      fOpenLinksInNewTab.setText(Messages.BrowserPreferencePage_OPEN_LINKS_IN_TABS);
      fOpenLinksInNewTab.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
      fOpenLinksInNewTab.setSelection(fGlobalScope.getBoolean(DefaultPreferences.OPEN_LINKS_IN_NEW_TAB));

      fReOpenBrowserTabs = new Button(group, SWT.CHECK);
      fReOpenBrowserTabs.setText(Messages.BrowserPreferencePage_REOPEN_WEBSITE);
      fReOpenBrowserTabs.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
      fReOpenBrowserTabs.setSelection(fGlobalScope.getBoolean(DefaultPreferences.REOPEN_BROWSER_TABS));

      fLoadBrowserTabInBackground = new Button(group, SWT.CHECK);
      fLoadBrowserTabInBackground.setText(Messages.BrowserPreferencePage_OPEN_IN_BACKGROUND);
      fLoadBrowserTabInBackground.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
      fLoadBrowserTabInBackground.setSelection(fGlobalScope.getBoolean(DefaultPreferences.OPEN_BROWSER_IN_BACKGROUND));

      fAlwaysReuseBrowser = new Button(group, SWT.CHECK);
      fAlwaysReuseBrowser.setText(Messages.BrowserPreferencePage_OPEN_IN_SAME_TAB);
      fAlwaysReuseBrowser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
      fAlwaysReuseBrowser.setSelection(fGlobalScope.getBoolean(DefaultPreferences.ALWAYS_REUSE_BROWSER));
    }

    /* Disable JavaScript in Browser / Popup Blocker */
    if (!Application.IS_WINDOWS || !CBrowser.isMozillaRunningOnWindows()) {
      Label label = new Label(browserGroup, SWT.NONE);
      label.setText(Messages.BrowserPreferencePage_CONTENT);
      label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

      /* Group */
      Composite group = new Composite(browserGroup, SWT.None);
      group.setLayout(LayoutUtils.createGridLayout(2, 7, 3));
      group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      if (Application.IS_WINDOWS) {
        fEnablePopupBlockerCheck = new Button(group, SWT.CHECK);
        fEnablePopupBlockerCheck.setText(Messages.BrowserPreferencePage_BLOCK_POPUPS);
        fEnablePopupBlockerCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
        fEnablePopupBlockerCheck.setSelection(fGlobalScope.getBoolean(DefaultPreferences.ENABLE_IE_POPUP_BLOCKER));
      }

      Composite jsContainer = new Composite(group, SWT.NONE);
      jsContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
      jsContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));

      fDisableJavaScriptCheck = new Button(jsContainer, SWT.CHECK);
      fDisableJavaScriptCheck.setText(Messages.BrowserPreferencePage_DISABLE_JS);
      fDisableJavaScriptCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true));
      fDisableJavaScriptCheck.setSelection(fGlobalScope.getBoolean(DefaultPreferences.DISABLE_JAVASCRIPT));
      fDisableJavaScriptCheck.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          fDisableJavaScriptExceptionsButton.setEnabled(fDisableJavaScriptCheck.getSelection());
        }
      });

      fDisableJavaScriptExceptionsButton = new Button(jsContainer, SWT.PUSH);
      fDisableJavaScriptExceptionsButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
      fDisableJavaScriptExceptionsButton.setText(Messages.BrowserPreferencePage_EXCEPTIONS);
      Dialog.applyDialogFont(fDisableJavaScriptExceptionsButton);
      setButtonLayoutData(fDisableJavaScriptExceptionsButton);
      fDisableJavaScriptExceptionsButton.setEnabled(fDisableJavaScriptCheck.getSelection());
      fDisableJavaScriptExceptionsButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          new WebsiteListDialog(getShell()).open();
        }
      });

      /* Info Container to Link to Internet Options */
      if (Application.IS_WINDOWS) {
        Composite infoContainer = new Composite(group, SWT.None);
        infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
        ((GridLayout) infoContainer.getLayout()).marginTop = 5;

        Label infoImg = new Label(infoContainer, SWT.NONE);
        infoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/info.gif")); //$NON-NLS-1$
        infoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        Link infoText = new Link(infoContainer, SWT.WRAP);
        infoText.setText(Messages.BrowserPreferencePage_INTERNET_OPTIONS);
        infoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        infoText.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            Program.launch("inetcpl.cpl"); //$NON-NLS-1$
          }
        });
      }
    }

    updateEnablement();

    /* Enable Apply Button on Selection Changes */
    OwlUI.runOnSelection(new Runnable() {
      public void run() {
        updateApplyEnablement(true);
      }
    }, browserGroup);
  }

  private String getDefaultBrowserName() {
    Program program = Program.findProgram("html"); //$NON-NLS-1$
    if (program != null) {
      String name = program.getName();
      if (StringUtils.isSet(name)) {
        name = name.toLowerCase();
        if (name.contains("firefox")) //$NON-NLS-1$
          return "Mozilla Firefox"; //$NON-NLS-1$

        if (name.contains("mozilla")) //$NON-NLS-1$
          return "Mozilla"; //$NON-NLS-1$

        if (name.contains("opera")) //$NON-NLS-1$
          return "Opera"; //$NON-NLS-1$

        if (name.contains("safari")) //$NON-NLS-1$
          return "Safari"; //$NON-NLS-1$

        if (name.contains("chrome")) //$NON-NLS-1$
          return "Google Chrome"; //$NON-NLS-1$

        if (name.equals("html document") && Application.IS_WINDOWS) //$NON-NLS-1$
          return "Internet Explorer"; //$NON-NLS-1$

        if (name.contains("internet explorer") && Application.IS_WINDOWS) //$NON-NLS-1$
          return "Internet Explorer"; //$NON-NLS-1$

        if (name.contains("netscape")) //$NON-NLS-1$
          return "Netscape"; //$NON-NLS-1$

        if (name.contains("konqueror")) //$NON-NLS-1$
          return "Konqueror"; //$NON-NLS-1$

        if (name.contains("camino")) //$NON-NLS-1$
          return "Camino"; //$NON-NLS-1$
      }
    }

    return null;
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
    fGlobalScope.putBoolean(DefaultPreferences.OPEN_LINKS_IN_NEW_TAB, fOpenLinksInNewTab.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.REOPEN_BROWSER_TABS, fReOpenBrowserTabs.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.ALWAYS_REUSE_BROWSER, fAlwaysReuseBrowser.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.OPEN_BROWSER_IN_BACKGROUND, fLoadBrowserTabInBackground.getSelection());
    if (fDisableJavaScriptCheck != null) {
      fGlobalScope.putBoolean(DefaultPreferences.DISABLE_JAVASCRIPT, fDisableJavaScriptCheck.getSelection());
      fDisableJavaScriptExceptionsButton.setEnabled(fDisableJavaScriptCheck.getSelection());
    }
    if (fEnablePopupBlockerCheck != null)
      fGlobalScope.putBoolean(DefaultPreferences.ENABLE_IE_POPUP_BLOCKER, fEnablePopupBlockerCheck.getSelection());

    boolean wasUseDefaultExternalBrowser = fGlobalScope.getBoolean(DefaultPreferences.USE_DEFAULT_EXTERNAL_BROWSER);
    boolean wasUseCustomExternalBrowser = fGlobalScope.getBoolean(DefaultPreferences.USE_CUSTOM_EXTERNAL_BROWSER);
    boolean updateForEmbeddedBrowser = (wasUseDefaultExternalBrowser != fUseDefaultExternalBrowser.getSelection()) || (wasUseCustomExternalBrowser != fUseCustomExternalBrowser.getSelection());

    fGlobalScope.putBoolean(DefaultPreferences.USE_DEFAULT_EXTERNAL_BROWSER, fUseDefaultExternalBrowser.getSelection());
    fGlobalScope.putBoolean(DefaultPreferences.USE_CUSTOM_EXTERNAL_BROWSER, fUseCustomExternalBrowser.getSelection());
    fGlobalScope.putString(DefaultPreferences.CUSTOM_BROWSER_PATH, fCustomBrowserInput.getText());

    if (updateForEmbeddedBrowser)
      EditorUtils.updateToolbarVisibility();

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

    fOpenLinksInNewTab.setSelection(defaultScope.getBoolean(DefaultPreferences.OPEN_LINKS_IN_NEW_TAB));
    fReOpenBrowserTabs.setSelection(defaultScope.getBoolean(DefaultPreferences.REOPEN_BROWSER_TABS));
    fAlwaysReuseBrowser.setSelection(defaultScope.getBoolean(DefaultPreferences.ALWAYS_REUSE_BROWSER));
    fLoadBrowserTabInBackground.setSelection(defaultScope.getBoolean(DefaultPreferences.OPEN_BROWSER_IN_BACKGROUND));
    if (fDisableJavaScriptCheck != null) {
      fDisableJavaScriptCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.DISABLE_JAVASCRIPT));
      fDisableJavaScriptExceptionsButton.setEnabled(fDisableJavaScriptCheck.getSelection());
    }
    if (fEnablePopupBlockerCheck != null)
      fEnablePopupBlockerCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.ENABLE_IE_POPUP_BLOCKER));

    fUseDefaultExternalBrowser.setSelection(defaultScope.getBoolean(DefaultPreferences.USE_DEFAULT_EXTERNAL_BROWSER));
    fUseCustomExternalBrowser.setSelection(defaultScope.getBoolean(DefaultPreferences.USE_CUSTOM_EXTERNAL_BROWSER));
    fUseInternalBrowser.setSelection(!fUseDefaultExternalBrowser.getSelection() && !fUseDefaultExternalBrowser.getSelection());

    fCustomBrowserInput.setEnabled(fUseCustomExternalBrowser.getSelection());
    fCustomBrowserSearchButton.setEnabled(fUseCustomExternalBrowser.getSelection());

    updateEnablement();
    updateApplyEnablement(true);
  }

  private void updateApplyEnablement(boolean enable) {
    Button applyButton = getApplyButton();
    if (applyButton != null && !applyButton.isDisposed() && applyButton.isEnabled() != enable)
      applyButton.setEnabled(enable);
  }

  private void updateEnablement() {
    boolean autoCloseTab = fEclipseScope.getBoolean(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS);
    int autoCloseTabCount = fEclipseScope.getInteger(DefaultPreferences.ECLIPSE_AUTOCLOSE_TABS_THRESHOLD);

    boolean enable = fUseInternalBrowser.getSelection() && (!autoCloseTab || autoCloseTabCount > 1);
    fAlwaysReuseBrowser.setEnabled(enable);
    fLoadBrowserTabInBackground.setEnabled(enable);
    fReOpenBrowserTabs.setEnabled(enable);
    fOpenLinksInNewTab.setEnabled(enable);
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
   */
  @Override
  public void applyData(Object data) {
    if (Boolean.TRUE.equals(data) && fDisableJavaScriptExceptionsButton != null && !fDisableJavaScriptExceptionsButton.isDisposed())
      fDisableJavaScriptExceptionsButton.setFocus();
  }
}