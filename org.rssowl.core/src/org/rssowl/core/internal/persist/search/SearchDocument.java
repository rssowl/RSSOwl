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

package org.rssowl.core.internal.persist.search;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumberTools;
import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.ICategory;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.util.HTMLStripReader;

import java.io.StringReader;
import java.util.Date;
import java.util.List;

/**
 * Base class for all {@link IEntity} based search documents in the fulltext
 * index.
 *
 * @author ijuma
 * @author bpasero
 * @param <T> the {@link IEntity} that fills this document.
 */
public abstract class SearchDocument<T extends IEntity> {

  /** The value of an Entitie's ID as integer */
  protected static final int ENTITY_ID = -2;

  /** The value of an Entitie's ID as String */
  public static final String ENTITY_ID_TEXT = String.valueOf(ENTITY_ID);

  private final Document fDocument;
  private final T fType;

  /**
   * @param type the {@link IEntity} that fills this document.
   */
  public SearchDocument(T type) {
    Assert.isLegal(type != null);
    fType = type;
    fDocument = new Document();
  }

  /**
   * @return the {@link IEntity} that fills this document.
   */
  public final T getType() {
    return fType;
  }

  /**
   * @return the search document to be indexed.
   */
  public final Document getDocument() {
    return fDocument;
  }

  /**
   * @return <code>true</code> if fields have been added and <code>false</code>
   * otherwise.
   */
  public abstract boolean addFields();

  /**
   * @param fields the list of fields to add to this document
   * @return <code>true</code> if fields have been added and <code>false</code>
   * otherwise.
   */
  protected boolean addFieldsToDocument(List<Field> fields) {
    if (fields.size() == 0)
      return false;

    for (Field field : fields) {
      getDocument().add(field);
    }

    return true;
  }

  /**
   * @param fields the list of fields to add to
   * @param field the field to add
   * @return <code>true</code> if fields have been added and <code>false</code>
   * otherwise.
   */
  protected boolean addField(List<Field> fields, Field field) {
    if (field == null)
      return false;

    fields.add(field);

    return true;
  }

  /**
   * Creates a new <code>Field</code> representing the entities ID this Document
   * wraps around.
   *
   * @return Field
   */
  protected Field createDocumentIDField() {
    return new Field(ENTITY_ID_TEXT, String.valueOf(getType().getId()), Store.YES, Index.UN_TOKENIZED);
  }

