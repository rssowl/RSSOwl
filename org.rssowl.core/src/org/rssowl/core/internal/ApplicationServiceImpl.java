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

package org.rssowl.core.internal;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.IApplicationService;
import org.rssowl.core.INewsAction;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.MergeResult;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.SortedLongArrayList;
import org.rssowl.core.internal.persist.pref.DefaultPreferences;
import org.rssowl.core.internal.persist.search.Indexer;
import org.rssowl.core.internal.persist.search.ModelSearchImpl;
import org.rssowl.core.internal.persist.search.ModelSearchQueries;
import org.rssowl.core.internal.persist.search.NewsDocument;
import org.rssowl.core.internal.persist.search.SearchDocument;
import org.rssowl.core.internal.persist.service.DB4OIDGenerator;
import org.rssowl.core.internal.persist.service.DBHelper;
import org.rssowl.core.internal.persist.service.DBManager;
import org.rssowl.core.internal.persist.service.DatabaseEvent;
import org.rssowl.core.internal.persist.service.DatabaseListener;
import org.rssowl.core.internal.persist.service.EventManager;
import org.rssowl.core.internal.persist.service.EventsMap;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsDAO;
import org.rssowl.core.persist.dao.ISearchFilterDAO;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.runnable.NewsEventRunnable;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.IDGenerator;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.LoggingSafeRunnable;
import org.rssowl.core.util.RetentionStrategy;
import org.rssowl.core.util.SyncUtils;

import com.db4o.ObjectContainer;
import com.db4o.ext.Db4oException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * db4o and Lucene implementation of IApplicationService.
 */
public class ApplicationServiceImpl implements IApplicationService {

  /* ID of the contributed News Actions */
  private static final String NEWS_ACTION_EXTENSION_POINT = "org.rssowl.core.NewsAction"; //$NON-NLS-1$

  private final Map<String, INewsAction> fNewsActions;
  private volatile ObjectContainer fDb;
  private volatile ReadWriteLock fLock;
  private volatile Lock fWriteLock;

  /**
   * Creates an instance of this class.
   */
  public ApplicationServiceImpl() {
    fNewsActions = new HashMap<String, INewsAction>();
    loadNewsActions();

    DBManager.getDefault().addEntityStoreListener(new DatabaseListener() {
      @Override
      public void databaseOpened(DatabaseEvent event) {
        fDb = event.getObjectContainer();
        fLock = event.getLock();
        fWriteLock = fLock.writeLock();
      }

      @Override
      public void databaseClosed(DatabaseEvent event) {
        fDb = null;
      }
    });
  }

