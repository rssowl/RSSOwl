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

import org.jdom.Attribute;
import org.jdom.Element;
import org.rssowl.core.persist.IPersistable;

/**
 * The Namespace Handler allows to contribute processing of Namespaces. Use this
 * mechanism if you want to interpret Elements or Attributes with Namespaces.
 * <p>
 * Contributed via <code>org.rssowl.core.NamespaceHandler</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface INamespaceHandler {

  /**
   * This Method is called whenever an Element has been reached that makes use
   * of the defined NamespaceURI. The type-parameter is the current
   * Interpreter-Model at the time the Element was reached.
   *
   * @param element The Element to process.
   * @param type The Interpreter Type the given Element belongs to.
   */
  void processElement(Element element, IPersistable type);

  /**
   * This Method is called whenever an Attribute has been reached that makes use
   * of the defined NamespaceURI. The type-parameter is the current
   * Interpreter-Model at the time the Attribute was reached.
   * <p>
   * Note: This Method is <em>only</em> called in case the Element this
   * Attribute is belonging to does not itself use a Namespace. In that case,
   * only the processElement-Method belonging to that Namespace is called.
   * </p>
   *
   * @param attribute The Attribute to process.
   * @param type The Interpreter Type the given Element belongs to.
   */
  void processAttribute(Attribute attribute, IPersistable type);
}