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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.filter.INewsActionPresentation;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.LabelDialog;
import org.rssowl.ui.internal.dialogs.LabelDialog.DialogMode;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link INewsActionPresentation} to select a Label.
 *
 * @author bpasero
 */
public class LabelNewsActionPresentation implements INewsActionPresentation {
  private Combo fLabelCombo;
  private ComboViewer fViewer;
  private Composite fContainer;
  private static final Object NEW_LABEL_MARKER = new Object();

  /*
   * @see org.rssowl.ui.IFilterActionPresentation#create(org.eclipse.swt.widgets.Composite, java.lang.Object)
   */
  public void create(Composite parent, Object data) {
    fContainer = new Composite(parent, SWT.NONE);
    fContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 0, false));
    ((GridLayout) fContainer.getLayout()).marginLeft = 5;
    fContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

    fLabelCombo = new Combo(fContainer, SWT.READ_ONLY | SWT.BORDER);
    fLabelCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fLabelCombo.setVisibleItemCount(15);

    fViewer = new ComboViewer(fLabelCombo);
    fViewer.setContentProvider(new ArrayContentProvider());
    fViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        if (element == NEW_LABEL_MARKER)
          return Messages.LabelNewsActionPresentation_NEW_LABEL;
        return ((ILabel) element).getName();
      }
    });

    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (NEW_LABEL_MARKER == selection.getFirstElement())
          onCreateLabel();
      }
    });

    /* Set Input */
    Collection<ILabel> labels = CoreUtils.loadSortedLabels();
    updateInput(labels);

    /* Set Selection */
    if (fLabelCombo.getItemCount() > 0) {
      if (data != null) {
        for (ILabel label : labels) {
          if (label.getId().equals(data)) {
            fViewer.setSelection(new StructuredSelection(label));
            break;
          }
        }
      }

      if (fLabelCombo.getSelectionIndex() == -1)
        fLabelCombo.select(0);
    }
  }

  private void updateInput(Collection<ILabel> labels) {
    List<Object> input = new ArrayList<Object>();
    input.addAll(labels);
    input.add(NEW_LABEL_MARKER);
    fViewer.setInput(input);
  }

  private void onCreateLabel() {
    LabelDialog dialog = new LabelDialog(fContainer.getShell(), DialogMode.ADD, null);
    if (dialog.open() == IDialogConstants.OK_ID) {
      String name = dialog.getName();
      RGB color = dialog.getColor();

      ILabel newLabel = Owl.getModelFactory().createLabel(null, name);
      newLabel.setColor(OwlUI.toString(color));
      newLabel.setOrder(fViewer.getCombo().getItemCount() - 1); // Do not count Marker
      DynamicDAO.save(newLabel);

      updateInput(CoreUtils.loadSortedLabels());
      fViewer.setSelection(new StructuredSelection(newLabel));
    } else
      fLabelCombo.select(0);
  }

  /*
   * @see org.rssowl.ui.IFilterActionPresentation#dispose()
   */
  public void dispose() {
    fContainer.dispose();
  }

  /*
   * @see org.rssowl.ui.IFilterActionPresentation#getData()
   */
  public Long getData() {
    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    Object object = selection.getFirstElement();
    if (object instanceof ILabel)
      return ((ILabel) selection.getFirstElement()).getId();

    return null;
  }
}