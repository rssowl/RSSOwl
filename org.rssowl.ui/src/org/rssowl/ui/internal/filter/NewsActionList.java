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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.newsaction.MoveNewsAction;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The {@link NewsActionList} offers the user a control to define
 * {@link NewsActionItem} that represent actions to perform on news.
 *
 * @author bpasero
 */
public class NewsActionList extends ScrolledComposite {
  private final NewsActionPresentationManager fNewsActionPresentationManager = NewsActionPresentationManager.getInstance();
  private List<NewsActionItem> fItems;
  private int fVisibleItemCount;
  private LocalResourceManager fResources;
  private Image fAddIcon;
  private Image fDeleteIcon;
  private Composite fContainer;

  /**
   * @param parent
   * @param style
   * @param actions the list of initial {@link IFilterAction} to show.
   */
  public NewsActionList(Composite parent, int style, List<IFilterAction> actions) {
    super(parent, style | SWT.V_SCROLL);

    fItems = new ArrayList<NewsActionItem>();
    fResources = new LocalResourceManager(JFaceResources.getResources(), this);

    initResources();
    initComponents(actions);
  }

  /**
   * Sets the number of <code>NewsActionItem</code>s that should be visible in
   * the List. If the number of items is higher, scrollbars will be shown
   * automatically.
   *
   * @param count the number of <code>NewsActionItem</code>s that should be
   * visible in the List.
   */
  public void setVisibleItemCount(int count) {
    Assert.isLegal(count >= 0);
    fVisibleItemCount = count;
  }

  /**
   * Returns <code>TRUE</code> when this List has no items with a specific
   * value, and <code>FALSE</code> otherwise.
   *
   * @return <code>TRUE</code> when this List has no items with a specific
   * value, and <code>FALSE</code> otherwise.
   */
  public boolean isEmpty() {
    for (NewsActionItem item : fItems) {
      if (item.hasValue())
        return false;
    }

    return true;
  }

  /**
   * Passes focus to the first Item in the list.
   */
  public void focusInput() {
    if (!fItems.isEmpty())
      fItems.get(0).focusInput();
  }

  /*
   * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
   */
  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    Point point = super.computeSize(wHint, hHint, changed);

    /* Compute from Action Item */
    if (fVisibleItemCount > 0 && fItems.size() > 0) {
      int itemHeight = fItems.get(0).computeSize(wHint, hHint).y + 4;
      point.y = fVisibleItemCount * itemHeight;
    }

