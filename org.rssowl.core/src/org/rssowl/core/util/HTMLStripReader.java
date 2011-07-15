/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rssowl.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * A Reader that wraps another reader and attempts to strip out HTML constructs.
 * Entities found in the Text are being replaced if possible.
 * <p>
 * This class is part of Apache Solr and is versioned: 472574 (2006-11-08)
 * </p>
 */
public class HTMLStripReader extends Reader {

  /* Some constants being used */
  private static final int MISMATCH = -2;
  private static final int MATCH = -3;
  private static final int READAHEAD = 4096;

  /* Common Entities */
  private static final Map<String, Character> fgEntityTable;
  private final boolean fReplaceEntities;

  /* Wrapped Reader */
  private final Reader fIn;

  /* pushback buffer */
  private final StringBuilder fPushed = new StringBuilder();

  /* temporary buffer */
  private final StringBuilder fStrBuf = new StringBuilder();

  /* Static Initializer: Cache Entities */
  static {
    fgEntityTable = new HashMap<String, Character>();

    /* Entity Names */
    final String[] entityName = { "zwnj", "aring", "gt", "yen", "ograve", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        "Chi", "delta", "rang", "sup", "trade", "Ntilde", "xi", "upsih", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "nbsp", "Atilde", "radic", "otimes", "aelig", "oelig", "equiv", "ni", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "infin", "Psi", "auml", "cup", "Epsilon", "otilde", "lt", "Icirc", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "Eacute", "Lambda", "sbquo", "Prime", "prime", "psi", "Kappa", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "rsaquo", "Tau", "uacute", "ocirc", "lrm", "zwj", "cedil", "Alpha", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "not", "amp", "AElig", "oslash", "acute", "lceil", "alefsym", "laquo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "shy", "loz", "ge", "Igrave", "nu", "Ograve", "lsaquo", "sube", "euro", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
        "rarr", "sdot", "rdquo", "Yacute", "lfloor", "lArr", "Auml", "Dagger", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "brvbar", "Otilde", "szlig", "clubs", "diams", "agrave", "Ocirc", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "Iota", "Theta", "Pi", "zeta", "Scaron", "frac14", "egrave", "sub", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "iexcl", "frac12", "ordf", "sum", "prop", "Uuml", "ntilde", "atilde", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "asymp", "uml", "prod", "nsub", "reg", "rArr", "Oslash", "emsp", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "THORN", "yuml", "aacute", "Mu", "hArr", "le", "thinsp", "dArr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "ecirc", "bdquo", "Sigma", "Aring", "tilde", "nabla", "mdash", "uarr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "times", "Ugrave", "Eta", "Agrave", "chi", "real", "circ", "eth", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "rceil", "iuml", "gamma", "lambda", "harr", "Egrave", "frac34", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "dagger", "divide", "Ouml", "image", "ndash", "hellip", "igrave", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "Yuml", "ang", "alpha", "frasl", "ETH", "lowast", "Nu", "plusmn", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "bull", "sup1", "sup2", "sup3", "Aacute", "cent", "oline", "Beta", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "perp", "Delta", "there4", "pi", "iota", "empty", "euml", "notin", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "iacute", "para", "epsilon", "weierp", "OElig", "uuml", "larr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "icirc", "Upsilon", "omicron", "upsilon", "copy", "Iuml", "Oacute", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "Xi", "kappa", "ccedil", "Ucirc", "cap", "mu", "scaron", "lsquo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "isin", "Zeta", "minus", "deg", "and", "tau", "pound", "curren", "int", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
        "ucirc", "rfloor", "ensp", "crarr", "ugrave", "exist", "cong", "theta", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "oplus", "permil", "Acirc", "piv", "Euml", "Phi", "Iacute", "quot", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "Uacute", "Omicron", "ne", "iquest", "eta", "rsquo", "yacute", "Rho", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "darr", "Ecirc", "Omega", "acirc", "sim", "phi", "sigmaf", "macr", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "thetasym", "Ccedil", "ordm", "uArr", "forall", "beta", "fnof", "rho", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "micro", "eacute", "omega", "middot", "Gamma", "rlm", "lang", "spades", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "supe", "thorn", "ouml", "or", "raquo", "part", "sect", "ldquo", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "hearts", "sigma", "oacute", "apos" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
     };

    /* Entity Values */
    final char[] entityVal = { 8204, 229, 62, 165, 242, 935, 948, 9002, 8835,
        8482, 209, 958, 978, 160, 195, 8730, 8855, 230, 339, 8801, 8715, 8734,
        936, 228, 8746, 917, 245, 60, 206, 201, 923, 8218, 8243, 8242, 968,
        922, 8250, 932, 250, 244, 8206, 8205, 184, 913, 172, 38, 198, 248, 180,
        8968, 8501, 171, 173, 9674, 8805, 204, 957, 210, 8249, 8838, 8364,
        8594, 8901, 8221, 221, 8970, 8656, 196, 8225, 166, 213, 223, 9827,
        9830, 224, 212, 921, 920, 928, 950, 352, 188, 232, 8834, 161, 189, 170,
        8721, 8733, 220, 241, 227, 8776, 168, 8719, 8836, 174, 8658, 216, 8195,
        222, 255, 225, 924, 8660, 8804, 8201, 8659, 234, 8222, 931, 197, 732,
        8711, 8212, 8593, 215, 217, 919, 192, 967, 8476, 710, 240, 8969, 239,
        947, 955, 8596, 200, 190, 8224, 247, 214, 8465, 8211, 8230, 236, 376,
        8736, 945, 8260, 208, 8727, 925, 177, 8226, 185, 178, 179, 193, 162,
        8254, 914, 8869, 916, 8756, 960, 953, 8709, 235, 8713, 237, 182, 949,
        8472, 338, 252, 8592, 238, 933, 959, 965, 169, 207, 211, 926, 954, 231,
        219, 8745, 956, 353, 8216, 8712, 918, 8722, 176, 8743, 964, 163, 164,
        8747, 251, 8971, 8194, 8629, 249, 8707, 8773, 952, 8853, 8240, 194,
        982, 203, 934, 205, 34, 218, 927, 8800, 191, 951, 8217, 253, 929, 8595,
        202, 937, 226, 8764, 966, 962, 175, 977, 199, 186, 8657, 8704, 946,
        402, 961, 181, 233, 969, 183, 915, 8207, 9001, 9824, 8839, 254, 246,
        8744, 187, 8706, 167, 8220, 9829, 963, 243, 39
    };

    /* Fill Entities */
    for (int i = 0; i < entityName.length; i++)
      fgEntityTable.put(entityName[i], Character.valueOf(entityVal[i]));

    /* Special-case nbsp to a simple space instead of 0xa0 */
    fgEntityTable.put("nbsp", Character.valueOf(' ')); //$NON-NLS-1$
  }

