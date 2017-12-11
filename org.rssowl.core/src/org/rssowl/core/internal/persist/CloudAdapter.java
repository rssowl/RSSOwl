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

import org.rssowl.core.persist.ICloud;
import org.rssowl.core.persist.reference.FeedReference;

/**
 * Adapter Implementation of this Type. Methods of interest should be overriden
 * to keep the Data.
 *
 * @author bpasero
 */
public class CloudAdapter extends Persistable implements ICloud {

  /**
   * This Type is not used in the implementation
   */
  public CloudAdapter() {}

  /*
   * @see org.rssowl.core.model.types.ICloud#setDomain(java.lang.String)
   */
  @Override
  public void setDomain(String domain) {}

  /*
   * @see org.rssowl.core.model.types.ICloud#setPort(int)
   */
  @Override
  public void setPort(int port) {}

  /*
   * @see org.rssowl.core.model.types.ICloud#setPath(java.lang.String)
   */
  @Override
  public void setPath(String path) {}

  /*
   * @see org.rssowl.core.model.types.ICloud#setRegisterProcedure(java.lang.String)
   */
  @Override
  public void setRegisterProcedure(String registerProcedure) {}

  /*
   * @see org.rssowl.core.model.types.ICloud#setProtocol(java.lang.String)
   */
  @Override
  public void setProtocol(String protocol) {}

  /*
   * @see org.rssowl.core.model.types.ICloud#getDomain()
   */
  @Override
  public String getDomain() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ICloud#getPath()
   */
  @Override
  public String getPath() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ICloud#getPort()
   */
  @Override
  public int getPort() {
    return 0;
  }

  /*
   * @see org.rssowl.core.model.types.ICloud#getProtocol()
   */
  @Override
  public String getProtocol() {
    return null;
  }

  /*
   * @see org.rssowl.core.interpreter.types.ICloud#getRegisterProcedure()
   */
  @Override
  public String getRegisterProcedure() {
    return null;
  }

  /*
   * @see org.rssowl.core.model.types.ICloud#getFeed()
   */
  @Override
  public FeedReference getFeed() {
    return null;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return super.toString() + "The Cloud Type is not handled by the Implementation yet)"; //$NON-NLS-1$
  }
}