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

/**
 * Instances of <code>Pair</code> combine two Objects in a single Object. This
 * can be useful in situations where multiple return-values are required from a
 * method.
 *
 * @author bpasero
 * @param <F> The Class of the First Object.
 * @param <S> The Class of the Second Object.
 */
public final class Pair<F, S> {
  private final F fFirst;
  private final S fSecond;

  private Pair(F first, S second) {
    fFirst = first;
    fSecond = second;
  }

  /**
   * Creates a new <code>Pair</code> from the given Objects.
   *
   * @param first The first Object of the new Pair.
   * @param second The second Object of the new Pair.
   * @param <F> The Class of the First Object.
   * @param <S> The Class of the Second Object.
   * @return Returns a new <code>Pair</code> from the given Objects.
   */
  public static <F, S> Pair<F, S> create(F first, S second) {
    return new Pair<F, S>(first, second);
  }

  /**
   * @return Returns the first Object of this Pair.
   */
  public final F getFirst() {
    return fFirst;
  }

  /**
   * @return Returns the second Object of this Pair.
   */
  public final S getSecond() {
    return fSecond;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fFirst == null) ? 0 : fFirst.hashCode());
    result = prime * result + ((fSecond == null) ? 0 : fSecond.hashCode());
    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    final Pair<?, ?> other = Pair.class.cast(obj);
    if (fFirst == null) {
      if (other.fFirst != null)
        return false;
    } else if (!fFirst.equals(other.fFirst))
      return false;

    if (fSecond == null) {
      if (other.fSecond != null)
        return false;
    } else if (!fSecond.equals(other.fSecond))
      return false;

    return true;
  }
}