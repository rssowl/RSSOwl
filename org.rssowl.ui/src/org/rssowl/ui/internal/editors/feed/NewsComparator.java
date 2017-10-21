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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.event.NewsEvent;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.editors.feed.NewsGrouping.Group;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sorts the elements of the feed view based on the choices provided by the
 * user.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 * @author bpasero
 */
public class NewsComparator extends ViewerComparator implements Comparator<INews> {
  private NewsColumn fSortBy;
  private boolean fAscending;

  /* A cache for the Location Column */
  private Map<Long, String> fMapBinIdToLocation = new HashMap<Long, String>();
  private Map<String, String> fMapFeedLinkToLocation = new HashMap<String, String>();

  /**
   * @return Returns the ascending.
   */
  public boolean isAscending() {
    return fAscending;
  }

  /**
   * @param ascending The ascending to set.
   */
  public void setAscending(boolean ascending) {
    fAscending = ascending;
  }

  /**
   * @return Returns the sortBy.
   */
  public NewsColumn getSortBy() {
    return fSortBy;
  }

  /**
   * @param sortBy The sortBy to set.
   */
  public void setSortBy(NewsColumn sortBy) {
    fSortBy = sortBy;
  }

  /**
   * @param events the {@link Set} of NewsEvents that occured.
   * @return <code>true</code> if the sorter requires a refresh and
   * <code>false</code> otherwise
   */
  public boolean needsRefresh(Collection<NewsEvent> events) {
    if (fSortBy != null) {
      switch (fSortBy) {
        case AUTHOR:
          return CoreUtils.isAuthorChange(events);

        case CATEGORY:
          return CoreUtils.isCategoryChange(events);

        case DATE:
          return CoreUtils.isDateChange(events);

        case MODIFIED:
          return CoreUtils.isModifiedDateChange(events);

        case PUBLISHED:
          return CoreUtils.isPublishedDateChange(events);

        case TITLE:
          return CoreUtils.isTitleChange(events);
      }
    }

    return false; //We ignore some fields intentionally (e.g. state, sticky) to avoid refresh from user interaction
  }

  /*
   * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {

    /* Compare Entity Groups */
    if (e1 instanceof EntityGroup && e2 instanceof EntityGroup)
      return compare((EntityGroup) e1, (EntityGroup) e2);

    /* Compare News */
    if (e1 instanceof INews && e2 instanceof INews)
      return compare((INews) e1, (INews) e2);

