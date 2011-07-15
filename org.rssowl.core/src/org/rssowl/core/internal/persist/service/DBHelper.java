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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.InternalOwl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.LazyList;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.dao.DAOServiceImpl;
import org.rssowl.core.internal.persist.dao.EntitiesToBeIndexedDAOImpl;
import org.rssowl.core.internal.persist.dao.IDescriptionDAO;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsBin.StatesUpdateInfo;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.INewsBinDAO;
import org.rssowl.core.persist.dao.INewsCounterDAO;
import org.rssowl.core.persist.event.FeedEvent;
import org.rssowl.core.persist.event.ModelEvent;
import org.rssowl.core.persist.event.NewsBinEvent;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.event.runnable.EventRunnable;
import org.rssowl.core.persist.event.runnable.FeedEventRunnable;
import org.rssowl.core.persist.event.runnable.NewsEventRunnable;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;
import org.rssowl.core.persist.service.PersistenceException;
import org.rssowl.core.persist.service.UniqueConstraintException;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.ext.Db4oException;
import com.db4o.query.Query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper for DB related tasks.
 */
public final class DBHelper {
  static final int BUFFER = 32768;

  private DBHelper() {
    super();
  }

  public static void rename(File origin, File destination) throws PersistenceException {

    /* Try atomic rename first. If that fails, rely on delete + rename */
    if (!origin.renameTo(destination)) {
      destination.delete();
      if (!origin.renameTo(destination)) {
        throw new PersistenceException("Failed to rename: " + origin + " to: " + destination); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  public static String readFirstLineFromFile(File file) {
    BufferedReader reader = null;
    try {
      try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); //$NON-NLS-1$
      } catch (UnsupportedEncodingException e) {
        reader = new BufferedReader(new FileReader(file));
      }

      String text = reader.readLine();
      return text;
    } catch (IOException e) {
      throw new PersistenceException(e);
    } finally {
      DBHelper.closeQuietly(reader);
    }
  }

  public static final void copyFileNIO(File originFile, File destinationFile) {
    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;
    try {
      inputStream = new FileInputStream(originFile);
      FileChannel srcChannel = inputStream.getChannel();

      if (!destinationFile.exists())
        destinationFile.createNewFile();

      outputStream = new FileOutputStream(destinationFile);
      FileChannel dstChannel = outputStream.getChannel();

      long bytesToTransfer = srcChannel.size();
      long position = 0;
      while (bytesToTransfer > 0) {
        long bytesTransferred = dstChannel.transferFrom(srcChannel, position, bytesToTransfer);
        position += bytesTransferred;
        bytesToTransfer -= bytesTransferred;
      }
    } catch (IOException e) {
      Activator.getDefault().logError("Failed to copy file using NIO. Falling back to traditional IO", e); //$NON-NLS-1$
      copyFileIO(originFile, destinationFile, new NullProgressMonitor());
    } finally {
      closeQuietly(inputStream);
      closeQuietly(outputStream);
    }
  }

  public static void copyFileIO(File originFile, File destinationFile, IProgressMonitor monitor) {
    FileInputStream inputStream = null;
    FileOutputStream outputStream = null;
    try {
      inputStream = new FileInputStream(originFile);

      if (!destinationFile.exists())
        destinationFile.createNewFile();
      outputStream = new FileOutputStream(destinationFile);

      int i = 0;
      byte[] buf = new byte[BUFFER];
      while ((i = inputStream.read(buf)) != -1 && !monitor.isCanceled()) {
        outputStream.write(buf, 0, i);
        monitor.worked(1);
      }
    } catch (IOException e) {
      throw new PersistenceException(e);
    } finally {
      closeQuietly(inputStream);
      closeQuietly(outputStream);
    }
  }

  public static void writeToFile(File file, String text) {
    BufferedWriter writer = null;
    try {
      try {
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); //$NON-NLS-1$
      } catch (UnsupportedEncodingException e) {
        writer = new BufferedWriter(new FileWriter(file));
      }
      writer.write(text);
      writer.flush();
    } catch (IOException e) {
      throw new PersistenceException(e);
    } finally {
      closeQuietly(writer);
    }
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        Activator.getDefault().logError("Failed to close stream.", e); //$NON-NLS-1$
      }
    }
  }

  public static final List<EventRunnable<?>> cleanUpEvents() {
    List<EventRunnable<?>> eventNotifiers = EventsMap.getInstance().removeEventRunnables();
    EventsMap.getInstance().removeEventTemplatesMap();
    EventManager.getInstance().clear();
    return eventNotifiers;
  }

  public static final void cleanUpAndFireEvents() {
    fireEvents(cleanUpEvents());
  }

  public static final void fireEvents(List<EventRunnable<?>> eventNotifiers) {
    if (eventNotifiers == null) {
      return;
    }

    for (EventRunnable<?> runnable : eventNotifiers) {
      runnable.run();
    }
  }

  public static final PersistenceException rollbackAndPE(ObjectContainer db, Exception e) {
    DBHelper.cleanUpEvents();
    db.rollback();
    return new PersistenceException(e);
  }

  public static final void putEventTemplate(ModelEvent modelEvent) {
    EventsMap.getInstance().putEventTemplate(modelEvent);
  }

  public static final void saveFeed(ObjectContainer db, IFeed feed) {
    if (feed.getId() == null && feedExists(db, feed.getLink()))
      throw new UniqueConstraintException("link", feed); //$NON-NLS-1$

    ModelEvent feedEventTemplate = new FeedEvent(feed, true);
    DBHelper.putEventTemplate(feedEventTemplate);
    saveAndCascadeAllNews(db, feed.getNews(), false);
    saveEntities(db, feed.getCategories());
    saveEntity(db, feed.getAuthor());
    saveEntity(db, feed.getImage());

    db.ext().set(feed, 2);
  }

  private static void saveEntity(ObjectContainer db, IPersistable entity) {
    if (entity != null)
      db.set(entity);
  }

  private static void saveEntities(ObjectContainer db, List<? extends IEntity> entities) {
    for (IEntity entity : entities)
      db.ext().set(entity, 1);
  }

  static void saveAndCascadeAllNews(ObjectContainer db, Collection<INews> newsCollection, boolean root) {
    for (INews news : newsCollection)
      ((News) news).acquireReadLockSpecial();

    try {
      for (INews news : newsCollection)
        saveAndCascadeNews(db, news, root);
    } finally {
      for (INews news : newsCollection) {
        News n = (News) news;
        n.releaseReadLockSpecial();
        n.clearTransientDescription();
      }
    }
  }

  public static final INews peekPersistedNews(ObjectContainer db, INews news) {
    INews oldNews = db.ext().peekPersisted(news, 2, true);
    if (oldNews instanceof News)
      ((News) oldNews).init();

    return oldNews;
  }

  public static final void saveUpdatedNews(ObjectContainer db, INews news) {
    INews oldNews = peekPersistedNews(db, news);
    if (oldNews != null) {
      ModelEvent newsEventTemplate = new NewsEvent(oldNews, news, false, true);
      DBHelper.putEventTemplate(newsEventTemplate);
    }

    db.ext().set(news, 2);
  }

  static final boolean feedExists(ObjectContainer db, URI link) {
    return !getFeeds(db, link).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private static List<Feed> getFeeds(ObjectContainer db, URI link) {
    Query query = db.query();
    query.constrain(Feed.class);
    query.descend("fLinkText").constrain(link.toString()); //$NON-NLS-1$
    List<Feed> set = query.execute();

    return set;
  }

  public static final Feed loadFeed(ObjectContainer db, URI link, Integer activationDepth) {
    try {
      List<Feed> feeds = getFeeds(db, link);
      if (!feeds.isEmpty()) {
        Feed feed = feeds.iterator().next();
        if (activationDepth != null)
          db.ext().activate(feed, activationDepth.intValue());

        return feed;
      }

      return null;
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }

  public static final boolean existsFeed(ObjectContainer db, URI link) {
    try {
      List<Feed> feeds = getFeeds(db, link);
      return !feeds.isEmpty();
    } catch (Db4oException e) {
      throw new PersistenceException(e);
    }
  }

  public static final void saveAndCascadeNews(ObjectContainer db, INews news, boolean root) {
    INews oldNews = peekPersistedNews(db, news);
    if (oldNews != null || root) {
      ModelEvent event = new NewsEvent(oldNews, news, root);
      putEventTemplate(event);
    }

    saveEntities(db, news.getCategories());
    saveEntity(db, news.getAuthor());
    saveEntities(db, news.getAttachments());
    saveEntity(db, news.getSource());
    db.ext().set(news, 2);
    saveDescription(db, news);
  }

  private static void saveDescription(ObjectContainer db, INews news) {
    News n = (News) news;

    /*
     * Avoid loading from the db if the description of the news being saved has
     * not been changed.
     */
    if (!n.isTransientDescriptionSet())
      return;

    Description dbDescription = null;
    String dbDescriptionValue = null;

    dbDescription = getDescriptionDAO().load(news.getId());
    if (dbDescription != null)
      dbDescriptionValue = dbDescription.getValue();

    String newsDescriptionValue = n.getTransientDescription();

    /*
     * If the description in the news has been set to null and it's already null
     * in the database, there is nothing to do.
     */
    if (dbDescriptionValue == null && newsDescriptionValue == null)
      return;
    else if (dbDescriptionValue == null && newsDescriptionValue != null)
      db.set(new Description(news, newsDescriptionValue));
    else if (dbDescriptionValue != null && newsDescriptionValue == null)
      db.delete(dbDescription);
    else if (dbDescriptionValue != null && !dbDescriptionValue.equals(newsDescriptionValue)) {
      if (dbDescription != null) {
        dbDescription.setDescription(newsDescriptionValue);
        db.set(dbDescription);
      }
    }
  }

  public static IDescriptionDAO getDescriptionDAO() {
    DAOService daoService = InternalOwl.getDefault().getPersistenceService().getDAOService();
    if (daoService instanceof DAOServiceImpl)
      return ((DAOServiceImpl) daoService).getDescriptionDAO();

    throw new IllegalStateException("This method should only be called if DAOService is of type " + DAOServiceImpl.class + ", but it is of type: " + daoService.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static void preCommit(ObjectContainer db) {
    updateNewsCounter(db);
    updateNewsToBeIndexed(db);
    updateNewsBins(db);
  }

  public static EntitiesToBeIndexedDAOImpl getEntitiesToBeIndexedDAO() {
    DAOService service = InternalOwl.getDefault().getPersistenceService().getDAOService();
    if (service instanceof DAOServiceImpl) {
      EntitiesToBeIndexedDAOImpl entitiesToBeIndexedDAO = ((DAOServiceImpl) service).getEntitiesToBeIndexedDAO();
      return entitiesToBeIndexedDAO;
    }

    return null;
  }

  private static void updateNewsToBeIndexed(ObjectContainer db) {
    NewsEventRunnable newsEventRunnables = getNewsEventRunnables(EventsMap.getInstance().getEventRunnables());
    if (newsEventRunnables == null)
      return;

    EntitiesToBeIndexedDAOImpl dao = getEntitiesToBeIndexedDAO();
    EntityIdsByEventType newsToBeIndexed = dao.load();
    Set<NewsEvent> updateEvents = new HashSet<NewsEvent>(newsEventRunnables.getUpdateEvents().size());
    Set<NewsEvent> deleteEvents = new HashSet<NewsEvent>(newsEventRunnables.getRemoveEvents());
    Set<NewsEvent> persistEvents = filterPersistedNewsForIndexing(newsEventRunnables.getPersistEvents());
    for (NewsEvent event : newsEventRunnables.getUpdateEvents())
      indexTypeForNewsUpdate(event, persistEvents, updateEvents, deleteEvents);

    NewsEventRunnable copy = new NewsEventRunnable();
    for (NewsEvent persistEvent : persistEvents)
      copy.addCheckedPersistEvent(persistEvent);
    for (NewsEvent updateEvent : updateEvents)
      copy.addCheckedUpdateEvent(updateEvent);
    for (NewsEvent deleteEvent : deleteEvents)
      copy.addCheckedRemoveEvent(deleteEvent);

    newsToBeIndexed.addAllEntities(copy.getPersistEvents(), copy.getUpdateEvents(), copy.getRemoveEvents());
    newsToBeIndexed.compact();
    db.ext().set(newsToBeIndexed, Integer.MAX_VALUE);
  }

  public static Set<NewsEvent> filterPersistedNewsForIndexing(Collection<NewsEvent> events) {
    Set<NewsEvent> result = new HashSet<NewsEvent>(events.size());
    for (NewsEvent event : events)
      if (event.getEntity().isVisible())
        result.add(event);

    return result;
  }

  public static void indexTypeForNewsUpdate(NewsEvent event, Collection<NewsEvent> newsToRestore, Collection<NewsEvent> newsToUpdate, Collection<NewsEvent> newsToDelete) {
    boolean wasVisible = event.getOldNews().isVisible();
    boolean isVisible = event.getEntity().isVisible();

    /* News got Deleted/Hidden */
    if (wasVisible && !isVisible)
      newsToDelete.add(event);

    /* News got Restored */
    else if (!wasVisible && isVisible)
      newsToRestore.add(event);

    /* Normal Update */
    else if (wasVisible && isVisible)
      newsToUpdate.add(event);
  }

  public static NewsEventRunnable getNewsEventRunnables(List<EventRunnable<?>> eventRunnables) {
    for (EventRunnable<?> eventRunnable : eventRunnables) {
      if (eventRunnable instanceof NewsEventRunnable)
        return (NewsEventRunnable) eventRunnable;
    }

    return null;
  }

  private static void updateNewsBins(ObjectContainer db) {
    NewsEventRunnable newsEventRunnable = getNewsEventRunnables(EventsMap.getInstance().getEventRunnables());
    if (newsEventRunnable == null)
      return;

    Map<Long, List<StatesUpdateInfo>> statesUpdateInfos = new HashMap<Long, List<StatesUpdateInfo>>(5);
    for (NewsEvent newsEvent : newsEventRunnable.getUpdateEvents()) {
      INews news = newsEvent.getEntity();
      if (news.getParentId() != 0 && (newsEvent.getOldNews().getState() != news.getState())) {
        List<StatesUpdateInfo> list = statesUpdateInfos.get(news.getParentId());
        if (list == null) {
          list = new ArrayList<StatesUpdateInfo>();
          statesUpdateInfos.put(news.getParentId(), list);
        }
        list.add(new StatesUpdateInfo(newsEvent.getOldNews().getState(), news.getState(), news.toReference()));
      }
    }

    for (NewsEvent newsEvent : newsEventRunnable.getPersistEvents()) {
      INews news = newsEvent.getEntity();
      if (news.getParentId() != 0) {
        List<StatesUpdateInfo> list = statesUpdateInfos.get(news.getParentId());
        if (list == null) {
          list = new ArrayList<StatesUpdateInfo>();
          statesUpdateInfos.put(news.getParentId(), list);
        }
        list.add(new StatesUpdateInfo(null, news.getState(), news.toReference()));
      }
    }

    if (!statesUpdateInfos.isEmpty()) {
      Set<FeedLinkReference> removedFeedRefs = new HashSet<FeedLinkReference>();
      INewsBinDAO newsBinDAO = DynamicDAO.getDAO(INewsBinDAO.class);
      for (Map.Entry<Long, List<StatesUpdateInfo>> mapEntry : statesUpdateInfos.entrySet()) {
        INewsBin newsBin = newsBinDAO.load(mapEntry.getKey());
        if (newsBin.updateNewsStates(mapEntry.getValue())) {
          removeNews(db, removedFeedRefs, newsBin.removeNews(EnumSet.of(INews.State.DELETED)));
          putEventTemplate(new NewsBinEvent(newsBin, null, true));
          db.ext().set(newsBin, Integer.MAX_VALUE);
        }
      }
      removeFeedsAfterNewsBinUpdate(db, removedFeedRefs);
    }
  }

  static void removeNews(ObjectContainer db, Set<FeedLinkReference> feedRefs, Collection<NewsReference> newsRefs) {
    for (NewsReference newsRef : newsRefs) {
      INews news = newsRef.resolve();
      if (news != null) {
        feedRefs.add(news.getFeedReference());
        db.delete(news);
      }
    }
  }

  static void removeFeedsAfterNewsBinUpdate(ObjectContainer db, Set<FeedLinkReference> removedFeedRefs) {
    NewsCounter newsCounter = DynamicDAO.getDAO(INewsCounterDAO.class).load();
    boolean changed = false;
    for (FeedLinkReference feedRef : removedFeedRefs) {
      if ((countBookMarkReference(db, feedRef) == 0) && !feedHasNewsWithCopies(db, feedRef)) {
        db.delete(feedRef.resolve());
        changed = true;
      }
    }
    if (changed)
      db.ext().set(newsCounter, Integer.MAX_VALUE);
  }

  static int countBookMarkReference(ObjectContainer db, FeedLinkReference feedRef) {
    Collection<IBookMark> marks = loadAllBookMarks(db, feedRef);
    return marks.size();
  }

  @SuppressWarnings("unchecked")
  public static Collection<IBookMark> loadAllBookMarks(ObjectContainer db, FeedLinkReference feedRef) {
    Query query = db.query();
    query.constrain(BookMark.class);
    query.descend("fFeedLink").constrain(feedRef.getLink().toString()); //$NON-NLS-1$
    return query.execute();
  }

  static boolean feedHasNewsWithCopies(ObjectContainer db, FeedLinkReference feedRef) {
    Query query = db.query();
    query.constrain(News.class);
    query.descend("fFeedLink").constrain(feedRef.getLink().toString()); //$NON-NLS-1$
    query.descend("fParentId").constrain(0).not(); //$NON-NLS-1$
    return !query.execute().isEmpty();
  }

  public static void updateNewsCounter(ObjectContainer db) {
    List<EventRunnable<?>> eventRunnables = EventsMap.getInstance().getEventRunnables();
    NewsCounterService newsCounterService = new NewsCounterService(Owl.getPersistenceService().getDAOService().getNewsCounterDAO(), db);
    NewsEventRunnable newsEventRunnable = getNewsEventRunnables(eventRunnables);
    if (newsEventRunnable != null) {
      newsCounterService.onNewsAdded(newsEventRunnable.getPersistEvents());
      newsCounterService.onNewsRemoved((newsEventRunnable.getRemoveEvents()));
      newsCounterService.onNewsUpdated(newsEventRunnable.getUpdateEvents());
    }
    for (EventRunnable<?> eventRunnable : eventRunnables) {
      if (eventRunnable instanceof FeedEventRunnable) {
        FeedEventRunnable feedEventRunnable = (FeedEventRunnable) eventRunnable;
        newsCounterService.onFeedRemoved(feedEventRunnable.getRemoveEvents());
        break;
      }
    }
  }

  public static Collection<IFeed> loadAllFeeds(ObjectContainer db) {
    ObjectSet<? extends IFeed> entities = db.query(Feed.class);
    return new LazyList<IFeed>(entities, db);
  }
}