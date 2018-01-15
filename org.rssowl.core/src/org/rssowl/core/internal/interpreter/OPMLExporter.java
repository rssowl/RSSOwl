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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.internal.interpreter.OPMLConstants.Attributes;
import org.rssowl.core.internal.interpreter.OPMLConstants.Tag;
import org.rssowl.core.interpreter.ITypeExporter;
import org.rssowl.core.interpreter.InterpreterException;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IFolderChild;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.pref.IPreferenceScope;
import org.rssowl.core.persist.pref.IPreferenceScope.Kind;
import org.rssowl.core.persist.pref.IPreferenceType;
import org.rssowl.core.persist.pref.Preference;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Implementation of {@link ITypeExporter} for the OPML XML format.
 *
 * @author bpasero
 */
public class OPMLExporter implements ITypeExporter {

  /* Default Encoding */
  private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

  /*
   * @see org.rssowl.core.interpreter.ITypeExporter#exportTo(java.io.File,
   * java.util.Collection, java.util.Set)
   */
  @Override
  public void exportTo(File destination, Collection<? extends IFolderChild> elements, Set<Options> options) throws InterpreterException {
    Format format = Format.getPrettyFormat();
    format.setEncoding(UTF_8);
    XMLOutputter output = new XMLOutputter(format);
    DateFormat dateFormat = DateFormat.getDateInstance();

    Document document = new Document();
    Element root = new Element(Tag.OPML.get());
    root.setAttribute(Attributes.VERSION.get(), "1.1"); //$NON-NLS-1$
    root.addNamespaceDeclaration(RSSOWL_NS);
    document.setRootElement(root);

    /* Head */
    Element head = new Element(Tag.HEAD.get());
    root.addContent(head);

    Element title = new Element(Tag.TITLE.get());
    title.setText(Messages.OPMLExporter_RSSOWL_SUBSCRIPTIONS);
    head.addContent(title);

    Element dateModified = new Element(Tag.DATE_MODIFIED.get());
    dateModified.setText(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").format(new Date())); //$NON-NLS-1$
    head.addContent(dateModified);

    /* Body */
    Element body = new Element(Tag.BODY.get());
    root.addContent(body);

    boolean exportPreferences = (options != null && options.contains(Options.EXPORT_PREFERENCES));

    /* Export Folder Childs */
    if (elements != null && !elements.isEmpty()) {
      Map<IFolder, Element> mapFolderToElement = new HashMap<IFolder, Element>();
      mapFolderToElement.put(null, body);

      /* Ensure that all Parent Elements exist */
      repairHierarchy(mapFolderToElement, elements, exportPreferences);

      /* Now export Elements */
      exportFolderChilds(mapFolderToElement, elements, exportPreferences, dateFormat);
    }

    /* Export Labels */
    if (options != null && options.contains(Options.EXPORT_LABELS))
      exportLabels(body);

    /* Export Filters */
    if (options != null && options.contains(Options.EXPORT_FILTERS))
      exportFilters(body, dateFormat);

    /* Export Preferences */
    if (exportPreferences)
      exportPreferences(body);

    /* Write to File */
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(destination);
      output.output(document, out);
      out.close();
    } catch (FileNotFoundException e) {
      throw new InterpreterException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    } catch (IOException e) {
      throw new InterpreterException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          throw new InterpreterException(Activator.getDefault().createErrorStatus(e.getMessage(), e));
        }
      }
    }
  }

  private void repairHierarchy(Map<IFolder, Element> mapFolderToElement, Collection<? extends IFolderChild> elementsToExport, boolean exportPreferences) {

    /* Retrieve all Parents */
    Set<IFolder> allParentFolders = new HashSet<IFolder>();
    for (IFolderChild element : elementsToExport) {
      fillParents(allParentFolders, element);
    }

    /* Return if Hierarchy is already consistent */
    if (allParentFolders.isEmpty())
      return;

    /* Create Elements for all Parents */
    for (IFolder parent : allParentFolders) {
      Element folderElement = createElement(parent, exportPreferences);
      mapFolderToElement.put(parent, folderElement);
    }

    /* Connect all Parent Nodes according to hierarchy */
    for (IFolder parent : allParentFolders) {
      Element folderElement = mapFolderToElement.get(parent);
      Element parentElement = mapFolderToElement.get(parent.getParent());

      parentElement.addContent(folderElement);
    }
  }

  private void fillParents(Set<IFolder> parents, IFolderChild child) {
    IFolder parent = child.getParent();
    if (parent != null) {
      parents.add(parent);
      fillParents(parents, parent);
    }
  }

  private void exportFolderChilds(Map<IFolder, Element> mapFolderToElement, Collection<? extends IFolderChild> childs, boolean exportPreferences, DateFormat df) {
    for (IFolderChild child : childs) {

      /* Export Folder */
      if (child instanceof IFolder)
        exportFolder(mapFolderToElement, (IFolder) child, exportPreferences, df);

      /* Export Bookmark, Search or Bin */
      else if (child instanceof IMark)
        exportMark(mapFolderToElement, (IMark) child, exportPreferences, df);
    }
  }

  private Element createElement(IFolder folder, boolean exportPreferences) {
    Element folderElement = new Element(Tag.OUTLINE.get());
    folderElement.setAttribute(Attributes.TEXT.get(), folder.getName());
    folderElement.setAttribute(Attributes.IS_SET.get(), String.valueOf(folder.getParent() == null), RSSOWL_NS);
    folderElement.setAttribute(Attributes.ID.get(), String.valueOf(folder.getId()), RSSOWL_NS);

    /* Export Preferences if set */
    if (exportPreferences)
      exportPreferences(folderElement, folder);

    return folderElement;
  }

  private void exportFolder(Map<IFolder, Element> mapFolderToElement, IFolder folder, boolean exportPreferences, DateFormat df) {
    Element folderElement = createElement(folder, exportPreferences);

    /* Store in Map for Childs to Use */
    mapFolderToElement.put(folder, folderElement);

    /* Connect Node according to hierarchy */
    mapFolderToElement.get(folder.getParent()).addContent(folderElement);

    /* Proceed with Folder Childs */
    exportFolderChilds(mapFolderToElement, folder.getChildren(), exportPreferences, df);
  }

  private void exportPreferences(Element parent, IFolderChild child) {
    Map<String, Serializable> properties = child.getProperties();
    if (properties != null) {
      Set<Entry<String, Serializable>> entries = properties.entrySet();
      for (Entry<String, Serializable> entry : entries) {
        if (StringUtils.isSet(entry.getKey()) && entry.getValue() != null) {
          String value = getValueAsString(entry.getValue());

          if (value != null) {
            Element prefElement = new Element(Tag.PREFERENCE.get(), RSSOWL_NS);
            prefElement.setAttribute(Attributes.ID.get(), entry.getKey());
            prefElement.setAttribute(Attributes.VALUE.get(), value);
            prefElement.setAttribute(Attributes.TYPE.get(), String.valueOf(IPreferenceType.getType(entry.getValue()).ordinal()));
            parent.addContent(prefElement);
          }
        }
      }
    }
  }

  private String getValueAsString(Object property) {
    if (property instanceof String)
      return (String) property;

    if (property instanceof Long || property instanceof Integer || property instanceof Boolean)
      return String.valueOf(property);

    if (property instanceof long[]) {
      long[] values = (long[]) property;

      StringBuilder builder = new StringBuilder();
      for (long val : values) {
        builder.append(val).append(OPMLConstants.SEPARATOR);
      }

      if (values.length > 0)
        builder.delete(builder.length() - 1, builder.length());

      return builder.toString();
    }

    if (property instanceof int[]) {
      int[] values = (int[]) property;

      StringBuilder builder = new StringBuilder();
      for (int val : values) {
        builder.append(val).append(OPMLConstants.SEPARATOR);
      }

      if (values.length > 0)
        builder.delete(builder.length() - 1, builder.length());

      return builder.toString();
    }

    if (property instanceof String[]) {
      String[] values = (String[]) property;

      StringBuilder builder = new StringBuilder();
      for (String val : values) {
        builder.append(val).append(OPMLConstants.SEPARATOR);
      }

      if (values.length > 0)
        builder.delete(builder.length() - 1, builder.length());

      return builder.toString();
    }

    return null;
  }

  private void exportMark(Map<IFolder, Element> mapFolderToElement, IMark mark, boolean exportPreferences, DateFormat df) {
    String name = mark.getName();
    Element element = null;

    /* Export BookMark */
    if (mark instanceof IBookMark) {
      String link = ((IBookMark) mark).getFeedLinkReference().getLinkAsText();

      element = new Element(Tag.OUTLINE.get());
      element.setAttribute(Attributes.TEXT.get(), name);
      element.setAttribute(Attributes.XML_URL.get(), link);
      element.setAttribute(Attributes.ID.get(), String.valueOf(mark.getId()), RSSOWL_NS);
      mapFolderToElement.get(mark.getParent()).addContent(element);
    }

    /* Export SearchMark */
    else if (mark instanceof ISearchMark) {
      ISearchMark searchMark = (ISearchMark) mark;
      List<ISearchCondition> conditions = searchMark.getSearchConditions();

      element = new Element(Tag.SAVED_SEARCH.get(), RSSOWL_NS);
      element.setAttribute(Attributes.NAME.get(), name);
      element.setAttribute(Attributes.MATCH_ALL_CONDITIONS.get(), String.valueOf(searchMark.matchAllConditions()));
      element.setAttribute(Attributes.ID.get(), String.valueOf(mark.getId()), RSSOWL_NS);
      mapFolderToElement.get(mark.getParent()).addContent(element);

      for (ISearchCondition condition : conditions) {
        Element conditionElement = new Element(Tag.SEARCH_CONDITION.get(), RSSOWL_NS);
        element.addContent(conditionElement);

        if (condition.getValue() != null)
          fillElement(conditionElement, condition, df);
      }
    }

    /* Export Newsbin */
    else if (mark instanceof INewsBin) {
      element = new Element(Tag.BIN.get(), RSSOWL_NS);
      element.setAttribute(Attributes.NAME.get(), name);
      element.setAttribute(Attributes.ID.get(), String.valueOf(mark.getId()), RSSOWL_NS);
      mapFolderToElement.get(mark.getParent()).addContent(element);
    }

    /* Export Preferences if set */
    if (element != null && exportPreferences)
      exportPreferences(element, mark);
  }

  private void exportFilters(Element body, DateFormat df) {
    Collection<ISearchFilter> filters = DynamicDAO.loadAll(ISearchFilter.class);
    for (ISearchFilter filter : filters) {
      String name = filter.getName();
      int order = filter.getOrder();
      boolean isEnabled = filter.isEnabled() && !CoreUtils.isOrphaned(filter);
      boolean matchAllNews = filter.matchAllNews();

      Element filterElement = new Element(Tag.FILTER.get(), RSSOWL_NS);
      filterElement.setAttribute(Attributes.NAME.get(), name);
      filterElement.setAttribute(Attributes.ORDER.get(), String.valueOf(order));
      filterElement.setAttribute(Attributes.ENABLED.get(), String.valueOf(isEnabled));
      filterElement.setAttribute(Attributes.MATCH_ALL_NEWS.get(), String.valueOf(matchAllNews));
      body.addContent(filterElement);

      /* Export Search if provided */
      ISearch search = filter.getSearch();
      if (search != null) {
        List<ISearchCondition> conditions = search.getSearchConditions();

        Element searchElement = new Element(Tag.SEARCH.get(), RSSOWL_NS);
        searchElement.setAttribute(Attributes.MATCH_ALL_CONDITIONS.get(), String.valueOf(search.matchAllConditions()));
        filterElement.addContent(searchElement);

        for (ISearchCondition condition : conditions) {
          Element conditionElement = new Element(Tag.SEARCH_CONDITION.get(), RSSOWL_NS);
          searchElement.addContent(conditionElement);

          if (condition.getValue() != null)
            fillElement(conditionElement, condition, df);
        }
      }

      /* Export Actions */
      List<IFilterAction> actions = filter.getActions();
      for (IFilterAction action : actions) {
        String actionId = action.getActionId();

        Element actionElement = new Element(Tag.ACTION.get(), RSSOWL_NS);
        actionElement.setAttribute(Attributes.ID.get(), actionId);

        /* Action Data as Properties */
        if (action.getData() instanceof Properties) {
          Properties data = (Properties) action.getData();
          Set<Entry<Object, Object>> entries = data.entrySet();
          for (Entry<Object, Object> entry : entries) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();

            Element actionProperty = new Element(Tag.ACTION_PROPERTY.get(), RSSOWL_NS);
            actionProperty.setAttribute(Attributes.ID.get(), key);
            actionProperty.setAttribute(Attributes.VALUE.get(), value);
            actionElement.addContent(actionProperty);
          }
        }

        /* Action Data as Primitive */
        else {
          String data = toString(action.getData());
          if (data != null)
            actionElement.setAttribute(Attributes.DATA.get(), data);
        }

        filterElement.addContent(actionElement);
      }
    }
  }

  private void fillElement(Element conditionElement, ISearchCondition condition, DateFormat df) {

    /* Search Specifier */
    Element searchSpecifier = new Element(Tag.SPECIFIER.get(), RSSOWL_NS);
    searchSpecifier.setAttribute(Attributes.ID.get(), String.valueOf(condition.getSpecifier().ordinal()));
    conditionElement.addContent(searchSpecifier);

    /* Search Condition: Location */
    if (condition.getValue() instanceof Long[][]) {
      List<IFolderChild> locations = CoreUtils.toEntities((Long[][]) condition.getValue());

      Element searchValue = new Element(Tag.SEARCH_VALUE.get(), RSSOWL_NS);
      searchValue.setAttribute(Attributes.TYPE.get(), String.valueOf(condition.getField().getSearchValueType().getId()));
      conditionElement.addContent(searchValue);

      for (IFolderChild child : locations) {
        boolean isFolder = (child instanceof IFolder);
        boolean isNewsbin = (child instanceof INewsBin);

        Element location = new Element(Tag.LOCATION.get(), RSSOWL_NS);
        location.setAttribute(Attributes.IS_BIN.get(), String.valueOf(isNewsbin));
        location.setAttribute(Attributes.IS_FOLDER.get(), String.valueOf(isFolder));
        location.setAttribute(Attributes.VALUE.get(), String.valueOf(child.getId()));
        searchValue.addContent(location);
      }
    }

    /* Single Value */
    else if (!EnumSet.class.isAssignableFrom(condition.getValue().getClass())) {
      Element searchValue = new Element(Tag.SEARCH_VALUE.get(), RSSOWL_NS);
      searchValue.setAttribute(Attributes.TYPE.get(), String.valueOf(condition.getField().getSearchValueType().getId()));
      searchValue.setAttribute(Attributes.VALUE.get(), getValueString(df, condition));
      conditionElement.addContent(searchValue);
    }

    /* Multiple Values */
    else {
      EnumSet<?> values = ((EnumSet<?>) condition.getValue());

      Element searchValue = new Element(Tag.SEARCH_VALUE.get(), RSSOWL_NS);
      searchValue.setAttribute(Attributes.TYPE.get(), String.valueOf(condition.getField().getSearchValueType().getId()));
      conditionElement.addContent(searchValue);

      for (Enum<?> enumValue : values) {
        Element state = new Element(Tag.STATE.get(), RSSOWL_NS);
        state.setAttribute(Attributes.VALUE.get(), String.valueOf(enumValue.ordinal()));
        searchValue.addContent(state);
      }
    }

    /* Search Field */
    Element field = new Element(Tag.SEARCH_FIELD.get(), RSSOWL_NS);
    field.setAttribute(Attributes.ID.get(), String.valueOf(condition.getField().getId()));
    field.setAttribute(Attributes.ENTITY.get(), condition.getField().getEntityName());
    conditionElement.addContent(field);
  }

  private String toString(Object data) {
    if (data == null)
      return null;

    if (data instanceof String)
      return (String) data;

    if (data instanceof Long)
      return String.valueOf(data);

    if (data instanceof Long[]) {
      Long[] value = (Long[]) data;
      StringBuilder builder = new StringBuilder();
      for (Long val : value) {
        builder.append(val).append(OPMLConstants.SEPARATOR);
      }

      if (value.length > 0)
        builder.delete(builder.length() - 1, builder.length());

      return builder.toString();
    }

    return null;
  }

  private String getValueString(DateFormat df, ISearchCondition condition) {
    if (condition.getValue() instanceof Date)
      return df.format((Date) condition.getValue());

    return condition.getValue().toString();
  }

  private void exportLabels(Element body) {
    Collection<ILabel> labels = DynamicDAO.loadAll(ILabel.class);
    for (ILabel label : labels) {
      Long id = label.getId();
      String name = label.getName();
      String color = label.getColor();
      int order = label.getOrder();

      Element labelElement = new Element(Tag.LABEL.get(), RSSOWL_NS);
      labelElement.setAttribute(Attributes.ID.get(), String.valueOf(id));
      labelElement.setAttribute(Attributes.NAME.get(), name);
      labelElement.setAttribute(Attributes.ORDER.get(), String.valueOf(order));
      labelElement.setAttribute(Attributes.COLOR.get(), color);

      body.addContent(labelElement);
    }
  }

  private void exportPreferences(Element body) {
    IPreferenceScope globalPreferences = Owl.getPreferenceService().getGlobalScope();
    IPreferenceScope eclipsePreferences = Owl.getPreferenceService().getEclipseScope();

    Preference[] preferences = Preference.values();
    for (Preference preference : preferences) {
      if (preference.getKind() == Kind.ENTITY)
        continue;

      String value = getValueAsString(preference, globalPreferences, eclipsePreferences);
      if (value != null) {
        Element prefElement = new Element(Tag.PREFERENCE.get(), RSSOWL_NS);
        prefElement.setAttribute(Attributes.ID.get(), preference.id());
        prefElement.setAttribute(Attributes.VALUE.get(), value);
        prefElement.setAttribute(Attributes.TYPE.get(), String.valueOf(preference.getType().ordinal()));
        prefElement.setAttribute(Attributes.KIND.get(), String.valueOf(preference.getKind().ordinal()));
        body.addContent(prefElement);
      }
    }
  }

  private String getValueAsString(Preference preference, IPreferenceScope global, IPreferenceScope eclipse) {
    IPreferenceScope actualScope = (preference.getKind() == Kind.GLOBAL) ? global : eclipse;
    String id = preference.id();

    switch (preference.getType()) {
      case BOOLEAN:
        return getValueAsString(actualScope.getBoolean(id));
      case INTEGER:
        return getValueAsString(actualScope.getInteger(id));
      case INTEGERS:
        return getValueAsString(actualScope.getIntegers(id));
      case LONG:
        return getValueAsString(actualScope.getLong(id));
      case LONGS:
        return getValueAsString(actualScope.getLongs(id));
      case STRING:
        return getValueAsString(actualScope.getString(id));
      case STRINGS:
        return getValueAsString(actualScope.getStrings(id));
    }

    return null;
  }
}