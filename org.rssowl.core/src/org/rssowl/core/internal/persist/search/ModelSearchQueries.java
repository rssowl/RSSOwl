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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.NumberTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.persist.IBookMark;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IFolder;
import org.rssowl.core.persist.IMark;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.INewsBin;
import org.rssowl.core.persist.ISearch;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchValueType;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.reference.BookMarkReference;
import org.rssowl.core.persist.reference.FolderReference;
import org.rssowl.core.persist.reference.NewsBinReference;
import org.rssowl.core.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Helper to build the queries to search for {@link INews}.
 *
 * @author bpasero
 */
public class ModelSearchQueries {

  /* Maximum Date as String */
  private static final String MAX_DATE = DateTools.dateToString(new Date(Long.MAX_VALUE), Resolution.DAY);

  /* Minimum Date as String */
  private static final String MIN_DATE = DateTools.dateToString(new Date(0), Resolution.DAY);

  /* Maximum Number as String */
  private static final String MAX_NUMBER = NumberTools.longToString(Long.MAX_VALUE);

  /* Minimum Number as String */
  private static final String MIN_NUMBER = NumberTools.longToString(Long.MIN_VALUE);

  /* One Day in Millis */
  private static final Long DAY = 1000 * 3600 * 24L;

  /* One Minute in Millis */
  private static final Long MINUTE = 1000 * 60L;

  /* Wildcard matching any String */
  private static final char STRING_WILDCARD = '*';

  /* Wildcard matching any Char */
  private static final char CHAR_WILDCARD = '?';

  /**
   * Creates a Lucene {@link Query} from the given parameters.
   *
   * @param search the search for the query.
   * @return a {@link Query} from the given parameters.
   * @throws IOException in case of an error.
   */
  public static Query createQuery(ISearch search) throws IOException {
    return createQuery(search.getSearchConditions(), null, search.matchAllConditions());
  }

  /**
   * Creates a Lucene {@link Query} from the given parameters.
   *
   * @param conditions the search conditions for the query.
   * @param scope a specific {@link ISearchCondition} that scopes the results.
   * As such, the scope condition is a must criteria for the results.
   * @param matchAllConditions <code>true</code> to match all conditions and
   * <code>false</code> otherwise.
   * @return a {@link Query} from the given parameters.
   * @throws IOException in case of an error.
   */
  public static Query createQuery(Collection<ISearchCondition> conditions, ISearchCondition scope, boolean matchAllConditions) throws IOException {
    try {
      return internalCreateQuery(conditions, scope, matchAllConditions);
    }

    /* Too Many Clauses - Increase Clauses Limit */
    catch (TooManyClauses e) {

      /* Disable Clauses Limit */
      if (BooleanQuery.getMaxClauseCount() != ModelSearchImpl.MAX_CLAUSE_COUNT) {
        BooleanQuery.setMaxClauseCount(ModelSearchImpl.MAX_CLAUSE_COUNT);
        return internalCreateQuery(conditions, scope, matchAllConditions);
      }

      /* Maximum reached */
      throw e;
    }
  }

  private static Query internalCreateQuery(Collection<ISearchCondition> conditions, ISearchCondition scope, boolean matchAllConditions) throws IOException {
    boolean isScoped = false;
    BooleanQuery bQuery = new BooleanQuery();
    Analyzer analyzer = Indexer.createAnalyzer();

    /* Handle Location Scope Query separately */
    for (ISearchCondition condition : conditions) {
      if (isLocationScopeCondition(condition)) {
        BooleanQuery locationScopeClause = createLocationClause(condition);
        if (!locationScopeClause.clauses().isEmpty()) {
          bQuery.add(locationScopeClause, Occur.MUST);
          isScoped = true;
        }
      }
    }

    /* Add Condition Scope Query as necessary */
    if (scope != null) {
      addFieldClauses(Collections.singleton(scope), true, bQuery, analyzer);
      isScoped = true;
    }

    /* If scoped, the fieldQuery is a MUST-Clause to the outer Query */
    BooleanQuery fieldQuery = bQuery;
    if (isScoped)
      fieldQuery = new BooleanQuery();

    /* Add Conditions into the Boolean Query */
    addFieldClauses(conditions, matchAllConditions, fieldQuery, analyzer);

    /* Only add if not empty if scoped */
    if (isScoped && !fieldQuery.clauses().isEmpty())
      bQuery.add(fieldQuery, Occur.MUST);

    return bQuery;
  }