  /**
   * Creates a new <code>Field</code> from the given ID value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param entity the entity the field is provided from
   * @param store one of the {@link Store} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createIDField(int fieldConstant, IEntity entity, Store store) {
    return new Field(String.valueOf(fieldConstant), String.valueOf(entity.getId()), store, Index.UN_TOKENIZED);
  }

  /**
   * Creates a new <code>Field</code> from the given long value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createLongField(int fieldConstant, long value, Store store) {
    String valueText = NumberTools.longToString(value);
    return new Field(String.valueOf(fieldConstant), valueText, store, Index.UN_TOKENIZED);
  }

  /**
   * Creates a new <code>Field</code> from the given boolean value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createBooleanField(int fieldConstant, boolean value, Store store) {
    return new Field(String.valueOf(fieldConstant), String.valueOf(value), store, Index.UN_TOKENIZED);
  }

  /**
   * Creates a new <code>Field</code> from the given Date value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createDateField(int fieldConstant, Date value, Store store) {
    return createDateField(fieldConstant, value, store, Resolution.DAY);
  }

  /**
   * Creates a new <code>Field</code> from the given Date value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @param resolution one of the {@link Resolution} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createDateField(int fieldConstant, Date value, Store store, Resolution resolution) {
    if (value == null)
      return null;

    String valueText = DateTools.dateToString(value, resolution);
    return new Field(String.valueOf(fieldConstant), valueText, store, Index.UN_TOKENIZED);
  }

  /**
   * Creates a new <code>Field</code> from the given URI value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @param index one of the {@link Index} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createURIField(int fieldConstant, String value, Store store, Index index) {
    if (value == null)
      return null;

    return new Field(String.valueOf(fieldConstant), value.toLowerCase(), store, index);
  }

  /**
   * Creates a new <code>Field</code> from the given String value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @param index one of the {@link Index} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createStringField(int fieldConstant, String value, Store store, Index index) {
    if (value == null)
      return null;

    return new Field(String.valueOf(fieldConstant), value, store, index);
  }

  /**
   * Creates a new <code>Field</code> from the given String value containing
   * HTML. The HTML will be stripped from the field and entities replaced. The
   * value is <em>not stored</em>and <em>tokenized indexed</em>.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createHTMLField(int fieldConstant, String value) {
    if (value == null)
      return null;

    return new Field(String.valueOf(fieldConstant), new HTMLStripReader(new StringReader(value)));
  }

  /**
   * Creates a new <code>Field</code> from the given enum value.
   *
   * @param fieldConstant the constant identifying the target field
   * @param value the value of the field
   * @param store one of the {@link Store} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createEnumField(int fieldConstant, Enum<?> value, Store store) {
    if (value == null)
      return null;

    return new Field(String.valueOf(fieldConstant), String.valueOf(value.ordinal()), store, Index.UN_TOKENIZED);
  }

  /**
   * Creates a new <code>Field</code> from the given {@link IPerson}.
   *
   * @param fieldConstant the constant identifying the target field
   * @param person the value of the field
   * @param store one of the {@link Store} constants.
   * @param index one of the {@link Index} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createPersonField(int fieldConstant, IPerson person, Store store, Index index) {
    if (person == null)
      return null;

    /* Add Name and EMail */
    if (person.getName() != null && person.getEmail() != null) {
      return createStringField(fieldConstant, person.getName() + " " + person.getEmail().toString(), store, index); //$NON-NLS-1$
    }

    /* Add Name if present */
    if (person.getName() != null)
      return createStringField(fieldConstant, person.getName(), store, index);

    /* Add EMail if present */
    else if (person.getEmail() != null)
      return createURIField(fieldConstant, person.getEmail().toString(), store, Index.UN_TOKENIZED);

    return null;
  }

  /**
   * Creates a new <code>Field</code> from the given list of {@link ICategory}.
   *
   * @param fieldConstant the constant identifying the target field
   * @param categories the value of the field
   * @param store one of the {@link Store} constants.
   * @param index one of the {@link Index} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createCategoriesField(int fieldConstant, List<ICategory> categories, Store store, Index index) {
    if (categories == null || categories.size() == 0)
      return null;

    /* Convert categories to a single String */
    StringBuilder categoryValues = new StringBuilder();
    for (ICategory category : categories) {
      if (category.getName() != null)
        categoryValues.append(category.getName()).append('\n');
    }

    if (categoryValues.length() > 0)
      return createStringField(fieldConstant, categoryValues.toString(), store, index);

    return null;
  }

  /**
   * Creates a new <code>Field</code> from the given list of {@link IAttachment}
   * .
   *
   * @param fieldConstant the constant identifying the target field
   * @param attachments the value of the field
   * @param store one of the {@link Store} constants.
   * @param index one of the {@link Index} constants.
   * @return Field the {@link Field} that can be used for indexing.
   */
  protected Field createAttachmentsField(int fieldConstant, List<IAttachment> attachments, Store store, Index index) {
    if (attachments == null || attachments.size() == 0)
      return null;

    /* Convert attachments to a single String */
    StringBuilder attachmentValues = new StringBuilder();
    for (IAttachment attachment : attachments) {
      if (attachment.getLink() != null)
        attachmentValues.append(attachment.getLink()).append(" "); //$NON-NLS-1$

      if (attachment.getType() != null)
        attachmentValues.append(attachment.getType()).append(" "); //$NON-NLS-1$
    }

    if (attachmentValues.length() > 0)
      return createStringField(fieldConstant, attachmentValues.toString(), store, index);

    return null;
  }
}