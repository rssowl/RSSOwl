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
import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.ILabel;
import org.rssowl.ui.dialogs.properties.IEntityPropertyPage;
import org.rssowl.ui.dialogs.properties.IPropertyDialogSite;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.dialogs.properties.EntityPropertyPageWrapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tests about contributing Entity-Property-Pages
 *
 * @author bpasero
 */
public class EntityPropertyPageTests implements IEntityPropertyPage {

  /**
   * @throws Exception
   */
  @SuppressWarnings("null")
  @Test
  public void testEntityPropertyPage() throws Exception {
    ILabel entity1 = Owl.getModelFactory().createLabel(null, "Label1");
    ILabel entity2 = Owl.getModelFactory().createLabel(null, "Label2");
    IFeed entity3 = Owl.getModelFactory().createFeed(null, new URI("http://www.link.com"));

    /* Case 1: Single Selection */
    {
      List<IEntity> entities = new ArrayList<IEntity>();
      entities.add(entity1);

      Set<EntityPropertyPageWrapper> pages = Controller.getDefault().getEntityPropertyPagesFor(entities);

      assertNotNull(pages);
      assertEquals(3, pages.size());

      EntityPropertyPageWrapper page1Wrapper = null;
      EntityPropertyPageWrapper page2Wrapper = null;
      EntityPropertyPageWrapper page3Wrapper = null;
      for (EntityPropertyPageWrapper page : pages) {
        if (page.getName().equals("TestPage1"))
          page1Wrapper = page;
        else if (page.getName().equals("TestPage2"))
          page2Wrapper = page;
        else if (page.getName().equals("TestPage3"))
          page3Wrapper = page;
      }

      assertNotNull(page1Wrapper);
      assertNotNull(page2Wrapper);
      assertNotNull(page3Wrapper);

      assertEquals(true, page1Wrapper.compareTo(page2Wrapper) == -1);

      assertEquals(true, page1Wrapper.getTargetEntities().contains(ILabel.class));
      assertEquals(true, page2Wrapper.getTargetEntities().contains(ILabel.class));
    }

    /* Case 2: Multi Selection */
    {
      List<IEntity> entities = new ArrayList<IEntity>();
      entities.add(entity1);
      entities.add(entity2);

      Set<EntityPropertyPageWrapper> pages = Controller.getDefault().getEntityPropertyPagesFor(entities);

      assertNotNull(pages);
      assertEquals(2, pages.size());

      EntityPropertyPageWrapper page1Wrapper = null;
      EntityPropertyPageWrapper page3Wrapper = null;
      for (EntityPropertyPageWrapper page : pages) {
        if (page.getName().equals("TestPage1"))
          page1Wrapper = page;
        else if (page.getName().equals("TestPage3"))
          page3Wrapper = page;
      }

      assertNotNull(page1Wrapper);
      assertNotNull(page3Wrapper);

      assertEquals(true, page1Wrapper.getTargetEntities().contains(ILabel.class));
    }

    /* Case 3 : Mixed Multi Selection */
    {
      List<IEntity> entities = new ArrayList<IEntity>();
      entities.add(entity1);
      entities.add(entity2);
      entities.add(entity3);

      Set<EntityPropertyPageWrapper> pages = Controller.getDefault().getEntityPropertyPagesFor(entities);

      assertNotNull(pages);
      assertEquals(1, pages.size());

      EntityPropertyPageWrapper page3Wrapper = null;
      for (EntityPropertyPageWrapper page : pages) {
        if (page.getName().equals("TestPage3"))
          page3Wrapper = page;
      }

      assertNotNull(page3Wrapper);

      assertEquals(true, page3Wrapper.getTargetEntities().contains(ILabel.class));
      assertEquals(true, page3Wrapper.getTargetEntities().contains(IFeed.class));
    }
  }

  public void init(IPropertyDialogSite site, List<IEntity> entities) {}

  public Control createContents(Composite parent) {
    return null;
  }

  public void finish() {}

  public boolean performOk(Set<IEntity> entitiesToSave) {
    return false;
  }

  public ImageDescriptor getImage() {
    return null;
  }

  public void setFocus() {}
}