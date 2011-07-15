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

package org.rssowl.core.tests.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rssowl.core.util.ExpandingReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link ExpandingReader}
 *
 * @author bpasero
 */
public class ExpandingReaderTests {

  /**
   * @throws Exception
   */
  @Test
  public void testSingleWordNoTags() throws Exception {
    String s = "Hello";

    List<String> words = Arrays.asList("World");

    String preExpand = "(";
    String postExpand = ")";

    boolean skipTags = true;

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    String result = readFully(reader);

    assertEquals("Hello", result);

    //2
    skipTags = false;
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("Hello", result);

    //3
    skipTags = true;
    words = Arrays.asList("hello");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("(Hello)", result);

    //4
    skipTags = false;
    words = Arrays.asList("hello");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("(Hello)", result);

    //5
    preExpand = "hello";
    postExpand = "Hello";
    skipTags = true;
    words = Arrays.asList("hello");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("helloHelloHello", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSingleWordTags() throws Exception {
    String s = "<Hello>";

    List<String> words = Arrays.asList("World");

    String preExpand = "(";
    String postExpand = ")";

    boolean skipTags = true;

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    String result = readFully(reader);

    assertEquals("<Hello>", result);

    //2
    skipTags = false;
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("<Hello>", result);

    //3
    skipTags = true;
    words = Arrays.asList("hello");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("<Hello>", result);

    //4
    skipTags = false;
    words = Arrays.asList("hello");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    result = readFully(reader);

    assertEquals("<(Hello)>", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMultipleWordsTags() throws Exception {
    String s = "<html>\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\">RSSOwl.org</a></p></body></html>";

    List<String> words = Arrays.asList("world");

    String preExpand = "<span>";
    String postExpand = "</span>";

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    String result = readFully(reader);

    assertEquals("<html>\n<body>\n\t<p>Hello <b><span>World</span></b> in <a href=\"http://www.rssowl.org\">RSSOwl.org</a></p></body></html>", result);

    //2
    words = Arrays.asList("html");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    result = readFully(reader);

    assertEquals("<html>\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\">RSSOwl.org</a></p></body></html>", result);

    //3
    words = Arrays.asList("www.rssowl.org");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    result = readFully(reader);

    assertEquals("<html>\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\">RSSOwl.org</a></p></body></html>", result);

    //4
    words = Arrays.asList("rssowl");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    result = readFully(reader);

    assertEquals("<html>\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\"><span>RSSOwl</span>.org</a></p></body></html>", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMultipleWordsBrokenTags() throws Exception {
    String s = "<html\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\">RSSOwl.org/a></p></body></html>";

    List<String> words = Arrays.asList("world");

    String preExpand = "<span>";
    String postExpand = "</span>";

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    String result = readFully(reader);

    assertEquals("<html\n<body>\n\t<p>Hello <b><span>World</span></b> in <a href=\"http://www.rssowl.org\">RSSOwl.org/a></p></body></html>", result);

    //2
    words = Arrays.asList("html");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    result = readFully(reader);

    assertEquals("<html\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\">RSSOwl.org/a></p></body></html>", result);

    //3
    words = Arrays.asList("www.rssowl.org");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    result = readFully(reader);

    assertEquals("<html\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\">RSSOwl.org/a></p></body></html>", result);

    //4
    words = Arrays.asList("rssowl");
    reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    result = readFully(reader);

    assertEquals("<html\n<body>\n\t<p>Hello <b>World</b> in <a href=\"http://www.rssowl.org\"><span>RSSOwl</span>.org/a></p></body></html>", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRealWorldExample1() throws Exception {
    String s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\r\n"
        + "\r\n"
        + "<head>\r\n"
        + "  <title>RSSOwl - A Java RSS / RDF / Atom Newsreader | May the owl be with you</title>\r\n"
        + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n"
        + "<base href=\"http://www.rssowl.org/\" />\r\n"
        + "<style type=\"text/css\" media=\"all\">@import \"misc/drupal.css\";</style><link rel=\"alternate\" type=\"application/atom+xml\" title=\"Atom\" href=\"newsfeed\" />\r\n"
        + "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS\" href=\"http://www.rssowl.org/node/feed\" />\r\n"
        + "\r\n"
        + "\r\n"
        + "  <style type=\"text/css\" media=\"all\">@import \"themes/negen9/style.css\";</style>\r\n"
        + "\r\n"
        + "  <script type=\"text/javascript\"> </script>\r\n"
        + "\r\n"
        + "    <!-- Google Analytics -->\r\n"
        + "    <script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\"></script>\r\n"
        + "    <script type=\"text/javascript\">\r\n"
        + "        _uacct = \"UA-581315-1\";\r\n"
        + "        urchinTracker();\r\n"
        + "    </script>\r\n"
        + "  <link rel=\"shortcut icon\" href=\"misc/favicon.ico\" />\r\n"
        + "\r\n"
        + "</head>\r\n"
        + "\r\n"
        + "<body>\r\n"
        + "\r\n"
        + "\r\n"
        + "\r\n"
        + "<div id=\"shadow_top\">\r\n"
        + "<br />\r\n"
        + "</div>\r\n"
        + "\r\n"
        + "<div id=\"body_wrapper\">\r\n"
        + "\r\n"
        + "<div id=\"page\">\r\n"
        + "\r\n"
        + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"header\">\r\n"
        + "  <tr>\r\n"
        + "    <td id=\"logo\">\r\n"
        + "\r\n"
        + "      <img src=\"files/logo.jpg\" alt=\"Home\" usemap=\"#bannermap\" />\r\n"
        + "       <map id=\"bannermap\" name=\"bannermap\">\r\n"
        + "       <area shape=\"rect\" coords=\"5,2,240,73\" href=\"/\" alt=\"Owl\" />\r\n"
        + "       </map>\r\n"
        + "\r\n"
        + "       \r\n"
        + "\r\n"
        + "       \r\n"
        + "\r\n"
        + "\r\n"
        + "    </td>\r\n"
        + "    </tr>\r\n"
        + "    <tr>\r\n"
        + "    <td id=\"menu\">\r\n"
        + "\r\n"
        + "      <div id=\"primary\"><a href=\"overview\">Overview</a>\r\n"
        + "<a href=\"download\">Download</a>\r\n"
        + "<a href=\"contribute\">Contribute</a>\r\n"
        + "<a href=\"help\">Help</a>\r\n"
        + "<a href=\"dev\">Development</a>\r\n"
        + "<a href=\"search\">Search</a>\r\n"
        + "<a href=\"contact\">Contact</a></div>\r\n"
        + "      <div id=\"secondary\"></div>\r\n"
        + "\r\n"
        + "       \r\n"
        + "    </td>\r\n"
        + "  </tr>\r\n"
        + "</table>\r\n"
        + "\r\n"
        + "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" id=\"content\">\r\n"
        + "  <tr>\r\n"
        + "    \r\n"
        + "    <td valign=\"top\">\r\n"
        + "       \r\n"
        + "      <div id=\"main\">\r\n"
        + "         \r\n"
        + "         \r\n"
        + "         \r\n"
        + "\r\n"
        + "<!-- begin content -->\r\n"
        + "\r\n"
        + "  <div class=\"node sticky\">\r\n"
        + "\r\n"
        + "     \r\n"
        + "    <h2 class=\"title\"><a href=\"node/216\">RSSOwl 2.0 Milestone 6 is out!</a></h2>\r\n"
        + "    <div class=\"submitted\">Submitted by bpasero on Thu, 2007-06-07 23:19.</div>\r\n"
        + "\r\n"
        + "    <div class=\"content\"><p>We are happy to announce that the next milestone of RSSOwl 2.0 is now available for <a href=\"http://boreal.rssowl.org/#download\">download</a>. This release comes with some very cool features:</p>\r\n"
        + "<ul >\r\n"
        + "<li >Live update of saved searches</li>\r\n"
        + "<li >Notification popup when receiving news</li>\r\n"
        + "\r\n"
        + "<li >Faster search and relevance indicator</li>\r\n"
        + "</ul>\r\n"
        + "<p>A flash demo showing the new features in action is <a href=\"http://boreal.rssowl.org/demos/rssowl_2_m6_nn.html\">available</a> as well. If you have used previous versions of RSSOwl 2.0, make sure to read the notes on <a href=\"http://boreal.rssowl.org/#2a\">how to update</a> to milestone 6.</p>\r\n"
        + "<p>For more information on RSSOwl 2.0, please visit <a href=\"http://boreal.rssowl.org\">boreal.rssowl.org</a>.</p>\r\n"
        + "</div>\r\n"
        + "    <div class=\"links\"><a href=\"user/login\">login</a> or <a href=\"user/register\">register</a> to post comments</div>\r\n"
        + "\r\n"
        + "     \r\n"
        + "  </div>\r\n"
        + "\r\n"
        + "  <div class=\"node \">\r\n"
        + "     \r\n"
        + "    <h2 class=\"title\"><a href=\"node/217\">See what&#039;s hot in upcoming RSSOwl 2.0 M7</a></h2>\r\n"
        + "    <div class=\"submitted\">Submitted by bpasero on Mon, 2007-10-01 09:43.</div>\r\n"
        + "\r\n"
        + "    <div class=\"content\"><p>\r\n"
        + "We have been very busy working on the next milestone 7 of RSSOwl 2 \r\n"
        + "and made some great progress. Here is a sneak preview of four of \r\n"
        + "the hottest new features. Expect M7 to be released in november. <a href=\"http://www.rssowl.org/node/217\">Read more...</a>\r\n"
        + "\r\n"
        + "</p>\r\n"
        + "</div>\r\n"
        + "    <div class=\"links\"><a href=\"user/login\">login</a> or <a href=\"user/register\">register</a> to post comments | <a href=\"node/217\" title=\"Read the rest of this posting.\" class=\"read-more\">read more</a></div>\r\n"
        + "     \r\n"
        + "  </div>\r\n"
        + "\r\n"
        + "  <div class=\"node \">\r\n"
        + "     \r\n"
        + "    <h2 class=\"title\"><a href=\"node/213\">RSSOwl 2.0 Milestone 5a is released</a></h2>\r\n"
        + "\r\n"
        + "    <div class=\"submitted\">Submitted by bpasero on Sun, 2007-03-25 22:18.</div>\r\n"
        + "\r\n"
        + "    <div class=\"content\">This release addresses some bugs in the previous version as well as providing support for connections through a proxy server. The full list of changes is available from  <a href=\"http://dev.rssowl.org/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=&target_milestone=2.0+M5eh&long_desc_type=substring&long_desc=&bug_file_loc_type=allwordssubstr&bug_file_loc=&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&emailassigned_to1=1&emailtype1=substring&email1=&emailassigned_to2=1&emailreporter2=1&emailcc2=1&emailtype2=substring&email2=&bugidtype=include&bug_id=&votes=&chfieldfrom=&chfieldto=Now&chfieldvalue=&cmdtype=doit&order=Reuse+same+sort+as+last+time&field0-0-0=noop&type0-0-0=noop&value0-0-0=\">here</a> and downloads are available from <a href=\"http://boreal.rssowl.org\">boreal.rssowl.org</a>.\r\n"
        + "<br><br>\r\n"
        + "Ben</div>\r\n"
        + "    <div class=\"links\"><a href=\"node/213#comment\" title=\"Jump to the first comment of this posting.\">5 comments</a></div>\r\n"
        + "\r\n"
        + "     \r\n"
        + "  </div>\r\n"
        + "\r\n"
        + "  <div class=\"node \">\r\n"
        + "     \r\n"
        + "    <h2 class=\"title\"><a href=\"node/212\">Flash demo of RSSOwl 2.0 M5 now available</a></h2>\r\n"
        + "    <div class=\"submitted\">Submitted by bpasero on Tue, 2007-03-13 15:19.</div>\r\n"
        + "\r\n"
        + "    <div class=\"content\">I am back from the EclipseCon (it was just great) and finally had enough time to upload the flash demo. You can watch it from <a href=\"http://boreal.rssowl.org/\">boreal.rssowl.org</a>. Make sure to turn audio volume on!\r\n"
        + "<br><br>\r\n"
        + "\r\n"
        + "Ben</div>\r\n"
        + "    <div class=\"links\"><a href=\"node/212#comment\" title=\"Jump to the first comment of this posting.\">2 comments</a></div>\r\n"
        + "     \r\n"
        + "  </div>\r\n"
        + "\r\n"
        + "  <div class=\"node \">\r\n"
        + "     \r\n"
        + "    <h2 class=\"title\"><a href=\"node/211\">RSSOwl 2.0 Milestone 5 now available for download</a></h2>\r\n"
        + "    <div class=\"submitted\">Submitted by bpasero on Fri, 2007-03-02 15:09.</div>\r\n"
        + "\r\n"
        + "    <div class=\"content\">We are happy to announce the availability of <b>RSSOwl 2.0 Milestone 5</b>. Keep in mind that this is a <b>preview</b> and not yet the final version. You will find more information and links to downloads from the new domain:<br><br>\r\n"
        + "\r\n"
        + "<a href=\"http://boreal.rssowl.org/\">http://boreal.rssowl.org</a>\r\n"
        + "<br><br>\r\n"
        + "Ben</div>\r\n"
        + "    <div class=\"links\"><a href=\"node/211#comment\" title=\"Jump to the first comment of this posting.\">19 comments</a></div>\r\n"
        + "\r\n"
        + "     \r\n"
        + "  </div>\r\n"
        + "<div id=\"pager\" class=\"container-inline\"><div class=\"pager-first\"> </div><div class=\"pager-previous\"><div class=\"pager-first\"> </div></div><div class=\"pager-list\"><strong>1</strong> <div class=\"pager-next\"><a href=\"node?from=5\">2</a></div> <div class=\"pager-next\"><a href=\"node?from=10\">3</a></div> <div class=\"pager-next\"><a href=\"node?from=15\">4</a></div> <div class=\"pager-next\"><a href=\"node?from=20\">5</a></div> <div class=\"pager-next\"><a href=\"node?from=25\">6</a></div> <div class=\"pager-next\"><a href=\"node?from=30\">7</a></div> <div class=\"pager-next\"><a href=\"node?from=35\">8</a></div> <div class=\"pager-next\"><a href=\"node?from=40\">9</a></div> <div class=\"pager-list-dots-right\">...</div></div><div class=\"pager-next\"><a href=\"node?from=5\">next page</a></div><div class=\"pager-last\"><a href=\"node?from=60\">last page</a></div></div>\r\n"
        + "\r\n"
        + "<!-- end content -->\r\n"
        + "\r\n"
        + "      </div><!-- main -->\r\n"
        + "\r\n"
        + "    </td>\r\n"
        + "\r\n"
        + "      <td id=\"sidebar-right\">\r\n"
        + "\r\n"
        + "            <div class=\"sidelinks\">\r\n"
        + "      <a href=\"http://sourceforge.net/project/showfiles.php?group_id=86683&amp;package_id=90094&amp;release_id=466721\">\r\n"
        + "        <img src=\"images/common/quickdl_small.jpg\" alt=\"Quick Download!\" width=\"130\" height=\"70\" /></a><br /><br />\r\n"
        + "\r\n"
        + "      </div>\r\n"
        + "      <div class=\"login\">\r\n"
        + "      \r\n"
        + "  <div class=\"block block-user\" id=\"block-user-0\">\r\n"
        + "    <h2 class=\"title\">User login</h2>\r\n"
        + "    <div class=\"content\"><form action=\"user/login?destination=node\" method=\"post\">\r\n"
        + "<div class=\"user-login-block\">\r\n"
        + "<div class=\"form-item\">\r\n"
        + " <label for=\"edit-name\">Username:</label><br />\r\n"
        + " <input type=\"text\" maxlength=\"64\" class=\"form-text\" name=\"edit[name]\" id=\"edit-name\" size=\"15\" value=\"\" />\r\n"
        + "\r\n"
        + "</div>\r\n"
        + "<div class=\"form-item\">\r\n"
        + " <label for=\"edit-pass\">Password:</label><br />\r\n"
        + " <input type=\"password\" class=\"form-password\" maxlength=\"64\" name=\"edit[pass]\" id=\"edit-pass\" size=\"15\" value=\"\" />\r\n"
        + "</div>\r\n"
        + "<input type=\"submit\" class=\"form-submit\" name=\"op\" value=\"Log in\"  />\r\n"
        + "</div>\r\n"
        + "\r\n"
        + "</form>\r\n"
        + "<div class=\"item-list\"><ul><li><a href=\"user/register\" title=\"Create a new user account.\">Create new account</a></li><li><a href=\"user/password\" title=\"Request new password via e-mail.\">Request new password</a></li></ul></div></div>\r\n"
        + " </div>\r\n"
        + "\r\n"
        + "      </div>\r\n"
        + "      <br />\r\n"
        + "\r\n"
        + "      <div class=\"contrib\">\r\n"
        + "      <table>\r\n"
        + "        <tr>\r\n"
        + "            <td>\r\n"
        + "                <img src=\"images/common/contrib_small.jpg\" alt=\"Contributors\" width=\"130\" height=\"15\" /><br /><br />\r\n"
        + "            </td>\r\n"
        + "\r\n"
        + "        </tr>\r\n"
        + "        <tr>\r\n"
        + "            <td>\r\n"
        + "                <a href=\"http://sourceforge.net/donate/index.php?group_id=86683\">\r\n"
        + "                    <img src=\"/images/donate.gif\">\r\n"
        + "                </a>\r\n"
        + "                \r\n"
        + "                <!--<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"post\">\r\n"
        + "                    <input type=\"hidden\" name=\"cmd\" value=\"_s-xclick\">\r\n"
        + "                    <input type=\"image\" style=\"border:0;\" src=\"http://www.rssowl.org/images/donate.gif\" border=\"0\" name=\"submit\" alt=\"Zahlen Sie mit PayPal - schnell, kostenlos und sicher!\">\r\n"
        + "                    <input type=\"hidden\" name=\"encrypted\" value=\"-----BEGIN PKCS7-----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-----END PKCS7-----\">\r\n"
        + "                </form>\r\n" + "                <img alt=\"\" border=\"0\" src=\"https://www.paypal.com/de_DE/i/scr/pixel.gif\" width=\"0\" height=\"0\">\r\n" + "                <br />-->\r\n" + "            </td>\r\n" + "\r\n" + "\r\n" + "        </tr>\r\n" + "        <tr>\r\n" + "            <td>\r\n" + "                <a href=\"http://sourceforge.net/users/weppos/\">pokeronamac.com: </a><br />\"RSSOwl is a huge time saver\"<br /><br />\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "        <tr>\r\n" + "\r\n" + "            <td>\r\n" + "                <a href=\"http://sourceforge.net/users/weppos/\">weppos: </a><br />\"Simply the best RSS reader. Fast, lightweight and cross platform.\"<br /><br />\r\n" + "            </td>\r\n" + "        </tr>\r\n"
        + "        <tr>\r\n" + "            <td>\r\n" + "                <a href=\"http://sourceforge.net/users/mr_dfuse/\">mr_dfuse: </a><br />\"Best RSS Reader there is! Very polished and attention to detail!\"<br /><br />\r\n" + "\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "        <tr>\r\n" + "            <td>\r\n" + "                <a href=\"\">anonymous: </a><br />\"I would give RSSOwl my first newborn. Yes, it\'s that good.\"<br /><br />\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "        <tr>\r\n" + "\r\n" + "            <td>\r\n" + "                <a href=\"http://sourceforge.net/users/aguafuertes/\">aguafuertes: </a><br />\"Great tool and impressive development. Let\'s go!!!\"<br /><br />\r\n" + "            </td>\r\n" + "        </tr>\r\n"
        + "        <tr>\r\n" + "            <td>\r\n" + "                <a href=\"http://sourceforge.net/users/mac586/\">mac586: </a><br />\"I Love this application!\"<br /><br />\r\n" + "\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "      </table>\r\n" + "      </div>\r\n" + "      <br />\r\n" + "      <div class=\"contrib\">\r\n" + "      <table>\r\n" + "        <tr>\r\n" + "            <td>\r\n" + "\r\n" + "                <img src=\"images/common/oss_small.jpg\" alt=\"Contributors\" width=\"130\" height=\"15\" /><br /><br />\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "        <tr>\r\n" + "            <td>\r\n"
        + "                <a href=\"https://sourceforge.net/projects/rssowl/\"> <img src=\"http://sflogo.sourceforge.net/sflogo.php?group_id=86683&amp;type=1\"\r\n" + "                  alt=\"SourceForge.net Logo\" height=\"31\" width=\"88\" /></a>\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "\r\n" + "        <tr>\r\n" + "            <td>\r\n" + "                <a href=\"http://www.opensource.org\"><img src=\"images/common/opensource.gif\" alt=\"Open Source\" width=\"88\" height=\"31\" /></a>\r\n" + "            </td>\r\n" + "        </tr>\r\n" + "\r\n" + "      </table>\r\n" + "      </div>\r\n" + "\r\n" + "      <br />\r\n" + "\r\n" + "      <div class=\"contrib\" align=\"center\">\r\n"
        + "        RSSOwl 2.0 is profiled by YourKit <a href=\"http://www.yourkit.com\">Java Profiler</a>\r\n" + "      </div>\r\n" + "\r\n" + "    </td>\r\n" + "\r\n" + "  </tr>\r\n" + "</table>\r\n" + "\r\n" + "<div id=\"footer\">\r\n" + "\r\n" + "  2003-2007 rssowl.org . Powered by <a href=\"http://www.drupal.org\">Drupal</a>. Original Theme by <a href=\"http://negen.altervista.org\">Negen</a>. Logo Design by <a href=\"http://www.jesseross.com\">Jesse Ross</a>. Site Concept by <a href=\"http://eichert.co.uk\">Tobias Eichert</a>.\r\n" + "</div>\r\n" + "</div> <!-- page -->\r\n" + "\r\n" + "</div> <!-- page wrapper -->\r\n" + "\r\n" + "<div id=\"shadow_bottom\">\r\n" + "<br />\r\n" + "</div>\r\n" + "</body>\r\n" + "</html>\r\n" + "";

    String preExpand = "<span>";
    String postExpand = "</span>";

    /* Just make sure no Exception is thrown with this content */
    List<String> words = Arrays.asList("rssowl", "blog", "news", "boreal", "download", "about", "contact", "img", "href", "\"", "\'", ".", ",");

    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, true);
    readFully(reader);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSingleCharTag() throws Exception {
    String s = "<a>Foo</a>";

    List<String> words = Arrays.asList("a");

    String preExpand = "(";
    String postExpand = ")";

    boolean skipTags = true;

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    String result = readFully(reader);

    assertEquals("<a>Foo</a>", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testStarWildcard() throws Exception {
    String s = "Hello <a>World</a>";

    List<String> words = Arrays.asList("*");

    String preExpand = "(";
    String postExpand = ")";

    boolean skipTags = true;

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    String result = readFully(reader);

    assertEquals("(Hello) <a>(World)</a>", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testQuestionmarkWildcard() throws Exception {
    String s = "Hello i <a>World</a>";

    List<String> words = Arrays.asList("?");

    String preExpand = "(";
    String postExpand = ")";

    boolean skipTags = true;

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    String result = readFully(reader);

    assertEquals("Hello (i) <a>World</a>", result);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testWildcards() throws Exception {
    String s = "Hello <a>World</a>";

    List<String> words = Arrays.asList("*", "?");

    String preExpand = "(";
    String postExpand = ")";

    boolean skipTags = true;

    //1
    ExpandingReader reader = new ExpandingReader(new StringReader(s), words, preExpand, postExpand, skipTags);
    String result = readFully(reader);

    assertEquals("(Hello) <a>(World)</a>", result);
  }

  private String readFully(Reader reader) throws IOException {
    StringBuilder str = new StringBuilder();

    int i;
    while ((i = reader.read()) != -1)
      str.append((char) i);

    return str.toString();
  }
}