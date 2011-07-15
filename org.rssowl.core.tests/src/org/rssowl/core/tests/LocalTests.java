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

package org.rssowl.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.rssowl.core.tests.controller.ControllerTestLocal;
import org.rssowl.core.tests.controller.ControllerTestLocalLBS;
import org.rssowl.core.tests.controller.ReloadTestLocal;
import org.rssowl.core.tests.controller.ReloadTestLocalLBS;
import org.rssowl.core.tests.importer.ImporterTest;
import org.rssowl.core.tests.interpreter.InterpreterTest;
import org.rssowl.core.tests.model.ApplicationLayerTest;
import org.rssowl.core.tests.model.ApplicationLayerTestLBS;
import org.rssowl.core.tests.model.DBManagerTest;
import org.rssowl.core.tests.model.DBManagerTestLBS;
import org.rssowl.core.tests.model.ModelSearchTest1;
import org.rssowl.core.tests.model.ModelSearchTest2;
import org.rssowl.core.tests.model.ModelSearchTest3;
import org.rssowl.core.tests.model.ModelSearchTest4;
import org.rssowl.core.tests.model.ModelTest2;
import org.rssowl.core.tests.model.ModelTest2LBS;
import org.rssowl.core.tests.model.ModelTest3;
import org.rssowl.core.tests.model.ModelTest3LBS;
import org.rssowl.core.tests.model.ModelTest4;
import org.rssowl.core.tests.model.ModelTest4LBS;
import org.rssowl.core.tests.model.NewsFilterTest;
import org.rssowl.core.tests.model.NewsFilterTestLBS;
import org.rssowl.core.tests.model.PreferencesDAOTest;
import org.rssowl.core.tests.model.PreferencesDAOTestLBS;
import org.rssowl.core.tests.model.PreferencesScopeTest;
import org.rssowl.core.tests.model.PreferencesScopeTestLBS;
import org.rssowl.core.tests.persist.INewsTest;
import org.rssowl.core.tests.persist.LongArrayListTest;
import org.rssowl.core.tests.persist.MigrationsTest;
import org.rssowl.core.tests.ui.ExpandingReaderTests;
import org.rssowl.core.tests.ui.RetentionStrategyTests;
import org.rssowl.core.tests.ui.TreeTraversalTest;
import org.rssowl.core.tests.util.CoreUtilsTest;
import org.rssowl.core.tests.util.MergeUtilsTest;
import org.rssowl.core.tests.util.StringUtilsTest;
import org.rssowl.core.tests.util.SyncUtilsTest;
import org.rssowl.core.tests.util.URIUtilsTest;

/**
 * Test-Suite for Core-Tests that are not requiring Network-Access.
 *
 * @author bpasero
 * @author Ismael Juma (ismael@juma.me.uk)
 */
@RunWith(Suite.class)
@SuiteClasses({
  InterpreterTest.class,
  ImporterTest.class,
  ControllerTestLocal.class,
  ControllerTestLocalLBS.class, //Running twice to test with large block size
  ReloadTestLocal.class,
  ReloadTestLocalLBS.class, //Running twice to test with large block size
  ModelTest2.class,
  ModelTest2LBS.class, //Running twice to test with large block size
  ModelTest3.class,
  ModelTest3LBS.class, //Running twice to test with large block size
  ModelTest4.class,
  ModelTest4LBS.class, //Running twice to test with large block size
  PreferencesDAOTest.class,
  PreferencesDAOTestLBS.class, //Running twice to test with large block size
  ApplicationLayerTest.class,
  ApplicationLayerTestLBS.class, //Running twice to test with large block size
  NewsFilterTest.class,
  NewsFilterTestLBS.class, //Running twice to test with large block size
  ModelSearchTest1.class,
  ModelSearchTest2.class,
  ModelSearchTest3.class,
  ModelSearchTest4.class,
  DBManagerTest.class,
  DBManagerTestLBS.class, //Running twice to test with large block size
  PreferencesScopeTest.class,
  PreferencesScopeTestLBS.class, //Running twice to test with large block size
  MergeUtilsTest.class,
  INewsTest.class,
  StringUtilsTest.class,
  SyncUtilsTest.class,
  CoreUtilsTest.class,
  URIUtilsTest.class,
  MigrationsTest.class,
  LongArrayListTest.class,
  RetentionStrategyTests.class,
  TreeTraversalTest.class,
  ExpandingReaderTests.class
})
public class LocalTests {}