  private static void addFieldClauses(Collection<ISearchCondition> conditions, boolean matchAllConditions, BooleanQuery bQuery, Analyzer analyzer) throws IOException {

    /* Handle State-Field separately (group) */
    BooleanQuery statesQuery = null;
    for (ISearchCondition condition : conditions) {
      if (requiresStateGrouping(condition)) {

        /* Create and add new BooleanQuery for State */
        if (statesQuery == null) {
          statesQuery = new BooleanQuery();
          bQuery.add(statesQuery, matchAllConditions ? Occur.MUST : Occur.SHOULD);
        }

        /* Add Boolean Clause per State */
        addStateClause(statesQuery, condition);
      }
    }

    /* Create a Query for each condition */
    BooleanQuery fieldQuery = null;
    for (ISearchCondition condition : conditions) {

      /* State and Scope Queries already handled */
      if (requiresStateGrouping(condition) || isLocationScopeCondition(condition))
        continue;

      /* Create and add new BooleanQuery for other Fields */
      if (fieldQuery == null) {
        fieldQuery = new BooleanQuery();
        bQuery.add(fieldQuery, matchAllConditions ? Occur.MUST : Occur.SHOULD);
      }

      /* Create the Clause */
      BooleanClause clause = null;
      if (condition.getField().getId() == IEntity.ALL_FIELDS)
        clause = createAllNewsFieldsClause(analyzer, condition, matchAllConditions);
      else
        clause = createBooleanClause(analyzer, condition, matchAllConditions);

      /* Check if the Clause has any valid Query */
      Query query = clause.getQuery();
      if (query instanceof BooleanQuery && ((BooleanQuery) query).clauses().isEmpty())
        continue;

      /*
       * Specially treat this case where the specifier is a negation but any of
       * the supplied conditions should match in the result set.
       */
      if (condition.getSpecifier().isNegation() && !matchAllConditions) {
        BooleanQuery nestedquery = new BooleanQuery();
        nestedquery.add(clause);
        nestedquery.add(new BooleanClause(new MatchAllDocsQuery(), Occur.MUST));
        fieldQuery.add(new BooleanClause(nestedquery, Occur.SHOULD));
      }

      /* Normal Case */
      else {
        fieldQuery.add(clause);
      }
    }

    /* Add the MatchAllDocsQuery (MUST_NOT is used, All Conditions match) */
    if (fieldQuery != null && matchAllConditions) {
      boolean requireAllDocsQuery = true;
      BooleanClause[] clauses = fieldQuery.getClauses();
      for (BooleanClause clause : clauses) {
        if (clause.getOccur() != Occur.MUST_NOT) {
          requireAllDocsQuery = false;
          break;
        }
      }

      /* Add if required */
      if (requireAllDocsQuery)
        fieldQuery.add(new BooleanClause(new MatchAllDocsQuery(), Occur.MUST));
    }
  }

  @SuppressWarnings("unchecked")
  private static void addStateClause(BooleanQuery statesQuery, ISearchCondition condition) {
    String fieldName = String.valueOf(INews.STATE);
    Occur occur = condition.getSpecifier().isNegation() ? Occur.MUST_NOT : Occur.SHOULD;
    EnumSet<INews.State> newsStates = (EnumSet<State>) condition.getValue();
    for (INews.State state : newsStates) {
      String value = String.valueOf(state.ordinal());
      TermQuery stateQuery = new TermQuery(new Term(fieldName, value));
      statesQuery.add(new BooleanClause(stateQuery, occur));
    }

    /* Check if the match-all-docs query is required */
    if (condition.getSpecifier().isNegation())
      statesQuery.add(new BooleanClause(new MatchAllDocsQuery(), Occur.MUST));
  }

  private static boolean requiresStateGrouping(ISearchCondition condition) {
    return condition.getField().getId() == INews.STATE;
  }

  private static boolean isLocationScopeCondition(ISearchCondition condition) {
    return condition.getSpecifier() == SearchSpecifier.SCOPE;
  }

