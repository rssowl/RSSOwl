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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.rssowl.core.INewsAction;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.filter.INewsActionPresentation;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.Collection;

/**
 * A subclass of {@link Composite} to show the presentation of a
 * {@link INewsAction}.
 *
 * @author bpasero
 */
public class NewsActionItem extends Composite {
  private final IFilterAction fInitialFilterAction;
  private final IModelFactory fFactory = Owl.getModelFactory();
  private final NewsActionPresentationManager fNewsActionPresentationManager = NewsActionPresentationManager.getInstance();
  private ComboViewer fViewer;
  private INewsActionPresentation fShowingPresentation;

  /**
   * @param parent
   * @param style
   * @param initialFilterAction the initial filter action to show, never
   * <code>null</code>.
   */
  public NewsActionItem(Composite parent, int style, IFilterAction initialFilterAction) {
    super(parent, style);
    Assert.isNotNull(initialFilterAction);

    fInitialFilterAction = initialFilterAction;
    initComponents();
  }

  IFilterAction createFilterAction(boolean ignoreEmpty) {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    NewsActionDescriptor descriptor = (NewsActionDescriptor) selection.getFirstElement();
    IFilterAction filterAction = fFactory.createFilterAction(descriptor.getActionId());
    if (fShowingPresentation != null) {
      Object data = fShowingPresentation.getData();
      if (data != null)
        filterAction.setData(data);
      else if (ignoreEmpty) //Action was not fully specified
        return null;
    }

    return filterAction;
  }

  boolean hasValue() {
    if (fShowingPresentation == null)
      return true;

    return fShowingPresentation.getData() != null;
  }

  void focusInput() {
    fViewer.getCombo().setFocus();
  }

  private void initComponents() {
    setLayout(LayoutUtils.createGridLayout(2, 5, 5));

    /* Chooser for Action */
    Combo combo = new Combo(this, SWT.READ_ONLY | SWT.BORDER);
    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    fViewer = new ComboViewer(combo);
    fViewer.setContentProvider(new ArrayContentProvider());
    fViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return ((NewsActionDescriptor) element).getName();
      }
    });

    Collection<NewsActionDescriptor> actions = fNewsActionPresentationManager.getSortedNewsActions();
    fViewer.setInput(actions);
    combo.setVisibleItemCount(actions.size());

    /* Properly set Selection */
    NewsActionDescriptor selectedFilterAction = null;
    for (NewsActionDescriptor action : actions) {
      if (action.getActionId().equals(fInitialFilterAction.getActionId())) {
        fViewer.setSelection(new StructuredSelection(action));
        selectedFilterAction = action;
        updateInfoControl(action);
        break;
      }
    }

    /* Update Presentation on Selection Changes */
    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        NewsActionDescriptor descriptor = (NewsActionDescriptor) selection.getFirstElement();
        showFilterAction(descriptor, null);
        updateInfoControl(descriptor);
      }
    });

    if (selectedFilterAction != null)
      showFilterAction(selectedFilterAction, fInitialFilterAction.getData());
  }

  private void updateInfoControl(NewsActionDescriptor descriptor) {
    if (StringUtils.isSet(descriptor.getDescription()))
      fViewer.getControl().setToolTipText(descriptor.getDescription());
    else
      fViewer.getControl().setToolTipText(null);
  }

  private void showFilterAction(NewsActionDescriptor action, Object data) {

    /* Dispose old */
    if (fShowingPresentation != null)
      fShowingPresentation.dispose();

    /* Create New */
    INewsActionPresentation presentation = fNewsActionPresentationManager.getPresentation(action.getActionId());
    if (presentation != null) {
      presentation.create(this, data);
      fShowingPresentation = presentation;
    } else
      fShowingPresentation = null;

    fViewer.getControl().getParent().layout(true, true);
  }
}