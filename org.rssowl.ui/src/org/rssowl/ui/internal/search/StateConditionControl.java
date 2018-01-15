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

package org.rssowl.ui.internal.search;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolTip;
import org.rssowl.core.persist.INews;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.EnumSet;

/**
 * The <code>StateConditionControl</code> is a <code>Composite</code> providing
 * the UI to define State-Conditions for a Search.
 *
 * @author bpasero
 */
public class StateConditionControl extends Composite {
  private Button fNewState;
  private Button fUnreadState;
  private Button fUpdatedState;
  private Button fReadState;

  /**
   * @param parent The parent Composite.
   * @param style The Style as defined by SWT constants.
   */
  StateConditionControl(Composite parent, int style) {
    super(parent, style);

    initComponents();
  }

  EnumSet<INews.State> getSelection() {
    EnumSet<INews.State> set = null;

    if (fNewState.getSelection()) {
      set = EnumSet.of(INews.State.NEW);
    }

    if (fUnreadState.getSelection()) {
      if (set == null)
        set = EnumSet.of(INews.State.UNREAD);
      else
        set.add(INews.State.UNREAD);
    }

    if (fUpdatedState.getSelection()) {
      if (set == null)
        set = EnumSet.of(INews.State.UPDATED);
      else
        set.add(INews.State.UPDATED);
    }

    if (fReadState.getSelection()) {
      if (set == null)
        set = EnumSet.of(INews.State.READ);
      else
        set.add(INews.State.READ);
    }

    return set;
  }

  /**
   * Selects the given States in the Control. Will deselect all states if the
   * field is <code>NULL</code>.
   *
   * @param selectedStates the news states to select in the Control or
   * <code>NULL</code> if none.
   */
  void select(EnumSet<INews.State> selectedStates) {
    fNewState.setSelection(selectedStates != null && selectedStates.contains(INews.State.NEW));
    fUnreadState.setSelection(selectedStates != null && selectedStates.contains(INews.State.UNREAD));
    fUpdatedState.setSelection(selectedStates != null && selectedStates.contains(INews.State.UPDATED));
    fReadState.setSelection(selectedStates != null && selectedStates.contains(INews.State.READ));
  }

  private void initComponents() {

    /* Apply Gridlayout */
    setLayout(LayoutUtils.createGridLayout(4, 0, 0));

    /* State: New */
    fNewState = new Button(this, SWT.CHECK);
    fNewState.setText(Messages.StateConditionControl_NEW);
    fNewState.setToolTipText(Messages.StateConditionControl_NEW_INFO);
    fNewState.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    /* State: Unread */
    fUnreadState = new Button(this, SWT.CHECK);
    fUnreadState.setText(Messages.StateConditionControl_UNREAD);
    fUnreadState.setToolTipText(Messages.StateConditionControl_UNREAD_INFO);
    fUnreadState.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    /* Use Control Decoration on Mac */
    if (Application.IS_MAC) {

      /* Use a decoration to help the user understand the State Semantic */
      final ControlDecoration newControlDeco = new ControlDecoration(fNewState, SWT.LEFT | SWT.TOP);
      newControlDeco.setImage(OwlUI.getImage(fNewState, "icons/obj16/dotempty.gif")); //$NON-NLS-1$
      newControlDeco.hide();

      final ControlDecoration unreadControlDeco = new ControlDecoration(fUnreadState, SWT.LEFT | SWT.TOP);
      unreadControlDeco.setImage(OwlUI.getImage(fUnreadState, "icons/obj16/dotempty.gif")); //$NON-NLS-1$
      unreadControlDeco.hide();

      fNewState.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (fNewState.getSelection() && !fUnreadState.getSelection()) {
            unreadControlDeco.show();
            unreadControlDeco.showHoverText(Messages.StateConditionControl_UNREAD_HINT);
          } else {
            unreadControlDeco.hide();
            unreadControlDeco.hideHover();
          }
        }
      });

      fUnreadState.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (fUnreadState.getSelection() && !fNewState.getSelection()) {
            newControlDeco.show();
            newControlDeco.showHoverText(Messages.StateConditionControl_NEW_HINT);
          } else {
            newControlDeco.hide();
            newControlDeco.hideHover();
          }
        }
      });

      fNewState.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          newControlDeco.hide();
          newControlDeco.hideHover();
        }
      });

      fUnreadState.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          unreadControlDeco.hide();
          unreadControlDeco.hideHover();
        }
      });
    }

    /* Use Balloon Tooltip on Windows and Linux */
    else {

      /* Use a Tooltip to help the user understand the State Semantic */
      final ToolTip newStateToolTip = new ToolTip(getShell(), SWT.BALLOON);
      newStateToolTip.setMessage(Messages.StateConditionControl_NEW_HINT);
      newStateToolTip.setAutoHide(false);

      final ToolTip unreadStateToolTip = new ToolTip(getShell(), SWT.BALLOON);
      unreadStateToolTip.setMessage(Messages.StateConditionControl_UNREAD_HINT);
      unreadStateToolTip.setAutoHide(false);

      fNewState.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (fNewState.getSelection() && !fUnreadState.getSelection()) {
            Point toolTipLocation = toDisplay(fUnreadState.getLocation());
            toolTipLocation.y += fUnreadState.getSize().y;
            if (Application.IS_WINDOWS)
              toolTipLocation.x += 5;
            else if (Application.IS_LINUX)
              toolTipLocation.x += 12;

            unreadStateToolTip.setLocation(toolTipLocation);
            unreadStateToolTip.setVisible(true);
          } else {
            unreadStateToolTip.setVisible(false);
          }
        }
      });

      fUnreadState.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          if (fUnreadState.getSelection() && !fNewState.getSelection()) {
            Point toolTipLocation = toDisplay(fNewState.getLocation());
            toolTipLocation.y += fNewState.getSize().y;
            if (Application.IS_WINDOWS)
              toolTipLocation.x += 5;
            else if (Application.IS_LINUX)
              toolTipLocation.x += 12;

            newStateToolTip.setLocation(toolTipLocation);
            newStateToolTip.setVisible(true);
          } else {
            newStateToolTip.setVisible(false);
          }
        }
      });

      fNewState.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          newStateToolTip.setVisible(false);
        }

        @Override
        public void focusLost(FocusEvent e) {
          unreadStateToolTip.setVisible(false);
        }
      });

      fUnreadState.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          unreadStateToolTip.setVisible(false);
        }

        @Override
        public void focusLost(FocusEvent e) {
          newStateToolTip.setVisible(false);
        }
      });

      addDisposeListener(new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e) {
          unreadStateToolTip.dispose();
          newStateToolTip.dispose();
        }
      });
    }

    /* State: Updated */
    fUpdatedState = new Button(this, SWT.CHECK);
    fUpdatedState.setText(Messages.StateConditionControl_UPDATED);
    fUpdatedState.setToolTipText(Messages.StateConditionControl_UPDATED_INFO);
    fUpdatedState.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    /* State: Read */
    fReadState = new Button(this, SWT.CHECK);
    fReadState.setText(Messages.StateConditionControl_READ);
    fReadState.setToolTipText(Messages.StateConditionControl_READ_INFO);
    fReadState.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));

    /* Selection Listener to issue modify events */
    SelectionListener selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        notifyListeners(SWT.Modify, new Event());
      }
    };
    fNewState.addSelectionListener(selectionListener);
    fUnreadState.addSelectionListener(selectionListener);
    fUpdatedState.addSelectionListener(selectionListener);
    fReadState.addSelectionListener(selectionListener);
  }
}