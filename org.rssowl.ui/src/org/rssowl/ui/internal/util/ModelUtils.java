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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.SearchHit;
import org.rssowl.core.util.URIUtils;
import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.EntityGroupItem;
import org.rssowl.ui.internal.FolderNewsMark;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.editors.feed.NewsFilter;
import org.rssowl.ui.internal.editors.feed.NewsGrouping;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class for various Model-Transformations required by the UI.
 *
 * @author bpasero
 */
public class ModelUtils {

  /* This utility class constructor is hidden */
  private ModelUtils() {
    // Protect default constructor
  }

  /**
   * @param entities A list of folder childs.
   * @return Returns a multi-dimensional array where an array of {@link Long} is
   * stored in the first index representing IDs of all {@link IFolder} in the
   * list. The second index is an array of {@link Long} that represents the IDs
   * of all {@link IBookMark} in the list. The third index is an array of
   * {@link Long} that represents the IDs of all {@link INewsBin} in the list.
   * Returns <code>null</code> if the list of {@link IFolderChild} did not
   * contain any folders or bookmarks.
   */
  public static Long[][] toPrimitive(Collection<IFolderChild> entities) {
    List<Long> folderIds = null;
    List<Long> bookmarkIds = null;
    List<Long> newsbinIds = null;

    int folderCounter = 0;
    int bookmarkCounter = 0;
    int newsbinCounter = 0;

    for (IEntity entity : entities) {

      /* Folder */
      if (entity instanceof IFolder) {
        if (folderIds == null)
          folderIds = new ArrayList<Long>();

        folderIds.add(entity.getId());
        folderCounter++;
      }

      /* FolderMark (for aggregations) */
      else if (entity instanceof FolderNewsMark) {
        if (folderIds == null)
          folderIds = new ArrayList<Long>();

        folderIds.add(((FolderNewsMark) entity).getFolder().getId());
        folderCounter++;
      }

      /* BookMark */
      else if (entity instanceof IBookMark) {
        if (bookmarkIds == null)
          bookmarkIds = new ArrayList<Long>();

        bookmarkIds.add(entity.getId());
        bookmarkCounter++;
      }

      /* NewsBin */
      else if (entity instanceof INewsBin) {
        if (newsbinIds == null)
          newsbinIds = new ArrayList<Long>();

        newsbinIds.add(entity.getId());
        newsbinCounter++;
      }

      /* Other type not supported */
      else
        throw new IllegalArgumentException("Only Folders, Feeds and News Bins are allowed!"); //$NON-NLS-1$
    }

    if (folderIds == null && bookmarkIds == null && newsbinIds == null)
      return null;

    Long[][] result = new Long[3][];

    int maxEntityCount = Math.max(folderCounter, Math.max(bookmarkCounter, newsbinCounter));

    result[CoreUtils.FOLDER] = toArray(folderIds, maxEntityCount);
    result[CoreUtils.BOOKMARK] = toArray(bookmarkIds, maxEntityCount);
    result[CoreUtils.NEWSBIN] = toArray(newsbinIds, maxEntityCount);;

    return result;
  }

  private static Long[] toArray(List<Long> values, int fillFactor) {
    Long[] array = new Long[fillFactor];

    for (int i = 0; i < fillFactor; i++) {
      if (values != null && i < values.size())
        array[i] = values.get(i);
      else
        array[i] = 0L;
    }

    return array;
  }

  /**
   * @param selection Any structured selection.
   * @return A List of Entities from the given Selection. In case the selection
   * contains an instanceof <code>EntityGroup</code>, only the content of the
   * group is considered.
   */
  public static List<IEntity> getEntities(IStructuredSelection selection) {
    if (selection.isEmpty())
      return new ArrayList<IEntity>(0);

    List<?> elements = selection.toList();
    List<IEntity> entities = new ArrayList<IEntity>(elements.size());

    for (Object object : elements) {
      if (object instanceof IEntity && !entities.contains(object))
        entities.add((IEntity) object);
      else if (object instanceof EntityGroup) {
        List<EntityGroupItem> items = ((EntityGroup) object).getItems();
        for (EntityGroupItem item : items)
          if (!entities.contains(item.getEntity()))
            entities.add(item.getEntity());
      }
    }

    return entities;
  }

  /**
   * @param selection Any structured selection.
   * @return A List of {@link IFolderChild} from the given Selection containing
   * {@link IFolder}, {@link IBookMark} and {@link INewsBin}.
   */
  public static List<IFolderChild> getFoldersBookMarksBins(IStructuredSelection selection) {
    if (selection.isEmpty())
      return new ArrayList<IFolderChild>(0);

    List<?> elements = selection.toList();
    List<IFolderChild> entities = new ArrayList<IFolderChild>(elements.size());

    for (Object object : elements) {
      if (object instanceof IFolder || object instanceof IBookMark || object instanceof INewsBin)
        entities.add((IFolderChild) object);
    }

    return entities;
  }

