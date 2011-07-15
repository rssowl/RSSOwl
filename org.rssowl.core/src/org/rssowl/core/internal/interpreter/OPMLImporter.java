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

package org.rssowl.core.internal.interpreter;

import static org.rssowl.core.internal.interpreter.OPMLConstants.RSSOWL_NS;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.interpreter.OPMLConstants.Attributes;
import org.rssowl.core.internal.interpreter.OPMLConstants.Tag;
import org.rssowl.core.internal.newsaction.CopyNewsAction;
import org.rssowl.core.internal.newsaction.LabelNewsAction;
import org.rssowl.core.internal.newsaction.MoveNewsAction;
import org.rssowl.core.interpreter.ITypeImporter;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.IPersistable;
import org.rssowl.core.persist.IPreference;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISearchValueType;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferenceType;
import org.rssowl.core.persist.reference.FeedLinkReference;
import org.rssowl.core.util.StringUtils;
import org.rssowl.core.util.URIUtils;

import java.io.Serializable;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Importer for the popular OPML Format. Will create a new Folder that contains
 * all Folders and Feeds of the OPML.
 *
 * @author bpasero
 */
public class OPMLImporter implements ITypeImporter {

  /*
   * @see
   * org.rssowl.core.interpreter.ITypeImporter#importFrom(org.jdom.Document)
   */
  public List<IEntity> importFrom(Document document) {
    DateFormat dateFormat = DateFormat.getDateInstance();
    Element root = document.getRootElement();

    /* Interpret Children */
    List<?> feedChildren = root.getChildren();
    for (Iterator<?> iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Process Body */
      if (Tag.BODY.get().equals(name))
        return processBody(child, dateFormat);
    }

    return null;
  }

  private List<IEntity> processBody(Element body, DateFormat dateFormat) {
    IFolder defaultRootFolder = Owl.getModelFactory().createFolder(null, null, Messages.OPMLImporter_BOOKMARKS);
    defaultRootFolder.setProperty(ITypeImporter.TEMPORARY_FOLDER, true);
    List<IEntity> importedEntities = new ArrayList<IEntity>();

    /* Interpret Children */
    List<?> feedChildren = body.getChildren();
    for (Iterator<?> iter = feedChildren.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Process Outline */
      if (Tag.OUTLINE.get().equals(name))
        processOutline(child, defaultRootFolder, importedEntities, dateFormat);

      /* Process Saved Search */
      else if (Tag.SAVED_SEARCH.get().equals(name))
        processSavedSearch(child, defaultRootFolder, dateFormat);

      /* Process News Bin */
      else if (Tag.BIN.get().equals(name))
        processNewsBin(child, defaultRootFolder);

      /* Process Label */
      else if (Tag.LABEL.get().equals(name))
        processLabel(child, importedEntities);

      /* Process News Filter */
      else if (Tag.FILTER.get().equals(name))
        processFilter(child, importedEntities, dateFormat);

      /* Process Global/Eclipse Preferences */
      else if (Tag.PREFERENCE.get().equals(name))
        processPreference(child, importedEntities);
    }

    /* Only add the default root folder if actually used */
    if (!defaultRootFolder.getChildren().isEmpty())
      importedEntities.add(defaultRootFolder);

    return importedEntities;
  }

