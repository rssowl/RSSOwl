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

package org.rssowl.ui.internal.views.explorer;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.INewsMark;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.util.DateUtils;
import org.rssowl.ui.internal.EntityGroup;
import org.rssowl.ui.internal.EntityGroupItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * @author bpasero
 */
public class BookMarkGrouping {

  /* Some Date Constants */
  private static final long DAY = 24 * 60 * 60 * 1000;
  private static final long WEEK = 7 * DAY;

  /* Popularity Ratio */
  private static final float VERY_POPULAR_RATIO = 0.8f;
  private static final float POPULAR_RATIO = 0.5f;
  private static final float FAIRLY_POPULAR_RATIO = 0.2f;

  /* ID of Group Category */
  static final String GROUP_CATEGORY_ID = "org.rssowl.ui.internal.views.explorer.BookMarkGrouping"; //$NON-NLS-1$

  /** Supported Grouping Types */
  public enum Type {

    /** Grouping is Disabled */
    NO_GROUPING,

    /** Group by Last Visit Date */
    GROUP_BY_LAST_VISIT,

    /** Group by Popularity */
    GROUP_BY_POPULARITY,

    /** Group by Type */
    GROUP_BY_TYPE,

    /** Group by State */
    GROUP_BY_STATE
  }

  /** Valid Groups */
  public enum Group {

    /** Group: Visited Today */
    TODAY(Messages.BookMarkGrouping_TODAY),

    /** Group: Visited Yesterday */
    YESTERDAY(Messages.BookMarkGrouping_YESTERDAY),

    /** Group: Visited Earlier this Week */
    EARLIER_THIS_WEEK(Messages.BookMarkGrouping_EARLIER_WEEK),

    /** Group: Visited Last Week */
    LAST_WEEK(Messages.BookMarkGrouping_LAST_WEEK),

    /** Group: Older */
    OLDER(Messages.BookMarkGrouping_MORE_THAN_WEEK),

    /** Group: Never Visited */
    NEVER(Messages.BookMarkGrouping_NEVER),

    /** Group: Very Popular */
    VERY_POPULAR(Messages.BookMarkGrouping_VERY_POPULAR),

    /** Group: Popular */
    POPULAR(Messages.BookMarkGrouping_POPULAR),

    /** Group: Fairly Popular */
    FAIRLY_POPULAR(Messages.BookMarkGrouping_FAILRY_POPULAR),

    /** Group: Unpopular */
    UNPOPULAR(Messages.BookMarkGrouping_UNPOPULAR),

    /** Group: Bins */
    BIN(Messages.BookMarkGrouping_NEWS_BINS),

    /** Group: Searches */
    SEARCH(Messages.BookMarkGrouping_SAVED_SEARCHES),

    /** Group: Bookmarks */
    BOOKMARK(Messages.BookMarkGrouping_BOOKMARKS),

    /** Group: Sticky */
    STICKY(Messages.BookMarkGrouping_STICKY),

    /** Group: New */
    NEW(Messages.BookMarkGrouping_NEW),

    /** Group: Unread */
    UNREAD(Messages.BookMarkGrouping_UNREAD),

    /** Group: Other */
    OTHER(Messages.BookMarkGrouping_OTHER);

    String fName;

    Group(String name) {
      fName = name;
    }

    String getName() {
      return fName;
    }
  }

  /* Current Type of Grouping */
  private Type fType = Type.NO_GROUPING;

  /* Get the Type of grouping as defined in the Type Enum */
  Type getType() {
    return fType;
  }

  /**
   * Set the Type of grouping as defined in the Type Enum
   *
   * @param type The new Grouping Type.
   */
  public void setType(Type type) {
    fType = type;
  }

  /**
   * Group the Input based on the selected Type
   *
   * @param input The Input to Group.
   * @return The Input grouped in an array of EntityGroup, as specified by the
   * Type of Group.
   */
  public EntityGroup[] group(List<? extends IEntity> input) {
    Assert.isTrue(fType != Type.NO_GROUPING, "Grouping is not enabled!"); //$NON-NLS-1$

    /* Group by Last Visit Date */
    if (Type.GROUP_BY_LAST_VISIT == fType)
      return createLastVisitDateGroups(input);

    /* Group by Popularity */
    else if (Type.GROUP_BY_POPULARITY == fType)
      return createPopularityGroups(input);

    /* Group by State */
    else if (Type.GROUP_BY_STATE == fType)
      return createStateGroups(input);

    /* Group by Type */
    else if (Type.GROUP_BY_TYPE == fType)
      return createTypeGroups(input);

    /* Should not happen */
    return new EntityGroup[0];
  }

