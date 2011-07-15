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

import org.rssowl.core.persist.IFolderChild;

import java.io.File;
import java.util.Collection;
import java.util.Set;

/**
 * This interface allows to contribute Exporters for {@link IFolderChild}. The
 * application is deciding which Exporter to use based on the name of the file
 * extension choosed.
 * <p>
 * Contributed via <code>org.rssowl.core.TypeExporter</code> Extension Point.
 * </p>
 *
 * @author bpasero
 */
public interface ITypeExporter {

  /** Options for the Export */
  public enum Options {

    /** Export Labels */
    EXPORT_LABELS,

    /** Export Filters */
    EXPORT_FILTERS,

    /** Export Settings */
    EXPORT_PREFERENCES;
  }

  /**
   * Perform Export of provided {@link IFolderChild} with {@link Options} to
   * consider.
   *
   * @param destination the destination {@link File} to export to.
   * @param elements the list of {@link IFolderChild} to export.
   * @param options a set of {@link Options} for the export or <code>null</code>
   * if none.
   * @throws InterpreterException in case of an exception during export.
   */
  public void exportTo(File destination, Collection<? extends IFolderChild> elements, Set<Options> options) throws InterpreterException;
}