  private void processFilter(Element filterElement, List<IEntity> importedEntities, DateFormat dateFormat) {
    IModelFactory factory = Owl.getModelFactory();

    String name = filterElement.getAttributeValue(Attributes.NAME.get());
    int order = Integer.parseInt(filterElement.getAttributeValue(Attributes.ORDER.get()));
    boolean isEnabled = Boolean.parseBoolean(filterElement.getAttributeValue(Attributes.ENABLED.get()));
    boolean matchAllNews = Boolean.parseBoolean(filterElement.getAttributeValue(Attributes.MATCH_ALL_NEWS.get()));

    /* Search if provided */
    ISearch search = null;
    Element searchElement = filterElement.getChild(Tag.SEARCH.get(), RSSOWL_NS);
    if (searchElement != null) {
      search = factory.createSearch(null);
      search.setMatchAllConditions(Boolean.parseBoolean(searchElement.getAttributeValue(Attributes.MATCH_ALL_CONDITIONS.get())));

      /* Search Conditions */
      List<?> conditions = searchElement.getChildren(Tag.SEARCH_CONDITION.get(), RSSOWL_NS);
      for (int i = 0; i < conditions.size(); i++) {
        try {
          Element condition = (Element) conditions.get(i);

          ISearchCondition searchCondition = processSearchCondition(condition, dateFormat);
          if (searchCondition != null)
            search.addSearchCondition(searchCondition);
        } catch (NumberFormatException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        } catch (ParseException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }
    }

    /* Filter */
    ISearchFilter filter = factory.createSearchFilter(null, search, name);
    filter.setEnabled(isEnabled);
    filter.setMatchAllNews(matchAllNews);
    filter.setOrder(order);

    /* Filter Actions */
    List<?> actions = filterElement.getChildren(Tag.ACTION.get(), RSSOWL_NS);
    for (int i = 0; i < actions.size(); i++) {
      Element action = (Element) actions.get(i);
      String id = action.getAttributeValue(Attributes.ID.get());
      String data = action.getAttributeValue(Attributes.DATA.get());

      IFilterAction filterAction = factory.createFilterAction(id);
      if (data != null) {

        /* Special case Label Action */
        if (LabelNewsAction.ID.equals(id)) {
          Long labelId = Long.parseLong(data);
          filterAction.setData(labelId);
        }

        /* Special case Move/Copy Action */
        else if (MoveNewsAction.ID.equals(id) || CopyNewsAction.ID.equals(id)) {
          String[] binIds = data.split(OPMLConstants.SEPARATOR);
          Long[] binIdsLong = new Long[binIds.length];
          for (int j = 0; j < binIds.length; j++) {
            binIdsLong[j] = Long.parseLong(binIds[j]);
          }

          filterAction.setData(binIdsLong);
        }

        /* Any other Action */
        else {
          filterAction.setData(data);
        }
      }

      /* Look for Action Properties */
      else {
        List<?> actionProperties = action.getChildren(Tag.ACTION_PROPERTY.get(), RSSOWL_NS);
        if (!actionProperties.isEmpty()) {
          Properties properties = new Properties();
          for (Object actionProperty : actionProperties) {
            Element actionPropertyElement = (Element) actionProperty;
            String key = actionPropertyElement.getAttributeValue(Attributes.ID.get());
            String value = actionPropertyElement.getAttributeValue(Attributes.VALUE.get());

            properties.setProperty(key, value);
          }

          filterAction.setData(properties);
        }
      }

      filter.addAction(filterAction);
    }

    importedEntities.add(filter);
  }

  private void processSavedSearch(Element savedSearchElement, IFolder folder, DateFormat dateFormat) {
    String name = savedSearchElement.getAttributeValue(Attributes.NAME.get());
    boolean matchAllConditions = Boolean.parseBoolean(savedSearchElement.getAttributeValue(Attributes.MATCH_ALL_CONDITIONS.get()));

    ISearchMark searchmark = Owl.getModelFactory().createSearchMark(null, folder, name);
    searchmark.setMatchAllConditions(matchAllConditions);

    /* Recursively Interpret Children */
    List<?> children = savedSearchElement.getChildren();
    for (Iterator<?> iter = children.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String childName = child.getName().toLowerCase();

      /* Process Search Condition */
      if (Tag.SEARCH_CONDITION.get().equals(childName)) {
        try {
          ISearchCondition searchCondition = processSearchCondition(child, dateFormat);
          if (searchCondition != null)
            searchmark.addSearchCondition(searchCondition);
        } catch (NumberFormatException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        } catch (ParseException e) {
          Activator.getDefault().logError(e.getMessage(), e);
        }
      }

      /* Process Preference */
      else if (Tag.PREFERENCE.get().equals(childName))
        processPreference(searchmark, child);
    }
  }

  private ISearchCondition processSearchCondition(Element conditionElement, DateFormat dateFormat) throws ParseException {

    /* Search Specifier */
    Element specifierElement = conditionElement.getChild(Tag.SPECIFIER.get(), RSSOWL_NS);
    if (specifierElement == null)
      return null;
    SearchSpecifier searchSpecifier = SearchSpecifier.values()[Integer.parseInt(specifierElement.getAttributeValue(Attributes.ID.get()))];

    /* Search Value */
    Element valueElement = conditionElement.getChild(Tag.SEARCH_VALUE.get(), RSSOWL_NS);
    if (valueElement == null)
      return null;
    Object value = getValue(valueElement, RSSOWL_NS, dateFormat);
    if (value == null)
      return null;

    /* Search Field */
    Element fieldElement = conditionElement.getChild(Tag.SEARCH_FIELD.get(), RSSOWL_NS);
    if (fieldElement == null)
      return null;
    String entityName = fieldElement.getAttributeValue(Attributes.ENTITY.get());

    String fieldName = fieldElement.getAttributeValue(Attributes.NAME.get());
    String fieldId = fieldElement.getAttributeValue(Attributes.ID.get());

    ISearchField searchField;
    if (StringUtils.isSet(fieldName)) //Pre 2.0 M10
      searchField = Owl.getModelFactory().createSearchField(getFieldID(fieldName), entityName);
    else //Since 2.0 M10
      searchField = Owl.getModelFactory().createSearchField(Integer.parseInt(fieldId), entityName);

    return Owl.getModelFactory().createSearchCondition(searchField, searchSpecifier, value);
  }

  /* Support for RSSOwl pre 2.0 M10 */
  private int getFieldID(String fieldName) {
    if ("allFields".equals(fieldName)) //$NON-NLS-1$
      return IEntity.ALL_FIELDS;

    if ("title".equals(fieldName)) //$NON-NLS-1$
      return INews.TITLE;

    if ("link".equals(fieldName)) //$NON-NLS-1$
      return INews.LINK;

    if ("description".equals(fieldName)) //$NON-NLS-1$
      return INews.DESCRIPTION;

    if ("publishDate".equals(fieldName)) //$NON-NLS-1$
      return INews.PUBLISH_DATE;

    if ("modifiedDate".equals(fieldName)) //$NON-NLS-1$
      return INews.MODIFIED_DATE;

    if ("receiveDate".equals(fieldName)) //$NON-NLS-1$
      return INews.RECEIVE_DATE;

    if ("author".equals(fieldName)) //$NON-NLS-1$
      return INews.AUTHOR;

    if ("comments".equals(fieldName)) //$NON-NLS-1$
      return INews.COMMENTS;

    if ("guid".equals(fieldName)) //$NON-NLS-1$
      return INews.GUID;

    if ("source".equals(fieldName)) //$NON-NLS-1$
      return INews.SOURCE;

    if ("hasAttachments".equals(fieldName)) //$NON-NLS-1$
      return INews.HAS_ATTACHMENTS;

    if ("attachments".equals(fieldName)) //$NON-NLS-1$
      return INews.ATTACHMENTS_CONTENT;

    if ("categories".equals(fieldName)) //$NON-NLS-1$
      return INews.CATEGORIES;

    if ("isFlagged".equals(fieldName)) //$NON-NLS-1$
      return INews.IS_FLAGGED;

    if ("state".equals(fieldName)) //$NON-NLS-1$
      return INews.STATE;

    if ("label".equals(fieldName)) //$NON-NLS-1$
      return INews.LABEL;

    if ("rating".equals(fieldName)) //$NON-NLS-1$
      return INews.RATING;

    if ("feed".equals(fieldName)) //$NON-NLS-1$
      return INews.FEED;

    if ("ageInDays".equals(fieldName)) //$NON-NLS-1$
      return INews.AGE_IN_DAYS;

    if ("location".equals(fieldName)) //$NON-NLS-1$
      return INews.LOCATION;

    return IEntity.ALL_FIELDS;
  }

  private Object getValue(Element valueElement, Namespace namespace, DateFormat dateFormat) throws ParseException {
    Object value = null;
    int valueType = Integer.parseInt(valueElement.getAttributeValue(Attributes.TYPE.get()));

    List<?> locationElements = valueElement.getChildren(Tag.LOCATION.get(), namespace);
    List<?> newsStateElements = valueElement.getChildren(Tag.STATE.get(), namespace);

    /* Treat set of Locations separately */
    if (locationElements.size() > 0) {
      List<Long> folderIds = new ArrayList<Long>(locationElements.size());
      List<Long> bookmarkIds = new ArrayList<Long>(locationElements.size());
      List<Long> newsbinIds = new ArrayList<Long>(locationElements.size());

      for (int i = 0; i < locationElements.size(); i++) {
        Element locationElement = (Element) locationElements.get(i);
        Long id = Long.parseLong(locationElement.getAttributeValue(Attributes.VALUE.get()));
        boolean isFolder = Boolean.parseBoolean(locationElement.getAttributeValue(Attributes.IS_FOLDER.get()));
        boolean isBin = Boolean.parseBoolean(locationElement.getAttributeValue(Attributes.IS_BIN.get()));

        if (isFolder)
          folderIds.add(id);
        else if (isBin)
          newsbinIds.add(id);
        else
          bookmarkIds.add(id);
      }

      Long[][] result = new Long[3][];
      result[0] = folderIds.toArray(new Long[folderIds.size()]);
      result[1] = bookmarkIds.toArray(new Long[bookmarkIds.size()]);
      result[2] = newsbinIds.toArray(new Long[bookmarkIds.size()]);

      return result;
    }

    /* Treat set of News States separately */
    else if (newsStateElements.size() > 0) {
      List<INews.State> states = new ArrayList<INews.State>(newsStateElements.size());
      for (int i = 0; i < newsStateElements.size(); i++) {
        Element newsStateElement = (Element) newsStateElements.get(i);
        int ordinal = Integer.parseInt(newsStateElement.getAttributeValue(Attributes.VALUE.get()));
        states.add(INews.State.values()[ordinal]);
      }

      value = EnumSet.copyOf(states);
    }

    /* Any other Value */
    else {
      String valueAsString = valueElement.getAttributeValue(Attributes.VALUE.get());

      switch (valueType) {
        case ISearchValueType.BOOLEAN:
          value = Boolean.parseBoolean(valueAsString);
          break;

        case ISearchValueType.STRING:
          value = valueAsString;
          break;

        case ISearchValueType.LINK:
          value = valueAsString;
          break;

        case ISearchValueType.INTEGER:
          value = Integer.parseInt(valueAsString);
          break;

        case ISearchValueType.NUMBER:
          value = Integer.parseInt(valueAsString);
          break;

        case ISearchValueType.DATE:
          value = dateFormat.parse(valueAsString);
          break;

        case ISearchValueType.DATETIME:
          value = dateFormat.parse(valueAsString);
          break;

        case ISearchValueType.TIME:
          value = dateFormat.parse(valueAsString);
          break;

        case ISearchValueType.ENUM:
          value = valueAsString;
          break;
      }
    }

    return value;
  }

  private void processOutline(Element outline, IPersistable parent, List<IEntity> importedEntities, DateFormat dateFormat) {
    IEntity type = null;
    Long id = null;
    String title = null;
    String link = null;
    String homepage = null;
    String description = null;

    /* Interpret Attributes */
    List<?> attributes = outline.getAttributes();
    for (Iterator<?> iter = attributes.iterator(); iter.hasNext();) {
      Attribute attribute = (Attribute) iter.next();
      String name = attribute.getName();

      /* Link */
      if (name.toLowerCase().equals(Attributes.XML_URL.get().toLowerCase()))
        link = attribute.getValue();

      /* Title */
      else if (name.toLowerCase().equals(Attributes.TITLE.get()))
        title = attribute.getValue();

      /* Text */
      else if (title == null && name.toLowerCase().equals(Attributes.TEXT.get()))
        title = attribute.getValue();

      /* Homepage */
      else if (name.toLowerCase().equals(Attributes.HTML_URL.get().toLowerCase()))
        homepage = attribute.getValue();

      /* Description */
      else if (name.toLowerCase().equals(Attributes.DESCRIPTION.get()))
        description = attribute.getValue();
    }

    /* RSSOwl Namespace Attributes */
    Attribute idAttribute = outline.getAttribute(Attributes.ID.get(), RSSOWL_NS);
    if (idAttribute != null)
      id = Long.valueOf(idAttribute.getValue());

    boolean isSet = Boolean.parseBoolean(outline.getAttributeValue(Attributes.IS_SET.get(), RSSOWL_NS));

    /* Outline is a Folder */
    if (link == null && title != null) {
      type = Owl.getModelFactory().createFolder(null, isSet ? null : (IFolder) parent, title);

      /* Assign old ID value */
      if (id != null)
        type.setProperty(ID_KEY, id);

      if (isSet)
        importedEntities.add(type);
    }

    /* Outline is a BookMark */
    else {
      URI uri = link != null ? URIUtils.createURI(link) : null;
      if (uri != null) {

        /* If no Scheme is Provided, use HTTP as default */
        if (uri.getScheme() == null)
          uri = URIUtils.createURI(URIUtils.HTTP + link);

        /* Create the BookMark */
        FeedLinkReference feedLinkRef = new FeedLinkReference(uri);
        type = Owl.getModelFactory().createBookMark(null, (IFolder) parent, feedLinkRef, title != null ? title : link);

        /* Assign old ID value */
        if (id != null)
          type.setProperty(ID_KEY, id);

        /* Store Description if set */
        if (StringUtils.isSet(description))
          type.setProperty(ITypeImporter.DESCRIPTION_KEY, description);

        /* Store Homepage if set */
        if (StringUtils.isSet(homepage))
          type.setProperty(ITypeImporter.HOMEPAGE_KEY, homepage);
      }
    }

    /* In case this Outline Element did not represent a Entity */
    if (type == null)
      return;

    /* Recursively Interpret Children */
    List<?> children = outline.getChildren();
    for (Iterator<?> iter = children.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String name = child.getName().toLowerCase();

      /* Process Outline */
      if (Tag.OUTLINE.get().equals(name))
        processOutline(child, type, importedEntities, dateFormat);

      /* Process Saved Search */
      else if (Tag.SAVED_SEARCH.get().equals(name))
        processSavedSearch(child, (IFolder) type, dateFormat);

      /* Process News Bin */
      else if (Tag.BIN.get().equals(name))
        processNewsBin(child, (IFolder) type);

      /* Process Preference */
      else if (Tag.PREFERENCE.get().equals(name))
        processPreference(type, child);
    }
  }

  private void processLabel(Element labelElement, List<IEntity> importedEntities) {
    String id = labelElement.getAttributeValue(Attributes.ID.get());
    String name = labelElement.getAttributeValue(Attributes.NAME.get());
    String order = labelElement.getAttributeValue(Attributes.ORDER.get());
    String color = labelElement.getAttributeValue(Attributes.COLOR.get());

    ILabel label = Owl.getModelFactory().createLabel(null, name);
    label.setColor(color);
    label.setOrder(Integer.parseInt(order));
    label.setProperty(ID_KEY, Long.valueOf(id));

    importedEntities.add(label);
  }

  private void processNewsBin(Element newsBinElement, IFolder folder) {
    String name = newsBinElement.getAttributeValue(Attributes.NAME.get());

    /* RSSOwl Namespace Attributes */
    Long id = null;
    Attribute idAttribute = newsBinElement.getAttribute(Attributes.ID.get(), RSSOWL_NS);
    if (idAttribute != null)
      id = Long.valueOf(idAttribute.getValue());

    INewsBin newsbin = Owl.getModelFactory().createNewsBin(null, folder, name);

    /* Assign old ID value */
    if (id != null)
      newsbin.setProperty(ID_KEY, id);

    /* Recursively Interpret Children */
    List<?> children = newsBinElement.getChildren();
    for (Iterator<?> iter = children.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      String childName = child.getName().toLowerCase();

      /* Process Preference */
      if (Tag.PREFERENCE.get().equals(childName))
        processPreference(newsbin, child);
    }
  }

  private void processPreference(IEntity entity, Element element) {
    String key = element.getAttributeValue(Attributes.ID.get());
    String value = element.getAttributeValue(Attributes.VALUE.get());
    String type = element.getAttributeValue(Attributes.TYPE.get());

    if (StringUtils.isSet(key) && value != null && type != null)
      entity.setProperty(key, getPropertyValue(value, IPreferenceType.values()[Integer.parseInt(type)]));
  }

  private Serializable getPropertyValue(String value, IPreferenceType type) {
    switch (type) {
      case BOOLEAN:
        return Boolean.valueOf(value);

      case INTEGER:
        return Integer.valueOf(value);

      case INTEGERS:
        String[] values = value.split(OPMLConstants.SEPARATOR);
        int[] intValues = new int[values.length];
        for (int i = 0; i < values.length; i++) {
          intValues[i] = Integer.parseInt(values[i]);
        }
        return intValues;

      case LONG:
        return Long.valueOf(value);

      case LONGS:
        values = value.split(OPMLConstants.SEPARATOR);
        long[] longValues = new long[values.length];
        for (int i = 0; i < values.length; i++) {
          longValues[i] = Long.parseLong(values[i]);
        }
        return longValues;

      case STRING:
        return value;

      case STRINGS:
        return value.split(OPMLConstants.SEPARATOR);
    }

    return value;
  }

  private void processPreference(Element element, List<IEntity> importedEntities) {
    String key = element.getAttributeValue(Attributes.ID.get());
    String value = element.getAttributeValue(Attributes.VALUE.get());
    String typeVal = element.getAttributeValue(Attributes.TYPE.get());
    String kindVal = element.getAttributeValue(Attributes.KIND.get());

    if (StringUtils.isSet(key) && value != null && typeVal != null && kindVal != null) {
      IPreferenceScope.Kind kind = IPreferenceScope.Kind.values()[Integer.parseInt(kindVal)];
      IPreferenceType type = IPreferenceType.values()[Integer.parseInt(typeVal)];
      IPreference preference = Owl.getModelFactory().createPreference(key);
      preference.setProperty(ITypeImporter.DATA_KEY, new Object[] { kind, type });

      switch (type) {
        case BOOLEAN:
          preference.putBooleans(Boolean.parseBoolean(value));
          break;
        case INTEGER:
          preference.putIntegers(Integer.parseInt(value));
          break;
        case INTEGERS:
          preference.putIntegers((int[]) getPropertyValue(value, type));
          break;
        case LONG:
          preference.putLongs(Long.parseLong(value));
          break;
        case LONGS:
          preference.putLongs((long[]) getPropertyValue(value, type));
          break;
        case STRING:
          preference.putStrings(value);
          break;
        case STRINGS:
          preference.putStrings((String[]) getPropertyValue(value, type));
          break;
      }

      importedEntities.add(preference);
    }
  }
}