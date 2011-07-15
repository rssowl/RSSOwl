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
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Overview preferences page with links to other pages.
 *
 * @author bpasero
 */
public class OverviewPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
  private LocalResourceManager fResources;

  /** ID of this Preference Page */
  public static final String ID = "org.eclipse.ui.preferencePages.Workbench"; //$NON-NLS-1$

  /** Leave for reflection */
  public OverviewPreferencesPage() {
    fResources = new LocalResourceManager(JFaceResources.getResources());
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
    noDefaultAndApplyButton();
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
  @SuppressWarnings("restriction")
  @Override
  protected Control createContents(Composite parent) {
    final IWorkbenchPreferenceContainer preferences = (IWorkbenchPreferenceContainer) getContainer();

    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    container.setLayout(layout);
    container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
    container.setFont(parent.getFont());

    Label titleLabel = new Label(container, SWT.None);
    titleLabel.setText(Messages.OverviewPreferencesPage_OVERVIEW_INFO);
    titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

    List<String> ids = new ArrayList<String>();
    List<Image> images = new ArrayList<Image>();
    List<String> labels = new ArrayList<String>();

    Composite linkContainer = new Composite(container, SWT.NONE);
    linkContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    linkContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 15, 8, false));
    ((GridLayout) linkContainer.getLayout()).marginTop = 10;

    /* Feeds */
    ids.add(FeedsPreferencePage.ID);
    images.add(OwlUI.getImage(fResources, OwlUI.BOOKMARK));
    labels.add(Messages.OverviewPreferencesPage_FEED_OPTIONS);

    /* Browser */
    ids.add(BrowserPreferencePage.ID);
    images.add(OwlUI.getImage(fResources, "icons/elcl16/browser.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_BROWSER_OPTIONS);

    /* Sharing */
    ids.add(SharingPreferencesPage.ID);
    images.add(OwlUI.getImage(fResources, "icons/elcl16/share.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_SHARE_OPTIONS);

    /* Key Bindings */
    ids.add("org.rssowl.ui.preferences.Keys"); //$NON-NLS-1$
    images.add(OwlUI.getImage(fResources, "icons/elcl16/keyspref.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_KEYS_OPTIONS);

    /* View */
    ids.add(MiscPreferencePage.ID);
    images.add(OwlUI.getImage(fResources, "icons/elcl16/view.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_VIEW_OPTIONS);

    /* Colors and Fonts */
    ids.add("org.rssowl.ui.preferences.ColorsAndFonts"); //$NON-NLS-1$
    images.add(OwlUI.getImage(fResources, "icons/elcl16/colors.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_COLOR_FONT_OPTIONS);

    /* Network */
    ids.add("org.eclipse.ui.net.NetPreferences"); //$NON-NLS-1$
    images.add(OwlUI.getImage(fResources, "icons/elcl16/network.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_CONNECTION_OPTIONS);

    /* Notifier */
    ids.add(NotifierPreferencesPage.ID);
    images.add(OwlUI.getImage(fResources, "icons/elcl16/notification.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_NOTIFIER_OPTIONS);

    /* Labels */
    ids.add(ManageLabelsPreferencePage.ID);
    images.add(OwlUI.getImage(fResources, "icons/elcl16/labels.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_LABEL_OPTIONS);

    /* Passwords */
    ids.add(CredentialsPreferencesPage.ID);
    images.add(OwlUI.getImage(fResources, "icons/elcl16/passwords.gif")); //$NON-NLS-1$
    labels.add(Messages.OverviewPreferencesPage_PASSWORD_OPTIONS);

    /* Create */
    for (int i = 0; i < ids.size(); i++) {
      final String id = ids.get(i);

      Label imgLabel = new Label(linkContainer, SWT.None);
      imgLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
      imgLabel.setImage(images.get(i));

      Link link = new Link(linkContainer, SWT.None);
      link.setText("<a>" + labels.get(i) + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
      link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
      link.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          preferences.openPage(id, null);
        }
      });
    }

    /* Search Info Container */
    Composite infoContainer = new Composite(container, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.END, true, true, 2, 1));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    ((GridLayout) infoContainer.getLayout()).marginBottom = 10;

    Label infoImg = new Label(infoContainer, SWT.NONE);
    infoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/info.gif")); //$NON-NLS-1$
    infoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    Label infoText = new Label(infoContainer, SWT.WRAP);
    infoText.setText(Messages.OverviewPreferencesPage_OVERVIEW_TIP);
    infoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    applyDialogFont(container);

    /* Reveal the Items below the Overview Node for better Usability */
    if (!Application.IS_ECLIPSE)
      ((org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog) preferences).getTreeViewer().expandAll();

    return container;
  }
}