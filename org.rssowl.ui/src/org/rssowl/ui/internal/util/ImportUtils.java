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

package org.rssowl.ui.internal.util;

import org.eclipse.ui.PlatformUI;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.newsaction.CopyNewsAction;
import org.rssowl.core.internal.newsaction.LabelNewsAction;
import org.rssowl.core.internal.newsaction.MoveNewsAction;
import org.rssowl.core.interpreter.ITypeImporter;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.IFeedDAO;
import org.rssowl.core.persist.dao.ISearchMarkDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferenceScope.Kind;
import org.rssowl.core.persist.pref.IPreferenceType;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper to perform import of Feeds, Labels, Filters and Preferences including
 * the ability to restore location conditions.
 *
 * @author bpasero
 */
public class ImportUtils {

  /**
   * @param target the target {@link IFolder} for the import or
   * <code>null</code> if none and this is a direct import. May only be
   * <code>null</code> if a {@link IFolder} is already present in the
   * application or the list of elements to import contains a bookmark set
   * itself.
   * @param elements the list of elements to import. Supported types are
   * {@link IFolderChild}, {@link ILabel}, {@link ISearchFilter} and
   * {@link IPreference}.
   * @param checkExistingFeeds if <code>true</code>, the method will first check
   * if a feed with a given URL exists before creating it. set to
   * <code>false</code> to avoid this and improve import performance (e.g. when
   * doing an initial import into an empty RSSOwl instance).
   */
  public static void doImport(IFolder target, List<? extends IEntity> elements, boolean checkExistingFeeds) {
    List<IFolderChild> folderChilds = new ArrayList<IFolderChild>();
    List<ILabel> labels = new ArrayList<ILabel>();
    List<ISearchFilter> filters = new ArrayList<ISearchFilter>();
    List<IPreference> preferences = new ArrayList<IPreference>();

    if (elements != null) {
      for (IEntity entity : elements) {
        if (entity instanceof IFolderChild)
          folderChilds.add((IFolderChild) entity);
        else if (entity instanceof ILabel)
          labels.add((ILabel) entity);
        else if (entity instanceof ISearchFilter)
          filters.add((ISearchFilter) entity);
        else if (entity instanceof IPreference)
          preferences.add((IPreference) entity);
      }
    }

    doImport(target, folderChilds, labels, filters, preferences, checkExistingFeeds);
  }

