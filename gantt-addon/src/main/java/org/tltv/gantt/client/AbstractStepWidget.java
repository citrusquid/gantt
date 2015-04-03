package org.tltv.gantt.client;

import org.tltv.gantt.client.shared.AbstractStep;
import org.tltv.gantt.client.shared.Step;
import org.tltv.gantt.client.shared.StepState;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;

public class AbstractStepWidget extends ComplexPanel {

    public static final String STYLE_BAR = "bar";
    public static final String STYLE_BAR_LABEL = "bar-label";
    public static final String STYLE_INVALID = "invalid";

    protected DivElement caption;

    protected String extraStyle;
    protected long start = -1;
    protected long end = -1;

    protected AbstractStep step;

    protected GanttWidget gantt;
    protected LocaleDataProvider localeDataProvider;

    @Override
    protected void onDetach() {
        super.onDetach();
    }

    public AbstractStepWidget() {
        DivElement bar = DivElement.as(DOM.createDiv());
        bar.setClassName(STYLE_BAR);
        setElement(bar);

        caption = DivElement.as(DOM.createDiv());
        caption.setClassName(STYLE_BAR_LABEL);
        bar.appendChild(caption);

        // hide by default
        bar.getStyle().setVisibility(Visibility.HIDDEN);
    }

    public void setGantt(GanttWidget gantt,
            LocaleDataProvider localeDataProvider) {
        this.gantt = gantt;
        setLocaleDataProvider(localeDataProvider);
    }

    public void setLocaleDataProvider(LocaleDataProvider localeDataProvider) {
        this.localeDataProvider = localeDataProvider;
    }

    public LocaleDataProvider getLocaleDataProvider() {
        return localeDataProvider;
    }

    /**
     * Set data source for this widget. Called when {@linkplain StepState#state}
     * is changed.
     * 
     * @param step
     */
    public void setStep(AbstractStep step) {
        this.step = step;
        updateBackground();
        updateStyle();
        updateCaption();
    }

    /**
     * Get state object. read-only. Changes to the Step object on client side
     * are not registered to the server side.
     */
    public AbstractStep getStep() {
        return step;
    }

    protected void updateCaption() {
        if (step.getCaptionMode() == Step.CaptionMode.HTML) {
            caption.setInnerHTML(step.getCaption());
        } else {
            caption.setInnerText(step.getCaption());
        }
    }

    protected void updateBackground() {
        getElement().getStyle().setBackgroundColor(step.getBackgroundColor());
    }

    protected void updateStyle() {
        if (!isEmpty(step.getStyleName())) {
            if (!step.getStyleName().equals(extraStyle)) {
                // style name changed. Clear old and add new style.
                if (!isEmpty(extraStyle)) {
                    getElement().removeClassName(extraStyle);
                }
                getElement().addClassName(step.getStyleName());
            }
        } else if (!isEmpty(extraStyle)) {
            getElement().removeClassName(extraStyle);
        }
    }

    protected boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * Updates width of this widget to match the Gantt chart's timeline.
     */
    public void updateWidth() {
        if (gantt == null || !getElement().hasParentElement()) {
            return;
        }

        getElement().getStyle().clearVisibility();

        if (start != step.getStartDate() || end != step.getEndDate()) {

            // sanity check
            if (step.getStartDate() < 0 || step.getEndDate() < 0
                    || step.getEndDate() <= step.getStartDate()) {
                getElement().addClassName(STYLE_INVALID);
            } else {
                long offset = 0;
                if (getLocaleDataProvider() != null) {
                    offset = getLocaleDataProvider().getTimeZoneOffset();
                }
                gantt.updateBarPercentagePosition(step.getStartDate() + offset,
                        step.getEndDate() + offset, getElement());
            }

        }
    }

}