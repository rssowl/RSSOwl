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
 * Instances of <code>Triple</code> combine three Objects in a single Object.
 * This can be useful in situations where multiple return-values are required
 * from a method.
 *
 * @author bpasero
 * @param <F> The Class of the First Object.
 * @param <S> The Class of the Second Object.
 * @param <T> The Class of the Third Object.
 */
public final class Triple<F, S, T> {
  private final F fFirst;
  private final S fSecond;
  private final T fThird;

  private Triple(F first, S second, T third) {
    fFirst = first;
    fSecond = second;
    fThird = third;
  }

  /**
   * Creates a new <code>Triple</code> from the given Objects.
   *
   * @param first The first Object of the new Triple.
   * @param second The second Object of the new Triple.
   * @param third The third Object of the new Triple.
   * @param <F> The Class of the First Object.
   * @param <S> The Class of the Second Object.
   * @param <T> The Class of the Third Object.
   * @return Returns a new <code>Triple</code> from the given Objects.
   */
  public static <F, S, T> Triple<F, S, T> create(F first, S second, T third) {
    return new Triple<F, S, T>(first, second, third);
  }

  /**
   * @return Returns the first Object of this Triple.
   */
  public final F getFirst() {
    return fFirst;
  }

  /**
   * @return Returns the second Object of this Triple.
   */
  public final S getSecond() {
    return fSecond;
  }

  /**
   * @return Returns the third Object of this Triple.
   */
  public T getThird() {
    return fThird;
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
    result = prime * result + ((fThird == null) ? 0 : fThird.hashCode());
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

    Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
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

    if (fThird == null) {
      if (other.fThird != null)
        return false;
    } else if (!fThird.equals(other.fThird))
      return false;

    return true;
  }
}