package actor;

import java.util.function.Consumer;

import org.requirementsascode.AbstractActor;
import org.requirementsascode.Model;

public class ActorExample {
  public static void main(String[] args) {
    AbstractActor greetingService = new GreetingService(new SayHello());
    new MessageSender(greetingService).sendMessages();
  }
}

/**
 * Actor that owns and runs the use case model, and reacts to messages by
 * dispatching them to message handlers.
 */
class GreetingService extends AbstractActor {
  private static final Class<RequestHello> requestsHello = RequestHello.class;
  private Consumer<RequestHello> saysHello;

  public GreetingService(Consumer<RequestHello> saysHello) {
    this.saysHello = saysHello;
  }

  @Override
  public Model behavior() {
    Model model = Model.builder()
      .user(requestsHello).system(saysHello)
    .build();
    return model;
  }
}

/**
 * Message sender class
 */
class MessageSender {
  private AbstractActor greetingService;

  public MessageSender(AbstractActor greetingService) {
    this.greetingService = greetingService;
  }

  /**
   * Send message to the service actor. In this example, we don't care about the
   * return value of the call, because we don't send a query or publish events.
   */
  public void sendMessages() {
    greetingService.reactTo(new RequestHello("Joe"));
  }
}

/**
 * Command class
 */
class RequestHello {
  private String userName;

  public RequestHello(String userName) {
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }
}

/**
 * Message handler
 */
class SayHello implements Consumer<RequestHello> {
  private OutputAdapter outputAdapter;

  public SayHello() {
    this.outputAdapter = new OutputAdapter();
  }

  public void accept(RequestHello requestHello) {
    String greeting = Greeting.forUser(requestHello.getUserName());
    outputAdapter.showMessage(greeting);
  }
}

/**
 * Infrastructure class
 */
class OutputAdapter {
  public void showMessage(String message) {
    System.out.println(message);
  }
}

/**
 * Domain class
 */
class Greeting {
  public static String forUser(String userName) {
    return "Hello, " + userName + ".";
  }
}