  /**
   * @param <T>
   * @param selection
   * @param entityClass
   * @return A List of Entities that are instances of <code>entityClass</code>
   * from the given selection. In case the selection contains an instanceof
   * <code>EntityGroup</code>, only the content of the group is considered.
   */
  public static <T extends IEntity> List<T> getEntities(IStructuredSelection selection, Class<T> entityClass) {
    if (selection.isEmpty())
      return new ArrayList<T>(0);

    List<?> elements = selection.toList();
    List<T> entities = new ArrayList<T>(elements.size());

    for (Object object : elements) {
      if (entityClass.isInstance(object) && !entities.contains(entityClass.cast(object)))
        entities.add(entityClass.cast(object));
      else if (object instanceof EntityGroup) {
        List<EntityGroupItem> items = ((EntityGroup) object).getItems();
        for (EntityGroupItem item : items) {
          if (entityClass.isInstance(item.getEntity()) && !entities.contains(entityClass.cast(item.getEntity())))
            entities.add(entityClass.cast(item.getEntity()));
        }
      }
    }

    return entities;
  }

  /**
   * Will return all News of the List of Objects also considering EntityGroups.
   *
   * @param objects
   * @return all News of the List of Objects also considering EntityGroups.
   */
  public static Collection<INews> normalize(List<?> objects) {
    List<INews> normalizedNews = new ArrayList<INews>(objects.size());
    for (Object object : objects) {

      /* News */
      if (object instanceof INews && !normalizedNews.contains(object)) {
        normalizedNews.add((INews) object);
      }

      /* Group */
      else if (object instanceof EntityGroup) {
        EntityGroup group = (EntityGroup) object;
        if (NewsGrouping.GROUP_CATEGORY_ID.equals(group.getCategory())) {
          List<IEntity> entities = group.getEntities();
          for (IEntity entity : entities) {
            if (!normalizedNews.contains(entity))
              normalizedNews.add((INews) entity);
          }
        }
      }
    }

    return normalizedNews;
  }

  /**
   * @param selection Any list of selected <code>INews</code> or
   * <code>EntityGroup</code>.
   * @return Returns a Set of <code>ILabel</code> that <em>all entities</em> of
   * the given Selection had applied to.
   */
  public static Set<ILabel> getLabelsForAll(IStructuredSelection selection) {
    Set<ILabel> labelsForAll = new HashSet<ILabel>(5);

    List<INews> selectedNews = getEntities(selection, INews.class);

    /* For each selected News */
    for (INews news : selectedNews) {
      Set<ILabel> newsLabels = news.getLabels();

      /* Only add Label if contained in all News */
      LabelLoop: for (ILabel newsLabel : newsLabels) {
        if (!labelsForAll.contains(newsLabel)) {
          for (INews news2 : selectedNews) {
            if (!news2.getLabels().contains(newsLabel))
              break LabelLoop;
          }

          labelsForAll.add(newsLabel);
        }
      }
    }

    return labelsForAll;
  }

  /**
   * @param selection a {@link IStructuredSelection} of {@link INews}.
   * @return a {@link List} of {@link IAttachment} and {@link URI} from the
   * selection of {@link INews} pointing to downloadable attachment links.
   */
  public static List<Pair<IAttachment, URI>> getAttachmentLinks(IStructuredSelection selection) {
    List<Pair<IAttachment, URI>> attachmentLinks = new ArrayList<Pair<IAttachment, URI>>();
    Collection<INews> news = normalize(selection.toList());
    for (INews newsitem : news) {
      List<IAttachment> attachments = newsitem.getAttachments();
      for (IAttachment attachment : attachments) {
        URI link = attachment.getLink();
        if (link != null) {
          if (!link.isAbsolute()) {
            try {
              link = URIUtils.resolve(URIUtils.toHTTP(newsitem.getFeedReference().getLink()), link);
            } catch (URISyntaxException e) {
              Activator.getDefault().logError(e.getMessage(), e);
              continue; //Proceed with other Attachments
            }
          }

          attachmentLinks.add(Pair.create(attachment, link));
        }
      }
    }

    return attachmentLinks;
  }

