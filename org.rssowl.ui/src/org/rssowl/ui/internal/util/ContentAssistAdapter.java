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

import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter2;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.rssowl.ui.internal.Application;

/**
 * An implementation of {@link IControlContentAdapter} with a more clever rule
 * of inserting proposals. Supports {@link Text} and {@link Combo} widgets.
 *
 * @author bpasero
 */
public class ContentAssistAdapter implements IControlContentAdapter, IControlContentAdapter2 {
  private TextContentAdapter fTextAdapter;
  private ComboContentAdapter fComboAdapter;
  private Control fControl;
  private final char fSeparator;
  private String fSeparatorStr;
  private final boolean fExpectMultiValues;

  /**
   * @param control
   * @param separator
   * @param expectMultiValues
   */
  public ContentAssistAdapter(Control control, char separator, boolean expectMultiValues) {
    fControl = control;
    fSeparator = separator;
    fExpectMultiValues = expectMultiValues;
    fSeparatorStr = new String(new char[] { fSeparator });
    if (fSeparator != ' ')
      fSeparatorStr = fSeparatorStr + " "; //$NON-NLS-1$

    /* Text */
    if (control instanceof Text) {
      fTextAdapter = new TextContentAdapter() {
        @Override
        public Rectangle getInsertionBounds(Control control) {
          Rectangle bounds = super.getInsertionBounds(control);

          /* Bug on Mac: Insertion Bounds off by some pixels */
          if (Application.IS_MAC)
            bounds.y += 10;

          return bounds;
        }
      };
    }

    /* Combo */
    else if (control instanceof Combo) {
      fComboAdapter = new ComboContentAdapter() {
        @Override
        public Rectangle getInsertionBounds(Control control) {
          Rectangle bounds = super.getInsertionBounds(control);

          /* Bug on Mac: Insertion Bounds off by some pixels */
          if (Application.IS_MAC)
            bounds.x -= 10;

          return bounds;
        }
      };
    } else
      throw new IllegalArgumentException("Can only be used for Text and Combo Widgets"); //$NON-NLS-1$
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getControlContents(org.eclipse.swt.widgets.Control)
   */
  public String getControlContents(Control control) {
    String text = getText();
    int selectionOffset = getSelection().x;
    if (selectionOffset == 0)
      return ""; //$NON-NLS-1$

    int previousSeparatorIndex = getPreviousSeparatorIndex(text, selectionOffset);

    /* No Previous Separator Found - Return from Beginning */
    if (previousSeparatorIndex == -1)
      return text.substring(0, selectionOffset).trim();

    /* Previous Separator Found - Return from Separator */
    return text.substring(previousSeparatorIndex + 1, selectionOffset).trim();
  }

  private int getPreviousSeparatorIndex(String text, int selectionOffset) {
    int previousSeparatorIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (i == selectionOffset)
        break;

      if (text.charAt(i) == fSeparator)
        previousSeparatorIndex = i;
    }
    return previousSeparatorIndex;
  }

  private int getNextSeparatorIndex(String text, int selectionOffset) {
    int nextSeparatorIndex = -1;
    for (int i = selectionOffset + 1; i < text.length(); i++) {
      if (text.charAt(i) == fSeparator)
        return i;
    }
    return nextSeparatorIndex;
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#insertControlContents(org.eclipse.swt.widgets.Control, java.lang.String, int)
   */
  public void insertControlContents(Control control, String textToInsert, int cursorPosition) {
    String text = getText();

    int selectionOffset = getSelection().x;
    int previousSeparatorIndex = getPreviousSeparatorIndex(text, selectionOffset);
    int nextSeparatorIndex = getNextSeparatorIndex(text, selectionOffset);

    /* Replace All: No Separator Found */
    if (previousSeparatorIndex == -1 && nextSeparatorIndex == -1) {
      text = fExpectMultiValues ? textToInsert + fSeparatorStr : textToInsert;
    }

    /* Replace All beginning with Previous Separator  */
    else if (previousSeparatorIndex != -1 && nextSeparatorIndex == -1) {
      text = text.substring(0, previousSeparatorIndex);
      text = text + fSeparatorStr + textToInsert + fSeparatorStr;
    }

    /* Replace all from beginning till Next Separator */
    else if (previousSeparatorIndex == -1 && nextSeparatorIndex != -1) {
      text = textToInsert + text.substring(nextSeparatorIndex);
    }

    /* Replace all from previous Separator till next Separator */
    else {
      String leftHand = text.substring(0, previousSeparatorIndex);
      String rightHand = text.substring(nextSeparatorIndex);

      text = leftHand + fSeparatorStr + textToInsert + rightHand;
    }

    setText(text);
    setSelection(new Point(getText().length(), getText().length()));
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getCursorPosition(org.eclipse.swt.widgets.Control)
   */
  public int getCursorPosition(Control control) {
    if (control instanceof Text)
      return fTextAdapter.getCursorPosition(control);

    return fComboAdapter.getCursorPosition(control);
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getInsertionBounds(org.eclipse.swt.widgets.Control)
   */
  public Rectangle getInsertionBounds(Control control) {
    if (control instanceof Text)
      return fTextAdapter.getInsertionBounds(control);

    return fComboAdapter.getInsertionBounds(control);
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setControlContents(org.eclipse.swt.widgets.Control, java.lang.String, int)
   */
  public void setControlContents(Control control, String contents, int cursorPosition) {
    if (control instanceof Text)
      fTextAdapter.setControlContents(control, contents, cursorPosition);
    else
      fComboAdapter.setControlContents(control, contents, cursorPosition);
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setCursorPosition(org.eclipse.swt.widgets.Control, int)
   */
  public void setCursorPosition(Control control, int index) {
    if (control instanceof Text)
      fTextAdapter.setCursorPosition(control, index);
    else
      fComboAdapter.setCursorPosition(control, index);
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#getSelection(org.eclipse.swt.widgets.Control)
   */
  public Point getSelection(Control control) {
    if (control instanceof Text)
      return fTextAdapter.getSelection(control);

    return fComboAdapter.getSelection(control);
  }

  /*
   * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#setSelection(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point)
   */
  public void setSelection(Control control, Point range) {
    if (control instanceof Text)
      fTextAdapter.setSelection(control, range);
    else
      fComboAdapter.setSelection(control, range);
  }

  private String getText() {
    if (fControl instanceof Text)
      return ((Text) fControl).getText();

    return ((Combo) fControl).getText();
  }

  private Point getSelection() {
    if (fControl instanceof Text)
      return ((Text) fControl).getSelection();

    return ((Combo) fControl).getSelection();
  }

  private void setText(String text) {
    if (fControl instanceof Text)
      ((Text) fControl).setText(text);
    else
      ((Combo) fControl).setText(text);
  }

  private void setSelection(Point selection) {
    if (fControl instanceof Text)
      ((Text) fControl).setSelection(selection);
    else
      ((Combo) fControl).setSelection(selection);
  }
}