  private static BooleanClause createAllNewsFieldsClause(Analyzer analyzer, ISearchCondition condition, boolean matchAllConditions) throws IOException {
    IModelFactory factory = Owl.getModelFactory();
    BooleanQuery allFieldsQuery = new BooleanQuery();

    /* Require all words to be contained or not contained */
    if (condition.getSpecifier() == SearchSpecifier.CONTAINS_ALL) {
      List<ISearchCondition> tokenConditions = new ArrayList<ISearchCondition>();

      List<String> tokens = StringUtils.tokenizePhraseAware((String) condition.getValue(), true);
      for (String token : tokens) {
        ISearchCondition tokenCondition = factory.createSearchCondition(condition.getField(), condition.getSpecifier(), token);

        /* Rewrite Specifier */
        if (condition.getSpecifier() == SearchSpecifier.CONTAINS_ALL)
          tokenCondition.setSpecifier(SearchSpecifier.CONTAINS);
        else
          tokenCondition.setSpecifier(SearchSpecifier.CONTAINS_NOT);

        tokenConditions.add(tokenCondition);
      }

      /* Build custom Query out of Conditions */
      for (ISearchCondition tokenCondition : tokenConditions) {
        BooleanClause tokenClause = createAllNewsFieldsClause(analyzer, tokenCondition, matchAllConditions);

        /* Ignore empty clauses (e.g. due to Stop Words) */
        if (tokenClause.getQuery() instanceof BooleanQuery && ((BooleanQuery) tokenClause.getQuery()).getClauses().length == 0)
          continue;

        tokenClause.setOccur(Occur.MUST);
        allFieldsQuery.add(tokenClause);
      }
    }

    /* Require any word to be contained or not contained */
    else {
      List<ISearchCondition> allFieldsConditions = new ArrayList<ISearchCondition>(5);

      /* Title */
      ISearchField field = factory.createSearchField(INews.TITLE, condition.getField().getEntityName());
      allFieldsConditions.add(factory.createSearchCondition(field, condition.getSpecifier(), condition.getValue()));

      /* Description */
      field = factory.createSearchField(INews.DESCRIPTION, condition.getField().getEntityName());
      allFieldsConditions.add(factory.createSearchCondition(field, condition.getSpecifier(), condition.getValue()));

      /* Author */
      field = factory.createSearchField(INews.AUTHOR, condition.getField().getEntityName());
      allFieldsConditions.add(factory.createSearchCondition(field, condition.getSpecifier(), condition.getValue()));

      /* Category (Phrase search stripped because unsupported */
      field = factory.createSearchField(INews.CATEGORIES, condition.getField().getEntityName());
      List<String> tokens = StringUtils.tokenizePhraseAware(condition.getValue().toString(), false);
      if (!tokens.contains(condition.getValue().toString()))
        tokens.add(condition.getValue().toString());
      for (String token : tokens) {
        if (token.length() != 0)
          allFieldsConditions.add(factory.createSearchCondition(field, condition.getSpecifier().isNegation() ? SearchSpecifier.IS_NOT : SearchSpecifier.IS, token));
      }

      /* Attachment Content */
      field = factory.createSearchField(INews.ATTACHMENTS_CONTENT, condition.getField().getEntityName());
      allFieldsConditions.add(factory.createSearchCondition(field, condition.getSpecifier(), condition.getValue()));

      /* Create Clauses out of Conditions */
      boolean anyClauseIsEmpty = false;
      List<BooleanClause> clauses = new ArrayList<BooleanClause>();
      for (ISearchCondition allFieldCondition : allFieldsConditions) {
        BooleanClause clause = createBooleanClause(analyzer, allFieldCondition, matchAllConditions);
        clause.setOccur(Occur.SHOULD);
        clauses.add(clause);

        /* Ignore empty clauses (e.g. due to Stop Words) */
        if (clause.getQuery() instanceof BooleanQuery && ((BooleanQuery) clause.getQuery()).getClauses().length == 0)
          anyClauseIsEmpty = true;
      }

      /* Only add if none of the clauses is empty */
      if (!anyClauseIsEmpty) {
        for (BooleanClause clause : clauses) {
          allFieldsQuery.add(clause);
        }
      }
    }

    /* Determine Occur (MUST, SHOULD, MUST NOT) */
    Occur occur = getOccur(condition.getSpecifier(), matchAllConditions);
    return new BooleanClause(allFieldsQuery, occur);
  }

  /*
   * Will fallback to a TermQuery if the search-term is not valid for a
   * WildcardQuery
   */
  private static Query createWildcardQuery(String field, String term) {
    if (String.valueOf(INews.LABEL).equals(field) || isValidWildcardTerm(term))
      return new WildcardQuery(new Term(field, term));

    return new TermQuery(new Term(field, term));
  }

  private static boolean isValidWildcardTerm(String term) {
    for (int i = 0; i < term.length(); i++) {
      char charAtIndex = term.charAt(i);
      if (charAtIndex != STRING_WILDCARD && charAtIndex != CHAR_WILDCARD)
        return true;
    }

    return false;
  }

