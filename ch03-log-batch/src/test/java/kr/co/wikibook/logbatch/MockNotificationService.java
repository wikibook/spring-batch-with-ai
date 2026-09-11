package kr.co.wikibook.logbatch;

public class MockNotificationService implements NotificationService {

  private String lastMessage;

  @Override
  public void send(String message) {
    lastMessage = message;
  }

  public String getLastMessage() {
    return lastMessage;
  }
}
