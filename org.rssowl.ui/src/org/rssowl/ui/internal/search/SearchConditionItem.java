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

package org.rssowl.ui.internal.search;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.rssowl.core.Owl;
import org.rssowl.core.persist.IEntity;
import org.rssowl.core.persist.IModelFactory;
import org.rssowl.core.persist.INews;
import org.rssowl.core.persist.INews.State;
import org.rssowl.core.persist.ISearchCondition;
import org.rssowl.core.persist.ISearchField;
import org.rssowl.core.persist.ISearchMark;
import org.rssowl.core.persist.ISearchValueType;
import org.rssowl.core.persist.SearchSpecifier;
import org.rssowl.core.persist.dao.DAOService;
import org.rssowl.core.util.CoreUtils;
import org.rssowl.core.util.Pair;
import org.rssowl.core.util.StringUtils;
import org.rssowl.ui.internal.Application;
import org.rssowl.ui.internal.OwlUI;
import org.rssowl.ui.internal.util.JobRunner;
import org.rssowl.ui.internal.util.LayoutUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The <code>SearchConditionItem</code> is an item of the
 * <code>SearchConditionList</code> representing the UI of a
 * <code>ISearchCondition</code>.
 *
 * @author bpasero
 */
public class SearchConditionItem extends Composite {

  /* Composites */
  private Composite fInputFieldContainer;

  /* Model */
  private final ISearchCondition fCondition;
  private final List<Integer> fFieldsToExclude;
  private Object fInputValue;
  private IModelFactory fFactory;
  private DAOService fDaoService;
  private boolean fModified;

  /* Viewer */
  private ComboViewer fFieldViewer;
  private ComboViewer fSpecifierViewer;

  /* Content Provider: Field Combo and Specifier Combo */
  private class ComboContentProvider implements IStructuredContentProvider {
    @Override
    public Object[] getElements(Object input) {

      /* Create all supported Fields */
      if (input instanceof ISearchCondition)
        return createFields(((ISearchCondition) input)).toArray();

      /* Create all supported specifiers */
      if (input instanceof ISearchField)
        return createSpecifier(((ISearchField) input)).toArray();

      return null;
    }

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
  }

  /* LabelProvider: Field Combo and Specifier Combo */
  private static class ComboLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
      if (element instanceof ISearchField)
        return ((ISearchField) element).getName();

      if (element instanceof SearchSpecifier)
        return ((SearchSpecifier) element).getName();