  /**
   * @param target the target {@link IFolder} for the import or
   * <code>null</code> if none and this is a direct import. May only be
   * <code>null</code> if a {@link IFolder} is already present in the
   * application or the list of elements to import contains a bookmark set
   * itself.
   * @param elements the list of {@link IFolderChild} to import.
   * @param labels the list of {@link ILabel} to import.
   * @param filters the list of {@link ISearchFilter} to import.
   * @param preferences the list of {@link IPreference} to import.
   * @param checkExistingFeeds if <code>true</code>, the method will first check
   * if a feed with a given URL exists before creating it. set to
   * <code>false</code> to avoid this and improve import performance (e.g. when
   * doing an initial import into an empty RSSOwl instance).
   */
  public static void doImport(IFolder target, List<IFolderChild> elements, List<ILabel> labels, List<ISearchFilter> filters, List<IPreference> preferences, boolean checkExistingFeeds) {

    /* Map Old Id to IFolderChild */
    Map<Long, IFolderChild> mapOldIdToFolderChild = createOldIdToEntityMap(elements);

    /* Load SearchMarks containing location condition */
    List<ISearchMark> locationConditionSavedSearches = getLocationConditionSavedSearches(elements);

    /* Look for Feeds in Elements and Save them if required */
    IFeedDAO feedDao = DynamicDAO.getDAO(IFeedDAO.class);
    List<URI> feedsCreated = new ArrayList<URI>();
    for (IFolderChild element : elements) {
      saveFeedsOfBookmarks(feedsCreated, element, feedDao, checkExistingFeeds);
    }

    /* Direct Import */
    if (target == null)
      doDirectImport(elements, mapOldIdToFolderChild);

    /* Import to target */
    else
      doImportToTarget(target, elements, mapOldIdToFolderChild);

    /* Fix locations in Search Marks if required and save */
    if (!locationConditionSavedSearches.isEmpty()) {
      updateLocationConditions(mapOldIdToFolderChild, locationConditionSavedSearches);
      DynamicDAO.getDAO(ISearchMarkDAO.class).saveAll(locationConditionSavedSearches);
    }

    /* Import Labels  */
    boolean fixLabelOrder = false;
    Map<String, ILabel> mapExistingLabelToName = new HashMap<String, ILabel>();
    Map<Long, ILabel> mapOldIdToImportedLabel = new HashMap<Long, ILabel>();
    if (labels != null && !labels.isEmpty()) {
      Collection<ILabel> existingLabels = DynamicDAO.loadAll(ILabel.class);
      for (ILabel existingLabel : existingLabels) {
        mapExistingLabelToName.put(existingLabel.getName(), existingLabel);
      }

      for (ILabel importedLabel : labels) {
        Object oldIdValue = importedLabel.getProperty(ITypeImporter.ID_KEY);
        if (oldIdValue != null && oldIdValue instanceof Long)
          mapOldIdToImportedLabel.put((Long) oldIdValue, importedLabel);
      }

      for (ILabel importedLabel : labels) {
        ILabel existingLabel = mapExistingLabelToName.get(importedLabel.getName());

        /* Update Existing */
        if (existingLabel != null) {
          existingLabel.setColor(importedLabel.getColor());
          if (existingLabel.getOrder() != importedLabel.getOrder())
            fixLabelOrder = true;
          existingLabel.setOrder(importedLabel.getOrder());
          DynamicDAO.save(existingLabel);
        }

        /* Save as New */
        else {
          importedLabel.removeProperty(ITypeImporter.ID_KEY);
          DynamicDAO.save(importedLabel);
          fixLabelOrder = true;
        }
      }

      /* Fix Order to be a sequence again */
      if (fixLabelOrder && !existingLabels.isEmpty()) {
        Set<ILabel> sortedLabels = CoreUtils.loadSortedLabels();
        int index = 0;
        for (Iterator<?> iterator = sortedLabels.iterator(); iterator.hasNext();) {
          ILabel label = (ILabel) iterator.next();
          label.setOrder(index);
          index++;
        }

        DynamicDAO.saveAll(sortedLabels);
      }
    }

    /* Import Filters */
    if (filters != null && !filters.isEmpty()) {
      int existingFiltersCount = DynamicDAO.loadAll(ISearchFilter.class).size();

      /* Fix locations in Searches if required */
      List<ISearch> locationConditionSearches = getLocationConditionSearchesFromFilters(filters);
      if (!locationConditionSearches.isEmpty())
        updateLocationConditions(mapOldIdToFolderChild, locationConditionSearches);

      /* Fix locations in Actions if required */
      for (ISearchFilter filter : filters) {
        List<IFilterAction> actions = filter.getActions();
        for (IFilterAction action : actions) {
          if (MoveNewsAction.ID.equals(action.getActionId()) || CopyNewsAction.ID.equals(action.getActionId())) {
            Object data = action.getData();
            if (data != null && data instanceof Long[]) {
              Long[] oldBinLocations = (Long[]) data;
              List<Long> newBinLocations = new ArrayList<Long>(oldBinLocations.length);

              for (int i = 0; i < oldBinLocations.length; i++) {
                Long oldLocation = oldBinLocations[i];
                if (mapOldIdToFolderChild.containsKey(oldLocation)) {
                  IFolderChild location = mapOldIdToFolderChild.get(oldLocation);
                  newBinLocations.add(location.getId());
                }
              }

              action.setData(newBinLocations.toArray(new Long[newBinLocations.size()]));
            }
          }
        }
      }

      /* Fix labels in Actions if required */
      for (ISearchFilter filter : filters) {
        List<IFilterAction> actions = filter.getActions();
        for (IFilterAction action : actions) {
          if (LabelNewsAction.ID.equals(action.getActionId())) {
            Object data = action.getData();
            if (data != null && data instanceof Long) {
              ILabel label = mapOldIdToImportedLabel.get(data);
              if (label != null) {
                String name = label.getName();
                ILabel existingLabel = mapExistingLabelToName.get(name);
                if (existingLabel != null)
                  action.setData(existingLabel.getId());
                else
                  action.setData(label.getId());
              }
            }
          }
        }
      }

      /* Fix Order and Enablement */
      for (ISearchFilter filter : filters) {
        filter.setOrder(filter.getOrder() + existingFiltersCount);

        if (filter.isEnabled() && CoreUtils.isOrphaned(filter))
          filter.setEnabled(false);
      }

      /* Save */
      DynamicDAO.saveAll(filters);
    }

    /* Import Preferences */
    if (preferences != null && !preferences.isEmpty()) {
      IPreferenceScope globalPreferences = Owl.getPreferenceService().getGlobalScope();
      IPreferenceScope eclipsePreferences = Owl.getPreferenceService().getEclipseScope();
      boolean flushEclipsePreferences = false;

      for (IPreference preference : preferences) {
        Object data = preference.getProperty(ITypeImporter.DATA_KEY);
        if (data != null && data instanceof Object[] && ((Object[]) data).length == 2) {
          IPreferenceScope.Kind kind = (IPreferenceScope.Kind) ((Object[]) data)[0];
          IPreferenceType type = (IPreferenceType) ((Object[]) data)[1];
          IPreferenceScope scope = (kind == Kind.GLOBAL) ? globalPreferences : eclipsePreferences;
          if (kind == Kind.ECLIPSE)
            flushEclipsePreferences = true;

          switch (type) {
            case BOOLEAN:
              scope.putBoolean(preference.getKey(), preference.getBoolean());
              break;

            case INTEGER:
              scope.putInteger(preference.getKey(), preference.getInteger());
              break;

            case INTEGERS:
              scope.putIntegers(preference.getKey(), preference.getIntegers());
              break;

            case LONG:
              scope.putLong(preference.getKey(), preference.getLong());
              break;

            case LONGS:
              scope.putLongs(preference.getKey(), preference.getLongs());
              break;

            case STRING:
              scope.putString(preference.getKey(), preference.getString());
              break;

            case STRINGS:
              scope.putStrings(preference.getKey(), preference.getStrings());
              break;
          }
        }
      }

      /* Flush Eclipse preferences if required */
      if (flushEclipsePreferences)
        eclipsePreferences.flush();
    }
  }

