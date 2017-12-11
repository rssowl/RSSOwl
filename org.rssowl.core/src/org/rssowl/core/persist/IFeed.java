/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2009 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse  License v1.0 which accompanies this    **
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

package org.rssowl.core.persist;

import org.rssowl.core.internal.persist.MergeResult;
import org.rssowl.core.persist.reference.FeedReference;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The super-type of all Feed Elements.
 *
 * @author bpasero
 */
public interface IFeed extends IEntity, MergeCapable<IFeed> {

  /** One of the fields in this type described as constant */
  public static final int LINK = 0;

  /** One of the fields in this type described as constant */
  public static final int TITLE = 1;

  /** One of the fields in this type described as constant */
  public static final int PUBLISH_DATE = 2;

  /** One of the fields in this type described as constant */
  public static final int DESCRIPTION = 3;

  /** One of the fields in this type described as constant */
  public static final int HOMEPAGE = 4;

  /** One of the fields in this type described as constant */
  public static final int LANGUAGE = 5;

  /** One of the fields in this type described as constant */
  public static final int COPYRIGHT = 6;

  /** One of the fields in this type described as constant */
  public static final int DOCS = 7;

  /** One of the fields in this type described as constant */
  public static final int GENERATOR = 8;

  /** One of the fields in this type described as constant */
  public static final int LAST_BUILD_DATE = 9;

  /** One of the fields in this type described as constant */
  public static final int WEBMASTER = 10;

  /** One of the fields in this type described as constant */
  public static final int LAST_MODIFIED_DATE = 11;

  /** One of the fields in this type described as constant */
  public static final int TTL = 12;

  /** One of the fields in this type described as constant */
  public static final int FORMAT = 13;

  /** One of the fields in this type described as constant */
  public static final int IMAGE = 14;

  /** One of the fields in this type described as constant */
  public static final int AUTHOR = 15;

  /** One of the fields in this type described as constant */
  public static final int NEWS = 16;

  /** One of the fields in this type described as constant */
  public static final int CATEGORIES = 17;

  /** Used by the skipDays Element */
  static final List<String> DAYS = new ArrayList<String>(Arrays.asList(new String[] { "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" })); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

  /** Used by the skipDays Element */
  static final int MONDAY = 0;

  /** Used by the skipDays Element */
  static final int TUESDAY = 1;

  /** Used by the skipDays Element */
  static final int WEDNESDAY = 2;

  /** Used by the skipDays Element */
  static final int THURSDAY = 3;

  /** Used by the skipDays Element */
  static final int FRIDAY = 4;

  /** Used by the skipDays Element */
  static final int SATURDAY = 5;

  /** Used by the skipDays Element */
  static final int SUNDAY = 6;

