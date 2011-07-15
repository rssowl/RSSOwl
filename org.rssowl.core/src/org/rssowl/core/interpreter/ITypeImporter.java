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
import org.rssowl.core.persist.IEntity;

import java.util.List;

/**
 * This interface allows to contribute Importers for various XML Formats. The
 * application is deciding which Importer to use based on the name of the root
 * Element of the XML.
 * <p>
 * Contributed via <code>org.rssowl.core.TypeImporter</code> Extension Point.
 * </p>
 *
 * @author bpasero
 */
public interface ITypeImporter {

  /** Key to store the actual ID of an {@link IEntity} if required */
  public static final String ID_KEY = "org.rssowl.core.interpreter.typeimporter.EntityId"; //$NON-NLS-1$

  /** Key to store extra data with the {@link IEntity} if required */
  public static final String DATA_KEY = "org.rssowl.core.interpreter.typeimporter.DataId"; //$NON-NLS-1$

  /**
   * Key to store a boolean indicating that a Folder was only created as
   * temporary container
   */
  public static final String TEMPORARY_FOLDER = "org.rssowl.core.interpreter.typeimporter.TemporaryFolder"; //$NON-NLS-1$

  /** Key to store the Homepage with the {@link IEntity} */
  public static final String HOMEPAGE_KEY = "org.rssowl.core.interpreter.typeimporter.Homepage"; //$NON-NLS-1$

  /** Key to store the Description with the {@link IEntity} */
  public static final String DESCRIPTION_KEY = "org.rssowl.core.interpreter.typeimporter.Description"; //$NON-NLS-1$

  /**
   * Import a Type from the given Document. A very common usecase is importing
   * an <code>IFolder</code> from an OPML or other XML Document.
   *
   * @param document The document to import a Type from.
   * @return Returns the Types imported from the Document.
   * @throws InterpreterException Checked Exception to be used in case of any
   * Exception.
   */
  List<IEntity> importFrom(Document document) throws InterpreterException;
}