/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * A Dialog that is shown to the user to report progress on startup of RSSOwl
 * (e.g. when performing defragmentation of the database).
 *
 * @author bpasero
 */
public class StartupProgressDialog extends ProgressMonitorDialog {
  private LocalResourceManager fResources;

  public StartupProgressDialog() {
    super(null);
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
  protected void configureShell(final Shell shell) {
    super.configureShell(shell);

    shell.setText("RSSOwl"); //$NON-NLS-1$
    shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_APPSTARTING));

    /* Images not initialized yet */
    Image img_16x16 = OwlUI.getImage(fResources, "icons/product/16x16.png"); //$NON-NLS-1$
    Image img_32x32 = OwlUI.getImage(fResources, "icons/product/32x32.png"); //$NON-NLS-1$
    Image img_48x48 = OwlUI.getImage(fResources, "icons/product/48x48.png"); //$NON-NLS-1$
    Image img_64x64 = OwlUI.getImage(fResources, "icons/product/64x64.png"); //$NON-NLS-1$
    Image img_128x128 = OwlUI.getImage(fResources, "icons/product/128x128.png"); //$NON-NLS-1$
    shell.setImages(new Image[] { img_16x16, img_32x32, img_48x48, img_64x64, img_128x128 });
  }

  /*
   * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    initializeDialogUnits(parent);
    parent.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 0, 0, false));
    GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

    /* Title Area */
    Composite titleArea = new Composite(parent, SWT.None);
    titleArea.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 0, false));
    titleArea.setBackground(JFaceColors.getBannerBackground(parent.getDisplay()));
    GridDataFactory.fillDefaults().grab(true, true).applyTo(titleArea);
    createTitleArea(titleArea);

    /* Content Area */
    Composite contentArea = new Composite(parent, SWT.NONE);
    Point defaultSpacing = LayoutConstants.getSpacing();
    GridLayoutFactory.fillDefaults().margins(LayoutConstants.getMargins()).spacing(defaultSpacing.x * 2, defaultSpacing.y).numColumns(2).applyTo(contentArea);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(contentArea);
    createDialogAndButtonArea(contentArea);

    return parent;
  }

  private void createTitleArea(Composite parent) {
    Composite messageContainer = new Composite(parent, SWT.NONE);
    messageContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    messageContainer.setLayout(LayoutUtils.createGridLayout(1));
    messageContainer.setBackground(parent.getBackground());

    /* Title Message */
    Label titleMessage = new Label(messageContainer, SWT.NONE);
    titleMessage.setFont(JFaceResources.getBannerFont());
    titleMessage.setText(Messages.StartupProgressDialog_PROGRESS_INFO);
    titleMessage.setBackground(parent.getBackground());

    /* Title Footer */
    Label titleFooter = new Label(messageContainer, SWT.NONE);
    titleFooter.setText(Messages.StartupProgressDialog_PROGRESS_MESSAGE);
    titleFooter.setFont(JFaceResources.getDialogFont());
    titleFooter.setBackground(parent.getBackground());

    GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
    data.horizontalIndent = 10;
    data.verticalIndent = 5;
    titleFooter.setLayoutData(data);

    /* RSSOwl Logo */
    Label imageLabel = new Label(parent, SWT.None);
    imageLabel.setImage(OwlUI.getImage(fResources, "icons/wizban/welcome_wiz.gif")); //$NON-NLS-1$
    imageLabel.setBackground(parent.getBackground());

    /* Separator */
    Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    separator.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
    separator.setBackground(parent.getBackground());
  }

  /*
   * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#getImage()
   */
  @Override
  protected Image getImage() {
    return null;
  }

  /*
   * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#createCancelButton(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createCancelButton(Composite parent) {
    cancel = createButton(parent, IDialogConstants.CANCEL_ID, Messages.StartupProgressDialog_SKIP, true);
    setOperationCancelButtonEnabled(enableCancelButton);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
   */
  @Override
  protected Point getInitialLocation(Point initialSize) {
    Rectangle displayBounds = Display.getDefault().getPrimaryMonitor().getBounds();
    Point shellSize = getInitialSize();
    int x = displayBounds.x + (displayBounds.width - shellSize.x) >> 1;
    int y = displayBounds.y + (displayBounds.height - shellSize.y) >> 1;

    return new Point(x, y);
  }

  /*
   * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#getInitialSize()
   */
  @Override
  protected Point getInitialSize() {
    int minWidth = 380; //Do not overlap with splash width (400 pixels)
    int minHeight = getShell().computeSize(minWidth, SWT.DEFAULT).y;

    return new Point(minWidth, minHeight);
  }
}