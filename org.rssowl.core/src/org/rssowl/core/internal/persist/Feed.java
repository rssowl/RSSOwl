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

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.ICloud;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ITextInput;
import org.rssowl.core.persist.reference.FeedReference;
import org.rssowl.core.util.ArrayUtils;
import org.rssowl.core.util.MergeUtils;
import org.rssowl.core.util.SyncUtils;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Feed is the container of <code>News</code>. There can only be one Feed
 * that matches a certain Link. A Feed is created whenever a
 * <code>BookMark</code> was created on a Link that is not yet present as a
 * Feed in the database.
 *
 * @author bpasero
 */
public class Feed extends AbstractEntity implements IFeed {
  private String fTitle;
  private Date fPublishDate;
  private String fDescription;
  private String fHomepage;
  private String fLanguage;
  private String fCopyright;
  private String fDocs;
  private String fGenerator;
  private Date fLastBuildDate;
  private String fWebmaster;
  private Date fLastModifiedDate;
  private int fTTL;
  private String fFormat;
  private String fBaseUri;
  private transient URI fLink;
  private String fLinkText;
  private IImage fImage;
  private IPerson fAuthor;
  private List<INews> fNews;
  private List<ICategory> fCategories;

  /**
   * Creates a new Element of the type Feed. A Feed is visually represented in
   * the GUI by a relation of one to many to Elements of the type
   * <code>BookMark</code>. The id property of the created object will be
   * IExtendableType.NO_ID.
   *
   * @param link The unique Link of this Feed pointing to the location to
   * retrieve <code>News</code>
   */
  public Feed(URI link) {
    this(null, link);
  }

  /**
   * Creates a new Element of the type Feed. A Feed is visually represented in
   * the GUI by a relation of one to many to Elements of the type
   * <code>BookMark</code>
   *
   * @param id The unique id of this Feed.
   * @param link The unique Link of this Feed pointing to the location to
   * retrieve <code>News</code>
   */
  public Feed(Long id, URI link) {
    super(id);
    Assert.isNotNull(link, "The type Feed requires a Link that is not NULL"); //$NON-NLS-1$
    fLink = link;
    fLinkText = link.toString();
    fNews = new ArrayList<INews>();
  }

