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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Link;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.filter.INewsActionPresentation;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.File;

/**
 * An implementation of {@link INewsActionPresentation} to configure the path to
 * the growlnotify executable used to show news in Growl.
 *
 * @author bpasero
 */
public class ShowGrowlActionPresentation implements INewsActionPresentation {
  private static final String DEFAULT_GROWLNOTIFY_LOCATION = "/usr/local/bin/growlnotify"; //$NON-NLS-1$
  private Link fGrowlPathLink;
  private Composite fContainer;

  /*
   * @see org.rssowl.ui.filter.INewsActionPresentation#create(org.eclipse.swt.widgets.Composite, java.lang.Object)
   */
  public void create(Composite parent, Object data) {
    fContainer = new Composite(parent, SWT.NONE);
    fContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));
    ((GridLayout) fContainer.getLayout()).marginLeft = 5;
    fContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    fGrowlPathLink = new Link(fContainer, SWT.WRAP);
    fGrowlPathLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    ((GridData) fGrowlPathLink.getLayoutData()).widthHint = 100;
    updateLink(data);

    fGrowlPathLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onSelect();
      }
    });
  }

  private void onSelect() {
    FileDialog dialog = new FileDialog(fGrowlPathLink.getShell(), SWT.OPEN);
    dialog.setText(Messages.ShowGrowlActionPresentation_SELECT_GROWL_TITLE);

    /* Preset with existing folder if present */
    if (fGrowlPathLink.getData() != null) {
      File file = new File(fGrowlPathLink.getData().toString());
      if (file.exists())
        dialog.setFilterPath(file.toString());
    }

    /* Preset with a good Location */
    if (!StringUtils.isSet(dialog.getFilterPath())) {
      if (new File(DEFAULT_GROWLNOTIFY_LOCATION).exists())
        dialog.setFilterPath(DEFAULT_GROWLNOTIFY_LOCATION);
    }

    String folderPath = dialog.open();
    if (folderPath != null) {
      updateLink(folderPath);

      /* Link might require more space now */
      fGrowlPathLink.getShell().layout(true, true);
    }
  }

  private void updateLink(Object data) {
    if (data == null) {
      resetLink();
    } else {
      File file = new File(data.toString());
      if (file.exists()) {
        fGrowlPathLink.setText("<a>" + file.getAbsolutePath() + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        fGrowlPathLink.setData(data);
      } else
        resetLink();
    }
  }

  private void resetLink() {
    fGrowlPathLink.setText(Messages.ShowGrowlActionPresentation_SELECT_GROWL_LINK);
    fGrowlPathLink.setData(null);
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
    return fGrowlPathLink.getData();
  }
}