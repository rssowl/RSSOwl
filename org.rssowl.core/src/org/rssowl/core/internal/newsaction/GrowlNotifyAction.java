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

package org.rssowl.core.internal.newsaction;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.rssowl.core.INewsAction;
import org.rssowl.core.Owl;
import org.rssowl.core.internal.Activator;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.INews;
import org.rssowl.core.util.BatchedBuffer;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.DateUtils;
import org.rssowl.core.util.StreamGobbler;
import org.rssowl.core.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of {@link INewsAction} to show matching news in Growl. This
 * is only supported on Mac.
 *
 * @author bpasero
 */
public class GrowlNotifyAction implements INewsAction {

  /* Batch News-Events for every 5 seconds */
  private static final int BATCH_INTERVAL = 5000;

  /* Max number of items to show per Notification */
  private static final int MAX_ITEMS_TO_SHOW = 5;

  private static final String APPLICATION_NAME = "RSSOwl"; //$NON-NLS-1$
  private static final String SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

  private BatchedBuffer<INews> fBatchedBuffer;
  private String fPathToGrowlNotify;

  /** Initialize a Batched Buffer for Growl Notifications */
  public GrowlNotifyAction() {
    BatchedBuffer.Receiver<INews> receiver = new BatchedBuffer.Receiver<INews>() {
      @Override
      public IStatus receive(Collection<INews> items, Job job, IProgressMonitor monitor) {
        try {
          if (!Owl.isShuttingDown())
            executeCommand(fPathToGrowlNotify, items);
        }

        /* Log any error message */
        catch (IOException e) {
          Activator.safeLogError(e.getMessage(), e);
        }

        return Status.OK_STATUS;
      }
    };

    fBatchedBuffer = new BatchedBuffer<INews>(receiver, BATCH_INTERVAL);
  }

  /*
   * @see org.rssowl.core.INewsAction#run(java.util.List, java.util.Map,
   * java.lang.Object)
   */
  @Override
  public List<IEntity> run(List<INews> news, Map<INews, INews> replacements, Object data) {

    /* Ensure to Pickup Replaces */
    news = CoreUtils.replace(news, replacements);

    /* Launch if file exists */
    if (data instanceof String && new File((String) data).exists()) {
      fPathToGrowlNotify = (String) data;
      fBatchedBuffer.addAll(news);
    }

    /* Nothing to Save */
    return Collections.emptyList();
  }

  private void executeCommand(String pathToGrowlnotify, Collection<INews> news) throws IOException {
    if (StringUtils.isSet(pathToGrowlnotify)) {
      List<String> commands = new ArrayList<String>();
      commands.add(pathToGrowlnotify);
      commands.add("--name"); //$NON-NLS-1$
      commands.add(APPLICATION_NAME);
      commands.add("-a"); //$NON-NLS-1$
      commands.add(APPLICATION_NAME);
      commands.add("-t"); //$NON-NLS-1$
      commands.add(NLS.bind(Messages.GrowlNotifyAction_N_INCOMING_NEWS, news.size()));
      commands.add("-m"); //$NON-NLS-1$

      /* Sort News by Date */
      Set<INews> sortedNews = new TreeSet<INews>(new Comparator<INews>() {
        @Override
        public int compare(INews news1, INews news2) {
          Date date1 = DateUtils.getRecentDate(news1);
          Date date2 = DateUtils.getRecentDate(news2);

          int res = date2.compareTo(date1);
          if (res != 0)
            return res;

          return -1;
        }
      });
      sortedNews.addAll(news);

      int i = 0;
      StringBuilder message = new StringBuilder();
      for (INews item : sortedNews) {
        if (++i > MAX_ITEMS_TO_SHOW)
          break;

        message.append(CoreUtils.getHeadline(item, true)).append(SEPARATOR).append(SEPARATOR);
      }

      if (news.size() > MAX_ITEMS_TO_SHOW)
        message.append(NLS.bind(Messages.GrowlNotifyAction_N_MORE, (news.size() - MAX_ITEMS_TO_SHOW)));

      commands.add(message.toString());

      /* Execute */
      Process proc = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));

      /* Write Message to Growl */
      OutputStream outputStream = proc.getOutputStream();
      outputStream.write(message.toString().getBytes());
      outputStream.close();

      /* Let StreamGobbler handle error message */
      StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());

      /* Let StreamGobbler handle output */
      StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

      /* Flush both error and output streams */
      errorGobbler.schedule();
      outputGobbler.schedule();
    }
  }

  /*
   * @see org.rssowl.core.INewsAction#conflictsWith(org.rssowl.core.INewsAction)
   */
  @Override
  public boolean conflictsWith(INewsAction otherAction) {
    return false;
  }

  /*
   * @see org.rssowl.core.INewsAction#getLabel(java.lang.Object)
   */
  @Override
  public String getLabel(Object data) {
    return null;
  }
}