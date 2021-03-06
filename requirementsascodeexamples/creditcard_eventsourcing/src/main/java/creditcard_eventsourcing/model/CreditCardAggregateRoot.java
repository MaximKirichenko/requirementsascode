package creditcard_eventsourcing.model;

import static creditcard_eventsourcing.model.CreditCard.assigningLimit;
import static creditcard_eventsourcing.model.CreditCard.assigningLimitTwice;
import static creditcard_eventsourcing.model.CreditCard.closingCycle;
import static creditcard_eventsourcing.model.CreditCard.repaying;
import static creditcard_eventsourcing.model.CreditCard.repeating;
import static creditcard_eventsourcing.model.CreditCard.withdrawingCard;
import static creditcard_eventsourcing.model.CreditCard.withdrawingCardAgain;
import static creditcard_eventsourcing.model.CreditCard.withdrawingCardTooOften;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.requirementsascode.Condition;
import org.requirementsascode.Model;
import org.requirementsascode.ModelRunner;
import org.requirementsascode.Step;

import creditcard_eventsourcing.model.command.RequestToCloseCycle;
import creditcard_eventsourcing.model.command.RequestRepay;
import creditcard_eventsourcing.model.command.RequestToAssignLimit;
import creditcard_eventsourcing.model.command.RequestWithdrawal;
import creditcard_eventsourcing.model.event.CardRepaid;
import creditcard_eventsourcing.model.event.CardWithdrawn;
import creditcard_eventsourcing.model.event.CycleClosed;
import creditcard_eventsourcing.model.event.DomainEvent;
import creditcard_eventsourcing.model.event.LimitAssigned;
import creditcard_eventsourcing.persistence.EventStore;

public class CreditCardAggregateRoot {
	private static final String useCreditCard = "Use credit card";

	// Command types
	private static final Class<RequestToAssignLimit> requestsToAssignLimit = RequestToAssignLimit.class;
	private static final Class<RequestWithdrawal> requestsWithdrawingCard = RequestWithdrawal.class;
	private static final Class<RequestRepay> requestsRepay = RequestRepay.class;
	private static final Class<RequestToCloseCycle> requestToCloseCycle = RequestToCloseCycle.class;

	// Command handling methods
	private Function<RequestToAssignLimit, DomainEvent> assignedLimit = this::assignedLimit;
	private Function<RequestWithdrawal, DomainEvent> withdrawnCard = this::withdrawnCard;
	private Function<RequestRepay, DomainEvent> repay = this::repay;
	private Function<RequestToCloseCycle, DomainEvent> closedCycle = this::closedCycle;
	private Consumer<RequestToAssignLimit> throwsAssignLimitException = this::throwAssignLimitException;
	private Consumer<RequestWithdrawal> throwsTooManyWithdrawalsException = this::throwTooManyWithdrawalsException;

	// Conditions
	private Condition tooManyWithdrawalsInCycle = this::tooManyWithdrawalsInCycle;
	private Condition limitAlreadyAssigned = this::limitAlreadyAssigned;
	private Condition accountIsOpen = this::accountIsOpen;

	// Other fields
	private final UUID uuid;
	private final EventStore eventStore;
	private final Model model;
	
	private CreditCard creditCard;

	public CreditCardAggregateRoot(UUID uuid, EventStore eventStore) {
		this.uuid = uuid;
		this.eventStore = eventStore;
		this.model = buildModel();
		this.creditCard = loadCreditCard();
	}

	/**
	 * Builds the model that defines the credit card behavior.
	 * 
	 * @return the use case model
	 */
	private Model buildModel() {
		Model model = Model.builder()
		  .useCase(useCreditCard)
		    .basicFlow()
		    	.step(assigningLimit).user(requestsToAssignLimit).systemPublish(assignedLimit)
		    	.step(withdrawingCard).user(requestsWithdrawingCard).systemPublish(withdrawnCard).reactWhile(accountIsOpen)
		    	.step(repaying).user(requestsRepay).systemPublish(repay).reactWhile(accountIsOpen)
		    	
		    .flow("Withdraw again").after(repaying)
		    	.step(withdrawingCardAgain).user(requestsWithdrawingCard).systemPublish(withdrawnCard)
		    	.step(repeating).continuesAt(withdrawingCard)
		    	
		    .flow("Cycle is over").anytime()
		    	.step(closingCycle).on(requestToCloseCycle).systemPublish(closedCycle)
		    	
		    .flow("Limit can only be assigned once").condition(limitAlreadyAssigned)
		    	.step(assigningLimitTwice).user(requestsToAssignLimit).system(throwsAssignLimitException)
		    	
		    .flow("Too many withdrawals").condition(tooManyWithdrawalsInCycle) 
		    	.step(withdrawingCardTooOften).user(requestsWithdrawingCard).system(throwsTooManyWithdrawalsException)
		.build();
		return model;
	}
	