      return null;
    }
  }

  /* List of Search Warnings */
  private enum SearchWarning {
    NO_WARNING,
    PHRASE_AND_WILDCARD_SEARCH_COMBINED,
    PHRASE_SEARCH_UNSUPPORTED,
    WILDCARD_AND_SPECIAL_CHAR_SEARCH
  }

  /**
   * @param parent The parent Composite.
   * @param style The Style as defined by SWT constants.
   * @param condition The condition this Item is showing.
   * @param fieldsToExclude A list of search fields to exclude from the UI.
   */
  public SearchConditionItem(Composite parent, int style, ISearchCondition condition, List<Integer> fieldsToExclude) {
    super(parent, style);
    fCondition = condition;
    fFieldsToExclude = fieldsToExclude;
    fFactory = Owl.getModelFactory();
    fDaoService = Owl.getPersistenceService().getDAOService();

    initComponents();
  }

  boolean isModified() {
    return fModified;
  }

  boolean hasValue() {
    return fInputValue != null && !"".equals(fInputValue); //$NON-NLS-1$
  }

  void focusInput() {
    Control[] children = fInputFieldContainer.getChildren();
    if (children.length > 0)
      children[0].setFocus();
  }

  ISearchCondition createCondition(ISearchMark searchmark, boolean filterEmpty) {

    /* Filter Bogus Conditions if requiered */
    if (filterEmpty && (fInputValue == null || "".equals(fInputValue))) //$NON-NLS-1$
      return null;

    ISearchField field = (ISearchField) ((IStructuredSelection) fFieldViewer.getSelection()).getFirstElement();
    SearchSpecifier specifier = (SearchSpecifier) ((IStructuredSelection) fSpecifierViewer.getSelection()).getFirstElement();

    if (searchmark != null)
      return fFactory.createSearchCondition(null, searchmark, field, specifier, fInputValue != null ? fInputValue : ""); //$NON-NLS-1$

    return fFactory.createSearchCondition(field, specifier, fInputValue != null ? fInputValue : ""); //$NON-NLS-1$
  }

  private void initComponents() {
    setLayout(LayoutUtils.createGridLayout(3, 5, 5));
    ((GridLayout) getLayout()).horizontalSpacing = 10;

    /* 1.) Create Field Combo */
    createFieldCombo();

    /* 2.) Create Specifier Combo */
    createSpecifierCombo();

    /* 3.) Create Input Field */
    createInputField();
  }

  private void createFieldCombo() {
    Combo fieldCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
    fieldCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
    fieldCombo.setVisibleItemCount(100);

    fFieldViewer = new ComboViewer(fieldCombo);
    fFieldViewer.setContentProvider(new ComboContentProvider());
    fFieldViewer.setLabelProvider(new ComboLabelProvider());
    fFieldViewer.setInput(fCondition);

    /* Select the Condition's Field */
    fFieldViewer.setSelection(new StructuredSelection(fCondition.getField()));

    /* Listen to Changes */
    fFieldViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        fModified = true;
      }
    });
  }

  private void createSpecifierCombo() {
    final Combo specifierCombo = new Combo(this, SWT.BORDER | SWT.READ_ONLY);
    specifierCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, true));
    ((GridData) specifierCombo.getLayoutData()).widthHint = Application.IS_WINDOWS ? 90 : 160;
    specifierCombo.setVisibleItemCount(100);

    fSpecifierViewer = new ComboViewer(specifierCombo);
    fSpecifierViewer.setContentProvider(new ComboContentProvider());
    fSpecifierViewer.setLabelProvider(new ComboLabelProvider());
    fSpecifierViewer.setInput(fCondition.getField());

    /* Select the Condition's Specifier */
    if (fCondition.getSpecifier() != null)
      fSpecifierViewer.setSelection(new StructuredSelection(fCondition.getSpecifier()));

    /* Make sure to at least select the first item */
    if (fSpecifierViewer.getSelection().isEmpty())
      fSpecifierViewer.getCombo().select(0);

    specifierCombo.setToolTipText(getSpecifierTooltip((IStructuredSelection) fSpecifierViewer.getSelection()));

    /* Listen to Selection Changes in the Field-Viewer */
    fFieldViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {

          /* Remember old Selection */
          ISelection oldSelection = fSpecifierViewer.getSelection();

          /* Set Field as Input */
          ISearchField field = (ISearchField) selection.getFirstElement();
          fSpecifierViewer.setInput(field);

          /* Try keeping the selection */
          fSpecifierViewer.setSelection(oldSelection);
          if (fSpecifierViewer.getCombo().getSelectionIndex() == -1)
            selectFirstItem(fSpecifierViewer);
        }
      }
    });

    /* Listen to Changes */
    fSpecifierViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        fModified = true;
        specifierCombo.setToolTipText(getSpecifierTooltip((IStructuredSelection) event.getSelection()));
      }
    });
  }

  private String getSpecifierTooltip(IStructuredSelection selection) {
    Object element = selection.getFirstElement();
    if (element instanceof SearchSpecifier) {
      SearchSpecifier specifier = (SearchSpecifier) element;
      if (specifier == SearchSpecifier.CONTAINS)
        return Messages.SearchConditionItem_CONTAINS_ANY;
    }

    return null;
  }

  private void createInputField() {
    fInputFieldContainer = new Composite(this, SWT.None);
    fInputFieldContainer.setLayout(LayoutUtils.createFillLayout(true, 0, 0));
    fInputFieldContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
    ((GridData) fInputFieldContainer.getLayoutData()).widthHint = 220;

    updateInputField(fInputFieldContainer, fCondition.getField(), fCondition.getValue());

    /* Listen to Selection Changes in the Field-Viewer */
    fFieldViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          ISearchField field = (ISearchField) selection.getFirstElement();
          updateInputField(fInputFieldContainer, field, null);
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void updateInputField(Composite inputField, final ISearchField field, final Object input) {

    /* Dispose any old Children first */
    Control[] children = inputField.getChildren();
    for (Control child : children) {
      child.dispose();
    }

    /* Specially treat News-State */
    if (field.getId() == INews.STATE) {
      final StateConditionControl stateConditionControl = new StateConditionControl(inputField, SWT.NONE);
      stateConditionControl.addListener(SWT.Modify, new Listener() {
        @Override
        public void handleEvent(Event event) {
          fInputValue = stateConditionControl.getSelection();

          if (fInputValue == null && input != null || (fInputValue != null && !fInputValue.equals(input)))
            fModified = true;
        }
      });

      /* Pre-Select input if given */
      Object presetInput = (input == null) ? fInputValue : input;
      if (presetInput != null && presetInput instanceof EnumSet)
        stateConditionControl.select((EnumSet<State>) presetInput);

      /* Update Input Value */
      fInputValue = stateConditionControl.getSelection();
    }

    /* Specially treat News-Location */
    else if (field.getId() == INews.LOCATION) {
      final LocationControl locationConditionControl = new LocationControl(inputField, SWT.NONE);
      locationConditionControl.addListener(SWT.Modify, new Listener() {
        @Override
        public void handleEvent(Event event) {
          fInputValue = locationConditionControl.getSelection();

          if (fInputValue == null && input != null || (fInputValue != null && !fInputValue.equals(input)))
            fModified = true;
        }
      });

      /* Pre-Select input if given */
      Object presetInput = (input == null) ? fInputValue : input;
      if (presetInput != null && presetInput instanceof Long[][])
        locationConditionControl.select((Long[][]) presetInput);

      /* Update Input Value */
      fInputValue = locationConditionControl.getSelection();
    }

    /* Specially treat Age */
    else if (field.getId() == INews.AGE_IN_DAYS || field.getId() == INews.AGE_IN_MINUTES) {
      Composite container = new Composite(inputField, SWT.NONE);
      container.setLayout(LayoutUtils.createGridLayout(2, 0, 0));

      final Spinner spinner = new Spinner(container, SWT.BORDER);
      spinner.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      spinner.setMinimum(1);
      spinner.setMaximum(1000000);

      final Combo combo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      combo.add(Messages.SearchConditionItem_DAYS);
      combo.add(Messages.SearchConditionItem_HOURS);
      combo.add(Messages.SearchConditionItem_MINUTES);

      Listener listener = new Listener() {
        @Override
        public void handleEvent(Event event) {
          fInputValue = getAgeValue(spinner, combo);

          if (!fInputValue.equals(input))
            fModified = true;
        }
      };
      spinner.addListener(SWT.Modify, listener);
      combo.addListener(SWT.Modify, listener);

      /* Pre-Select input if given */
      Object presetInput = (input == null) ? fInputValue : input;
      if (presetInput != null && presetInput instanceof Integer) {
        Integer inputValue = (Integer) presetInput;

        /* Day */
        if (inputValue >= 0) {
          spinner.setSelection(inputValue);
          combo.select(0);
        }

        /* Hour */
        else if (inputValue % 60 == 0) {
          spinner.setSelection(Math.abs(inputValue) / 60);
          combo.select(1);
        }

        /* Minute */
        else {
          spinner.setSelection(Math.abs(inputValue));
          combo.select(2);
        }
      }

      /* Otherwise use Default */
      else {
        spinner.setSelection(1);
        combo.select(0);
      }

      /* Update Input Value */
      fInputValue = getAgeValue(spinner, combo);
    }

    /* Create new Input Field based on search-value-type */
    else {
      switch (field.getSearchValueType().getId()) {

        /* Type: Boolean */
        case ISearchValueType.BOOLEAN: {
          final Combo combo = new Combo(inputField, SWT.BORDER | SWT.READ_ONLY);
          combo.add(Messages.SearchConditionItem_TRUE);
          combo.add(Messages.SearchConditionItem_FALSE);
          combo.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
              fInputValue = Boolean.valueOf(combo.getItem(combo.getSelectionIndex()));

              if (!fInputValue.equals(input))
                fModified = true;
            }
          });

          /* Pre-Select input if given */
          Object presetInput = (input == null) ? fInputValue : input;
          if (presetInput != null && presetInput instanceof Boolean)
            combo.select(((Boolean) presetInput) ? 0 : 1);
          else
            combo.select(0);

          /* Update Input Value */
          fInputValue = Boolean.valueOf(combo.getItem(combo.getSelectionIndex()));

          break;
        }

          /* Type: Date / Time */
        case ISearchValueType.DATE:
        case ISearchValueType.TIME:
        case ISearchValueType.DATETIME: {
          final Calendar cal = Calendar.getInstance();
          final DateTime datetime = new DateTime(inputField, SWT.DATE | SWT.BORDER);
          datetime.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
              cal.set(Calendar.DATE, datetime.getDay());
              cal.set(Calendar.MONTH, datetime.getMonth());
              cal.set(Calendar.YEAR, datetime.getYear());

              fInputValue = cal.getTime();

              if (!fInputValue.equals(input))
                fModified = true;
            }
          });

          /* Pre-Select input if given */
          Object presetInput = (input == null) ? fInputValue : input;
          if (presetInput != null && presetInput instanceof Date)
            cal.setTime((Date) presetInput);

          datetime.setDay(cal.get(Calendar.DATE));
          datetime.setMonth(cal.get(Calendar.MONTH));
          datetime.setYear(cal.get(Calendar.YEAR));

          /* Update Input Value */
          fInputValue = cal.getTime();

          break;
        }

          /* Type: Enumeration */
        case ISearchValueType.ENUM: {
          final Text text = new Text(inputField, SWT.BORDER);
          text.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
              fInputValue = text.getText();

              if (!fInputValue.equals(input))
                fModified = true;
            }
          });

          /* Provide Auto-Complete Field */
          OwlUI.hookAutoComplete(text, field.getSearchValueType().getEnumValues(), true, true);

          /* Pre-Select input if given */
          String inputValue = (input != null ? input.toString() : null);
          if (inputValue != null)
            text.setText(inputValue);

          /* Update Input Value */
          fInputValue = text.getText();

          break;
        }

          /* Type: Number */
        case ISearchValueType.NUMBER:
        case ISearchValueType.INTEGER: {
          final Spinner spinner = new Spinner(inputField, SWT.BORDER);
          spinner.setMinimum(0);
          spinner.setMaximum(1000);
          spinner.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
              fInputValue = spinner.getSelection();

              if (!fInputValue.equals(input))
                fModified = true;
            }
          });

          /* Pre-Select input if given */
          Object presetInput = (input == null) ? fInputValue : input;
          if (presetInput != null && presetInput instanceof Integer)
            spinner.setSelection((Integer) presetInput);

          /* Update Input Value */
          fInputValue = spinner.getSelection();

          break;
        }

          /* Type: String */
        case ISearchValueType.STRING:
        case ISearchValueType.LINK: {
          final Text text = new Text(inputField, SWT.BORDER);
          OwlUI.makeAccessible(text, NLS.bind(Messages.SearchConditionItem_SEARCH_VALUE_FIELD, field.getName()));

          /* Show UI Hint for extra information is available */
          final ControlDecoration controlDeco = new ControlDecoration(text, SWT.LEFT | SWT.TOP);
          controlDeco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());
          controlDeco.setShowOnlyOnFocus(true);

          /* Listen to Changes of Input */
          text.addListener(SWT.Modify, new Listener() {
            private boolean isShowingWarning = false;

            @Override
            public void handleEvent(Event event) {
              String textValue = text.getText();
              fInputValue = textValue;

              if (!fInputValue.equals(input))
                fModified = true;

              if (isShowingWarning)
                controlDeco.hideHover();

              /* Determine any Search Warning to show depending on field and text value */
              SearchWarning warning = SearchWarning.NO_WARNING;
              if (field.getId() == INews.CATEGORIES || field.getId() == INews.SOURCE || field.getId() == INews.FEED || field.getId() == INews.LINK) {
                if (StringUtils.isPhraseSearch(textValue))
                  warning = SearchWarning.PHRASE_SEARCH_UNSUPPORTED;
              } else {
                if (StringUtils.isPhraseSearchWithWildcardToken(textValue))
                  warning = SearchWarning.PHRASE_AND_WILDCARD_SEARCH_COMBINED;
                else if (StringUtils.isSpecialCharacterSearchWithWildcardToken(textValue))
                  warning = SearchWarning.WILDCARD_AND_SPECIAL_CHAR_SEARCH;
              }

              /* Indicate a warning to the user if phrase search and wildcards combined or wrongly used */
              if (warning != SearchWarning.NO_WARNING && !isShowingWarning) {
                updateFieldDecoration(text, controlDeco, warning, field);
                isShowingWarning = true;
              }

              /* Clear any error if shown previously */
              else if (warning == SearchWarning.NO_WARNING && isShowingWarning) {
                updateFieldDecoration(text, controlDeco, warning, field);
                isShowingWarning = false;
              }
            }
          });

          /* Provide auto-complete for Categories, Authors and Feeds */
          if (field.getId() == INews.CATEGORIES || field.getId() == INews.AUTHOR || field.getId() == INews.FEED) {
            controlDeco.setDescriptionText(Messages.SearchConditionItem_CONTENT_ASSIST_INFO);
            final Pair<SimpleContentProposalProvider, ContentProposalAdapter> pair = OwlUI.hookAutoComplete(text, null, false, true);

            /* Load proposals in the Background */
            JobRunner.runInBackgroundThread(100, new Runnable() {
              @Override
              public void run() {
                if (!text.isDisposed()) {
                  Set<String> values = new TreeSet<String>(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                      return o1.compareToIgnoreCase(o2);
                    }
                  });

                  if (field.getId() == INews.CATEGORIES)
                    values.addAll(fDaoService.getCategoryDAO().loadAllNames());
                  else if (field.getId() == INews.AUTHOR)
                    values.addAll(fDaoService.getPersonDAO().loadAllNames());
                  else if (field.getId() == INews.FEED)
                    values.addAll(CoreUtils.getFeedLinks());

                  /* Apply Proposals */
                  if (!text.isDisposed())
                    OwlUI.applyAutoCompleteProposals(values, pair.getFirst(), pair.getSecond(), true);
                }
              }
            });
          }

          /* Show UI Hint that Wildcards can be used */
          else {
            controlDeco.setDescriptionText(Messages.SearchConditionItem_SEARCH_HELP);
          }

          /* Pre-Select input if given */
          Object presetInput = (input == null && fInputValue instanceof String) ? fInputValue : input;
          if (presetInput != null)
            text.setText(presetInput.toString());

          /* Update Input Value */
          fInputValue = text.getText();

          break;
        }
      }
    }

    /* Update Layout */
    inputField.getParent().layout();
    inputField.getParent().update();
    inputField.layout();
    inputField.update();
  }

  private void updateFieldDecoration(Text text, ControlDecoration deco, SearchWarning warning, ISearchField field) {

    /* Show Warning in Control Decoration about wrong search pattern use */
    if (warning != SearchWarning.NO_WARNING) {
      String decoText;
      if (warning == SearchWarning.PHRASE_SEARCH_UNSUPPORTED)
        decoText = Messages.SearchConditionItem_WARNING_PHRASE_SEARCH_UNSUPPORTED;
      else if (warning == SearchWarning.PHRASE_AND_WILDCARD_SEARCH_COMBINED)
        decoText = Messages.SearchConditionItem_ERROR_PHRASE_AND_WILDCARD_SEARCH;
      else
        decoText = Messages.SearchConditionItem_WARNING_WILDCARD_SPECIAL_CHAR_SEARCH;

      deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_WARNING).getImage());
      deco.setDescriptionText(decoText);
      if (text.isFocusControl()) //Otherwise hover shows offscreen from some dialogs
        deco.showHoverText(decoText);
    }

    /* Restore Default Control Decoration */
    else {
      deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());
      if (field.getId() == INews.CATEGORIES || field.getId() == INews.AUTHOR || field.getId() == INews.FEED)
        deco.setDescriptionText(Messages.SearchConditionItem_CONTENT_ASSIST_INFO);
      else
        deco.setDescriptionText(Messages.SearchConditionItem_SEARCH_HELP);
    }
  }

  private int getAgeValue(Spinner valueSpinner, Combo scopeCombo) {
    int value = valueSpinner.getSelection();

    /* Day Value (positive int) */
    if (scopeCombo.getSelectionIndex() == 0)
      return value;

    /* Hour Value (negative int) */
    if (scopeCombo.getSelectionIndex() == 1)
      return value * 60 * -1;

    /* Minute Value (negative int) */
    return value * -1;
  }

  private List<ISearchField> createFields(ISearchCondition condition) {
    List<ISearchField> fields = new ArrayList<ISearchField>();
    String entityName = condition.getField().getEntityName();

    /* Return all Fields of News */
    if (INews.class.getName().equals(entityName)) {
      List<Integer> newsFields = new ArrayList<Integer>();
      newsFields.add(IEntity.ALL_FIELDS);
      newsFields.add(INews.STATE);
      newsFields.add(INews.LOCATION);
      newsFields.add(INews.TITLE);
      newsFields.add(INews.DESCRIPTION);
      newsFields.add(INews.AUTHOR);
      newsFields.add(INews.CATEGORIES);
      newsFields.add(INews.AGE_IN_DAYS);
      newsFields.add(INews.PUBLISH_DATE);
      newsFields.add(INews.MODIFIED_DATE);
      newsFields.add(INews.RECEIVE_DATE);
      newsFields.add(INews.HAS_ATTACHMENTS);
      newsFields.add(INews.ATTACHMENTS_CONTENT);
      newsFields.add(INews.SOURCE);
      newsFields.add(INews.LINK);
      newsFields.add(INews.IS_FLAGGED);
      newsFields.add(INews.LABEL);
      //newsFields.add(INews.RATING);
      newsFields.add(INews.FEED);

      if (fFieldsToExclude != null)
        newsFields.removeAll(fFieldsToExclude);

      for (Integer newsField : newsFields) {
        fields.add(fFactory.createSearchField(newsField, entityName));
      }
    }

    return fields;
  }

  private List<SearchSpecifier> createSpecifier(ISearchField field) {
    List<SearchSpecifier> specifiers = new ArrayList<SearchSpecifier>();
    String entityName = field.getEntityName();

    /* Return all Specifiers for News-Fields */
    if (INews.class.getName().equals(entityName)) {
      int fieldId = field.getId();
      int typeId = field.getSearchValueType().getId();

      /* Is / Is Not */
      if (fieldId != IEntity.ALL_FIELDS && fieldId != INews.TITLE && fieldId != INews.DESCRIPTION && fieldId != INews.ATTACHMENTS_CONTENT && fieldId != INews.AUTHOR) {
        specifiers.add(SearchSpecifier.IS);

        if (fieldId != INews.AGE_IN_DAYS)
          specifiers.add(SearchSpecifier.IS_NOT);
      }

      /* Contains / Contains Not */
      else {
        specifiers.add(SearchSpecifier.CONTAINS_ALL);
        specifiers.add(SearchSpecifier.CONTAINS);
        specifiers.add(SearchSpecifier.CONTAINS_NOT);
      }

      /* Begins With / Ends With */
      if (fieldId == INews.LINK || fieldId == INews.SOURCE || fieldId == INews.LABEL || fieldId == INews.CATEGORIES || fieldId == INews.FEED) {
        specifiers.add(SearchSpecifier.BEGINS_WITH);
        specifiers.add(SearchSpecifier.ENDS_WITH);
      }

      /* Is Before / Is After */
      if (typeId == ISearchValueType.DATE || typeId == ISearchValueType.TIME || typeId == ISearchValueType.DATETIME) {
        specifiers.add(SearchSpecifier.IS_BEFORE);
        specifiers.add(SearchSpecifier.IS_AFTER);
      }

      /* Is Greather Than / Is Less Than */
      if (typeId == ISearchValueType.NUMBER || typeId == ISearchValueType.INTEGER) {
        specifiers.add(SearchSpecifier.IS_GREATER_THAN);
        specifiers.add(SearchSpecifier.IS_LESS_THAN);
      }

      /* Is Similiar To */
      if (fieldId == INews.TITLE || fieldId == INews.AUTHOR || fieldId == INews.CATEGORIES)
        specifiers.add(SearchSpecifier.SIMILIAR_TO);
    }

    return specifiers;
  }

  private void selectFirstItem(ComboViewer viewer) {
    viewer.getCombo().select(0);
    viewer.setSelection(viewer.getSelection());
  }
}