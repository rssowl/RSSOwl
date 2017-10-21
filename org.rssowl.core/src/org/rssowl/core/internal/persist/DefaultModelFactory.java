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

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.ICloud;
import org.rssowl.core.persist.IConditionalGet;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.IGuid;
import org.rssowl.core.persist.IImage;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISource;
import org.rssowl.core.persist.ITextInput;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.reference.FeedLinkReference;

import java.net.URI;
import java.util.Date;

/**
 * Default implementation of IModelFactory. It instantiates the concrete classes
 * provided in the {@link org.rssowl.core.internal.persist} package.
 *
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public class DefaultModelFactory implements IModelFactory {

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createNews(java.lang.Long,
   * org.rssowl.core.model.types.IFeed, java.util.Date)
   */
  @Override
  public INews createNews(Long id, IFeed feed, Date receiveDate) {
    News news = new News(id, feed, receiveDate);

    /* Automatically add to the Feed */
    feed.addNews(news);
    return news;
  }

  /*
   * @see
   * org.rssowl.core.persist.IModelFactory#createNews(org.rssowl.core.persist
   * .INews, org.rssowl.core.persist.INewsBin)
   */
  @Override
  public INews createNews(INews news, INewsBin newsBin) {
    Assert.isNotNull(newsBin.getId(), "ID of the Bin must not be null!"); //$NON-NLS-1$

    INews copy = new News((News) news, newsBin.getId());
    copy.setId(Owl.getPersistenceService().getIDGenerator().getNext());
    newsBin.addNews(copy);
    return copy;
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createPerson(long,
   * org.rssowl.core.model.reference.ModelReference)
   */
  @Override
  public IPerson createPerson(Long id, IPersistable parentRef) {
    Person person = new Person(id);

    /* Automatically add to the Feed or News */
    if (parentRef instanceof IFeed)
      ((IFeed) parentRef).setAuthor(person);
    else if (parentRef instanceof INews)
      ((INews) parentRef).setAuthor(person);

    return person;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createImage(org.rssowl.core.model
   * .types.IFeed)
   */
  @Override
  public IImage createImage(IFeed feed) {
    Image image = new Image();

    /* Automatically add to the Feed */
    feed.setImage(image);

    return image;
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createAttachment(long,
   * java.net.URI, org.rssowl.core.model.reference.NewsReference)
   */
  @Override
  public IAttachment createAttachment(Long id, INews news) {
    Attachment attachment = new Attachment(id, news);
    news.addAttachment(attachment);

    return attachment;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createCategory(java.lang.Long,
   * org.rssowl.core.model.types.IEntity)
   */
  @Override
  public ICategory createCategory(Long id, IEntity parent) {
    Category category = new Category(id);

    /* Automatically add to the Feed or News */
    if (parent instanceof IFeed)
      ((IFeed) parent).addCategory(category);
    else if (parent instanceof INews)
      ((INews) parent).addCategory(category);

    return category;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createSource(org.rssowl.core.
   * model.types.INews)
   */
  @Override
  public ISource createSource(final INews news) {
    Source source = new Source();

    /* Automatically set to the News */
    news.setSource(source);

    return source;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createGuid(org.rssowl.core.model
   * .types.INews, java.lang.String)
   */
  @Override
  public IGuid createGuid(final INews news, String value, Boolean permaLink) {
    Guid guid = new Guid(value, permaLink);

    /* Automatically set to the News */
    news.setGuid(guid);

    return guid;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createCloud(org.rssowl.core.model
   * .types.IFeed)
   */
  @Override
  public ICloud createCloud(IFeed feed) {
    CloudAdapter cloud = new CloudAdapter();

    /* Automatically set to the Feed */
    feed.setCloud(cloud);

    return cloud;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createTextInput(org.rssowl.core
   * .model.types.IFeed)
   */
  @Override
  public ITextInput createTextInput(IFeed feed) {
    TextInputAdapter textInput = new TextInputAdapter();

    /* Automatically set to the Feed */
    feed.setTextInput(textInput);

    return textInput;
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createFeed(java.lang.Long,
   * java.net.URI)
   */
  @Override
  public IFeed createFeed(Long id, URI link) {
    return new Feed(id, link);
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createFolder(long,
   * java.lang.String, org.rssowl.core.model.reference.FolderReference)
   */
  @Override
  public IFolder createFolder(Long id, IFolder parent, String name) {
    return createFolder(id, parent, name, null, null);
  }

  /*
   * @see org.rssowl.core.persist.IModelFactory#createFolder(java.lang.Long,
   * org.rssowl.core.persist.IFolder, java.lang.String,
   * org.rssowl.core.persist.IFolderChild, boolean)
   */
  @Override
  public IFolder createFolder(Long id, IFolder parent, String name, IFolderChild position, Boolean after) {
    Folder folder = new Folder(id, parent, name);

    /* Automatically add to the Folder */
    if (parent != null)
      parent.addFolder(folder, position, after);

    return folder;
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createLabel(long,
   * java.lang.String)
   */
  @Override
  public ILabel createLabel(Long id, String name) {
    return new Label(id, name);
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createSearchMark(long,
   * java.lang.String, org.rssowl.core.model.reference.FolderReference)
   */
  @Override
  public ISearchMark createSearchMark(Long id, IFolder folder, String name) {
    return createSearchMark(id, folder, name, null, null);
  }

  /*
   * @see org.rssowl.core.persist.IModelFactory#createSearchMark(java.lang.Long,
   * org.rssowl.core.persist.IFolder, java.lang.String,
   * org.rssowl.core.persist.IFolderChild, boolean)
   */
  @Override
  public ISearchMark createSearchMark(Long id, IFolder folder, String name, IFolderChild position, Boolean after) {
    SearchMark searchMark = new SearchMark(id, folder, name);

    /* Automatically add to the Folder */
    folder.addMark(searchMark, position, after);

    return searchMark;
  }

  @Override
  public INewsBin createNewsBin(Long id, IFolder folder, String name) {
    return createNewsBin(id, folder, name, null, null);
  }

  @Override
  public INewsBin createNewsBin(Long id, IFolder folder, String name, IFolderChild position, Boolean after) {
    NewsBin newsBin = new NewsBin(id, folder, name);

    /* Automatically add to the Folder */
    folder.addMark(newsBin, position, after);

    return newsBin;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createBookMark(java.lang.Long,
   * org.rssowl.core.model.types.IFolder,
   * org.rssowl.core.model.reference.FeedLinkReference, java.lang.String)
   */
  @Override
  public IBookMark createBookMark(Long id, IFolder folder, FeedLinkReference feedRef, String name) {
    return createBookMark(id, folder, feedRef, name, null, null);
  }

  /*
   * @see org.rssowl.core.persist.IModelFactory#createBookMark(java.lang.Long,
   * org.rssowl.core.persist.IFolder,
   * org.rssowl.core.persist.reference.FeedLinkReference, java.lang.String,
   * org.rssowl.core.persist.IFolderChild, boolean)
   */
  @Override
  public IBookMark createBookMark(Long id, IFolder folder, FeedLinkReference feedRef, String name, IFolderChild position, Boolean after) {
    BookMark bookMark = new BookMark(id, folder, feedRef, name);

    /* Automatically add to the Folder */
    folder.addMark(bookMark, position, after);

    return bookMark;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createSearchCondition(java.lang
   * .Long, org.rssowl.core.model.types.ISearchMark,
   * org.rssowl.core.model.search.ISearchField,
   * org.rssowl.core.model.search.SearchSpecifier, java.lang.Object)
   */
  @Override
  public ISearchCondition createSearchCondition(Long id, ISearchMark searchMark, ISearchField field, SearchSpecifier specifier, Object value) {
    SearchCondition condition = new SearchCondition(id, field, specifier, value);

    /* Automatically add to the SearchMark */
    searchMark.addSearchCondition(condition);
    return condition;
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createSearchCondition(org.rssowl
   * .core.model.search.ISearchField,
   * org.rssowl.core.model.search.SearchSpecifier, java.lang.Object)
   */
  @Override
  public ISearchCondition createSearchCondition(ISearchField field, SearchSpecifier specifier, Object value) {
    return new SearchCondition(field, specifier, value);
  }

  /*
   * @see org.rssowl.core.model.types.IModelFactory#createSearchField(int,
   * java.lang.String)
   */
  @Override
  public ISearchField createSearchField(int id, String entityName) {
    return new SearchField(id, entityName);
  }

  /*
   * @see
   * org.rssowl.core.model.types.IModelFactory#createConditionalGet(java.lang
   * .String, java.net.URI, java.lang.String)
   */
  @Override
  public IConditionalGet createConditionalGet(String ifModifiedSince, URI link, String ifNoneMatch) {
    return new ConditionalGet(ifModifiedSince, link, ifNoneMatch);
  }

  /*
   * @see
   * org.rssowl.core.persist.IModelFactory#createPreference(java.lang.String)
   */
  @Override
  public IPreference createPreference(String key) {
    return new Preference(key);
  }

  /*
   * @see org.rssowl.core.persist.IModelFactory#createSearch(java.lang.Long)
   */
  @Override
  public ISearch createSearch(Long id) {
    return new Search(id);
  }

  /*
   * @see
   * org.rssowl.core.persist.IModelFactory#createFilterAction(java.lang.String)
   */
  @Override
  public IFilterAction createFilterAction(String actionId) {
    return new FilterAction(actionId);
  }

  /*
   * @see
   * org.rssowl.core.persist.IModelFactory#createSearchFilter(java.lang.Long,
   * org.rssowl.core.persist.ISearch, java.lang.String)
   */
  @Override
  public ISearchFilter createSearchFilter(Long id, ISearch search, String name) {
    return new SearchFilter(id, search, name);
  }
}