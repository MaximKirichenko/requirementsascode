package org.requirementsascode;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.requirementsascode.builder.BuildModelTest;

@RunWith(Suite.class)
@SuiteClasses({ BuildModelTest.class, RunStopAndRestartTest.class, FlowTest.class, ReactToTypesTest.class, FlowlessTest.class,
	ExceptionsThrownTest.class, ExceptionHandlingTest.class, NonStandardEventHandlingTest.class,
	IncludesTest.class, RecordingTest.class, ActorWithBehaviorTest.class, FlowWithCaseStepTest.class })
public class AllTests {
}