  /**
   * Creates a new <code>HTMLStripReader</code> that wraps another reader and
   * attempts to strip out HTML constructs.
   *
   * @param source The <code>Reader</code> to wrap around.
   */
  public HTMLStripReader(Reader source) {
    this(source, true);
  }

  /**
   * Creates a new <code>HTMLStripReader</code> that wraps another reader and
   * attempts to strip out HTML constructs.
   *
   * @param source The <code>Reader</code> to wrap around.
   * @param replaceEntities <code>true</code> to replace entities and
   * <code>false</code> otherwise.
   */
  public HTMLStripReader(Reader source, boolean replaceEntities) {
    super();
    fIn = source.markSupported() ? source : new BufferedReader(source);
    fReplaceEntities = replaceEntities;
  }

  private int next() throws IOException {
    int len = fPushed.length();

    if (len > 0) {
      int ch = fPushed.charAt(len - 1);
      fPushed.setLength(len - 1);
      return ch;
    }

    return fIn.read();
  }

  private int nextSkipWS() throws IOException {
    int ch = next();

    while (isSpace(ch))
      ch = next();

    return ch;
  }

  private int peek() throws IOException {
    int len = fPushed.length();
    if (len > 0)
      return fPushed.charAt(len - 1);

    int ch = fIn.read();
    push(ch);

    return ch;
  }

  private void push(int ch) {
    fPushed.append((char) ch);
  }

