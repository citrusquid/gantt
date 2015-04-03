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

import org.tltv.gantt.client.ArrowElement.ArrowChangeHandler;
import org.tltv.gantt.client.shared.GanttUtil;
import org.tltv.gantt.client.shared.Step;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget representing a one Step in the Gantt chart.
 * 
 * @author Tltv
 * 
 */
public class StepWidget extends AbstractStepWidget {

    private StepWidget predecessorStepWidget;

    private ArrowElement predecessorArrow;

    private ArrowChangeHandler arrowChangeHandler = new ArrowChangeHandler() {

        @Override
        public boolean onArrowChanged(boolean startingPointChanged,
                NativeEvent event) {
            Element target = GanttUtil.getElementFromPoint(
                    GanttUtil.getTouchOrMouseClientX(event),
                    GanttUtil.getTouchOrMouseClientY(event));
            if (target != null) {
                return gantt.getRpc().onStepRelationSelected(StepWidget.this,
                        startingPointChanged, target);
            }
            return false;
        }
    };

    @Override
    protected void onDetach() {
        if (gantt != null && predecessorArrow != null) {
            gantt.unregisterContentElement((Widget) predecessorArrow);
        }
        super.onDetach();
    }

    public StepWidget() {
        super();
    }

    @Override
    public Step getStep() {
        return (Step) super.getStep();
    }

    public StepWidget getPredecessorStepWidget() {
        return predecessorStepWidget;
    }

    public void setPredecessorStepWidget(StepWidget predecessorStepWidget) {
        this.predecessorStepWidget = predecessorStepWidget;
    }

    public void updatePredecessor() {
        createPredecessorElements();

        if (predecessorStepWidget == null) {
            return;
        }

        ArrowPositionData data = new ArrowPositionData(
                getPredecessorStepWidget().getElement(), getElement());

        predecessorArrow.setWidth(data.getWidth());
        predecessorArrow.setHeight(data.getHeight());
        predecessorArrow.setTop((int) data.getTop());
        predecessorArrow.setLeft((int) data.getLeft());

        predecessorArrow.draw(data);
    }

    public ArrowElement createArrowWidget() {
        return new SvgArrowWidget();
    }

    protected void createPredecessorElements() {
        if (predecessorStepWidget == null) {
            if (predecessorArrow != null) {
                gantt.unregisterContentElement((Widget) predecessorArrow);
            }
        } else {
            if (predecessorArrow == null) {
                predecessorArrow = createArrowWidget();
                predecessorArrow.setUpEventHandlers(gantt.isTouchSupported(),
                        gantt.isMsTouchSupported());
                predecessorArrow.setArrowChangeHandler(arrowChangeHandler);
            }
            gantt.registerContentElement((Widget) predecessorArrow);
        }
    }

    public Widget getPredecessorArrowWidget() {
        return (Widget) predecessorArrow;
    }

}
