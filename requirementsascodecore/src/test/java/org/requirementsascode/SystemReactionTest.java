package org.requirementsascode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.event.EnterNumberEvent;
import org.requirementsascode.event.EnterTextEvent;

public class SystemReactionTest extends AbstractTestCase{
	private Actor rightActor;
	private Actor secondActor;
	private Actor actorWithDisabledStep;
	
	private static final String SAY_HELLO_USE_CASE = "Say Hello Use Case";
	private static final String ANOTHER_USE_CASE = "Another Use Case";

	private static final String ALTERNATIVE_FLOW = "Alternative Flow";
	private static final String SYSTEM_DISPLAYS_TEXT = "System displays text";
	private static final String SYSTEM_DISPLAYS_TEXT_AGAIN = "System displays text again";

	private static final String CUSTOMER_ENTERS_SOME_TEXT = "Customer enters some text";
	private static final String CUSTOMER_ENTERS_SOME_TEXT_AGAIN = "Customer enters some text again";
	private static final String CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT = "Customer enters some different text";
	private static final String CUSTOMER_ENTERS_NUMBER = "Customer enters number";
		
	private static final String THIS_STEP_SHOULD_BE_SKIPPED_AS_WELL = "This step should be skipped as well";
	private static final String THIS_STEP_SHOULD_BE_SKIPPED = "This step should be skipped";
	
	private static final String REPEAT_STEP_POSTFIX = " (#REPEAT)";
	
	@Before
	public void setup() {
		super.setup();
		this.rightActor = useCaseModel.newActor("Right Actor");
		this.secondActor = useCaseModel.newActor("Second Actor");
		this.actorWithDisabledStep = useCaseModel.newActor("Actor With Disabled Step");
	}
	
