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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.newsaction.CopyNewsAction;
import org.rssowl.core.internal.newsaction.MoveNewsAction;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.interpreter.ITypeExporter.Options;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFolderDAO;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.views.explorer.BookMarkExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link Wizard} to export bookmarks, saved searches and bins with the option
 * to also export settings to OPML.
 * <p>
 * TODO While this wizard tries to add missing locations from saved searches and
 * filters, this is not recursive. Thus it can still happen that a missing
 * location adds more saved searches with missing locations in return.
 *</p>
 *
 * @author bpasero
 */
public class ExportWizard extends Wizard implements IExportWizard {
  private ExportElementsPage fExportElementsPage;
  private ExportOptionsPage fExportOptionsPage;

  /** Leave for Reflection */
  public ExportWizard() {}

  /*
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages() {
    setWindowTitle(Messages.ExportWizard_EXPORT);

    /* Page 1: Folder Child Selection */
    fExportElementsPage = new ExportElementsPage(Messages.ExportWizard_CHOOSE_ELEMENTS);
    addPage(fExportElementsPage);

    /* Page 2: Export Settings Configuration */
    fExportOptionsPage = new ExportOptionsPage(Messages.ExportWizard_EXPORT_OPTIONS);
    addPage(fExportOptionsPage);
  }

  /*
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {}

  /*
   * @see org.eclipse.jface.wizard.Wizard#canFinish()
   */
  @Override
  public boolean canFinish() {
    return true;
  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish() {

    /* Prompt for Filename */
    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
    dialog.setText(Messages.ExportWizard_EXPORT_FILE);

    /* Receive Formats for Export */
    List<String> filterExtensions = new ArrayList<String>();
    filterExtensions.add("*.opml"); //$NON-NLS-1$
    filterExtensions.add("*.xml"); //$NON-NLS-1$

    Collection<String> exportFormats = Owl.getInterpreter().getExportFormats();
    for (String exportFormat : exportFormats) {
      String format = "*." + exportFormat.toLowerCase(); //$NON-NLS-1$
      if (!filterExtensions.contains(format))
        filterExtensions.add(format);
    }

    if (!filterExtensions.contains("*.*")) //$NON-NLS-1$
      filterExtensions.add("*.*"); //$NON-NLS-1$

    dialog.setFilterExtensions(filterExtensions.toArray(new String[filterExtensions.size()]));
    dialog.setFileName("rssowl.opml"); //$NON-NLS-1$
    dialog.setOverwrite(true);
    String string = dialog.open();

    /* Export to given File */
    if (string != null) {

      /* Enforce that Explorer Settings are up to date if necessary */
      if (fExportOptionsPage.getExportOptions() != null && fExportOptionsPage.getExportOptions().contains(Options.EXPORT_PREFERENCES)) {
        BookMarkExplorer explorer = OwlUI.getOpenedBookMarkExplorer();
        if (explorer != null)
          explorer.saveState();
      }

      /* Export */
      File file = new File(string);
      try {
        Owl.getInterpreter().exportTo(file, getElementsToExport(), fExportOptionsPage.getExportOptions());

        return true;
      } catch (InterpreterException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      }
    }

    return false;
  }

  /* Ensure that Childs to Export is normalized and contains all dependent Locations from Searches and Filters */
  private List<IFolderChild> getElementsToExport() {
    List<IFolderChild> selectedElements = fExportElementsPage.getElementsToExport();
    Set<Options> options = fExportOptionsPage.getExportOptions();

    /* Check if dependent locations need to be added manually */
    Collection<IFolder> rootFolders = DynamicDAO.getDAO(IFolderDAO.class).loadRoots();
    if (!selectedElements.containsAll(rootFolders)) {

      /* Find Folders and Saved Searches */
      Set<ISearchMark> savedSearches = new HashSet<ISearchMark>();
      Set<IFolder> folders = new HashSet<IFolder>();
      for (IFolderChild child : selectedElements) {
        if (child instanceof IFolder)
          folders.add((IFolder) child);
        else if (child instanceof ISearchMark)
          savedSearches.add((ISearchMark) child);
      }

      /* Find Saved Searches from Folders */
      for (IFolder folder : folders) {
        findSavedSearches(savedSearches, folder);
      }

      /* Add those Locations required by Saved Searches */
      for (ISearchMark savedSearch : savedSearches) {
        collectLocations(selectedElements, savedSearch);
      }

      /* Add those Locations required by Filters */
      if (options != null && options.contains(Options.EXPORT_FILTERS)) {
        Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
        for (ISearchFilter filter : filters) {
          collectLocations(selectedElements, filter);
        }
      }
    }

    /* Remove those childs where its parents are present in the list already */
    CoreUtils.normalize(selectedElements);

    return selectedElements;
  }

  /* Collect Locations from Searches */
  private void collectLocations(List<IFolderChild> selectedElements, ISearch search) {
    if (search != null) {
      List<ISearchCondition> conditions = search.getSearchConditions();
      for (ISearchCondition condition : conditions) {
        if (condition.getField().getId() == INews.LOCATION) {
          Object value = condition.getValue();
          if (value instanceof Long[][]) {
            List<IFolderChild> locations = CoreUtils.toEntities((Long[][]) value);
            for (IFolderChild location : locations) {
              if (!selectedElements.contains(location))
                selectedElements.add(location);
            }
          }
        }
      }
    }
  }

  /* Collect Locations from Filters */
  private void collectLocations(List<IFolderChild> selectedElements, ISearchFilter filter) {

    /* Locations from Search */
    collectLocations(selectedElements, filter.getSearch());

    /* Locations from Actions */
    List<IFilterAction> actions = filter.getActions();
    for (IFilterAction action : actions) {
      if (MoveNewsAction.ID.equals(action.getActionId()) || CopyNewsAction.ID.equals(action.getActionId())) {
        Object value = action.getData();
        if (value instanceof Long[]) {
          Long[] binIds = (Long[]) value;
          for (Long binId : binIds) {
            INewsBin bin = new NewsBinReference(binId).resolve();
            if (bin != null && !selectedElements.contains(bin))
              selectedElements.add(bin);
          }
        }
      }
    }
  }

  private void findSavedSearches(Set<ISearchMark> savedSearches, IFolder folder) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IFolder)
        findSavedSearches(savedSearches, (IFolder) child);
      else if (child instanceof ISearchMark)
        savedSearches.add((ISearchMark) child);
    }

  }

  /*
   * @see org.eclipse.jface.wizard.Wizard#needsProgressMonitor()
   */
  @Override
  public boolean needsProgressMonitor() {
    return false;
  }
}