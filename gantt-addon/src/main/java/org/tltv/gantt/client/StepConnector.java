/*
 * Copyright 2014 Tomi Virtanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tltv.gantt.client;

import java.util.HashSet;
import java.util.Set;

import org.tltv.gantt.StepComponent;
import org.tltv.gantt.client.shared.Step;
import org.tltv.gantt.client.shared.StepState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector between GWT StepWidget and Vaadin StepComponent.
 * 
 * @author Tltv
 * 
 */
@Connect(StepComponent.class)
public class StepConnector extends AbstractHasComponentsConnector {

    private GanttWidget gantt;

    @Override
    protected Widget createWidget() {
        return GWT.create(StepWidget.class);
    }

    @Override
    public StepWidget getWidget() {
        return (StepWidget) super.getWidget();
    }

    @Override
    public StepState getState() {
        return (StepState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        if (!(getParent() instanceof GanttConnector)) {
            return;
        }

        if (gantt == null) {
            gantt = ((GanttConnector) getParent()).getWidget();
        }

        if (stateChangeEvent.hasPropertyChanged("step")) {
            updatePredecessorWidgetReference();// need to be called before
                                               // setStep
            getWidget().setStep(getState().step);
        }
        getWidget().updateWidth();
        if (!getWidget().getElement().hasParentElement()) {
            gantt.addStep(getWidget());
        }

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                getWidget().updatePredecessor();
                GanttConnector ganttConnector = (GanttConnector) getParent();
                for (StepWidget stepWidget : ganttConnector.findRelatedSteps(
                        getState().step, ganttConnector.getChildComponents())) {
                    stepWidget.updatePredecessor();
                }
            }
        });
    }

    private void updatePredecessorWidgetReference() {

        // check predecessor change and update widget reference if
        // needed.
        Step predecessor = getState().step.getPredecessor();
        Step oldPredecessor = null;
        if (getWidget().getPredecessorStepWidget() != null) {
            oldPredecessor = getWidget().getPredecessorStepWidget().getStep();
        }

        if ((predecessor == null && oldPredecessor != null)
                || (predecessor != null && !predecessor.equals(oldPredecessor))) {
            getWidget().setPredecessorStepWidget(
                    ((GanttConnector) getParent()).getStepWidget(predecessor));
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        // nop
    }

    @Override
    public void onConnectorHierarchyChange(
            ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        Set<SubStepWidget> removed = new HashSet<SubStepWidget>();
        // remove old sub-steps
        for (ComponentConnector c : connectorHierarchyChangeEvent
                .getOldChildren()) {
            if (!getChildComponents().contains(c)) {
                SubStepWidget stepWidget = ((SubStepConnector) c).getWidget();
                getWidget().remove(stepWidget);
                removed.add(stepWidget);
            }
        }

        // update new steps with references to gantt widget and locale data
        // provider.
        for (ComponentConnector c : getChildComponents()) {
            SubStepWidget stepWidget = ((SubStepConnector) c).getWidget();
            if (!connectorHierarchyChangeEvent.getOldChildren().contains(c)) {
                stepWidget.setGantt(gantt, gantt.getLocaleDataProvider());
            }
        }
    }
}
