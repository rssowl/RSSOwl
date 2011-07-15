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

package org.rssowl.core.internal.persist.service;

import org.rssowl.core.Owl;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.NewsBin;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.IConditionalGetDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.event.AttachmentEvent;
import org.rssowl.core.persist.event.BookMarkEvent;
import org.rssowl.core.persist.event.CategoryEvent;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.FolderEvent;
import org.rssowl.core.persist.event.LabelEvent;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.PersonEvent;
import org.rssowl.core.persist.event.PreferenceEvent;
import org.rssowl.core.persist.event.SearchConditionEvent;
import org.rssowl.core.persist.event.SearchEvent;
import org.rssowl.core.persist.event.SearchFilterEvent;
import org.rssowl.core.persist.event.SearchMarkEvent;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.service.IDGenerator;

import com.db4o.ObjectContainer;
import com.db4o.events.Event4;
import com.db4o.events.EventArgs;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectEventArgs;
import com.db4o.query.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central manager for events and related actions on the database.
 */
public class EventManager implements DatabaseListener {

  /**
   * Iterator implementation that iterates from the end of the list. Useful if
   * items need to be removed from the list during iteration and specific method
   * needs to be called for removal.
   */
  private final static class ReverseIterator<T> implements Iterable<T>, Iterator<T> {
    private final List<T> fList;
    private int index;

    static <T> ReverseIterator<T> createInstance(List<T> list) {
      return new ReverseIterator<T>(list);
    }

    private ReverseIterator(List<T> list) {
      fList = list;
      index = list.size() - 1;
    }

    public final Iterator<T> iterator() {
      return this;
    }

    public final boolean hasNext() {
      return index > -1;
    }

    public final T next() {
      return fList.get(index--);
    }

    public final void remove() {
      throw new UnsupportedOperationException();
    }

  }

  private final ThreadLocal<Set<Object>> fItemsBeingDeleted = new ThreadLocal<Set<Object>>();
  private static final String PARENT_DELETED_KEY = "rssowl.db4o.EventManager.parentDeleted"; //$NON-NLS-1$
  private final static EventManager INSTANCE = new EventManager();
  private ObjectContainer fDb;
  private IConditionalGetDAO fConditionalGetDAO;
  private IDGenerator fIDGenerator;
  private INewsCounterDAO fNewsCounterDAO;

  private EventManager() {
    initEntityStoreListener();
  }

  private IDGenerator getIDGenerator() {
    if (fIDGenerator == null)
      fIDGenerator = Owl.getPersistenceService().getIDGenerator();

    return fIDGenerator;
  }

  private IConditionalGetDAO getConditionalGetDAO() {
    if (fConditionalGetDAO == null)
      fConditionalGetDAO = Owl.getPersistenceService().getDAOService().getConditionalGetDAO();

    return fConditionalGetDAO;
  }

  private INewsCounterDAO getNewsCounterDAO() {
    if (fNewsCounterDAO == null)
      fNewsCounterDAO = InternalOwl.getDefault().getPersistenceService().getDAOService().getNewsCounterDAO();

    return fNewsCounterDAO;
  }

  private void initEventRegistry() {
    EventRegistry eventRegistry = EventRegistryFactory.forObjectContainer(fDb);

    EventListener4 updatedListener = new EventListener4() {
      public void onEvent(Event4 e, EventArgs args) {
        processUpdatedEvent(args);
      }
    };

    EventListener4 creatingListener = new EventListener4() {
      public void onEvent(Event4 e, EventArgs args) {
        processCreatingEvent(args);
      }
    };

    EventListener4 createdListener = new EventListener4() {
      public void onEvent(Event4 e, EventArgs args) {
        processCreatedEvent(args);
      }
    };

    EventListener4 deletingListener = new EventListener4() {
      public void onEvent(Event4 e, EventArgs args) {
        processDeletingEvent(args);
      }
    };

    EventListener4 deletedListener = new EventListener4() {
      public void onEvent(Event4 e, EventArgs args) {
        processDeletedEvent(args);
      }
    };

    EventListener4 activatedListener = new EventListener4() {
      public void onEvent(Event4 e, EventArgs args) {
        processActivated(args);
      }
    };

    eventRegistry.activated().addListener(activatedListener);
    eventRegistry.created().addListener(createdListener);
    eventRegistry.creating().addListener(creatingListener);
    eventRegistry.updated().addListener(updatedListener);
    eventRegistry.deleting().addListener(deletingListener);
    eventRegistry.deleted().addListener(deletedListener);
  }

