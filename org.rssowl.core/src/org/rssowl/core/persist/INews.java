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

import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.persist.reference.NewsReference;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The super-type of all News Elements in Feeds.
 *
 * @author bpasero
 */
public interface INews extends IEntity, MergeCapable<INews>, Reparentable<IFeed> {

  /** One of the fields in this type described as constant */
  public static final int TITLE = 0;

  /** One of the fields in this type described as constant */
  public static final int LINK = 1;

  /** One of the fields in this type described as constant */
  public static final int DESCRIPTION = 2;

  /** One of the fields in this type described as constant */
  public static final int AUTHOR = 3;

  /** One of the fields in this type described as constant */
  public static final int COMMENTS = 4;

  /** One of the fields in this type described as constant */
  public static final int GUID = 5;

  /** One of the fields in this type described as constant */
  public static final int PUBLISH_DATE = 6;

  /** One of the fields in this type described as constant */
  public static final int MODIFIED_DATE = 7;

  /** One of the fields in this type described as constant */
  public static final int RECEIVE_DATE = 8;

  /** One of the fields in this type described as constant */
  public static final int SOURCE = 9;

  /** One of the fields in this type described as constant */
  public static final int HAS_ATTACHMENTS = 10;

  /** One of the fields in this type described as constant */
  public static final int ATTACHMENTS_CONTENT = 11;

  /** One of the fields in this type described as constant */
  public static final int CATEGORIES = 12;

  /** One of the fields in this type described as constant */
  public static final int IS_FLAGGED = 13;

  /** One of the fields in this type described as constant */
  public static final int STATE = 14;

  /** One of the fields in this type described as constant */
  public static final int LABEL = 15;

  /** One of the fields in this type described as constant */
  public static final int RATING = 16;

  /** One of the fields in this type described as constant */
  public static final int FEED = 17;

  /** One of the fields in this type described as constant */
  public static final int AGE_IN_DAYS = 18;

  /** One of the fields in this type described as constant */
  public static final int LOCATION = 19;

  /** One of the fields in this type described as constant */
  public static final int PARENT_ID = 20;

  /** One of the fields in this type described as constant */
  public static final int AGE_IN_MINUTES = 21;

  /**
   * States of a INews being exclusive. Explanation follows:
   * <ul>
   * <li>NEW: The News has not yet been displayed to the User (Implies Unread)</li>
   * <li>READ: The News has been read by the User</li>
   * <li>UNREAD: The News has been displayed to the User, but is marked Unread</li>
   * <li>UPDATED: The News has updated content</li>
   * <li>HIDDEN: The News was deleted by the user and is hidden from the Feed</li>
   * <li>DELETED: The News is ready to be deleted from the Database</li>
   * </ul>
   */
  public static enum State {

    /** News has not yet been displayed */
    NEW,

    /** News is marked as Read */
    READ,

    /** News is marked as not Read */
    UNREAD,

    /** News has been Updated */
    UPDATED,

    /** News has been deleted from the Feed */
    HIDDEN,

    /** News is ready to be deleted from the Database */
    DELETED;

    private static final transient Set<State> VISIBLE_STATES = EnumSet.of(NEW, READ, UNREAD, UPDATED);
    private static final transient State[] VALUES = values();

    /**
     * Returns an unmodifiable set containing the visible states (all of them
     * apart from HIDDEN and DELETED).
     *
     * @return an unmodifiable set containing the visible states (all of them
     * apart from HIDDEN and DELETED).
     */
    public static final Set<State> getVisible() {
      return Collections.unmodifiableSet(VISIBLE_STATES);
    }

    /**
     * Returns the {@code State} with the matching {@code ordinal}.
     *
     * @param ordinal identifying the State required.
     * @return State with the provided ordinal.
     */
    public static final State getState(int ordinal) {
      return VALUES[ordinal];
    }
  };

  /**
   * The title of the News.
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
   * @param title The title of the News to set.
   */
  void setTitle(String title);

  /**
   * The URL of the News.
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
   * </ul>
   * </p>
   *
   * @param link The URL of the News to set.
   */
  void setLink(URI link);

  /**
   * The Content of the News.
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
   * <li>Content Namespace</li>
   * </ul>
   * </p>
   *
   * @param description The Content of the News to set.
   */
  void setDescription(String description);

  /**
   * Email address or Name of the author of the News.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param author Email address or Name of the author of the News to set.
   */
  void setAuthor(IPerson author);

  /**
   * Most often an URL of a page for comments relating to the News.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * </ul>
   * </p>
   *
   * @param comments URL of a page for comments relating to the News to set.
   */
  void setComments(String comments);

  /**
   * A String that uniquely identifies the News.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param guid The String that uniquely identifies the News to set.
   */
  void setGuid(IGuid guid);

  /**
   * The Date this News was published. <a
   * href="http://asg.web.cmu.edu/rfc/rfc822.html">RFC 822</a> Date Format is
   * commonly used.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>CDF</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param pubDate The Date this News was published to set.
   */
  void setPublishDate(Date pubDate);

  /**
   * The Date this News was received. This information is not part of the
   * Newsfeed, but will be set when downloading the News from the Feed.
   *
   * @param receiveDate The Date this News was received to set.
   */
  void setReceiveDate(Date receiveDate);

  /**
   * Indicates the time that the entry was last modified
   * <p>
   * Used by:
   * <ul>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param modifiedDate Indicates the time that the entry was last modified.
   */
  void setModifiedDate(Date modifiedDate);

  /**
   * The Source this News came from.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param source The Source this News came from to set.
   */
  void setSource(ISource source);