  private static void saveFeedsOfBookmarks(List<URI> feedsCreated, IFolderChild element, IFeedDAO feedDao, boolean checkExistingFeeds) {

    /* Bookmark */
    if (element instanceof IBookMark) {
      IBookMark bm = (IBookMark) element;
      FeedLinkReference feedReference = bm.getFeedLinkReference();

      /* Check for Existing Feed if set */
      boolean feedExists = feedsCreated.contains(feedReference.getLink());
      if (!feedExists && checkExistingFeeds)
        feedExists = feedDao.exists(feedReference.getLink());

      /* Create a new Feed if necessary */
      if (!feedExists) {
        IFeed feed = Owl.getModelFactory().createFeed(null, feedReference.getLink());

        Object homepage = bm.getProperty(ITypeImporter.HOMEPAGE_KEY);
        if (homepage != null && homepage instanceof String)
          feed.setHomepage(URIUtils.createURI((String) homepage));

        Object description = bm.getProperty(ITypeImporter.DESCRIPTION_KEY);
        if (description != null && description instanceof String)
          feed.setDescription((String) description);

        feedDao.save(feed);
        feedsCreated.add(feedReference.getLink());
      }

      bm.removeProperty(ITypeImporter.DESCRIPTION_KEY);
      bm.removeProperty(ITypeImporter.HOMEPAGE_KEY);
    }

    /* Folder */
    else if (element instanceof IFolder) {
      IFolder folder = (IFolder) element;
      List<IFolderChild> children = folder.getChildren();
      for (IFolderChild child : children) {
        saveFeedsOfBookmarks(feedsCreated, child, feedDao, checkExistingFeeds);
      }
    }
  }

