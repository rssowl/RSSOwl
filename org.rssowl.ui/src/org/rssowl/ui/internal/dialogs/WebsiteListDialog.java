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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.ApplicationWorkbenchWindowAdvisor;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.CColumnLayoutData;
import org.rssowl.ui.internal.util.CTable;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.CColumnLayoutData.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A dialog to list web sites allowing to add and remove sites.
 *
 * @author bpasero
 */
public class WebsiteListDialog extends Dialog {
  private IPreferenceScope fPreferences = Owl.getPreferenceService().getGlobalScope();
  private TableViewer fViewer;
  private Text fWebsiteInput;
  private Button fRemoveSelectedButton;

  /**
   * @param parentShell
   */
  public WebsiteListDialog(Shell parentShell) {
    super(parentShell);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 10, 10));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Info Label */
    Label infoLabel = new Label(composite, SWT.None);
    infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false, 2, 1));
    infoLabel.setText(Messages.WebsiteListDialog_ENTER_WEBSITE);

    /* URL Input */
    fWebsiteInput = new Text(composite, SWT.BORDER | SWT.SINGLE);
    fWebsiteInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    fWebsiteInput.setFocus();

    Button addWebsiteButton = new Button(composite, SWT.PUSH);
    addWebsiteButton.getShell().setDefaultButton(addWebsiteButton);
    addWebsiteButton.setText(Messages.WebsiteListDialog_ADD);
    applyDialogFont(addWebsiteButton);
    setButtonLayoutData(addWebsiteButton);
    addWebsiteButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onAdd();
      }
    });

    /* Website List Viewer */
    Composite tableContainer = new Composite(composite, SWT.NONE);
    tableContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

    CTable cTable = new CTable(tableContainer, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

    fViewer = new TableViewer(cTable.getControl());
    fViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    fViewer.getTable().setHeaderVisible(true);
    ((GridData) fViewer.getTable().getLayoutData()).heightHint = fViewer.getTable().getItemHeight() * 10;
    fViewer.getTable().setData(ApplicationWorkbenchWindowAdvisor.FOCUSLESS_SCROLL_HOOK, new Object());

    TableColumn nameCol = new TableColumn(fViewer.getTable(), SWT.NONE);

    CColumnLayoutData data = new CColumnLayoutData(Size.FILL, 100);
    cTable.manageColumn(nameCol, data, Messages.WebsiteListDialog_WEBSITE, null, null, false, false);

    /* ContentProvider returns all providers */
    fViewer.setContentProvider(new ArrayContentProvider());

    /* Label Provider */
    fViewer.setLabelProvider(new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        cell.setText(cell.getElement().toString());
      }
    });

    fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        fRemoveSelectedButton.setEnabled(!event.getSelection().isEmpty());
      }
    });

    /* Set input */
    fViewer.setInput(fPreferences.getStrings(DefaultPreferences.DISABLE_JAVASCRIPT_EXCEPTIONS));

    applyDialogFont(composite);

    return composite;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {
    Composite buttonBar = (Composite) super.createButtonBar(parent);
    ((GridLayout) buttonBar.getLayout()).marginHeight = 0;
    ((GridLayout) buttonBar.getLayout()).marginBottom = 10;
    ((GridLayout) buttonBar.getLayout()).makeColumnsEqualWidth = false;

    ((GridData) buttonBar.getLayoutData()).horizontalAlignment = SWT.FILL;

    return buttonBar;
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.WebsiteListDialog_JS_EXCEPTIONS);
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.APPLICATION_MODAL | SWT.TITLE | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | getDefaultOrientation();

    return style;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {

    /* Remove Selected */
    fRemoveSelectedButton = createButton(parent, -1, Messages.WebsiteListDialog_REMOVE_WEBSITE, false);
    ((GridData) fRemoveSelectedButton.getLayoutData()).horizontalAlignment = SWT.BEGINNING;
    ((GridData) fRemoveSelectedButton.getLayoutData()).grabExcessHorizontalSpace = false;
    fRemoveSelectedButton.setEnabled(false);
    fRemoveSelectedButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onRemoveSelected();
      }
    });

    /* Remove All */
    Button removeAllButton = createButton(parent, -2, Messages.WebsiteListDialog_REMOVE_ALL_WEBSITES, false);
    ((GridData) removeAllButton.getLayoutData()).horizontalAlignment = SWT.BEGINNING;
    ((GridData) removeAllButton.getLayoutData()).grabExcessHorizontalSpace = false;
    removeAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onRemoveAll();
      }
    });

    /* Close */
    Button closeButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
    int widthHint = ((GridData) closeButton.getLayoutData()).widthHint;
    closeButton.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false));
    ((GridData) closeButton.getLayoutData()).widthHint = widthHint;
  }

  private void onAdd() {
    String website = StringUtils.normalizeString(fWebsiteInput.getText());
    if (StringUtils.isSet(website)) {
      String[] websites = fPreferences.getStrings(DefaultPreferences.DISABLE_JAVASCRIPT_EXCEPTIONS);
      if (websites == null)
        websites = new String[0];
      List<String> newWebsites = new ArrayList<String>(Arrays.asList(websites));
      if (!newWebsites.contains(website))
        newWebsites.add(website);
      String[] newWebsitesArray = newWebsites.toArray(new String[newWebsites.size()]);
      fPreferences.putStrings(DefaultPreferences.DISABLE_JAVASCRIPT_EXCEPTIONS, newWebsitesArray);
      fViewer.setInput(newWebsitesArray);
    }

    fWebsiteInput.setText(""); //$NON-NLS-1$
    fWebsiteInput.setFocus();
  }

  private void onRemoveAll() {
    fPreferences.delete(DefaultPreferences.DISABLE_JAVASCRIPT_EXCEPTIONS);
    fViewer.setInput(new String[0]);
  }

  private void onRemoveSelected() {
    String[] websites = fPreferences.getStrings(DefaultPreferences.DISABLE_JAVASCRIPT_EXCEPTIONS);
    List<String> newWebsites = new ArrayList<String>(Arrays.asList(websites));

    IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
    List<?> list = selection.toList();
    for (Object object : list) {
      String website = (String) object;
      newWebsites.remove(website);
    }

    String[] newWebsitesArray = newWebsites.toArray(new String[newWebsites.size()]);
    fPreferences.putStrings(DefaultPreferences.DISABLE_JAVASCRIPT_EXCEPTIONS, newWebsitesArray);
    fViewer.setInput(newWebsitesArray);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();

    Shell shell = getShell();

    /* Minimum Size */
    int minWidth = convertHorizontalDLUsToPixels(OwlUI.MIN_DIALOG_WIDTH_DLU);
    int minHeight = shell.computeSize(minWidth, SWT.DEFAULT).y;

    /* Required Size */
    Point requiredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    shell.setSize(Math.max(minWidth, requiredSize.x), Math.max(minHeight, requiredSize.y));
    LayoutUtils.positionShell(shell);
  }
}