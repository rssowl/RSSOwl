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
import org.rssowl.core.tests.importer.FileImportTest;
import org.rssowl.core.tests.importer.ImportExportOPMLTest;
import org.rssowl.core.tests.ui.CleanUpTests;
import org.rssowl.core.tests.ui.DownloadServiceTests;
import org.rssowl.core.tests.ui.EntityPropertyPageTests;
import org.rssowl.core.tests.ui.FolderMarkGroupFilterTest;
import org.rssowl.core.tests.ui.FolderNewsMarkTest;
import org.rssowl.core.tests.ui.MiscUITests;
import org.rssowl.core.tests.ui.ModelUtilsTest;
import org.rssowl.core.tests.ui.NewsBrowserViewModelTests;
import org.rssowl.core.tests.ui.NewsGroupFilterTest;
import org.rssowl.core.tests.ui.SyncServiceTest;
import org.rssowl.core.tests.ui.UndoTest;

/**
 * Test-Suite for UI-Tests.
 *
 * @author bpasero
 * @author Ismael Juma (ismael@juma.me.uk)
 */

@RunWith(Suite.class)
@SuiteClasses( {
  EntityPropertyPageTests.class,
  ModelUtilsTest.class,
  FolderMarkGroupFilterTest.class,
  NewsGroupFilterTest.class,
  MiscUITests.class,
  FolderNewsMarkTest.class,
  CleanUpTests.class,
  ImportExportOPMLTest.class,
  FileImportTest.class,
  DownloadServiceTests.class,
  NewsBrowserViewModelTests.class,
  SyncServiceTest.class,
  UndoTest.class })
public class UITests {}