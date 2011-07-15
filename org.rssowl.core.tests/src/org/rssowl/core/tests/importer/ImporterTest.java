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

package org.rssowl.core.tests.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.interpreter.ITypeImporter;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * This TestCase covers use-cases for the Interpreter Plugin in terms of
 * Importing Data.
 *
 * @author bpasero
 */
public class ImporterTest {

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * Test importing an OPML as new Category with BookMarks.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testImportOPML() throws Exception {
    InputStream inS = getClass().getResourceAsStream("/data/interpreter/import_opml.xml");
    List<? extends IEntity> types = Owl.getInterpreter().importFrom(inS);
    assertEquals(1, types.size());
    assertTrue(types.get(0) instanceof IFolder);

    /* Root */
    IFolder root = (IFolder) types.get(0);
    assertEquals(2, root.getFolders().size());
    assertEquals(0, root.getMarks().size());

    /* Category 1 */
    IFolder category1 = root.getFolders().get(0);
    assertEquals("category_1", category1.getName());
    assertEquals(3, category1.getMarks().size());

    /* Category 1 Marks */
    IBookMark category1_mark1 = (IBookMark) category1.getMarks().get(0);
    assertEquals("category_1_feed_1_title", category1_mark1.getName());
    assertEquals("http://www.category_1_feed_1_url.com", category1_mark1.getFeedLinkReference().getLink().toString());
    assertEquals("category_1_feed_1_website", category1_mark1.getProperty(ITypeImporter.HOMEPAGE_KEY));
    assertEquals("category_1_feed_1_description", category1_mark1.getProperty(ITypeImporter.DESCRIPTION_KEY));

    IBookMark category1_mark2 = (IBookMark) category1.getMarks().get(1);
    assertEquals("category_1_feed_2_text", category1_mark2.getName());
    assertEquals("http://www.category_1_feed_2_url.com", category1_mark2.getFeedLinkReference().getLink().toString());
    assertEquals("category_1_feed_2_website", category1_mark2.getProperty(ITypeImporter.HOMEPAGE_KEY));
    assertEquals("category_1_feed_2_description", category1_mark2.getProperty(ITypeImporter.DESCRIPTION_KEY));

    IBookMark category1_mark3 = (IBookMark) category1.getMarks().get(2);
    assertEquals("category_1_feed_3_title", category1_mark3.getName());

    /* Category 2 */
    IFolder category2 = root.getFolders().get(1);
    assertEquals("category_2", category2.getName());
    assertEquals(1, category2.getFolders().size());
    assertEquals(0, category2.getMarks().size());

    /* Sub Category 1 */
    IFolder subcategory_1 = category2.getFolders().get(0);
    assertEquals("sub_category_1", subcategory_1.getName());
    assertEquals(1, subcategory_1.getFolders().size());
    assertEquals(2, subcategory_1.getMarks().size());

    /* Sub Category 1 Marks */
    IBookMark subcategory_1_mark_1 = (IBookMark) subcategory_1.getMarks().get(0);
    assertEquals("sub_category_1_feed_1_title", subcategory_1_mark_1.getName());

    IBookMark subcategory_1_mark_2 = (IBookMark) subcategory_1.getMarks().get(1);
    assertEquals("http://www.sub_category_1_feed_2_url.com", subcategory_1_mark_2.getName());

    /* Sub Sub Category 1 */
    IFolder sub_subcategory_1 = subcategory_1.getFolders().get(0);
    assertEquals("sub_sub_category_1", sub_subcategory_1.getName());
    assertEquals(1, sub_subcategory_1.getMarks().size());

    IBookMark sub_subcategory_1_mark = (IBookMark) sub_subcategory_1.getMarks().get(0);
    assertEquals("sub_sub_category_1_feed_1_title", sub_subcategory_1_mark.getName());
  }

  /**
   * Test importing an OPML as new Category with BookMarks.
   *
   * @throws Exception
   */
  @Test
  @SuppressWarnings("nls")
  public void testImportMyImport() throws Exception {
    InputStream inS = getClass().getResourceAsStream("/data/interpreter/import_myimport.xml");
    List<? extends IEntity> types = Owl.getInterpreter().importFrom(inS);
    assertEquals(1, types.size());
    assertTrue(types.get(0) instanceof IFolder);

    /* Root */
    IFolder root = (IFolder) types.get(0);
    assertEquals(1, root.getFolders().size());
    assertEquals(0, root.getMarks().size());

    /* Category 1 */
    IFolder category1 = root.getFolders().get(0);
    assertEquals("category_1", category1.getName());
    assertEquals(3, category1.getMarks().size());

    /* Category 1 Marks */
    IBookMark category1_mark1 = (IBookMark) category1.getMarks().get(0);
    assertEquals("category_1_feed_1_title", category1_mark1.getName());
    assertNotNull(category1_mark1.getFeedLinkReference().resolve());

    IFeed category1_mark1_feed = category1_mark1.getFeedLinkReference().resolve();
    assertEquals(new URI("http://www.category_1_feed_1_url.com").toString(), category1_mark1_feed.getLink().toString());
    assertEquals(new URI("category_1_feed_1_website"), category1_mark1_feed.getHomepage());
    assertEquals("category_1_feed_1_description", category1_mark1_feed.getDescription());

    IBookMark category1_mark2 = (IBookMark) category1.getMarks().get(1);
    assertEquals("category_1_feed_2_text", category1_mark2.getName());
    assertNotNull(category1_mark2.getFeedLinkReference().resolve());

    IFeed category1_mark2_feed = category1_mark2.getFeedLinkReference().resolve();
    assertEquals(new URI("http://www.category_1_feed_2_url.com").toString(), category1_mark2_feed.getLink().toString());
    assertEquals(new URI("category_1_feed_2_website"), category1_mark2_feed.getHomepage());
    assertEquals("category_1_feed_2_description", category1_mark2_feed.getDescription());

    IBookMark category1_mark3 = (IBookMark) category1.getMarks().get(2);
    assertEquals("category_1_feed_3_title", category1_mark3.getName());
    assertNotNull(category1_mark3.getFeedLinkReference().resolve());

    IFeed category1_mark3_feed = category1_mark3.getFeedLinkReference().resolve();
    assertEquals(new URI("http://www.category_1_feed_3_url.com").toString(), category1_mark3_feed.getLink().toString());
  }
}