    return 0;
  }

  private int compare(EntityGroup e1, @SuppressWarnings("unused") EntityGroup e2) {

    /* Support sorting Entity Groups of type DATE */
    if (fSortBy != null && (fSortBy == NewsColumn.DATE || fSortBy == NewsColumn.PUBLISHED || fSortBy == NewsColumn.MODIFIED || fSortBy == NewsColumn.RECEIVED) && fAscending) {
      long id = e1.getId();
      if (id == Group.TODAY.ordinal() || id == Group.YESTERDAY.ordinal() || id == Group.EARLIER_THIS_WEEK.ordinal() || id == Group.LAST_WEEK.ordinal() || id == Group.OLDER.ordinal())
        return fAscending ? 1 : -1;
    }

    return 0;
  }

  /*
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(INews news1, INews news2) {
    int result = 0;

    if (fSortBy != null) {
      switch (fSortBy) {

        /* Sort by Date */
        case DATE:
          return compareByDate(news1, news2, false);

          /* Sort by Publish Date */
        case PUBLISHED:
          return compareByDate(news1.getPublishDate(), news2.getPublishDate(), false);

          /* Sort by Modified Date */
        case MODIFIED:
          return compareByDate(news1.getModifiedDate(), news2.getModifiedDate(), false);

          /* Sort by Received Date */
        case RECEIVED:
          return compareByDate(news1.getReceiveDate(), news2.getReceiveDate(), false);

          /* Sort by Title */
        case TITLE:
          result = compareByTitle(CoreUtils.getHeadline(news1, true), CoreUtils.getHeadline(news2, true));
          break;

        /* Sort by Author */
        case AUTHOR:
          result = compareByAuthor(news1.getAuthor(), news2.getAuthor());
          break;

        /* Sort by Category */
        case CATEGORY:
          result = compareByCategory(news1.getCategories(), news2.getCategories());
          break;

        /* Sort by Stickyness */
        case STICKY:
          result = compareByStickyness(news1.isFlagged(), news2.isFlagged());
          break;

        /* Sort by Feed */
        case FEED:
          result = compareByFeed(news1.getFeedLinkAsText(), news2.getFeedLinkAsText());
          break;

        /* Sort by "Has Attachments" */
        case ATTACHMENTS:
          result = compareByHasAttachments(!news1.getAttachments().isEmpty(), !news2.getAttachments().isEmpty());
          break;

        /* Sort by Labels */
        case LABELS:
          result = compareByLabels(CoreUtils.getSortedLabels(news1), CoreUtils.getSortedLabels(news2));
          break;

        /* Sort by Status */
        case STATUS:
          result = compareByStatus(news1.getState(), news2.getState());
          break;

        /* Sort by Location */
        case LOCATION:
          result = compareByLocation(news1, news2);
          break;

        /* Sort by Link */
        case LINK:
          result = compareByLink(news1, news2);
          break;
      }
    }

    /* Fall Back to default sort if result is 0 */
    if (result == 0)
      result = compareByDate(news1, news2, true);

    return result;
  }

  private int compareByFeed(String feedLink1, String feedLink2) {
    int result = feedLink1.compareTo(feedLink2);

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByDate(INews news1, INews news2, boolean forceDescending) {
    Date date1 = DateUtils.getRecentDate(news1);
    Date date2 = DateUtils.getRecentDate(news2);

    return compareByDate(date1, date2, forceDescending);
  }

  private int compareByDate(Date date1, Date date2, boolean forceDescending) {
    if (date1 == null)
      return fAscending && !forceDescending ? -1 : 1;

    if (date2 == null)
      return fAscending && !forceDescending ? 1 : -1;

    int result = date1.compareTo(date2);

    /* Respect ascending / descending Order */
    return fAscending && !forceDescending ? result : result * -1;
  }

  private int compareByTitle(String title1, String title2) {
    int result = compareByString(title1, title2);

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByStatus(INews.State s1, INews.State s2) {
    int result = 0;

    if (s1 != s2) {
      if (s1 == State.NEW)
        result = -1;
      else if (s2 == State.NEW)
        result = 1;
      else if (s1 == State.UPDATED)
        result = -1;
      else if (s2 == State.UPDATED)
        result = 1;
      else if (s1 == State.UNREAD)
        result = -1;
      else if (s2 == State.UNREAD)
        result = 1;
      else
        result = s1.compareTo(s2);
    }

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByLocation(INews n1, INews n2) {
    int result = compareByString(getLocation(n1), getLocation(n2));

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByLink(INews n1, INews n2) {
    int result = compareByString(CoreUtils.getLink(n1), CoreUtils.getLink(n2));

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private String getLocation(INews news) {

    /* Location: Bin */
    if (news.getParentId() > 0) {
      String location = fMapBinIdToLocation.get(news.getParentId());
      if (location == null) {
        NewsBinReference ref = new NewsBinReference(news.getParentId());
        INewsBin bin = ref.resolve();
        location = bin.getName();
        fMapBinIdToLocation.put(news.getParentId(), location);
      }

      return location;
    }

    /* Location: Bookmark */
    String location = fMapFeedLinkToLocation.get(news.getFeedLinkAsText());
    if (location == null) {
      IBookMark bookmark = CoreUtils.getBookMark(news.getFeedLinkAsText());
      if (bookmark != null) {
        location = bookmark.getName();
        fMapFeedLinkToLocation.put(news.getFeedLinkAsText(), location);
      }
    }

    return location;
  }

  private int compareByHasAttachments(boolean hasAttachments1, boolean hasAttachments2) {
    int result = 0;

    if (hasAttachments1 && !hasAttachments2)
      result = 1;

    else if (!hasAttachments1 && hasAttachments2)
      result = -1;

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByLabels(Set<ILabel> labels1, Set<ILabel> labels2) {

    /* Detect cases of empty Labels first */
    if (labels1.isEmpty() && labels2.isEmpty())
      return 0;
    else if (labels1.isEmpty())
      return fAscending ? 1 : -1;
    else if (labels2.isEmpty())
      return fAscending ? -1 : 1;

    /* Now compare all labels as there can be more than one assigned */
    int result = 0;
    Iterator<ILabel> labels1Iterator = labels1.iterator();
    Iterator<ILabel> labels2Iterator = labels2.iterator();
    while (labels1Iterator.hasNext() && labels2Iterator.hasNext()) {
      ILabel label1 = labels1Iterator.next();
      ILabel label2 = labels2Iterator.next();

      /* Labels identical at this point */
      if (label1.getOrder() == label2.getOrder()) {

        /* Look for the next label to compare if still labels present */
        if (labels1Iterator.hasNext() && labels2Iterator.hasNext())
          continue;

        /* Sort news with more labels below */
        if (labels1Iterator.hasNext())
          result = -1;

        /* Otherwise keep label above */
        else
          result = 1;

        break;
      }

      /* Labels not identical - compare order and break */
      result = label1.getOrder() < label2.getOrder() ? -1 : 1;
      break;
    }

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByAuthor(IPerson author1, IPerson author2) {
    int result = 0;

    if (author1 != null && author2 != null) {
      String value1 = author1.getName();
      if (value1 == null && author1.getEmail() != null)
        value1 = author1.getEmail().toString();
      else if (value1 == null && author1.getUri() != null)
        value1 = author1.getUri().toString();

      String value2 = author2.getName();
      if (value2 == null && author2.getEmail() != null)
        value2 = author2.getEmail().toString();
      else if (value2 == null && author2.getUri() != null)
        value2 = author2.getUri().toString();

      result = compareByString(value1, value2);
    }

    else if (author1 != null)
      result = -1;

    else if (author2 != null)
      result = 1;

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByCategory(List<ICategory> categories1, List<ICategory> categories2) {
    int result = 0;

    if (categories1 != null && categories1.size() > 0 && categories2 != null && categories2.size() > 0) {
      ICategory category1 = categories1.get(0);
      ICategory category2 = categories2.get(0);

      String value1 = category1.getName();
      if (value1 == null)
        value1 = category1.getDomain();

      String value2 = category2.getName();
      if (value2 == null)
        value2 = category2.getName();

      result = compareByString(value1, value2);
    }

    else if (categories1 != null && categories1.size() > 0)
      result = -1;

    else if (categories2 != null && categories2.size() > 0)
      result = 1;

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByStickyness(boolean sticky1, boolean sticky2) {
    int result = 0;

    if (sticky1 && !sticky2)
      result = 1;

    else if (!sticky1 && sticky2)
      result = -1;

    /* Respect ascending / descending Order */
    return fAscending ? result : result * -1;
  }

  private int compareByString(String str1, String str2) {
    if (str1 != null && str2 != null)
      return str1.compareToIgnoreCase(str2);
    else if (str1 != null)
      return -1;

    return 1;
  }
}