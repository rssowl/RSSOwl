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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.rssowl.ui.filter.INewsActionPresentation;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.ColorPicker;
import org.rssowl.ui.internal.util.LayoutUtils;

/**
 * An implementation of {@link INewsActionPresentation} to select a color to be
 * used for the news in the notifier.
 *
 * @author bpasero
 */
public class ShowNotifierNewsActionPresentation implements INewsActionPresentation {
  private Composite fContainer;
  private RGB fSelectedColor;
  private ColorPicker fColorPicker;

  /*
   * @see org.rssowl.ui.filter.INewsActionPresentation#create(org.eclipse.swt.widgets.Composite, java.lang.Object)
   */
  public void create(Composite parent, Object data) {
    if (data != null && data instanceof String)
      fSelectedColor = OwlUI.getRGB((String) data);
    else
      fSelectedColor = new RGB(200, 118, 10);

    fContainer = new Composite(parent, SWT.NONE);
    fContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 5, false));
    ((GridLayout) fContainer.getLayout()).marginLeft = 5;
    fContainer.setLayoutData(new GridData(SWT.FILL, Application.IS_WINDOWS ? SWT.FILL : SWT.CENTER, true, true));

    Label nameLabel = new Label(fContainer, SWT.NONE);
    nameLabel.setText(Messages.ShowNotifierNewsActionPresentation_SELECT_COLOR);
    nameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    fColorPicker = new ColorPicker(fContainer, SWT.FLAT);
    fColorPicker.setColor(fSelectedColor);
    fColorPicker.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
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
    return OwlUI.toString(fColorPicker.getColor());
  }
}