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

package org.rssowl.core.tests.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.persist.BookMark;
import org.rssowl.core.internal.persist.Folder;
import org.rssowl.core.internal.persist.service.PersistenceServiceImpl;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.ITreeNode;
import org.rssowl.core.util.ModelTreeNode;
import org.rssowl.core.util.TreeTraversal;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Tests the <code>TreeTraversal</code> class being used for tree traversal.
 * <p>
 * TODO Test previous navigation.
 * </p>
 *
 * @author bpasero
 */
public class TreeTraversalTest {

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ((PersistenceServiceImpl)Owl.getPersistenceService()).recreateSchemaForTests();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testTreeTraversalFromRootWithoutFilter() throws Exception {
    ITreeNode startingNode = getStartingNode(true);

    IFolder rootFolder = (IFolder) startingNode.getData();

    IFolder subRootFolder1 = rootFolder.getFolders().get(0);
    IBookMark subRoot1Mark1 = (IBookMark) subRootFolder1.getMarks().get(0);
    IBookMark subRoot1Mark2 = (IBookMark) subRootFolder1.getMarks().get(1);
    IFolder subRoot1Folder1 = subRootFolder1.getFolders().get(0);
    IBookMark subRoot11Mark1 = (IBookMark) subRoot1Folder1.getMarks().get(0);
    IFolder subRoot11Folder1 = subRoot1Folder1.getFolders().get(0);

    IFolder subRootFolder2 = rootFolder.getFolders().get(1);
    IBookMark subRoot2Mark1 = (IBookMark) subRootFolder2.getMarks().get(0);
    IFolder subRoot2Folder1 = subRootFolder2.getFolders().get(0);
    IFolder subRoot21Folder1 = subRoot2Folder1.getFolders().get(0);
    IBookMark subRoot211Mark1 = (IBookMark) subRoot21Folder1.getMarks().get(0);

    IFolder subRootFolder3 = rootFolder.getFolders().get(2);
    IFolder subRoot3Folder1 = subRootFolder3.getFolders().get(0);
    IBookMark subRoot31Mark1 = (IBookMark) subRoot3Folder1.getMarks().get(0);

    TreeTraversal t = new TreeTraversal(startingNode) {
      @Override
      public boolean select(ITreeNode node) {
        return true;
      }
    };

    assertEquals(subRootFolder1, t.nextNode().getData());
    assertEquals(subRoot1Folder1, t.nextNode().getData());
    assertEquals(subRoot11Folder1, t.nextNode().getData());
    assertEquals(subRoot11Mark1, t.nextNode().getData());
    assertEquals(subRoot1Mark1, t.nextNode().getData());
    assertEquals(subRoot1Mark2, t.nextNode().getData());
    assertEquals(subRootFolder2, t.nextNode().getData());
    assertEquals(subRoot2Folder1, t.nextNode().getData());
    assertEquals(subRoot21Folder1, t.nextNode().getData());
    assertEquals(subRoot211Mark1, t.nextNode().getData());
    assertEquals(subRoot2Mark1, t.nextNode().getData());
    assertEquals(subRootFolder3, t.nextNode().getData());
    assertEquals(subRoot3Folder1, t.nextNode().getData());
    assertEquals(subRoot31Mark1, t.nextNode().getData());
    assertEquals(null, t.nextNode());

  }

