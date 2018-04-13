package org.requirementsascode;

/**
 * Class that builds a {@link UseCaseModel}, in a fluent way.
 *
 * @author b_muth
 */
public class UseCaseModelBuilder {

  private UseCaseModel useCaseModel;

  private UseCaseModelBuilder(UseCaseModel useCaseModel) {
    this.useCaseModel = useCaseModel;
  }

  /**
   * Create a builder for a new use case model.
   *
   * @return the new builder
   */
  public static UseCaseModelBuilder newBuilder() {
    return builderOf(new UseCaseModel());
  }

  /**
   * Create a builder for an existing use case model, to continue building it.
   *
   * @param useCaseModel the model to continue building
   * @return a builder for the existing model
   */
  public static UseCaseModelBuilder builderOf(UseCaseModel useCaseModel) {
    return new UseCaseModelBuilder(useCaseModel);
  }

  /**
   * Creates a new actor in the current model.
   * If an actor with the specified name already exists, returns the existing actor.
   *
   * @param actorName the name of the existing actor / actor to be created.
   * @return the created / found actor.
   */
  public Actor actor(String actorName) {
    Actor actor = useCaseModel.hasActor(actorName)? useCaseModel.findActor(actorName) : useCaseModel.newActor(actorName);
    return actor;
  }

  /**
   * Creates a new use case in the current model, and returns a part for building its details.
   * If a use case with the specified name already exists, returns a part for the existing use case.
   *
   * @param useCaseName the name of the existing use case / use case to be created.
   * @return the created / found use case's part.
   */
  public UseCasePart useCase(String useCaseName) {
    UseCase useCase = useCaseModel.hasUseCase(useCaseName)? useCaseModel.findUseCase(useCaseName) : useCaseModel.newUseCase(useCaseName);

    return new UseCasePart(useCase, this);
  }

  /**
   * Returns the use case model built so far.
   *
   * @return the use case model
   */
  public UseCaseModel build() {
    return useCaseModel;
  }
}
