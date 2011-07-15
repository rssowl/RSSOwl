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

import org.rssowl.core.persist.IGuid;

/**
 * Immutable and thread-safe implementation of IGuid.
 *
 * @author bpasero
 */
public final class Guid extends Persistable implements IGuid {
  private final String fValue;
  private final boolean fIsPermaLink;

  /**
   * @param value The unique identifier.
   * @param isPermaLink indicates whether this guid is a permalink to the item.
   * {@code null} indicates that the feed had no permaLink attribute. See
   * {@link #isPermaLink()} for more information.
   */
  public Guid(String value, Boolean isPermaLink) {
    fValue = value;
    if (isPermaLink == null)
      fIsPermaLink = true;
    else
      fIsPermaLink = isPermaLink.booleanValue();
  }

  /*
   * @see org.rssowl.core.model.types.IGuid#isPermaLink()
   */
  public boolean isPermaLink() {
    return fIsPermaLink;
  }

  /*
   * @see org.rssowl.core.model.types.IGuid#getValue()
   */
  public String getValue() {
    return fValue;
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + (fIsPermaLink ? 1231 : 1237);
    result = PRIME * result + ((fValue == null) ? 0 : fValue.hashCode());
    return result;
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param guid to be compared.
   * @return whether this object and <code>guid</code> are identical. It
   * compares all the fields.
   */
  @Override
  public boolean equals(Object guid) {
    if (this == guid)
      return true;

    if (!(guid instanceof Guid))
      return false;

    synchronized (guid) {
      Guid g = (Guid) guid;

      return (fValue == null ? g.fValue == null : fValue.equals(g.fValue)) && fIsPermaLink == g.isPermaLink();
    }
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "Value = " + fValue + ", IsPermaLink = " + fIsPermaLink + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}