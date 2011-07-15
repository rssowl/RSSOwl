/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2011 RSSOwl Development Team                                  **
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

package org.rssowl.core.util;

import org.rssowl.core.internal.newsaction.DeleteNewsAction;
import org.rssowl.core.internal.newsaction.LabelNewsAction;
import org.rssowl.core.internal.newsaction.MarkReadNewsAction;
import org.rssowl.core.internal.newsaction.MarkStickyNewsAction;
import org.rssowl.core.internal.newsaction.MarkUnreadNewsAction;
import org.rssowl.core.persist.IFilterAction;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.ISearchFilter;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.event.NewsEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link SyncItem} is used to synchronize changes to {@link INews} with an
 * online service like Google Reader.
 *
 * @author bpasero
 */
public class SyncItem implements Serializable {

  /* Serial Version UID to support class changes */
  private static final long serialVersionUID = 4093540431879243015L;

  /* Set of unread states */
  private static final Set<INews.State> UNREAD_STATES = EnumSet.of(INews.State.NEW, INews.State.UNREAD, INews.State.UPDATED);

  private final String fId;
  private final String fStreamId;
  private boolean fMarkedRead;
  private boolean fMarkedUnread;
  private boolean fStarred;
  private boolean fUnStarred;
  private List<String> fAddedLabels;
  private List<String> fRemovedLabels;

  /**
   * Creates a {@link SyncItem} out of a {@link NewsEvent}.
   *
   * @param event the {@link NewsEvent} to create a {@link SyncItem} from.
   * @return the {@link SyncItem} from the {@link NewsEvent}.
   */
  public static SyncItem toSyncItem(NewsEvent event) {
    boolean requiresSync = false;
    INews item = event.getEntity();
    SyncItem syncItem = toSyncItem(item);

    /* State Change */
    INews.State oldState = event.getOldNews().getState();
    INews.State newState = item.getState();
    if (oldState != newState) {

      /* Marked Read */
      if (newState == INews.State.READ && UNREAD_STATES.contains(oldState)) {
        syncItem.setMarkedRead();
        requiresSync = true;
      }

      /* Marked Unread */
      else if (newState == INews.State.UNREAD && oldState == INews.State.READ) {
        syncItem.setMarkedUnread();
        requiresSync = true;
      }

      /* Delete */
      else if (newState == INews.State.HIDDEN || newState == INews.State.DELETED) {

        /* Mark Read if Unread and remove Star if Flagged */
        if (UNREAD_STATES.contains(oldState)) {
          syncItem.setMarkedRead();
          if (item.isFlagged())
            syncItem.setUnStarred();
          requiresSync = true;
        }

        /* Remove Star if Flagged */
        else if (oldState == INews.State.READ && item.isFlagged()) {
          syncItem.setUnStarred();
          requiresSync = true;
        }
      }

      /* Restored */
      else if (!event.getOldNews().isVisible() && event.getEntity().isVisible()) {

        /* Restore Unread if previously unread */
        if (UNREAD_STATES.contains(newState)) {
          syncItem.setMarkedUnread();
          requiresSync = true;
        }

        /* Restore Star if previously starred */
        if (event.getEntity().isFlagged()) {
          syncItem.setStarred();
          requiresSync = true;
        }
      }
    }

    /* Sticky Change */
    boolean oldSticky = event.getOldNews().isFlagged();
    boolean newSticky = item.isFlagged();
    if (oldSticky != newSticky) {
      if (oldSticky)
        syncItem.setUnStarred();
      else
        syncItem.setStarred();

      requiresSync = true;
    }

    /* Label Change */
    Set<ILabel> oldLabels = event.getOldNews().getLabels();
    Set<ILabel> newLabels = item.getLabels();

    if (!Arrays.equals(oldLabels.toArray(), newLabels.toArray())) {
      Set<String> oldLabelNames = new HashSet<String>(oldLabels.size());
      for (ILabel oldLabel : oldLabels) {
        oldLabelNames.add(oldLabel.getName());
      }

      for (ILabel newLabel : newLabels) {
        if (!oldLabelNames.remove(newLabel.getName())) {
          syncItem.addLabel(newLabel.getName());
          requiresSync = true;
        }
      }

      for (String oldLabelName : oldLabelNames) {
        syncItem.removeLabel(oldLabelName);
        requiresSync = true;
      }
    }

    return requiresSync ? syncItem : null;
  }