  /** Used by the updatePeriod Element */
  static final List<String> PERIODS = new ArrayList<String>(Arrays.asList(new String[] { "hourly", "daily", "weekly", "monthly", "yearly" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

  /** Used by the updatePeriod Element */
  static final int HOURLY = 0;

  /** Used by the updatePeriod Element */
  static final int DAILY = 1;

  /** Used by the updatePeriod Element */
  static final int WEEKLY = 2;

  /** Used by the updatePeriod Element */
  static final int MONTHLY = 3;

  /** Used by the updatePeriod Element */
  static final int YEARLY = 4;

  /**
   * Convenience method that returns all the news from the feed that are
   * visible. The news that are visible are the ones whose state matches any of
   * the states returned by {@link INews.State#getVisible()}.
   *
   * @return the visible news in the feed.
   * @see #getNewsByStates(Set)
   * @see INews.State#getVisible()
   */
  List<INews> getVisibleNews();

  /**
   * Returns the list of news from the feed that are in the same state as one of
   * the elements of <code>states</code>.
   *
   * @param states Set containing all the allowable news states in the returned
   * list.
   * @return List containing INews that match any of the given states.
   * @see INews#getState()
   * @see INews.State
   */
  List<INews> getNewsByStates(Set<INews.State> states);

  /**
   * Identify the Feed's Format, for example "RSS" or "Atom".
   *
   * @param format The Format this Feed was created from.
   */
  void setFormat(String format);

  /**
   * The Name of the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>OPML 1.0</li>
   * <li>CDF</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param title The Name of the Feed to set.
   */
  void setTitle(String title);

  /**
   * Phrase or sentence describing the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>CDF</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param description The Phrase or sentence describing the Feed to set.
   */
  void setDescription(String description);

  /**
   * The Link to the HTML website corresponding to the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RDF 1.0</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>CDF</li>
   * </ul>
   * </p>
   *
   * @param link The Link to the HTML website corresponding to the Feed to set.
   */
  void setHomepage(URI link);

  /**
   * The language the Feed is written in. Allowed values are described by <a
   * href="http://blogs.law.harvard.edu/tech/stories/storyReader$15">Netscape</a>
   * and by the <a
   * href="http://www.w3.org/TR/REC-html40/struct/dirlang.html#langcodes">W3C</a>.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param language The language the Feed is written in to set.
   */
  void setLanguage(String language);

  /**
   * A Feed may contain any number of News. A News is most often representing a
   * story like in a newspaper or magazine.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>OPML</li>
   * <li>CDF</li>
   * </ul>
   * </p>
   *
   * @param news The News contained in this Feed.
   */
  void addNews(INews news);

  /**
   * The <a href="http://www.w3.org/PICS/">PICS</a> rating for the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param rating The PICS rating for the Feed to set.
   */
  void setRating(String rating);

  /**
   * Specifies an Image that can be displayed with the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param image The Image for the Feed to set.
   */
  void setImage(IImage image);

  /**
   * Specifies a text input box that can be displayed with the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param input A text input box for the Feed to set.
   */
  void setTextInput(ITextInput input);

  /**
   * Copyright notice for content in the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param copyright The Copyright notice for content in the Feed to set.
   */
  void setCopyright(String copyright);

  /**
   * The ation date for the content in the Feed. <a
   * href="http://asg.web.cmu.edu/rfc/rfc822.html">RFC 822</a> Date Format is
   * commonly used. The year may be expressed with two characters or four
   * characters (four preferred).
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param pubDate The ation date for the content in the Feed to set.
   */
  void setPublishDate(Date pubDate);

  /**
   * The last time the content of the Feed changed. <a
   * href="http://asg.web.cmu.edu/rfc/rfc822.html">RFC 822</a> Date Format is
   * commonly used.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>OPML 1.0</li>
   * <li>CDF</li>
   * </ul>
   * </p>
   *
   * @param lastBuildDate The last time the content of the Feed changed to set.
   */
  void setLastBuildDate(Date lastBuildDate);

  /**
   * A Date construct indicating the most recent instant in time when an entry
   * or feed was modified in a way the publisher considers significant.
   * <p>
   * Used by:
   * <ul>
   * <li>Atom</li>
   * <li>OPML 1.0</li>
   * </ul>
   * </p>
   *
   * @param lastModifiedDate The Date of last modification.
   */
  void setLastModifiedDate(Date lastModifiedDate);

  /**
   * Specify one or more categories that the Feed belongs to.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param category Add a Category this Feed belongs to.
   */
  void addCategory(ICategory category);

  /**
   * A String indicating the program used to generate the Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param generator The String indicating the program used to generate the
   * Feed to set.
   */
  void setGenerator(String generator);

  /**
   * Stands for time to live. It's a number of minutes that indicates how long a
   * Feed can be cached before refreshing from the source.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param ttl The Time to live to set.
   */
  void setTTL(int ttl);

  /**
   * A Link that points to the documentation for the format used in the Feed's
   * XML file.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param docs The Link to the Docs to set.
   */
  void setDocs(URI docs);

  /**
   * Indicates the Author or Editor of the Feed.
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>OPML 1.0</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   *
   * @param author The Author or Editor of the Feed.
   */
  void setAuthor(IPerson author);

  /**
   * Email address for person responsible for technical issues relating to the
   * Feed.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param webmaster The Email address for person responsible for technical
   * issues relating to the Feed to set.
   */
  void setWebmaster(String webmaster);

  /**
   * A hint for aggregators telling them which hours they can skip. More
   * specifically an XML element that contains up to 24 Hour sub-elements whose
   * value is a number between 0 and 23, representing a time in GMT, when
   * aggregators, if they support the feature, may not read the Feed on hours
   * listed in the skipHours element.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param hour Add an Hour to the list of hours that the Feed shall not be
   * loaded.
   */
  void addHourToSkip(int hour);

  /**
   * A hint for aggregators telling them which days they can skip. More
   * specifically an XML element that contains up to seven Day sub-elements
   * whose value is Monday, Tuesday, Wednesday, Thursday, Friday, Saturday or
   * Sunday. Aggregators may not read the channel during days listed in the
   * skipDays element.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.91</li>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param day Add a Day to the list of Days that the Feed shall not be loaded.
   * The integer points to one of the days as defined by IFeed.
   */
  void addDayToSkip(int day);

  /**
   * Allows processes to register with a cloud to be notified of updates to the
   * Feed, implementing a lightweight publish-subscribe protocol for Feeds. More
   * specifically it specifies a web service that supports the rssCloud
   * interface which can be implemented in HTTP-POST, XML-RPC or SOAP 1.1. A
   * full explanation of this element and the rssCloud interface is <a
   * href="http://blogs.law.harvard.edu/tech/soapMeetsRss#rsscloudInterface">here</a>.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param cloud The Cloud to set for this Feed.
   */
  void setCloud(ICloud cloud);

  /**
   * Describes the period over which the channel format is updated. Acceptable
   * values are: hourly, daily, weekly, monthly, yearly. If omitted, daily is
   * assumed.
   * <p>
   * Used by:
   * <ul>
   * <li>Syndication Namespace</li>
   * </ul>
   * </p>
   *
   * @param updatePeriod On of the constants defined in <code>IFeed</code> for
   * Update Periods.
   */
  void setUpdatePeriod(int updatePeriod);

  /**
   * Used to describe the frequency of updates in relation to the update period.
   * A positive integer indicates how many times in that period the channel is
   * updated. For example, an updatePeriod of daily, and an updateFrequency of 2
   * indicates the channel format is updated twice daily. If omitted a value of
   * 1 is assumed.
   * <p>
   * Used by:
   * <ul>
   * <li>Syndication Namespace</li>
   * </ul>
   * </p>
   *
   * @param updateFrequency The frequency of updates in relation to the update
   * period.
   */
  void setUpdateFrequency(int updateFrequency);

  /**
   * Defines a base date to be used in concert with updatePeriod and
   * updateFrequency to calculate the publishing schedule. The date format takes
   * the form: yyyy-mm-ddThh:mm
   * <p>
   * Used by:
   * <ul>
   * <li>Syndication Namespace</li>
   * </ul>
   * </p>
   *
   * @param updateBase The base date to be used in concert with updatePeriod and
   * updateFrequency to set.
   */
  void setUpdateBase(Date updateBase);

  /**
   * Define the Base URI to be used to resolve any URIs in this News.
   * <p>
   * Used by:
   * <ul>
   * <li>XML Namespace</li>
   * </ul>
   * </p>
   *
   * @param baseUri
   */
  void setBase(URI baseUri);

  /**
   * @return The Base URI all URIs must be resolved with.
   */
  URI getBase();

  /**
   * @return On of the constants defined in <code>IFeed</code> for Update
   * Periods.
   */
  int getUpdatePeriod();

  /**
   * @return The frequency of updates in relation to the update period.
   */
  int getUpdateFrequency();

  /**
   * @return The base date to be used in concert with updatePeriod and
   * updateFrequency
   */
  Date getUpdateBase();

  /**
   * @return Returns the cloud.
   */
  ICloud getCloud();

  /**
   * @return Returns the copyright.
   */
  String getCopyright();

  /**
   * @return Returns the description.
   */
  String getDescription();

  /**
   * @return Returns the docs.
   */
  URI getDocs();

  /**
   * @return Returns the format.
   */
  String getFormat();

  /**
   * @return Returns the generator.
   */
  String getGenerator();

  /**
   * @return Returns the image.
   */
  IImage getImage();

  /**
   * @return Returns the language.
   */
  String getLanguage();

  /**
   * @return Returns the lastBuildDate.
   */
  Date getLastBuildDate();

  /**
   * @return Returns the link.
   */
  URI getHomepage();

  /**
   * @return Returns the pubDate.
   */
  Date getPublishDate();

  /**
   * @return Returns the lastModificationDate.
   */
  Date getLastModifiedDate();

  /**
   * @return Returns the rating.
   */
  String getRating();

  /**
   * @return Returns the title.
   */
  String getTitle();

  /**
   * @return Returns the text input.
   */
  ITextInput getTextInput();

  /**
   * @return Returns the webmaster.
   */
  String getWebmaster();

  /**
   * @return Returns the Time to live.
   */
  int getTTL();

  /**
   * @return Days to Skip...
   */
  int[] getDaysToSkip();

  /**
   * @return Hours to Skip...
   */
  int[] getHoursToSkip();

  /**
   * @return All Categories of this Feed.
   */
  List<ICategory> getCategories();

  /**
   * @return All News of this Feed.
   */
  List<INews> getNews();

  /**
   * @return The Author of this Feed.
   */
  IPerson getAuthor();

  /**
   * @return The unique Link of this Feed.
   */
  URI getLink();

  /**
   * In addition to {@link #merge(IFeed)}, it also removes all the news that
   * are not in <code>objectToMerge</code> and have a state of DELETED from
   * this feed and returns them.
   *
   * @param objectToMerge
   * @param cleanUp
   * @return the list of INews that have been removed from this feed. To
   */
  MergeResult mergeAndCleanUp(IFeed objectToMerge);

  /**
   * Removes <code>news</code> from the feed if such news exists in the feed.
   * Otherwise, the method does nothing. In general, it's not necessary to use
   * this. Instead, the state of the news should be changed to
   * <code>DELETED</code> and they will be removed as part of
   * <code>mergeAndCleanUp</code>.
   *
   * @param news to delete.
   * @return <code>true</code> if feed contained the given news.
   * @see INews#setState(org.rssowl.core.persist.INews.State)
   * @see INews.State#DELETED
   */
  boolean removeNews(INews news);

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  @Override
  FeedReference toReference();
}