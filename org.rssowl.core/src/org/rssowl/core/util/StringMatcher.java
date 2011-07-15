/* *****************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * *****************************************************************************/

package org.rssowl.core.util;

import java.util.Vector;

/**
 * A string pattern matcher, suppporting "*" and "?" wildcards.
 */
public class StringMatcher {
  private final String fPattern;
  private final int fLength; // pattern length
  private final boolean fIgnoreWildCards;
  private final boolean fIgnoreCase;
  private boolean fHasLeadingStar;
  private boolean fHasTrailingStar;
  private String fSegments[]; //the given pattern is split into * separated segments

  /* boundary value beyond which we don't need to search in the text */
  private int fBound = 0;

  private static final char fSingleWildCard = '\u0000';

  /**
   * StringMatcher constructor takes in a String object that is a simple pattern
   * which may contain '*' for 0 and many characters and '?' for exactly one
   * character. Literal '*' and '?' characters must be escaped in the pattern
   * e.g., "\*" means literal "*", etc. Escaping any other character (including
   * the escape character itself), just results in that character in the
   * pattern. e.g., "\a" means "a" and "\\" means "\" If invoking the
   * StringMatcher with string literals in Java, don't forget escape characters
   * are represented by "\\".
   *
   * @param pattern the pattern to match text against
   * @param ignoreCase if true, case is ignored
   * @param ignoreWildCards if true, wild cards and their escape sequences are
   * ignored (everything is taken literally).
   */
  public StringMatcher(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
    if (pattern == null) {
      throw new IllegalArgumentException();
    }
    fIgnoreCase = ignoreCase;
    fIgnoreWildCards = ignoreWildCards;
    fPattern = pattern;
    fLength = pattern.length();

    if (fIgnoreWildCards) {
      parseNoWildCards();
    } else {
      parseWildCards();
    }
  }

  /**
   * match the given <code>text</code> with the pattern
   *
   * @return true if matched eitherwise false
   * @param text a String object
   */
  public boolean match(String text) {
    if (text == null) {
      return false;
    }
    return match(text, 0, text.length());
  }

  /**
   * Given the starting (inclusive) and the ending (exclusive) positions in the
   * <code>text</code>, determine if the given substring matches with aPattern
   *
   * @return true if the specified portion of the text matches the pattern
   * @param text a String object that contains the substring to match
   * @param start marks the starting position (inclusive) of the substring
   * @param end marks the ending index (exclusive) of the substring
   */
  public boolean match(String text, int start, int end) {
    if (null == text) {
      throw new IllegalArgumentException();
    }

    if (start > end) {
      return false;
    }

    if (fIgnoreWildCards) {
      return (end - start == fLength) && fPattern.regionMatches(fIgnoreCase, 0, text, start, fLength);
    }
    int segCount = fSegments.length;
    if (segCount == 0 && (fHasLeadingStar || fHasTrailingStar)) {
      return true;
    }
    if (start == end) {
      return fLength == 0;
    }
    if (fLength == 0) {
      return start == end;
    }

    int tlen = text.length();
    if (start < 0) {
      start = 0;
    }
    if (end > tlen) {
      end = tlen;
    }

    int tCurPos = start;
    int bound = end - fBound;
    if (bound < 0) {
      return false;
    }
    int i = 0;
    String current = fSegments[i];
    int segLength = current.length();

    /* process first segment */
    if (!fHasLeadingStar) {
      if (!regExpRegionMatches(text, start, current, 0, segLength))
        return false;
      ++i;
      tCurPos = tCurPos + segLength;
    }
    if ((fSegments.length == 1) && (!fHasLeadingStar) && (!fHasTrailingStar)) {
      // only one segment to match, no wildcards specified
      return tCurPos == end;
    }
    /* process middle segments */
    while (i < segCount) {
      current = fSegments[i];
      int currentMatch;
      int k = current.indexOf(fSingleWildCard);
      if (k < 0) {
        currentMatch = textPosIn(text, tCurPos, end, current);
        if (currentMatch < 0) {
          return false;
        }
      } else {
        currentMatch = regExpPosIn(text, tCurPos, end, current);
        if (currentMatch < 0) {
          return false;
        }
      }
      tCurPos = currentMatch + current.length();
      i++;
    }

    /* process final segment */
    if (!fHasTrailingStar && tCurPos != end) {
      int clen = current.length();
      return regExpRegionMatches(text, end - clen, current, 0, clen);
    }
    return i == segCount;
  }

  /**
   * This method parses the given pattern into segments seperated by wildcard
   * '*' characters. Since wildcards are not being used in this case, the
   * pattern consists of a single segment.
   */
  private void parseNoWildCards() {
    fSegments = new String[1];
    fSegments[0] = fPattern;
    fBound = fLength;
  }