  /**
   * Creates a {@link SyncItem} out of a {@link ISearchFilter}.
   *
   * @param filter the {@link ISearchFilter} to create a {@link SyncItem} from.
   * @param item the {@link INews} the {@link ISearchFilter} is operating on.
   * @return the {@link SyncItem} from the {@link ISearchFilter}.
   */
  public static SyncItem toSyncItem(ISearchFilter filter, INews item) {
    boolean requiresSync = false;
    SyncItem syncItem = toSyncItem(item);

    List<IFilterAction> actions = filter.getActions();
    for (IFilterAction action : actions) {
      String actionId = action.getActionId();

      /* State Change (Mark Read) */
      if (MarkReadNewsAction.ID.equals(actionId)) {
        syncItem.setMarkedRead();
        requiresSync = true;
      }

      /* State Change (Mark Unread) */
      if (MarkUnreadNewsAction.ID.equals(actionId)) {
        syncItem.setMarkedUnread();
        requiresSync = true;
      }

      /* Delete (Sync like Mark Read) */
      if (DeleteNewsAction.ID.equals(actionId)) {
        syncItem.setMarkedRead();
        requiresSync = true;
      }

      /* Sticky Change */
      if (MarkStickyNewsAction.ID.equals(actionId)) {
        syncItem.setStarred();
        requiresSync = true;
      }

      /* Label Change */
      if (LabelNewsAction.ID.equals(actionId)) {
        Object data = action.getData();
        if (data != null && data instanceof Long) {
          Long labelId = (Long) data;
          ILabel label = DynamicDAO.load(ILabel.class, labelId);
          if (label != null) {
            syncItem.addLabel(label.getName());
            requiresSync = true;
          }
        }
      }
    }

    return requiresSync ? syncItem : null;
  }

  /**
   * @param news the {@link INews} to create a {@link SyncItem} from.
   * @return the {@link SyncItem} from the given {@link INews}.
   */
  public static SyncItem toSyncItem(INews news) {
    String itemId = news.getGuid().getValue();
    String streamId = news.getInReplyTo();

    return new SyncItem(itemId, streamId);
  }

  SyncItem(String id, String streamId) {
    fId = id;
    fStreamId = streamId;
  }

  /**
   * @return the identifier of this item.
   */
  public String getId() {
    return fId;
  }

  /**
   * @return the identifier of the stream this item belongs to.
   */
  public String getStreamId() {
    return fStreamId;
  }

  /**
   * Marks the item as read.
   */
  public void setMarkedRead() {
    fMarkedRead = true;
    fMarkedUnread = false;
  }

  /**
   * Marks the item as unread.
   */
  public void setMarkedUnread() {
    fMarkedUnread = true;
    fMarkedRead = false;
  }

  /**
   * Marks the item as starred.
   */
  public void setStarred() {
    fStarred = true;
    fUnStarred = false;
  }

  /**
   * Marks the item as un-starred.
   */
  public void setUnStarred() {
    fUnStarred = true;
    fStarred = false;
  }

  /**
   * @param label the label to add to the item.
   */
  public void addLabel(String label) {
    if (fAddedLabels == null)
      fAddedLabels = new ArrayList<String>(3);

    if (!fAddedLabels.contains(label))
      fAddedLabels.add(label);

    if (fRemovedLabels != null)
      fRemovedLabels.remove(label);
  }

  /**
   * @param label the label to remove from the item.
   */
  public void removeLabel(String label) {
    if (fRemovedLabels == null)
      fRemovedLabels = new ArrayList<String>(1);

    if (!fRemovedLabels.contains(label))
      fRemovedLabels.add(label);

    if (fAddedLabels != null)
      fAddedLabels.remove(label);
  }

  /**
   * @return <code>true</code> if the item is marked read.
   */
  public boolean isMarkedRead() {
    return fMarkedRead;
  }

  /**
   * @return <code>true</code> if the item is marked unread.
   */
  public boolean isMarkedUnread() {
    return fMarkedUnread;
  }

  /**
   * @return <code>true</code> if the item is starred.
   */
  public boolean isStarred() {
    return fStarred;
  }

  /**
   * @return <code>true</code> if the item is unstarred.
   */
  public boolean isUnStarred() {
    return fUnStarred;
  }

