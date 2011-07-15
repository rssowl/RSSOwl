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

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IAttachment;
import org.rssowl.core.persist.IFeed;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.services.DownloadService;
import org.rssowl.ui.internal.services.DownloadService.DownloadRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Testing the DownloadService.
 *
 * @author bpasero
 */
public class DownloadServiceTests {
  private static final SimpleDateFormat DOWNLOAD_FILE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US); //$NON-NLS-1$

  private File fTmpDir;
  private DownloadService fService;
  private IModelFactory fFactory = Owl.getModelFactory();

  /**
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    fService = Controller.getDefault().getDownloadService();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAttachmentNameGivenFileNotExistNormal() throws Exception {
    File tmpDir = getTmpDir();
    String fileName = "mytest.txt";

    DownloadRequest request = getAttachmentDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", "text/plain", 23, tmpDir, fileName);
    fService.download(request);

    File download = new File(tmpDir, "mytest.txt");
    assertTrue(download.exists());
    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAttachmentNameGivenFileNotExistContentDisposition() throws Exception {
    File tmpDir = getTmpDir();
    String fileName = "mytest.txt";

    DownloadRequest request = getAttachmentDownloadRequest("http://www.jtricks.com/download-text", null, 0, tmpDir, fileName);
    fService.download(request);

    File download = new File(tmpDir, fileName);
    assertTrue(download.exists());
    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAttachmentNoNameGivenFileNotExistNormal() throws Exception {
    File tmpDir = getTmpDir();

    DownloadRequest request = getAttachmentDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", null, 20, tmpDir, null);
    fService.download(request);

    File download = new File(tmpDir, "test.txt");
    assertTrue(download.exists());
    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAttachmentNoNameGivenFileNotExistContentDisposition() throws Exception {
    File tmpDir = getTmpDir();

    DownloadRequest request = getAttachmentDownloadRequest("http://www.jtricks.com/download-text", null, 0, tmpDir, null);
    fService.download(request);

    File download = new File(tmpDir, "content.txt");
    assertTrue(download.exists());
    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAttachmentNameGivenFileExists() throws Exception {
    File tmpDir = getTmpDir();
    String fileName = "mytest.txt";

    Date date = new Date(System.currentTimeMillis());

    DownloadRequest request = getAttachmentDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", null, 0, tmpDir, fileName, date);
    fService.download(request);

    File download = new File(tmpDir, "mytest.txt");
    assertTrue(download.exists());

    request = getAttachmentDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", null, 0, tmpDir, fileName, date);
    fService.download(request);

    File secondDownload = new File(tmpDir, DOWNLOAD_FILE_DATE_FORMAT.format(date) + "_" + "mytest.txt");
    assertTrue(!secondDownload.exists());

    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAttachmentNoNameGivenFileExists() throws Exception {
    File tmpDir = getTmpDir();

    Date date = new Date(System.currentTimeMillis());

    DownloadRequest request = getAttachmentDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", null, 0, tmpDir, null, date);
    fService.download(request);

    File download = new File(tmpDir, "test.txt");
    assertTrue(download.exists());

    request = getAttachmentDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", null, 0, tmpDir, null, date);
    fService.download(request);

    File secondDownload = new File(tmpDir, "test_" + DOWNLOAD_FILE_DATE_FORMAT.format(date) + ".txt");
    assertTrue(secondDownload.exists());

    secondDownload.delete();
    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsTextFile() throws Exception {
    File tmpDir = getTmpDir();

    DownloadRequest request = getNewsDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.txt", tmpDir);
    fService.download(request);

    File download = new File(tmpDir, "test.txt");
    assertTrue(download.exists());
    download.delete();
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsWebsite() throws Exception {
    File tmpDir = getTmpDir();

    DownloadRequest request = getNewsDownloadRequest("http://www.rssowl.org/rssowl2dg/tests/download/test.html", tmpDir);
    fService.download(request);

    File download = new File(tmpDir, "test.html");
    assertTrue(!download.exists());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testNewsContentDisposition() throws Exception {
    File tmpDir = getTmpDir();

    DownloadRequest request = getNewsDownloadRequest("http://www.jtricks.com/download-text", tmpDir);
    fService.download(request);

    File download = new File(tmpDir, "content.txt");
    assertTrue(download.exists());
    download.delete();
  }

  private File getTmpDir() throws IOException {
    if (fTmpDir == null) {
      File tmpFile = File.createTempFile("rssowl", "tmp");
      fTmpDir = tmpFile.getParentFile();
      tmpFile.delete();
    }

    return fTmpDir;
  }

  private INews getNews(String link, Date date) {
    IFeed feed = fFactory.createFeed(null, URI.create("http://www.rssowl.org"));
    INews news = fFactory.createNews(null, feed, date != null ? date : new Date());
    news.setLink(URI.create(link));

    return news;
  }

  private IAttachment getAttachment(INews news, String link, String type, int length) {
    IAttachment attachment = fFactory.createAttachment(null, news);
    attachment.setLink(URI.create(link));
    attachment.setType(type);
    attachment.setLength(length);

    return attachment;
  }

  private DownloadService.DownloadRequest getAttachmentDownloadRequest(String link, String type, int length, File folder, String name) {
    return getAttachmentDownloadRequest(link, type, length, folder, name, null);
  }

  private DownloadService.DownloadRequest getAttachmentDownloadRequest(String link, String type, int length, File folder, String name, Date date) {
    INews news = getNews("", date);
    IAttachment attachment = getAttachment(news, link, type, length);

    return DownloadService.DownloadRequest.createAttachmentDownloadRequest(attachment, URI.create(link), folder, false, name);
  }

  private DownloadService.DownloadRequest getNewsDownloadRequest(String link, File folder) {
    INews news = getNews("", null);

    return DownloadService.DownloadRequest.createNewsDownloadRequest(news, URI.create(link), folder);
  }
}