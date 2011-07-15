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

package org.rssowl.ui.internal.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.rssowl.ui.internal.OwlUI;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ToolItem} drop down that allows to either select from some
 * predefined colors or to choose any color from the native color dialog.
 *
 * @author bpasero
 */
public class ColorPicker {

  private static final String[] COLOR_LABELS = new String[] { //
      Messages.ColorPicker_BARN_RED, //
      Messages.ColorPicker_SALEM_RED, //
      Messages.ColorPicker_SALMON, //
      Messages.ColorPicker_PUMPKIN, //
      Messages.ColorPicker_MARIGOLD_YELLOW, //
      Messages.ColorPicker_MUSTARD, //
      Messages.ColorPicker_BARBERRY_GREEN, //
      Messages.ColorPicker_TAVERN_GREEN, //
      Messages.ColorPicker_LEXINGTON_GREEN, //
      Messages.ColorPicker_SEA_GREEN, //
      Messages.ColorPicker_FEDERAL_BLUE, //
      Messages.ColorPicker_SOLIDER_BLUE, //
      Messages.ColorPicker_SLATE, //
      Messages.ColorPicker_PITCH_BLACK, //
      Messages.ColorPicker_DRIFTWOOD, //
      Messages.ColorPicker_CHOCOLATE_BROWN //
  };

  private static final RGB[] COLOR_VALUES = new RGB[] { //
      new RGB(124, 10, 2), // "Barn Red",
      new RGB(163, 21, 2), // "Salem Red",
      new RGB(214, 148, 99), // "Salmon",
      new RGB(200, 118, 10), // "Pumpkin",
      new RGB(240, 177, 12), // "Marigold Yellow",
      new RGB(209, 161, 17), // "Mustard",
      new RGB(136, 128, 54), // "Bayberry Green",
      new RGB(129, 150, 93), // "Tavern Green",
      new RGB(82, 92, 58), // "Lexington Green",
      new RGB(126, 135, 130), // "Sea Green",
      new RGB(111, 121, 174), // "Federal Blue",
      new RGB(92, 101, 126), // "Soldier Blue",
      new RGB(144, 152, 163), // "Slate",
      new RGB(25, 16, 17), // "Pitch Black",
      new RGB(82, 66, 41), // "Driftwood",
      new RGB(82, 16, 0), // "Chocolate Brown"
  };

  private Menu fColorMenu;
  private final Composite fParent;
  private ToolBar fBar;
  private ToolItem fColorItem;
  private List<Image> fImagesToDispose = new ArrayList<Image>();
  private RGB fSelectedColor = new RGB(0, 0, 0);

  /**
   * @param parent
   * @param style
   */
  public ColorPicker(Composite parent, int style) {
    fParent = parent;
    initControl(style);
  }

  /**
   * @param color the color to show.
   */
  public void setColor(RGB color) {
    onColorSelected(color);
  }

  /**
   * @return the control of the picker.
   */
  public Control getControl() {
    return fBar;
  }

  /**
   * @return the selected color
   */
  public RGB getColor() {
    return fSelectedColor;
  }

  private void initControl(int style) {
    fBar = new ToolBar(fParent, style);
    OwlUI.makeAccessible(fBar, Messages.ColorPicker_COLOR_LABEL);
    fBar.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        OwlUI.safeDispose(fColorMenu);

        for (Image img : fImagesToDispose) {
          img.dispose();
        }
      }
    });

    createColorMenu();

    fColorItem = new ToolItem(fBar, SWT.DROP_DOWN);
    fColorItem.setImage(createColorImage(fSelectedColor));
    fColorItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        Rectangle rect = fColorItem.getBounds();
        Point pt = new Point(rect.x, rect.y + rect.height);
        pt = fBar.toDisplay(pt);
        fColorMenu.setLocation(pt.x, pt.y);
        fColorMenu.setVisible(true);
      }
    });
  }

  private void createColorMenu() {
    fColorMenu = new Menu(fParent.getShell(), SWT.POP_UP);

    /* Add some useful Colors */
    for (int i = 0; i < COLOR_LABELS.length; i++) {
      MenuItem item = new MenuItem(fColorMenu, SWT.RADIO);
      item.setText(COLOR_LABELS[i]);

      final RGB color = COLOR_VALUES[i];
      item.setImage(createColorImage(color));
      item.setData(color);
      item.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onColorSelected(color);
        }
      });
    }

    /* Add Item to open the native Color Picker */
    new MenuItem(fColorMenu, SWT.SEPARATOR);
    MenuItem moreColor = new MenuItem(fColorMenu, SWT.PUSH);
    moreColor.setText(Messages.ColorPicker_OTHER);
    moreColor.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onSelectOtherColor();
      }
    });
  }

  private void onColorSelected(RGB color) {
    fSelectedColor = color;

    if (fColorItem.getImage() != null)
      fColorItem.getImage().dispose();

    fColorItem.setImage(createColorImage(fSelectedColor));

    MenuItem[] items = fColorMenu.getItems();
    for (MenuItem item : items) {
      if (fSelectedColor.equals(item.getData()))
        item.setSelection(true);
      else if (item.getSelection())
        item.setSelection(false);
    }
  }

  private Image createColorImage(RGB color) {
    Image img = OwlUI.createColorImage(fParent.getDisplay(), color);
    fImagesToDispose.add(img);

    return img;
  }

  private void onSelectOtherColor() {
    ColorDialog dialog = new ColorDialog(fParent.getShell());
    dialog.setRGB(fSelectedColor);
    RGB color = dialog.open();
    if (color != null)
      onColorSelected(color);
  }
}