  /**
   * Helper method to load a specific value from preferences by bypassing an
   * implementation detail (see Bug 1291: Simplify filter and group settings
   * treatment).
   *
   * @param preferences the actual scope to load from.
   * @param key the key of the setting to load.
   * @param fallback the fallback preferences to use in case the setting has a
   * negative value.
   * @param fallbackKey the key of the fallback setting to load in case the
   * setting has a negative value.
   * @return the integer value from the given preferences. Never negative.
   */
  public static int loadIntegerValueWithFallback(IPreferenceScope preferences, String key, IPreferenceScope fallback, String fallbackKey) {
    int iVal = preferences.getInteger(key);
    if (iVal >= 0)
      return iVal;

    return Math.max(0, fallback.getInteger(fallbackKey));
  }

  /**
   * Uses the {@link IModelSearch} to determine the number of news inside the
   * given bookmark.
   *
   * @param bookmark the {@link IBookMark} to count news of.
   * @return the number of news inside the bookmark.
   */
  public static int countNews(IBookMark bookmark) {
    ISearchField locationField = Owl.getModelFactory().createSearchField(INews.LOCATION, INews.class.getName());
    ISearchCondition condition = Owl.getModelFactory().createSearchCondition(locationField, SearchSpecifier.SCOPE, ModelUtils.toPrimitive(Arrays.asList(new IFolderChild[] { bookmark })));

    List<SearchHit<NewsReference>> result = Owl.getPersistenceService().getModelSearch().searchNews(Collections.singleton(condition), false);
    return result.size();
  }

  /**
   * @param selection the {@link IStructuredSelection} to look for
   * {@link IFolder} and {@link IFolderChild}. Never <code>null</code>.
   * @return a {@link Pair} of location as {@link IFolder} and position as
   * {@link IFolderChild} or <code>null</code> if no position can be determined.
   */
  public static Pair<IFolder, IFolderChild> getLocationAndPosition(IStructuredSelection selection) {
    IFolder folder = null;
    IFolderChild position = null;

    /* Check Selection */
    if (!selection.isEmpty()) {
      Object firstElement = selection.getFirstElement();
      if (firstElement instanceof IFolder)
        folder = (IFolder) firstElement;
      else if (firstElement instanceof IFolderChild) {
        folder = ((IFolderChild) firstElement).getParent();
        position = ((IFolderChild) firstElement);
      }
    }

    /* Otherwise use visible root-folder */
    if (folder == null)
      folder = OwlUI.getSelectedBookMarkSet();

    return Pair.create(folder, position);
  }

  /**
   * @param selection the selected elements as {@link ISelection}.
   * @return <code>true</code> if the selection contains a {@link EntityGroup}
   * and <code>false</code> otherwise.
   */
  public static boolean isEntityGroupSelected(ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      List<?> list = ((IStructuredSelection) selection).toList();
      for (Object object : list) {
        if (object instanceof EntityGroup)
          return true;
      }
    }

    return false;
  }

  /**
   * @param type the {@link NewsFilter} type.
   * @return a {@link ISearchCondition} matching the provided NewsFilter.Type or
   * <code>null</code> if none.
   */
  public static ISearchCondition getConditionForFilter(NewsFilter.Type type) {
    IModelFactory factory = Owl.getModelFactory();
    switch (type) {
      case SHOW_ALL:
        return null;

      case SHOW_NEW:
        ISearchField field = factory.createSearchField(INews.STATE, INews.class.getName());
        return factory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(INews.State.NEW));

      case SHOW_UNREAD:
        field = factory.createSearchField(INews.STATE, INews.class.getName());
        return factory.createSearchCondition(field, SearchSpecifier.IS, EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED));

      case SHOW_STICKY:
        field = factory.createSearchField(INews.IS_FLAGGED, INews.class.getName());
        return factory.createSearchCondition(field, SearchSpecifier.IS, true);

      case SHOW_LABELED:
        field = factory.createSearchField(INews.LABEL, INews.class.getName());
        return factory.createSearchCondition(field, SearchSpecifier.IS, "*"); //$NON-NLS-1$

      case SHOW_LAST_5_DAYS:
        long now = System.currentTimeMillis();
        long lastFiveDays = DateUtils.getToday().getTimeInMillis() - 5 * DateUtils.DAY;
        long minutes = (now - lastFiveDays) / 60000;
        field = factory.createSearchField(INews.AGE_IN_MINUTES, INews.class.getName());
        return factory.createSearchCondition(field, SearchSpecifier.IS_LESS_THAN, (int) (minutes * -1));

      case SHOW_RECENT:
        now = System.currentTimeMillis();
        long recent = DateUtils.getToday().getTimeInMillis() - DateUtils.DAY;
        minutes = (now - recent) / 60000;
        field = factory.createSearchField(INews.AGE_IN_MINUTES, INews.class.getName());
        return factory.createSearchCondition(field, SearchSpecifier.IS_LESS_THAN, (int) (minutes * -1));
    }

    return null;
  }
}