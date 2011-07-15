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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISearchValueType;
import org.rssowl.core.util.CoreUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Instances of <code>ISearchField</code> describe the target field for a search
 * condition. The field is described by its identifier in the system and a
 * human-readable name, used in the UI.
 * </p>
 * <p>
 * A call to <code>getSearchValueType()</code> will give Information of the
 * data-type the field is using. This information can be used for validating the
 * search-value and to perform the search in the persistence layer.
 * </p>
 *
 * @author bpasero
 */
public class SearchField implements ISearchField {
  private int fField;
  private String fEntityName;

  /**
   * Instantiates a new SearchField that is describing the field a target type
   * needs to check for a match.
   *
   * @param field The ID of the field from the given Type, which is described by
   * one of the constants in the type's Interface.
   * @param entityName entityName The fully qualified Name of the
   * <code>IEntity</code> this <code>ISearchField</code> is referring to.
   */
  public SearchField(int field, String entityName) {
    fField = field;
    Assert.isNotNull(entityName, "The type SearchField requires a entityName that is not NULL"); //$NON-NLS-1$
    fEntityName = entityName;
  }

  /**
   * Default constructor for deserialization
   */
  protected SearchField() {
  // As per javadoc
  }

  /*
   * @see org.rssowl.core.model.search.ISearchField#getField()
   */
  public synchronized int getId() {
    return fField;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchField#getEntityName()
   */
  public synchronized String getEntityName() {
    return fEntityName;
  }

  /*
   * @see org.rssowl.core.model.search.ISearchField#getName()
   */
  public synchronized String getName() {

    /* Field from the Type IAttachment */
    if (fEntityName.equals(IAttachment.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_ATTACHMENT;
        case (IAttachment.LINK):
          return Messages.SearchField_LINK;
        case (IAttachment.LENGTH):
          return Messages.SearchField_SIZE;
        case (IAttachment.TYPE):
          return Messages.SearchField_TYPE;
      }
    }

    /* Field from Type IFolder */
    if (fEntityName.equals(IFolder.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_FOLDER;
        case (IFolder.NAME):
          return Messages.SearchField_NAME;
        case (IFolder.BLOGROLL_LINK):
          return Messages.SearchField_BLOGROLL_LINK;
        case (IFolder.FOLDERS):
          return Messages.SearchField_SUB_FOLDERS;
        case (IFolder.MARKS):
          return Messages.SearchField_BOOKMARKS;
      }
    }

    /* Field from Type ILabel */
    if (fEntityName.equals(ILabel.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_LABEL;
        case (ILabel.NAME):
          return Messages.SearchField_NAME;
        case (ILabel.COLOR):
          return Messages.SearchField_COLOR;
      }
    }

    /* Field from Type ICategory */
    if (fEntityName.equals(ICategory.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_CATEGORY;
        case (ICategory.NAME):
          return Messages.SearchField_NAME;
        case (ICategory.DOMAIN):
          return Messages.SearchField_DOMAIN;
      }
    }

    /* Field from Type IBookMark */
    if (fEntityName.equals(IBookMark.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_BOOKMARK;
        case (IMark.CREATION_DATE):
          return Messages.SearchField_CREATION_DATE;
        case (IMark.NAME):
          return Messages.SearchField_NAME;
        case (IMark.LAST_VISIT_DATE):
          return Messages.SearchField_LAST_VISIT;
        case (IMark.POPULARITY):
          return Messages.SearchField_NUMBER_OF_VISITS;
        case (IBookMark.IS_ERROR_LOADING):
          return Messages.SearchField_ERROR_LOADING;
      }
    }

    /* Field from Type ISearchMark */
    if (fEntityName.equals(ISearchMark.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_SEARCHMARK;
        case (IMark.CREATION_DATE):
          return Messages.SearchField_CREATION_DATE;
        case (IMark.NAME):
          return Messages.SearchField_NAME;
        case (IMark.LAST_VISIT_DATE):
          return Messages.SearchField_LAST_VISIT;
        case (IMark.POPULARITY):
          return Messages.SearchField_NUMBER_OF_VISITS;
      }
    }

    /* Field from Type IPerson */
    if (fEntityName.equals(IPerson.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_PERSON;
        case (IPerson.NAME):
          return Messages.SearchField_NAME;
        case (IPerson.EMAIL):
          return Messages.SearchField_EMAIL;
        case (IPerson.URI):
          return Messages.SearchField_URI;
      }
    }

    /* Field from Type INews */
    if (fEntityName.equals(INews.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_NEWS;
        case (INews.TITLE):
          return Messages.SearchField_TITLE;
        case (INews.LINK):
          return Messages.SearchField_LINK;
        case (INews.DESCRIPTION):
          return Messages.SearchField_DESCRIPTION;
        case (INews.PUBLISH_DATE):
          return Messages.SearchField_PUBLISH_DATE;
        case (INews.MODIFIED_DATE):
          return Messages.SearchField_MODIFIED_DATE;
        case (INews.RECEIVE_DATE):
          return Messages.SearchField_RECEIVED_DATE;
        case (INews.AUTHOR):
          return Messages.SearchField_AUTHOR;
        case (INews.COMMENTS):
          return Messages.SearchField_COMMENTS;
        case (INews.GUID):
          return Messages.SearchField_GUID;
        case (INews.SOURCE):
          return Messages.SearchField_SOURCE;
        case (INews.HAS_ATTACHMENTS):
          return Messages.SearchField_HAS_ATTACHMENT;
        case (INews.ATTACHMENTS_CONTENT):
          return Messages.SearchField_ATTACHMENT;
        case (INews.CATEGORIES):
          return Messages.SearchField_CATEGORY;
        case (INews.IS_FLAGGED):
          return Messages.SearchField_IS_STICKY;
        case (INews.STATE):
          return Messages.SearchField_STATE_OF_NEWS;
        case (INews.LABEL):
          return Messages.SearchField_LABEL;
        case (INews.RATING):
          return Messages.SearchField_RATING;
        case (INews.FEED):
          return Messages.SearchField_FEED;
        case (INews.AGE_IN_DAYS):
          return Messages.SearchField_AGE;
        case (INews.AGE_IN_MINUTES):
          return Messages.SearchField_AGE;
        case (INews.LOCATION):
          return Messages.SearchField_LOCATION;
      }
    }

    /* Field from Type IFeed */
    if (fEntityName.equals(IFeed.class.getName())) {
      switch (fField) {
        case (IEntity.ALL_FIELDS):
          return Messages.SearchField_ENTIRE_NEWS;
        case (IFeed.LINK):
          return Messages.SearchField_LINK;
        case (IFeed.TITLE):
          return Messages.SearchField_TITLE;
        case (IFeed.PUBLISH_DATE):
          return Messages.SearchField_PUBLISH_DATE;
        case (IFeed.DESCRIPTION):
          return Messages.SearchField_DESCRIPTION;
        case (IFeed.HOMEPAGE):
          return Messages.SearchField_HOMEPAGE;
        case (IFeed.LANGUAGE):
          return Messages.SearchField_LANGUAGE;
        case (IFeed.COPYRIGHT):
          return Messages.SearchField_COPYRIGHT;
        case (IFeed.DOCS):
          return Messages.SearchField_DOCS;
        case (IFeed.GENERATOR):
          return Messages.SearchField_GENERATOR;
        case (IFeed.LAST_BUILD_DATE):
          return Messages.SearchField_LAST_BUILT_DATE;
        case (IFeed.WEBMASTER):
          return Messages.SearchField_WEBMASTER;
        case (IFeed.LAST_MODIFIED_DATE):
          return Messages.SearchField_LAST_MODIFIED_DATE;
        case (IFeed.TTL):
          return Messages.SearchField_TIME_TO_LIVE;
        case (IFeed.FORMAT):
          return Messages.SearchField_FORMAT;
        case (IFeed.AUTHOR):
          return Messages.SearchField_AUTHOR;
        case (IFeed.NEWS):
          return Messages.SearchField_NUMBER_OF_NEWS;
        case (IFeed.CATEGORIES):
          return Messages.SearchField_CATEGORY;
        case (IFeed.IMAGE):
          return Messages.SearchField_IMAGE;
      }
    }

    /* Default */
    return fEntityName + "#" + fField; //$NON-NLS-1$
  }

  /*
   * @see org.rssowl.core.model.search.ISearchField#getAllowedSearchTerm()
   */
  public synchronized ISearchValueType getSearchValueType() {

    /* Field from the Type IAttachment */
    if (fEntityName.equals(IAttachment.class.getName())) {
      switch (fField) {
        case (IAttachment.LENGTH):
          return SearchValueType.INTEGER;
        case (IAttachment.LINK):
          return SearchValueType.LINK;
      }
    }

    /* Field from Type IFolder */
    else if (fEntityName.equals(IFolder.class.getName())) {
      switch (fField) {
        case (IFolder.FOLDERS):
          return SearchValueType.INTEGER;
        case (IFolder.MARKS):
          return SearchValueType.INTEGER;
      }
    }

    /* Field from Type IBookMark */
    else if (fEntityName.equals(IBookMark.class.getName())) {
      switch (fField) {
        case (IMark.CREATION_DATE):
          return SearchValueType.DATETIME;
        case (IMark.LAST_VISIT_DATE):
          return SearchValueType.DATETIME;
        case (IMark.POPULARITY):
          return SearchValueType.INTEGER;
        case (IBookMark.IS_ERROR_LOADING):
          return SearchValueType.BOOLEAN;
      }
    }

    /* Field from Type ISearchMark */
    else if (fEntityName.equals(ISearchMark.class.getName())) {
      switch (fField) {
        case (IMark.CREATION_DATE):
          return SearchValueType.DATETIME;
        case (IMark.LAST_VISIT_DATE):
          return SearchValueType.DATETIME;
        case (IMark.POPULARITY):
          return SearchValueType.INTEGER;
      }
    }

    /* Field from Type IPerson */
    else if (fEntityName.equals(IPerson.class.getName()))
      return SearchValueType.STRING;

    /* Field from Type ILabel */
    else if (fEntityName.equals(ILabel.class.getName()))
      return SearchValueType.STRING;

    /* Field from Type ICategory */
    else if (fEntityName.equals(ICategory.class.getName()))
      return SearchValueType.STRING;

    /* Field from Type INews */
    else if (fEntityName.equals(INews.class.getName())) {
      switch (fField) {
        case (INews.PUBLISH_DATE):
          return SearchValueType.DATETIME;
        case (INews.MODIFIED_DATE):
          return SearchValueType.DATETIME;
        case (INews.RECEIVE_DATE):
          return SearchValueType.DATETIME;
        case (INews.IS_FLAGGED):
          return SearchValueType.BOOLEAN;
        case (INews.STATE):
          return new SearchValueType(loadStateValues());
        case (INews.LABEL):
          return new SearchValueType(loadLabelValues());
        case (INews.RATING):
          return SearchValueType.INTEGER;
        case (INews.LINK):
          return SearchValueType.LINK;
        case (INews.FEED):
          return SearchValueType.LINK;
        case (INews.AGE_IN_DAYS):
          return SearchValueType.INTEGER;
        case (INews.AGE_IN_MINUTES):
          return SearchValueType.INTEGER;
        case (INews.SOURCE):
          return SearchValueType.LINK;
        case (INews.HAS_ATTACHMENTS):
          return SearchValueType.BOOLEAN;
      }
    }

    /* Field from Type IFeed */
    else if (fEntityName.equals(IFeed.class.getName())) {
      switch (fField) {
        case (IFeed.LINK):
          return SearchValueType.LINK;
        case (IFeed.PUBLISH_DATE):
          return SearchValueType.DATETIME;
        case (IFeed.LANGUAGE):
          return new SearchValueType(loadLanguageValues());
        case (IFeed.LAST_BUILD_DATE):
          return SearchValueType.DATETIME;
        case (IFeed.LAST_MODIFIED_DATE):
          return SearchValueType.DATETIME;
        case (IFeed.TTL):
          return SearchValueType.INTEGER;
        case (IFeed.NEWS):
          return SearchValueType.INTEGER;
      }
    }

    return SearchValueType.STRING;
  }

  /* Return human-readable list of News-States */
  private List<String> loadStateValues() {
    return new ArrayList<String>(Arrays.asList(new String[] { Messages.SearchField_NEW, Messages.SearchField_READ, Messages.SearchField_UNREAD, Messages.SearchField_UPDATED, Messages.SearchField_DELETED }));
  }

  private List<String> loadLanguageValues() {
    return new ArrayList<String>();
  }

  /* Return the Label Values */
  private List<String> loadLabelValues() {
    Collection<ILabel> labels = CoreUtils.loadSortedLabels();

    List<String> values = new ArrayList<String>(labels.size());
    for (ILabel label : labels) {
      values.add(label.getName());
    }

    return values;
  }

  /**
   * Returns an object which is an instance of the given class associated with
   * this object. Returns <code>null</code> if no such object can be found.
   * <p>
   * This implementation of the method declared by <code>IAdaptable</code>
   * passes the request along to the platform's adapter manager; roughly
   * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
   * Subclasses may override this method (however, if they do so, they should
   * invoke the method on their superclass to ensure that the Platform's adapter
   * manager is consulted).
   * </p>
   *
   * @param adapter the class to adapt to
   * @return the adapted object or <code>null</code>
   * @see IAdaptable#getAdapter(Class)
   * @see Platform#getAdapterManager()
   */
  @SuppressWarnings("unchecked")
  public Object getAdapter(Class adapter) {
    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public synchronized boolean equals(Object obj) {
    if (this == obj)
      return true;

    if ((obj == null) || (obj.getClass() != getClass()))
      return false;

    synchronized (obj) {
      SearchField f = (SearchField) obj;
      return fField == f.fField && fEntityName.equals(f.fEntityName);
    }
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public synchronized int hashCode() {
    int typeHashCode = fEntityName == null ? 0 : fEntityName.hashCode();
    return (((fField + 2) * typeHashCode + 17)) * 37;
  }

  /*
   * @see java.lang.Object#toString()
   */
  @Override
  public synchronized String toString() {
    return fEntityName + ": " + fField; //$NON-NLS-1$
  }

  /**
   * Returns a String describing the state of this Entity.
   *
   * @return A String describing the state of this Entity.
   */
  public synchronized String toLongString() {
    return super.toString() + "(Field = " + fField + ", Class = " + fEntityName + ", Name = " + getName() + ", Search-Value-Type = " + getSearchValueType() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  }
}