  /**
   * If this News is a reply to a different News, indicate this by adding the
   * guid of the other News. Note that the other News <em>must</em> contain a
   * GUID then in order to be referenced properly.
   * <p>
   * Used by:
   * <ul>
   * <li>Newsgroups</li>
   * </ul>
   * </p>
   *
   * @param guid The guid of the other News this News is a reply to.
   */
  void setInReplyTo(String guid);

  /**
   * Add a media object that is attached to this News.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * </ul>
   * </p>
   *
   * @param attachment A media object that is attached to this News.
   */
  void addAttachment(IAttachment attachment);

  /**
   * Add a Category this News is included in.
   * <p>
   * Used by:
   * <ul>
   * <li>RSS 0.92</li>
   * <li>RSS 2.0</li>
   * <li>Atom</li>
   * <li>Dublin Core Namespace</li>
   * </ul>
   * </p>
   *
   * @param category A Category this News is included in.
   */
  void addCategory(ICategory category);

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
   * @return All Categories of this News.
   */
  List<ICategory> getCategories();

  /**
   * @return All Attachments of this News.
   */
  List<IAttachment> getAttachments();

  /**
   * @return Returns the author.
   */
  IPerson getAuthor();

  /**
   * @return Returns the comments.
   */
  String getComments();

  /**
   * @return Returns the description.
   */
  String getDescription();

  /**
   * @return Returns the guid.
   */
  IGuid getGuid();

  /**
   * @return Returns the link.
   */
  URI getLink();

  /**
   * @return Returns the pubDate.
   */
  Date getPublishDate();

  /**
   * @return Returns the modifiedDate.
   */
  Date getModifiedDate();

  /**
   * @return Returns the source.
   */
  ISource getSource();

  /**
   * @return Returns the inReplyTo.
   */
  String getInReplyTo();

  /**
   * @return Returns the title.
   */
  String getTitle();

  /**
   * @return The Time this News was received.
   */
  Date getReceiveDate();

  /**
   * @return The Base URI all URIs must be resolved with.
   */
  URI getBase();

  /**
   * @param rating The Rating for this News to set. Implementors can decide how
   * the rating is defined by the given int value.
   */
  void setRating(int rating);

  /**
   * @return Returns the rating for this News. Implementors can decide how the
   * rating is defined by the given int value.
   */
  int getRating();

  /**
   * @param state Sets the state of this News, as defined in the
   * <code>INews.State</code> enum.
   */
  void setState(INews.State state);

  /**
   * @return Returns the state of this News as defined in the
   * <code>INews.State</code> enum.
   */
  INews.State getState();

  /**
   * @return TRUE if this News has been flagged by the user or system.
   */
  boolean isFlagged();

  /**
   * @param isFlagged TRUE if this News has been flagged by the user or system.
   */
  void setFlagged(boolean isFlagged);

  /**
   * @return An Set containing the Labels of this News.
   */
  Set<ILabel> getLabels();

  /**
   * Add {@code label} to this News if it's not present in the News yet.
   *
   * @param label Label to add.
   * @return {@code true} if {@code label} was added to the INews or
   * {@code false} if not.
   */
  boolean addLabel(ILabel label);

  /**
   * Remove {@code label} from this News if it's present in the News.
   *
   * @param label Label to remove.
   * @return {@code true} if {@code label} was removed from this News.
   */
  boolean removeLabel(ILabel label);

  /**
   * @return The feed that this object belongs to.
   */
  FeedLinkReference getFeedReference();

  /**
   * @return TRUE if this News is visible (thereby not Hidden or Deleted) and
   * FALSE otherwise.
   */
  boolean isVisible();

  /**
   * Returns whether this news is equivalent to <code>other</code>. The
   * algorithm used follows:
   * <li>If the guid is not null in both news and {@link IGuid#getValue()}
   * returns the same, then both news are equivalent.</li>
   * <li>If the guid is null for both news and the link property is the same
   * for both news, then both news are equivalent.</li>
   * <li>If the guid and link are null for both news, and the link from the
   * parent feed and title are equal for both news, then they are equivalent.</li>
   * <p>
   * Otherwise, the news are not equivalent.
   * </p>
   * <p>
   * <strong>Note that this algorithm is still being tweaked to deal in the best
   * way possible with existing feeds and news, so it's subject to change</strong>.
   * Use the algorithm description as a guide instead of a specification.
   * </p>
   *
   * @param other INews to be compared.
   * @return <code>true</code> if this news is equivalent to
   * <code>other</code>.
   */
  boolean isEquivalent(INews other);

  /**
   * Removes <code>attachment</code> from the news if such attachment exists
   * in the feed. Otherwise, the method does nothing.
   *
   * @param attachment
   */
  void removeAttachment(IAttachment attachment);

  /**
   * Return the id of the parent of this INews or 0 if the parent is a feed.
   * Hence this method can used to find out whether this news is still attached
   * to the original feed or it's a copy attached to another parent (e.g. INewsBin).
   * If the latter, calling {@code getFeedReference().resolve()} will return a feed
   * that does _not_ contain this news.
   *
   * @return the id of the parent of this INews or 0 if the parent is a feed.
   */
  long getParentId();

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  NewsReference toReference();

  /**
   * Convenience method that returns the feed this news belongs to as text.
   *
   * @return the feed's link as text.
   */
  String getFeedLinkAsText();

  /**
   * Convenience method that returns the link of this news as text.
   *
   * @return the link of this news as text.
   */
  String getLinkAsText();
}