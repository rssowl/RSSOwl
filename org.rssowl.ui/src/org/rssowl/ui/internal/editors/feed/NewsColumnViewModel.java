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

package org.rssowl.ui.internal.editors.feed;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.CColumnLayoutData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The view model behind the columns of the feed view used in
 * {@link NewsTableControl}.
 *
 * @author bpasero
 */
public class NewsColumnViewModel {

  /** ID to associate a Column with its ID */
  public static final String COL_ID = "org.rssowl.ui.internal.editors.feed.ColumnIdentifier"; //$NON-NLS-1$

  private final List<NewsColumn> fColumns = new ArrayList<NewsColumn>();
  private NewsColumn fSortColumn;
  private boolean fAscending;

  private NewsColumnViewModel() {}

  /**
   * Copy constructor for the model.
   *
   * @param copyFrom the model to copy from.
   */
  public NewsColumnViewModel(NewsColumnViewModel copyFrom) {
    fColumns.addAll(copyFrom.getColumns());
    fSortColumn = copyFrom.getSortColumn();
    fAscending = copyFrom.isAscending();
  }

  /**
   * @return a default {@link NewsColumnViewModel} initialized from the global
   * scope of preferences.
   */
  public static NewsColumnViewModel createGlobal() {
    return loadFrom(Owl.getPreferenceService().getGlobalScope());
  }

  /**
   * @param isSearch set to <code>true</code> if column model is for search
   * results, <code>false</code> otherwise.
   * @return a default {@link NewsColumnViewModel} initialized from the default
   * scope of preferences.
   */
  public static NewsColumnViewModel createDefault(boolean isSearch) {
    return loadFrom(Owl.getPreferenceService().getDefaultScope(), isSearch);
  }

  /**
   * @param preferences the preferences to load the news column model from.
   * @return the {@link NewsColumnViewModel} from the provided preferences.
   */
  public static NewsColumnViewModel loadFrom(IPreferenceScope preferences) {
    return loadFrom(preferences, false);
  }

  /**
   * @param preferences the preferences to load the news column model from.
   * @param isSearch set to <code>true</code> if column model is for search
   * results, <code>false</code> otherwise.
   * @return the {@link NewsColumnViewModel} from the provided preferences.
   */
  public static NewsColumnViewModel loadFrom(IPreferenceScope preferences, boolean isSearch) {
    int[] columns = preferences.getIntegers(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_COLUMNS : DefaultPreferences.BM_NEWS_COLUMNS);
    int sortColumn = preferences.getInteger(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_SORT_COLUMN : DefaultPreferences.BM_NEWS_SORT_COLUMN);
    boolean ascending = preferences.getBoolean(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_SORT_ASCENDING : DefaultPreferences.BM_NEWS_SORT_ASCENDING);

    return createFrom(columns, sortColumn, ascending);
  }

  /**
   * @param columns the selected columns.
   * @param sortColumn the sorted column.
   * @param ascending the sort order.
   * @return the {@link NewsColumnViewModel} from the provided settings.
   */
  public static NewsColumnViewModel createFrom(int[] columns, int sortColumn, boolean ascending) {
    NewsColumn[] newsColumns = NewsColumn.values();
    NewsColumnViewModel model = new NewsColumnViewModel();

    /* News Columns */
    for (int column : columns) {
      model.addColumn(newsColumns[column]);
    }

    /* Sort Column */
    model.setSortColumn(newsColumns[sortColumn]);

    /* Sort Order */
    model.setAscending(ascending);

    return model;
  }

  /**
   * @param tree the tree to initialize the model from.
   * @return the {@link NewsColumnViewModel} from the provided tree.
   */
  public static NewsColumnViewModel initializeFrom(Tree tree) {
    NewsColumnViewModel model = new NewsColumnViewModel();

    TreeColumn[] columns = tree.getColumns();
    int[] columnOrder = tree.getColumnOrder();
    for (int order : columnOrder) {
      Object data = columns[order].getData(COL_ID);
      if (data != null)
        model.addColumn((NewsColumn) data);
    }

    return model;
  }

  /**
   * @param table the table to initialize the model from.
   * @return the {@link NewsColumnViewModel} from the provided table.
   */
  public static NewsColumnViewModel initializeFrom(Table table) {
    NewsColumnViewModel model = new NewsColumnViewModel();

    TableColumn[] columns = table.getColumns();
    int[] columnOrder = table.getColumnOrder();
    for (int order : columnOrder) {
      Object data = columns[order].getData(COL_ID);
      if (data != null)
        model.addColumn((NewsColumn) data);
    }

    return model;
  }

  /**
   * @return the visible columns.
   */
  public List<NewsColumn> getColumns() {
    return fColumns;
  }

  /**
   * @param column the column to add to the model.
   */
  public void addColumn(NewsColumn column) {
    if (!fColumns.contains(column))
      fColumns.add(column);
  }

  /**
   * @param column the column to remove from the model.
   */
  public void removeColumn(NewsColumn column) {
    fColumns.remove(column);
  }

  /**
   * @param index the index of the column to return.
   * @return the {@link NewsColumn} at the given index.
   */
  public NewsColumn getColumn(int index) {
    return fColumns.get(index);
  }

  /**
   * @param column the column to check if included in this model.
   * @return <code>true</code> if the column is part of this model and
   * <code>false</code> otherwise.
   */
  public boolean contains(NewsColumn column) {
    return fColumns.contains(column);
  }

  /**
   * @return the sorted column.
   */
  public NewsColumn getSortColumn() {
    return fSortColumn;
  }

