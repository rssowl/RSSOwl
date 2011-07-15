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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.NewsColumn;
import org.rssowl.ui.internal.editors.feed.NewsColumnViewModel;

import java.util.List;

/**
 * The <code>NewsColumnSelectionControl</code> is a <code>Composite</code>
 * providing the UI to define the number and order of columns to display news.
 *
 * @author bpasero
 */
public class NewsColumnSelectionControl extends Composite {
  private ListViewer fNewsColumnViewer;
  private FontMetrics fFontMetrics;
  private NewsColumnViewModel fModel;
  private ComboViewer fSortByViewer;
  private ComboViewer fSortAscendingViewer;
  private Button fRemoveButton;
  private Button fMoveUpButton;
  private Button fMoveDownButton;

  /* Sort Order */
  private enum Order {
    ASCENDING(Messages.NewsColumnSelectionControl_ASCENDING), DESCENDING(Messages.NewsColumnSelectionControl_DESCENDING);

    private String fName;

    private Order(String name) {
      fName = name;
    }

    String getName() {
      return fName;
    }
  }

  /**
   * @param parent
   * @param style
   */
  public NewsColumnSelectionControl(Composite parent, int style) {
    super(parent, style);

    initMetrics();
    initComponents();
  }

  private void initMetrics() {
    GC gc = new GC(this);
    gc.setFont(JFaceResources.getDialogFont());
    fFontMetrics = gc.getFontMetrics();
    gc.dispose();
  }

