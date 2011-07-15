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
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;

/**
 * The <code>LowercaseDelimiterAnalyzer</code> will tokenize the input by the
 * given delimiter char. In addition, all <code>Token</code>s get lowercased.
 *
 * @author bpasero
 */
public class LowercaseDelimiterAnalyzer extends Analyzer {
  private final char fDelim;

  /**
   * The <code>LowercaseDelimiterAnalyzer</code> will tokenize the input by
   * the given delimiter char. In addition, all <code>Token</code>s get
   * lowercased.
   *
   * @param delim the char that is used to separate tokens.
   */
  public LowercaseDelimiterAnalyzer(char delim) {
    fDelim = delim;
  }

  /*
   * @see org.apache.lucene.analysis.KeywordAnalyzer#tokenStream(java.lang.String,
   * java.io.Reader)
   */
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {

    /* Split at delim Char */
    TokenStream result = new CharTokenizer(reader) {

      @Override
      protected boolean isTokenChar(char c) {
        return c != fDelim;
      }
    };

    /* Lowercase */
    result = new LowerCaseFilter(result);

    return result;
  }
}