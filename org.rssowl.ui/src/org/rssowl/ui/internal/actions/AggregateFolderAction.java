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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.dialogs.AggregateNewsDialog;
import org.rssowl.ui.internal.dialogs.SearchMarkDialog;
import org.rssowl.ui.internal.util.ModelUtils;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates all bookmarks of a selected folder to be displayed in the
 * feedview. Asks the user to either use a saved search or
 * {@link FolderNewsMark} to display.
 *
 * @author bpasero
 */
public class AggregateFolderAction implements IObjectActionDelegate {
  private ISelection fSelection;
  private IWorkbenchPart fTargetPart;

  /** Leave for Reflection */
  public AggregateFolderAction() {}

  /*
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    fTargetPart = targetPart;
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    if (!fSelection.isEmpty() && fSelection instanceof IStructuredSelection) {
      Object firstElem = ((IStructuredSelection) fSelection).getFirstElement();

      /* Aggregate News of Folder */
      if (firstElem instanceof IFolder) {
        IPreferenceScope preferences = Owl.getPreferenceService().getGlobalScope();
        boolean aggregateAsSearch = preferences.getBoolean(DefaultPreferences.AGGREGATE_NEWS_AS_SEARCH);
        boolean askUserForAggregationOption = !preferences.getBoolean(DefaultPreferences.REMEMBER_AGGREGATE_NEWS_OPTION);

        /* Ask user for Aggregation Mode if required */
        if (askUserForAggregationOption) {
          AggregateNewsDialog dialog = new AggregateNewsDialog(fTargetPart.getSite().getShell(), ((IFolder) firstElem).getName());
          int res = dialog.open();

          /* Check for Cancellation */
          if (res == IDialogConstants.CANCEL_ID)
            return;

          aggregateAsSearch = (res == IDialogConstants.YES_ID);
        }

        /* Create Search on Folder */
        if (aggregateAsSearch)
          createAndOpenSearch((IFolder) firstElem);

        /* Otherwise directly aggregate */
        else
          aggregateFolder((IFolder) firstElem);
      }
    }
  }

  private void createAndOpenSearch(IFolder folder) {
    ISearchMark locationSearch = findSearch(folder);
    if (locationSearch == null) {
      IModelFactory factory = Owl.getModelFactory();
      List<ISearchCondition> conditions = new ArrayList<ISearchCondition>();

      ISearchField locationField = factory.createSearchField(INews.LOCATION, INews.class.getName());
      conditions.add(factory.createSearchCondition(locationField, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singletonList((IFolderChild) folder))));

      SearchMarkDialog dialog = new SearchMarkDialog(fTargetPart.getSite().getShell(), folder.getParent(), folder, conditions, true, folder.getProperties());
      if (dialog.open() == IDialogConstants.OK_ID)
        locationSearch = dialog.getSearchMark();
      else
        return;
    }

    /* Open Search and Reload Bookmarks that have never been reloaded before */
    if (locationSearch != null) {
      StructuredSelection selection = new StructuredSelection(locationSearch);

      /* Ensure Selected */
      BookMarkExplorer explorer = OwlUI.getOpenedBookMarkExplorer();
      if (explorer != null)
        explorer.getViewSite().getSelectionProvider().setSelection(selection);

      /* Open */
      OwlUI.openInFeedView(fTargetPart.getSite().getPage(), selection);

      /* Reload if necessary */
      List<IBookMark> bookMarksToReload = new ArrayList<IBookMark>();
      fillBookMarksToReload(bookMarksToReload, folder);
      if (!bookMarksToReload.isEmpty())
        new ReloadTypesAction(new StructuredSelection(bookMarksToReload.toArray()), fTargetPart.getSite().getShell()).run();
    }
  }

  private void fillBookMarksToReload(List<IBookMark> bookMarksToReload, IFolder folder) {
    List<IMark> marks = folder.getMarks();
    for (IMark mark : marks) {
      if (mark instanceof IBookMark) {
        if ((((IBookMark) mark).getMostRecentNewsDate() == null))
          bookMarksToReload.add((IBookMark) mark);
      }
    }

    List<IFolder> childs = folder.getFolders();
    for (IFolder child : childs) {
      fillBookMarksToReload(bookMarksToReload, child);
    }
  }

  private ISearchMark findSearch(IFolderChild folder) {
    Collection<ISearchMark> existingSearches = DynamicDAO.loadAll(ISearchMark.class);
    for (ISearchMark search : existingSearches) {
      List<ISearchCondition> conditions = search.getSearchConditions();
      if (conditions.size() == 1) {
        ISearchCondition condition = conditions.get(0);

        /* Check on Search Field */
        ISearchField field = condition.getField();
        if (field.getId() != INews.LOCATION)
          continue;

        /* Check on Search Specifier */
        SearchSpecifier specifier = condition.getSpecifier();
        if (specifier != SearchSpecifier.IS)
          continue;

        /* Check on Search Value */
        Object value = condition.getValue();
        if (!(value instanceof Long[][]))
          continue;

        Long[][] valueLong = (Long[][]) value;
        if (valueLong.length == 0)
          continue;

        if (valueLong[CoreUtils.FOLDER].length != 1)
          continue;

        if (valueLong[CoreUtils.FOLDER][0] != null && valueLong[CoreUtils.FOLDER][0].equals(folder.getId()))
          return search;
      }
    }

    return null;
  }

  /* Create in-memory Newsmark */
  private void aggregateFolder(final IFolder folder) {
    BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(), new Runnable() {
      public void run() {
        FolderNewsMark folderNewsMark = new FolderNewsMark(folder);
        StructuredSelection newSelection = new StructuredSelection(folderNewsMark);

        /* Open in Feedview */
        OwlUI.openInFeedView(fTargetPart.getSite().getPage(), newSelection);
      }
    });
  }

  /*
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    fSelection = selection;
  }
}