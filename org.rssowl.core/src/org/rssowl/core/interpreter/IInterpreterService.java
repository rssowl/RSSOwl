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
import org.rssowl.core.internal.interpreter.json.JSONObject;
import org.rssowl.core.interpreter.ITypeExporter.Options;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolderChild;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides access to the interpreter service of RSSOwl. This service provides
 * API to convert a stream of data into a model representation. In the common
 * case of a XML stream this involves using a XML-Parser and creating the model
 * out of the content. Various extension points allow to customize the behavor
 * of the interpreter:
 * <ul>
 * <li>Contribute a new format interpreter using the FormatInterpreter extension
 * point. This allows to display any XML in RSSOwl as Feed.</li>
 * <li>Contribute a new namespace handler using the NamespaceHandler extension
 * point. This allows to properly handle any new namespace in RSSOwl.</li>
 * <li>Contribute a new element handler using the ElementHandler extension
 * point. This makes RSSOwl understand new elements or even attributes.</li>
 * <li>Contribute a new xml parser using the XMLParser extension point if you
 * are not happy with the default one.</li>
 * </ul>
 *
 * @author bpasero
 * @see IFormatInterpreter
 * @see IElementHandler
 * @see INamespaceHandler
 * @see IXMLParser
 */
public interface IInterpreterService {

  /**
   * Parse the given InputStream into a <code>org.jdom.Document</code> and
   * delegate the Interpretation to the contributed FormatInterpreters.
   *
   * @param inS The InputStream to Interpret as <code>IFeed</code>.
   * @param feed An instance of <code>IFeed</code> that stores the interpretion.
   * @param properties a map of properties to configure interpretion or
   * <code>null</code> if none.
   * @throws ParserException In case of an Error while Parsing.
   * @throws InterpreterException In case of an Error while Interpreting.
   */
  void interpret(InputStream inS, IFeed feed, Map<Object, Object> properties) throws ParserException, InterpreterException;

  /**
   * Interpret the given <code>org.w3c.dom.Document</code> as Feed by delegating
   * the Interpretation to the contributed FormatInterpreters.
   *
   * @param w3cDocument The Document to interpret as <code>IFeed</code>.
   * @param feed An instance of <code>IFeed</code> that stores the interpretion.
   * @throws InterpreterException In case of an Error while Interpreting.
   */
  void interpretW3CDocument(org.w3c.dom.Document w3cDocument, IFeed feed) throws InterpreterException;

  /**
   * Interpret the given <code>org.jdom.Document</code> as Feed by delegating
   * the Interpretation to the contributed FormatInterpreters.
   *
   * @param document The Document to interpret as <code>IFeed</code>.
   * @param feed An instance of <code>IFeed</code> that stores the interpretion.
   * @throws InterpreterException In case of an Error while Interpreting.
   */
  void interpretJDomDocument(Document document, IFeed feed) throws InterpreterException;

  /**
   * Interpret the given {@link JSONObject} as Feed.
   *
   * @param json The {@link JSONObject} to interpret as <code>IFeed</code>.
   * @param feed An instance of <code>IFeed</code> that stores the interpretion.
   * @throws InterpreterException In case of an Error while Interpreting.
   */
  void interpretJSONObject(JSONObject json, IFeed feed) throws InterpreterException;

  /**
   * Imports the given Document as OPML into Types and returns them.
   *
   * @param inS The InputStream to Interpret as Document.
   * @return Returns the Types imported from the Document.
   * @throws InterpreterException In case of an Error while Interpreting.
   * @throws ParserException In case of an Error while Parsing.
   */
  List<IEntity> importFrom(InputStream inS) throws InterpreterException, ParserException;

  /**
   * Perform Export of provided {@link IFolderChild} with {@link Options} to
   * consider.
   *
   * @param destination the destination {@link File} to export to.
   * @param elements the list of {@link IFolderChild} to export.
   * @param options a set of {@link Options} for the export.
   * @throws InterpreterException in case of an exception during export.
   */
  void exportTo(File destination, Collection<? extends IFolderChild> elements, Set<Options> options) throws InterpreterException;

  /**
   * Returns a {@link List} of all formats supported for an export.
   *
   * @return a {@link List} of all formats supported for an export.
   */
  Collection<String> getExportFormats();

  /**
   * Get the Namespace Handler for the given Namespace or NULL if not
   * contributed.
   *
   * @param namespaceUri The Namespace URI as String.
   * @return The Namespace Handler for the given Namespace or NULL if not
   * contributed.
   */
  INamespaceHandler getNamespaceHandler(String namespaceUri);

  /**
   * Get the Element Handler for the given Element and Namespace or NULL if not
   * contributed.
   *
   * @param elementName The Name of the Element.
   * @param rootName The Name of the root element of the used Format.
   * @return The Namespace Handler for the given Namespace or NULL if not
   * contributed.
   */
  IElementHandler getElementHandler(String elementName, String rootName);
}