  /**
   * Parses the given pattern into segments seperated by wildcard '*'
   * characters.
   *
   * @param p a String object that is a simple regular expression with '*'
   * and/or '?'
   */
  @SuppressWarnings("unchecked")
  private void parseWildCards() {
    if (fPattern.startsWith("*")) { //$NON-NLS-1$
      fHasLeadingStar = true;
    }
    if (fPattern.endsWith("*")) {//$NON-NLS-1$
      /* make sure it's not an escaped wildcard */
      if (fLength > 1 && fPattern.charAt(fLength - 2) != '\\') {
        fHasTrailingStar = true;
      }
    }

    Vector temp = new Vector();

    int pos = 0;
    StringBuilder buf = new StringBuilder();
    while (pos < fLength) {
      char c = fPattern.charAt(pos++);
      switch (c) {
        case '\\':
          if (pos >= fLength) {
            buf.append(c);
          } else {
            char next = fPattern.charAt(pos++);
            /* if it's an escape sequence */
            if (next == '*' || next == '?' || next == '\\') {
              buf.append(next);
            } else {
              /* not an escape sequence, just insert literally */
              buf.append(c);
              buf.append(next);
            }
          }
          break;
        case '*':
          if (buf.length() > 0) {
            /* new segment */
            temp.addElement(buf.toString());
            fBound += buf.length();
            buf.setLength(0);
          }
          break;
        case '?':
          /* append special character representing single match wildcard */
          buf.append(fSingleWildCard);
          break;
        default:
          buf.append(c);
      }
    }

    /* add last buffer to segment list */
    if (buf.length() > 0) {
      temp.addElement(buf.toString());
      fBound += buf.length();
    }

    fSegments = new String[temp.size()];
    temp.copyInto(fSegments);
  }

  /**
   * @param text a string which contains no wildcard
   * @param start the starting index in the text for search, inclusive
   * @param end the stopping point of search, exclusive
   * @return the starting index in the text of the pattern , or -1 if not found
   */
  protected int posIn(String text, int start, int end) {//no wild card in pattern
    int max = end - fLength;

    if (!fIgnoreCase) {
      int i = text.indexOf(fPattern, start);
      if (i == -1 || i > max) {
        return -1;
      }
      return i;
    }

    for (int i = start; i <= max; ++i) {
      if (text.regionMatches(true, i, fPattern, 0, fLength)) {
        return i;
      }
    }

    return -1;
  }

  /**
   * @param text a simple regular expression that may only contain '?'(s)
   * @param start the starting index in the text for search, inclusive
   * @param end the stopping point of search, exclusive
   * @param p a simple regular expression that may contains '?'
   * @return the starting index in the text of the pattern , or -1 if not found
   */
  protected int regExpPosIn(String text, int start, int end, String p) {
    int plen = p.length();

    int max = end - plen;
    for (int i = start; i <= max; ++i) {
      if (regExpRegionMatches(text, i, p, 0, plen)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @return boolean
   * @param text a String to match
   * @param tStart int that indicates the starting index of match, inclusive
   * @param p String, String, a simple regular expression that may contain '?'
   * @param pStart
   * @param plen
   */
  protected boolean regExpRegionMatches(String text, int tStart, String p, int pStart, int plen) {
    while (plen-- > 0) {
      char tchar = text.charAt(tStart++);
      char pchar = p.charAt(pStart++);

      /* process wild cards */
      if (!fIgnoreWildCards) {
        /* skip single wild cards */
        if (pchar == fSingleWildCard) {
          continue;
        }
      }
      if (pchar == tchar) {
        continue;
      }
      if (fIgnoreCase) {
        if (Character.toUpperCase(tchar) == Character.toUpperCase(pchar)) {
          continue;
        }
        // comparing after converting to upper case doesn't handle all cases;
        // also compare after converting to lower case
        if (Character.toLowerCase(tchar) == Character.toLowerCase(pchar)) {
          continue;
        }
      }
      return false;
    }
    return true;
  }

  /**
   * @param text the string to match
   * @param start the starting index in the text for search, inclusive
   * @param end the stopping point of search, exclusive
   * @param p a string that has no wildcard
   * @return the starting index in the text of the pattern , or -1 if not found
   */
  protected int textPosIn(String text, int start, int end, String p) {

    int plen = p.length();
    int max = end - plen;

    if (!fIgnoreCase) {
      int i = text.indexOf(p, start);
      if (i == -1 || i > max) {
        return -1;
      }
      return i;
    }

    for (int i = start; i <= max; ++i) {
      if (text.regionMatches(true, i, p, 0, plen)) {
        return i;
      }
    }

    return -1;
  }
}