  private static BooleanClause createBooleanClause(Analyzer analyzer, ISearchCondition condition, boolean matchAllConditions) throws IOException {
    Query query = null;

    /* Separately handle this dynamic Query */
    if (condition.getField().getId() == INews.AGE_IN_DAYS || condition.getField().getId() == INews.AGE_IN_MINUTES)
      query = createAgeClause(condition);

    /* Separately handle this dynamic Query */
    else if (condition.getField().getId() == INews.LOCATION)
      query = createLocationClause(condition);

    /* Other Fields */
    else {
      try {
        switch (condition.getField().getSearchValueType().getId()) {

        /* Boolean: Simple Term-Query */
          case ISearchValueType.BOOLEAN:
            query = createTermQuery(condition);
            break;

          /* String / Link / Enum: String Query */
          case ISearchValueType.ENUM:
          case ISearchValueType.STRING:
          case ISearchValueType.LINK:
            query = createStringQuery(analyzer, condition);
            break;

          /* Date / Time / DateTime: Date Query (Ranged) */
          case ISearchValueType.DATE:
          case ISearchValueType.TIME:
          case ISearchValueType.DATETIME:
            query = createDateQuery(condition);
            break;

          /* Number / Integer: Number Query (Ranged) */
          case ISearchValueType.NUMBER:
          case ISearchValueType.INTEGER:
            query = createNumberQuery(condition);
        }
      } catch (ParseException e) {
        Activator.getDefault().getLog().log(Activator.getDefault().createErrorStatus(e.getMessage(), e));
      }
    }

    /* In case of the Query not being created, fallback to Term-Query */
    if (query == null) {
      query = createTermQuery(condition);
    }

    /* Determine Occur (MUST, SHOULD, MUST NOT) */
    Occur occur = getOccur(condition.getSpecifier(), matchAllConditions);
    return new BooleanClause(query, occur);
  }

  /* This Clause needs to be generated dynamically */
  private static Query createAgeClause(ISearchCondition condition) {
    Integer age = (Integer) condition.getValue();
    String value;

    /* Minute Format */
    if (age < 0) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(System.currentTimeMillis() + age * MINUTE); //age is negative
      value = DateTools.dateToString(cal.getTime(), Resolution.MINUTE);

      String fieldname = String.valueOf(INews.AGE_IN_MINUTES);
      return createAgeQuery(condition, fieldname, value);
    }

    /* Day Format */
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(System.currentTimeMillis() - age * DAY);
    value = DateTools.dateToString(cal.getTime(), Resolution.DAY);

