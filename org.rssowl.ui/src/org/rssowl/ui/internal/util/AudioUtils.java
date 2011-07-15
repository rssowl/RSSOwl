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

package org.rssowl.ui.internal.util;

import org.rssowl.ui.internal.Activator;
import org.rssowl.ui.internal.Controller;

import java.io.File;
import java.io.IOException;

/**
 * @author bpasero
 */
public class AudioUtils {

  /* Determine if Audio is Supported */
  private static final boolean IS_SUPPORTED = getSupported();

  /* This utility class constructor is hidden */
  private AudioUtils() {
  // Protect default constructor
  }

  private static boolean getSupported() {
    try {
      Class.forName("javax.sound.sampled.AudioSystem"); //$NON-NLS-1$
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * @return <code>true</code> if playing audio files is supported and
   * <code>false</code> otherwise.
   */
  public static boolean isSupported() {
    return IS_SUPPORTED;
  }

  /**
   * Tries to play the given file.
   *
   * @param file the sound to play.
   */
  public static void play(final String file) {
    JobRunner.runInBackgroundThread(new Runnable() {
      public void run() {
        try {
          if (!Controller.getDefault().isShuttingDown())
            doPlay(file);
        } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
          Activator.safeLogError(e.getMessage(), e);
        } catch (IOException e) {
          Activator.safeLogError(e.getMessage(), e);
        } catch (javax.sound.sampled.LineUnavailableException e) {
          Activator.safeLogError(e.getMessage(), e);
        }
      }
    });
  }

  private static void doPlay(String file) throws javax.sound.sampled.UnsupportedAudioFileException, IOException, javax.sound.sampled.LineUnavailableException {

    /* Open the Input-Stream to the Audio File */
    javax.sound.sampled.AudioInputStream inS = null;
    try {
      inS = javax.sound.sampled.AudioSystem.getAudioInputStream(new File(file));

      /* Retrieve Format to actually play the sound */
      javax.sound.sampled.AudioFormat audioFormat = inS.getFormat();

      /* Open a SourceDataLine for Playback */
      javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, audioFormat);
      javax.sound.sampled.SourceDataLine line = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem.getLine(info);
      line.open(audioFormat);

      /* Activate the line */
      line.start();

      int read = 0;
      byte[] buf = new byte[1024];
      while ((read = inS.read(buf, 0, buf.length)) != -1 && !Controller.getDefault().isShuttingDown())
        line.write(buf, 0, read);

      line.drain();
      line.close();
    } finally {
      if (inS != null)
        inS.close();
    }
  }
}