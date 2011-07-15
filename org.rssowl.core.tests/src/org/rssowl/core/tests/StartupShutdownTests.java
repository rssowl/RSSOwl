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
import org.rssowl.core.tests.model.CachingDAOTest;
import org.rssowl.core.tests.model.CachingDAOTestLBS;
import org.rssowl.core.tests.model.ModelTest1;
import org.rssowl.core.tests.model.ModelTest1LBS;
import org.rssowl.core.tests.persist.StartupShutdownTest;
import org.rssowl.core.tests.persist.StartupShutdownTestLBS;
import org.rssowl.core.tests.persist.service.DefragmentTest;

/**
 * Tests that trigger lifecycle methods run as last tests to not interfer other tests.
 *
 * @author bpasero
 * @author Ismael Juma (ismael@juma.me.uk)
 */

@RunWith(Suite.class)
@SuiteClasses( {
  ModelTest1.class,
  ModelTest1LBS.class, //Running twice to test with large block size
  CachingDAOTest.class,
  CachingDAOTestLBS.class, //Running twice to test with large block size
  DefragmentTest.class,
  StartupShutdownTest.class,
  StartupShutdownTestLBS.class })
public class StartupShutdownTests {}