  private void processActivated(EventArgs args) {
    IEntity entity = getEntity(args);
    if (entity == null)
      return;

    if (entity instanceof News)
      ((News) entity).init();
    else if (entity instanceof BookMark)
      initBookMark((BookMark) entity);
  }

  private void initBookMark(BookMark entity) {
    entity.setNewsCounter(getNewsCounterDAO().load());
  }

  private void processUpdatedEvent(EventArgs args) {
    IEntity entity = getEntity(args);
    if (entity == null)
      return;

    ModelEvent event = createModelEvent(entity);
    if (event != null)
      EventsMap.getInstance().putUpdateEvent(event);
  }

  /*
   * Test items: News created, needs to save a description, but before assign
   * news id to description News updated from NewsDAO or recursively from Feed
   * dao (handleFeedReloaded does it on its own), must check if description
   * changed. If it did, then update description too and make sure that news
   * event is fired (e.g. nothing else changed) News deleted, needs to delete
   * description on handle feed reloaded, if description.getValue is null,
   * delete
   */
  private void processCreatingEvent(EventArgs args) {
    IEntity entity = getEntity(args);

    if (entity != null) {
      setId(entity);
      if (entity instanceof BookMark)
        initBookMark((BookMark) entity);
    }
  }

  private void processCreatedEvent(EventArgs args) {
    IEntity entity = getEntity(args);
    if (entity == null)
      return;

    ModelEvent event = createModelEvent(entity);
    if (event != null)
      EventsMap.getInstance().putPersistEvent(event);
  }

  private void processDeletingEvent(EventArgs args) {
    IEntity entity = getEntity(args);
    if (entity == null)
      return;

    if (entity instanceof INews)
      cascadeNewsDeletion((INews) entity);
    else if (entity instanceof IFeed)
      cascadeFeedDeletion((IFeed) entity);
    else if (entity instanceof IMark)
      cascadeMarkDeletion((IMark) entity);
    else if (entity instanceof IFolder)
      removeFromParentFolderAndCascade((IFolder) entity);
    else if (entity instanceof IAttachment)
      removeFromParentNews((IAttachment) entity);
    else if (entity instanceof ISearchCondition)
      cascadeSearchConditionDeletion((ISearchCondition) entity);
    else if (entity instanceof ISearchFilter)
      cascadeSearchFilterDeletion((ISearchFilter) entity);
    else if (entity instanceof ISearch)
      cascadeSearchDeletion((ISearch) entity);
  }

  private void cascadeSearchFilterDeletion(ISearchFilter entity) {
    fDb.delete(entity.getSearch());
  }

  private void cascadeNewsBinDeletion(INewsBin entity) {
    Set<FeedLinkReference> removedFeedRefs = new HashSet<FeedLinkReference>();
    DBHelper.removeNews(fDb, removedFeedRefs, entity.getNewsRefs());
    DBHelper.removeFeedsAfterNewsBinUpdate(fDb, removedFeedRefs);
    if (entity instanceof NewsBin)
      fDb.delete(((NewsBin) entity).internalGetNewsContainer());

  }

  private void cascadeSearchConditionDeletion(ISearchCondition searchCondition) {
    ISearchMark searchMark = loadSearchMark(searchCondition);
    if (searchMark != null) {
      if (!itemsBeingDeletedContains(searchMark)) {
        if (searchMark.removeSearchCondition(searchCondition))
          fDb.ext().set(searchMark, 2);
      }
    }
    fDb.delete(searchCondition.getField());
  }

  private ISearchMark loadSearchMark(ISearchCondition searchCondition) {
    ISearchMark mark = Owl.getPersistenceService().getDAOService().getSearchMarkDAO().load(searchCondition);
    return mark;
  }