  /**
   * Default constructor for deserialization
   */
  protected Feed() {
  // As per javadoc
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#addNews(org.rssowl.core.model.types.INews)
   */
  public synchronized void addNews(INews news) {
    Assert.isNotNull(news, "Exception adding NULL as News into Feed"); //$NON-NLS-1$

    /* Rule: Child needs to know about its new parent already! */
    Assert.isTrue(fLinkText.equals(news.getFeedLinkAsText()), "The News has a different Feed set!"); //$NON-NLS-1$

    fNews.add(news);
  }

  private int findNews(List<INews> newsList, INews news) {
    for (int i = 0, c = newsList.size(); i < c; ++i) {
      if (news.isEquivalent(newsList.get(i)))
        return i;
    }

    return -1;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getNews()
   */
  public synchronized List<INews> getNews() {
    return new ArrayList<INews>(fNews);
  }

  /**
   * @return the number of sticky news in this feed.
   */
  public synchronized int getStickyCount() {
    int count = 0;
    for (INews news : fNews) {
      if (news.isFlagged())
        ++count;
    }
    return count;
  }

  /**
   * @return a map of states pointing to the news count with this state.
   */
  public synchronized Map<State, Integer> getNewsCount() {
    int[] counts = new int[State.values().length];
    for (INews news : fNews) {
      ++counts[news.getState().ordinal()];
    }

    Map<State, Integer> stateToCountMap = new EnumMap<State, Integer>(State.class);
    int ordinal = 0;
    for (int count : counts) {
      stateToCountMap.put(State.getState(ordinal), count);
      ++ordinal;
    }

    return stateToCountMap;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getLink()
   */
  public synchronized URI getLink() {
    if (fLink == null && fLinkText != null)
      fLink = createURI(fLinkText);

    return fLink;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getCopyright()
   */
  public synchronized String getCopyright() {
    return fCopyright;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setCopyright(java.lang.String)
   */
  public synchronized void setCopyright(String copyright) {
    fCopyright = copyright;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getDescription()
   */
  public synchronized String getDescription() {
    return fDescription;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setDescription(java.lang.String)
   */
  public synchronized void setDescription(String description) {
    fDescription = description;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getDocs()
   */
  public synchronized URI getDocs() {
    return createURI(fDocs);
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setDocs(java.lang.String)
   */
  public synchronized void setDocs(URI docs) {
    fDocs = getURIText(docs);
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getFormat()
   */
  public synchronized String getFormat() {
    return fFormat;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setFormat(java.lang.String)
   */
  public synchronized void setFormat(String format) {
    fFormat = format;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getGenerator()
   */
  public synchronized String getGenerator() {
    return fGenerator;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setGenerator(java.lang.String)
   */
  public synchronized void setGenerator(String generator) {
    fGenerator = generator;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getHomepage()
   */
  public synchronized URI getHomepage() {
    return createURI(fHomepage);
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setHomepage(java.lang.String)
   */
  public synchronized void setHomepage(URI homepage) {
    fHomepage = getURIText(homepage);
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getLanguage()
   */
  public synchronized String getLanguage() {
    return fLanguage;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setLanguage(java.lang.String)
   */
  public synchronized void setLanguage(String language) {
    fLanguage = language;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getLastBuildDate()
   */
  public synchronized Date getLastBuildDate() {
    return fLastBuildDate;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setLastBuildDate(java.util.Date)
   */
  public synchronized void setLastBuildDate(Date lastBuildDate) {
    fLastBuildDate = lastBuildDate;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getAuthor()
   */
  public synchronized IPerson getAuthor() {
    return fAuthor;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setAuthor(org.rssowl.core.model.types.IPerson)
   */
  public synchronized void setAuthor(IPerson author) {
    fAuthor = author;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getPublishDate()
   */
  public synchronized Date getPublishDate() {
    return fPublishDate;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setPublishDate(java.util.Date)
   */
  public synchronized void setPublishDate(Date publishDate) {
    fPublishDate = publishDate;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getTitle()
   */
  public synchronized String getTitle() {
    return fTitle;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setTitle(java.lang.String)
   */
  public synchronized void setTitle(String title) {
    fTitle = title;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getVisibleNews()
   */
  public synchronized List<INews> getVisibleNews() {
    return getNewsByStates(INews.State.getVisible());
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getNewsByStates(java.util.Set)
   */
  public synchronized List<INews> getNewsByStates(Set<INews.State> states) {
    List<INews> newsList = new ArrayList<INews>();
    for (INews news : fNews) {
      if (states.contains(news.getState()))
          newsList.add(news);
    }
    return newsList;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getTTL()
   */
  public synchronized int getTTL() {
    return fTTL;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setTTL(int)
   */
  public synchronized void setTTL(int ttl) {
    fTTL = ttl;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getWebmaster()
   */
  public synchronized String getWebmaster() {
    return fWebmaster;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setWebmaster(java.lang.String)
   */
  public synchronized void setWebmaster(String webmaster) {
    fWebmaster = webmaster;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setImage(org.rssowl.core.model.types.IImage)
   */
  public synchronized void setImage(IImage image) {
    fImage = image;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#addCategory(org.rssowl.core.model.types.ICategory)
   */
  public synchronized void addCategory(ICategory category) {
    if (fCategories == null)
      fCategories = new ArrayList<ICategory>();

    fCategories.add(category);
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setLastModifiedDate(java.util.Date)
   */
  public synchronized void setLastModifiedDate(Date lastModifiedDate) {
    fLastModifiedDate = lastModifiedDate;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getLastModificationDate()
   */
  public synchronized Date getLastModifiedDate() {
    return fLastModifiedDate;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#setBase(java.net.URI)
   */
  public synchronized void setBase(URI baseUri) {
    fBaseUri = getURIText(baseUri);
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getBase()
   */
  public synchronized URI getBase() {
    return createURI(fBaseUri);
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param feed to be compared.
   * @return whether this object and <code>feed</code> are identical. It
   * compares all the fields.
   */
  public synchronized boolean isIdentical(IFeed feed) {
    if (this == feed)
      return true;

    if (!(feed instanceof Feed))
      return false;

    synchronized (feed) {
      Feed f = (Feed) feed;

      return (getId() == null ? f.getId() == null : getId().equals(f.getId()))
          && (fAuthor == null ? f.fAuthor == null : fAuthor.equals(f.fAuthor))
          && (fCategories == null ? f.fCategories == null : fCategories.equals(f.fCategories))
          && (fCopyright == null ? f.fCopyright == null : fCopyright.equals(f.fCopyright))
          && (fDescription == null ? f.fDescription == null : fDescription.equals(f.fDescription))
          && (getDocs() == null ? f.getDocs() == null : getDocs().equals(f.getDocs()))
          && (fFormat == null ? f.fFormat == null : fFormat.equals(f.fFormat))
          && (fGenerator == null ? f.fGenerator == null : fGenerator.equals(f.fGenerator))
          && (getHomepage() == null ? f.getHomepage() == null : getHomepage().equals(f.getHomepage()))
          && (getBase() == null ? f.getBase() == null : getBase().equals(f.getBase()))
          && (fImage == null ? f.fImage == null : fImage.equals(f.fImage))
          && (fLanguage == null ? f.fLanguage == null : fLanguage.equals(f.fLanguage))
          && (fLastBuildDate == null ? f.fLastBuildDate == null : fLastBuildDate.equals(f.fLastBuildDate))
          && (fLastModifiedDate == null ? f.fLastModifiedDate == null : fLastModifiedDate.equals(f.fLastModifiedDate))
          && (getLink() == null ? f.getLink() == null : getLink().toString().equals(f.getLink() != null ? f.getLink().toString() : null))
          && (fNews == null ? f.fNews == null : new HashSet<INews>(fNews).equals(new HashSet<INews>(f.fNews)))
          && (fPublishDate == null ? f.fPublishDate == null : fPublishDate.equals(f.fPublishDate))
          && (fTitle == null ? f.fTitle == null : fTitle.equals(f.fTitle))
          && fTTL == f.fTTL
          && (fWebmaster == null ? f.fWebmaster == null : fWebmaster.equals(f.fWebmaster))
          && (getProperties() == null ? f.getProperties() == null : getProperties().equals(f.getProperties()));
    }
  }

  /**
   * This type is not handled by the Application! Use Properties instead.
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void setRating(String rating) {}

  /**
   * This type is not handled by the Application! Use Properties instead.
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void setTextInput(ITextInput input) {}

  /**
   * This type is not handled by the Application! Use Properties instead.
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void addHourToSkip(int hour) {}

  /**
   * This type is not handled by the Application! Use Properties instead.
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void addDayToSkip(int day) {}

  /**
   * This type is not handled by the Application! Use Properties instead.
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void setCloud(ICloud cloud) {}

  /**
   * This type is not handled by the Application! Use Properties instead.
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public ICloud getCloud() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getImage()
   */
  public synchronized IImage getImage() {
    return fImage;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public String getRating() {
    return null;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public int[] getDaysToSkip() {
    return null;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public int[] getHoursToSkip() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.IFeed#getCategories()
   */
  public synchronized List<ICategory> getCategories() {
    if (fCategories == null)
      return new ArrayList<ICategory>(0);

    return new ArrayList<ICategory>(fCategories);
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public ITextInput getTextInput() {
    return null;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void setUpdateBase(Date updateBase) {}

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public Date getUpdateBase() {
    return null;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public int getUpdateFrequency() {
    return 0;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void setUpdateFrequency(int updateFrequency) {}

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public int getUpdatePeriod() {
    return 0;
  }

  /**
   * This type is not handled by the Application! Use Properties instead
   * <p>
   * The Methods <code>setProperty()</code> and <code>getProperty()</code>
   * allows to store and retrieve extensible Data that is not part of this Types
   * model.
   * </p>
   */
  public void setUpdatePeriod(int updatePeriod) {}

  /*
   * @see org.rssowl.core.persist.IFeed#mergeAndCleanUp(org.rssowl.core.persist.IFeed)
   */
  public synchronized MergeResult mergeAndCleanUp(IFeed objectToMerge) {
    Assert.isNotNull(objectToMerge);
    Assert.isLegal(this != objectToMerge, "Trying to merge the same feed. This is most likely a mistake: " + objectToMerge); //$NON-NLS-1$
    synchronized (objectToMerge) {
      return merge(objectToMerge, true);
    }
  }

  private MergeResult merge(IFeed objectToMerge, boolean cleanUp) {
    Assert.isLegal(getLink().toString().equals(objectToMerge.getLink().toString()),
        "Only feeds with the same link can be merged."); //$NON-NLS-1$

    MergeResult result = new MergeResult();
    boolean updated = processListMergeResult(result, mergeNews(objectToMerge.getNews(), cleanUp));
    updated |= processListMergeResult(result, mergeCategories(objectToMerge.getCategories()));
    updated |= processListMergeResult(result, mergeAuthor(objectToMerge.getAuthor()));
    updated |= processListMergeResult(result, mergeImage(objectToMerge.getImage()));
    updated |= !simpleFieldsEqual(objectToMerge);

    setBase(objectToMerge.getBase());
    fCopyright = objectToMerge.getCopyright();
    fDescription = objectToMerge.getDescription();
    setDocs(objectToMerge.getDocs());
    fFormat = objectToMerge.getFormat();
    fGenerator = objectToMerge.getGenerator();
    setHomepage(objectToMerge.getHomepage());
    fLanguage = objectToMerge.getLanguage();
    fLastBuildDate = objectToMerge.getLastBuildDate();
    fLastModifiedDate = objectToMerge.getLastModifiedDate();
    fPublishDate = objectToMerge.getPublishDate();
    fTitle = objectToMerge.getTitle();
    fTTL = objectToMerge.getTTL();
    fWebmaster = objectToMerge.getWebmaster();

    ComplexMergeResult<?> propertiesResult = MergeUtils.mergeProperties(this, objectToMerge);
    if (updated || propertiesResult.isStructuralChange()) {
      result.addUpdatedObject(this);
      result.addAll(propertiesResult);
    }
    return result;
  }

  private boolean simpleFieldsEqual(IFeed feed) {
    return MergeUtils.equals(getBase(), feed.getBase()) &&
        MergeUtils.equals(fCopyright, feed.getCopyright()) &&
        MergeUtils.equals(fDescription, feed.getDescription()) &&
        MergeUtils.equals(getDocs(), feed.getDocs()) &&
        MergeUtils.equals(fFormat, feed.getFormat()) &&
        MergeUtils.equals(fGenerator, feed.getGenerator()) &&
        MergeUtils.equals(getHomepage(), feed.getHomepage()) &&
        MergeUtils.equals(fLanguage, feed.getLanguage()) &&
        MergeUtils.equals(fLastBuildDate, feed.getLastBuildDate()) &&
        MergeUtils.equals(fLastModifiedDate, feed.getLastModifiedDate()) &&
        MergeUtils.equals(fPublishDate, feed.getPublishDate()) &&
        MergeUtils.equals(fTitle, feed.getTitle()) &&
        MergeUtils.equals(fTTL, feed.getTTL()) &&
        MergeUtils.equals(fWebmaster, feed.getWebmaster());
  }

  /*
   * @see org.rssowl.core.persist.MergeCapable#merge(java.lang.Object)
   */
  public synchronized MergeResult merge(IFeed objectToMerge) {
    Assert.isNotNull(objectToMerge);
    synchronized (objectToMerge) {
      return merge(objectToMerge, false);
    }
  }

  private ComplexMergeResult<IPerson> mergeAuthor(IPerson author) {
    ComplexMergeResult<IPerson> mergeResult = MergeUtils.merge(getAuthor(), author);
    fAuthor = mergeResult.getMergedObject();
    return mergeResult;
  }

  private ComplexMergeResult<List<ICategory>> mergeCategories(List<ICategory> categories) {
    Comparator<ICategory> comparator = new Comparator<ICategory>() {
      public int compare(ICategory o1, ICategory o2) {
        if (o1.getName() == null ? o2.getName() == null : o1.getName().equals(o2.getName())) {
          return 0;
        }

        return -1;
      }

    };

    ComplexMergeResult<List<ICategory>> mergeResult = MergeUtils.merge(fCategories, categories, comparator, null);
    fCategories = mergeResult.getMergedObject();
    return mergeResult;
  }

  private ComplexMergeResult<IImage> mergeImage(IImage image) {
    ComplexMergeResult<IImage> mergeResult = MergeUtils.merge(getImage(), image);
    fImage = mergeResult.getMergedObject();
    return mergeResult;
  }

  private ComplexMergeResult<List<INews>> mergeNews(List<INews> newsList, boolean cleanUp) {
    List<INews> newsListCopy = copyWithoutDuplicates(newsList);
    int[] newsToCleanUp = null;
    int newsToCleanUpSize = 0;
    if (cleanUp)
      newsToCleanUp = new int[fNews.size() / 2];

    ComplexMergeResult<List<INews>> mergeResult = ComplexMergeResult.create(newsListCopy);

    /* Synchronized Feed (speed up by relying on GUID) */
    if (SyncUtils.isSynchronized(fLinkText)) {

      /* Map unique GUID to Pair of Index/News */
      Map<String, INews> mapGuidToIncomingNews = new HashMap<String, INews>(newsListCopy.size());
      for (int i = 0; i < newsListCopy.size(); i++) {
        INews news = newsListCopy.get(i);
        if (news.getGuid() != null)
          mapGuidToIncomingNews.put(news.getGuid().getValue(), news);
      }

      for (int i = fNews.size() - 1; i >= 0; --i) {
        INews existingNews = fNews.get(i);
        INews incomingNews = existingNews.getGuid() != null ? mapGuidToIncomingNews.get(existingNews.getGuid().getValue()) : null;

        /* News exists in feed: Merge it */
        if (incomingNews != null) {
          mergeResult.addAll(existingNews.merge(incomingNews));
          newsListCopy.remove(incomingNews);
        }

        /* News does not exist in feed: Delete it */
        else if (newsToCleanUp != null && existingNews.getState() == INews.State.DELETED) {
          newsToCleanUp = ArrayUtils.ensureCapacity(newsToCleanUp, newsToCleanUpSize + 1);
          newsToCleanUp[newsToCleanUpSize++] = i;
        }
      }
    }

    /* Non Synchronized Feed */
    else {
      for (int i = fNews.size() - 1; i >= 0; --i) {
        INews existingNews = fNews.get(i);
        int existingNewsIndex = findNews(newsListCopy, existingNews);

        /* News exists in feed: Merge it */
        if (existingNewsIndex > -1) {
          mergeResult.addAll(existingNews.merge(newsListCopy.get(existingNewsIndex)));
          newsListCopy.remove(existingNewsIndex);
        }

        /* News does not exist in feed: Delete it */
        else if ((newsToCleanUp != null) && (existingNews.getState() == INews.State.DELETED)) {
          newsToCleanUp = ArrayUtils.ensureCapacity(newsToCleanUp, newsToCleanUpSize + 1);
          newsToCleanUp[newsToCleanUpSize++] = i;
        }
      }
    }

    /* Delete News as necessary */
    if (newsToCleanUp != null && newsToCleanUp.length > 0) {
      int[] tempNewsToCleanUp = new int[newsToCleanUpSize];
      System.arraycopy(newsToCleanUp, 0, tempNewsToCleanUp, 0, newsToCleanUpSize);
      newsToCleanUp = tempNewsToCleanUp;
      mergeResult.setStructuralChange(true);

      /*
       * These numbers were found through experimentation. It's possible that a
       * better way to decide when to run this exists.
       */
      if (newsToCleanUpSize > 20 || (newsToCleanUpSize > 5 && (fNews.size() / newsToCleanUpSize < 5))) {
        ArrayUtils.reverse(newsToCleanUp, newsToCleanUpSize);
        for (int i = 0, c = fNews.size(), newIndex = 0; i < c; ++i) {
          if (Arrays.binarySearch(newsToCleanUp, i) < 0) {
            fNews.set(newIndex, fNews.get(i));
            ++newIndex;
          } else {
            mergeResult.addRemovedObject(fNews.get(i));
          }
        }
        int newSize = fNews.size() - newsToCleanUpSize;
        for (int i = fNews.size() - 1; i >= newSize; --i) {
          fNews.remove(i);
        }
      }
      else {
        /*
         * Indices are stored in decreasing order in newsToCleanUp so we iterate
         * in increasing order.
         */
        for (int i = 0, c = newsToCleanUpSize; i < c; ++i) {
          INews news = fNews.remove(newsToCleanUp[i]);
          mergeResult.addRemovedObject(news);
        }
      }
    }

    for (INews news : newsListCopy) {
      news.setParent(this);
      fNews.add(news);
      mergeResult.setStructuralChange(true);
      mergeResult.addUpdatedObject(news);
    }

    return mergeResult;
  }

  private List<INews> copyWithoutDuplicates(List<INews> newsList) {

    /* Perform fast lookup for synchronized feeds using GUID */
    if (SyncUtils.isSynchronized(fLinkText))
      return copyWithoutDuplicatesSynced(newsList);

    /* Otherwise search rawly */
    List<INews> newsListCopy = new ArrayList<INews>(newsList.size());
    for (INews outerNews : newsList) {
      boolean containsNews = false;
      for (INews innerNews : newsListCopy) {
        if (innerNews.isEquivalent(outerNews)) {
          containsNews = true;
          break;
        }
      }

      if (!containsNews)
        newsListCopy.add(outerNews);
    }

    return newsListCopy;
  }

  private List<INews> copyWithoutDuplicatesSynced(List<INews> newsList) {
    Set<String> guids= new HashSet<String>(newsList.size());
    List<INews> newsListCopy = new ArrayList<INews>(newsList.size());
    for (INews news : newsList) {
      IGuid guid = news.getGuid();
      if (guid == null)
        continue; //Can not happen for a synchronized feed

      if (!guids.contains(guid.getValue())) {
        guids.add(guid.getValue());
        newsListCopy.add(news);
      }
    }

    return newsListCopy;
  }

  /*
   * @see org.rssowl.core.persist.IFeed#removeNews(org.rssowl.core.persist.INews)
   */
  public synchronized boolean removeNews(INews news) {
    return fNews.remove(news);
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  public FeedReference toReference() {
    return new FeedReference(getIdAsPrimitive());
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#toString()
   */
  @Override
  public synchronized String toString() {
    StringBuilder str = new StringBuilder();

    str.append("\n\n\n****************************** Feed ******************************"); //$NON-NLS-1$
    str.append("\nID: ").append(getId()); //$NON-NLS-1$
    str.append("\nLink: ").append(getLink()); //$NON-NLS-1$
    if (getTitle() != null)
      str.append("\nTitle: ").append(getTitle()); //$NON-NLS-1$

    return str.toString();
  }

  /**
   * Returns a String describing the state of this Entity.
   *
   * @return A String describing the state of this Entity.
   */
  public synchronized String toLongString() {
    StringBuilder str = new StringBuilder();

    str.append("\n\n\n****************************** Feed ******************************"); //$NON-NLS-1$
    str.append("\nID: ").append(getId()); //$NON-NLS-1$
    str.append("\nFormat: ").append(getFormat()); //$NON-NLS-1$
    str.append("\nLink: ").append(getLink()); //$NON-NLS-1$
    if (getBase() != null)
      str.append("\nBase URI: ").append(getBase()); //$NON-NLS-1$
    if (getTitle() != null)
      str.append("\nTitle: ").append(getTitle()); //$NON-NLS-1$
    if (getDescription() != null)
      str.append("\nDescription: ").append(getDescription()); //$NON-NLS-1$
    if (getHomepage() != null)
      str.append("\nHomepage: ").append(getHomepage()); //$NON-NLS-1$
    if (getLanguage() != null)
      str.append("\nLanguage: ").append(getLanguage()); //$NON-NLS-1$
    if (getImage() != null)
      str.append("\nImage: ").append(getImage()); //$NON-NLS-1$
    if (getCopyright() != null)
      str.append("\nCopyright: ").append(getCopyright()); //$NON-NLS-1$
    if (getPublishDate() != null)
      str.append("\nPublish Date: ").append(DateFormat.getDateTimeInstance().format(getPublishDate())); //$NON-NLS-1$
    if (getLastBuildDate() != null)
      str.append("\nLast Build: ").append(DateFormat.getDateTimeInstance().format(getLastBuildDate())); //$NON-NLS-1$
    if (getLastModifiedDate() != null)
      str.append("\nLast Modified: ").append(DateFormat.getDateTimeInstance().format(getLastModifiedDate())); //$NON-NLS-1$
    str.append("\nCategories: ").append(getCategories()); //$NON-NLS-1$
    if (getGenerator() != null)
      str.append("\nGenerator: ").append(getGenerator()); //$NON-NLS-1$
    if (getTTL() != 0)
      str.append("\nTTL: ").append(getTTL()); //$NON-NLS-1$
    if (getDocs() != null)
      str.append("\nDocs: ").append(getDocs()); //$NON-NLS-1$
    if (getAuthor() != null)
      str.append("\nAuthor: ").append(getAuthor()); //$NON-NLS-1$
    if (getWebmaster() != null)
      str.append("\nWebmaster: ").append(getWebmaster()); //$NON-NLS-1$
    str.append("\nProperties: ").append(getProperties()); //$NON-NLS-1$
    str.append("\n\nNews: ").append(getNews()); //$NON-NLS-1$

    str.append("\n******************\n"); //$NON-NLS-1$

    return str.toString();
  }
}