  private void loadNewsActions() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IConfigurationElement elements[] = reg.getConfigurationElementsFor(NEWS_ACTION_EXTENSION_POINT);
    for (IConfigurationElement element : elements) {
      try {
        String id = element.getAttribute("id"); //$NON-NLS-1$
        fNewsActions.put(id, (INewsAction) element.createExecutableExtension("class"));//$NON-NLS-1$
      } catch (InvalidRegistryObjectException e) {
        Activator.getDefault().logError(e.getMessage(), e);
      } catch (CoreException e) {
        Activator.getDefault().getLog().log(e.getStatus());
      }
    }
  }

  /*
   * @see
   * org.rssowl.core.IApplicationService#handleFeedReload(org.rssowl.core.persist
   * .IBookMark, org.rssowl.core.persist.IFeed,
   * org.rssowl.core.persist.IConditionalGet, boolean, boolean,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public final void handleFeedReload(final IBookMark bookMark, IFeed interpretedFeed, IConditionalGet conditionalGet, boolean deleteConditionalGet, boolean runRetention, final IProgressMonitor monitor) {
    fWriteLock.lock();
    MergeResult mergeResult = null;
    try {

      /* Resolve reloaded Feed */
      IFeed feed = bookMark.getFeedLinkReference().resolve();

      /* Feed could have been deleted meanwhile! */
      if (feed == null)
        return;

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Copy over Properties to reloaded Feed to keep them */
      Map<String, Serializable> feedProperties = feed.getProperties();
      if (feedProperties != null) {
        feedProperties.entrySet();
        for (Map.Entry<String, Serializable> entry : feedProperties.entrySet())
          interpretedFeed.setProperty(entry.getKey(), entry.getValue());
      }

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Create labels as necessary from Sync and assign to news */
      boolean isSynced = SyncUtils.isSynchronized(bookMark);
      if (isSynced) {

        /* Determine those Labels the user has explicitly deleted and ignore */
        String[] labelsToIgnore = Owl.getPreferenceService().getGlobalScope().getStrings(DefaultPreferences.DELETED_LABELS);
        List<String> labelsToIgnoreList = (labelsToIgnore != null) ? new ArrayList<String>(labelsToIgnore.length) : Collections.<String> emptyList();
        if (labelsToIgnore != null) {
          for (String label : labelsToIgnore) {
            labelsToIgnoreList.add(label);
          }
        }

        /* Collect All Incoming Labels */
        boolean hasLabels = false;
        Set<String> incomingLabels = new HashSet<String>();
        for (INews item : interpretedFeed.getNews()) {
          Object labelsObj = item.getProperty(SyncUtils.GOOGLE_LABELS);
          if (labelsObj != null && labelsObj instanceof String[]) {
            String[] labels = (String[]) labelsObj;
            for (String label : labels) {
              if (!labelsToIgnoreList.contains(label))
                incomingLabels.add(label);
            }
            hasLabels = true;
          }
        }

        /* Determine the New Labels to Create */
        if (!incomingLabels.isEmpty()) {

          /* Existing Labels */
          Collection<ILabel> existingLabels = DynamicDAO.loadAll(ILabel.class);
          Map<String, ILabel> mapNameToLabel = new HashMap<String, ILabel>();
          for (ILabel label : existingLabels) {
            mapNameToLabel.put(label.getName(), label);
          }

          /* New Labels to Create */
          Set<ILabel> labelsToCreate = new HashSet<ILabel>();
          for (String incomingLabel : incomingLabels) {
            if (!mapNameToLabel.containsKey(incomingLabel)) {
              ILabel newLabel = Owl.getModelFactory().createLabel(null, incomingLabel);
              newLabel.setColor("0,0,0"); //$NON-NLS-1$
              newLabel.setOrder(mapNameToLabel.size());
              mapNameToLabel.put(incomingLabel, newLabel);

              labelsToCreate.add(newLabel);
            }
          }

          /* Save new Labels */
          if (!labelsToCreate.isEmpty())
            DynamicDAO.saveAll(labelsToCreate);

          /* Assign Labels to News */
          for (INews item : interpretedFeed.getNews()) {
            Object labelsObj = item.getProperty(SyncUtils.GOOGLE_LABELS);
            if (labelsObj != null && labelsObj instanceof String[]) {
              String[] labels = (String[]) labelsObj;
              for (String labelName : labels) {
                ILabel label = mapNameToLabel.get(labelName);
                if (label != null)
                  item.addLabel(label);
              }
            }
            item.removeProperty(SyncUtils.GOOGLE_LABELS);
          }
        }

        /* Otherwise make sure to clean up properties for Labels */
        else if (hasLabels) {
          for (INews item : interpretedFeed.getNews()) {
            item.removeProperty(SyncUtils.GOOGLE_LABELS);
          }
        }

        /* Return early on cancellation */
        if (monitor.isCanceled() || Owl.isShuttingDown())
          return;
      }

      /* Merge with existing */
      mergeResult = feed.mergeAndCleanUp(interpretedFeed);
      final List<INews> newNewsAdded = getNewNewsAdded(feed);

      /* Now adjust News State based on Sync */
      if (isSynced) {
        for (INews item : newNewsAdded) {

          /* News Marked Read */
          if (item.getProperty(SyncUtils.GOOGLE_MARKED_READ) != null) {
            item.setState(INews.State.READ);
            item.removeProperty(SyncUtils.GOOGLE_MARKED_READ);
          }

          /* News Marked Unread */
          else if (item.getProperty(SyncUtils.GOOGLE_MARKED_UNREAD) != null) {
            item.setState(INews.State.UNREAD);
            item.removeProperty(SyncUtils.GOOGLE_MARKED_UNREAD);
          }
        }
      }

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Update Date of last added news in Bookmark */
      if (!newNewsAdded.isEmpty()) {
        Date mostRecentDate = DateUtils.getRecentDate(newNewsAdded);
        Date previousMostRecentDate = bookMark.getMostRecentNewsDate();
        if (previousMostRecentDate == null || mostRecentDate.after(previousMostRecentDate)) {
          bookMark.setMostRecentNewsDate(mostRecentDate);
          fDb.set(bookMark);
        }
      }

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Update state of added news if equivalent news already exists */
      SafeRunner.run(new LoggingSafeRunnable() {
        @Override
        public void run() throws Exception { //See Bug 1216 (NPE in ModelSearchImpl.getCurrentSearcher)
          if (Owl.getPreferenceService().getGlobalScope().getBoolean(DefaultPreferences.MARK_READ_DUPLICATES))
            updateStateOfUnsavedNewNews(newNewsAdded, monitor);
        }
      });

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Retention Policy */
      final List<INews> deletedNews = runRetention ? RetentionStrategy.process(bookMark, feed) : Collections.<INews>emptyList();
      for (INews news : deletedNews)
        mergeResult.addUpdatedObject(news);

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Set ID to News and handle Description entity */
      IDGenerator generator = Owl.getPersistenceService().getIDGenerator();
      for (INews news : newNewsAdded) {

        /* Return early on cancellation */
        if (monitor.isCanceled() || Owl.isShuttingDown())
          return;

        long id;
        if (generator instanceof DB4OIDGenerator)
          id = ((DB4OIDGenerator) generator).getNext(false);
        else
          id = generator.getNext();

        news.setId(id);

        String description = ((News) news).getTransientDescription();
        if (description != null) {
          mergeResult.addUpdatedObject(new Description(news, description));
        }
      }

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Run News Filters */
      final AtomicBoolean someNewsFiltered = new AtomicBoolean(false);
      SafeRunner.run(new LoggingSafeRunnable() {
        @Override
        public void run() throws Exception {
          newNewsAdded.removeAll(deletedNews);
          if (!newNewsAdded.isEmpty()) {
            boolean result = runNewsFilters(newNewsAdded, bookMark.getFeedLinkReference().getLinkAsText(), monitor);
            someNewsFiltered.set(result);
          }
        }
      });

      /* Return early on cancellation and if no filter was running */
      if ((monitor.isCanceled() || Owl.isShuttingDown()) && !someNewsFiltered.get())
        return;

      try {
        lockNewsObjects(mergeResult);
        saveFeed(mergeResult);

        /* Update Conditional GET */
        if (conditionalGet != null) {
          if (deleteConditionalGet)
            fDb.delete(conditionalGet);
          else
            fDb.ext().set(conditionalGet, 1);
        }
        DBHelper.preCommit(fDb);
        fDb.commit();
      } finally {
        unlockNewsObjects(mergeResult);
      }
    } catch (Db4oException e) {
      DBHelper.rollbackAndPE(fDb, e);
    } finally {
      fWriteLock.unlock();
    }
    DBHelper.cleanUpAndFireEvents();
  }

  private Set<ISearchFilter> loadEnabledFilters(String feedLink) {

    /* Load Filters */
    Collection<ISearchFilter> filters = DynamicDAO.getDAO(ISearchFilterDAO.class).loadAll();
    if (filters.isEmpty())
      return Collections.emptySet();

    /* Sort filters by ID */
    Set<ISearchFilter> enabledFilters = new TreeSet<ISearchFilter>(new Comparator<ISearchFilter>() {
      @Override
      public int compare(ISearchFilter f1, ISearchFilter f2) {
        if (f1.equals(f2))
          return 0;

        return f1.getOrder() < f2.getOrder() ? -1 : 1;
      }
    });

    /* Only consider enabled filters */
    for (ISearchFilter filter : filters) {
      if (filter.isEnabled())
        enabledFilters.add(filter);
    }

    /* Return early if only disabled filters found */
    if (enabledFilters.isEmpty())
      return Collections.emptySet();

    /* Return early if the first filter "Matches All" news */
    if (!needToIndex(enabledFilters))
      return enabledFilters;

    /* Finally remove those filters that are scoped to different feeds */
    CoreUtils.removeFiltersByScope(enabledFilters, feedLink);

    return enabledFilters;
  }

  private boolean needToIndex(Set<ISearchFilter> filters) {
    ISearchFilter firstFilter = filters.iterator().next();
    return firstFilter.getSearch() != null;
  }

  private boolean runNewsFilters(final List<INews> news, String feedLink, final IProgressMonitor monitor) throws Exception {

    /* Load Enabled Filters that are scoped to given Feed */
    Set<ISearchFilter> enabledFilters = loadEnabledFilters(feedLink);

    /* Nothing to do */
    if (enabledFilters.isEmpty())
      return false;

    /* Return early on cancellation */
    if (monitor.isCanceled() || Owl.isShuttingDown())
      return false;

    /* Need to index News and perform Searches */
    RAMDirectory directory = null;
    final IndexSearcher[] searcher = new IndexSearcher[1];
    if (needToIndex(enabledFilters)) {
      boolean indexDescription = needToIndexDescription(enabledFilters);
      directory = new RAMDirectory();
      directory.setLockFactory(NoLockFactory.getNoLockFactory());

      /* Index News */
      try {
        IndexWriter indexWriter = new IndexWriter(directory, Indexer.createAnalyzer());
        for (int i = 0; i < news.size(); i++) {

          /* Return early on cancellation */
          if (monitor.isCanceled() || Owl.isShuttingDown())
            return false;

          NewsDocument document = new NewsDocument(news.get(i));
          document.addFields(indexDescription);
          document.getDocument().getField(SearchDocument.ENTITY_ID_TEXT).setValue(String.valueOf(i));
          indexWriter.addDocument(document.getDocument());
        }
        indexWriter.close();

        searcher[0] = new IndexSearcher(directory);
      } catch (Exception e) {
        directory.close();
        throw e;
      }
    }

    /* Remember the news already filtered */
    List<INews> filteredNews = new ArrayList<INews>(news.size());
    boolean filterMatchedAll = false;

    /* Iterate over Filters */
    for (ISearchFilter filter : enabledFilters) {

      /* No Search Required */
      if (filter.getSearch() == null) {
        filterMatchedAll = true;

        List<INews> remainingNews = new ArrayList<INews>(news);
        remainingNews.removeAll(filteredNews);
        if (!remainingNews.isEmpty())
          applyFilter(filter, remainingNews);

        /* Done - we only support 1 filter per News */
        break;
      }

      /* Search Required */
      else if (directory != null && searcher[0] != null) {

        /* Return early if cancelled and nothing filtered yet */
        if ((monitor.isCanceled() || Owl.isShuttingDown()) && filteredNews.isEmpty())
          return false;

        try {
          final List<INews> matchingNews = new ArrayList<INews>();

          /* Perform Query */
          Query query = ModelSearchQueries.createQuery(filter.getSearch());
          searcher[0].search(query, new HitCollector() {
            @Override
            public void collect(int doc, float score) {
              try {
                Document document = searcher[0].doc(doc);
                int index = Integer.valueOf(document.get(SearchDocument.ENTITY_ID_TEXT));
                matchingNews.add(news.get(index));
              } catch (CorruptIndexException e) {
                Activator.getDefault().logError(e.getMessage(), e);
              } catch (IOException e) {
                Activator.getDefault().logError(e.getMessage(), e);
              }
            }
          });

          /* Apply Filter */
          matchingNews.removeAll(filteredNews);
          if (!matchingNews.isEmpty()) {
            applyFilter(filter, matchingNews);
            filteredNews.addAll(matchingNews);
          }
        } catch (IOException e) {
          directory.close();
          throw e;
        }
      }
    }

    /* Free RAMDirectory if it was built */
    if (directory != null)
      directory.close();

    return filterMatchedAll || !filteredNews.isEmpty();
  }

  private boolean needToIndexDescription(Set<ISearchFilter> filters) {
    for (ISearchFilter filter : filters) {
      ISearch search = filter.getSearch();
      if (search != null) {
        List<ISearchCondition> conditions = search.getSearchConditions();
        for (ISearchCondition condition : conditions) {
          int fieldId = condition.getField().getId();
          if (fieldId == IEntity.ALL_FIELDS || fieldId == INews.DESCRIPTION)
            return true;
        }
      }
    }
    return false;
  }

  private void applyFilter(final ISearchFilter filter, final List<INews> news) {
    final Map<INews, INews> replacements = new HashMap<INews, INews>();
    Collection<IFilterAction> actions = CoreUtils.getActions(filter); //Need to sort structural actions to end
    for (final IFilterAction action : actions) {
      final INewsAction newsAction = fNewsActions.get(action.getActionId());
      if (newsAction != null) {
        SafeRunner.run(new LoggingSafeRunnable() {
          @Override
          public void run() throws Exception {
            newsAction.run(news, replacements, action.getData());
          }
        });
      }
    }

    /* Notify listeners */
    SafeRunner.run(new LoggingSafeRunnable() {
      @Override
      public void run() throws Exception {
        DynamicDAO.getDAO(ISearchFilterDAO.class).fireFilterApplied(filter, news);
      }
    });
  }

  private void lockNewsObjects(MergeResult mergeResult) {
    for (Object object : mergeResult.getUpdatedObjects()) {
      if (object instanceof News) {
        ((News) object).acquireReadLockSpecial();
      }
    }
  }

  private void unlockNewsObjects(MergeResult mergeResult) {
    if (mergeResult != null) {
      for (Object object : mergeResult.getUpdatedObjects()) {
        if (object instanceof News) {
          News news = (News) object;
          news.releaseReadLockSpecial();
          news.clearTransientDescription();
        }
      }
    }
  }

  private List<INews> getNewNewsAdded(IFeed feed) {
    List<INews> newsList = feed.getNewsByStates(EnumSet.of(INews.State.NEW));

    for (ListIterator<INews> it = newsList.listIterator(newsList.size()); it.hasPrevious();) {
      INews news = it.previous();
      if (news.getId() != null) //News added during merge have no id assigned yet
        it.remove();
    }
    return newsList;
  }

  private void updateStateOfUnsavedNewNews(List<INews> news, IProgressMonitor monitor) {
    if (news.isEmpty())
      return;

    /* Find Links and GUIDs */
    List<URI> links = new ArrayList<URI>();
    List<IGuid> guids = new ArrayList<IGuid>();
    for (INews item : news) {
      if (SyncUtils.isSynchronized(item))
        continue; //Not offering state sync from duplicates for synced news items

      if (item.getGuid() != null)
        guids.add(item.getGuid());
      else if (item.getLink() != null)
        links.add(item.getLink());
    }

    if (links.isEmpty() && guids.isEmpty())
      return;

    /* Search existing News by Links and GUIDs */
    ModelSearchImpl modelSearch = (ModelSearchImpl) Owl.getPersistenceService().getModelSearch();
    Map<URI, List<NewsReference>> linkToNewsRefs = modelSearch.searchNewsByLinks(links, false, monitor);
    Map<IGuid, List<NewsReference>> guidToNewsRefs = modelSearch.searchNewsByGuids(guids, false, monitor);
    for (INews item : news) {

      /* Return early on cancellation */
      if (monitor.isCanceled() || Owl.isShuttingDown())
        return;

      /* Lookup equivalent news via GUID */
      List<NewsReference> equivalentNewsRefs = guidToNewsRefs.get(item.getGuid());
      if (equivalentNewsRefs != null && !equivalentNewsRefs.isEmpty()) {
        NewsReference newsRef = equivalentNewsRefs.get(0);
        INews resolvedNews = newsRef.resolve();
        if (resolvedNews != null && resolvedNews.isVisible())
          item.setState(resolvedNews.getState());
        else {
          logWarning(NLS.bind(Messages.ApplicationServiceImpl_ERROR_STALE_LUCENE_INDEX, newsRef.getId()));
          CoreUtils.reportIndexIssue();
        }
      }

      /* Lookup equivalent news via Link */
      else {
        equivalentNewsRefs = linkToNewsRefs.get(item.getLink());
        if (equivalentNewsRefs != null && !equivalentNewsRefs.isEmpty()) {
          NewsReference newsRef = equivalentNewsRefs.get(0);
          INews resolvedNews = newsRef.resolve();
          if (resolvedNews != null && resolvedNews.isVisible())
            item.setState(resolvedNews.getState());
          else {
            logWarning(NLS.bind(Messages.ApplicationServiceImpl_ERROR_STALE_LUCENE_INDEX, newsRef.getId()));
            CoreUtils.reportIndexIssue();
          }
        }
      }
    }
  }

  private void logWarning(String message) {
    Activator activator = Activator.getDefault();
    activator.getLog().log(activator.createWarningStatus(message, null));
  }

  private void saveFeed(MergeResult mergeResult) {
    SortedLongArrayList descriptionUpdatedIds = new SortedLongArrayList(10);

    /* Removed Objects */
    for (Object o : mergeResult.getRemovedObjects()) {
      if (o instanceof INews)
        EventManager.getInstance().addItemBeingDeleted(((INews) o).getFeedReference());
      else if (o instanceof IAttachment)
        EventManager.getInstance().addItemBeingDeleted(((IAttachment) o).getNews());
      else if (o instanceof Description)
        descriptionUpdatedIds.add(((Description) o).getNews().getId());

      fDb.delete(o);
    }

    /* Updated Objects */
    List<Object> otherObjects = new ArrayList<Object>();
    for (Object o : mergeResult.getUpdatedObjects()) {
      if (o instanceof INews)
        DBHelper.saveUpdatedNews(fDb, (INews) o);
      else {
        if (o instanceof Description)
          descriptionUpdatedIds.add(((Description) o).getNews().getId());

        otherObjects.add(o);
      }
    }

    for (Object o : otherObjects) {
      if (o instanceof IFeed)
        fDb.ext().set(o, 2);
      else
        fDb.ext().set(o, 1);
    }

    NewsEventRunnable eventRunnables = DBHelper.getNewsEventRunnables(EventsMap.getInstance().getEventRunnables());
    if (eventRunnables != null) {
      for (NewsEvent event : eventRunnables.getAllEvents())
        descriptionUpdatedIds.removeByElement(event.getEntity().getId().longValue());
    }

    INewsDAO newsDao = DynamicDAO.getDAO(INewsDAO.class);
    for (int i = 0, c = descriptionUpdatedIds.size(); i < c; ++i) {
      long newsId = descriptionUpdatedIds.get(i);
      INews news = newsDao.load(newsId);
      INews oldNews = DBHelper.peekPersistedNews(fDb, news);
      EventsMap.getInstance().putUpdateEvent(new NewsEvent(oldNews, news, false));
    }
  }
}