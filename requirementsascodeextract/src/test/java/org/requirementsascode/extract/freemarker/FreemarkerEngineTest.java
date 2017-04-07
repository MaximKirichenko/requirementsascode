package org.requirementsascode.extract.freemarker;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.requirementsascode.UseCaseModel;
import org.requirementsascode.UseCaseModelBuilder;
import org.requirementsascode.UseCaseModelRunner;
import org.requirementsascode.extract.freemarker.FreeMarkerEngine;
import org.requirementsascode.extract.freemarker.systemreaction.GreetUser;
import org.requirementsascode.extract.freemarker.systemreaction.PromptUserToEnterName;
import org.requirementsascode.extract.freemarker.userevent.EnterName;

public class FreemarkerEngineTest {
  private FreeMarkerEngine engine;

  @Before
  public void setUp() throws Exception {
    engine = new FreeMarkerEngine();
  }

  @Test
  public void printsUseCaseModelToConsole() throws Exception {
		UseCaseModel useCaseModel = UseCaseModelBuilder.newBuilder()
			.useCase("Get greeted")
				.basicFlow()
					.step("S1").system(promptUserToEnterName())
					.step("S2").user(enterName()).system(greetUser())
		.build();

    engine.put("useCaseModel", useCaseModel);
    File templateFile = new File("src/test/resources/example.ftlh");
    engine.process(templateFile, new OutputStreamWriter(System.out));
  }

  private Consumer<UseCaseModelRunner> promptUserToEnterName() {
    return new PromptUserToEnterName();
  }

  private Class<EnterName> enterName() {
    return EnterName.class;
  }

  private Consumer<EnterName> greetUser() {
    return new GreetUser();
  }
}