  private static void doDirectImport(List<IFolderChild> elements, Map<Long, IFolderChild> mapOldIdToFolderChild) {
    List<IFolder> foldersToSave = new ArrayList<IFolder>();
    Set<IFolder> rootFolders = CoreUtils.loadRootFolders();

    /* Load the current selected Set as Location if necessary */
    IFolder selectedSet = null;
    if (!rootFolders.isEmpty()) {
      if (!InternalOwl.TESTING && PlatformUI.isWorkbenchRunning() && Controller.getDefault().isStarted())
        selectedSet = OwlUI.getSelectedBookMarkSet();
      else
        selectedSet = rootFolders.iterator().next();
    }

    /* Import Elements */
    for (IFolderChild element : elements) {

      /* Folder */
      if (element instanceof IFolder) {
        IFolder folder = (IFolder) element;

        /* Bookmark Set */
        if (folder.getParent() == null) {

          /* Default Bookmark Set */
          if (folder.getProperty(ITypeImporter.TEMPORARY_FOLDER) != null && selectedSet != null) {

            /* Reparent Childs into selected set */
            reparent(folder, selectedSet);
            foldersToSave.add(selectedSet);

            /* Also Update Mapping if necessary */
            if (folder.getProperty(ITypeImporter.ID_KEY) != null)
              mapOldIdToFolderChild.put((Long) folder.getProperty(ITypeImporter.ID_KEY), selectedSet);
          }

          /* Any other Bookmark Set */
          else {

            /* Check if set already exists */
            IFolder existingSetFolder = null;
            for (IFolder rootFolder : rootFolders) {
              if (rootFolder.getName().equals(folder.getName())) {
                existingSetFolder = rootFolder;
                break;
              }
            }

            /* Reparent into Existing Set */
            if (existingSetFolder != null) {
              reparent(folder, existingSetFolder);
              foldersToSave.add(existingSetFolder);

              /* Also Update Mapping if necessary */
              if (folder.getProperty(ITypeImporter.ID_KEY) != null)
                mapOldIdToFolderChild.put((Long) folder.getProperty(ITypeImporter.ID_KEY), existingSetFolder);
            }

            /* Otherwise save as new Set */
            else {
              foldersToSave.add(folder);
            }
          }
        }

        /* Normal Folder */
        else if (selectedSet != null) {
          folder.setParent(selectedSet);
          selectedSet.addFolder(folder, null, null);
          foldersToSave.add(selectedSet);
        }
      }

      /* Any Newsmark */
      else if (element instanceof INewsMark && selectedSet != null) {
        INewsMark mark = (INewsMark) element;
        mark.setParent(selectedSet);
        selectedSet.addMark(mark, null, null);
        foldersToSave.add(selectedSet);
      }
    }

    /* Remove Duplicates (using object identity) */
    foldersToSave = CoreUtils.removeIdentityDuplicates(foldersToSave);

    /* Un-set ID Property prior Save */
    for (IFolder folderToSave : foldersToSave) {
      unsetIdProperty(folderToSave);
    }

    /* Save Folders that have changed */
    DynamicDAO.saveAll(foldersToSave);
  }

  private static void doImportToTarget(IFolder target, List<IFolderChild> elements, Map<Long, IFolderChild> mapOldIdToFolderChild) {
    List<IFolder> foldersToSave = new ArrayList<IFolder>();

    /* Import Elements */
    for (IFolderChild element : elements) {

      /* Folder */
      if (element instanceof IFolder) {
        IFolder folder = (IFolder) element;

        /* Bookmark Set */
        if (folder.getParent() == null) {

          /* Default Bookmark Set */
          if (folder.getProperty(ITypeImporter.TEMPORARY_FOLDER) != null) {

            /* Reparent Childs into selected target */
            reparent(folder, target);
            foldersToSave.add(target);

            /* Also Update Mapping if necessary */
            if (folder.getProperty(ITypeImporter.ID_KEY) != null)
              mapOldIdToFolderChild.put((Long) folder.getProperty(ITypeImporter.ID_KEY), target);
          }

          /* Any other Bookmark Set */
          else {
            folder.setParent(target);
            target.addFolder(folder, null, null);
            foldersToSave.add(target);
          }
        }

        /* Normal Folder */
        else {
          folder.setParent(target);
          target.addFolder(folder, null, null);
          foldersToSave.add(target);
        }
      }

      /* Any Newsmark */
      else if (element instanceof INewsMark) {
        INewsMark mark = (INewsMark) element;
        mark.setParent(target);
        target.addMark(mark, null, null);
        foldersToSave.add(target);
      }
    }

    /* Remove Duplicates (using object identity) */
    foldersToSave = CoreUtils.removeIdentityDuplicates(foldersToSave);

    /* Un-set ID Property prior Save */
    for (IFolder folderToSave : foldersToSave) {
      unsetIdProperty(folderToSave);
    }

    /* Save Folders that have changed */
    DynamicDAO.saveAll(foldersToSave);
  }

  private static void reparent(IFolder from, IFolder to) {
    List<IFolderChild> children = from.getChildren();
    for (IFolderChild child : children) {

      /* Reparent Folder */
      if (child instanceof IFolder) {
        IFolder folder = (IFolder) child;
        folder.setParent(to);
        to.addFolder(folder, null, null);
      }

      /* Reparent Mark */
      else if (child instanceof IMark) {
        IMark mark = (IMark) child;
        mark.setParent(to);
        to.addMark(mark, null, null);
      }
    }
  }

