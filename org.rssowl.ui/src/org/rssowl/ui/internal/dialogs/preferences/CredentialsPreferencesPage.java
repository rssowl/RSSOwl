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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rssowl.core.Owl;
import org.rssowl.core.connection.CredentialsException;
import org.rssowl.core.connection.IConnectionService;
import org.rssowl.core.connection.ICredentials;
import org.rssowl.core.connection.ICredentialsProvider;
import org.rssowl.core.connection.PlatformCredentialsProvider;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.AddCredentialsDialog;
import org.rssowl.ui.internal.dialogs.ConfirmDialog;
import org.rssowl.ui.internal.util.CColumnLayoutData;
import org.rssowl.ui.internal.util.CTable;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Preferences page to manage stored credentials for bookmarks.
 *
 * @author bpasero
 */
public class CredentialsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

  /** ID of the Page */
  public static String ID = "org.rssowl.ui.CredentialsPreferences"; //$NON-NLS-1$

  /* Dummy for creating and changing the master password */
  private static final String DUMMY_LINK = "http://www.rssowl.org"; //$NON-NLS-1$

  private IConnectionService fConService = Owl.getConnectionService();
  private TableViewer fViewer;
  private Button fAddCredentials;
  private Button fRemoveAll;
  private Button fRemoveSelected;
  private Button fUseMasterPasswordCheck;
  private IPreferenceScope fGlobalScope = Owl.getPreferenceService().getGlobalScope();
  private Button fChangeMasterPassword;
  private Button fResetMasterPassword;
  private boolean fIsError = false;

  /* Model used in the Viewer */
  private static class CredentialsModelData {
    private URI fNormalizedLink;
    private String fRealm;
    private String fUsername;
    private String fPassword;

    CredentialsModelData(String username, String password, URI normalizedLink, String realm) {
      fUsername = username;
      fPassword = password;
      fNormalizedLink = normalizedLink;
      fRealm = realm;
    }

    String getUsername() {
      return fUsername;
    }

    URI getNormalizedLink() {
      return fNormalizedLink;
    }

    String getRealm() {
      return fRealm;
    }

    ICredentials toCredentials() {
      return new ICredentials() {
        public String getDomain() {
          return ""; //$NON-NLS-1$
        }

        public String getPassword() {
          return fPassword;
        }

        public String getUsername() {
          return fUsername;
        }
      };
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fNormalizedLink == null) ? 0 : fNormalizedLink.hashCode());
      result = prime * result + ((fRealm == null) ? 0 : fRealm.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;

      if (obj == null)
        return false;

      if (getClass() != obj.getClass())
        return false;

      CredentialsModelData other = (CredentialsModelData) obj;
      if (fNormalizedLink == null) {
        if (other.fNormalizedLink != null)
          return false;
      } else if (!fNormalizedLink.equals(other.fNormalizedLink))
        return false;

      if (fRealm == null) {
        if (other.fRealm != null)
          return false;
      } else if (!fRealm.equals(other.fRealm))
        return false;

      return true;
    }
  }

  /** Leave for reflection */
  public CredentialsPreferencesPage() {
    setImageDescriptor(OwlUI.getImageDescriptor("icons/elcl16/passwords.gif")); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {}

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = createComposite(parent);

    /* Use a master password */
    if (!Application.IS_ECLIPSE) {
      Composite masterContainer = new Composite(container, SWT.NONE);
      masterContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
      masterContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0));
      ((GridLayout) masterContainer.getLayout()).marginBottom = 15;
      ((GridLayout) masterContainer.getLayout()).verticalSpacing = 10;

      Label infoText = new Label(masterContainer, SWT.WRAP);
      infoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
      ((GridData) infoText.getLayoutData()).widthHint = 200;
      if (Application.IS_WINDOWS || Application.IS_MAC)
        infoText.setText(Messages.CredentialsPreferencesPage_MASTER_PW_INFO);
      else
        infoText.setText(Messages.CredentialsPreferencesPage_MASTER_PW_MSG);

      /* Use Own Master Password */
      fUseMasterPasswordCheck = new Button(masterContainer, SWT.CHECK);
      fUseMasterPasswordCheck.setText(Messages.CredentialsPreferencesPage_USE_MASTER_PW);
      fUseMasterPasswordCheck.setFocus();
      fUseMasterPasswordCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      fUseMasterPasswordCheck.setSelection(fGlobalScope.getBoolean(DefaultPreferences.USE_MASTER_PASSWORD));
      fUseMasterPasswordCheck.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (!fUseMasterPasswordCheck.getSelection())
            fChangeMasterPassword.setEnabled(false);
        }
      });

      /* Change Own Master Password */
      fChangeMasterPassword = new Button(masterContainer, SWT.PUSH);
      fChangeMasterPassword.setEnabled(fUseMasterPasswordCheck.getSelection());
      fChangeMasterPassword.setText(Messages.CredentialsPreferencesPage_CHANGE_MASTER_PW);
      Dialog.applyDialogFont(fChangeMasterPassword);
      setButtonLayoutData(fChangeMasterPassword);
      fChangeMasterPassword.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onChangeMasterPassword();
        }
      });

      /* Reset Master Password */
      fResetMasterPassword = new Button(masterContainer, SWT.PUSH);
      fResetMasterPassword.setEnabled(false);
      fResetMasterPassword.setText(Messages.CredentialsPreferencesPage_RESET);
      Dialog.applyDialogFont(fResetMasterPassword);
      setButtonLayoutData(fResetMasterPassword);
      fResetMasterPassword.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onResetMasterPassword();
        }
      });
    }

    /* Label */
    Label infoLabel = new Label(container, SWT.NONE);
    infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    infoLabel.setText(Messages.CredentialsPreferencesPage_SAVED_PWS);

    /* Viewer to display Passwords */
    int style = SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER;

    CTable customTable = new CTable(container, style);
    customTable.getControl().setHeaderVisible(true);

    fViewer = new TableViewer(customTable.getControl());
    fViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    ((GridData) fViewer.getTable().getLayoutData()).heightHint = 190;
    fViewer.getControl().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    /* Create Columns */
    TableViewerColumn col = new TableViewerColumn(fViewer, SWT.LEFT);
    customTable.manageColumn(col.getColumn(), new CColumnLayoutData(CColumnLayoutData.Size.FILL, 45), Messages.CredentialsPreferencesPage_SITE, null, null, false, true);
    col.getColumn().setMoveable(false);

    col = new TableViewerColumn(fViewer, SWT.LEFT);
    customTable.manageColumn(col.getColumn(), new CColumnLayoutData(CColumnLayoutData.Size.FILL, 30), Messages.CredentialsPreferencesPage_REALM, null, null, false, true);
    col.getColumn().setMoveable(false);

    col = new TableViewerColumn(fViewer, SWT.LEFT);
    customTable.manageColumn(col.getColumn(), new CColumnLayoutData(CColumnLayoutData.Size.FILL, 25), Messages.CredentialsPreferencesPage_USERNAME, null, null, false, true);
    col.getColumn().setMoveable(false);

    /* Content Provider */
    fViewer.setContentProvider(new IStructuredContentProvider() {
      public Object[] getElements(Object inputElement) {
        Set<CredentialsModelData> credentials = loadCredentials();
        return credentials.toArray();
      }

      public void dispose() {}

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    });

    /* Label Provider */
    fViewer.setLabelProvider(new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        CredentialsModelData data = (CredentialsModelData) cell.getElement();

        switch (cell.getColumnIndex()) {
          case 0:
            String link = data.getNormalizedLink().toString();
            if (SyncUtils.GOOGLE_LOGIN_URL.equals(link))
              cell.setText("Google Reader"); //$NON-NLS-1$
            else
              cell.setText(link);
            break;

          case 1:
            cell.setText(data.getRealm());
            break;

          case 2:
            cell.setText(data.getUsername());
            break;
        }
      }
    });

    /* Sorter */
    fViewer.setSorter(new ViewerSorter() {
      @Override
      public int compare(Viewer viewer, Object e1, Object e2) {
        CredentialsModelData data1 = (CredentialsModelData) e1;
        CredentialsModelData data2 = (CredentialsModelData) e2;

        return data1.getNormalizedLink().toString().compareTo(data2.getNormalizedLink().toString());
      }
    });

    /* Buttons Container */
    Composite buttonContainer = new Composite(container, SWT.None);
    buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    buttonContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0));

    /* Offer Button to add Credentials */
    fAddCredentials = new Button(buttonContainer, SWT.PUSH);
    fAddCredentials.setText(Messages.CredentialsPreferencesPage_ADD);
    fAddCredentials.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onAdd();
      }
    });

    /* Offer Buttons to remove Credentials */
    fRemoveSelected = new Button(buttonContainer, SWT.PUSH);
    fRemoveSelected.setText(Messages.CredentialsPreferencesPage_REMOVE);
    fRemoveSelected.setEnabled(false);
    fRemoveSelected.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onRemove();
      }
    });

    fRemoveAll = new Button(buttonContainer, SWT.PUSH);
    fRemoveAll.setText(Messages.CredentialsPreferencesPage_REMOVE_ALL);
    fRemoveAll.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onRemoveAll();
      }
    });

    Dialog.applyDialogFont(fAddCredentials);
    Dialog.applyDialogFont(fRemoveSelected);
    Dialog.applyDialogFont(fRemoveAll);
    setButtonLayoutData(fAddCredentials);
    setButtonLayoutData(fRemoveSelected);
    setButtonLayoutData(fRemoveAll);
    ((GridData) fRemoveAll.getLayoutData()).grabExcessHorizontalSpace = false;
    ((GridData) fRemoveAll.getLayoutData()).horizontalAlignment = SWT.BEGINNING;

    /* Set Dummy Input */
    fViewer.setInput(new Object());
    fRemoveAll.setEnabled(fViewer.getTable().getItemCount() > 0);

    /* Listen to Selection Changes */
    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        fRemoveSelected.setEnabled(!fViewer.getSelection().isEmpty());
      }
    });

    applyDialogFont(container);

    /* Enable Apply Button on Selection Changes */
    OwlUI.runOnSelection(new Runnable() {
      public void run() {
        updateApplyEnablement(true);
      }
    }, container);

    return container;
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);

    if (getDefaultsButton() != null && fIsError)
      getDefaultsButton().setEnabled(false);

    updateApplyEnablement(false);
  }

  private void onAdd() {
    AddCredentialsDialog dialog = new AddCredentialsDialog(getShell());
    if (dialog.open() == IDialogConstants.OK_ID) {
      String site = dialog.getSite();
      final String username = dialog.getUsername();
      final String password = dialog.getPassword();

      try {
        URI siteUri = new URI(site);
        ICredentialsProvider credentialsProvider = Owl.getConnectionService().getCredentialsProvider(siteUri);
        if (credentialsProvider != null) {
          credentialsProvider.setAuthCredentials(new ICredentials() {
            public String getUsername() {
              return username;
            }

            public String getPassword() {
              return password;
            }

            public String getDomain() {
              return null;
            }
          }, siteUri, null);

          fViewer.refresh();
          fRemoveSelected.setEnabled(!fViewer.getSelection().isEmpty());
          fRemoveAll.setEnabled(fViewer.getTable().getItemCount() > 0);
        }
      } catch (URISyntaxException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (CredentialsException e) {
        Activator.getDefault().logError(e.getMessage(), e);
        setShowError(true);
      }
    }
  }

  private void onChangeMasterPassword() {
    reSetAllCredentials();
  }

  private void onResetMasterPassword() {

    /* Ask for Confirmation */
    ConfirmDialog dialog = new ConfirmDialog(getShell(), Messages.CredentialsPreferencesPage_CONFIRM_RESET, Messages.CredentialsPreferencesPage_NO_UNDO, Messages.CredentialsPreferencesPage_RESET_PASSWORDS, Messages.CredentialsPreferencesPage_RESET_TITLE, null) {
      @Override
      public void setTitle(String newTitle) {
        super.setTitle(Messages.CredentialsPreferencesPage_RESET_TITLE);
      }
    };

    /* Bring Back Security to Default State resetting everything */
    if (dialog.open() == IDialogConstants.OK_ID) {
      ICredentialsProvider provider = Owl.getConnectionService().getCredentialsProvider(URI.create(DUMMY_LINK));
      ((PlatformCredentialsProvider) provider).clear();
      reSetAllCredentials();
      setShowError(false);

      if (!Application.IS_ECLIPSE)
        fResetMasterPassword.setEnabled(false);
    }
  }

  private void onRemove() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    List<?> credentialsToRemove = selection.toList();
    for (Object obj : credentialsToRemove) {
      CredentialsModelData data = (CredentialsModelData) obj;
      remove(data, false);
    }

    /* Update in UI */
    fViewer.refresh();
    fRemoveSelected.setEnabled(!fViewer.getSelection().isEmpty());
    fRemoveAll.setEnabled(fViewer.getTable().getItemCount() > 0);
  }

  private void onRemoveAll() {

    /* Ask for Confirmation first */
    ConfirmDialog dialog = new ConfirmDialog(getShell(), Messages.CredentialsPreferencesPage_CONFIRM_REMOVE, Messages.CredentialsPreferencesPage_NO_UNDO, Messages.CredentialsPreferencesPage_REMOVE_ALL_CONFIRM, null);
    if (dialog.open() != IDialogConstants.OK_ID)
      return;

    Set<CredentialsModelData> credentials = loadCredentials();
    for (CredentialsModelData data : credentials) {
      remove(data, true);
    }

    /* Update in UI */
    fViewer.refresh();
    fRemoveSelected.setEnabled(!fViewer.getSelection().isEmpty());
    fRemoveAll.setEnabled(fViewer.getTable().getItemCount() > 0);
  }

  private void remove(CredentialsModelData data, boolean all) {

    /* Remove normalized link and realm */
    ICredentialsProvider provider = fConService.getCredentialsProvider(data.getNormalizedLink());
    if (provider != null) {
      try {
        provider.deleteAuthCredentials(data.getNormalizedLink(), data.getRealm());
      } catch (CredentialsException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    /* Remove all other stored Credentials matching normalized link and realm if set */
    if (all) {
      Collection<IBookMark> bookmarks = DynamicDAO.loadAll(IBookMark.class);
      for (IBookMark bookmark : bookmarks) {
        String realm = (String) bookmark.getProperty(Controller.BM_REALM_PROPERTY);

        URI feedLink = bookmark.getFeedLinkReference().getLink();
        URI normalizedLink = URIUtils.normalizeUri(feedLink, true);

        /*
         * If realm is null, then this bookmark successfully loaded due to another bookmark
         * that the user successfully authenticated to. If the realm is not null, then we
         * have to compare the realm to ensure that no credentials from the same host but
         * a different realm gets removed.
         */
        if ((realm == null || realm.equals(data.getRealm())) && normalizedLink.equals(data.getNormalizedLink())) {
          provider = fConService.getCredentialsProvider(feedLink);
          if (provider != null) {
            try {
              provider.deleteAuthCredentials(feedLink, null); //Null as per contract in DefaultProtocolHandler
              bookmark.removeProperty(Controller.BM_REALM_PROPERTY);
            } catch (CredentialsException e) {
              Activator.getDefault().logError(e.getMessage(), e);
            }
          }
        }
      }
    }
  }

  private Set<CredentialsModelData> loadCredentials() {
    Set<CredentialsModelData> credentials = new HashSet<CredentialsModelData>();

    /* Add all Feeds */
    List<Pair<URI, String>> pairs = new ArrayList<Pair<URI, String>>();
    Collection<IBookMark> bookmarks = DynamicDAO.loadAll(IBookMark.class);
    for (IBookMark bookmark : bookmarks) {
      String realm = (String) bookmark.getProperty(Controller.BM_REALM_PROPERTY);
      URI feedLink = bookmark.getFeedLinkReference().getLink();

      pairs.add(Pair.create(feedLink, realm));
    }

    /* Also add Google Reader Login */
    pairs.add(Pair.create(URI.create(SyncUtils.GOOGLE_LOGIN_URL), (String) null));

    for (Pair<URI, String> pair : pairs) {
      URI feedLink = pair.getFirst();
      String realm = pair.getSecond();
      URI normalizedLink = URIUtils.normalizeUri(feedLink, true);

      try {

        /* First try Normalized Link with Realm */
        CredentialsModelData data = loadCredentials(normalizedLink, realm);
        if (data != null)
          credentials.add(data);

        /* Then try actual Feed Link with Realm */
        data = loadCredentials(feedLink, realm);
        if (data != null)
          credentials.add(data);

        /* Then try actual Feed Link without Realm */
        data = loadCredentials(feedLink, null);
        if (data != null)
          credentials.add(data);
      } catch (CredentialsException e) {
        Activator.getDefault().logError(e.getMessage(), e);
        setShowError(true);
        break;
      }
    }

    return credentials;
  }

  private CredentialsModelData loadCredentials(URI link, String realm) throws CredentialsException {
    if (StringUtils.isSet(link.getScheme())) {
      ICredentialsProvider credentialsProvider = fConService.getCredentialsProvider(link);
      if (credentialsProvider != null) {
        ICredentials authCredentials = credentialsProvider.getPersistedAuthCredentials(link, realm);
        if (authCredentials != null)
          return new CredentialsModelData(authCredentials.getUsername(), authCredentials.getPassword(), link, realm);
      }
    }

    return null;
  }

  private void setShowError(boolean isError) {
    fIsError = isError;

    if (isError)
      setErrorMessage(Messages.CredentialsPreferencesPage_WRONG_MASTER_PW);
    else
      setErrorMessage(null);

    if (!Application.IS_ECLIPSE) {
      fUseMasterPasswordCheck.setEnabled(!isError);
      fChangeMasterPassword.setEnabled(!isError && fUseMasterPasswordCheck.getSelection());
      fResetMasterPassword.setEnabled(isError);
    }

    fViewer.getTable().setEnabled(!isError);
    fAddCredentials.setEnabled(!isError);

    if (getDefaultsButton() != null)
      getDefaultsButton().setEnabled(!isError);

    if (getApplyButton() != null)
      getApplyButton().setEnabled(!isError);
  }

  private Composite createComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout(2, false);
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
    if (!Application.IS_ECLIPSE) {
      boolean oldUseMasterPassword = fGlobalScope.getBoolean(DefaultPreferences.USE_MASTER_PASSWORD);
      boolean newUseMasterPassword = fUseMasterPasswordCheck.getSelection();

      fGlobalScope.putBoolean(DefaultPreferences.USE_MASTER_PASSWORD, fUseMasterPasswordCheck.getSelection());

      /*
       * Hack: There does not seem to be any API to update the stored credentials in Equinox Secure Storage.
       * In order to enable/disable the master password, the workaround is to save all known credentials again.
       * The provider will automatically prompt for the new master password to use for the credentials.
       */
      if (oldUseMasterPassword != newUseMasterPassword)
        reSetAllCredentials();
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

    if (!Application.IS_ECLIPSE)
      fChangeMasterPassword.setEnabled(fUseMasterPasswordCheck.getSelection());
  }

  /*
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults() {
    super.performDefaults();

    if (!Application.IS_ECLIPSE) {
      IPreferenceScope defaultScope = Owl.getPreferenceService().getDefaultScope();
      fUseMasterPasswordCheck.setSelection(defaultScope.getBoolean(DefaultPreferences.USE_MASTER_PASSWORD));
      fChangeMasterPassword.setEnabled(fUseMasterPasswordCheck.getSelection());
    }

    updateApplyEnablement(true);
  }

  private void reSetAllCredentials() {
    boolean clearedOnce = false; // Implementation Detail of PlatformCredentialsProvider
    Set<CredentialsModelData> credentials = loadCredentials();

    /* Add Dummy Credentials if no credentials present */
    CredentialsModelData dummyCredentials = null;
    if (credentials.isEmpty()) {
      try {
        dummyCredentials = new CredentialsModelData("", "", new URI(DUMMY_LINK), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        credentials.add(dummyCredentials);
      } catch (URISyntaxException e) {
        /* Should not happen */
      }
    }

    /* Write all Credentials into credential provider again */
    for (CredentialsModelData credential : credentials) {
      ICredentialsProvider credentialsProvider = Owl.getConnectionService().getCredentialsProvider(credential.fNormalizedLink);
      if (credentialsProvider != null) {

        /* Implementation Detail: Need to clear PlatformCredentialsProvider once if provided */
        if (!clearedOnce && credentialsProvider instanceof PlatformCredentialsProvider) {
          ((PlatformCredentialsProvider) credentialsProvider).clear();
          clearedOnce = true;
        }

        try {
          credentialsProvider.setAuthCredentials(credential.toCredentials(), credential.fNormalizedLink, credential.fRealm);
        } catch (CredentialsException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }
    }

    /* Delete Dummy Credentials Again */
    if (dummyCredentials != null) {
      ICredentialsProvider credentialsProvider = Owl.getConnectionService().getCredentialsProvider(dummyCredentials.fNormalizedLink);
      if (credentialsProvider != null) {
        try {
          credentialsProvider.deleteAuthCredentials(dummyCredentials.fNormalizedLink, dummyCredentials.fRealm);
        } catch (CredentialsException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }
    }
  }

  private void updateApplyEnablement(boolean enable) {
    if (fIsError)
      enable = false;

    Button applyButton = getApplyButton();
    if (applyButton != null && !applyButton.isDisposed() && applyButton.isEnabled() != enable)
      applyButton.setEnabled(enable);
  }
}