  private boolean isSpace(int ch) {
    switch (ch) {
      case ' ':
      case '\n':
      case '\r':
      case '\t':
        return true;
      default:
        return false;
    }
  }

  private boolean isHex(int ch) {
    return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
  }

  private boolean isAlpha(int ch) {
    return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
  }

  private boolean isDigit(int ch) {
    return ch >= '0' && ch <= '9';
  }

  private boolean isIdChar(int ch) {
    return isAlpha(ch) || isDigit(ch) || ch == '.' || ch == '-' || ch == '_' || ch == ':' || Character.isLetter(ch);
  }

  private boolean isFirstIdChar(int ch) {
    return Character.isUnicodeIdentifierStart(ch);
  }

  private void saveState() throws IOException {
    fIn.mark(READAHEAD);
  }

  private void restoreState() throws IOException {
    fIn.reset();
    fPushed.setLength(0);
  }

  private int readNumericEntity() throws IOException {
    int ch = next();
    int base = 10;
    fStrBuf.setLength(0);

    /* Decimal character entity */
    if (isDigit(ch)) {
      fStrBuf.append((char) ch);
      for (int i = 0; i < 10; i++) {
        ch = next();
        if (isDigit(ch)) {
          fStrBuf.append((char) ch);
        } else {
          break;
        }
      }
    }

    /* Hex character entity */
    else if (ch == 'x') {
      base = 16;
      fStrBuf.setLength(0);
      for (int i = 0; i < 10; i++) {
        ch = next();
        if (isHex(ch)) {
          fStrBuf.append((char) ch);
        } else {
          break;
        }
      }
    } else {
      return MISMATCH;
    }

    /*
     * In older HTML, an entity may not have always been terminated with a
     * semicolon. We'll also treat EOF or whitespace as terminating the entity.
     */
    if (ch == ';' || ch == -1) {
      return Integer.parseInt(fStrBuf.toString(), base);
    }

    /*
     * if whitespace terminated the entity, we need to return that whitespace on
     * the next call to read().
     */
    if (isSpace(ch)) {
      push(ch);
      return Integer.parseInt(fStrBuf.toString(), base);
    }

    /* Not an entity... */
    return MISMATCH;
  }

  private int readEntity() throws IOException {
    int ch = next();
    if (ch == '#')
      return readNumericEntity();

    /*
     * read an entity reference for an entity reference, require the ';' for
     * safety. otherwise we may try and convert part of some company names to an
     * entity. "Alpha&Beta Corp" for instance.
     */
    fStrBuf.setLength(0);
    fStrBuf.append((char) ch);

    for (int i = 0; i < READAHEAD; i++) {
      ch = next();
      if (Character.isLetter(ch)) {
        fStrBuf.append((char) ch);
      } else {
        break;
      }
    }

    if (ch == ';' && fReplaceEntities) {
      String entity = fStrBuf.toString();
      Character entityChar = fgEntityTable.get(entity);
      if (entityChar != null) {
        return entityChar.charValue();
      }
    }

    return MISMATCH;
  }

  private int readBang(boolean inScript) throws IOException {

    /* at this point, "<!" has been read */
    int ret = readComment(inScript);
    if (ret == MATCH)
      return MATCH;

    int ch = next();
    if (ch == '>')
      return MATCH;

    /* if it starts with <! and isn't a comment, simply read until ">" */
    while (true) {
      ch = next();
      if (ch == '>') {
        return MATCH;
      } else if (ch < 0) {
        return MISMATCH;
      }
    }
  }

  /* Tries to read comments the way browsers do, not strictly by the standards */
  private int readComment(boolean inScript) throws IOException {

    /* at this point "<!" has been read */
    int ch = next();
    if (ch != '-') {
      push(ch);
      return MISMATCH;
    }

    ch = next();
    if (ch != '-') {
      push(ch);
      push('-');
      return MISMATCH;
    }

    while (true) {
      ch = next();
      if (ch < 0)
        return MISMATCH;
      if (ch == '-') {
        ch = next();
        if (ch < 0)
          return MISMATCH;
        if (ch != '-') {
          push(ch);
          continue;
        }

        ch = next();
        if (ch < 0)
          return MISMATCH;
        if (ch != '>') {
          push(ch);
          push('-');
          continue;
        }

        return MATCH;
      } else if ((ch == '\'' || ch == '"') && inScript) {
        push(ch);
        readScriptString();

        /*
         * if this wasn't a string, there's not much we can do at this point
         * without having a stack of stream states in order to "undo" just the
         * latest.
         */
      } else if (ch == '<') {
        eatSSI();
      }
    }
  }

