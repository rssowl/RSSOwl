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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.BrowserUtils;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.net.URI;

/**
 * Dialog to accept credentials. Still work in progress!
 * <p>
 * TODO: Add ability to not store credentials permanently.
 * </p>
 *
 * @author bpasero
 */
public class LoginDialog extends TitleAreaDialog {

  /* Divider between Protocol and Host */
  private static final String PROTOCOL_SEPARATOR = "://"; //$NON-NLS-1$

  private final LocalResourceManager fResources;
  private final URI fLink;
  private Text fUsername;
  private Text fPassword;
  private Button fRememberPassword;
  private final ICredentialsProvider fCredProvider;
  private final String fRealm;
  private String fHeader;
  private String fSubline;
  private ImageDescriptor fTitleImageDescriptor;
  private final IPreferenceScope fPreferences;
  private final boolean fIsSyncLogin;

  /**
   * @param parentShell
   * @param link
   * @param realm
   */
  public LoginDialog(Shell parentShell, URI link, String realm) {
    this(parentShell, link, realm, false);
  }

  /**
   * @param parentShell
   * @param link
   * @param realm
   * @param isSyncLogin
   */
  public LoginDialog(Shell parentShell, URI link, String realm, boolean isSyncLogin) {
    super(parentShell);
    fLink = link;
    fRealm = realm;
    fIsSyncLogin = isSyncLogin;
    fResources = new LocalResourceManager(JFaceResources.getResources());
    fCredProvider = Owl.getConnectionService().getCredentialsProvider(link);
    fPreferences = Owl.getPreferenceService().getGlobalScope();
  }

  /**
   * @param header the header message of the dialog.
   */
  public void setHeader(String header) {
    fHeader = header;
  }

  /**
   * @param subline the subline message below the header of the dialog.
   */
  public void setSubline(String subline) {
    fSubline = subline;
  }

  /**
   * @param titleImageDescriptor the image to use in the title.
   */
  public void setTitleImageDescriptor(ImageDescriptor titleImageDescriptor) {
    fTitleImageDescriptor = titleImageDescriptor;
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
    if (buttonId == IDialogConstants.OK_ID) {
      boolean rememberPassword = fIsSyncLogin || fRememberPassword.getSelection();
      if (rememberPassword != fPreferences.getBoolean(DefaultPreferences.REMEMBER_PASSWORD))
        fPreferences.putBoolean(DefaultPreferences.REMEMBER_PASSWORD, rememberPassword);

      final String username = fUsername.getText();
      final String password = fPassword.getText();

      ICredentials credentials = new ICredentials() {
        @Override
        public String getDomain() {
          return null;
        }

        @Override
        public String getPassword() {
          return password;
        }

        @Override
        public String getUsername() {
          return username;
        }
      };

      try {
        if (fCredProvider != null) {

          /* Store for URI */
          if (rememberPassword)
            fCredProvider.setAuthCredentials(credentials, fLink, null);
          else
            fCredProvider.setInMemoryAuthCredentials(credentials, fLink, null);

          /* Also store for Realm */
          if (fRealm != null) {
            if (rememberPassword)
              fCredProvider.setAuthCredentials(credentials, URIUtils.normalizeUri(fLink, true), fRealm);
            else
              fCredProvider.setInMemoryAuthCredentials(credentials, URIUtils.normalizeUri(fLink, true), fRealm);
          }
        }
      } catch (CredentialsException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }

    super.buttonPressed(buttonId);
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    shell.setText(Messages.LoginDialog_FEED_REQUIRES_AUTH);
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
    setTitle(StringUtils.isSet(fHeader) ? fHeader : Messages.LoginDialog_LOGIN);

    /* Title Image */
    if (fTitleImageDescriptor != null)
      setTitleImage(OwlUI.getImage(fResources, fTitleImageDescriptor));
    else
      setTitleImage(OwlUI.getImage(fResources, "icons/wizban/auth.gif")); //$NON-NLS-1$

    /* Title Message */
    if (StringUtils.isSet(fSubline))
      setMessage(fSubline);
    else if (fRealm != null)
      setMessage(NLS.bind(Messages.LoginDialog_ENTER_USER_PW_REALM, fRealm));
    else
      setMessage(Messages.LoginDialog_ENTER_USER_PW);

    /* Spacer */
    new Label(composite, SWT.NONE);

    /* Host to authenticate to */
    Label hostLabel = new Label(composite, SWT.WRAP);
    hostLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Read Protocol */
    StringBuilder hostLabelValue = new StringBuilder();
    if (StringUtils.isSet(fLink.getScheme()))
      hostLabelValue.append(fLink.getScheme()).append(PROTOCOL_SEPARATOR);

    /* Read Host */
    hostLabelValue.append(URIUtils.safeGetHost(fLink));

    /* Show Value */
    hostLabel.setText(hostLabelValue.toString());

    /* Username Label */
    Label usernameLabel = new Label(composite, SWT.NONE);
    usernameLabel.setText(Messages.LoginDialog_USERNAME);
    usernameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    /* Username input field */
    fUsername = new Text(composite, SWT.SINGLE | SWT.BORDER);
    fUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fUsername.setFocus();

    /* Password Label */
    Label passwordLabel = new Label(composite, SWT.NONE);
    passwordLabel.setText(Messages.LoginDialog_PASSWORD);
    passwordLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

    /* Password input field */
    fPassword = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
    fPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    /* Spacer */
    if (!fIsSyncLogin) {
      new Label(composite, SWT.NONE);

      /* Remember Password */
      fRememberPassword = new Button(composite, SWT.CHECK);
      fRememberPassword.setText(Messages.LoginDialog_REMEMBER_PASSWORD);
      fRememberPassword.setSelection(fPreferences.getBoolean(DefaultPreferences.REMEMBER_PASSWORD));
      fRememberPassword.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      ((GridData) fRememberPassword.getLayoutData()).verticalIndent = 5;
    }

    /* Separator */
    Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true));

