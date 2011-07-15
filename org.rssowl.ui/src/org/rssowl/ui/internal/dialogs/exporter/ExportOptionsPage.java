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

package org.rssowl.ui.internal.dialogs.exporter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.rssowl.core.internal.newsaction.LabelNewsAction;
import org.rssowl.core.interpreter.ITypeExporter;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.ui.internal.OwlUI;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link WizardPage} to select which RSSOwl settings to include in the
 * export.
 *
 * @author bpasero
 */
public class ExportOptionsPage extends WizardPage {
  private Button fExportSettingsCheck;
  private Button fExportFiltersCheck;
  private Button fExportLabelsCheck;

  /**
   * @param pageName
   */
  protected ExportOptionsPage(String pageName) {
    super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/export_wiz.png")); //$NON-NLS-1$
    setMessage(Messages.ExportOptionsPage_EXPORT_OPTIONS);
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    StyledText infoText = new StyledText(container, SWT.WRAP | SWT.READ_ONLY);
    infoText.setEnabled(false);
    infoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    ((GridData) infoText.getLayoutData()).widthHint = 200;
    infoText.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    infoText.setText(Messages.ExportOptionsPage_OPTIONS_INFO);

    /* Labels */
    Collection<ILabel> labels = DynamicDAO.loadAll(ILabel.class);
    fExportLabelsCheck = new Button(container, SWT.CHECK);
    fExportLabelsCheck.setImage(OwlUI.getImage(fExportLabelsCheck, "icons/elcl16/labels.gif")); //$NON-NLS-1$
    if (!labels.isEmpty())
      fExportLabelsCheck.setText(NLS.bind(Messages.ExportOptionsPage_EXPORT_N_LABELS, labels.size()));
    else
      fExportLabelsCheck.setText(Messages.ExportOptionsPage_EXPORT_LABELS);
    fExportLabelsCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    ((GridData) fExportLabelsCheck.getLayoutData()).verticalIndent = 10;
    fExportLabelsCheck.setEnabled(!labels.isEmpty());

    /* Filters */
    Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
    final boolean filtersUseLabels = filtersUseLabels(filters);
    fExportFiltersCheck = new Button(container, SWT.CHECK);
    fExportFiltersCheck.setImage(OwlUI.getImage(fExportFiltersCheck, OwlUI.FILTER));
    if (!filters.isEmpty())
      fExportFiltersCheck.setText(NLS.bind(Messages.ExportOptionsPage_EXPORT_N_FILTERS, filters.size()));
    else
      fExportFiltersCheck.setText(Messages.ExportOptionsPage_EXPORT_FILTERS);
    fExportFiltersCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fExportFiltersCheck.setEnabled(!filters.isEmpty());
    fExportFiltersCheck.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (fExportFiltersCheck.getSelection() && !fExportLabelsCheck.getSelection() && filtersUseLabels) {
          fExportLabelsCheck.setSelection(true);
          setMessage(Messages.ExportOptionsPage_EXPORT_LABEL_FILTER_INFO, IMessageProvider.INFORMATION);
        } else if (!fExportFiltersCheck.getSelection()) {
          setMessage(Messages.ExportOptionsPage_EXPORT_OPTIONS);
        }
      }
    });

    /* Properties */
    fExportSettingsCheck = new Button(container, SWT.CHECK);
    fExportSettingsCheck.setImage(OwlUI.getImage(fExportSettingsCheck, "icons/elcl16/preferences.gif")); //$NON-NLS-1$
    fExportSettingsCheck.setText(Messages.ExportOptionsPage_EXPORT_PREFERENCES);
    fExportSettingsCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  /* Check if any Filter uses the Label Action */
  private boolean filtersUseLabels(Collection<ISearchFilter> filters) {
    for (ISearchFilter filter : filters) {
      List<IFilterAction> actions = filter.getActions();
      for (IFilterAction action : actions) {
        if (LabelNewsAction.ID.equals(action.getActionId()))
          return true;
      }
    }

    return false;
  }

  /* Get selected Export Options */
  Set<ITypeExporter.Options> getExportOptions() {
    Set<ITypeExporter.Options> options = new HashSet<ITypeExporter.Options>();
    if (fExportLabelsCheck.getSelection())
      options.add(ITypeExporter.Options.EXPORT_LABELS);
    if (fExportFiltersCheck.getSelection())
      options.add(ITypeExporter.Options.EXPORT_FILTERS);
    if (fExportSettingsCheck.getSelection())
      options.add(ITypeExporter.Options.EXPORT_PREFERENCES);

    return options;
  }

  /*
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete() {
    return true;
  }
}