  private int readTag() throws IOException {
    int ch = next();
    if (!isAlpha(ch)) {
      push(ch);
      return MISMATCH;
    }

    fStrBuf.setLength(0);
    fStrBuf.append((char) ch);

    while (true) {
      ch = next();
      if (isIdChar(ch)) {
        fStrBuf.append((char) ch);
      } else if (ch == '/') {
        return nextSkipWS() == '>' ? MATCH : MISMATCH;
      } else {
        break;
      }
    }

    /* After the tag id, there needs to be either whitespace or '>' */
    if (!(ch == '>' || isSpace(ch))) {
      return MISMATCH;
    }

    if (ch != '>') {
      while (true) {
        ch = next();
        if (isSpace(ch)) {
          continue;
        } else if (isFirstIdChar(ch)) {
          push(ch);
          int ret = readAttr2();
          if (ret == MISMATCH)
            return ret;
        } else if (ch == '/') {
          return nextSkipWS() == '>' ? MATCH : MISMATCH;
        } else if (ch == '>') {
          break;
        } else {
          return MISMATCH;
        }
      }
    }

    /*
     * We only get to this point after we have read the entire tag. Now let's
     * see if it's a special tag.
     */
    String name = fStrBuf.toString();
    if (name.equals("script") || name.equals("style")) { //$NON-NLS-1$ //$NON-NLS-2$
      // The content of script and style elements is
      //  CDATA in HTML 4 but PCDATA in XHTML.

      /*
       * From HTML4: Although the STYLE and SCRIPT elements use CDATA for their
       * data model, for these elements, CDATA must be handled differently by
       * user agents. Markup and entities must be treated as raw text and passed
       * to the application as is. The first occurrence of the character
       * sequence "</" (end-tag open delimiter) is treated as terminating the
       * end of the element's content. In valid documents, this would be the end
       * tag for the element.
       */

      // discard everything until endtag is hit (except
      // if it occurs in a comment.
      // reset the stream mark to here, since we know that we sucessfully matched
      // a tag, and if we can't find the end tag, this is where we will want
      // to roll back to.
      saveState();
      fPushed.setLength(0);
      return findEndTag();
    }
    return MATCH;
  }

  /*
   * find an end tag, but beware of comments... <script><!-- </script> -->foo</script>
   * beware markup in script strings: </script>...document.write("</script>")foo</script>
   */
  int findEndTag() throws IOException {
    while (true) {
      int ch = next();
      if (ch == '<') {
        ch = next();
        // skip looking for end-tag in comments
        if (ch == '!') {
          int ret = readBang(true);
          if (ret == MATCH)
            continue;
          continue;
        }
        // did we match "</"
        if (ch != '/') {
          push(ch);
          continue;
        }
        int ret = readName();
        if (ret == MISMATCH)
          return MISMATCH;
        ch = nextSkipWS();
        if (ch != '>')
          return MISMATCH;
        return MATCH;
      } else if (ch == '\'' || ch == '"') {
        // read javascript string to avoid a false match.
        push(ch);
        int ret = readScriptString();
        // what to do about a non-match (non-terminated string?)
        // play it safe and index the rest of the data I guess...
        if (ret == MISMATCH)
          return MISMATCH;
      } else if (ch < 0) {
        return MISMATCH;
      }
    }
  }

  /* Read a string escaped by backslashes */
  private int readScriptString() throws IOException {
    int quoteChar = next();
    if (quoteChar != '\'' && quoteChar != '"')
      return MISMATCH;
    while (true) {
      int ch = next();
      if (ch == quoteChar)
        return MATCH;
      else if (ch == '\\') {
        ch = next();
      } else if (ch < 0) {
        return MISMATCH;
      } else if (ch == '<') {
        eatSSI();
      }
    }
  }

