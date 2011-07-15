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

import org.jdom.Document;

import java.io.InputStream;
import java.util.Map;

/**
 * This interface allows to contribute the XML-Parser that is to be used. It is
 * also responsible to parse a given InputStream into an instance of
 * <code>org.jdom.Document</code>.
 * <p>
 * Contributed via <code>org.rssowl.core.XMLParser</code> Extension Point.
 * </p>
 *
 * @author bpasero
 */
public interface IXMLParser {

  /**
   * Called prior usage of the XML-Parser. For example, load the Class of the
   * used Parser here and setup some properties.
   *
   * @throws ParserException Checked Exception to be used in case of any
   * Exception.
   */
  void init() throws ParserException;

  /**
   * Parse the given InputStream and return the an instance of
   * <code>org.jdom.Document</code>.
   *
   * @param inS The InputStream to parse.
   * @param properties a map of properties to configure parsing or
   * <code>null</code> if none.
   * @return An instance of <code>org.jdom.Document</code> as parsed
   * InputStream.
   * @throws ParserException Checked Exception to be used in case of any
   * Exception.
   */
  Document parse(InputStream inS, Map<Object, Object> properties) throws ParserException;
}