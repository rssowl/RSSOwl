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

package org.rssowl.core.persist;

import org.rssowl.core.persist.reference.FeedLinkReference;

import java.net.URI;
import java.util.Date;

/**
 * Provides access to the model factory of RSSOwl. This factory is used
 * everywhere when new entities are created.
 * <p>
 * Contributed via <code>org.rssowl.core.ModelFactory</code> Extension Point.
 * </p>
 *
 * @author Ismael Juma (ismael@juma.uk)
 */
public interface IModelFactory {

  /**
   * Creates a new instance of INews with the provided parameters. The News is
   * automatically added to the given Feed.
   *
   * @param id The unique ID of the News or <code>null</code> if this is a new
   * entity.
   * @param feed The Feed this News belongs to.
   * @param receiveDate The Date this News was received.
   * @return A new instance of INews with the provided parameters.
   */
  INews createNews(Long id, IFeed feed, Date receiveDate);

  /**
   * Creates a deep copy of {@code news}.
   *
   * @param news INews to be copied.
   * @param newsBin The instance of {@link INewsBin} the {@link INews} should be
   * added to.
   * @return a deep copy of {@code news}.
   */
  INews createNews(INews news, INewsBin newsBin);

  /**
   * Creates a new instance of IPerson with the provided parameters. The new
   * Person is automatically added to the given Type.
   *
   * @param id The unique ID of the Person or <code>null</code> if this is a new
   * entity.
   * @param parentRef The Type this Person belongs to.
   * @return A new instance of IPerson with the provided parameters.
   */
  IPerson createPerson(Long id, IPersistable parentRef);

  /**
   * Creates a new instance of IImage with the provided parameters. The new
   * Image is automatically added to the given Feed.
   *
   * @param feed The Feed this image belongs to.
   * @return A new instance of IImage with the provided parameters.
   */
  IImage createImage(IFeed feed);

  /**
   * Creates a new instance of IAttachment with the provided parameters. The new
   * Attachment is automatically added to the given News.
   *
   * @param id The unique ID of the Attachment or <code>null</code> if this is a
   * new entity.
   * @param news The News this Attachment belongs to.
   * @return A new instance of IAttachment with the provided parameters.
   */
  IAttachment createAttachment(Long id, INews news);

  /**
   * Creates a new instance of ICategory with the provided parameters.
   *
   * @param id The unique ID of the Category or <code>null</code> if this is a
   * new entity.
   * @param parent The Type this Category belongs to.
   * @return A new instance ICategory with the provided parameters.
   */
  ICategory createCategory(Long id, IEntity parent);

  /**
   * Creates a new instance of ISource with the provided parameters. The new
   * Source is automatically added to the given News.
   *
   * @param news The News this Source belongs to.
   * @return A new instance ISource with the provided parameters.
   */
  ISource createSource(INews news);

  /**
   * Creates a new instance of Guid with the provided parameters. The new Guid
   * is automatically added to the given News.
   *
   * @param news The News this Guid belongs to.
   * @param value The identifier of the Guid.
   * @param permaLink Indicates whether this guid is a permalink to the item.
   * {@code null} indicates that the feed had no permaLink attribute. See
   * {@link IGuid#isPermaLink()} for more information.
   * @return A new instance of IGuid with the provided parameters.
   */
  IGuid createGuid(INews news, String value, Boolean permaLink);

  /**
   * Creates a new instance of ICloud with the provided parameters. The new
   * Cloud is automatically set to the given feed.
   *
   * @param feed The Feed this Cloud belongs to.
   * @return A new instance of ICloud with the provided parameters.
   */
  ICloud createCloud(IFeed feed);

  /**
   * Creates a new instance of ITextInput with the provided parameters. The new
   * TextInput is automatically set to the given feed.
   *
   * @param feed The Feed this TextInput belongs to.
   * @return A new instance of ITextInput with the provided parameters.
   */
  ITextInput createTextInput(IFeed feed);

  /**
   * Creates a new instance of IFeed with the provided parameters.
   *
   * @param id The unique ID of the Feed or <code>null</code> if this is a new
   * entity.
   * @param link The URI of this Feed, where to retrieve the News from.
   * @return A new instance of IFeed with the provided parameters.
   */
  IFeed createFeed(Long id, URI link);