  /* Create the Group for the Type GROUP_BY_LAST_VISIT */
  private EntityGroup[] createLastVisitDateGroups(List<? extends IEntity> input) {

    /* Today */
    Calendar today = DateUtils.getToday();
    long todayMillis = today.getTimeInMillis();

    /* Yesterday */
    Date yesterday = new Date(todayMillis - DAY);

    /* Earlier this Week */
    today.set(Calendar.DAY_OF_WEEK, today.getFirstDayOfWeek());
    Date earlierThisWeek = today.getTime();

    /* Last Week */
    Date lastWeek = new Date(earlierThisWeek.getTime() - WEEK);

    /* Build Groups */
    EntityGroup gToday = new EntityGroup(Group.TODAY.ordinal(), GROUP_CATEGORY_ID, Group.TODAY.getName());
    EntityGroup gYesterday = new EntityGroup(Group.YESTERDAY.ordinal(), GROUP_CATEGORY_ID, Group.YESTERDAY.getName());
    EntityGroup gEarlierThisWeek = new EntityGroup(Group.EARLIER_THIS_WEEK.ordinal(), GROUP_CATEGORY_ID, Group.EARLIER_THIS_WEEK.getName());
    EntityGroup gLastWeek = new EntityGroup(Group.LAST_WEEK.ordinal(), GROUP_CATEGORY_ID, Group.LAST_WEEK.getName());
    EntityGroup gOlder = new EntityGroup(Group.OLDER.ordinal(), GROUP_CATEGORY_ID, Group.OLDER.getName());
    EntityGroup gNever = new EntityGroup(Group.NEVER.ordinal(), GROUP_CATEGORY_ID, Group.NEVER.getName());

    /* Group Input */
    for (Object object : input) {
      if (object instanceof IMark) {
        IMark mark = (IMark) object;
        Date lastVisitDate = mark.getLastVisitDate();

        /* Feed was never visited */
        if (lastVisitDate == null)
          new EntityGroupItem(gNever, mark);

        /* Feed was visited Today */
        else if (lastVisitDate.getTime() >= todayMillis)
          new EntityGroupItem(gToday, mark);

        /* Feed was visited Yesterday */
        else if (lastVisitDate.compareTo(yesterday) >= 0)
          new EntityGroupItem(gYesterday, mark);

        /* Feed was visited Earlier this Week */
        else if (lastVisitDate.compareTo(earlierThisWeek) >= 0)
          new EntityGroupItem(gEarlierThisWeek, mark);

        /* Feed was visited Last Week */
        else if (lastVisitDate.compareTo(lastWeek) >= 0)
          new EntityGroupItem(gLastWeek, mark);

        /* Feed was visited more than a Week ago */
        else
          new EntityGroupItem(gOlder, mark);
      }
    }

    /* Select all that are non empty */
    return maskEmpty(new ArrayList<EntityGroup>(Arrays.asList(new EntityGroup[] { gNever, gToday, gYesterday, gEarlierThisWeek, gLastWeek, gOlder })));
  }

  /* Create the Group for the Type GROUP_BY_POPULARITY */
  private EntityGroup[] createPopularityGroups(List<? extends IEntity> input) {

    /* Build Groups */
    EntityGroup gVeryPopular = new EntityGroup(Group.VERY_POPULAR.ordinal(), GROUP_CATEGORY_ID, Group.VERY_POPULAR.getName());
    EntityGroup gPopular = new EntityGroup(Group.POPULAR.ordinal(), GROUP_CATEGORY_ID, Group.POPULAR.getName());
    EntityGroup gFairlyPopular = new EntityGroup(Group.FAIRLY_POPULAR.ordinal(), GROUP_CATEGORY_ID, Group.FAIRLY_POPULAR.getName());
    EntityGroup gUnpopular = new EntityGroup(Group.UNPOPULAR.ordinal(), GROUP_CATEGORY_ID, Group.UNPOPULAR.getName());

    /* Get the Max. Popularity */
    float maxPopularity = -1;
    for (Object object : input) {
      if (object instanceof IMark) {
        IMark bookmark = (IMark) object;
        maxPopularity = Math.max(maxPopularity, bookmark.getPopularity());
      }
    }

    /* Group Input */
    for (Object object : input) {
      if (object instanceof IMark) {
        IMark bookmark = (IMark) object;

        float popularity = bookmark.getPopularity();
        float ratio = maxPopularity > 0 ? popularity / maxPopularity : 0;

        /* Feed is Very Popular */
        if (ratio >= VERY_POPULAR_RATIO)
          new EntityGroupItem(gVeryPopular, bookmark);

        /* Feed is Popular */
        else if (ratio >= POPULAR_RATIO)
          new EntityGroupItem(gPopular, bookmark);

        /* Feed is Fairly Popular */
        else if (ratio >= FAIRLY_POPULAR_RATIO)
          new EntityGroupItem(gFairlyPopular, bookmark);

        /* Feed is Unpopular */
        else
          new EntityGroupItem(gUnpopular, bookmark);
      }
    }

    /* Select all that are non empty */
    return maskEmpty(new ArrayList<EntityGroup>(Arrays.asList(new EntityGroup[] { gVeryPopular, gPopular, gFairlyPopular, gUnpopular })));
  }