  private void initComponents() {

    /* Apply Gridlayout */
    setLayout(LayoutUtils.createGridLayout(2, 0, 0));

    /* Left: List of Columns */
    fNewsColumnViewer = new ListViewer(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    fNewsColumnViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    fNewsColumnViewer.setContentProvider(new ArrayContentProvider());
    fNewsColumnViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        NewsColumn column = (NewsColumn) element;
        return column.getName();
      }
    });

    fNewsColumnViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateMoveEnablement();
        updateRemoveEnablement();
      }
    });

    /* Right: Buttons to manage Columns */
    Composite buttonContainer = new Composite(this, SWT.None);
    buttonContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    buttonContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

    /* Add */
    final Menu menu = new Menu(getShell(), SWT.POP_UP);
    menu.addMenuListener(new MenuListener() {
      public void menuShown(MenuEvent e) {
        MenuItem[] items = menu.getItems();
        for (MenuItem item : items) {
          item.dispose();
        }

        NewsColumn[] newsColumns = NewsColumn.values();
        for (final NewsColumn column : newsColumns) {
          if (column.isSelectable() && !fModel.contains(column)) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(column.getName());
            item.addSelectionListener(new SelectionAdapter() {
              @Override
              public void widgetSelected(SelectionEvent e) {
                fModel.addColumn(column);
                fNewsColumnViewer.add(column);
                updateRemoveEnablement();
                fNewsColumnViewer.setSelection(new StructuredSelection(column));
              }
            });
          }
        }
      }

      public void menuHidden(MenuEvent e) {}
    });

    final Button addButton = new Button(buttonContainer, SWT.DOWN);
    setButtonLayoutData(addButton);
    addButton.setText(Messages.NewsColumnSelectionControl_ADD);
    addButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        Rectangle rect = addButton.getBounds();
        Point pt = new Point(rect.x, rect.y + rect.height);
        pt = addButton.toDisplay(pt);
        menu.setLocation(pt.x, pt.y);
        menu.setVisible(true);
      }
    });

    addButton.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        OwlUI.safeDispose(menu);
      }
    });

    /* Remove */
    fRemoveButton = new Button(buttonContainer, SWT.PUSH);
    setButtonLayoutData(fRemoveButton);
    fRemoveButton.setText(Messages.NewsColumnSelectionControl_REMOVE);
    fRemoveButton.setEnabled(false);
    fRemoveButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onDelete();
      }
    });

    /* Move Up */
    fMoveUpButton = new Button(buttonContainer, SWT.PUSH);
    setButtonLayoutData(fMoveUpButton);
    fMoveUpButton.setText(Messages.NewsColumnSelectionControl_MOVE_UP);
    fMoveUpButton.setEnabled(false);
    fMoveUpButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onMove(true);
      }
    });

    /* Move Down */
    fMoveDownButton = new Button(buttonContainer, SWT.PUSH);
    setButtonLayoutData(fMoveDownButton);
    fMoveDownButton.setText(Messages.NewsColumnSelectionControl_MOVE_DOWN);
    fMoveDownButton.setEnabled(false);
    fMoveDownButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onMove(false);
      }
    });

    /* Bottom: Sort Column */
    Composite sortByContainer = new Composite(this, SWT.None);
    sortByContainer.setLayout(LayoutUtils.createGridLayout(3, 0, 0));
    sortByContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

    Label sortByLabel = new Label(sortByContainer, SWT.NONE);
    sortByLabel.setText(Messages.NewsColumnSelectionControl_SORT_BY);

    fSortByViewer = new ComboViewer(sortByContainer, SWT.READ_ONLY | SWT.BORDER);
    fSortByViewer.getCombo().setVisibleItemCount(20);
    fSortByViewer.setContentProvider(new ArrayContentProvider());
    fSortByViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        NewsColumn column = (NewsColumn) element;
        return column.getName();
      }
    });
    fSortByViewer.addFilter(new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element == NewsColumn.RELEVANCE)
          return false;

        return true;
      }
    });

    fSortByViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        NewsColumn column = (NewsColumn) selection.getFirstElement();
        fModel.setSortColumn(column);
      }
    });

    fSortAscendingViewer = new ComboViewer(sortByContainer, SWT.READ_ONLY | SWT.BORDER);
    fSortAscendingViewer.getCombo().setVisibleItemCount(2);
    fSortAscendingViewer.setContentProvider(new ArrayContentProvider());
    fSortAscendingViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return (((Order) element)).getName();
      }
    });

    fSortAscendingViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Order order = (Order) selection.getFirstElement();
        fModel.setAscending(order == Order.ASCENDING ? true : false);
      }
    });
  }

  private void onMove(boolean up) {
    IStructuredSelection selection = (IStructuredSelection) fNewsColumnViewer.getSelection();
    Object element = selection.getFirstElement();

    List<NewsColumn> columns = fModel.getColumns();
    int index = columns.indexOf(element);

    if (up && index > 0) {
      columns.remove(element);
      columns.add(index - 1, (NewsColumn) element);
    } else if (index < columns.size() - 1) {
      columns.remove(element);
      columns.add(index + 1, (NewsColumn) element);
    }

    fNewsColumnViewer.refresh();
    fNewsColumnViewer.getList().showSelection();
    updateMoveEnablement();
  }

  private void updateMoveEnablement() {
    boolean enableMoveUp = true;
    boolean enableMoveDown = true;

    int[] selectionIndices = fNewsColumnViewer.getList().getSelectionIndices();

    if (selectionIndices.length == 1) {
      enableMoveUp = selectionIndices[0] != 0;
      enableMoveDown = selectionIndices[0] != fNewsColumnViewer.getList().getItemCount() - 1;
    } else {
      enableMoveUp = false;
      enableMoveDown = false;
    }

    fMoveUpButton.setEnabled(enableMoveUp);
    fMoveDownButton.setEnabled(enableMoveDown);
  }

  private void updateRemoveEnablement() {
    fRemoveButton.setEnabled(fNewsColumnViewer.getList().getItemCount() > 1 && !fNewsColumnViewer.getSelection().isEmpty());
  }

  /**
   * @param model the news column model to show in the selection control.
   */
  public void setInput(NewsColumnViewModel model) {
    fModel = model;
    fNewsColumnViewer.setInput(model.getColumns());
    fSortByViewer.setInput(NewsColumn.values());
    fSortByViewer.setSelection(new StructuredSelection(model.getSortColumn()));
    fSortAscendingViewer.setInput(Order.values());
    fSortAscendingViewer.setSelection(new StructuredSelection(model.isAscending() ? Order.ASCENDING : Order.DESCENDING));
  }

  /**
   * @return the news column model from this selection control
   */
  public NewsColumnViewModel getModel() {
    return fModel;
  }

  private GridData setButtonLayoutData(Button button) {
    GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    int widthHint = Dialog.convertHorizontalDLUsToPixels(fFontMetrics, IDialogConstants.BUTTON_WIDTH);
    Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    data.widthHint = Math.max(widthHint, minSize.x);
    button.setLayoutData(data);
    return data;
  }

  private void onDelete() {
    IStructuredSelection selection = (IStructuredSelection) fNewsColumnViewer.getSelection();
    List<?> elements = selection.toList();
    for (Object element : elements) {
      fModel.removeColumn((NewsColumn) element);
      fNewsColumnViewer.remove(element);
    }

    if (fModel.getColumns().isEmpty()) {
      fModel.addColumn(NewsColumn.TITLE);
      fNewsColumnViewer.add(NewsColumn.TITLE);
    }

    updateRemoveEnablement();
    updateMoveEnablement();
  }
}