package org.requirementsascode;

import java.util.function.Consumer;

/**
 * Part used by the {@link ModelBuilder} to build a {@link Model}.
 *
 * @see UseCase
 * @author b_muth
 */
public class UseCasePart {
    private UseCase useCase;
    private ModelBuilder modelBuilder;

    UseCasePart(UseCase useCase, ModelBuilder modelBuilder) {
	this.useCase = useCase;
	this.modelBuilder = modelBuilder;
    }

    /**
     * Start the "happy day scenario" where all is fine and dandy.
     * 
     * @return the flow part to create the steps of the basic flow.
     */
    public FlowPart basicFlow() {
	Flow useCaseFlow = getUseCase().getBasicFlow();
	return new FlowPart(useCaseFlow, this);
    }

    /**
     * Start a flow with the specified name.
     * 
     * @param flowName
     *            the name of the flow.
     * 
     * @return the flow part to create the steps of the flow.
     */
    public FlowPart flow(String flowName) {
	Flow useCaseFlow = getUseCase().newFlow(flowName);
	return new FlowPart(useCaseFlow, this);
    }

    UseCase getUseCase() {
	return useCase;
    }

    ModelBuilder getModelBuilder() {
	return modelBuilder;
    }

    /**
     * Returns the model that has been built.
     * 
     * @return the model
     */
    public Model build() {
	return modelBuilder.build();
    }

    public <T> FlowlessUserPart<T> on(Class<T> eventOrExceptionClass) {
	WhenPart whenPart = when(null);
	FlowlessUserPart<T> flowlessUserPart = whenPart.on(eventOrExceptionClass);
	return flowlessUserPart;
    }

    public WhenPart when(Condition whenCondition) {
	WhenPart whenPart = new WhenPart(whenCondition, 1);
	return whenPart;
    }

    public class WhenPart {
	private long flowlessStepCounter;
	private Condition optionalWhenCondition;

	private WhenPart(Condition optionalWhenCondition, long flowlessStepCounter) {
	    this.optionalWhenCondition = optionalWhenCondition;
	    this.flowlessStepCounter = flowlessStepCounter;
	}

	public <T> FlowlessUserPart<T> on(Class<T> eventOrExceptionClass) {
	    FlowlessUserPart<T> flowless = new FlowlessUserPart<>(optionalWhenCondition, eventOrExceptionClass, flowlessStepCounter);
	    return flowless;
	}

	public <T> FlowlessSystemPart<ModelRunner> system(Consumer<ModelRunner> systemReaction) {
	    return on(ModelRunner.class).system(systemReaction);
	}
    }

    public class FlowlessUserPart<T> {
	private StepUserPart<T> userPart;
	private long flowlessStepCounter;

	private FlowlessUserPart(Condition optionalWhenCondition, Class<T> eventOrExceptionClass,
		long flowlessStepCounter) {
	    this.flowlessStepCounter = flowlessStepCounter;
	    StepPart stepPart = createStepPart(optionalWhenCondition, eventOrExceptionClass, "S" + flowlessStepCounter);
	    this.userPart = stepPart.on(eventOrExceptionClass);
	}

	private StepPart createStepPart(Condition optionalWhenCondition, Class<T> eventOrExceptionClass,
		String stepName) {
	    FlowlessStep newStep = useCase.newFlowlessStep(stepName);
	    newStep.setWhen(optionalWhenCondition);
	    StepPart stepPart = new StepPart(newStep, UseCasePart.this, null);
	    return stepPart;
	}

	public FlowlessSystemPart<T> system(Consumer<T> systemReaction) {
	    userPart.system(systemReaction);
	    return new FlowlessSystemPart<>(flowlessStepCounter);
	}
    }

    public class FlowlessSystemPart<T> {
	private long flowlessStepCounter;

	private FlowlessSystemPart(long flowlessStepCounter) {
	    this.flowlessStepCounter = flowlessStepCounter;
	}

	public WhenPart when(Condition whenCondition) {
	    WhenPart whenPart = new WhenPart(whenCondition, ++flowlessStepCounter);
	    return whenPart;
	}

	public <U> FlowlessUserPart<U> on(Class<U> eventOrExceptionClass) {
	    FlowlessUserPart<U> flowlessUserPart = when(null).on(eventOrExceptionClass);
	    return flowlessUserPart;
	}

	public Model build() {
	    return UseCasePart.this.build();
	}

    }
}
