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
 **     IBM Corporation - initial API and implementation                     **
 **     RSSOwl Development Team - additional API and implementation          **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.ui.internal.util;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * The <code>ViewerOpenStrategy</code> is a subset of the JFace
 * <code>OpenStrategy</code> with some additional API.
 * <p>
 * TODO Remove me once Bug "[OpenModes] OpenStrategy not working properly for
 * the Expand-Event" is fixed (164372).
 * </p>
 * 
 * @author bpasero
 */
public class ViewerOpenStrategy implements Listener {

  /* Default behavior. Double click to open the item. */
  private static final int DOUBLE_CLICK = 0;

  /* Single click will open the item. */
  private static final int SINGLE_CLICK = 1;

  /* Hover will select the item. */
  private static final int SELECT_ON_HOVER = 1 << 1;

  /* Open item when using arrow keys */
  private static final int ARROW_KEYS_OPEN = 1 << 2;

  /* Time used in FILE_EXPLORER and ACTIVE_DESKTOP */
  private static final int TIME = 500;

  /* Listeners */
  private ListenerList fOpenEventListeners = new ListenerList();

  /* Event Fields */
  private boolean fTimerStarted;
  private Event fMouseUpEvent;
  private Event fMouseMoveEvent;
  private SelectionEvent fSelectionPendent;
  private boolean fEnterKeyDown;
  private SelectionEvent fDefaultSelectionPendent;
  private boolean fArrowKeyDown;
  private final int[] fCount = new int[1];
  private long fStartTime = System.currentTimeMillis();
  private boolean fCollapseOccurred;
  private boolean fExpandOccurred;
  private Display fDisplay;

  /**
   * @param control the control the strategy is applied to
   */
  public ViewerOpenStrategy(Control control) {
    fDisplay = control.getDisplay();
    addListener(control);
  }

  /**
   * Adds an IOpenEventListener to the collection of openEventListeners
   * 
   * @param listener the listener to add
   */
  public void addOpenListener(IOpenEventListener listener) {
    fOpenEventListeners.add(listener);
  }

  /**
   * Removes an IOpenEventListener to the collection of openEventListeners
   * 
   * @param listener the listener to remove
   */
  public void removeOpenListener(IOpenEventListener listener) {
    fOpenEventListeners.remove(listener);
  }

  /**
   * Sets the expandOccurred flag back to false.
   */
  public void clearExpandFlag() {
    fExpandOccurred = false;
  }

  /*
   * Adds all needed listener to the control in order to implement
   * single-click/double-click strategies.
   */
  private void addListener(Control c) {
    c.addListener(SWT.MouseEnter, this);
    c.addListener(SWT.MouseExit, this);
    c.addListener(SWT.MouseMove, this);
    c.addListener(SWT.MouseDown, this);
    c.addListener(SWT.MouseUp, this);
    c.addListener(SWT.KeyDown, this);
    c.addListener(SWT.Selection, this);
    c.addListener(SWT.DefaultSelection, this);
    c.addListener(SWT.Collapse, this);
    c.addListener(SWT.Expand, this);
  }

  /*
   * Fire the open event to all openEventListeners
   */
  private void fireOpenEvent(SelectionEvent e) {
    if (e.item != null && e.item.isDisposed())
      return;

    Object listeners[] = fOpenEventListeners.getListeners();
    for (int i = 0; i < listeners.length; i++)
      ((IOpenEventListener) listeners[i]).handleOpen(e);
  }

  /*
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(final Event e) {

    /* Default Selection */
    if (e.type == SWT.DefaultSelection) {
      SelectionEvent event = new SelectionEvent(e);
      if (OpenStrategy.getOpenMethod() == DOUBLE_CLICK) {
        fireOpenEvent(event);
      } else {
        if (fEnterKeyDown) {
          fireOpenEvent(event);
          fEnterKeyDown = false;
          fDefaultSelectionPendent = null;
        } else {
          fDefaultSelectionPendent = event;
        }
      }
      return;
    }