    return point;
  }

  /**
   * @return a list of {@link IFilterAction} as defined by the user. Duplicate
   * actions are automatically ignored.
   */
  public List<IFilterAction> createActions() {
    List<IFilterAction> actions = new ArrayList<IFilterAction>(fItems.size());

    /* For each Item */
    for (NewsActionItem item : fItems) {
      IFilterAction action = item.createFilterAction(true);
      if (action != null)
        actions.add(action);
    }

    /* Delete Duplicate Actions */
    List<IFilterAction> duplicateActions = new ArrayList<IFilterAction>(0);
    for (IFilterAction action : actions) {

      /* Check if already Ignored */
      if (duplicateActions.contains(action))
        continue;

      /* Check for Actions to Ignore */
      for (IFilterAction otherAction : actions) {
        if (action == otherAction)
          continue;

        /* Same Action IDs */
        if (action.getActionId().equals(otherAction.getActionId())) {

          /* Ignore Action: Both Data is unspecified */
          if (action.getData() == null && otherAction.getData() == null)
            duplicateActions.add(otherAction);

          /* Ignore Action: Both Data is identical (Case: Object) */
          else if (action.getData() != null && action.getData().equals(otherAction.getData()))
            duplicateActions.add(otherAction);

          /* Ignore Action: Both Data is identical (Case: Arrays) */
          else if (action.getData() != null && action.getData() instanceof Object[]) {
            Object[] data = (Object[]) action.getData();
            if (otherAction.getData() instanceof Object[]) {
              Object[] otherData = (Object[]) otherAction.getData();
              if (Arrays.equals(data, otherData))
                duplicateActions.add(otherAction);
            }
          }
        }
      }
    }

    /* Remove Actions to Ignore */
    actions.removeAll(duplicateActions);

    return actions;
  }

  /**
   * Shows the List of <code>IFilterAction</code> in this List.
   *
   * @param actions the List of <code>IFilterAction</code> to show in this List.
   */
  public void showActions(List<IFilterAction> actions) {
    setRedraw(false);
    try {

      /* Remove all */
      List<NewsActionItem> itemsToRemove = new ArrayList<NewsActionItem>(fItems);
      for (NewsActionItem itemToRemove : itemsToRemove) {
        itemToRemove.getParent().dispose();
        removeItem(itemToRemove);
      }

      /* Add Actions */
      if (actions != null) {
        boolean addDefaultAction = true;
        for (IFilterAction action : actions) {
          if (fNewsActionPresentationManager.hasNewsAction(action.getActionId())) {
            addItem(action);
            addDefaultAction = false;
          }
        }

        if (addDefaultAction)
          addItem(getDefaultAction());
      }
    } finally {
      setRedraw(true);
    }
  }

  private void initResources() {
    fAddIcon = OwlUI.getImage(fResources, "icons/etool16/add.gif"); //$NON-NLS-1$
    fDeleteIcon = OwlUI.getImage(fResources, "icons/etool16/remove.gif"); //$NON-NLS-1$
  }

  private void initComponents(List<IFilterAction> actions) {

    /* Adjust Scrolled Composite */
    setLayout(new GridLayout(1, false));
    setExpandHorizontal(true);
    setExpandVertical(true);
    if (getVerticalBar() != null)
      getVerticalBar().setIncrement(10);

    /* Create the Container */
    fContainer = new Composite(this, SWT.NONE);
    fContainer.setLayout(LayoutUtils.createGridLayout(1, 0, 0));
    fContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    setContent(fContainer);

    /* Add Actions */
    if (actions != null) {
      for (IFilterAction action : actions)
        addItem(action);
    }

    /* Update Size */
    updateSize();
  }

  NewsActionItem addItem(IFilterAction action) {
    return addItem(action, fItems.size());
  }

  NewsActionItem addItem(IFilterAction action, int index) {
    return addItem(action, index, false);
  }

  NewsActionItem addItem(IFilterAction action, int index, boolean scroll) {
    boolean wasScrollbarShowing = getVerticalBar() != null ? getVerticalBar().isVisible() : false;

    /* Container for Item */
    final Composite itemContainer = new Composite(fContainer, SWT.NONE);
    itemContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0, 0, 0, false));
    itemContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Create Item */
    final NewsActionItem item = new NewsActionItem(itemContainer, SWT.NONE, action);
    item.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

    /* Create Button Box */
    final ToolBar buttonBar = new ToolBar(itemContainer, SWT.FLAT);
    buttonBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

    /* Button to add Action */
    ToolItem addButton = new ToolItem(buttonBar, SWT.DROP_DOWN);
    addButton.setImage(fAddIcon);
    addButton.setToolTipText(Messages.NewsActionList_ADD_ACTION);

    /* Add Menu */
    final Menu actionMenu = new Menu(buttonBar);
    createActionMenu(actionMenu, item);
    addButton.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event) {
        if (event.detail == SWT.ARROW) {
          Rectangle rect = item.getBounds();
          Point pt = new Point(rect.x, rect.y + rect.height);
          pt = buttonBar.toDisplay(pt);
          actionMenu.setLocation(pt.x, pt.y);
          actionMenu.setVisible(true);
        } else
          onAdd(item);
      }
    });

    buttonBar.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        OwlUI.safeDispose(actionMenu);
      }
    });

    /* Button to delete Action */
    ToolItem deleteButton = new ToolItem(buttonBar, SWT.PUSH);
    deleteButton.setImage(fDeleteIcon);
    deleteButton.setToolTipText(Messages.NewsActionList_DELETE_ACTION);
    deleteButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        JobRunner.runInUIThread(0, true, buttonBar, new Runnable() {
          @Override
          public void run() {
            onDelete(item, itemContainer);
          }
        });
      }
    });

    /* Add to the End */
    boolean addedToEnd = false;
    if (index == fItems.size()) {
      addedToEnd = true;
      fItems.add(item);
    }

    /* Add to specific Index */
    else {
      NewsActionItem oldItem = fItems.get(index);
      fItems.add(index, item);
      item.getParent().moveAbove(oldItem.getParent());
    }

    /* Force Layout */
    layout(true, true);
    update();

    /* Update Size */
    updateSize();
    OwlUI.adjustSizeForScrollbar(getShell(), getVerticalBar(), wasScrollbarShowing);

    /* Scroll to Bottom if added as last element */
    if (scroll && addedToEnd)
      setOrigin(0, getContent().getSize().y);

    return item;
  }

  private void createActionMenu(Menu menu, NewsActionItem item) {
    NewsActionPresentationManager manager = NewsActionPresentationManager.getInstance();
    Collection<NewsActionDescriptor> actions = manager.getSortedNewsActions();

    String lastSortKey = null;
    for (NewsActionDescriptor action : actions) {
      if (lastSortKey != null && lastSortKey.charAt(0) != action.getSortKey().charAt(0))
        new MenuItem(menu, SWT.SEPARATOR);

      MenuItem mItem = new MenuItem(menu, SWT.PUSH);
      mItem.setText(action.getName());
      hookSelectionListener(mItem, item, action.getActionId());

      lastSortKey = action.getSortKey();
    }
  }

  private void hookSelectionListener(MenuItem item, final NewsActionItem action, final String actionId) {
    item.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        onAdd(action, actionId);
      }
    });
  }

  void onAdd(NewsActionItem selectedItem) {
    IFilterAction filterAction = createAction(selectedItem.createFilterAction(false));
    addItem(filterAction, indexOf(selectedItem) + 1, true);
  }

  void onAdd(NewsActionItem selectedItem, String actionId) {
    IFilterAction filterAction = Owl.getModelFactory().createFilterAction(actionId);
    addItem(filterAction, indexOf(selectedItem) + 1, true);
  }

  int indexOf(NewsActionItem item) {
    return fItems.indexOf(item);
  }

  void onDelete(final NewsActionItem item, final Composite itemContainer) {
    boolean wasScrollbarShowing = getVerticalBar() != null ? getVerticalBar().isVisible() : false;

    /* Delete */
    itemContainer.dispose();
    removeItem(item);

    /* Restore Default if required */
    if (fItems.size() == 0)
      addItem(getDefaultAction());

    OwlUI.adjustSizeForScrollbar(getShell(), getVerticalBar(), wasScrollbarShowing);
  }

  private IFilterAction createAction(IFilterAction current) {
    IModelFactory factory = Owl.getModelFactory();
    return factory.createFilterAction(current.getActionId());
  }

  private IFilterAction getDefaultAction() {
    IModelFactory factory = Owl.getModelFactory();
    return factory.createFilterAction(MoveNewsAction.ID); //TODO Layer break
  }

  void removeItem(NewsActionItem item) {

    /* Dispose and Remove */
    item.dispose();
    fItems.remove(item);

    /* Force Layout */
    layout(true, true);
    update();

    /* Update Size */
    updateSize();
  }

  private void updateSize() {
    setMinSize(fContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  /**
   * @return <code>true</code> if any item in the list was modified and
   * <code>false</code> otherwise.
   */
  public boolean isModified() {
    return true; //TODO Improve
  }
}