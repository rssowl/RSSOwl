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
 **     Eclipse.org - initial API and implementation                         **
 **     RSSOwl Development Team - minor changes                              **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.ui.internal.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * A Label which supports aligned text and/or an image and different border
 * styles.
 * <p>
 * If there is not enough space a CLabel uses the following strategy to fit the
 * information into the available space:
 *
 * <pre>
 * 		ignores the indent in left align mode
 * 		shortens the text by replacing the center portion of the label with an ellipsis
 * 		shortens the text by removing the center portion of the label
 * </pre>
 *
 * <p>
 * Compared to the CLabel this Widget is never removing the Icon when the
 * available width is not enough. In addition, the ellipsis is not put into the
 * center of the text, but at the end.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>LEFT, RIGHT, CENTER, SHADOW_IN, SHADOW_OUT, SHADOW_NONE</dd>
 * <dt><b>Events:</b>
 * <dd></dd>
 * </dl>
 * </p>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
public class CCLabel extends Canvas {

  /* Gap between icon and text */
  private static final int GAP = 5;

  /* Left and right margins */
  private static final int INDENT = 3;

  /* a string inserted in the middle of text that has been shortened */
  private static final String ELLIPSIS = "..."; //$NON-NLS-1$

  private static int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;

  /* the alignment. Either CENTER, RIGHT, LEFT. Default is LEFT */
  private int align = SWT.LEFT;
  private int hIndent = INDENT;
  private int vIndent = INDENT;

  /* the current text */
  private String text;

  /* the current icon */
  private Image image;

