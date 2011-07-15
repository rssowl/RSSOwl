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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.FolderChooser;
import org.rssowl.ui.internal.util.FolderChooser.ExpandStrategy;
import org.rssowl.ui.internal.util.LayoutUtils;
import org.rssowl.ui.internal.util.ModelUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * The {@link FolderWizard} is only used in the Eclipse Integration to create
 * new Folders.
 *
 * @author bpasero
 */
public class FolderWizard extends Wizard implements INewWizard {
  private NewFolderWizardPage fPage;
  private IStructuredSelection fSelection;

  /* Page for Wizard */
  private class NewFolderWizardPage extends WizardPage {
    private FolderChooser fFolderChooser;
    private Text fNameInput;

    NewFolderWizardPage(String pageName) {
      super(pageName, pageName, OwlUI.getImageDescriptor("icons/wizban/folder_wiz.gif")); //$NON-NLS-1$
      setMessage(Messages.FolderWizard_FOLDER_WIZ_TITLE);
    }

    public void createControl(Composite parent) {
      Composite control = new Composite(parent, SWT.NONE);
      control.setLayout(LayoutUtils.createGridLayout(2, 5, 5));
      control.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      Label l1 = new Label(control, SWT.NONE);
      l1.setText(Messages.FolderWizard_NAME);

      fNameInput = new Text(control, SWT.SINGLE | SWT.BORDER);
      fNameInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      fNameInput.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          setErrorMessage(null);
        }
      });

      Label l2 = new Label(control, SWT.NONE);
      l2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
      l2.setText(Messages.FolderWizard_LOCATION);

      /* Folder Chooser */
      fFolderChooser = new FolderChooser(control, getParent(), SWT.BORDER, true);
      fFolderChooser.setExpandStrategy(ExpandStrategy.PACK);
      fFolderChooser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      fFolderChooser.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 5, false));
      fFolderChooser.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

      setControl(control);
    }

    String getFolderName() {
      return fNameInput.getText();
    }

    void focusInput() {
      fNameInput.setFocus();
      fNameInput.selectAll();
    }

    IFolder getFolder() {
      return fFolderChooser.getFolder();
    }
  }

  /** Leave for Reflection */
  public FolderWizard() {}

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    fPage = new NewFolderWizardPage(Messages.FolderWizard_NEW_FOLDER);
    setWindowTitle(Messages.FolderWizard_FOLDER);
    setHelpAvailable(false);
    addPage(fPage);
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    fSelection = selection;
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    String name = fPage.getFolderName();
    IModelFactory factory = Owl.getModelFactory();

    /* Require Name */
    if (!StringUtils.isSet(name)) {
      fPage.setErrorMessage(Messages.FolderWizard_ENTER_NAME);
      fPage.focusInput();
      return false;
    }

    /* Get the parent Folder */
    IFolder parent = fPage.getFolder();

    /* Create the Folder */
    IFolder folder = factory.createFolder(null, parent, name);

    /* Copy all Properties from Parent into this Mark */
    if (parent != null) {
      Map<String, Serializable> properties = parent.getProperties();
      for (Map.Entry<String, Serializable> property : properties.entrySet())
        folder.setProperty(property.getKey(), property.getValue());
    }

    DynamicDAO.getDAO(IFolderDAO.class).save(parent);
    return true;
  }

  private IFolder getParent() throws PersistenceException {
    Pair<IFolder, IFolderChild> pair = ModelUtils.getLocationAndPosition(fSelection);
    return pair.getFirst();
  }
}