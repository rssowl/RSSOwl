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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.SyncUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * A dialog to add credentials for a website.
 *
 * @author bpasero
 */
public class AddCredentialsDialog extends Dialog {
  private Text fSiteInput;
  private Text fUsernameInput;
  private Text fPasswordInput;
  private String fSiteValue;
  private String fUsernameValue;
  private String fPasswordValue;
  private Set<String> fFeedLinks;
  private Label fInfoImg;
  private Label fInfoText;
  private ResourceManager fResources = new LocalResourceManager(JFaceResources.getResources());

  /**
   * @param parentShell
   */
  public AddCredentialsDialog(Shell parentShell) {
    super(parentShell);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#close()
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
    if (fFeedLinks == null || !fFeedLinks.contains(fSiteInput.getText())) {
      showError();
      fSiteInput.setFocus();
      fSiteInput.selectAll();
      return;
    }

    fSiteValue = fSiteInput.getText();
    fUsernameValue = fUsernameInput.getText();
    fPasswordValue = fPasswordInput.getText();

    super.okPressed();
  }

  /**
   * @return the site or <code>null</code> if the user cancelled the dialog.
   */
  public String getSite() {
    return fSiteValue;
  }

  /**
   * @return the username or <code>null</code> if the user cancelled the dialog.
   */
  public String getUsername() {
    return fUsernameValue;
  }

  /**
   * @return the password or <code>null</code> if the user cancelled the dialog.
   */
  public String getPassword() {
    return fPasswordValue;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 10, 10));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Site */
    Label siteLabel = new Label(composite, SWT.None);
    siteLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    siteLabel.setText(Messages.AddCredentialsDialog_LINK);

    fSiteInput = new Text(composite, SWT.BORDER | SWT.SINGLE);
    fSiteInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fSiteInput.setText(URIUtils.HTTP);
    fSiteInput.setSelection(URIUtils.HTTP.length());
    fSiteInput.setFocus();
    fSiteInput.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        updateOkButton();
      }
    });

    /* Show UI Hint for extra information is available */
    final Pair<SimpleContentProposalProvider, ContentProposalAdapter> pair = OwlUI.hookAutoComplete(fSiteInput, null, true, false);

    /* Load proposals in the Background */
    JobRunner.runInBackgroundThread(100, new Runnable() {
      @Override
      public void run() {
        if (!fSiteInput.isDisposed()) {
          Set<String> values = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
              return o1.compareToIgnoreCase(o2);
            }
          });

          values.addAll(CoreUtils.getFeedLinks());
          values.add(SyncUtils.GOOGLE_LOGIN_URL);

          /* Remember for Validation */
          fFeedLinks = new HashSet<String>(values);

          /* Apply Proposals */
          if (!fSiteInput.isDisposed())
            OwlUI.applyAutoCompleteProposals(values, pair.getFirst(), pair.getSecond(), false);
        }
      }
    });

    /* Username */
    Label usernameLabel = new Label(composite, SWT.None);
    usernameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    usernameLabel.setText(Messages.AddCredentialsDialog_USERNAME);

    fUsernameInput = new Text(composite, SWT.BORDER | SWT.SINGLE);
    fUsernameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fUsernameInput.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        updateOkButton();
      }
    });

    /* Password */
    Label passwordLabel = new Label(composite, SWT.None);
    passwordLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    passwordLabel.setText(Messages.AddCredentialsDialog_PASSWORD);

    fPasswordInput = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
    fPasswordInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fPasswordInput.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        updateOkButton();
      }
    });

    /* Info Container */
    Composite infoContainer = new Composite(composite, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    ((GridLayout) infoContainer.getLayout()).marginTop = 15;

    fInfoImg = new Label(infoContainer, SWT.NONE);
    fInfoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    fInfoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/info.gif")); //$NON-NLS-1$

    fInfoText = new Label(infoContainer, SWT.WRAP);
    fInfoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fInfoText.setText(Messages.AddCredentialsDialog_ENTER_EXISTING_FEED_LINK);

    applyDialogFont(composite);

    return composite;
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.AddCredentialsDialog_ADD_PASSWORD);
  }

  private void updateOkButton() {
    getButton(IDialogConstants.OK_ID).setEnabled(StringUtils.isSet(fSiteInput.getText()) && (StringUtils.isSet(fUsernameInput.getText()) || StringUtils.isSet(fPasswordInput.getText())));
  }

  private void showError() {
    fInfoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/error.gif")); //$NON-NLS-1$
    fInfoImg.getParent().getParent().layout();
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | getDefaultOrientation();

    return style;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {

    /* Spacer */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

    Control control = super.createButtonBar(parent);

    /* Udate enablement */
    getButton(IDialogConstants.OK_ID).setEnabled(false);

    return control;
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