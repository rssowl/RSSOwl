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

package org.rssowl.core.tests.model;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.rssowl.core.internal.persist.service.DBManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to enable and disable the large block size option for the database.
 * Tests that extend this class must run twice to have any effect.
 */
public class LargeBlockSizeTest {
  private static final AtomicBoolean ENABLE_LARGE_BLOCK_SIZE = new AtomicBoolean(false);

  @BeforeClass
  public static void beforeClass() throws IOException {
    if (!ENABLE_LARGE_BLOCK_SIZE.get())
      ENABLE_LARGE_BLOCK_SIZE.set(true);
    else {
      File largeBlockSizeMarkerFile = DBManager.getLargeBlockSizeMarkerFile();
      largeBlockSizeMarkerFile.createNewFile();
      ENABLE_LARGE_BLOCK_SIZE.set(false);
    }
  }

  @AfterClass
  public static void afterClass() {
    File largeBlockSizeMarkerFile = DBManager.getLargeBlockSizeMarkerFile();
    if (largeBlockSizeMarkerFile.exists())
      largeBlockSizeMarkerFile.delete();
  }
}