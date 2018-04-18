package org.requirementsascode.predicate;

import java.io.Serializable;
import java.util.Objects;

import org.requirementsascode.Step;
import org.requirementsascode.UseCaseModelRunner;

public class After implements FlowPosition, Serializable {
    private static final long serialVersionUID = -4951912635216926005L;

    private Step step;

    public After(Step step) {
	this.step = step;
    }

    @Override
    public boolean test(UseCaseModelRunner useCaseModelRunner) {
	Step latestStep = useCaseModelRunner.getLatestStep().orElse(null);
	boolean isSystemAtRightStep = Objects.equals(step, latestStep);
	return isSystemAtRightStep;
    }

    public String getStepName() {
	String name = step != null ? step.getName() : "";
	return name;
    }
}