  private int readName() throws IOException {
    int ch = read();
    if (!isFirstIdChar(ch))
      return MISMATCH;
    ch = read();
    while (isIdChar(ch))
      ch = read();
    if (ch != -1)
      push(ch);
    return MATCH;
  }

  /*
   * This reads attributes and attempts to handle any embedded server side
   * includes that would otherwise mess up the quote handling. <a href="a/<!--#echo
   * "path"-->">
   */
  private int readAttr2() throws IOException {
    int ch = read();
    if (!isFirstIdChar(ch))
      return MISMATCH;
    ch = read();
    while (isIdChar(ch))
      ch = read();
    if (isSpace(ch))
      ch = nextSkipWS();

    // attributes may not have a value at all!
    if (ch != '=') {
      push(ch);
      return MATCH;
    }

    int quoteChar = nextSkipWS();

    if (quoteChar == '"' || quoteChar == '\'') {
      while (true) {
        ch = next();
        if (ch < 0)
          return MISMATCH;
        else if (ch == '<') {
          eatSSI();
        } else if (ch == quoteChar) {
          return MATCH;
        }
      }
    }

    /* unquoted attribute */
    while (true) {
      ch = next();
      if (ch < 0)
        return MISMATCH;
      else if (isSpace(ch)) {
        push(ch);
        return MATCH;
      } else if (ch == '>') {
        push(ch);
        return MATCH;
      } else if (ch == '<') {
        eatSSI();
      }
    }

  }

  // skip past server side include
  // at this point, only a "<" was read.
  // on a mismatch, push back the last char so that if it was
  // a quote that closes the attribute, it will be re-read and matched.
  private int eatSSI() throws IOException {
    int ch = next();
    if (ch != '!') {
      push(ch);
      return MISMATCH;
    }
    ch = next();
    if (ch != '-') {
      push(ch);
      return MISMATCH;
    }
    ch = next();
    if (ch != '-') {
      push(ch);
      return MISMATCH;
    }
    ch = next();
    if (ch != '#') {
      push(ch);
      return MISMATCH;
    }

    push('#');
    push('-');
    push('-');
    return readComment(false);
  }

  private int readProcessingInstruction() throws IOException {
    while (true) {
      int ch = next();
      if (ch == '?' && peek() == '>') {
        next();
        return MATCH;
      } else if (ch == -1) {
        return MISMATCH;
      }
    }
  }

  /*
   * @see java.io.Reader#read()
   */
  @Override
  public int read() throws IOException {
    while (true) {
      int ch = next();

      switch (ch) {
        case '&':
          saveState();
          ch = readEntity();
          if (ch >= 0)
            return ch;
          if (ch == MISMATCH) {
            restoreState();
            return '&';
          }
          break;

        case '<':
          saveState();
          ch = next();
          int ret = MISMATCH;
          if (ch == '!') {
            ret = readBang(false);
          } else if (ch == '/') {
            ret = readName();
            if (ret == MATCH) {
              ch = nextSkipWS();
              ret = ch == '>' ? MATCH : MISMATCH;
            }
          } else if (isAlpha(ch)) {
            push(ch);
            ret = readTag();
          } else if (ch == '?') {
            ret = readProcessingInstruction();
          }

          /*
           * matched something to be discarded, so break from this case and
           * continue in the loop
           */
          if (ret == MATCH)
            break;

          /*
           * didn't match any HTML constructs, so roll back the stream state and
           * just return '<'
           */
          restoreState();
          return '<';

        default:
          return ch;
      }
    }
  }

  /*
   * @see java.io.Reader#read(char[], int, int)
   */
  @Override
  public int read(char cbuf[], int off, int len) throws IOException {
    int i = 0;
    for (i = 0; i < len; i++) {
      int ch = read();
      if (ch == -1)
        break;
      cbuf[off++] = (char) ch;
    }
    if (i == 0) {
      if (len == 0)
        return 0;
      return -1;
    }
    return i;
  }

  /*
   * @see java.io.Reader#close()
   */
  @Override
  public void close() throws IOException {
    fIn.close();
  }
}