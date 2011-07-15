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

package org.rssowl.ui.internal.dialogs.importer;

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
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.welcome.WelcomeWizard;

import java.util.Collection;
import java.util.List;

/**
 * A {@link WizardPage} to select additionsl options for the import.
 *
 * @author bpasero
 */
public class ImportOptionsPage extends WizardPage {
  private Button fImportLabelsCheck;
  private Button fImportFiltersCheck;
  private Button fImportPreferencesCheck;
  private boolean fFiltersUseLabels;

  ImportOptionsPage() {
    super(Messages.ImportOptionsPage_IMPORT_OPTIONS, Messages.ImportOptionsPage_IMPORT_OPTIONS, null);
    setMessage(Messages.ImportOptionsPage_SELECT_OPTIONS);
  }

  /*
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {

    /* Title Image */
    setImageDescriptor(OwlUI.getImageDescriptor(getWizard() instanceof WelcomeWizard ? "icons/wizban/welcome_wiz.gif" : "icons/wizban/import_wiz.png")); //$NON-NLS-1$ //$NON-NLS-2$

    /* Container */
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout(1, false));

    /* Info Text */
    StyledText infoText = new StyledText(container, SWT.WRAP | SWT.READ_ONLY);
    infoText.setEnabled(false);
    infoText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    ((GridData) infoText.getLayoutData()).widthHint = 300;
    infoText.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    infoText.setText(Messages.ImportOptionsPage_OPTIONS_INFO);

    /* Labels */
    fImportLabelsCheck = new Button(container, SWT.CHECK);
    fImportLabelsCheck.setImage(OwlUI.getImage(fImportLabelsCheck, "icons/elcl16/labels.gif")); //$NON-NLS-1$
    fImportLabelsCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    ((GridData) fImportLabelsCheck.getLayoutData()).verticalIndent = 10;

    /* Filters */
    fImportFiltersCheck = new Button(container, SWT.CHECK);
    fImportFiltersCheck.setImage(OwlUI.getImage(fImportFiltersCheck, OwlUI.FILTER));
    fImportFiltersCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fImportFiltersCheck.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (fImportFiltersCheck.getSelection() && !fImportLabelsCheck.getSelection() && fFiltersUseLabels) {
          fImportLabelsCheck.setSelection(true);
          setMessage(Messages.ImportOptionsPage_LABELS_INFO, IMessageProvider.INFORMATION);
        } else if (!fImportFiltersCheck.getSelection()) {
          setMessage(Messages.ImportOptionsPage_SELECT_OPTIONS);
        }
      }
    });

    /* Preferences */
    fImportPreferencesCheck = new Button(container, SWT.CHECK);
    fImportPreferencesCheck.setImage(OwlUI.getImage(fImportPreferencesCheck, "icons/elcl16/preferences.gif")); //$NON-NLS-1$
    fImportPreferencesCheck.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    Dialog.applyDialogFont(container);

    setControl(container);
  }

  /*
  * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
  */
  @Override
  public void setVisible(boolean visible) {

    /* Updated Checkboxes based on the Imported Elements */
    ImportElementsPage elementsPage;
    if (getPreviousPage() instanceof ImportElementsPage)
      elementsPage = (ImportElementsPage) getPreviousPage();
    else
      elementsPage = (ImportElementsPage) getPreviousPage().getPreviousPage();

    update(elementsPage.getLabelsToImport().size(), elementsPage.getFiltersToImport().size(), !elementsPage.getPreferencesToImport().isEmpty());
    fFiltersUseLabels = filtersUseLabels(elementsPage.getFiltersToImport());

    super.setVisible(visible);
  }

  /* Import Labels */
  boolean importLabels() {
    return fImportLabelsCheck.getSelection();
  }

  /* Import Filters */
  boolean importFilters() {
    return fImportFiltersCheck.getSelection();
  }

  /* Import Preferences */
  boolean importPreferences() {
    return fImportPreferencesCheck.getSelection();
  }

  /* Update Checkboxes based on the given Counter Values */
  private void update(int labelCount, int filterCount, boolean hasPreferences) {

    /* Labels */
    if (labelCount != 0)
      fImportLabelsCheck.setText(NLS.bind(Messages.ImportOptionsPage_IMPORT_N_LABELS, labelCount));
    else
      fImportLabelsCheck.setText(Messages.ImportOptionsPage_IMPORT_LABELS);
    fImportLabelsCheck.setEnabled(labelCount != 0);

    /* Filters */
    if (filterCount != 0)
      fImportFiltersCheck.setText(NLS.bind(Messages.ImportOptionsPage_IMPORT_N_FILTERS, filterCount));
    else
      fImportFiltersCheck.setText(Messages.ImportOptionsPage_IMPORT_FILTERS);
    fImportFiltersCheck.setEnabled(filterCount != 0);

    /* Preferences */
    if (hasPreferences)
      fImportPreferencesCheck.setText(Messages.ImportOptionsPage_IMPORT_PREFRENCES);
    else
      fImportPreferencesCheck.setText(Messages.ImportOptionsPage_IMPORT_PREFERENCES_UNAVAILABLE);
    fImportPreferencesCheck.setEnabled(hasPreferences);
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
}