  /* Create the Group for the Type GROUP_BY_TYPE */
  private EntityGroup[] createTypeGroups(List<? extends IEntity> input) {

    /* Build Groups */
    EntityGroup gBins = new EntityGroup(Group.BIN.ordinal(), GROUP_CATEGORY_ID, Group.BIN.getName());
    EntityGroup gSearches = new EntityGroup(Group.SEARCH.ordinal(), GROUP_CATEGORY_ID, Group.SEARCH.getName());
    EntityGroup gBookmarks = new EntityGroup(Group.BOOKMARK.ordinal(), GROUP_CATEGORY_ID, Group.BOOKMARK.getName());

    /* Group Input */
    for (Object object : input) {

      /* Bookmark */
      if (object instanceof IBookMark)
        new EntityGroupItem(gBookmarks, (IEntity) object);

      /* Bin */
      else if (object instanceof INewsBin)
        new EntityGroupItem(gBins, (IEntity) object);

      /* Saved Search */
      else if (object instanceof ISearchMark)
        new EntityGroupItem(gSearches, (IEntity) object);
    }

    /* Select all that are non empty */
    return maskEmpty(new ArrayList<EntityGroup>(Arrays.asList(new EntityGroup[] { gBins, gSearches, gBookmarks })));
  }

  /* Create the Group for the Type GROUP_BY_STATE */
  private EntityGroup[] createStateGroups(List<? extends IEntity> input) {

    /* Build Groups */
    EntityGroup gSticky = new EntityGroup(Group.STICKY.ordinal(), GROUP_CATEGORY_ID, Group.STICKY.getName());
    EntityGroup gNew = new EntityGroup(Group.NEW.ordinal(), GROUP_CATEGORY_ID, Group.NEW.getName());
    EntityGroup gUnread = new EntityGroup(Group.UNREAD.ordinal(), GROUP_CATEGORY_ID, Group.UNREAD.getName());
    EntityGroup gOther = new EntityGroup(Group.OTHER.ordinal(), GROUP_CATEGORY_ID, Group.OTHER.getName());

    /* Group Input */
    for (Object object : input) {
      if (object instanceof INewsMark) {
        INewsMark mark = (INewsMark) object;

        /* Early exclude saved searches (buggy) */
        if (mark instanceof ISearchMark)
          new EntityGroupItem(gOther, mark);

        /* Contains Sticky */
        else if (mark instanceof IBookMark && ((IBookMark) mark).getStickyNewsCount() > 0)
          new EntityGroupItem(gSticky, mark);

        /* Contains New */
        else if (mark.getNewsCount(EnumSet.of(INews.State.NEW)) > 0)
          new EntityGroupItem(gNew, mark);

        /* Contains Unread */
        else if (mark.getNewsCount(EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED)) > 0)
          new EntityGroupItem(gUnread, mark);

        /* Other */
        else
          new EntityGroupItem(gOther, mark);
      }
    }

    /* Select all that are non empty */
    return maskEmpty(new ArrayList<EntityGroup>(Arrays.asList(new EntityGroup[] { gSticky, gNew, gUnread, gOther })));
  }

  private EntityGroup[] maskEmpty(List<EntityGroup> items) {
    List<EntityGroup> maskedItems = new ArrayList<EntityGroup>();
    for (EntityGroup item : items) {
      if (item.size() > 0)
        maskedItems.add(item);
    }

    return maskedItems.toArray(new EntityGroup[maskedItems.size()]);
  }

  boolean needsRefresh(Class<? extends IEntity> entityClass) {

    /* In case the Grouping is not active at all */
    if (fType == Type.NO_GROUPING)
      return false;

    /* Early handle News */
    if (entityClass.equals(INews.class))
      return fType == Type.GROUP_BY_STATE;

    /* Folder Event (e.g. Mark deleted) */
    if (entityClass.equals(IFolder.class))
      return true;

    /* Bookmark Event */
    if (IBookMark.class.isAssignableFrom(entityClass))
      return true;

    /* Searchmark Event */
    if (ISearchMark.class.isAssignableFrom(entityClass))
      return true;

    /* News Bin Event */
    if (INewsBin.class.isAssignableFrom(entityClass))
      return true;

    return false;
  }

  boolean isActive() {
    return fType != Type.NO_GROUPING;
  }
}