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

package org.rssowl.ui.internal.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.ILabel;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.dao.DynamicDAO;
import org.rssowl.core.persist.dao.ICategoryDAO;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Controller;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.ContentAssistAdapter;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Dialog to assign Labels to a selection of News.
 *
 * @author bpasero
 */
public class AssignLabelsDialog extends Dialog {
  private Text fLabelsInput;
  private final Set<INews> fNews;
  private Set<ILabel> fExistingLabels;
  private ResourceManager fResources = new LocalResourceManager(JFaceResources.getResources());
  private HashSet<String> fExistingLabelNames;
  private Label fInfoImg;
  private Label fInfoText;

  /**
   * @param parentShell
   * @param news
   */
  public AssignLabelsDialog(Shell parentShell, Set<INews> news) {
    super(parentShell);
    fNews = news;
    fExistingLabels = CoreUtils.loadSortedLabels();
    fExistingLabelNames = new HashSet<String>(fExistingLabels.size());
    for (ILabel label : fExistingLabels) {
      fExistingLabelNames.add(label.getName().toLowerCase());
    }
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed() {
    String labelsValue = fLabelsInput.getText();
    String[] labelsValueSplit = labelsValue.split(","); //$NON-NLS-1$

    /* Remove All Labels first */
    for (INews news : fNews) {
      Set<ILabel> newsLabels = news.getLabels();
      for (ILabel newsLabel : newsLabels) {
        news.removeLabel(newsLabel);
      }
    }

    /* Assign New Labels */
    if (labelsValueSplit.length > 0) {

      /* For each typed Label */
      for (String labelValue : labelsValueSplit) {
        ILabel label = null;
        labelValue = labelValue.trim();
        if (labelValue.length() == 0)
          continue;

        /* Check if Label exists */
        for (ILabel existingLabel : fExistingLabels) {
          if (existingLabel.getName().toLowerCase().equals(labelValue.toLowerCase())) {
            label = existingLabel;
            break;
          }
        }

        /* Create new Label if necessary */
        if (label == null) {
          ILabel newLabel = Owl.getModelFactory().createLabel(null, labelValue);
          newLabel.setColor(OwlUI.toString(new RGB(0, 0, 0)));
          newLabel.setOrder(fExistingLabels.size());
          DynamicDAO.save(newLabel);
          fExistingLabels.add(newLabel);
          label = newLabel;
        }

        /* Add Label to all News */
        for (INews news : fNews) {
          news.addLabel(label);
        }
      }
    }

    /* Mark Saved Search Service as in need for a quick Update */
    Controller.getDefault().getSavedSearchService().forceQuickUpdate();

    /* Save News */
    DynamicDAO.saveAll(fNews);

    super.okPressed();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#close()
   */
  @Override
  public boolean close() {
    fResources.dispose();
    return super.close();
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(LayoutUtils.createGridLayout(2, 10, 10, 5, 6, false));
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    /* Name */
    Label nameLabel = new Label(composite, SWT.NONE);
    nameLabel.setText(Messages.AssignLabelsDialog_LABELS);

    fLabelsInput = new Text(composite, SWT.BORDER | SWT.SINGLE);
    fLabelsInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    fLabelsInput.setText(getLabelsValue());
    fLabelsInput.setSelection(fLabelsInput.getText().length());
    fLabelsInput.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        onModifyName();
      }
    });

    /* Add auto-complete for Labels taken from existing Categories */
    ContentAssistAdapter adapter = new ContentAssistAdapter(fLabelsInput, ',', true);

    /* Labels */
    final List<String> labelNames = new ArrayList<String>(fExistingLabels.size());
    for (ILabel label : fExistingLabels) {
      labelNames.add(label.getName());
    }

