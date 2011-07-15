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

package org.rssowl.ui.internal.filter;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Link;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.filter.INewsActionPresentation;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.File;

/**
 * An implementation of {@link INewsActionPresentation} to select a folder to
 * download attachments to.
 *
 * @author bpasero
 */
public class DownloadAttachmentsNewsActionPresentation implements INewsActionPresentation {
  private Link fFolderPathLink;
  private Composite fContainer;

  /*
   * @see org.rssowl.ui.filter.INewsActionPresentation#create(org.eclipse.swt.widgets.Composite, java.lang.Object)
   */
  public void create(Composite parent, Object data) {
    fContainer = new Composite(parent, SWT.NONE);
    fContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));
    ((GridLayout) fContainer.getLayout()).marginLeft = 5;
    fContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    fFolderPathLink = new Link(fContainer, SWT.WRAP);
    fFolderPathLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    ((GridData) fFolderPathLink.getLayoutData()).widthHint = 100;
    updateLink(data);

    fFolderPathLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onSelect();
      }
    });
  }

  private void onSelect() {
    IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();

    DirectoryDialog dialog = new DirectoryDialog(fFolderPathLink.getShell(), SWT.None);
    dialog.setText(Messages.DownloadAttachmentsNewsActionPresentation_SELECT_FOLDER);

    /* Preset with existing folder if present */
    if (fFolderPathLink.getData() != null) {
      File file = new File(fFolderPathLink.getData().toString());
      if (file.exists())
        dialog.setFilterPath(file.toString());
    }

    /* Preset with a good Location from Preferences if present */
    if (!StringUtils.isSet(dialog.getFilterPath())) {
      String downloadFolder = preferences.getString(DefaultPreferences.DOWNLOAD_FOLDER);
      if (StringUtils.isSet(downloadFolder) && new File(downloadFolder).exists())
        dialog.setFilterPath(downloadFolder);
    }

    String folderPath = dialog.open();
    if (StringUtils.isSet(folderPath)) {
      updateLink(folderPath);

      /* Link might require more space now */
      fFolderPathLink.getShell().layout(true, true);

      /* Remember Download Folder in Settings */
      preferences.putString(DefaultPreferences.DOWNLOAD_FOLDER, folderPath);
    }
  }

  private void updateLink(Object data) {
    if (data == null) {
      resetLink();
    } else {
      File file = new File(data.toString());
      if (file.exists()) {
        fFolderPathLink.setText(NLS.bind(Messages.DownloadAttachmentsNewsActionPresentation_TO_N, file.getAbsolutePath()));
        fFolderPathLink.setData(data);
      } else
        resetLink();
    }
  }

  private void resetLink() {
    fFolderPathLink.setText(Messages.DownloadAttachmentsNewsActionPresentation_TO_SELECT_FOLDER);
    fFolderPathLink.setData(null);
  }

  /*
   * @see org.rssowl.ui.filter.INewsActionPresentation#dispose()
   */
  public void dispose() {
    fContainer.dispose();
  }

  /*
   * @see org.rssowl.ui.filter.INewsActionPresentation#getData()
   */
  public Object getData() {
    return fFolderPathLink.getData();
  }
}