    /* Try loading Credentials from Platform if available */
    preload();

    applyDialogFont(composite);

    return composite;
  }

  /*
   * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {

    /* Inject Link if showing Sync Login */
    if (fIsSyncLogin) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(2, false);
      layout.marginLeft = 5;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      layout.horizontalSpacing = 0;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
      composite.setFont(parent.getFont());
      if (isHelpAvailable()) {
        Control helpControl = createHelpControl(composite);
        ((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      }

      Link createAccountLink = new Link(composite, SWT.None);
      createAccountLink.setText(Messages.LoginDialog_CREATE_NEW_ACCOUNT);
      createAccountLink.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
      createAccountLink.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          BrowserUtils.openLinkExternal(SyncUtils.GOOGLE_NEW_ACCOUNT_URL);
        }
      });

      Control buttonSection = super.createButtonBar(composite);
      ((GridData) buttonSection.getLayoutData()).grabExcessHorizontalSpace = true;

      return composite;
    }

    /* Otherwise return super */
    return super.createButtonBar(parent);
  }

  private void preload() {
    ICredentials authCredentials = null;
    try {

      /* First try with full URI */
      if (fCredProvider != null)
        authCredentials = fCredProvider.getAuthCredentials(fLink, fRealm);

      /* Second try with Host / Port / Realm */
      if (fCredProvider != null && fRealm != null && authCredentials == null)
        authCredentials = fCredProvider.getAuthCredentials(URIUtils.normalizeUri(fLink, true), fRealm);
    } catch (CredentialsException e) {
      Activator.getDefault().getLog().log(e.getStatus());
    }

    if (authCredentials != null) {
      String username = authCredentials.getUsername();
      String password = authCredentials.getPassword();

      if (StringUtils.isSet(username)) {
        fUsername.setText(username);
        fUsername.selectAll();
      }

      if (StringUtils.isSet(password))
        fPassword.setText(password);
    }
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