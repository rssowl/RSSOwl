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

import org.jdom.Namespace;

/**
 * Common constants being used from {@link OPMLImporter} and
 * {@link OPMLExporter}
 *
 * @author bpasero
 */
public class OPMLConstants {

  /* The namespace RSSOwl is using */
  static final Namespace RSSOWL_NS = Namespace.getNamespace("rssowl", "http://www.rssowl.org"); //$NON-NLS-1$ //$NON-NLS-2$

  /* Separator for Multi Values */
  static final String SEPARATOR = ","; //$NON-NLS-1$

  /* List of Elements to use for the XML */
  enum Tag {

    /** Standard OPML */
    OPML("opml"), OUTLINE("outline"), BODY("body"), HEAD("head"), TITLE("title"), DATE_MODIFIED("dateModified"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    /** Custom RSSOwl */
    PREFERENCE("pref"), BIN("newsbin"), SAVED_SEARCH("savedsearch"), LABEL("label"), STATE("newsstate"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    SPECIFIER("searchspecifier"), LOCATION("location"), SEARCH_VALUE("searchvalue"), SEARCH_FIELD("searchfield"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    FILTER("searchfilter"), SEARCH("search"), SEARCH_CONDITION("searchcondition"), ACTION("filteraction"), ACTION_PROPERTY("filteractionproperty"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private String fName;

    Tag(String name) {
      fName = name;
    }

    String get() {
      return fName;
    }
  }

  /* List of Attributes to use for the XML */
  enum Attributes {

    /** Standard OPML */
    VERSION("version"), ID("id"), XML_URL("xmlUrl"), TEXT("text"), TITLE("title"), HTML_URL("htmlUrl"), DESCRIPTION("description"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

    /** Custom RSSOwl */
    IS_SET("isSet"), TYPE("type"), VALUE("value"), DATA("data"), NAME("name"), IS_BIN("isBin"), IS_FOLDER("isFolder"), KIND("kind"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    ENTITY("entity"), ORDER("order"), COLOR("color"), ENABLED("enabled"), MATCH_ALL_NEWS("matchAllNews"), MATCH_ALL_CONDITIONS("matchAllConditions"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    private String fName;

    Attributes(String name) {
      fName = name;
    }

    String get() {
      return fName;
    }
  }
}