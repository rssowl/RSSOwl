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

package org.rssowl.ui.internal.dialogs.properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An instance of <code>Dialog</code> providing a TabFolder to edit the
 * properties of Entities. The pages of the TabFolder can be added after the
 * Dialog has been instantiated. This allows to collect pages from
 * contributions.
 *
 * @author bpasero
 */
public class EntityPropertyDialog extends Dialog implements IPropertyDialogSite {
  private List<IEntity> fEntities;
  private String fTitle;
  private Set<EntityPropertyPageWrapper> fPages = new TreeSet<EntityPropertyPageWrapper>();
  private Set<IEntityPropertyPage> fCreatedPages = new HashSet<IEntityPropertyPage>();
  private Label fMessageText;
  private Label fMessageImage;
  private LocalResourceManager fResources;
  private TabFolder fTabFolder;
  private String fInitialMessage;
  private MessageType fInitialMessageType;
  private boolean fEntitiesUpdated;

  /**
   * @param parentShell
   * @param entities
   */
  public EntityPropertyDialog(Shell parentShell, List<IEntity> entities) {
    super(parentShell);
    fEntities = entities;
    fResources = new LocalResourceManager(JFaceResources.getResources());
  }

  /**
   * @param page
   */
  public void addPage(EntityPropertyPageWrapper page) {
    fPages.add(page);
    page.getPage().init(this, fEntities);
  }

  /**
   * @param title
   */
  public void setTitle(String title) {
    fTitle = title;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#close()
   */
  @Override
  public boolean close() {
    super.close();
    fResources.dispose();

    return true;
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    final boolean proceed[] = new boolean[] { true };
    final Set<IEntity> entitiesToSave = new HashSet<IEntity>();

    /* Notify any Page and perform in Safe-Runner */
    for (Iterator<EntityPropertyPageWrapper> iterator = fPages.iterator(); iterator.hasNext() && proceed[0];) {
      final EntityPropertyPageWrapper pageWrapper = iterator.next();
      if (fCreatedPages.contains(pageWrapper.getPage())) {
        SafeRunner.run(new LoggingSafeRunnable() {
          @Override
          public void run() throws Exception {
            proceed[0] = pageWrapper.getPage().performOk(entitiesToSave);
          }
        });
      }
    }

    /* Operation Canceld */
    if (!proceed[0])
      return;

    /* Save Entities while showing Busy-Cursor */
    fEntitiesUpdated = !entitiesToSave.isEmpty();
    BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
      @Override
      public void run() {
        for (IEntity entity : entitiesToSave)
          DynamicDAO.save(entity);
      }
    });

    /* Tell pages that the Dialog is about to close */
    for (EntityPropertyPageWrapper pageWrapper : fPages) {
      if (fCreatedPages.contains(pageWrapper.getPage()))
        pageWrapper.getPage().finish();
    }