  /**
   * Creates a new instance of IFolder with the provided parameters. The new
   * Folder is automatically added as last item to the given parent folder,
   * unless its <code>NULL</code> or not cached.
   *
   * @param id The unique id of the Folder or <code>null</code> if this is a new
   * entity.
   * @param parent A parent Folder, or <code>NULL</code> if this is root Folder.
   * @param name The Name of the Folder.
   * @return a new instance of IFolder with the provided parameters.
   */
  IFolder createFolder(Long id, IFolder parent, String name);

  /**
   * Creates a new instance of IFolder with the provided parameters. The new
   * Folder is automatically added to the given parent folder using the given
   * position, unless its <code>NULL</code> or not cached.
   *
   * @param id The unique id of the Folder or <code>null</code> if this is a new
   * entity.
   * @param parent A parent Folder, or <code>NULL</code> if this is root Folder.
   * @param name The Name of the Folder.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder or <code>NULL</code> to add the folder as last
   * element.
   * @param after If <code>true</code>, move the folders to a one index after
   * the given position. May be <code>NULL</code> if the position is unknown.
   * @return a new instance of IFolder with the provided parameters.
   */
  IFolder createFolder(Long id, IFolder parent, String name, IFolderChild position, Boolean after);

  /**
   * Creates a new instance of ILabel with the provided parameters.
   *
   * @param id The unique ID of this Label or <code>null</code> if this is a new
   * entity.
   * @param name The Name of this Label.
   * @return a new instance of ILabel with the provided parameters.
   */
  ILabel createLabel(Long id, String name);

  /**
   * Creates a new instance of ISearchMark with the provided parameters. The new
   * SearchMark is automatically added as last item to the given parent folder.
   *
   * @param id The unique id of the ISearchMark or <code>null</code> if this is
   * a new entity.
   * @param folder The parent Folder.
   * @param name The Name of the ISearchMark.
   * @return a new instance of ISearchMark with the provided parameters.
   */
  ISearchMark createSearchMark(Long id, IFolder folder, String name);

  /**
   * Creates a new instance of ISearchMark with the provided parameters. The new
   * SearchMark is automatically added to the given parent folder at the given
   * position.
   *
   * @param id The unique id of the ISearchMark or <code>null</code> if this is
   * a new entity.
   * @param folder The parent Folder.
   * @param name The Name of the ISearchMark.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder or <code>NULL</code> to add the mark as the last
   * element.
   * @param after If <code>true</code>, add the mark to the index after the
   * given position. May be <code>NULL</code> if the position is unknown.
   * @return a new instance of ISearchMark with the provided parameters.
   */
  ISearchMark createSearchMark(Long id, IFolder folder, String name, IFolderChild position, Boolean after);

  /**
   * Creates a new instance of INewsBin with the provided parameters. The new
   * NewsBin is automatically added as last item to the given parent folder.
   *
   * @param id The unique id of the INewsBin or <code>null</code> if this is a
   * new entity.
   * @param folder The parent Folder.
   * @param name The Name of the INewsBin.
   * @return a new instance of INewsBin with the provided parameters.
   */
  INewsBin createNewsBin(Long id, IFolder folder, String name);

  /**
   * Creates a new instance of INewsBin with the provided parameters. The new
   * NewsBin is automatically added to the given parent folder at the given
   * position.
   *
   * @param id The unique id of the INewsBin or <code>null</code> if this is a
   * new entity.
   * @param folder The parent Folder.
   * @param name The Name of the INewsBin.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder or <code>null</code> to add the INewsBin as the
   * last element.
   * @param after If <code>true</code>, add the INewsBin to the index after the
   * given position. May be <code>NULL</code> if the position is unknown.
   * @return a new instance of INewsBin with the provided parameters.
   */
  INewsBin createNewsBin(Long id, IFolder folder, String name, IFolderChild position, Boolean after);

  /**
   * Creates a new instance of IBookMark with the provided parameters. The new
   * BookMark is automatically added to the given parent folder as last element.
   *
   * @param id The unique id of the BookMark or <code>null</code> if this is a
   * new entity.
   * @param folder The parent Folder
   * @param feedRef The reference to the feed this BookMark is related to.
   * @param name The Name of the Folder.
   * @return a new instance of IBookMark with the provided parameters.
   */
  IBookMark createBookMark(Long id, IFolder folder, FeedLinkReference feedRef, String name);

