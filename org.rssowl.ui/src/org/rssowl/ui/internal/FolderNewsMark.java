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

package org.rssowl.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.LongArrayList;
import org.rssowl.core.internal.persist.Mark;
import org.rssowl.core.internal.persist.NewsContainer;
import org.rssowl.core.internal.persist.SearchMark;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.ModelReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IModelSearch;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.util.SearchHit;
import org.rssowl.ui.internal.editors.feed.NewsFilter;
import org.rssowl.ui.internal.editors.feed.NewsFilter.Type;
import org.rssowl.ui.internal.util.ModelUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An internal subclass of {@link Mark} that implements {@link INewsMark} to
 * provide the news of all bookmarks, bins and saved searches inside a folder.
 * The {@link FolderNewsMark} is created dynamically whenever a folder is opened
 * in the feedview and is never persisted to the DB.
 *
 * @author bpasero
 */
@SuppressWarnings("restriction")
public class FolderNewsMark extends Mark implements INewsMark {
  private final Set<Long> fNewsContainer;
  private final IFolder fFolder;
  private final INewsDAO fNewsDao;
  private final IModelFactory fFactory;
  private final IModelSearch fSearch;

  /**
   * Internal implementation of the <code>ModelReference</code> for the internal
   * Type <code>FolderNewsMark</code>.
   *
   * @author bpasero
   */
  public static final class FolderNewsMarkReference extends ModelReference {

    /**
     * @param id
     */
    public FolderNewsMarkReference(long id) {
      super(id, FolderNewsMark.class);
    }