  private void cascadeNewsDeletion(INews news) {
    addItemBeingDeleted(news);
    if (news.getParentId() == 0)
      removeFromParentFeed(news);

    /*
     * It seems like the categories are not activated at this stage at times.
     * This seems like a db4o bug, but not totally sure. In any case, we play
     * safe and activate the news fully in that case.
     */
    if (news.getCategories().isEmpty())
      fDb.activate(news, Integer.MAX_VALUE);

    fDb.delete(news.getGuid());
    fDb.delete(news.getSource());
    fDb.delete(news.getAuthor());

    for (ICategory category : ReverseIterator.createInstance(news.getCategories()))
      fDb.delete(category);
    for (IAttachment attachment : ReverseIterator.createInstance(news.getAttachments()))
      fDb.delete(attachment);

    Description description = DBHelper.getDescriptionDAO().load(news.getId());
    if (description != null)
      fDb.delete(description);
  }

  private void cascadeMarkDeletion(IMark mark) {
    removeFromParentFolder(mark);
    if (mark instanceof IBookMark)
      deleteFeedIfNecessary((IBookMark) mark);
    else if (mark instanceof ISearchMark)
      cascadeSearchMarkDeletion((ISearchMark) mark);
    else if (mark instanceof INewsBin)
      cascadeNewsBinDeletion((INewsBin) mark);
  }

  private void cascadeSearchMarkDeletion(ISearchMark mark) {
    addItemBeingDeleted(mark);
    for (ISearchCondition condition : mark.getSearchConditions())
      fDb.delete(condition);
  }

  private void cascadeSearchDeletion(ISearch search) {
    for (ISearchCondition condition : search.getSearchConditions())
      fDb.delete(condition);
  }

  private void cascadeFeedDeletion(IFeed feed) {
    addItemBeingDeleted(new FeedLinkReference(feed.getLink()));
    fDb.delete(feed.getImage());
    fDb.delete(feed.getAuthor());
    for (ICategory category : ReverseIterator.createInstance(feed.getCategories())) {
      fDb.delete(category);
    }

    for (INews news : ReverseIterator.createInstance(feed.getNews())) {
      fDb.delete(news);
    }

    IConditionalGet conditionalGet = getConditionalGetDAO().load(feed.getLink());
    if (conditionalGet != null)
      fDb.delete(conditionalGet);

    removeFromItemsBeingDeleted(feed);
  }

  private void removeFromParentNews(IAttachment attachment) {
    INews news = attachment.getNews();
    if (itemsBeingDeletedContains(news))
      return;

    news.removeAttachment(attachment);
    fDb.set(news);
  }

  private void removeFromParentFolderAndCascade(IFolder folder) {
    IFolder parentFolder = folder.getParent();
    if (parentFolder != null) {
      parentFolder.removeChild(folder);
      fDb.set(parentFolder);
    }

    for (IFolder child : ReverseIterator.createInstance(folder.getFolders())) {
      cascadeFolderDeletion(child);
    }

    for (IMark mark : ReverseIterator.createInstance(folder.getMarks())) {
      mark.setProperty(PARENT_DELETED_KEY, true);
      fDb.delete(mark);
    }
  }

  private void cascadeFolderDeletion(IFolder folder) {
    for (IFolder child : ReverseIterator.createInstance(folder.getFolders())) {
      cascadeFolderDeletion(child);
    }

    for (IMark mark : ReverseIterator.createInstance(folder.getMarks())) {
      mark.setProperty(PARENT_DELETED_KEY, true);
      fDb.delete(mark);
    }

    folder.setParent(null);

    fDb.delete(folder);
  }

  private void removeFromParentFolder(IMark mark) {
    IFolder parentFolder = mark.getParent();
    parentFolder.removeChild(mark);
    if (mark.getProperty(PARENT_DELETED_KEY) == null)
      fDb.set(parentFolder);
    else
      mark.removeProperty(PARENT_DELETED_KEY);
  }

