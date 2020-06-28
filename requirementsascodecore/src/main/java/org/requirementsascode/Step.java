package org.requirementsascode;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A step is a part of a use case. The steps define the behavior of the use
 * case.
 *
 * <p>
 * A step is the core class of requirementsascode, providing all the necessary
 * configuration information to the {@link ModelRunner} to cause the system to
 * react to messages.
 *
 * @author b_muth
 */
public abstract class Step extends ModelElement implements Serializable {
	private static final long serialVersionUID = -2926490717985964131L;

	private UseCase useCase;
	private Actor[] actors;
	private Condition condition;
	private Class<?> messageClass;
	private SystemReaction<?> systemReaction;
	private Actor publishTo;

	/**
	 * Creates a step with the specified name that belongs to the specified use
	 * case.
	 *
	 * @param useCase   the use case this step belongs to
	 * @param stepName  the name of the step to be created
	 * @param condition
	 */
	Step(String stepName, UseCase useCase, Condition condition) {
		super(stepName, useCase.getModel());
		this.useCase = useCase;
		this.condition = condition;
	}

	public abstract Predicate<ModelRunner> getPredicate();

	public UseCase getUseCase() {
		return useCase;
	}

	public Optional<Condition> getCondition() {
		return Optional.ofNullable(condition);
	}

	public Actor[] getActors() {
		return actors;
	}

	public void setActors(Actor[] actors) {
		connectActorsToThisStep(this, actors);
	}
	
	private void connectActorsToThisStep(Step useCaseStep, Actor[] actors) {
		this.actors = actors;
		for (Actor actor : actors) {
			actor.connectToStep(useCaseStep);
		}
	}

	public Class<?> getMessageClass() {
		return messageClass;
	}

	public void setMessageClass(Class<?> eventClass) {
		this.messageClass = eventClass;
	}

	public SystemReaction<?> getSystemReaction() {
		return systemReaction;
	}
	
	public void setSystemReaction(Runnable systemReaction) {
		this.systemReaction = new SystemReaction<>(systemReaction);
	}
	
	public <T> void setSystemReaction(Consumer<? super T> systemReaction) {
		this.systemReaction = new SystemReaction<>(systemReaction);
	}
	
	public <T> void setSystemReaction(Function<? super T, ?> systemReaction) {
		this.systemReaction = new SystemReaction<>(systemReaction);
	}

	public <T> void setSystemReaction(Supplier<? super T> systemReaction) {
		this.systemReaction = new SystemReaction<>(systemReaction);
	}

	protected static Predicate<ModelRunner> toPredicate(Condition condition) {
		return modelRunner -> condition.evaluate();
	}

	public Optional<Actor> getPublishTo() {
		return Optional.ofNullable(publishTo);
	}

	public void setPublishTo(Actor recipient) {
		this.publishTo = recipient;
	}
}
