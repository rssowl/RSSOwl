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

package org.rssowl.core.interpreter;

import org.jdom.Element;
import org.rssowl.core.persist.IPersistable;

/**
 * The Element Handler allows to contribute processing of Elements for a Format.
 * Use this mechanism if you want to interpret custom Elements or override the
 * processing of standard ones.
 * <p>
 * Note: Use this Handler with caution. It will override the applications
 * Element-Handler for the given Format.
 * </p>
 * <p>
 * Contributed via <code>org.rssowl.core.ElementHandler</code> Extension Point.
 * </p>
 *
 * @author bpasero
 */
public interface IElementHandler {

  /**
   * This Method is called whenever an Element of the given Format has been
   * reached that has the defined Name. The type-parameter is the current
   * Interpreter-Model at the time the Element was reached.
   *
   * @param element The Element to process.
   * @param type The Interpreter Type the given Element belongs to.
   */
  void processElement(Element element, IPersistable type);
}