  /**
   * Creates a new instance of IBookMark with the provided parameters. The new
   * BookMark is automatically added to the given parent folder at the given
   * position.
   *
   * @param id The unique id of the BookMark or <code>null</code> if this is a
   * new entity.
   * @param folder The parent Folder
   * @param feedRef The reference to the feed this BookMark is related to.
   * @param name The Name of the Folder.
   * @param position The new Position identified by a <code>IFolderChild</code>
   * contained in this folder or <code>NULL</code> to add the mark as last
   * element.
   * @param after If <code>true</code>, move the folders to a one index after
   * the given position. May be <code>NULL</code> if the position is unknown.
   * @return a new instance of IBookMark with the provided parameters.
   */
  IBookMark createBookMark(Long id, IFolder folder, FeedLinkReference feedRef, String name, IFolderChild position, Boolean after);

  /**
   * Creates a new instance of ISearchCondition with the provided parameters.
   * The new SearchCondition is automatically added to the given parent
   * SearchMark.
   *
   * @param id The unique id of the SearchCondition or <code>null</code> if this
   * is a new entity.
   * @param searchMark The SearckMark this type belongs to.
   * @param field The SearchField this SearchCondition is targeting.
   * @param specifier The specifier tells about how the value should match the
   * target field.
   * @param value The value of the Search (will be converted to a String).
   * Unless the value is a <code>String</code>, primitive Type, or
   * <code>Enum</code>, make sure to provide a decent toString() implementation.
   * @param isAndSearch If <code>TRUE</code>, this SearchCondition and all
   * others having TRUE as value for this parameter, must be matched by the
   * target.
   * @return a new instance of ISearchCondition with the provided parameters.
   */
  ISearchCondition createSearchCondition(Long id, ISearchMark searchMark, ISearchField field, SearchSpecifier specifier, Object value);

  /**
   * Creates a new instance of ISearchCondition with the provided parameters.
   *
   * @param field The SearchField this SearchCondition is targeting.
   * @param specifier The specifier tells about how the value should match the
   * target field.
   * @param value The value of the Search (will be converted to a String).
   * Unless the value is a <code>String</code>, primitive Type, or
   * <code>Enum</code>, make sure to provide a decent toString() implementation.
   * @return a new instance of ISearchCondition with the provided parameters.
   */
  ISearchCondition createSearchCondition(ISearchField field, SearchSpecifier specifier, Object value);

  /**
   * Creates a new instance of ISearchField with the provided parameters.
   *
   * @param id The unique id of the searchfield as defined in the given
   * <code>class</code> through constants.
   * @param entityName The fully qualified Name of the <code>IEntity</code> this
   * <code>ISearchField</code> is referring to.
   * @return a new instance of ISearchField with the provided parameters.
   */
  ISearchField createSearchField(int id, String entityName);

  /**
   * Creates an instance of IConditionalGet with the provided parameters.
   *
   * @param ifModifiedSince the If-Modified Header to be sent as
   * If-Modified-Since Request Header.
   * @param link the link that this object refers to.
   * @param ifNoneMatch the ETag Header to be sent as If-None-Match Request
   * Header.
   * @return a new instance of IConditionalGet.
   * @throws IllegalArgumentException if <code>ifModifiedSince</code> and
   * <code>ifNoneMatch</code> are both null, or if <code>link</code> is null.
   */
  IConditionalGet createConditionalGet(String ifModifiedSince, URI link, String ifNoneMatch);

  /**
   * Creates an instance of IPreference with the provided key.
   *
   * @param key String that uniquely identifies the IPreference.
   * @return a new instance of IPreference.
   */
  IPreference createPreference(String key);

  /**
   * Creates a new instance of ISearch with the provided parameters.
   *
   * @param id The unique id of the ISearch.
   * @return a new instance of ISearch with the provided parameters.
   */
  ISearch createSearch(Long id);

  /**
   * Creates a new instance of ISearchFilter with the provided parameters.
   *
   * @param id the unique ID of the ISearchFilter or <code>null</code> if this
   * is a new entity.
   * @param search the search conditions to define the resulting entities.
   * @param name a human readable name for the filter
   * @return a new instance of ISearchFilter with the provided parameters.
   */
  ISearchFilter createSearchFilter(Long id, ISearch search, String name);

  /**
   * Creates a new instance of IFilterAction with the provided parameters.
   *
   * @param actionId the ID of an action to perform on the resulting entities of
   * a {@link ISearchFilter}.
   * @return a new instance of IFilterAction with the provided parameters.
   */
  IFilterAction createFilterAction(String actionId);
}