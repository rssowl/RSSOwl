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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.rssowl.core.persist.IPersistable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The abstract super-type of all Model Objects that are persisted. It offers
 * the following features:
 * <ul>
 * <li>Lookup for <code>org.eclipse.core.runtime.IAdaptable</code> Adapters
 * using the Platform's Adapter Registry. This allows dynamic API extension on
 * Runtime without touching the Model Object's source.</li>
 * </ul>
 *
 * @author bpasero
 */
public abstract class Persistable implements IPersistable {

  /**
   * Default constructor provided for deserialization purposes.
   */
  protected Persistable() {}

  /**
   * If <code>uri</code> is <code>null</code>, returns <code>null</code>.
   * Otherwise, tries to create a URI from <code>uri</code> and throws an
   * IllegalStateException if this fails.
   *
   * @param uri
   * @return a URI created from <code>uri</code>.
   * @throws IllegalStateException
   */
  protected final URI createURI(String uri) {
    if (uri == null)
      return null;

    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Somehow an invalid URI was stored with the value of '" + uri + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Returns the text representation of <code>uri</code> if <code>uri</code>
   * is not <code>null</code>. Returns <code>null</code> otherwise.
   *
   * @param uri
   * @return the text representation of <code>uri</code> or <code>null</code>.
   */
  protected final String getURIText(URI uri) {
    return uri == null ? null : uri.toString();
  }

  /**
   * Copies the contents of <code>listMergeResult</code> into
   * <code>mergeResult</code> and returns whether there were any structural
   * changes in <code>listMergeResult</code>.
   *
   * @param mergeResult
   * @param listMergeResult
   * @return <code>true</code> if there were structural changes in
   * <code>listMergeResult</code>. Returns <code>false</code> otherwise.
   */
  protected final boolean processListMergeResult(MergeResult mergeResult, ComplexMergeResult< ? > listMergeResult) {
    mergeResult.addAll(listMergeResult);
    if (listMergeResult.isStructuralChange())
      return true;

    return false;
  }

  /**
   * Returns an object which is an instance of the given class associated with
   * this object. Returns <code>null</code> if no such object can be found.
   * <p>
   * This implementation of the method declared by <code>IAdaptable</code>
   * passes the request along to the platform's adapter manager; roughly
   * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
   * Subclasses may override this method (however, if they do so, they should
   * invoke the method on their superclass to ensure that the Platform's adapter
   * manager is consulted).
   * </p>
   *
   * @param adapter the class to adapt to
   * @return the adapted object or <code>null</code>
   * @see IAdaptable#getAdapter(Class)
   * @see Platform#getAdapterManager()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }
}