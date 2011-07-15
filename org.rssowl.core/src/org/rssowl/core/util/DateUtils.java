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

package org.rssowl.core.util;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.INews;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility Class for working with <code>Dates</code>.
 *
 * @author bpasero
 */
public class DateUtils {

  /** 1 Day in Millis */
  public static final long DAY = 24L * 60L * 60L * 1000L;

  /** 1 Week in Millis */
  public static final long WEEK = 7 * DAY;

  /* An array of custom date formats */
  private static final DateFormat[] CUSTOM_DATE_FORMATS;

  /* The Default Timezone to be used */
  private static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$

  /**
   * @return A Calendar instance with the time being Today with a Time of
   * 0:00:00
   */
  public static Calendar getToday() {
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);

    return today;
  }

  /**
   * @param date any {@link Date} to see if it is from today or later or not.
   * @param todayMidnightInMillies the time of the day at midnight.
   * @return <code>true</code> if the given {@link Date} is from today or later
   * as provided by the today parameter and <code>false</code> otherwise.
   */
  public static boolean isAfterIncludingToday(Date date, long todayMidnightInMillies) {
    long time = date.getTime();
    return todayMidnightInMillies <= time;
  }

  /**
   * Returns the first Date-Field from the given News that is not NULL. Tries
   * Modified-Date, Publish-Date, and Received-Date. The latter one is never
   * NULL so this Method will never return NULL at all.
   *
   * @param news The News to get the Date from.
   * @return Either Modified-Date, Publish-Date or Received-Date if the formers
   * are NULL.
   */
  public static Date getRecentDate(INews news) {
    if (news.getModifiedDate() != null)
      return news.getModifiedDate();

    if (news.getPublishDate() != null)
      return news.getPublishDate();

    return news.getReceiveDate();
  }

  /**
   * Works like getRecentData(INews news) with the difference of returning the
   * most recent date from a List of News.
   *
   * @param news A List of News to get the most recent Date from.
   * @return Either Modified-Date, Publish-Date or Received-Date from the most
   * recent News.
   */
  public static Date getRecentDate(List<INews> news) {
    Assert.isTrue(!news.isEmpty());

    Date mostRecentDate = null;
    for (INews newsitem : news) {
      Date date = getRecentDate(newsitem);
      if (mostRecentDate == null || date.after(mostRecentDate))
        mostRecentDate = date;
    }

    return mostRecentDate;
  }

  /**
   * Tries different date formats to parse against the given string
   * representation to retrieve a valid Date object.
   *
   * @param strdate Date as String
   * @return Date The parsed Date
   */
  public static Date parseDate(String strdate) {

    /* Return in case the string date is not set */
    if (strdate == null || strdate.length() == 0)
      return null;

    Date result = null;
    strdate = strdate.trim();
    if (strdate.length() > 10) {

      /* Open: deal with +4:00 (no zero before hour) */
      if ((strdate.substring(strdate.length() - 5).indexOf("+") == 0 || strdate.substring(strdate.length() - 5).indexOf("-") == 0) && strdate.substring(strdate.length() - 5).indexOf(":") == 2) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String sign = strdate.substring(strdate.length() - 5, strdate.length() - 4);
        strdate = strdate.substring(0, strdate.length() - 5) + sign + "0" + strdate.substring(strdate.length() - 4); //$NON-NLS-1$
      }

      String dateEnd = strdate.substring(strdate.length() - 6);

      /*
       * try to deal with -05:00 or +02:00 at end of date replace with -0500 or
       * +0200
       */
      if ((dateEnd.indexOf("-") == 0 || dateEnd.indexOf("+") == 0) && dateEnd.indexOf(":") == 3) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        if (!"GMT".equals(strdate.substring(strdate.length() - 9, strdate.length() - 6))) { //$NON-NLS-1$
          String oldDate = strdate;
          String newEnd = dateEnd.substring(0, 3) + dateEnd.substring(4);
          strdate = oldDate.substring(0, oldDate.length() - 6) + newEnd;
        }
      }
    }

    /* Try to parse the date */
    int i = 0;
    while (i < CUSTOM_DATE_FORMATS.length) {
      try {

        /*
         * This Block needs to be synchronized, because the parse-Method in
         * SimpleDateFormat is not Thread-Safe.
         */
        synchronized (CUSTOM_DATE_FORMATS[i]) {
          return CUSTOM_DATE_FORMATS[i].parse(strdate);
        }
      } catch (ParseException e) {
        i++;
      } catch (NumberFormatException e) {
        i++;
      }
    }
    return result;
  }

  /** Initialize the array of common date formats and formatter */
  static {

    /* Create Date Formats */
    final String[] possibleDateFormats = {

        /* RFC 1123 with 2-digit Year */
        "EEE, dd MMM yy HH:mm:ss z", //$NON-NLS-1$

        /* RFC 1123 with 4-digit Year */
        "EEE, dd MMM yyyy HH:mm:ss z", //$NON-NLS-1$

        /* RFC 1123 with no Timezone */
        "EEE, dd MMM yy HH:mm:ss", //$NON-NLS-1$

        /* Variant of RFC 1123 */
        "EEE, MMM dd yy HH:mm:ss", //$NON-NLS-1$

        /* RFC 1123 with no Seconds */
        "EEE, dd MMM yy HH:mm z", //$NON-NLS-1$

        /* Variant of RFC 1123 */
        "EEE dd MMM yyyy HH:mm:ss", //$NON-NLS-1$

        /* RFC 1123 with no Day */
        "dd MMM yy HH:mm:ss z", //$NON-NLS-1$

        /* RFC 1123 with no Day or Seconds */
        "dd MMM yy HH:mm z", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HH:mm:ssZ", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HH:mm:ss'Z'", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HH:mm:sszzzz", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HH:mm:ss z", //$NON-NLS-1$

        /* ISO 8601 */
        "yyyy-MM-dd'T'HH:mm:ssz", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HH:mm:ss.SSSz", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HHmmss.SSSz", //$NON-NLS-1$

        /* ISO 8601 slightly modified */
        "yyyy-MM-dd'T'HH:mm:ss", //$NON-NLS-1$

        /* ISO 8601 w/o seconds */
        "yyyy-MM-dd'T'HH:mmZ", //$NON-NLS-1$

        /* ISO 8601 w/o seconds */
        "yyyy-MM-dd'T'HH:mm'Z'", //$NON-NLS-1$

        /* RFC 1123 without Day Name */
        "dd MMM yyyy HH:mm:ss z", //$NON-NLS-1$

        /* RFC 1123 without Day Name and Seconds */
        "dd MMM yyyy HH:mm z", //$NON-NLS-1$

        /* Simple Date Format */
        "yyyy-MM-dd", //$NON-NLS-1$

        /* Simple Date Format */
        "MMM dd, yyyy" //$NON-NLS-1$
    };

    /* Create the dateformats */
    CUSTOM_DATE_FORMATS = new SimpleDateFormat[possibleDateFormats.length];

    for (int i = 0; i < possibleDateFormats.length; i++) {
      CUSTOM_DATE_FORMATS[i] = new SimpleDateFormat(possibleDateFormats[i], Locale.ENGLISH);
      CUSTOM_DATE_FORMATS[i].setTimeZone(TIMEZONE);
    }
  }
}