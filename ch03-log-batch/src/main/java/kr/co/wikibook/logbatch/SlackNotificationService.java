package kr.co.wikibook.logbatch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SlackNotificationService implements NotificationService {

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final URI webhookUrl;

  public SlackNotificationService(URI webhookUrl) {
    this.webhookUrl = webhookUrl;
  }

  @Override
  public void send(String message) {
    var request = HttpRequest.newBuilder(webhookUrl)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + message + "\"}"))
        .build();
    try {
      httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    } catch (IOException ex) {
      throw new IllegalStateException("슬랙 알림 전송에 실패했습니다", ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("슬랙 알림 전송이 중단되었습니다", ex);
    }
  }
}
