package helloworld;

import org.requirementsascode.Actor;
import org.requirementsascode.UseCaseModel;
import org.requirementsascode.UseCaseModelBuilder;
import org.requirementsascode.UseCaseModelRunner;

import helloworld.userevent.EntersText;

public class HelloWorld06 extends AbstractHelloWorldExample{
	private static final Class<EntersText> ENTERS_FIRST_NAME = EntersText.class;
	private static final Class<EntersText> ENTERS_AGE = EntersText.class;
	private static final Class<NumberFormatException> NON_NUMERICAL_AGE = NumberFormatException.class;
	
	private static final int MIN_AGE = 5;
	private static final int MAX_AGE = 130;
	
	private String firstName;
	private int age;
	
	private Actor normalUser;
	private Actor anonymousUser;
	
	public UseCaseModel buildWith(UseCaseModelBuilder modelBuilder) {
		normalUser = modelBuilder.actor("Normal User");
		anonymousUser = modelBuilder.actor("Anonymous User");
				
		UseCaseModel useCaseModel = 
			modelBuilder.useCase("Get greeted")
				.basicFlow()
					.step("S1").as(normalUser).system(this::promptsUserToEnterFirstName)
					.step("S2").as(normalUser).user(ENTERS_FIRST_NAME).system(this::savesFirstName)
					.step("S3").as(normalUser, anonymousUser).system(this::promptsUserToEnterAge)
					.step("S4").as(normalUser, anonymousUser).user(ENTERS_AGE).system(this::savesAge)
					.step("S5").as(normalUser).system(this::greetsUserWithFirstName)
					.step("S6").as(normalUser, anonymousUser).system(this::greetsUserWithAge)
					.step("S7").as(normalUser, anonymousUser).system(this::stops)
						
				.flow("Handle out-of-bounds age").insteadOf("S5").when(this::ageIsOutOfBounds)
					.step("S5a_1").system(this::informsUserAboutOutOfBoundsAge)
					.step("S5a_2").continuesAt("S3")
						
				.flow("Handle non-numerical age").insteadOf("S5")
					.step("S5b_1").handles(NON_NUMERICAL_AGE).system(this::informsUserAboutNonNumericalAge)
					.step("S5b_2").continuesAt("S3")
					
				.flow("Anonymous greeted with age only").insteadOf("S5").when(this::ageIsOk)
					.step("S5c_1").as(anonymousUser).continuesAt("S6")
					
				.flow("Anonymous does not enter name").insteadOf("S1")
					.step("S1a_1").as(anonymousUser).continuesAt("S3")
			.build();
		return useCaseModel;
	}

	private void promptsUserToEnterFirstName(UseCaseModelRunner runner) {
		System.out.print("Please enter your first name: ");
	}
	
	private void promptsUserToEnterAge(UseCaseModelRunner runner) {
		System.out.print("Please enter your age: ");
	}

	private void savesFirstName(EntersText enterText) {
		firstName = enterText.text;
	}
	
	private void savesAge(EntersText enterText) {
		age = Integer.parseInt(enterText.text);
	}
	
	private void greetsUserWithFirstName(UseCaseModelRunner runner) {
		System.out.println("Hello, " + firstName + ".");
	}
	
	private void greetsUserWithAge(UseCaseModelRunner runner) {
		System.out.println("You are " + age + " years old.");
	}
	
	private boolean ageIsOk(UseCaseModelRunner runner) {
		return !ageIsOutOfBounds(runner);
	}
	
	private boolean ageIsOutOfBounds(UseCaseModelRunner runner) {
		return age < MIN_AGE || age > MAX_AGE;
	}
	
	private void informsUserAboutOutOfBoundsAge(UseCaseModelRunner runner) {
		System.out.println("Please enter your real age, between " + MIN_AGE + " and " + MAX_AGE);
	}
	
	private void informsUserAboutNonNumericalAge(NumberFormatException exception) {
		System.out.println("You entered a non-numerical age.");
	}
	
	public static void main(String[] args){		
		HelloWorld06 example = new HelloWorld06();
		example.start(); 
	}

	private void start() {
		UseCaseModel useCaseModel = buildWith(UseCaseModelBuilder.newBuilder());
		UseCaseModelRunner useCaseModelRunner = new UseCaseModelRunner();
		useCaseModelRunner.as(anonymousUser()).run(useCaseModel);			
		while(!systemStopped())
			useCaseModelRunner.reactTo(entersText());	
		exitSystem();	
	}

	public Actor normalUser() {
		return normalUser;
	}

	public Actor anonymousUser() {
		return anonymousUser;
	}
}