  private void removeFromParentFeed(INews news) {
    FeedLinkReference feedRef = news.getFeedReference();
    if (itemsBeingDeletedContains(feedRef))
      return;

    IFeed feed = feedRef.resolve();

    /* If the news was still within parent, update parent */
    if (feed.removeNews(news))
      fDb.ext().set(feed, 2);
  }

  private boolean removeFromItemsBeingDeleted(Object entity) {
    Set<Object> entities = fItemsBeingDeleted.get();
    if (entities == null)
      return false;

    return entities.remove(entity);
  }

  private boolean itemsBeingDeletedContains(Object entity) {
    Set<Object> entities = fItemsBeingDeleted.get();
    if (entities == null)
      return false;

    return entities.contains(entity);
  }

  private void deleteFeedIfNecessary(IBookMark mark) {
    Query query = fDb.query();
    query.constrain(Feed.class);
    query.descend("fLinkText").constrain(mark.getFeedLinkReference().getLink().toString()); //$NON-NLS-1$
    @SuppressWarnings("unchecked")
    List<IFeed> feeds = query.execute();
    for (IFeed feed : feeds) {
      FeedLinkReference feedRef = new FeedLinkReference(feed.getLink());
      if (DBHelper.countBookMarkReference(fDb, feedRef) == 1) {
        if (DBHelper.feedHasNewsWithCopies(fDb, feedRef)) {
          List<INews> newsList = new ArrayList<INews>(feed.getNews());
          for (INews news : newsList) {
            feed.removeNews(news);
            addItemBeingDeleted(feed);
            fDb.delete(news);
          }
          fDb.ext().set(feed, 2);
        } else
          fDb.delete(feed);
      }
    }
  }

  private void processDeletedEvent(EventArgs args) {
    IEntity entity = getEntity(args);
    if (entity == null)
      return;

    ModelEvent event = createModelEvent(entity);
    if (event != null)
      EventsMap.getInstance().putRemoveEvent(event);
  }

  private IEntity getEntity(EventArgs args) {
    ObjectEventArgs queryArgs = ((ObjectEventArgs) args);
    Object o = queryArgs.object();
    if (o instanceof IEntity) {
      IEntity entity = (IEntity) o;
      return entity;
    }
    return null;
  }

  private ModelEvent createModelEvent(IEntity entity) {
    ModelEvent modelEvent = null;
    Map<IEntity, ModelEvent> templatesMap = EventsMap.getInstance().getEventTemplatesMap();
    ModelEvent template = templatesMap.get(entity); //TODO In some cases, the template is complete. We can save some object allocation by reusing it.

    boolean root = isRoot(template);
    boolean merged = isMerged(template);
    if (entity instanceof INews) {
      modelEvent = createNewsEvent((INews) entity, template, root, merged);
    } else if (entity instanceof IAttachment) {
      IAttachment attachment = (IAttachment) entity;
      modelEvent = new AttachmentEvent(attachment, root);
    } else if (entity instanceof ICategory) {
      ICategory category = (ICategory) entity;
      modelEvent = new CategoryEvent(category, root);
    } else if (entity instanceof IFeed) {
      IFeed feed = (IFeed) entity;
      modelEvent = new FeedEvent(feed, root);
    } else if (entity instanceof IPerson) {
      IPerson person = (IPerson) entity;
      modelEvent = new PersonEvent(person, root);
    } else if (entity instanceof IBookMark) {
      IBookMark mark = (IBookMark) entity;
      BookMarkEvent eventTemplate = (BookMarkEvent) template;
      IFolder oldParent = eventTemplate == null ? null : eventTemplate.getOldParent();
      modelEvent = new BookMarkEvent(mark, oldParent, root);
    } else if (entity instanceof ISearchMark) {
      ISearchMark mark = (ISearchMark) entity;
      SearchMarkEvent eventTemplate = (SearchMarkEvent) template;
      IFolder oldParent = eventTemplate == null ? null : eventTemplate.getOldParent();
      modelEvent = new SearchMarkEvent(mark, oldParent, root);
    } else if (entity instanceof INewsBin) {
      INewsBin newsBin = (INewsBin) entity;
      NewsBinEvent eventTemplate = (NewsBinEvent) template;
      IFolder oldParent = eventTemplate == null ? null : eventTemplate.getOldParent();
      modelEvent = new NewsBinEvent(newsBin, oldParent, root);
    } else if (entity instanceof IFolder) {
      IFolder folder = (IFolder) entity;
      FolderEvent eventTemplate = (FolderEvent) template;
      IFolder oldParent = eventTemplate == null ? null : eventTemplate.getOldParent();
      modelEvent = new FolderEvent(folder, oldParent, root);
    } else if (entity instanceof ILabel) {
      ILabel label = (ILabel) entity;
      LabelEvent eventTemplate = (LabelEvent) template;
      ILabel oldLabel = eventTemplate == null ? null : eventTemplate.getOldLabel();
      modelEvent = new LabelEvent(oldLabel, label, root);
    } else if (entity instanceof ISearchCondition) {
      ISearchCondition searchCond = (ISearchCondition) entity;
      modelEvent = new SearchConditionEvent(searchCond, root);
    } else if (entity instanceof IPreference) {
      IPreference pref = (IPreference) entity;
      modelEvent = new PreferenceEvent(pref);
    } else if (entity instanceof ISearch) {
      ISearch search = (ISearch) entity;
      modelEvent = new SearchEvent(search, root);
    } else if (entity instanceof ISearchFilter) {
      ISearchFilter filter = (ISearchFilter) entity;
      modelEvent = new SearchFilterEvent(filter, root);
    }

    return modelEvent;
  }