    @Override
    public IFolder resolve() throws PersistenceException {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * @param folder the {@link IFolder} that makes the contents of this
   * {@link INewsMark}.
   */
  public FolderNewsMark(IFolder folder) {
    super(folder.getId(), folder, folder.getName());
    fFolder = folder;
    fNewsContainer = new HashSet<Long>();
    fNewsDao = DynamicDAO.getDAO(INewsDAO.class);
    fFactory = Owl.getModelFactory();
    fSearch = Owl.getPersistenceService().getModelSearch();
  }

  /**
   * @param news the {@link List} of {@link INews} to add into this news mark.
   */
  public void add(Collection<INews> news) {
    synchronized (this) {
      for (INews item : news) {
        if (item != null && item.getId() != null)
          fNewsContainer.add(item.getId());
      }
    }
  }

  private void addAll(long[] elements) {
    synchronized (this) {
      for (long element : elements) {
        if (element > 0)
          fNewsContainer.add(element);
      }
    }
  }

  private void addAll(List<SearchHit<NewsReference>> results) {
    synchronized (this) {
      for (SearchHit<NewsReference> result : results) {
        fNewsContainer.add(result.getResult().getId());
      }
    }
  }

  /**
   * @param news the {@link List} of {@link INews} to remove from this news
   * mark.
   */
  public void remove(Collection<INews> news) {
    synchronized (this) {
      for (INews item : news) {
        if (item != null && item.getId() != null) {
          fNewsContainer.remove(item.getId());
        }
      }
    }
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNewsRefs()
   */
  public List<NewsReference> getNewsRefs() {
    synchronized (this) {
      List<NewsReference> news = new ArrayList<NewsReference>(fNewsContainer.size());
      for (Long id : fNewsContainer) {
        news.add(new NewsReference(id));
      }

      return news;
    }
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNewsRefs(java.util.Set)
   */
  public List<NewsReference> getNewsRefs(Set<State> states) {
    return getNewsRefs();
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNewsCount(java.util.Set)
   */
  public int getNewsCount(Set<State> states) {
    synchronized (this) {
      return fNewsContainer.size();
    }
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#containsNews(org.rssowl.core.persist.INews)
   */
  public boolean containsNews(INews news) {
    return news.getId() != null && containsNews(news.getId());
  }

  /**
   * @param newsId the identifier of the news.
   * @return <code>true</code> if this news mark contains the news and
   * <code>false</code> otherwise.
   */
  public boolean containsNews(long newsId) {
    synchronized (this) {
      return fNewsContainer.contains(newsId);
    }
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNews()
   */
  public List<INews> getNews() {
    synchronized (this) {
      List<INews> news = new ArrayList<INews>(fNewsContainer.size());
      for (Long id : fNewsContainer) {
        INews item = fNewsDao.load(id);
        if (item != null)
          news.add(item);
      }

      return news;
    }
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#getNews(java.util.Set)
   */
  public List<INews> getNews(Set<State> states) {
    return getNews();
  }

  /*
   * @see org.rssowl.core.persist.INewsMark#isGetNewsRefsEfficient()
   */
  public boolean isGetNewsRefsEfficient() {
    return true;
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  public ModelReference toReference() {
    return new FolderNewsMarkReference(getId());
  }

  /**
   * @return the {@link IFolder} that serves as input to this {@link INewsMark}.
   */
  public IFolder getFolder() {
    return fFolder;
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#setProperty(java.lang.String, java.io.Serializable)
   */
  @Override
  public synchronized void setProperty(String key, Serializable value) {
    fFolder.setProperty(key, value);
  }

  /*
   * @see org.rssowl.core.internal.persist.Mark#getName()
   */
  @Override
  public synchronized String getName() {
    return fFolder.getName();
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#getProperties()
   */
  @Override
  public synchronized Map<String, Serializable> getProperties() {
    return fFolder.getProperties();
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#getProperty(java.lang.String)
   */
  @Override
  public synchronized Object getProperty(String key) {
    return fFolder.getProperty(key);
  }

  /*
   * @see org.rssowl.core.internal.persist.Mark#getParent()
   */
  /*
   * @see org.rssowl.core.internal.persist.Mark#getParent()
   */
  @Override
  public synchronized IFolder getParent() {
    return fFolder.getParent();
  }

  /**
   * @param mark the {@link INewsMark} to search in this folder.
   * @return <code>true</code> if this folder contains the given
   * {@link INewsMark} and <code>false</code> otherwise.
   */
  public boolean contains(INewsMark mark) {
    return contains(fFolder, mark);
  }

  private boolean contains(IFolder folder, INewsMark mark) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IFolder && contains((IFolder) child, mark))
        return true;

      if (child.equals(mark))
        return true;
    }

    return false;
  }

  /**
   * @param searchMark the search to find out if its contained in this folder.
   * @return <code>true</code> if the given Search is contained in the
   * {@link IFolder} of this news mark and <code>false</code> otherwise.
   */
  public boolean isRelatedTo(ISearchMark searchMark) {
    return isRelatedTo(fFolder, searchMark);
  }

  private boolean isRelatedTo(IFolder folder, ISearchMark searchMark) {
    List<IFolderChild> children = folder.getChildren();

    for (IFolderChild child : children) {

      /* Check contained in Folder */
      if (child instanceof IFolder && isRelatedTo((IFolder) child, searchMark))
        return true;

      /* Check identical to Child */
      else if (child.equals(searchMark))
        return true;
    }

    return false;
  }

  /**
   * @param news the news to check if its related to this news mark.
   * @return <code>true</code> if the given News belongs to any
   * {@link IBookMark} or {@link INewsBin} of the given {@link IFolder}.
   */
  public boolean isRelatedTo(INews news) {

    /* Check News Container first */
    if (containsNews(news))
      return true;

    /* Then check by Feedlink for new news yet unknown to the news mark */
    FeedLinkReference feedRef = news.getFeedReference();
    return isRelatedTo(fFolder, news, feedRef);
  }

  private boolean isRelatedTo(IFolder folder, INews news, FeedLinkReference ref) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {

      /* Check contained in Folder */
      if (child instanceof IFolder && isRelatedTo((IFolder) child, news, ref))
        return true;

      /* News could be part of the Feed (but is no copy) */
      else if (news.getParentId() == 0 && child instanceof IBookMark) {
        IBookMark bookmark = (IBookMark) child;
        if (bookmark.getFeedLinkReference().equals(ref))
          return true;
      }

      /* News could be part of Bin (and is a copy) */
      else if (news.getParentId() != 0 && child instanceof INewsBin) {
        INewsBin bin = (INewsBin) child;
        if (bin.getId() == news.getParentId())
          return true;
      }
    }

    return false;
  }

  /**
   * @param type the filter type to scope the resulting news properly.
   * @param monitor a monitor to react to cancellation.
   */
  public void resolve(NewsFilter.Type type, IProgressMonitor monitor) {
    doResolve(type, monitor, false);
  }

  /**
   * @param monitor a monitor to react to cancellation.
   * @return a {@link NewsContainer} providing information on included states
   * and news of this {@link IFolder}.
   */
  public NewsContainer resolveNewsContainer(IProgressMonitor monitor) {
    return doResolve(NewsFilter.Type.SHOW_ALL, monitor, true);
  }

  private NewsContainer doResolve(NewsFilter.Type type, IProgressMonitor monitor, boolean resolveContainer) {
    NewsContainer container = null;
    if (resolveContainer)
      container = new NewsContainer(Collections.<INews.State, Boolean> emptyMap());

    /* Clear caches */
    synchronized (this) {
      fNewsContainer.clear();
    }

    /* Return eary on cancellation */
    if (Controller.getDefault().isShuttingDown() || (monitor != null && monitor.isCanceled()))
      return container;

    /* Retrieve filter condition */
    ISearchCondition filterCondition = ModelUtils.getConditionForFilter(type);

    /* Resolve Bookmarks and Newsbins using Location search */
    {
      List<ISearchCondition> conditions = new ArrayList<ISearchCondition>(2);
      ISearchField field = fFactory.createSearchField(INews.LOCATION, INews.class.getName());
      ISearchCondition locationCondition = fFactory.createSearchCondition(field, SearchSpecifier.IS, ModelUtils.toPrimitive(Collections.singleton((IFolderChild) fFolder)));
      conditions.add(locationCondition);
      if (filterCondition != null)
        conditions.add(filterCondition);

      List<SearchHit<NewsReference>> results = fSearch.searchNews(conditions, conditions.size() == 2);
      addAll(results);

      /* Add to container if necessary */
      if (resolveContainer)
        addAll(container, results);

      /* Return eary on cancellation */
      if (Controller.getDefault().isShuttingDown() || (monitor != null && monitor.isCanceled()))
        return container;
    }

    /* Resolve Searches */
    {
      Set<ISearchMark> searches = new HashSet<ISearchMark>();
      findSearches(fFolder, searches);
      for (ISearchMark search : searches) {

        /* Inject the filter condition into the search if it is not an OR query or only 1 condition specified */
        if ((type == Type.SHOW_RECENT || type == Type.SHOW_LAST_5_DAYS || type == Type.SHOW_STICKY || type == Type.SHOW_LABELED)) {
          List<ISearchCondition> conditions = search.getSearchConditions();
          List<SearchHit<NewsReference>> results = fSearch.searchNews(conditions, filterCondition, search.matchAllConditions());
          addAll(results);

          /* Add to container if necessary */
          if (resolveContainer)
            addAll(container, results);
        }

        /* Otherwise pick up the results from the search directly */
        else {
          LongArrayList[] newsIds = ((SearchMark) search).internalGetNewsContainer().internalGetNewsIds();
          for (int i = 0; i < newsIds.length; i++) {

            /* Ignore hidden/deleted and states that are filtered */
            if (i == INews.State.HIDDEN.ordinal() || i == INews.State.DELETED.ordinal())
              continue;
            else if (type == Type.SHOW_NEW && i != INews.State.NEW.ordinal())
              continue;
            else if (type == Type.SHOW_UNREAD && i == INews.State.READ.ordinal())
              continue;

            addAll(newsIds[i].getElements());
          }

          /* Add to container if necessary */
          if (resolveContainer)
            addAll(container, newsIds);
        }

        /* Return eary on cancellation */
        if (Controller.getDefault().isShuttingDown() || (monitor != null && monitor.isCanceled()))
          return container;
      }
    }

    return container;
  }

  private void addAll(NewsContainer container, LongArrayList[] newsIds) {
    for (int i = 0; i < newsIds.length && i < container.internalGetNewsIds().length; i++) {
      LongArrayList list = container.internalGetNewsIds()[i];
      for (int j = 0; j < newsIds[i].size(); j++)
        list.add(newsIds[i].get(j));
    }
  }

  private void addAll(NewsContainer container, List<SearchHit<NewsReference>> results) {
    LongArrayList[] newsIds = container.internalGetNewsIds();
    for (SearchHit<NewsReference> result : results) {
      INews.State state = (State) result.getData(INews.STATE);
      if (newsIds.length > state.ordinal())
        newsIds[state.ordinal()].add(result.getResult().getId());
    }
  }

  private void findSearches(IFolder folder, Set<ISearchMark> searches) {
    List<IFolderChild> children = folder.getChildren();
    for (IFolderChild child : children) {
      if (child instanceof IFolder)
        findSearches((IFolder) child, searches);
      else if (child instanceof ISearchMark)
        searches.add((ISearchMark) child);
    }
  }
}