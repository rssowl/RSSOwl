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

package org.rssowl.ui.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.FolderChooser;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * @author bpasero
 */
public class NewFolderAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate {
  private Shell fShell;
  private IFolder fParent;
  private IMark fPosition;
  private boolean fRootMode;

  private class NewFolderDialog extends TitleAreaDialog {
    private Text fNameInput;
    private ResourceManager fResources;
    private String fName;
    private IFolder fFolder;
    private FolderChooser fFolderChooser;

    NewFolderDialog(Shell shell, IFolder folder) {
      super(shell);
      fFolder = folder;
      fResources = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void create() {
      super.create();
      getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
      fName = fNameInput.getText();
      super.okPressed();
    }

    @Override
    public boolean close() {
      boolean res = super.close();
      fResources.dispose();
      return res;
    }

    @Override
    protected void configureShell(Shell newShell) {
      newShell.setText(fRootMode ? Messages.NewFolderAction_NEW_SET : Messages.NewFolderAction_NEW_FOLDER);
      super.configureShell(newShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {

      /* Separator */
      new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      /* Title */
      setTitle(Messages.NewFolderAction_FOLDER);

      /* Title Image */
      setTitleImage(OwlUI.getImage(fResources, fRootMode ? "icons/wizban/bkmrk_set_title.gif" : "icons/wizban/folder_wiz.gif")); //$NON-NLS-1$ //$NON-NLS-2$

      /* Title Message */
      if (fRootMode)
        setMessage(Messages.NewFolderAction_NEW_SET_MSG);
      else
        setMessage(Messages.NewFolderAction_NEW_FOLDER_MSG);

      Composite container = new Composite(parent, SWT.NONE);
      container.setLayout(LayoutUtils.createGridLayout(2, 5, 5));
      container.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

      Label l1 = new Label(container, SWT.NONE);
      l1.setText(Messages.NewFolderAction_NAME);

      fNameInput = new Text(container, SWT.SINGLE | SWT.BORDER);
      fNameInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      fNameInput.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          validateInput();
        }
      });

      if (!fRootMode) {
        Label l2 = new Label(container, SWT.NONE);
        l2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        l2.setText(Messages.NewFolderAction_LOCATION);

        /* Folder Chooser */
        fFolderChooser = new FolderChooser(container, fFolder, SWT.BORDER, true);
        fFolderChooser.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        fFolderChooser.setLayout(LayoutUtils.createGridLayout(1, 0, 0, 2, 5, false));
        fFolderChooser.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      }

      applyDialogFont(container);

      return container;
    }

    @Override
    protected void initializeBounds() {
      super.initializeBounds();
      Point bestSize = getShell().computeSize(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT);
      getShell().setSize(bestSize.x, bestSize.y);
      LayoutUtils.positionShell(getShell());
    }

    private void validateInput() {
      boolean valid = fNameInput.getText().length() > 0;
      Control button = getButton(IDialogConstants.OK_ID);
      button.setEnabled(valid);
    }

    IFolder getFolder() {
      return fRootMode ? null : fFolderChooser.getFolder();
    }
  }

  /** Keep for Reflection */
  public NewFolderAction() {
    this(null, null, null);
  }

  /**
   * @param shell
   * @param parent
   * @param position
   */
  public NewFolderAction(Shell shell, IFolder parent, IMark position) {
    fShell = shell;
    fParent = parent;
    fPosition = position;
  }

  /**
   * @param rootMode If <code>TRUE</code>, creates new Folders on the root,
   * thereby making them Bookmark-Sets.
   */
  public void setRootMode(boolean rootMode) {
    fRootMode = rootMode;
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose() {}

  /*
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init(IWorkbenchWindow window) {
    fShell = window.getShell();
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    internalRun();
  }

  private void internalRun() throws PersistenceException {

    /* Get the parent Folder */
    IFolder parent = fRootMode ? null : OwlUI.getSelectedParent(fParent);

    /* Show Dialog */
    NewFolderDialog dialog = new NewFolderDialog(fShell, parent);
    if (dialog.open() == Window.OK) {
      String name = dialog.fName;
      parent = dialog.getFolder();

      /* Create the Folder */
      if (fRootMode || parent != null) {
        IFolder folder = Owl.getModelFactory().createFolder(null, parent, name, fPosition, fPosition != null ? true : null);

        /* Copy all Properties from Parent into this Mark */
        if (parent != null) {
          Map<String, Serializable> properties = parent.getProperties();
          for (Map.Entry<String, Serializable> property : properties.entrySet())
            folder.setProperty(property.getKey(), property.getValue());
        }

        DynamicDAO.save(fRootMode ? folder : parent);
      }
    }
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {

    /* Delete the old Selection */
    fParent = null;
    fPosition = null;

    /* Check Selection */
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structSel = (IStructuredSelection) selection;
      if (!structSel.isEmpty()) {
        Object firstElement = structSel.getFirstElement();
        if (firstElement instanceof IFolder)
          fParent = (IFolder) firstElement;
        else if (firstElement instanceof IMark) {
          fParent = ((IMark) firstElement).getParent();
          fPosition = ((IMark) firstElement);
        }
      }
    }
  }

  /*
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   * org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    fShell = targetPart.getSite().getShell();
  }
}