    switch (e.type) {

      /* Mouse Enter / Exit */
      case SWT.MouseEnter:
      case SWT.MouseExit:
        fMouseUpEvent = null;
        fMouseMoveEvent = null;
        fSelectionPendent = null;
        break;

      /* Mouse Move */
      case SWT.MouseMove:
        if ((OpenStrategy.getOpenMethod() & SELECT_ON_HOVER) == 0)
          return;

        if (e.stateMask != 0)
          return;

        if (e.widget.getDisplay().getFocusControl() != e.widget)
          return;

        fMouseMoveEvent = e;
        final Runnable runnable[] = new Runnable[1];
        runnable[0] = new Runnable() {
          public void run() {
            long time = System.currentTimeMillis();
            int diff = (int) (time - fStartTime);
            if (diff <= TIME) {
              fDisplay.timerExec(diff * 2 / 3, runnable[0]);
            } else {
              fTimerStarted = false;
              setSelection(fMouseMoveEvent);
            }
          }
        };

        fStartTime = System.currentTimeMillis();
        if (!fTimerStarted) {
          fTimerStarted = true;
          fDisplay.timerExec(TIME * 2 / 3, runnable[0]);
        }
        break;

      /* Mouse Down */
      case SWT.MouseDown:
        fMouseUpEvent = null;
        fArrowKeyDown = false;
        break;

      /* TreeItem Expand */
      case SWT.Expand:
        fExpandOccurred = true;
        break;

      /* TreeItem Collapse */
      case SWT.Collapse:
        fCollapseOccurred = true;
        break;

      /* Mouse Up */
      case SWT.MouseUp:
        fMouseMoveEvent = null;
        if ((e.button != 1) || ((e.stateMask & ~SWT.BUTTON1) != 0))
          return;

        if (fSelectionPendent != null && !(fCollapseOccurred || fExpandOccurred)) {
          mouseSelectItem(fSelectionPendent);
        } else {
          fMouseUpEvent = e;
          fCollapseOccurred = false;
          fExpandOccurred = false;
        }
        break;

      /* Key Down */
      case SWT.KeyDown:
        fMouseMoveEvent = null;
        fMouseUpEvent = null;
        fArrowKeyDown = ((e.keyCode == SWT.ARROW_UP) || (e.keyCode == SWT.ARROW_DOWN)) && e.stateMask == 0;
        if (e.character == SWT.CR) {
          if (fDefaultSelectionPendent != null) {
            fireOpenEvent(new SelectionEvent(e));
            fEnterKeyDown = false;
            fDefaultSelectionPendent = null;
          } else {
            fEnterKeyDown = true;
          }
        }
        break;

      /* Selection */
      case SWT.Selection:
        SelectionEvent event = new SelectionEvent(e);
        fMouseMoveEvent = null;
        if (fMouseUpEvent != null)
          mouseSelectItem(event);
        else
          fSelectionPendent = event;

        fCount[0]++;
        final int id = fCount[0];

        if (fArrowKeyDown) {
          if (id == fCount[0]) {
            if ((OpenStrategy.getOpenMethod() & ARROW_KEYS_OPEN) != 0)
              fireOpenEvent(new SelectionEvent(e));
          }
        }
        break;
    }
  }

  void mouseSelectItem(SelectionEvent e) {
    if ((OpenStrategy.getOpenMethod() & SINGLE_CLICK) != 0)
      fireOpenEvent(e);

    fMouseUpEvent = null;
    fSelectionPendent = null;
  }

  void setSelection(Event e) {
    if (e == null)
      return;

    Widget w = e.widget;
    if (w.isDisposed())
      return;

    SelectionEvent selEvent = new SelectionEvent(e);

    /*
     * ISSUE: May have to create a interface with method: setSelection(Point p)
     * so that user's custom widgets can use this class. If we keep this option.
     */
    if (w instanceof Tree) {
      Tree tree = (Tree) w;
      TreeItem item = tree.getItem(new Point(e.x, e.y));
      if (item != null)
        tree.setSelection(new TreeItem[] { item });

      selEvent.item = item;
    } else if (w instanceof Table) {
      Table table = (Table) w;
      TableItem item = table.getItem(new Point(e.x, e.y));
      if (item != null)
        table.setSelection(new TableItem[] { item });

      selEvent.item = item;
    } else
      return;

    if (selEvent.item == null)
      return;
  }
}