  /**
   * @throws Exception
   */
  @Test
  public void testTreeTraversalFromRootWithFilter() throws Exception {
    ITreeNode startingNode = getStartingNode(true);

    IFolder rootFolder = (IFolder) startingNode.getData();

    IFolder subRootFolder1 = rootFolder.getFolders().get(0);
    IBookMark subRoot1Mark1 = (IBookMark) subRootFolder1.getMarks().get(0);
    IBookMark subRoot1Mark2 = (IBookMark) subRootFolder1.getMarks().get(1);
    IFolder subRoot1Folder1 = subRootFolder1.getFolders().get(0);
    IBookMark subRoot11Mark1 = (IBookMark) subRoot1Folder1.getMarks().get(0);

    IFolder subRootFolder2 = rootFolder.getFolders().get(1);
    IBookMark subRoot2Mark1 = (IBookMark) subRootFolder2.getMarks().get(0);
    IFolder subRoot2Folder1 = subRootFolder2.getFolders().get(0);
    IFolder subRoot21Folder1 = subRoot2Folder1.getFolders().get(0);
    IBookMark subRoot211Mark1 = (IBookMark) subRoot21Folder1.getMarks().get(0);

    IFolder subRootFolder3 = rootFolder.getFolders().get(2);
    IFolder subRoot3Folder1 = subRootFolder3.getFolders().get(0);
    IBookMark subRoot31Mark1 = (IBookMark) subRoot3Folder1.getMarks().get(0);

    TreeTraversal t = new TreeTraversal(startingNode) {
      @Override
      public boolean select(ITreeNode node) {
        return node.getData() instanceof IBookMark;
      }
    };

    assertEquals(subRoot11Mark1, t.nextNode().getData());
    assertEquals(subRoot1Mark1, t.nextNode().getData());
    assertEquals(subRoot1Mark2, t.nextNode().getData());
    assertEquals(subRoot211Mark1, t.nextNode().getData());
    assertEquals(subRoot2Mark1, t.nextNode().getData());
    assertEquals(subRoot31Mark1, t.nextNode().getData());
    assertEquals(null, t.nextNode());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testTreeTraversalFromLeafWithBackwardNavigation() throws Exception {
    ITreeNode startingNode = getStartingNode(false);

    TreeTraversal t = new TreeTraversal(startingNode) {
      @Override
      public boolean select(ITreeNode node) {
        return true;
      }
    };

    assertEquals("SubRoot11Folder1", ((IFolder) t.previousNode().getData()).getName());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testTreeTraversalFromLeafWithoutFilter() throws Exception {
    ITreeNode startingNode = getStartingNode(false);

    IFolder rootFolder = ((IBookMark) startingNode.getData()).getParent().getParent().getParent().getParent();

    IFolder subRootFolder2 = rootFolder.getFolders().get(1);
    IBookMark subRoot2Mark1 = (IBookMark) subRootFolder2.getMarks().get(0);

    IFolder subRootFolder3 = rootFolder.getFolders().get(2);
    IFolder subRoot3Folder1 = subRootFolder3.getFolders().get(0);
    IBookMark subRoot31Mark1 = (IBookMark) subRoot3Folder1.getMarks().get(0);

    TreeTraversal t = new TreeTraversal(startingNode) {
      @Override
      public boolean select(ITreeNode node) {
        return true;
      }
    };

    assertEquals(subRoot2Mark1, t.nextNode().getData());
    assertEquals(subRootFolder3, t.nextNode().getData());
    assertEquals(subRoot3Folder1, t.nextNode().getData());
    assertEquals(subRoot31Mark1, t.nextNode().getData());
    assertEquals(null, t.nextNode());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testTreeTraversalFromLeafWithFilter() throws Exception {
    ITreeNode startingNode = getStartingNode(false);

    IFolder rootFolder = ((IBookMark) startingNode.getData()).getParent().getParent().getParent().getParent();

    IFolder subRootFolder2 = rootFolder.getFolders().get(1);
    IBookMark subRoot2Mark1 = (IBookMark) subRootFolder2.getMarks().get(0);

    IFolder subRootFolder3 = rootFolder.getFolders().get(2);
    IFolder subRoot3Folder1 = subRootFolder3.getFolders().get(0);
    IBookMark subRoot31Mark1 = (IBookMark) subRoot3Folder1.getMarks().get(0);

    TreeTraversal t = new TreeTraversal(startingNode) {
      @Override
      public boolean select(ITreeNode node) {
        return node.getData() instanceof IBookMark;
      }
    };

    assertEquals(subRoot2Mark1, t.nextNode().getData());
    assertEquals(subRoot31Mark1, t.nextNode().getData());
    assertEquals(null, t.nextNode());
  }

  private ITreeNode getStartingNode(boolean beginFromRoot) throws URISyntaxException {
    IFolder root = new Folder(null, null, "Root");
    FeedLinkReference feed = new FeedLinkReference(new URI("http://www.link.com"));

    /* Sub Root 1 */
    IFolder subRootFolder1 = new Folder(null, root, "SubRootFolder1");
    root.addFolder(subRootFolder1, null, false);

    IFolder subRoot1Folder1 = new Folder(null, subRootFolder1, "SubRoot1Folder1");
    subRootFolder1.addFolder(subRoot1Folder1, null, false);

    subRoot1Folder1.addFolder(new Folder(null, subRoot1Folder1, "SubRoot11Folder1"), null, false);
    subRoot1Folder1.addMark(new BookMark(null, subRoot1Folder1, feed, "SubRoot11Mark1"), null, false);

    subRootFolder1.addMark(new BookMark(null, subRootFolder1, feed, "SubRoot1Mark1"), null, false);
    subRootFolder1.addMark(new BookMark(null, subRootFolder1, feed, "SubRoot1Mark2"), null, false);

    /* Sub Root 2 */
    IFolder subRootFolder2 = new Folder(null, root, "SubRootFolder2");
    root.addFolder(subRootFolder2, null, false);

    subRootFolder2.addMark(new BookMark(null, subRootFolder2, feed, "SubRoot2Mark1"), null, false);

    IFolder subRoot2Folder1 = new Folder(null, subRootFolder2, "SubRoot2Folder1");
    subRootFolder2.addFolder(subRoot2Folder1, null, false);

    IFolder subRoot21Folder1 = new Folder(null, subRoot2Folder1, "SubRoot21Folder1");
    subRoot2Folder1.addFolder(subRoot21Folder1, null, false);

    IBookMark subRoot211Mark1 = new BookMark(null, subRoot21Folder1, feed, "SubRoot211Mark1");
    subRoot21Folder1.addMark(subRoot211Mark1, null, false);

    /* Sub Root 3 */
    IFolder subRootFolder3 = new Folder(null, root, "SubRootFolder1");
    root.addFolder(subRootFolder3, null, false);

    IFolder subRoot3Folder1 = new Folder(null, subRootFolder3, "SubRoot3Folder1");
    subRootFolder3.addFolder(subRoot3Folder1, null, false);

    subRoot3Folder1.addMark(new BookMark(null, subRoot3Folder1, feed, "SubRoot31Mark1"), null, false);

    return beginFromRoot ? new ModelTreeNode(root) : new ModelTreeNode(subRoot211Mark1);
  }
}