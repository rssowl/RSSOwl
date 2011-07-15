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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.FeedView;
import org.rssowl.ui.internal.editors.feed.FeedViewInput;
import org.rssowl.ui.internal.editors.feed.PerformAfterInputSet;
import org.rssowl.ui.internal.util.EditorUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>OpenNewsAction</code> will open a given Selection of
 * <code>INews</code> in the <code>FeedView</code> opening the related BookMark
 * and adjusting the selection.
 *
 * @author bpasero
 */
public class OpenNewsAction extends Action {
  private IStructuredSelection fSelection;
  private Shell fShellToMinimize;
  private boolean fRestoreWindow;
  private boolean fPreferActiveFeedView;

  /**
   * @param selection
   */
  public OpenNewsAction(IStructuredSelection selection) {
    this(selection, null);
  }

  /**
   * @param selection
   * @param shellToMinimize The <code>Shell</code> to minimize (e.g. a Dialog)
   * when executing this action, or <code>NULL</code> if none.
   */
  public OpenNewsAction(IStructuredSelection selection, Shell shellToMinimize) {
    Assert.isTrue(selection != null && !selection.isEmpty());
    fSelection = selection;
    fShellToMinimize = shellToMinimize;

    setText(Messages.OpenNewsAction_OPEN);
  }

  /**
   * @param restoreWindow <code>true</code> if the window should be restored and
   * <code>false</code> otherwise.
   */
  public void setRestoreWindow(boolean restoreWindow) {
    fRestoreWindow = restoreWindow;
  }

  /**
   * Indicates that this action should prefer the currently active feedview when
   * opening a news. This can be useful in situations where the user is having a
   * search open where the news is contained in and would prevent to open the
   * actual bookmark instead.
   */
  public void setPreferActiveFeedView() {
    fPreferActiveFeedView = true;
  }

  /*
   * @see org.eclipse.jface.action.Action#run()
   */
  @Override
  public void run() {
    internalRun();
  }

  /*
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent(Event event) {
    internalRun();
  }

  private void internalRun() {

    /* Require a Page */
    IWorkbenchPage page = OwlUI.getPage();
    if (page == null)
      return;

    /* Restore Window */
    if (fRestoreWindow)
      OwlUI.restoreWindow(page);

    int openedEditors = 0;
    int maxOpenEditors = EditorUtils.getOpenEditorLimit();

    /* Convert selection to List of News (1 per Feed and Bin) */
    List<?> list = fSelection.toList();
    List<FeedLinkReference> handledFeeds = new ArrayList<FeedLinkReference>(list.size() / 2);
    List<Long> handledBins = new ArrayList<Long>(list.size() / 2);
    List<INews> newsToOpen = new ArrayList<INews>(list.size());
    for (Object selection : list) {
      if (selection instanceof INews) {
        INews news = (INews) selection;

        /* News in a Bin */
        if (news.getParentId() != 0) {
          if (!handledBins.contains(news.getParentId())) {
            newsToOpen.add(news);
            handledBins.add(news.getParentId());
          }
        }

        /* News in a Feed */
        else {
          FeedLinkReference feedRef = news.getFeedReference();
          if (!handledFeeds.contains(feedRef)) {
            newsToOpen.add(news);
            handledFeeds.add(feedRef);
          }
        }
      }
    }

    /* Minimize Shell if present */
    if (newsToOpen.size() > 0 && fShellToMinimize != null)
      fShellToMinimize.setMinimized(true);

    /* Handle case where active feed view is prefered for 1 news */
    if (fPreferActiveFeedView && newsToOpen.size() == 1) {
      INews news = newsToOpen.get(0);

      /* Active Feedview contains News */
      FeedView feedView = OwlUI.getActiveFeedView();
      if (feedView != null && feedView.contains(news)) {

        /* Activate and Select News */
        feedView.getSite().getPage().activate(feedView);
        feedView.setSelection(new StructuredSelection(news));

        /* Reveal in Bookmark Explorer */
        IFolderChild child = ((FeedViewInput) feedView.getEditorInput()).getMark();
        if (child instanceof FolderNewsMark)
          child = ((FolderNewsMark) child).getFolder();

        reveal(child);

        return;
      }
    }

    /* Open Bookmarks belonging to the News */
    INewsMark lastOpenedNewsMark = null;
    for (int i = 0; i < newsToOpen.size() && openedEditors < maxOpenEditors; i++) {
      INews news = newsToOpen.get(i);
      INewsMark newsmark;
      if (news.getParentId() != 0)
        newsmark = new NewsBinReference(news.getParentId()).resolve();
      else
        newsmark = CoreUtils.getBookMark(news.getFeedReference());

      /* Open and Select */
      if (newsmark != null) {
        openAndSelect(page, news, newsmark);
        openedEditors++;
        lastOpenedNewsMark = newsmark;
      }
    }

    /* Reveal Newsmark of last opened News */
    reveal(lastOpenedNewsMark);
  }

  private void reveal(IFolderChild child) {
    BookMarkExplorer explorer = OwlUI.getOpenedBookMarkExplorer();
    if (explorer != null && child != null && !explorer.isLinkingEnabled())
      explorer.reveal(child, false);
  }

  private void openAndSelect(IWorkbenchPage page, INews news, INewsMark newsmark) {
    PerformAfterInputSet perform = PerformAfterInputSet.selectNews(new NewsReference(news.getId()));
    perform.setActivate(false);

    /* Open this Bookmark */
    FeedViewInput fvInput = new FeedViewInput(newsmark, perform);
    FeedView feedview = null;

    /* First check if input already shown */
    IEditorPart existingEditor = page.findEditor(fvInput);
    if (existingEditor != null && existingEditor instanceof FeedView) {
      feedview = (FeedView) existingEditor;

      /* Set Selection and bring to front */
      existingEditor.getSite().getPage().activate(existingEditor);
      feedview.setSelection(new StructuredSelection(news));
    }

    /* Otherwise open the Input in a new Editor */
    else
      OwlUI.openInFeedView(page, new StructuredSelection(newsmark), true, false, perform);
  }
}