  private ModelEvent createNewsEvent(INews news, ModelEvent template, boolean root, boolean merged) {
    ModelEvent modelEvent;
    NewsEvent newsTemplate = (NewsEvent) template;
    INews oldNews = newsTemplate == null ? null : newsTemplate.getOldNews();

    modelEvent = new NewsEvent(oldNews, news, root, merged);
    return modelEvent;
  }

  private boolean isRoot(ModelEvent template) {
    if (template == null)
      return false;

    return template.isRoot();
  }

  private boolean isMerged(ModelEvent template) {
    if (template == null)
      return false;

    return template instanceof NewsEvent && ((NewsEvent)template).isMerged();
  }

  private void setId(IEntity entity) {
    if (entity.getId() == null) {
      long id;

      IDGenerator idGenerator = getIDGenerator();
      if (idGenerator instanceof DB4OIDGenerator)
        id = ((DB4OIDGenerator) idGenerator).getNext(false);
      else
        id = idGenerator.getNext();

      /*
       * We must release the read lock before we can change the id of the news.
       * This should be fine because if the News has no id, it means that it's
       * not known to anyone but the caller and we will acquire the read lock
       * again before issuing any event.
       */
      if (entity instanceof News) {
        News n = (News) entity;
        n.releaseReadLockSpecial();
        try {
          entity.setId(id);
        } finally {
          n.acquireReadLockSpecial();
        }
      } else {
        entity.setId(id);
      }
    }
  }

  void initEntityStoreListener() {
    DBManager.getDefault().addEntityStoreListener(this);
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.DatabaseListener#databaseOpened
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  public void databaseOpened(DatabaseEvent event) {
    fDb = event.getObjectContainer();
    initEventRegistry();
  }

  /*
   * @see
   * org.rssowl.core.internal.persist.service.DatabaseListener#databaseClosed
   * (org.rssowl.core.internal.persist.service.DatabaseEvent)
   */
  public void databaseClosed(DatabaseEvent event) {
    fDb = null;
  }

  public final void addItemBeingDeleted(Object entity) {
    Set<Object> entities = fItemsBeingDeleted.get();
    if (entities == null) {
      entities = new HashSet<Object>(3);
      fItemsBeingDeleted.set(entities);
    }

    entities.add(entity);
  }

  /**
   * Clears any temporary storage used by the EventManager for the thread-bound
   * transaction.
   */
  public void clear() {
    fItemsBeingDeleted.set(null);
  }

  /**
   * @return singleton instance
   */
  public final static EventManager getInstance() {
    return INSTANCE;
  }
}