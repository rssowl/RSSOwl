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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.ITreeNode;
import org.rssowl.core.util.ModelTreeNode;
import org.rssowl.core.util.TreeTraversal;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;
import org.rssowl.ui.internal.editors.feed.PerformAfterInputSet;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * The <code>NavigationActionFactory</code> is providing a list of common
 * Actions to navigate to News-Items or Feeds.
 *
 * @author bpasero
 */
public class NavigationActionFactory implements IExecutableExtensionFactory, IExecutableExtension {
  private String fId;

  /** Actual Action for the Navigation */
  public static class NavigationAction implements IWorkbenchWindowActionDelegate {
    private final NavigationActionType fType;

    /**
     * @param type the type of Navigation.
     */
    public NavigationAction(NavigationActionType type) {
      fType = type;
    }

    public void dispose() {}

    public void init(IWorkbenchWindow window) {}

    public void run(IAction action) {

      /* Tab Navigation */
      if (fType == NavigationActionType.NEXT_TAB || fType == NavigationActionType.PREVIOUS_TAB) {
        navigateInTabs();
      }

      /* News/Feed Navigation */
      else {

        /* 1.) Navigate in opened FeedView */
        if (fType.isNewsScoped() && navigateOnActiveFeedView())
          return;

        /* 2.) Navigate in opened Explorer */
        if (navigateOnOpenExplorer())
          return;

        /* 3.) Navigate on entire Model */
        if (navigateOnModel())
          return;
      }
    }

    private void navigateInTabs() {

      /* Current Active Editor */
      IEditorPart activeEditor = OwlUI.getActiveEditor();
      if (activeEditor == null)
        return;

      List<IEditorReference> editors = OwlUI.getEditorReferences();

      int index = -1;
      for (int i = 0; i < editors.size(); i++) {
        try {
          if (activeEditor.getEditorInput().equals(editors.get(i).getEditorInput())) {
            index = i;
            break;
          }
        } catch (PartInitException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }

      if (index < 0)
        return;

      IEditorPart tab = null;

      /* Next Tab */
      if (fType == NavigationActionType.NEXT_TAB)
        tab = editors.get(index + 1 < editors.size() ? index + 1 : 0).getEditor(true);

      /* Previous Tab */
      else if (fType == NavigationActionType.PREVIOUS_TAB)
        tab = editors.get(index - 1 >= 0 ? index - 1 : editors.size() - 1).getEditor(true);

      /* Activate */
      if (tab != null) {
        IWorkbenchPage page = tab.getSite().getPage();
        page.activate(tab.getSite().getPart());
        page.activate(tab);
      }
    }

    private boolean navigateOnActiveFeedView() {

      /* Get active FeedView if any */
      FeedView activeFeedView = OwlUI.getActiveFeedView();

      /* Run on active FeedView if any */
      if (activeFeedView != null) {
        boolean success = activeFeedView.navigate(true, false, fType.isNext(), fType.isUnread());

        /* For unread & next news, consider all news of the active feed */
        if (!success && fType.isNewsScoped() && fType.isUnread() && fType.isNext())
          success = activeFeedView.navigate(false, false, fType.isNext(), fType.isUnread());

        if (success) {
          IWorkbenchPage page = activeFeedView.getSite().getPage();
          page.activate(activeFeedView.getSite().getPart());
          page.activate(activeFeedView);

          return true;
        }
      }

      return false;
    }

    private boolean navigateOnOpenExplorer() {

      /* Try finding the open Explorer for BookMarks */
      BookMarkExplorer bookmarkExplorer = OwlUI.getOpenedBookMarkExplorer();
      if (bookmarkExplorer == null)
        return false;

      /* Navigate on Explorer */
      bookmarkExplorer.navigate(fType.isNext(), fType.isUnread(), fType.performOnFeedView());

      return true; //Avoid navigation on Model if Explorer is Opened
    }

    private boolean navigateOnModel() {
      List<IFolderChild> startingNodes = new ArrayList<IFolderChild>();

      /* Check Current Active FeedView */
      FeedView activeFeedView = OwlUI.getActiveFeedView();
      if (activeFeedView != null) {
        IEditorInput input = activeFeedView.getEditorInput();
        if (input != null && input instanceof FeedViewInput) {
          INewsMark mark = ((FeedViewInput) input).getMark();
          if (mark != null)
            startingNodes.add(mark instanceof FolderNewsMark ? ((FolderNewsMark) mark).getFolder() : mark);
        }
      }

      /* Add all Root Folders */
      startingNodes.addAll(CoreUtils.loadRootFolders());

      /* Select from all available Starting Nodes */
      ITreeNode targetNode = null;
      for (IFolderChild startingNode : startingNodes) {
        TreeTraversal traversal = new TreeTraversal(startingNode instanceof IFolder ? new ModelTreeNode((IFolder) startingNode) : new ModelTreeNode((IMark) startingNode)) {

          @Override
          public boolean select(ITreeNode node) {
            Object data = node.getData();

            /* Check for Unread news if required */
            if (data instanceof INewsMark) {
              INewsMark newsmark = (INewsMark) data;
              if (fType.isUnread() && newsmark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)) == 0)
                return false;
            }

            /* Folders are no valid navigation nodes */
            else if (data instanceof IFolder)
              return false;

            return true;
          }
        };

        targetNode = fType.isNext() ? traversal.nextNode() : traversal.previousNode();
        if (targetNode != null)
          break;
      }