  /**
   * @return the {@link List} of labels to add.
   */
  public List<String> getAddedLabels() {
    return fAddedLabels != null ? fAddedLabels : Collections.<String> emptyList();
  }

  /**
   * @return the {@link List} of labels to be removed.
   */
  public List<String> getRemovedLabels() {
    return fRemovedLabels != null ? fRemovedLabels : Collections.<String> emptyList();
  }

  /**
   * Takes the properties from the provided {@link SyncItem} and updates them in
   * this item.
   *
   * @param item the other {@link SyncItem} to merge into this item.
   */
  public void merge(SyncItem item) {

    /* Mark Read */
    if (item.fMarkedRead)
      setMarkedRead();

    /* Mark Unread */
    if (item.fMarkedUnread)
      setMarkedUnread();

    /* Set Starred */
    if (item.fStarred)
      setStarred();

    /* Set Unstarred */
    if (item.fUnStarred)
      setUnStarred();

    /* Add Labels */
    if (item.fAddedLabels != null) {
      for (String label : item.fAddedLabels) {
        addLabel(label);
      }
    }

    /* Remove Labels */
    if (item.fRemovedLabels != null) {
      for (String label : item.fRemovedLabels) {
        removeLabel(label);
      }
    }
  }

  /**
   * This method will apply the properties of this {@link SyncItem} to the given
   * {@link INews}.
   *
   * @param news the {@link INews} to apply all properties of this
   * {@link SyncItem} to.
   */
  public void applyTo(INews news) {

    /* Mark Read */
    if (isMarkedRead()) {
      news.setProperty(SyncUtils.GOOGLE_MARKED_READ, true);
      news.removeProperty(SyncUtils.GOOGLE_MARKED_UNREAD);
    }

    /* Mark Unread */
    if (isMarkedUnread()) {
      news.setProperty(SyncUtils.GOOGLE_MARKED_UNREAD, true);
      news.removeProperty(SyncUtils.GOOGLE_MARKED_READ);
    }

    /* Set Starred */
    if (isStarred())
      news.setFlagged(true);

    /* Set Unstarred */
    if (isUnStarred())
      news.setFlagged(false);

    /* Update Labels */
    if (fAddedLabels != null || fRemovedLabels != null) {
      Object labelsObj = news.getProperty(SyncUtils.GOOGLE_LABELS);
      if (labelsObj == null)
        labelsObj = new String[0];

      if (labelsObj instanceof String[]) {
        Set<String> labels = new HashSet<String>(Arrays.asList((String[]) labelsObj));
        if (fAddedLabels != null)
          labels.addAll(fAddedLabels);
        if (fRemovedLabels != null)
          labels.removeAll(fRemovedLabels);

        news.setProperty(SyncUtils.GOOGLE_LABELS, labels.toArray(new String[labels.size()]));
      }
    }
  }

  /**
   * @param item the other {@link SyncItem} to check for equivalence.
   * @return <code>true</code> in case the provided {@link SyncItem} has the
   * identical properties as this one and <code>false</code> otherwise.
   */
  public boolean isEquivalent(SyncItem item) {
    if (fMarkedRead != item.fMarkedRead)
      return false;

    if (fMarkedUnread != item.fMarkedUnread)
      return false;

    if (fStarred != item.fStarred)
      return false;

    if (fUnStarred != item.fUnStarred)
      return false;

    if (!isLabelsEquivalent(fAddedLabels, item.fAddedLabels))
      return false;

    if (!isLabelsEquivalent(fRemovedLabels, item.fRemovedLabels))
      return false;

    return true;
  }

  @SuppressWarnings("null")
  private boolean isLabelsEquivalent(List<String> labelsA, List<String> labelsB) {
    if (labelsA == null && labelsB != null)
      return false;

    if (labelsA != null && labelsB == null)
      return false;

    if (labelsA == null && labelsB == null)
      return true;

    return Arrays.equals(labelsA.toArray(), labelsB.toArray());
  }

  /*
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fId == null) ? 0 : fId.hashCode());

    return result;
  }

  /*
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;

    if (obj == null)
      return false;

    if (getClass() != obj.getClass())
      return false;

    SyncItem other = (SyncItem) obj;
    if (fId == null) {
      if (other.fId != null)
        return false;
    } else if (!fId.equals(other.fId))
      return false;

    return true;
  }
}