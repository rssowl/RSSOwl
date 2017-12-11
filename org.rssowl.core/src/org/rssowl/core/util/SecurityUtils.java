/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

import java.security.Security;

public class SecurityUtils {

  public static void setUnlimitedSecurity() {
    //fixes problems with encrypted of rss feeds where the server uses stronger encryption
    //than the java client supports "Due to import control restrictions of some countries"
    //javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure

    //setting the property only works for later patches of jre versions >= 6
    //older patches need to install Java Cryptography Extension (JCE)
    //JCE works also for later versions if available
    //alternatively one can set the same property manually in
    //the installed JRE: jre\lib\security\java.security
    try {
      Security.setProperty("crypto.policy", "unlimited"); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
