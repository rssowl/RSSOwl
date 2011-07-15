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

import org.rssowl.core.persist.ITextInput;
import org.rssowl.core.persist.reference.FeedReference;

import java.net.URI;

/**
 * Adapter Implementation of this Type. Methods of interest should be overriden
 * to keep the Data.
 *
 * @author bpasero
 */
public class TextInputAdapter extends Persistable implements ITextInput {

  /**
   * This Type is not used in the model implementation
   */
  public TextInputAdapter() {}

  /*
   * @see org.rssowl.core.model.types.ITextInput#setTitle(java.lang.String)
   */
  public void setTitle(String title) {}

  /*
   * @see org.rssowl.core.model.types.ITextInput#setDescription(java.lang.String)
   */
  public void setDescription(String description) {}

  /*
   * @see org.rssowl.core.model.types.ITextInput#setName(java.lang.String)
   */
  public void setName(String name) {}

  /*
   * @see org.rssowl.core.model.types.ITextInput#setLink(java.lang.String)
   */
  public void setLink(URI link) {}

  /*
   * @see org.rssowl.core.model.types.ITextInput#getDescription()
   */
  public String getDescription() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ITextInput#getLink()
   */
  public URI getLink() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ITextInput#getName()
   */
  public String getName() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ITextInput#getTitle()
   */
  public String getTitle() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ITextInput#getFeed()
   */
  public FeedReference getFeed() {
    return null;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "The TextInput Type is not handled by the Implementation yet)"; //$NON-NLS-1$
  }
}