      /* Open Node if present */
      if (targetNode != null) {
        INewsMark mark = (INewsMark) targetNode.getData();

        /* Open in FeedView */
        PerformAfterInputSet perform = null;
        if (fType.isNewsScoped() && fType.isUnread())
          perform = PerformAfterInputSet.SELECT_UNREAD_NEWS;
        else if (fType.isNewsScoped())
          perform = PerformAfterInputSet.SELECT_FIRST_NEWS;

        IWorkbenchPage page = OwlUI.getPage();
        if (page != null)
          OwlUI.openInFeedView(page, new StructuredSelection(mark), true, false, perform);
      }

      return targetNode != null;
    }

    public void selectionChanged(IAction action, ISelection selection) {}
  }

  /** Enumeration with all possible types of NavigationAction */
  public enum NavigationActionType {

    /** Action: Go to the next News */
    NEXT_NEWS("nextNews", "org.rssowl.ui.NextNews", Messages.NavigationActionFactory_NEXT_NEWS, true, true, false), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the next unread News */
    NEXT_UNREAD_NEWS("nextUnreadNews", "org.rssowl.ui.NextUnreadNews", Messages.NavigationActionFactory_NEXT_UNREAD_NEWS, true, true, true), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the next Feed */
    NEXT_FEED("nextFeed", "org.rssowl.ui.NextFeed", Messages.NavigationActionFactory_NEXT_FEED, false, true, false), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the next unread Feed */
    NEXT_UNREAD_FEED("nextUnreadFeed", "org.rssowl.ui.NextUnreadFeed", Messages.NavigationActionFactory_NEXT_UNREAD_FEED, false, true, true), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the previous News */
    PREVIOUS_NEWS("previousNews", "org.rssowl.ui.PreviousNews", Messages.NavigationActionFactory_PREVIOUS_NEWS, true, false, false), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the previous unread News */
    PREVIOUS_UNREAD_NEWS("previousUnreadNews", "org.rssowl.ui.PreviousUnreadNews", Messages.NavigationActionFactory_PREVIOUS_UNREAD_NEWS, true, false, true), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the previous Feed */
    PREVIOUS_FEED("previousFeed", "org.rssowl.ui.PreviousFeed", Messages.NavigationActionFactory_PREVIOUS_FEED, false, false, false), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to the previous unread Feed */
    PREVIOUS_UNREAD_FEED("previousUnreadFeed", "org.rssowl.ui.PreviousUnreadFeed", Messages.NavigationActionFactory_PREVIOUS_UNREAD_FEED, false, false, true), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to next Tab */
    NEXT_TAB("nextTab", "org.rssowl.ui.NextTab", Messages.NavigationActionFactory_NEXT_TAB, false, false, false), //$NON-NLS-1$ //$NON-NLS-2$

    /** Action: Go to previous Tab */
    PREVIOUS_TAB("previousTab", "org.rssowl.ui.PreviousTab", Messages.NavigationActionFactory_PREVIOUS_TAB, false, false, false), //$NON-NLS-1$ //$NON-NLS-2$

    /** Special Combined Actions only executed from Browser Viewer */
    NEXT_FEED_NEXT_NEWS("nextFeedNextNews", null, null, false, true, false, true), //$NON-NLS-1$
    NEXT_UNREAD_FEED_NEXT_UNREAD_NEWS("nextUnreadFeedNextUnreadNews", null, null, false, true, true, true), //$NON-NLS-1$
    PREVIOUS_FEED_PREVIOUS_NEWS("previousFeedPreviousNews", null, null, false, false, false, true), //$NON-NLS-1$
    PREVIOUS_UNREAD_FEED_PREVIOUS_UNREAD_NEWS("previousUnreadFeedPreviousUnreadNews", null, null, false, false, true, true); //$NON-NLS-1$

    String fId;
    String fCommandId;
    private String fName;
    boolean fIsNewsScoped;
    boolean fIsNext;
    boolean fIsUnread;
    boolean fPerformOnFeedView;

    NavigationActionType(String id, String commandId, String name, boolean isNewsScoped, boolean isNext, boolean isUnread) {
      this(id, commandId, name, isNewsScoped, isNext, isUnread, isNewsScoped);
    }

    NavigationActionType(String id, String commandId, String name, boolean isNewsScoped, boolean isNext, boolean isUnread, boolean performOnFeedView) {
      fId = id;
      fCommandId = commandId;
      fName = name;
      fIsNewsScoped = isNewsScoped;
      fIsNext = isNext;
      fIsUnread = isUnread;
      fPerformOnFeedView = performOnFeedView;
    }

    /**
     * @return the id of this navigation action.
     */
    public String getId() {
      return fId;
    }

    /**
     * @return the id of the command for this navigation action.
     */
    public String getCommandId() {
      return fCommandId;
    }

    /**
     * @return the human readable name of this navigation action.
     */
    public String getName() {
      return fName;
    }

    /**
     * @return <code>true</code> if the navigation is around news and
     * <code>false</code> in the case of feeds.
     */
    boolean isNewsScoped() {
      return fIsNewsScoped;
    }

    /**
     * @return <code>true</code> if navigation should only consider unread items
     * and <code>false</code> otherwise.
     */
    boolean isUnread() {
      return fIsUnread;
    }

    /**
     * @return <code>true</code> if the navigation goes to the next item and
     * <code>false</code> otherwise.
     */
    boolean isNext() {
      return fIsNext;
    }

    /**
     * @return <code>true</code> if the navigational action should be performed
     * on the feedview and <code>false</code> otherwise.
     */
    public boolean performOnFeedView() {
      return fPerformOnFeedView;
    }
  };

  /** Keep for reflection */
  public NavigationActionFactory() {}

  /*
   * @see org.eclipse.core.runtime.IExecutableExtensionFactory#create()
   */
  public Object create() {
    if (NavigationActionType.NEXT_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_NEWS);

    if (NavigationActionType.NEXT_UNREAD_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_UNREAD_NEWS);

    if (NavigationActionType.NEXT_FEED.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_FEED);

    if (NavigationActionType.NEXT_UNREAD_FEED.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_UNREAD_FEED);

    if (NavigationActionType.NEXT_FEED_NEXT_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_FEED_NEXT_NEWS);

    if (NavigationActionType.NEXT_UNREAD_FEED_NEXT_UNREAD_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_UNREAD_FEED_NEXT_UNREAD_NEWS);

    if (NavigationActionType.PREVIOUS_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_NEWS);

    if (NavigationActionType.PREVIOUS_UNREAD_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_UNREAD_NEWS);

    if (NavigationActionType.PREVIOUS_FEED.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_FEED);

    if (NavigationActionType.PREVIOUS_UNREAD_FEED.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_UNREAD_FEED);

    if (NavigationActionType.PREVIOUS_FEED_PREVIOUS_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_FEED_PREVIOUS_NEWS);

    if (NavigationActionType.PREVIOUS_UNREAD_FEED_PREVIOUS_UNREAD_NEWS.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_UNREAD_FEED_PREVIOUS_UNREAD_NEWS);

    if (NavigationActionType.NEXT_TAB.getId().equals(fId))
      return new NavigationAction(NavigationActionType.NEXT_TAB);

    if (NavigationActionType.PREVIOUS_TAB.getId().equals(fId))
      return new NavigationAction(NavigationActionType.PREVIOUS_TAB);

    return null;
  }

  /*
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   * java.lang.String, java.lang.Object)
   */
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    if (data instanceof String)
      fId = (String) data;
    else
      throw new CoreException(Activator.getDefault().createErrorStatus("Data argument must be a String for " + getClass(), null)); //$NON-NLS-1$
  }
}