  /**
   * @param column the sorted column.
   */
  public void setSortColumn(NewsColumn column) {
    fSortColumn = column;
  }

  /**
   * @return <code>true</code> if sorting is ascending or <code>false</code>
   * otherwise.
   */
  public boolean isAscending() {
    return fAscending;
  }

  /**
   * @param ascending <code>true</code> if sorting is ascending or
   * <code>false</code> otherwise.
   */
  public void setAscending(boolean ascending) {
    fAscending = ascending;
  }

  /**
   * @param column the column to get the layout information for.
   * @return an instance of {@link CColumnLayoutData} describing the layout of
   * the column.
   */
  public CColumnLayoutData getLayoutData(NewsColumn column) {
    boolean useLargeColumns = Application.IS_LINUX || Application.IS_MAC;

    switch (column) {
      case TITLE:
        return new CColumnLayoutData(CColumnLayoutData.Size.FILL, 60);

      case AUTHOR:
        return new CColumnLayoutData(CColumnLayoutData.Size.FILL, 15);

      case CATEGORY:
        return new CColumnLayoutData(CColumnLayoutData.Size.FILL, 15);

      case LABELS:
        return new CColumnLayoutData(CColumnLayoutData.Size.FILL, 10);

      case DATE:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, OwlUI.getDateWidth());

      case PUBLISHED:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, OwlUI.getDateWidth());

      case MODIFIED:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, OwlUI.getDateWidth());

      case RECEIVED:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, OwlUI.getDateWidth());

      case ATTACHMENTS:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, useLargeColumns ? 20 : 18);

      case FEED:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, useLargeColumns ? 20 : 18);

      case RELEVANCE:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, 24);

      case STICKY:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, useLargeColumns ? 20 : 18);

      case STATUS:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, OwlUI.getStateWidth());

      case LOCATION:
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, 150);

      case LINK:
        return new CColumnLayoutData(CColumnLayoutData.Size.FILL, 25);

      default: //Never Reached
        return new CColumnLayoutData(CColumnLayoutData.Size.FIXED, 100);
    }
  }

  /**
   * @param preferences the preferences to save the news column model into.
   * @return <code>true</code> in case the settings have changed and
   * <code>false</code> otherwise.
   */
  public boolean saveTo(IPreferenceScope preferences) {
    return saveTo(preferences, false);
  }

  /**
   * @param preferences the preferences to save the news column model into.
   * @param isSearch set to <code>true</code> if column model is for search
   * results, <code>false</code> otherwise.
   * @return <code>true</code> in case the settings have changed and
   * <code>false</code> otherwise.
   */
  public boolean saveTo(IPreferenceScope preferences, boolean isSearch) {
    return saveTo(preferences, isSearch, true, true, true);
  }

  /**
   * @param preferences the preferences to save the news column model into.
   * @param isSearch set to <code>true</code> if column model is for search
   * results, <code>false</code> otherwise.
   * @param saveColumns if <code>true</code>, saves the columns of the model
   * @param saveSortColumn if <code>true</code>, saves the sort column of the
   * model
   * @param saveSortDirection if <code>true</code>, saves the sort direction of
   * the model
   * @return <code>true</code> in case the settings have changed and
   * <code>false</code> otherwise.
   */
  public boolean saveTo(IPreferenceScope preferences, boolean isSearch, boolean saveColumns, boolean saveSortColumn, boolean saveSortDirection) {
    boolean changed = true;

    /* News Columns */
    int[] columns = new int[fColumns.size()];
    for (int i = 0; i < fColumns.size(); i++)
      columns[i] = fColumns.get(i).ordinal();

    /* Check for Changes */
    int[] prefColumns = preferences.getIntegers(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_COLUMNS : DefaultPreferences.BM_NEWS_COLUMNS);
    int prefSortColumn = preferences.getInteger(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_SORT_COLUMN : DefaultPreferences.BM_NEWS_SORT_COLUMN);
    boolean prefAscending = preferences.getBoolean(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_SORT_ASCENDING : DefaultPreferences.BM_NEWS_SORT_ASCENDING);

    changed = !Arrays.equals(prefColumns, columns) || prefSortColumn != fSortColumn.ordinal() || prefAscending != fAscending;

    /* Save Columns */
    if (saveColumns)
      preferences.putIntegers(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_COLUMNS : DefaultPreferences.BM_NEWS_COLUMNS, columns);

    /* Save Sorting Column */
    if (saveSortColumn)
      preferences.putInteger(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_SORT_COLUMN : DefaultPreferences.BM_NEWS_SORT_COLUMN, fSortColumn.ordinal());

    /* Save Sorting Direction */
    if (saveSortDirection)
      preferences.putBoolean(isSearch ? DefaultPreferences.SEARCH_DIALOG_NEWS_SORT_ASCENDING : DefaultPreferences.BM_NEWS_SORT_ASCENDING, fAscending);

    return changed;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (fAscending ? 1231 : 1237);
    result = prime * result + ((fColumns == null) ? 0 : fColumns.hashCode());
    result = prime * result + ((fSortColumn == null) ? 0 : fSortColumn.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    NewsColumnViewModel other = (NewsColumnViewModel) obj;
    if (fAscending != other.fAscending)
      return false;

    if (fSortColumn == null) {
      if (other.fSortColumn != null)
        return false;
    } else if (!fSortColumn.equals(other.fSortColumn))
      return false;

    if (fColumns == null) {
      if (other.fColumns != null)
        return false;
    } else if (!fColumns.equals(other.fColumns))
      return false;

    return true;
  }
}