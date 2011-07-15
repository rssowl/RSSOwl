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

package org.rssowl.ui.internal.notifier;

import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.rssowl.ui.internal.OwlUI;

/**
 * Helper Class to create the colors for the Notifier.
 * <p>
 * Note: Copied from FormColors of UI Forms.
 * </p>
 *
 * @author bpasero
 */
public class NotifierColors {
  private Display fDisplay;
  private Color fGradientBegin;
  private Color fGradientEnd;
  private Color fBorder;
  private ResourceManager fResources;

  NotifierColors(Display display, ResourceManager resources) {
    fDisplay = display;
    fResources = resources;

    createColors();
  }

  private void createColors() {

    /* Notifier Border */
    createBorderColor();

    /* Notifier Gradient */
    createGradientColors();
  }

  Color getGradientBegin() {
    return fGradientBegin;
  }

  Color getGradientEnd() {
    return fGradientEnd;
  }

  Color getBorder() {
    return fBorder;
  }

  private void createBorderColor() {
    RGB tbBorder = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
    RGB bg = getImpliedBackground().getRGB();

    // Group 1
    // Rule: If at least 2 of the RGB values are equal to or between 180 and
    // 255, then apply specified opacity for Group 1
    // Examples: Vista, XP Silver, Wn High Con #2
    // Keyline = TITLE_BACKGROUND @ 70% Opacity over LIST_BACKGROUND
    if (testTwoPrimaryColors(tbBorder, 179, 256))
      tbBorder = blend(tbBorder, bg, 70);

    // Group 2
    // Rule: If at least 2 of the RGB values are equal to or between 121 and
    // 179, then apply specified opacity for Group 2
    // Examples: XP Olive, OSX Graphite, Linux GTK, Wn High Con Black
    // Keyline = TITLE_BACKGROUND @ 50% Opacity over LIST_BACKGROUND
    else if (testTwoPrimaryColors(tbBorder, 120, 180))
      tbBorder = blend(tbBorder, bg, 50);

    // Group 3
    // Rule: Everything else
    // Examples: XP Default, Wn Classic Standard, Wn Marine, Wn Plum, OSX
    // Aqua, Wn High Con White, Wn High Con #1
    // Keyline = TITLE_BACKGROUND @ 30% Opacity over LIST_BACKGROUND
    else {
      tbBorder = blend(tbBorder, bg, 30);
    }

    fBorder = OwlUI.getColor(fResources, tbBorder);
  }

  private void createGradientColors() {
    RGB titleBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
    Color bgColor = getImpliedBackground();
    RGB bg = bgColor.getRGB();
    RGB bottom, top;

    // Group 1
    // Rule: If at least 2 of the RGB values are equal to or between 180 and
    // 255, then apply specified opacity for Group 1
    // Examples: Vista, XP Silver, Wn High Con #2
    // Gradient Bottom = TITLE_BACKGROUND @ 30% Opacity over LIST_BACKGROUND
    // Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
    if (testTwoPrimaryColors(titleBg, 179, 256)) {
      bottom = blend(titleBg, bg, 30);
      top = bg;
    }

    // Group 2
    // Rule: If at least 2 of the RGB values are equal to or between 121 and
    // 179, then apply specified opacity for Group 2
    // Examples: XP Olive, OSX Graphite, Linux GTK, Wn High Con Black
    // Gradient Bottom = TITLE_BACKGROUND @ 20% Opacity over LIST_BACKGROUND
    // Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
    else if (testTwoPrimaryColors(titleBg, 120, 180)) {
      bottom = blend(titleBg, bg, 20);
      top = bg;
    }

    // Group 3
    // Rule: If at least 2 of the RGB values are equal to or between 0 and
    // 120, then apply specified opacity for Group 3
    // Examples: XP Default, Wn Classic Standard, Wn Marine, Wn Plum, OSX
    // Aqua, Wn High Con White, Wn High Con #1
    // Gradient Bottom = TITLE_BACKGROUND @ 10% Opacity over LIST_BACKGROUND
    // Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
    else {
      bottom = blend(titleBg, bg, 10);
      top = bg;
    }

    fGradientBegin = OwlUI.getColor(fResources, top);
    fGradientEnd = OwlUI.getColor(fResources, bottom);
  }

  private RGB blend(RGB c1, RGB c2, int ratio) {
    int r = blend(c1.red, c2.red, ratio);
    int g = blend(c1.green, c2.green, ratio);
    int b = blend(c1.blue, c2.blue, ratio);
    return new RGB(r, g, b);
  }

  private int blend(int v1, int v2, int ratio) {
    int b = (ratio * v1 + (100 - ratio) * v2) / 100;
    return Math.min(255, b);
  }

  private boolean testTwoPrimaryColors(RGB rgb, int from, int to) {
    int total = 0;
    if (testPrimaryColor(rgb.red, from, to))
      total++;
    if (testPrimaryColor(rgb.green, from, to))
      total++;
    if (testPrimaryColor(rgb.blue, from, to))
      total++;
    return total >= 2;
  }

  private boolean testPrimaryColor(int value, int from, int to) {
    return value > from && value < to;
  }

  private RGB getSystemColor(int code) {
    return getDisplay().getSystemColor(code).getRGB();
  }

  private Color getImpliedBackground() {
    return fDisplay.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
  }

  private Display getDisplay() {
    return fDisplay;
  }
}