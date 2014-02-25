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

package org.rssowl.core.internal.persist.service;

import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.persist.AbstractEntity;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.ConditionalGet;
import org.rssowl.core.internal.persist.Description;
import org.rssowl.core.internal.persist.Feed;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.News;
import org.rssowl.core.internal.persist.Preference;
import org.rssowl.core.internal.persist.SearchFilter;
import org.rssowl.core.persist.NewsCounter;
import org.rssowl.core.util.CoreUtils;

import com.db4o.Db4o;
import com.db4o.config.Configuration;
import com.db4o.config.ObjectClass;
import com.db4o.config.ObjectField;
import com.db4o.config.QueryEvaluationMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public final class ConfigurationHelper {

  private static final int LARGE_DB_BLOCK_SIZE = 8;
  private static final String LARGE_BLOCK_SIZE_MARKER = "largeblocksize"; //$NON-NLS-1$
  private static final long LARGE_DB_STARTING_SIZE = 1610612736; //1.5 GB in Bytes

  /**
   * @param forDefrag forDefrag show if configuration intended for db during
   * defrag
   * @param moveToLargeBlockSize if true use large block size otherwise use default
   * method to determine db block size
   * @return com.db4o.config.Configuration used for db creation
   */
  public static Configuration createConfiguration(boolean forDefrag, boolean moveToLargeBlockSize) {
    Configuration config = Db4o.newConfiguration();

    //TODO We can use dbExists to configure our parameters for a more
    //efficient startup. For example, the following could be used. We'd have
    //to include a file when we need to evolve the schema or something similar
    //config.detectSchemaChanges(false)

    if (moveToLargeBlockSize) {
      //The DB WILL BE migrated to a larger block size
      config.blockSize(LARGE_DB_BLOCK_SIZE);
    } else if (getLargeBlockSizeMarkerFile().exists()) {
      //The DB has been migrated to a larger block size
      config.blockSize(LARGE_DB_BLOCK_SIZE);
    }

    config.setOut(new PrintStream(new ByteArrayOutputStream()) {
      @Override
      public void write(byte[] buf, int off, int len) {
        if (buf != null && len >= 0 && off >= 0 && off <= buf.length - len) {
          CoreUtils.appendLogMessage(new String(buf, off, len));
        }
      }
    });

    config.lockDatabaseFile(true);
    config.queries().evaluationMode(forDefrag ? QueryEvaluationMode.LAZY : QueryEvaluationMode.IMMEDIATE);
    config.automaticShutDown(false);
    config.callbacks(false);
    config.activationDepth(2);
    config.flushFileBuffers(false);
    config.callConstructors(true);
    config.exceptionsOnNotStorable(true);
    configureAbstractEntity(config);
    config.objectClass(BookMark.class).objectField("fFeedLink").indexed(true); //$NON-NLS-1$
    config.objectClass(ConditionalGet.class).objectField("fLink").indexed(true); //$NON-NLS-1$
    configureFeed(config);
    configureNews(config);
    configureFolder(config);
    config.objectClass(Description.class).objectField("fNewsId").indexed(true); //$NON-NLS-1$
    config.objectClass(NewsCounter.class).cascadeOnDelete(true);
    config.objectClass(Preference.class).cascadeOnDelete(true);
    config.objectClass(Preference.class).objectField("fKey").indexed(true); //$NON-NLS-1$
    config.objectClass(SearchFilter.class).objectField("fActions").cascadeOnDelete(true); //$NON-NLS-1$

    if (isIBM_VM_1_6()) {
      config.objectClass("java.util.MiniEnumSet").translate(new com.db4o.config.TSerializable()); //$NON-NLS-1$
    }

    return config;
  }

  private static void configureAbstractEntity(Configuration config) {
    ObjectClass abstractEntityClass = config.objectClass(AbstractEntity.class);
    ObjectField idField = abstractEntityClass.objectField("fId"); //$NON-NLS-1$
    idField.indexed(true);
    idField.cascadeOnActivate(true);
    abstractEntityClass.objectField("fProperties").cascadeOnUpdate(true); //$NON-NLS-1$
  }

  private static void configureFolder(Configuration config) {
    ObjectClass oc = config.objectClass(Folder.class);
    oc.objectField("fChildren").cascadeOnUpdate(true); //$NON-NLS-1$
  }

  private static void configureNews(Configuration config) {
    ObjectClass oc = config.objectClass(News.class);

    /* Indexes */
    oc.objectField("fParentId").indexed(true); //$NON-NLS-1$
    oc.objectField("fFeedLink").indexed(true); //$NON-NLS-1$
    oc.objectField("fStateOrdinal").indexed(true); //$NON-NLS-1$
  }

  private static void configureFeed(Configuration config) {
    ObjectClass oc = config.objectClass(Feed.class);

    ObjectField linkText = oc.objectField("fLinkText"); //$NON-NLS-1$
    linkText.indexed(true);
    linkText.cascadeOnActivate(true);

    oc.objectField("fTitle").cascadeOnActivate(true); //$NON-NLS-1$
  }

  /**
   * Returns true if database should be converted to large block size
   *
   * @return Returns true if database should be converted to large block size
   */
  public static boolean moveToLargerBlockSize() {
    if (getLargeBlockSizeMarkerFile().exists()) {
      return false;
    }

    File database = new File(ProfileFileManager.getDBFilePath());
    long length = database.exists() ? database.length() : 0;
    return length > LARGE_DB_STARTING_SIZE;
  }

  /**
   * @return the File indicating whether the DB was defragmented to a larger
   * block size.
   */
  private static File getLargeBlockSizeMarkerFile() {
    File dir = new File(Activator.getDefault().getStateLocation().toOSString());
    return new File(dir, LARGE_BLOCK_SIZE_MARKER);
  }

  private static boolean isIBM_VM_1_6() {
    String javaVendor = System.getProperty("java.vendor"); //$NON-NLS-1$
    String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
    return javaVendor != null && javaVendor.contains("IBM") && javaVersion != null && javaVersion.contains("1.6"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public static void createLargeBlockSizeMarker() {
    File largeBlockSizeMarkerFile = getLargeBlockSizeMarkerFile();
    if (!largeBlockSizeMarkerFile.exists()) {
      try {
        if (!largeBlockSizeMarkerFile.createNewFile()) {
          Activator.getDefault().logError("Failed to create large blocksize marker file", null); //$NON-NLS-1$
        }
      } catch (IOException e) {
        Activator.getDefault().logError("Failed to create large blocksize marker file", e); //$NON-NLS-1$
      }
    }
  }

  public static void deleteLargeBlockSizeMarker() {
    File largeBlockSizeMarkerFile = getLargeBlockSizeMarkerFile();
    if (largeBlockSizeMarkerFile.exists()) {
      largeBlockSizeMarkerFile.delete();
    }
  }

  public static void updateLargeBlockSizemarker(long length) {
    try {
      File largeBlockSizeMarkerFile = getLargeBlockSizeMarkerFile();
      if (largeBlockSizeMarkerFile.exists() && length < LARGE_DB_STARTING_SIZE) {
        largeBlockSizeMarkerFile.delete();
      } else if (!largeBlockSizeMarkerFile.exists() && length > LARGE_DB_STARTING_SIZE) {
        largeBlockSizeMarkerFile.createNewFile();
      }
    } catch (IOException e) {
      Activator.getDefault().logError(e.getMessage(), e);
    }

  }

}