  private String appToolTipText;
  private Image backgroundImage;
  private Color[] gradientColors;
  private int[] gradientPercents;
  private boolean gradientVertical;
  private Color background;
  private Color roundRectangleBackground;

  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must
   * be built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code>
   * style constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a widget which will be the parent of the new instance (cannot
   * be null)
   * @param style the style of widget to construct
   * @exception IllegalArgumentException
   * <ul>
   * <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException
   * <ul>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the parent</li>
   * </ul>
   * @see SWT#LEFT
   * @see SWT#RIGHT
   * @see SWT#CENTER
   * @see SWT#SHADOW_IN
   * @see SWT#SHADOW_OUT
   * @see SWT#SHADOW_NONE
   * @see #getStyle()
   */
  public CCLabel(Composite parent, int style) {
    super(parent, checkStyle(style));
    if ((style & (SWT.CENTER | SWT.RIGHT)) == 0)
      style |= SWT.LEFT;
    if ((style & SWT.CENTER) != 0)
      align = SWT.CENTER;
    if ((style & SWT.RIGHT) != 0)
      align = SWT.RIGHT;
    if ((style & SWT.LEFT) != 0)
      align = SWT.LEFT;

    addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent event) {
        onPaint(event);
      }
    });

    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent event) {
        onDispose();
      }
    });

    addTraverseListener(new TraverseListener() {
      @Override
      public void keyTraversed(TraverseEvent event) {
        if (event.detail == SWT.TRAVERSE_MNEMONIC) {
          onMnemonic(event);
        }
      }
    });

    initAccessible();

  }

  /**
   * Check the style bits to ensure that no invalid styles are applied.
   */
  private static int checkStyle(int style) {
    if ((style & SWT.BORDER) != 0)
      style |= SWT.SHADOW_IN;
    int mask = SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.SHADOW_NONE | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
    style = style & mask;
    return style | SWT.NO_FOCUS | SWT.DOUBLE_BUFFERED;
  }

  /*
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
   */
  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    checkWidget();
    Point e = getTotalSize(image, text);
    if (wHint == SWT.DEFAULT) {
      e.x += 2 * hIndent;
    } else {
      e.x = wHint;
    }
    if (hHint == SWT.DEFAULT) {
      e.y += 2 * vIndent;
    } else {
      e.y = hHint;
    }
    return e;
  }

  /**
   * Draw a rectangle in the given colors.
   */
  private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
    gc.setForeground(bottomright);
    gc.drawLine(x + w, y, x + w, y + h);
    gc.drawLine(x, y + h, x + w, y + h);

    gc.setForeground(topleft);
    gc.drawLine(x, y, x + w - 1, y);
    gc.drawLine(x, y, x, y + h - 1);
  }

  /*
   * Return the lowercase of the first non-'&' character following an '&'
   * character in the given string. If there are no '&' characters in the given
   * string, return '\0'.
   */
  char _findMnemonic(String string) {
    if (string == null)
      return '\0';
    int index = 0;
    int length = string.length();
    do {
      while (index < length && string.charAt(index) != '&')
        index++;
      if (++index >= length)
        return '\0';
      if (string.charAt(index) != '&')
        return Character.toLowerCase(string.charAt(index));
      index++;
    } while (index < length);
    return '\0';
  }

  /**
   * Returns the alignment. The alignment style (LEFT, CENTER or RIGHT) is
   * returned.
   *
   * @return SWT.LEFT, SWT.RIGHT or SWT.CENTER
   */
  public int getAlignment() {
    //checkWidget();
    return align;
  }

  /**
   * Return the CLabel's image or <code>null</code>.
   *
   * @return the image of the label or null
   */
  public Image getImage() {
    //checkWidget();
    return image;
  }

  /**
   * Compute the minimum size.
   */
  private Point getTotalSize(Image image, String text) {
    Point size = new Point(0, 0);

    if (image != null) {
      Rectangle r = image.getBounds();
      size.x += r.width;
      size.y += r.height;
    }

    GC gc = new GC(this);
    if (text != null && text.length() > 0) {
      Point e = gc.textExtent(text, DRAW_FLAGS);
      size.x += e.x;
      size.y = Math.max(size.y, e.y);
      if (image != null)
        size.x += GAP;
    } else {
      size.y = Math.max(size.y, gc.getFontMetrics().getHeight());
    }
    gc.dispose();

    return size;
  }

  @Override
  public int getStyle() {
    int style = super.getStyle();
    switch (align) {
      case SWT.RIGHT:
        style |= SWT.RIGHT;
        break;
      case SWT.CENTER:
        style |= SWT.CENTER;
        break;
      case SWT.LEFT:
        style |= SWT.LEFT;
        break;
    }
    return style;
  }

  /**
   * Return the Label's text.
   *
   * @return the text of the label or null
   */
  public String getText() {
    //checkWidget();
    return text;
  }

  @Override
  public String getToolTipText() {
    checkWidget();
    return appToolTipText;
  }

  private void initAccessible() {
    Accessible accessible = getAccessible();
    accessible.addAccessibleListener(new AccessibleAdapter() {
      @Override
      public void getName(AccessibleEvent e) {
        e.result = getText();
      }

      @Override
      public void getHelp(AccessibleEvent e) {
        e.result = getToolTipText();
      }

      @Override
      public void getKeyboardShortcut(AccessibleEvent e) {
        char mnemonic = _findMnemonic(CCLabel.this.text);
        if (mnemonic != '\0') {
          e.result = "Alt+" + mnemonic; //$NON-NLS-1$
        }
      }
    });

    accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
      @Override
      public void getChildAtPoint(AccessibleControlEvent e) {
        e.childID = ACC.CHILDID_SELF;
      }

      @Override
      public void getLocation(AccessibleControlEvent e) {
        Rectangle rect = getDisplay().map(getParent(), null, getBounds());
        e.x = rect.x;
        e.y = rect.y;
        e.width = rect.width;
        e.height = rect.height;
      }

      @Override
      public void getChildCount(AccessibleControlEvent e) {
        e.detail = 0;
      }

      @Override
      public void getRole(AccessibleControlEvent e) {
        e.detail = ACC.ROLE_LABEL;
      }

      @Override
      public void getState(AccessibleControlEvent e) {
        e.detail = ACC.STATE_READONLY;
      }
    });
  }

  void onDispose() {
    gradientColors = null;
    gradientPercents = null;
    backgroundImage = null;
    text = null;
    image = null;
    appToolTipText = null;
  }

  void onMnemonic(TraverseEvent event) {
    char mnemonic = _findMnemonic(text);
    if (mnemonic == '\0')
      return;
    if (Character.toLowerCase(event.character) != mnemonic)
      return;
    Composite control = this.getParent();
    while (control != null) {
      Control[] children = control.getChildren();
      int index = 0;
      while (index < children.length) {
        if (children[index] == this)
          break;
        index++;
      }
      index++;
      if (index < children.length) {
        if (children[index].setFocus()) {
          event.doit = true;
          event.detail = SWT.TRAVERSE_NONE;
        }
      }
      control = control.getParent();
    }
  }

  void onPaint(PaintEvent event) {
    Rectangle rect = getClientArea();
    if (rect.width == 0 || rect.height == 0)
      return;

    boolean shortenText = false;
    String t = text;
    Image img = image;
    int availableWidth = Math.max(0, rect.width - 2 * hIndent);
    Point extent = getTotalSize(img, t);
    if (extent.x > availableWidth) {
      availableWidth -= img != null ? img.getBounds().width : 0;
      extent = getTotalSize(img, t);
      if (extent.x > availableWidth) {
        shortenText = true;
      }
    }

    GC gc = event.gc;
    String[] lines = text == null ? null : splitString(text);

    // shorten the text
    if (lines != null && shortenText) {
      extent.x = 0;
      for (int i = 0; i < lines.length; i++) {
        Point e = gc.textExtent(lines[i], DRAW_FLAGS);
        if (e.x > availableWidth) {
          lines[i] = shortenText(gc, lines[i], availableWidth);
          extent.x = Math.max(extent.x, getTotalSize(null, lines[i]).x);
        } else {
          extent.x = Math.max(extent.x, e.x);
        }
      }
      if (appToolTipText == null) {
        super.setToolTipText(text);
      }
    } else {
      super.setToolTipText(appToolTipText);
    }

    // determine horizontal position
    int x = rect.x + hIndent;
    if (align == SWT.CENTER) {
      x = (rect.width - extent.x) / 2;
    }
    if (align == SWT.RIGHT) {
      x = rect.width - hIndent - extent.x;
    }

    // draw a background image behind the text
    try {
      if (backgroundImage != null) {
        // draw a background image behind the text
        Rectangle imageRect = backgroundImage.getBounds();
        // tile image to fill space
        gc.setBackground(getBackground());
        gc.fillRectangle(rect);
        int xPos = 0;
        while (xPos < rect.width) {
          int yPos = 0;
          while (yPos < rect.height) {
            gc.drawImage(backgroundImage, xPos, yPos);
            yPos += imageRect.height;
          }
          xPos += imageRect.width;
        }
      } else if (gradientColors != null) {
        // draw a gradient behind the text
        final Color oldBackground = gc.getBackground();
        if (gradientColors.length == 1) {
          if (gradientColors[0] != null)
            gc.setBackground(gradientColors[0]);
          gc.fillRectangle(0, 0, rect.width, rect.height);
        } else {
          final Color oldForeground = gc.getForeground();
          Color lastColor = gradientColors[0];
          if (lastColor == null)
            lastColor = oldBackground;
          int pos = 0;
          for (int i = 0; i < gradientPercents.length; ++i) {
            gc.setForeground(lastColor);
            lastColor = gradientColors[i + 1];
            if (lastColor == null)
              lastColor = oldBackground;
            gc.setBackground(lastColor);
            if (gradientVertical) {
              final int gradientHeight = (gradientPercents[i] * rect.height / 100) - pos;
              gc.fillGradientRectangle(0, pos, rect.width, gradientHeight, true);
              pos += gradientHeight;
            } else {
              final int gradientWidth = (gradientPercents[i] * rect.width / 100) - pos;
              gc.fillGradientRectangle(pos, 0, gradientWidth, rect.height, false);
              pos += gradientWidth;
            }
          }
          if (gradientVertical && pos < rect.height) {
            gc.setBackground(getBackground());
            gc.fillRectangle(0, pos, rect.width, rect.height - pos);
          }
          if (!gradientVertical && pos < rect.width) {
            gc.setBackground(getBackground());
            gc.fillRectangle(pos, 0, rect.width - pos, rect.height);
          }
          gc.setForeground(oldForeground);
        }
        gc.setBackground(oldBackground);
      } else {
        if (background != null || (getStyle() & SWT.DOUBLE_BUFFERED) == 0) {
          gc.setBackground(getBackground());
          gc.fillRectangle(rect);
        }

        if (roundRectangleBackground != null) {
          Color oldBackground = gc.getBackground();
          gc.setBackground(roundRectangleBackground);
          gc.fillRoundRectangle(rect.x, rect.y, rect.width, rect.height, 10, 50);
          if (oldBackground != null)
            gc.setBackground(oldBackground);
        }
      }
    } catch (SWTException e) {
      if ((getStyle() & SWT.DOUBLE_BUFFERED) == 0) {
        gc.setBackground(getBackground());
        gc.fillRectangle(rect);
      }
    }

    // draw border
    int style = getStyle();
    if ((style & SWT.SHADOW_IN) != 0 || (style & SWT.SHADOW_OUT) != 0) {
      paintBorder(gc, rect);
    }

    // draw the image
    if (img != null) {
      Rectangle imageRect = img.getBounds();
      gc.drawImage(img, 0, 0, imageRect.width, imageRect.height, x, (rect.height - imageRect.height) / 2, imageRect.width, imageRect.height);
      x += imageRect.width + GAP;
      extent.x -= imageRect.width + GAP;
    }
    // draw the text
    if (lines != null) {
      int lineHeight = gc.getFontMetrics().getHeight();
      int textHeight = lines.length * lineHeight;
      int lineY = Math.max(vIndent, rect.y + (rect.height - textHeight) / 2);
      gc.setForeground(getForeground());
      for (int i = 0; i < lines.length; i++) {
        int lineX = x;
        if (lines.length > 1) {
          if (align == SWT.CENTER) {
            int lineWidth = gc.textExtent(lines[i], DRAW_FLAGS).x;
            lineX = x + Math.max(0, (extent.x - lineWidth) / 2);
          }
          if (align == SWT.RIGHT) {
            int lineWidth = gc.textExtent(lines[i], DRAW_FLAGS).x;
            lineX = Math.max(x, rect.x + rect.width - hIndent - lineWidth);
          }
        }
        gc.drawText(lines[i], lineX, lineY, DRAW_FLAGS);
        lineY += lineHeight;
      }
    }
  }

  /**
   * Paint the Label's border.
   */
  private void paintBorder(GC gc, Rectangle r) {
    Display disp = getDisplay();

    Color c1 = null;
    Color c2 = null;

    int style = getStyle();
    if ((style & SWT.SHADOW_IN) != 0) {
      c1 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
      c2 = disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    }
    if ((style & SWT.SHADOW_OUT) != 0) {
      c1 = disp.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
      c2 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    }

    if (c1 != null && c2 != null) {
      gc.setLineWidth(1);
      drawBevelRect(gc, r.x, r.y, r.width - 1, r.height - 1, c1, c2);
    }
  }

  /**
   * Set the alignment of the CLabel. Use the values LEFT, CENTER and RIGHT to
   * align image and text within the available space.
   *
   * @param align the alignment style of LEFT, RIGHT or CENTER
   * @exception SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * <li>ERROR_INVALID_ARGUMENT - if the value of align is not one of SWT.LEFT,
   * SWT.RIGHT or SWT.CENTER</li>
   * </ul>
   */
  public void setAlignment(int align) {
    checkWidget();
    if (align != SWT.LEFT && align != SWT.RIGHT && align != SWT.CENTER) {
      SWT.error(SWT.ERROR_INVALID_ARGUMENT);
    }
    if (this.align != align) {
      this.align = align;
      redraw();
    }
  }

  /*
   * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
   */
  @Override
  public void setBackground(Color color) {
    super.setBackground(color);
    // Are these settings the same as before?
    if (backgroundImage == null && gradientColors == null && gradientPercents == null) {
      if (color == null) {
        if (background == null)
          return;
      } else {
        if (color.equals(background))
          return;
      }
    }
    background = color;
    backgroundImage = null;
    gradientColors = null;
    gradientPercents = null;
    redraw();
  }

  /**
   * Specify a gradient of colours to be drawn in the background of the CLabel.
   * <p>
   * For example, to draw a gradient that varies from dark blue to blue and then
   * to white and stays white for the right half of the label, use the following
   * call to setBackground:
   * </p>
   *
   * <pre>
   *	clabel.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
   *		                           display.getSystemColor(SWT.COLOR_BLUE),
   *		                           display.getSystemColor(SWT.COLOR_WHITE),
   *		                           display.getSystemColor(SWT.COLOR_WHITE)},
   *		               new int[] {25, 50, 100});
   * </pre>
   *
   * @param colors an array of Color that specifies the colors to appear in the
   * gradient in order of appearance from left to right; The value
   * <code>null</code> clears the background gradient; the value
   * <code>null</code> can be used inside the array of Color to specify the
   * background color.
   * @param percents an array of integers between 0 and 100 specifying the
   * percent of the width of the widget at which the color should change; the
   * size of the percents array must be one less than the size of the colors
   * array.
   * @exception SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * <li>ERROR_INVALID_ARGUMENT - if the values of colors and percents are not
   * consistent</li>
   * </ul>
   */
  public void setBackground(Color[] colors, int[] percents) {
    setBackground(colors, percents, false);
  }

  /**
   * Specify a gradient of colours to be drawn in the background of the CLabel.
   * <p>
   * For example, to draw a gradient that varies from dark blue to white in the
   * vertical, direction use the following call to setBackground:
   * </p>
   *
   * <pre>
   *	clabel.setBackground(new Color[]{display.getSystemColor(SWT.COLOR_DARK_BLUE),
   *		                           display.getSystemColor(SWT.COLOR_WHITE)},
   *		                 new int[] {100}, true);
   * </pre>
   *
   * @param colors an array of Color that specifies the colors to appear in the
   * gradient in order of appearance from left/top to right/bottom; The value
   * <code>null</code> clears the background gradient; the value
   * <code>null</code> can be used inside the array of Color to specify the
   * background color.
   * @param percents an array of integers between 0 and 100 specifying the
   * percent of the width/height of the widget at which the color should change;
   * the size of the percents array must be one less than the size of the colors
   * array.
   * @param vertical indicate the direction of the gradient. True is vertical
   * and false is horizontal.
   * @exception SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * <li>ERROR_INVALID_ARGUMENT - if the values of colors and percents are not
   * consistent</li>
   * </ul>
   * @since 3.0
   */
  @SuppressWarnings("null")
  public void setBackground(Color[] colors, int[] percents, boolean vertical) {
    checkWidget();
    if (colors != null) {
      if (percents == null || percents.length != colors.length - 1) {
        SWT.error(SWT.ERROR_INVALID_ARGUMENT);
      }
      if (getDisplay().getDepth() < 15) {
        // Don't use gradients on low color displays
        colors = new Color[] { colors[colors.length - 1] };
        percents = new int[] { };
      }

      for (int i = 0; i < percents.length; i++) {
        if (percents[i] < 0 || percents[i] > 100) {
          SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        if (i > 0 && percents[i] < percents[i - 1]) {
          SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
      }
    }

    // Are these settings the same as before?
    final Color background = getBackground();
    if (backgroundImage == null) {
      if ((gradientColors != null) && (colors != null) && (gradientColors.length == colors.length)) {
        boolean same = false;
        for (int i = 0; i < gradientColors.length; i++) {
          same = (gradientColors[i] == colors[i]) || ((gradientColors[i] == null) && (colors[i] == background)) || ((gradientColors[i] == background) && (colors[i] == null));
          if (!same)
            break;
        }
        if (same) {
          for (int i = 0; i < gradientPercents.length; i++) {
            same = gradientPercents[i] == percents[i];
            if (!same)
              break;
          }
        }
        if (same && this.gradientVertical == vertical)
          return;
      }
    } else {
      backgroundImage = null;
    }
    // Store the new settings
    if (colors == null) {
      gradientColors = null;
      gradientPercents = null;
      gradientVertical = false;
    } else {
      gradientColors = new Color[colors.length];
      for (int i = 0; i < colors.length; ++i)
        gradientColors[i] = (colors[i] != null) ? colors[i] : background;
      gradientPercents = new int[percents.length];
      for (int i = 0; i < percents.length; ++i)
        gradientPercents[i] = percents[i];
      gradientVertical = vertical;
    }
    // Refresh with the new settings
    redraw();
  }

  /**
   * Set the image to be drawn in the background of the label.
   *
   * @param image the image to be drawn in the background
   * @exception SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setBackground(Image image) {
    checkWidget();
    if (image == backgroundImage)
      return;
    if (image != null) {
      gradientColors = null;
      gradientPercents = null;
    }
    backgroundImage = image;
    redraw();

  }

  /*
   * @see org.eclipse.swt.widgets.Canvas#setFont(org.eclipse.swt.graphics.Font)
   */
  @Override
  public void setFont(Font font) {
    super.setFont(font);
    redraw();
  }

  /**
   * Set the label's Image. The value <code>null</code> clears it.
   *
   * @param image the image to be displayed in the label or null
   * @exception SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setImage(Image image) {
    checkWidget();
    if (image != this.image) {
      this.image = image;
      redraw();
    }
  }

  /**
   * Set the label's text. The value <code>null</code> clears it.
   *
   * @param text the text to be displayed in the label or null
   * @exception SWTException
   * <ul>
   * <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   * <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   * created the receiver</li>
   * </ul>
   */
  public void setText(String text) {
    checkWidget();
    if (text == null)
      text = ""; //$NON-NLS-1$
    if (!text.equals(this.text)) {
      this.text = text;
      redraw();
    }
  }

  /*
   * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
   */
  @Override
  public void setToolTipText(String string) {
    super.setToolTipText(string);
    appToolTipText = super.getToolTipText();
  }

  /**
   * Shorten the given text <code>t</code> so that its length doesn't exceed
   * the given width. This implementation will add the ellipsis at the end of
   * the text.
   *
   * @param gc the gc to use for text measurement
   * @param text the text to shorten
   * @param width the width to shorten the text to, in pixels
   * @return the shortened text
   */
  protected String shortenText(GC gc, String text, int width) {
    if (text == null || text.length() == 0)
      return text;

    /* Calc Ellipsis Width */
    int ellipsisWidth = gc.textExtent(ELLIPSIS, DRAW_FLAGS).x;
    if (width <= ellipsisWidth)
      return text;

    /* Create substring that fits available width */
    String subStr = ""; //$NON-NLS-1$
    for (int i = 1; i < text.length(); i++) {
      subStr = text.substring(0, i);

      int subStrWidth = gc.textExtent(subStr, DRAW_FLAGS).x;
      if (subStrWidth + ellipsisWidth > width) {
        subStr = text.substring(0, i - 2);
        break;
      }
    }

    /* Try to trim at whitespace */
    for (int i = subStr.length() - 1; i >= subStr.length() - 10; i--) {
      if (subStr.charAt(i) == ' ')
        return subStr.substring(0, i) + ELLIPSIS;
    }

    return subStr + ELLIPSIS;
  }

  private String[] splitString(String text) {
    String[] lines = new String[1];
    int start = 0, pos;
    do {
      pos = text.indexOf('\n', start);
      if (pos == -1) {
        lines[lines.length - 1] = text.substring(start);
      } else {
        boolean crlf = (pos > 0) && (text.charAt(pos - 1) == '\r');
        lines[lines.length - 1] = text.substring(start, pos - (crlf ? 1 : 0));
        start = pos + 1;
        String[] newLines = new String[lines.length + 1];
        System.arraycopy(lines, 0, newLines, 0, lines.length);
        lines = newLines;
      }
    } while (pos != -1);
    return lines;
  }

  /**
   * @param roundRectangleBackground the color to use as round rectangle
   * background behind the text or <code>null</code> if none.
   */
  public void setRoundRectangleBackground(Color roundRectangleBackground) {
    this.roundRectangleBackground = roundRectangleBackground;
    redraw();
  }
}