    final Pair<SimpleContentProposalProvider, ContentProposalAdapter> pair = OwlUI.hookAutoComplete(fLabelsInput, adapter, labelNames, true, false);
    pair.getSecond().setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_INSERT);

    /* Load proposals in the Background */
    JobRunner.runDelayedInBackgroundThread(new Runnable() {
      @Override
      public void run() {
        if (!fLabelsInput.isDisposed()) {
          Set<String> values = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
              return o1.compareToIgnoreCase(o2);
            }
          });

          /* Categories */
          Set<String> categoryNames = DynamicDAO.getDAO(ICategoryDAO.class).loadAllNames();
          categoryNames = StringUtils.replaceAll(categoryNames, ",", " "); // Comma not allowed for Labels //$NON-NLS-1$ //$NON-NLS-2$
          values.addAll(categoryNames);

          /* Labels */
          values.addAll(labelNames);

          /* Apply Proposals */
          if (!fLabelsInput.isDisposed())
            OwlUI.applyAutoCompleteProposals(values, pair.getFirst(), pair.getSecond(), false);
        }
      }
    });

    /* Info Container */
    Composite infoContainer = new Composite(composite, SWT.None);
    infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    infoContainer.setLayout(LayoutUtils.createGridLayout(2, 0, 0));
    ((GridLayout) infoContainer.getLayout()).marginTop = 15;

    fInfoImg = new Label(infoContainer, SWT.NONE);
    fInfoImg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

    fInfoText = new Label(infoContainer, SWT.WRAP);
    fInfoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    showInfo();

    applyDialogFont(composite);

    return composite;
  }

  private String getLabelsValue() {

    /* Sort by Sort Key to respect order */
    Set<ILabel> labels = new TreeSet<ILabel>(new Comparator<ILabel>() {
      @Override
      public int compare(ILabel l1, ILabel l2) {
        if (l1.equals(l2))
          return 0;

        return l1.getOrder() < l2.getOrder() ? -1 : 1;
      }
    });

    for (INews news : fNews) {
      Set<ILabel> newsLabels = news.getLabels();
      labels.addAll(newsLabels);
    }

    StringBuilder str = new StringBuilder();
    for (ILabel label : labels) {
      str.append(label.getName()).append(", "); //$NON-NLS-1$
    }

    return str.toString();
  }

  private void showWarning(String msg) {
    fInfoText.setText(msg);
    fInfoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/warning.gif")); //$NON-NLS-1$
    fInfoImg.getParent().layout();
  }

  private void showInfo() {
    fInfoText.setText(Messages.AssignLabelsDialog_SEPARATE_LABELS);
    fInfoImg.setImage(OwlUI.getImage(fResources, "icons/obj16/info.gif")); //$NON-NLS-1$
    fInfoImg.getParent().layout();
  }

  private void onModifyName() {
    int newLabelCounter = 0;
    String labelsValue = fLabelsInput.getText();
    String[] labelsValueSplit = labelsValue.split(","); //$NON-NLS-1$
    Set<String> handledNewLabels = new HashSet<String>(1);
    for (String labelValue : labelsValueSplit) {
      labelValue = labelValue.trim().toLowerCase();
      if (labelValue.length() > 0 && !handledNewLabels.contains(labelValue) && !fExistingLabelNames.contains(labelValue)) {
        newLabelCounter++;
        handledNewLabels.add(labelValue.toLowerCase());
      }
    }

    if (newLabelCounter == 0)
      showInfo();
    else if (newLabelCounter == 1)
      showWarning(Messages.AssignLabelsDialog_NEW_LABEL_CREATE);
    else
      showWarning(NLS.bind(Messages.AssignLabelsDialog_N_LABEL_CREATE, newLabelCounter));
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createButtonBar(Composite parent) {

    /* Spacer */
    new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

    return super.createButtonBar(parent);
  }

  /*
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.AssignLabelsDialog_ASSIGN_LABELS);
  }

  /*
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  @Override
  protected void initializeBounds() {
    super.initializeBounds();

    Shell shell = getShell();

    /* Minimum Size */
    int minWidth = convertHorizontalDLUsToPixels(OwlUI.MIN_DIALOG_WIDTH_DLU);
    int minHeight = shell.computeSize(minWidth, SWT.DEFAULT).y;

    /* Required Size */
    Point requiredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    shell.setSize(Math.max(minWidth, requiredSize.x), Math.max(minHeight, requiredSize.y));
    LayoutUtils.positionShell(shell);
  }
}