    String fieldname = String.valueOf(INews.AGE_IN_DAYS);
    return createAgeQuery(condition, fieldname, value);
  }

  private static Query createAgeQuery(ISearchCondition condition, String fieldname, String value) {
    switch (condition.getSpecifier()) {
      case IS: {
        return new TermQuery(new Term(fieldname, value));
      }

      case IS_GREATER_THAN: {
        Term lowerBound = new Term(fieldname, MIN_DATE);
        Term upperBound = new Term(fieldname, value);

        return new ConstantScoreRangeQuery(fieldname, lowerBound.text(), upperBound.text(), false, false);
      }

      case IS_LESS_THAN: {
        Term lowerBound = new Term(fieldname, value);
        Term upperBound = new Term(fieldname, MAX_DATE);

        return new ConstantScoreRangeQuery(fieldname, lowerBound.text(), upperBound.text(), false, false);
      }
    }

    throw new UnsupportedOperationException("Unsupported Specifier for Age Query"); //$NON-NLS-1$
  }

  /* This Clause needs to be generated dynamically */
  private static BooleanQuery createLocationClause(ISearchCondition condition) {
    BooleanQuery bQuery = new BooleanQuery();
    Long[][] value = (Long[][]) condition.getValue();
    if (value != null) {

      /* Receive Folders */
      for (int i = 0; value[0] != null && i < value[0].length; i++) {
        if (value[0][i] != null) {
          IFolder folder = new FolderReference(value[0][i]).resolve();
          if (folder != null)
            addFolderLocationClause(bQuery, folder);
        }
      }

      /* Receive BookMarks */
      for (int i = 0; value[1] != null && i < value[1].length; i++) {
        if (value[1][i] != null) {
          IBookMark bookmark = new BookMarkReference(value[1][i]).resolve();
          if (bookmark != null)
            addBookMarkLocationClause(bQuery, bookmark);
        }
      }

      /* Receive NewsBins */
      if (value.length == 3) {
        for (int i = 0; value[2] != null && i < value[2].length; i++) {
          if (value[2][i] != null) {
            INewsBin newsbin = new NewsBinReference(value[2][i]).resolve();
            if (newsbin != null)
              addNewsBinLocationClause(bQuery, newsbin);
          }
        }
      }
    }

    /* The folder could be empty, make sure to add at least 1 Clause */
    if (bQuery.clauses().isEmpty())
      bQuery.add(new TermQuery(new Term(String.valueOf(INews.FEED), "")), Occur.SHOULD); //$NON-NLS-1$

    return bQuery;
  }

  private static void addFolderLocationClause(BooleanQuery bQuery, IFolder folder) {
    if (folder != null) {
      List<IFolder> folders = folder.getFolders();
      List<IMark> marks = folder.getMarks();

      /* Child Folders */
      for (IFolder childFolder : folders)
        addFolderLocationClause(bQuery, childFolder);

      /* BookMarks and Newsbins */
      for (IMark mark : marks)
        if (mark instanceof IBookMark)
          addBookMarkLocationClause(bQuery, (IBookMark) mark);
        else if (mark instanceof INewsBin)
          addNewsBinLocationClause(bQuery, (INewsBin) mark);
    }
  }

  private static void addBookMarkLocationClause(BooleanQuery bQuery, IBookMark bookmark) {
    if (bookmark != null) {
      BooleanQuery bookMarkLocationQuery = new BooleanQuery();

      /* Match on Feed */
      String feed = bookmark.getFeedLinkReference().getLinkAsText().toLowerCase();
      bookMarkLocationQuery.add(new TermQuery(new Term(String.valueOf(INews.FEED), feed)), Occur.MUST);

      /* But ignore News from Bins */
      bookMarkLocationQuery.add(new TermQuery(new Term(String.valueOf(INews.PARENT_ID), NumberTools.longToString(0))), Occur.MUST);

      bQuery.add(bookMarkLocationQuery, Occur.SHOULD);
    }
  }

  private static void addNewsBinLocationClause(BooleanQuery bQuery, INewsBin newsbin) {
    if (newsbin != null)
      bQuery.add(new TermQuery(new Term(String.valueOf(INews.PARENT_ID), NumberTools.longToString(newsbin.getId()))), Occur.SHOULD);
  }

  @SuppressWarnings("unchecked")
  private static Query createStringQuery(Analyzer analyzer, ISearchCondition condition) throws ParseException, IOException {
    SearchSpecifier specifier = condition.getSpecifier();
    String fieldname = String.valueOf(condition.getField().getId());

    /* Retrieve Value */
    String value;
    if (condition.getValue() instanceof Enum)
      value = String.valueOf(((Enum<?>) condition.getValue()).ordinal());
    else
      value = String.valueOf(condition.getValue());

    switch (specifier) {

    /* Create Wildcard-Query */
      case IS:
      case IS_NOT: {
        return createWildcardQuery(fieldname, value.toLowerCase());
      }

      /* Let Query-Parser handle this */
      case CONTAINS:
      case CONTAINS_ALL:
      case CONTAINS_NOT: {
        QueryParser parser = new QueryParser(fieldname, analyzer);
        Operator operator = (specifier == SearchSpecifier.CONTAINS || specifier == SearchSpecifier.CONTAINS_NOT) ? QueryParser.OR_OPERATOR : QueryParser.AND_OPERATOR;
        parser.setDefaultOperator(operator);
        parser.setAllowLeadingWildcard(true);

        /* Prepare the value for parsing */
        value = prepareForParsing(value);

        /* Parse */
        return parser.parse(value);
      }

      /* Wildcard-Query with trailing '*' */
      case BEGINS_WITH: {
        value = value.toLowerCase() + "*"; //$NON-NLS-1$
        return createWildcardQuery(fieldname, value);
      }

      /* Wildcard-Query with leading '*' */
      case ENDS_WITH: {
        value = "*" + value.toLowerCase(); //$NON-NLS-1$
        return createWildcardQuery(fieldname, value);
      }

      /* Fuzzy Query */
      case SIMILIAR_TO: {
        BooleanQuery similarityQuery = new BooleanQuery();

        LowercaseWhitespaceAnalyzer similarAnalyzer = new LowercaseWhitespaceAnalyzer();
        TokenStream tokenStream = similarAnalyzer.tokenStream(String.valueOf(IEntity.ALL_FIELDS), new StringReader(value));
        Token token = null;
        while ((token = tokenStream.next()) != null) {
          String termText = new String(token.termBuffer(), 0, token.termLength());
          Term term = new Term(fieldname, termText);
          similarityQuery.add(new BooleanClause(new FuzzyQuery(term), Occur.MUST));
        }

        return similarityQuery;
      }
    }

    throw new UnsupportedOperationException("Unsupported Specifier for Parsed Queries"); //$NON-NLS-1$
  }

  @SuppressWarnings("unchecked")
  private static Query createTermQuery(ISearchCondition condition) {
    String value;
    if (condition.getValue() instanceof Enum)
      value = String.valueOf(((Enum<?>) condition.getValue()).ordinal());
    else
      value = String.valueOf(condition.getValue());

    String fieldname = String.valueOf(condition.getField().getId());

    Term term = new Term(fieldname, value);
    return new TermQuery(term);
  }

  private static Query createDateQuery(ISearchCondition condition) {
    SearchSpecifier specifier = condition.getSpecifier();
    String value = DateTools.dateToString((Date) condition.getValue(), Resolution.DAY);
    String fieldname = String.valueOf(condition.getField().getId());

    switch (specifier) {
      case IS:
      case IS_NOT:
        return new TermQuery(new Term(fieldname, value));

      case IS_AFTER: {
        Term lowerBound = new Term(fieldname, value);
        Term upperBound = new Term(fieldname, MAX_DATE);

        return new ConstantScoreRangeQuery(fieldname, lowerBound.text(), upperBound.text(), false, false);
      }

      case IS_BEFORE: {
        Term lowerBound = new Term(fieldname, MIN_DATE);
        Term upperBound = new Term(fieldname, value);

        return new ConstantScoreRangeQuery(fieldname, lowerBound.text(), upperBound.text(), false, false);
      }
    }

    throw new UnsupportedOperationException("Unsupported Specifier for Date/Time Queries"); //$NON-NLS-1$
  }

  private static Query createNumberQuery(ISearchCondition condition) {
    SearchSpecifier specifier = condition.getSpecifier();
    String value = NumberTools.longToString((Integer) condition.getValue());
    String fieldname = String.valueOf(condition.getField().getId());

    switch (specifier) {
      case IS:
      case IS_NOT:
        return new TermQuery(new Term(fieldname, value));

      case IS_GREATER_THAN: {
        Term lowerBound = new Term(fieldname, value);
        Term upperBound = new Term(fieldname, MAX_NUMBER);

        return new ConstantScoreRangeQuery(fieldname, lowerBound.text(), upperBound.text(), false, false);
      }

      case IS_LESS_THAN: {
        Term lowerBound = new Term(fieldname, MIN_NUMBER);
        Term upperBound = new Term(fieldname, value);

        return new ConstantScoreRangeQuery(fieldname, lowerBound.text(), upperBound.text(), false, false);
      }
    }

    throw new UnsupportedOperationException("Unsupported Specifier for Number Queries"); //$NON-NLS-1$
  }

  private static Occur getOccur(SearchSpecifier specifier, boolean matchAllConditions) {
    switch (specifier) {
      case IS_NOT:
      case CONTAINS_NOT:
        return Occur.MUST_NOT;

      default:
        return matchAllConditions ? Occur.MUST : Occur.SHOULD;
    }
  }

  private static String prepareForParsing(String s) {
    int doubleQuoteCount = 0;
    boolean startsWithDoubleQuote = false;
    boolean endsWithDoubleQuote = false;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);

      /* Escape Special Characters being used in Lucene */
      if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^' || c == '[' || c == ']' || c == '{' || c == '}' || c == '~')
        sb.append('\\');

      /* Keep Track of Doublequotes being used - they need special treatment */
      else if (c == '\"') {
        doubleQuoteCount++;
        if (i == 0)
          startsWithDoubleQuote = true;
        else if (i == s.length() - 1)
          endsWithDoubleQuote = true;
      }

      sb.append(c);
    }

    String escapedString = sb.toString();

    /*
     * This results in a parser exception from QueryParser and thus needs
     * special treatment
     */
    if (doubleQuoteCount % 2 != 0) {
      escapedString = escapedString.replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
      if (startsWithDoubleQuote && endsWithDoubleQuote) //Restore Quotes for Phrase Search if necessary
        escapedString = escapedString.substring(1, escapedString.length() - 2) + "\""; //$NON-NLS-1$
    }

    return escapedString;
  }
}