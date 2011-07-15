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
import org.rssowl.core.persist.IFeed;

/**
 * This interface allows to contribute Interpreters for various XML Formats. The
 * application is deciding which Interpreter to use based on the name of the
 * root Element of the XML.
 * <p>
 * Contributed via <code>org.rssowl.core.FormatInterpreter</code> Extension
 * Point.
 * </p>
 *
 * @author bpasero
 */
public interface IFormatInterpreter {

  /**
   * Interpret the given <code>org.jdom.Document</code> as Feed.
   *
   * @param document The Document to be interpreted as Feed.
   * @param feed An instanceof IFeed that stores the interpreted Feed.
   * @throws InterpreterException Checked Exception to be used in case of any
   * Exception.
   */
  void interpret(Document document, IFeed feed) throws InterpreterException;
}