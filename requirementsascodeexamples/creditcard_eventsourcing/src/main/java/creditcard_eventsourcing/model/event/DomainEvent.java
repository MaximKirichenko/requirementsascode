package creditcard_eventsourcing.model.event;

/**
 * Based on code by Jakub Pilimon:
 * https://gitlab.com/pilloPl/eventsourced-credit-cards/blob/4329a0aac283067f1376b3802e13f5a561f18753
 *
 */
public interface DomainEvent {
	String getType();
}