	@Test
	public void shouldPrintText() {
		useCaseModel
			.newUseCase(SAY_HELLO_USE_CASE)
				.basicFlow()
					.newStep(SYSTEM_DISPLAYS_TEXT).system(displayConstantText());
		
		useCaseRunner.run();
		
		assertEquals(Arrays.asList(SYSTEM_DISPLAYS_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldPrintTextTwice() {
		useCaseModel
			.newUseCase(SAY_HELLO_USE_CASE)
				.basicFlow()
					.newStep(SYSTEM_DISPLAYS_TEXT).system(displayConstantText())
					.newStep(SYSTEM_DISPLAYS_TEXT_AGAIN).system(displayConstantText());
		
		useCaseRunner.run();
		
		assertEquals(Arrays.asList(SYSTEM_DISPLAYS_TEXT, SYSTEM_DISPLAYS_TEXT_AGAIN), getRunStepNames());
	}
	
	@Test
	public void shouldReactToOneStep() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
				
		UseCaseStep latestStepRun = 
				useCaseRunner.run().reactTo(enterTextEvent());
		
		assertEquals(CUSTOMER_ENTERS_SOME_TEXT, latestStepRun.getName());
	}
	
	@Test
	public void shouldNotReactToStepIfRunIsNotCalled() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
				
		useCaseRunner.reactTo(enterTextEvent());
		
		assertEquals(0, getRunStepNames().size());
	}
	
	@Test
	public void shouldReactToTwoSequentialStepsBasedOnSameType() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_TEXT_AGAIN), getRunStepNames());
	}
	
	@Test
	public void shouldReactToTwoSequentialStepsBasedOnDifferentType() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	
	@Test
	public void shouldReactToTwoSequentialStepsOnlyForThoseStepsWhereActorIsRight() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.actor(rightActor).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN)
					.actor(secondActor).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseRunner.run().as(rightActor).reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactToTwoSequentialStepsWhenRunningWithDifferentActors() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.actor(customer).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER)
					.actor(secondActor).handle(EnterNumberEvent.class).system(displayEnteredNumber());
		
		useCaseRunner.run().as(customer);
		
		useCaseRunner.reactTo(enterTextEvent());
		
		useCaseRunner.as(secondActor).reactTo(enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	
	@Test
	public void shouldNotReactToAlreadyRunStep() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactOnlyToEnabledStep() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())			
			.newFlow("Alternative Flow: Skipped").when(r -> false)
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).handle(EnterTextEvent.class).system(throwRuntimeException());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactToEnabledStepEvenIfDisabledStepWouldBePerformedBySystem() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())			
			.newFlow("Alternative Flow: Skipped").when(r -> false)
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).system(() -> {System.out.println("You should not see this!");});
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactOnlyToEnabledStepWithRightActorInSameFlowAtFirstStep() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.actor(rightActor).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN)
					.actor(secondActor).handle(EnterTextEvent.class).system(throwRuntimeException());
		
		useCaseRunner.run().as(rightActor).reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactOnlyToEnabledStepWithRightActorInDifferentFlow() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.actor(secondActor).handle(EnterTextEvent.class).system(throwRuntimeException())			
			.newFlow(ALTERNATIVE_FLOW).when(textIsNotAvailablePredicate())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN)
					.actor(rightActor).handle(EnterTextEvent.class).system(displayEnteredText());
		
		UseCaseStep lastStepRun = useCaseRunner.run().as(rightActor).reactTo(enterTextEvent());
		
		assertEquals(CUSTOMER_ENTERS_SOME_TEXT_AGAIN, lastStepRun.getName());
	}
	
	@Test
	public void shouldNotReactToEnabledStepWithWrongActorInDifferentFlow() { 		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.actor(rightActor).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT)
					.actor(rightActor).handle(EnterTextEvent.class).system(displayEnteredText())			
			.newFlow(ALTERNATIVE_FLOW).after(CUSTOMER_ENTERS_SOME_TEXT)
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED)
					.actor(secondActor).handle(EnterTextEvent.class).system(throwRuntimeException());
		
		useCaseRunner.run().as(rightActor).reactTo(enterTextEvent(), enterTextEvent());
		assertEquals(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT, getLatestStepName());
	}
	
	@Test
	public void shouldNotReactToDisabledStepWithSpecificActor() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.actor(customer).handle(EnterTextEvent.class).system(displayEnteredText())				
			.newFlow(ALTERNATIVE_FLOW).when(r -> false)
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED)
					.actor(actorWithDisabledStep).handle(EnterTextEvent.class).system(displayEnteredText());
				
		UseCaseStep lastStepRun = 
			useCaseRunner.run().as(actorWithDisabledStep).reactTo(enterTextEvent());
		
		assertNull(lastStepRun);
	}
	
	@Test
	public void shouldReactToFirstStepAlternativeWhen() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).handle(EnterTextEvent.class).system(throwRuntimeException())
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED_AS_WELL).handle(EnterTextEvent.class).system(throwRuntimeException())		
			.newFlow(ALTERNATIVE_FLOW).when(textIsNotAvailablePredicate())
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseRunner.run().reactTo(enterTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactToSecondStepAlternativeWhen() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).handle(EnterTextEvent.class).system(throwRuntimeException())		
			.newFlow(ALTERNATIVE_FLOW).when(r -> CUSTOMER_ENTERS_SOME_TEXT.equals(getLatestStepName()))
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldNotReenterAlternativeFlowEvenIfConditionsIsFulfilled() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).handle(EnterTextEvent.class).system(throwRuntimeException())
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED_AS_WELL).handle(EnterTextEvent.class).system(throwRuntimeException())		
			.newFlow(ALTERNATIVE_FLOW).when(r -> true)
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber());
		
		useCaseRunner.run()
			.reactTo(enterDifferentTextEvent(), enterNumberEvent(), enterDifferentTextEvent(), enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	 
	@Test
	public void shouldReactToAlternativeAfterFirstStep() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).handle(EnterTextEvent.class).system(throwRuntimeException())		
			.newFlow(ALTERNATIVE_FLOW).after(CUSTOMER_ENTERS_SOME_TEXT)
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldReactToAlternativeAtFirstStep() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED).handle(EnterTextEvent.class).system(throwRuntimeException())
				.newStep(THIS_STEP_SHOULD_BE_SKIPPED_AS_WELL).handle(EnterTextEvent.class).system(throwRuntimeException())		
			.newFlow(ALTERNATIVE_FLOW).atFirst()
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		UseCaseStep latestStep = 
			useCaseRunner.run().reactTo(enterTextEvent());
		
		assertEquals(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT, latestStep.getName());
	}
	
	@Test
	public void shouldStartOneOfTwoParallelUseCasesByDifferentEvent() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseModel.newUseCase(ANOTHER_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber());
		
		useCaseRunner.run().reactTo(enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	
	@Test
	public void shouldStartTwoUseCasesSequentially() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText());
		
		useCaseModel.newUseCase(ANOTHER_USE_CASE)
			.basicFlow().after(CUSTOMER_ENTERS_SOME_TEXT, SAY_HELLO_USE_CASE)
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber());
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	
	@Test
	public void shouldRestartUseCaseFromBasicFlow() {
		useCaseModel
			.newUseCase(SAY_HELLO_USE_CASE)
				.basicFlow().when(r -> getRunStepNames().size()<2)
					.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
					.restart();

		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent(), enterTextEvent());

		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldRestartUseCaseFromAlternativeFlow() {
		useCaseModel
			.newUseCase(SAY_HELLO_USE_CASE)
				.basicFlow().when(r -> getRunStepNames().size()<4)
					.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newFlow(ALTERNATIVE_FLOW).after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
					.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
					.restart();

		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent(), enterTextEvent(), enterDifferentTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT, CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldNotRepeatStepWhenConditionNotFulfilled() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.handle(EnterTextEvent.class).system(displayEnteredText())
						.repeatWhile(r -> false);
				
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent());
		
		assertEquals(1, getRunStepNames().size());
		assertEquals(CUSTOMER_ENTERS_SOME_TEXT, getLatestStepName());
	}
	
	@Test
	public void shouldRepeatStepOnce() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.handle(EnterTextEvent.class).system(displayEnteredText())
						.repeatWhile(r -> true);
				
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent());
		
		
		String repeatStepName = CUSTOMER_ENTERS_SOME_TEXT + REPEAT_STEP_POSTFIX;
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, repeatStepName), getRunStepNames());
	}
	
	@Test
	public void shouldRepeatStepTwice() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.handle(EnterTextEvent.class).system(displayEnteredText())
						.repeatWhile(r -> true);
				
		useCaseRunner.run()
			.reactTo(enterTextEvent(), enterTextEvent(), enterTextEvent());
		
		
		String repeatStepName = CUSTOMER_ENTERS_SOME_TEXT + REPEAT_STEP_POSTFIX;
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, repeatStepName, repeatStepName), 
			getRunStepNames());
	}
	
	@Test
	public void shouldRepeatStepThreeTimes() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.handle(EnterTextEvent.class).system(displayEnteredText())
						.repeatWhile(r -> getRunStepNames().size() < 3);
				
		// Create way to many events to see if the repeat stops after three events
		useCaseRunner.run()
			.reactTo(enterTextEvent(), enterTextEvent(), enterTextEvent(),
					enterTextEvent(), enterTextEvent(), enterTextEvent(),
					enterTextEvent(), enterTextEvent(), enterTextEvent());
		
		
		String repeatStepName = CUSTOMER_ENTERS_SOME_TEXT + REPEAT_STEP_POSTFIX;
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, repeatStepName, repeatStepName), 
			getRunStepNames());
	}
	
	@Test
	public void shouldRepeatStepThreeTimesAndThenReactToOtherEvent() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT)
					.handle(EnterTextEvent.class).system(displayEnteredText())
						.repeatWhile(r -> getRunStepNames().size() < 3)
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber());
				
		// Create way to many events to see if the repeat stops after three events
		useCaseRunner.run()
			.reactTo(enterTextEvent(), enterTextEvent(), enterTextEvent(),
					enterTextEvent(), enterTextEvent(), enterTextEvent(),
					enterTextEvent(), enterTextEvent(), enterTextEvent(),
					enterNumberEvent());
		
		
		String repeatStepName = CUSTOMER_ENTERS_SOME_TEXT + REPEAT_STEP_POSTFIX;
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, repeatStepName, repeatStepName, CUSTOMER_ENTERS_NUMBER), 
			getRunStepNames());
	}
	
	@Test
	public void shouldContinueAtFirstStepCalledFromFirstStepOfAlternativeFlow() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber())		
			.newFlow("Alternative Flow that continues with Basic Flow")
				.after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
					.continueAtFirst();
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent(), enterTextEvent());
		 
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldContinueAtFirstStepCalledFromSecondStepOfAlternativeFlow() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber())		
			.newFlow("Alternative Flow that continues with Basic Flow").after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.continueAtFirst();
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent(), enterTextEvent(), enterDifferentTextEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT,
				CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldContinueAtFirstStepCalledFromMultipleMutuallyExclusiveAlternativeFlows() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
		.basicFlow()
			.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
			.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText())
			.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber())		
		.newFlow("AF1: Alternative Flow that continues with Basic Flow").after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
			.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
			.continueAtFirst()	
		.newFlow("AF2: Alternative Flow that has a disabled condition").after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsNotAvailablePredicate())
			.newStep("Customer enters alternative number").handle(EnterNumberEvent.class).system(displayEnteredNumber())
			.continueAtFirst();
	
		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent(), enterTextEvent(), enterDifferentTextEvent());
	
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT,
			CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT), getRunStepNames());
	}
	
	@Test
	public void shouldContinueAfterFirstStepCalledFromFirstStepOfAlternativeFlow() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber())		
			.newFlow("Alternative Flow that continues with Basic Flow")
				.after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
					.continueAfter(CUSTOMER_ENTERS_SOME_TEXT);
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterTextEvent(), enterNumberEvent());
		 
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_TEXT_AGAIN, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	
	@Test
	public void shouldContinueAfterFirstStepCalledFromSecondStepOfAlternativeFlow() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber())		
			.newFlow("Alternative Flow that continues with Basic Flow").after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.continueAfter(CUSTOMER_ENTERS_SOME_TEXT);
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent(), enterTextEvent(), enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT,
			CUSTOMER_ENTERS_SOME_TEXT_AGAIN, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
	
	@Test
	public void shouldContinueAfterFirstStepCalledFromMultipleMutuallyExclusiveAlternativeFlows() {		
		useCaseModel.newUseCase(SAY_HELLO_USE_CASE)
			.basicFlow()
				.newStep(CUSTOMER_ENTERS_SOME_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_SOME_TEXT_AGAIN).handle(EnterTextEvent.class).system(displayEnteredText())
				.newStep(CUSTOMER_ENTERS_NUMBER).handle(EnterNumberEvent.class).system(displayEnteredNumber())		
			.newFlow("AF1: Alternative Flow that continues with Basic Flow").after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsAvailablePredicate())
				.newStep(CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT).handle(EnterTextEvent.class).system(displayEnteredText())
				.continueAfter(CUSTOMER_ENTERS_SOME_TEXT)		
			.newFlow("AF2: Alternative Flow that has a disabled condition").after(CUSTOMER_ENTERS_SOME_TEXT).when(textIsNotAvailablePredicate())
				.newStep("Customer enters alternative number").handle(EnterNumberEvent.class).system(displayEnteredNumber())
				.continueAfter(CUSTOMER_ENTERS_SOME_TEXT);
		
		useCaseRunner.run().reactTo(enterTextEvent(), enterDifferentTextEvent(), enterTextEvent(), enterNumberEvent());
		
		assertEquals(Arrays.asList(CUSTOMER_ENTERS_SOME_TEXT, CUSTOMER_ENTERS_SOME_DIFFERENT_TEXT,
				CUSTOMER_ENTERS_SOME_TEXT_AGAIN, CUSTOMER_ENTERS_NUMBER), getRunStepNames());
	}
}