    /* Proceed */
    super.okPressed();
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    if (StringUtils.isSet(fTitle))
      shell.setText(NLS.bind(Messages.EntityPropertyDialog_PROPERTIES_FOR_N, fTitle));
    else
      shell.setText(Messages.EntityPropertyDialog_PROPERTIES);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {

    /* Composite to hold all components */
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 5, 0, 3));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    ((GridLayout) composite.getLayout()).marginTop = 10;

    /* TabFolder containing the Pages */
    fTabFolder = new TabFolder(composite, SWT.None);
    fTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    fTabFolder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onTabSelect(e);
      }
    });

    /* For each Page - create TabItem */
    int i = 0;
    Iterator<EntityPropertyPageWrapper> iterator = fPages.iterator();
    while (iterator.hasNext()) {
      EntityPropertyPageWrapper pageWrapper = iterator.next();
      TabItem item = new TabItem(fTabFolder, SWT.None);
      IEntityPropertyPage page = pageWrapper.getPage();
      item.setData(page);
      item.setText(pageWrapper.getName());
      if (page.getImage() != null)
        item.setImage(OwlUI.getImage(fResources, page.getImage()));

      /* Create the first page but not the others yet */
      if (i == 0) {
        item.setControl(page.createContents(fTabFolder));
        fCreatedPages.add(page);
        page.setFocus();
      }

      i++;
    }

    /* Message Area */
    fMessageImage = new Label(composite, SWT.None);
    fMessageImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    ((GridData) fMessageImage.getLayoutData()).widthHint = 16;
    ((GridData) fMessageImage.getLayoutData()).heightHint = 16;
    fMessageText = new Label(composite, SWT.None);
    fMessageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    /* Show Initial if provided */
    if (fInitialMessage != null && fInitialMessageType != null)
      setMessage(fInitialMessage, fInitialMessageType);

    applyDialogFont(composite);

    return composite;
  }

  private void onTabSelect(SelectionEvent e) {
    if (e.item instanceof TabItem && e.item.getData() instanceof IEntityPropertyPage) {
      TabItem item = (TabItem) e.item;

      /* Create Page if not yet done */
      IEntityPropertyPage page = (IEntityPropertyPage) e.item.getData();
      if (item.getControl() == null) {
        item.setControl(page.createContents(fTabFolder));
        fCreatedPages.add(page);

        /* Check if the Dialog size needs to be updated now */
        Shell shell = getShell();
        Point requiredSize = getRequiredSize(shell);
        Point currentSize = shell.getSize();
        if (requiredSize.x > currentSize.x || requiredSize.y > currentSize.y)
          shell.setSize(new Point(Math.max(currentSize.x, requiredSize.x), Math.max(currentSize.y, requiredSize.y)));
      }

      /* Pass Focus to Page */
      page.setFocus();
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {
    Composite buttonBar = (Composite) super.createButtonBar(parent);

    int marginHeight = ((GridLayout) buttonBar.getLayout()).marginHeight;
    ((GridLayout) buttonBar.getLayout()).marginHeight = 0;
    ((GridLayout) buttonBar.getLayout()).marginTop = 3;
    ((GridLayout) buttonBar.getLayout()).marginBottom = marginHeight;

    return buttonBar;
  }

  /*
   * @see org.eclipse.jface.window.Window#getShellStyle()
   */
  @Override
  protected int getShellStyle() {
    int style = SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.CLOSE | getDefaultOrientation();

    return style;
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

    Point requiredSize = getRequiredSize(shell);
    shell.setSize(Math.max(minWidth, requiredSize.x), Math.max(minHeight, requiredSize.y));
    LayoutUtils.positionShell(shell);
  }

  private Point getRequiredSize(Shell shell) {
    Point requiredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    /* Bug in SWT: The preferred width of the state condition is wrong */
    if (displaysSavedSearch()) {
      if (Application.IS_LINUX)
        requiredSize.x = requiredSize.x + 100;
      else if (Application.IS_MAC)
        requiredSize.x = requiredSize.x + 50;
    }

    return requiredSize;
  }

  private boolean displaysSavedSearch() {
    for (IEntity entity : fEntities) {
      if (entity instanceof ISearchMark)
        return true;
    }

    return false;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IPropertyDialogSite#getHorizontalPixels(int)
   */
  @Override
  public int getHorizontalPixels(int dlus) {
    return convertHorizontalDLUsToPixels(dlus);
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IPropertyDialogSite#setMessage(java.lang.String,
   * org.rssowl.ui.dialogs.properties.IPropertyDialogSite.MessageType)
   */
  @Override
  public void setMessage(String message, MessageType type) {

    /* Mask NULL */
    if (message == null)
      message = ""; //$NON-NLS-1$

    /* Return early if not created yet */
    if (fMessageImage == null || fMessageText == null) {
      fInitialMessage = message;
      fInitialMessageType = type;
      return;
    }

    /* Update Image */
    if (message.length() == 0)
      fMessageImage.setImage(null);
    else if (type.equals(IPropertyDialogSite.MessageType.INFO))
      fMessageImage.setImage(OwlUI.getImage(fResources, OwlUI.INFO));
    else if (type.equals(IPropertyDialogSite.MessageType.WARNING))
      fMessageImage.setImage(OwlUI.getImage(fResources, OwlUI.WARNING));
    else if (type.equals(IPropertyDialogSite.MessageType.ERROR))
      fMessageImage.setImage(OwlUI.getImage(fResources, OwlUI.ERROR));

    /* Update Message */
    fMessageText.setText(message);
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IPropertyDialogSite#select(org.rssowl.ui.dialogs.properties.IEntityPropertyPage)
   */
  @Override
  public void select(IEntityPropertyPage page) {
    Assert.isNotNull(page);

    /* Guard against being disposed */
    if (fTabFolder.isDisposed())
      return;

    /* Select TabItem that shows given Page */
    TabItem[] items = fTabFolder.getItems();
    for (TabItem tabItem : items) {
      if (page.equals(tabItem.getData())) {
        fTabFolder.setSelection(tabItem);
        break;
      }
    }
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IPropertyDialogSite#getResourceManager()
   */
  @Override
  public ResourceManager getResourceManager() {
    return fResources;
  }

  /**
   * @return <code>true</code> if entities have been modified and saved, or
   * <code>false</code> otherwise.
   */
  public boolean entitiesUpdated() {
    return fEntitiesUpdated;
  }

  /*
   * @see org.rssowl.ui.dialogs.properties.IPropertyDialogSite#contentsChanged()
   */
  @Override
  public void contentsChanged() {

    /* Check if the Dialog size needs to be updated now */
    Shell shell = getShell();
    Point requiredSize = getRequiredSize(shell);
    Point currentSize = shell.getSize();
    if (requiredSize.x > currentSize.x || requiredSize.y > currentSize.y)
      shell.setSize(new Point(Math.max(currentSize.x, requiredSize.x), Math.max(currentSize.y, requiredSize.y)));
  }
}