  private static void updateLocationConditions(Map<Long, IFolderChild> oldIdToFolderChildMap, List<? extends ISearch> searches) {
    for (ISearch search : searches) {
      List<ISearchCondition> conditions = search.getSearchConditions();
      for (ISearchCondition condition : conditions) {

        /* Location Condition */
        if (condition.getField().getId() == INews.LOCATION && condition.getValue() != null) {
          Long[][] value = (Long[][]) condition.getValue();
          List<IFolderChild> newLocations = new ArrayList<IFolderChild>();

          /* Folders */
          for (int i = 0; value[CoreUtils.FOLDER] != null && i < value[CoreUtils.FOLDER].length; i++) {
            if (value[CoreUtils.FOLDER][i] != null && value[CoreUtils.FOLDER][i] != 0) {
              Long id = value[CoreUtils.FOLDER][i];
              if (oldIdToFolderChildMap.containsKey(id))
                newLocations.add(oldIdToFolderChildMap.get(id));
            }
          }

          /* BookMarks */
          for (int i = 0; value[CoreUtils.BOOKMARK] != null && i < value[CoreUtils.BOOKMARK].length; i++) {
            if (value[CoreUtils.BOOKMARK][i] != null && value[CoreUtils.BOOKMARK][i] != 0) {
              Long id = value[CoreUtils.BOOKMARK][i];
              if (oldIdToFolderChildMap.containsKey(id))
                newLocations.add(oldIdToFolderChildMap.get(id));
            }
          }

          /* NewsBins */
          if (value.length == 3) {
            for (int i = 0; value[CoreUtils.NEWSBIN] != null && i < value[CoreUtils.NEWSBIN].length; i++) {
              if (value[CoreUtils.NEWSBIN][i] != null && value[CoreUtils.NEWSBIN][i] != 0) {
                Long id = value[CoreUtils.NEWSBIN][i];
                if (oldIdToFolderChildMap.containsKey(id))
                  newLocations.add(oldIdToFolderChildMap.get(id));
              }
            }
          }

          /* Update */
          condition.setValue(ModelUtils.toPrimitive(newLocations));
        }
      }
    }
  }

  private static void unsetIdProperty(IEntity entity) {
    entity.removeProperty(ITypeImporter.ID_KEY);

    if (entity instanceof IFolder) {
      IFolder folder = (IFolder) entity;
      List<IFolderChild> children = folder.getChildren();
      for (IFolderChild child : children) {
        unsetIdProperty(child);
      }
    }
  }

  private static List<ISearchMark> getLocationConditionSavedSearches(List<? extends IEntity> types) {
    List<ISearchMark> locationConditionSavedSearches = new ArrayList<ISearchMark>();

    for (IEntity entity : types)
      fillLocationConditionSavedSearches(locationConditionSavedSearches, entity);

    return locationConditionSavedSearches;
  }

  private static List<ISearch> getLocationConditionSearchesFromFilters(List<ISearchFilter> filters) {
    List<ISearch> locationConditionSearches = new ArrayList<ISearch>();

    for (ISearchFilter filter : filters) {
      ISearch search = filter.getSearch();
      if (search != null && containsLocationCondition(search))
        locationConditionSearches.add(search);
    }

    return locationConditionSearches;
  }

  private static void fillLocationConditionSavedSearches(List<ISearchMark> searchmarks, IEntity entity) {
    if (entity instanceof ISearchMark && containsLocationCondition((ISearchMark) entity)) {
      searchmarks.add((ISearchMark) entity);
    } else if (entity instanceof IFolder) {
      IFolder folder = (IFolder) entity;
      List<IFolderChild> children = folder.getChildren();
      for (IFolderChild child : children) {
        fillLocationConditionSavedSearches(searchmarks, child);
      }
    }
  }

  private static boolean containsLocationCondition(ISearch search) {
    List<ISearchCondition> searchConditions = search.getSearchConditions();
    for (ISearchCondition condition : searchConditions) {
      if (condition.getField().getId() == INews.LOCATION)
        return true;
    }

    return false;
  }

  private static Map<Long, IFolderChild> createOldIdToEntityMap(List<? extends IEntity> types) {
    Map<Long, IFolderChild> oldIdToEntityMap = new HashMap<Long, IFolderChild>();

    for (IEntity entity : types) {
      if (entity instanceof IFolderChild)
        fillOldIdToEntityMap(oldIdToEntityMap, (IFolderChild) entity);
    }

    return oldIdToEntityMap;
  }

  private static void fillOldIdToEntityMap(Map<Long, IFolderChild> oldIdToEntityMap, IFolderChild folderChild) {
    Long oldId = (Long) folderChild.getProperty(ITypeImporter.ID_KEY);
    if (oldId != null)
      oldIdToEntityMap.put(oldId, folderChild);

    if (folderChild instanceof IFolder) {
      IFolder folder = (IFolder) folderChild;
      List<IFolderChild> children = folder.getChildren();
      for (IFolderChild child : children) {
        fillOldIdToEntityMap(oldIdToEntityMap, child);
      }
    }
  }
}