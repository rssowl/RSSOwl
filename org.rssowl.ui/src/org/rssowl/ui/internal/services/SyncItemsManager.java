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

package org.rssowl.ui.internal.services;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.util.SyncItem;
import org.rssowl.ui.internal.Activator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link SyncItemsManager} serializes and deserializes uncommitted
 * {@link SyncItem} to the file system.
 *
 * @author bpasero
 */
public class SyncItemsManager {

  /* File of uncommitted sync items */
  private static final String UNCOMMITTED_SYNCITEMS_FILE = "syncitems"; //$NON-NLS-1$

  private Map<String, SyncItem> fItems = new HashMap<String, SyncItem>();
  private final Object fLock = new Object();

  /**
   * Deserialize Items from Filesystem
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void startup() throws IOException, ClassNotFoundException {
    synchronized (fLock) {
      fItems = deserializeSyncItems();
    }
  }

  /**
   * Serialize Items to Filesystem
   *
   * @throws IOException
   */
  public void shutdown() throws IOException {
    synchronized (fLock) {
      serializeSyncItems(fItems);
      fItems.clear();
    }
  }

  /**
   * @param items the uncommitted {@link SyncItem} to add.
   */
  public void addUncommitted(Collection<SyncItem> items) {
    synchronized (fLock) {
      for (SyncItem item : items) {
        SyncItem existingItem = fItems.get(item.getId());
        if (existingItem != null)
          existingItem.merge(item);
        else
          fItems.put(item.getId(), item);
      }
    }
  }

  /**
   * @return all uncommitted {@link SyncItem}.
   */
  public Map<String, SyncItem> getUncommittedItems() {
    synchronized (fLock) {
      if (fItems.isEmpty())
        return Collections.emptyMap();

      return new HashMap<String, SyncItem>(fItems);
    }
  }

  /**
   * @return <code>true</code> if there are uncommitted {@link SyncItem}.
   */
  public boolean hasUncommittedItems() {
    synchronized (fLock) {
      return !fItems.isEmpty();
    }
  }

  /**
   * Removes all uncommitted {@link SyncItem}.
   */
  public void clearUncommittedItems() {
    synchronized (fLock) {
      fItems.clear();
    }
  }

  /**
   * @param items the uncommitted {@link SyncItem} to remove.
   */
  public void removeUncommitted(Collection<SyncItem> items) {
    synchronized (fLock) {
      for (SyncItem item : items) {
        fItems.remove(item.getId());
      }
    }
  }

  /**
   * @param item the uncommitted {@link SyncItem} to remove.
   */
  public void removeUncommitted(SyncItem item) {
    synchronized (fLock) {
      fItems.remove(item.getId());
    }
  }

  private void serializeSyncItems(Map<String, SyncItem> items) throws IOException {
    File store = getUncommittedSyncItemsFile();
    if (store == null)
      return;

    if (store.exists() && !store.delete())
      throw new IOException(NLS.bind("Synchronization: Unable to delete file ''{0}''", store.toString())); //$NON-NLS-1$

    if (fItems.isEmpty())
      return;

    ObjectOutputStream outS = null;
    try {
      outS = new ObjectOutputStream(new FileOutputStream(store));
      outS.writeObject(items);
    } finally {
      if (outS != null)
        outS.close();
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, SyncItem> deserializeSyncItems() throws IOException, ClassNotFoundException {
    Map<String, SyncItem> items = new HashMap<String, SyncItem>();

    File store = getUncommittedSyncItemsFile();
    if (store == null || !store.exists())
      return items;

    ObjectInputStream inS = null;
    try {
      inS = new ObjectInputStream(new FileInputStream(store));

      Object obj = inS.readObject();
      if (obj instanceof Map)
        items.putAll((Map) obj);
    } finally {
      if (inS != null)
        inS.close();
    }

    return items;
  }

  private File getUncommittedSyncItemsFile() throws IOException {
    Activator activator = Activator.getDefault();
    if (activator == null)
      return null;

    IPath path = new Path(activator.getStateLocation().toOSString());

    File bundleRoot = new File(path.toOSString());
    if (!bundleRoot.exists() && !bundleRoot.mkdir())
      throw new IOException(NLS.bind("Synchronization: Unable to create folder ''{0}''", bundleRoot.toString())); //$NON-NLS-1$

    path = path.append(UNCOMMITTED_SYNCITEMS_FILE);

    return new File(path.toOSString());
  }
}