	public BigDecimal getAvailableLimit() {
		return creditCard().getAvailableLimit();
	} 

	/**
	 * This is the method to be called by clients for handling commands.
	 * Each command that is accepted will cause an event to be applied to the credit card.
	 * After that, the events are saved to the event store.
	 * 
	 * @param command the command to handle.
	 */
	public void accept(Object command) {
		this.creditCard = loadCreditCard();
		Optional<DomainEvent> event = restoreStateAndHandle(command);
		applyToCreditCardIfPresent(event);
		saveCreditCard();
	}
	
	// Loads the credit card from the event store, replaying all saved events
	CreditCard loadCreditCard() {
		List<DomainEvent> events = eventStore.loadEvents(uuid());
		CreditCard creditCard = new CreditCard(uuid(), events);
		return creditCard;
	}
	
	// Creates a new model runner and restores the previous state.
	// The runner handles the command and returns an event.
	private Optional<DomainEvent> restoreStateAndHandle(Object command) {
		ModelRunner modelRunner = new ModelRunner().run(model());
		restorePreviousStateOf(modelRunner);
		return modelRunner.reactTo(command);
	}

	// If a command handler returned an event, apply it to the credit card 
	private void applyToCreditCardIfPresent(Optional<DomainEvent> event) {
		event.ifPresent(ev -> creditCard().apply(ev));
	}
	
	// Save all pending events of the credit card to the event store
	private void saveCreditCard() {
    List<DomainEvent> currentStream = eventStore().loadEvents(uuid());
    currentStream.addAll(creditCard().pendingEvents());
		eventStore().save(uuid(), currentStream);
    creditCard().flushEvents();
	}

	// Command handling methods (that return events)
	
	private DomainEvent assignedLimit(RequestToAssignLimit request) {
		BigDecimal amount = request.getAmount();
		return new LimitAssigned(uuid(), amount, Instant.now());
	}

	private DomainEvent withdrawnCard(RequestWithdrawal request) {
		BigDecimal amount = request.getAmount();
		if (creditCard().notEnoughMoneyToWithdraw(amount)) {
			throw new IllegalStateException();
		}
		return new CardWithdrawn(uuid(), amount, Instant.now());
	}

	private DomainEvent repay(RequestRepay request) {
		BigDecimal amount = request.getAmount();
		return new CardRepaid(uuid(), amount, Instant.now());
	}
	
	private DomainEvent closedCycle(RequestToCloseCycle request) {
		return new CycleClosed(uuid(), Instant.now());
	}

	private void throwAssignLimitException(RequestToAssignLimit request) {
		throw new IllegalStateException();
	}

	private void throwTooManyWithdrawalsException(RequestWithdrawal request) {
		throw new IllegalStateException();
	}
	
	// Conditions
	
	boolean tooManyWithdrawalsInCycle() {
		return creditCard().tooManyWithdrawalsInCycle();
	}

	boolean limitAlreadyAssigned() {
		return creditCard().isLimitAlreadyAssigned();
	}

	boolean accountIsOpen() {
		return creditCard().isAccountOpen();
	}
	
	// Methods for restoring the previous state of the ModelRunner
	
	private void restorePreviousStateOf(ModelRunner modelRunner) {
		Optional<Step> latestStepOfEventModel = creditCard().latestStep();
		latestStepOfEventModel.ifPresent(step -> {
			Step latestStepOfCommandModel = findNamedStep(step.getName());
			modelRunner.setLatestStep(latestStepOfCommandModel);
		});
	}

	private Step findNamedStep(final String stepName) {
		Step step = model().findUseCase(useCreditCard).findStep(stepName);
		return step;
	}

	private UUID uuid() {
		return uuid;
	}
	
	private CreditCard creditCard() {
		return creditCard;
	}

	private EventStore eventStore() {
		return eventStore;
	}

	private Model model() {
		return model;
	}
}
