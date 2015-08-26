package org.sidoh.alexa_dispatcher.speechlet;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.sidoh.alexa_dispatcher.AlexaDispatcherConfig;
import org.sidoh.alexa_dispatcher.router.ApplicationRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherSpeechlet implements Speechlet {
  private static final Logger LOG = LoggerFactory.getLogger(DispatcherSpeechlet.class);

  private final HttpClient httpClient;
  private final Gson gson;
  private ApplicationRouter applicationRouter;

  public static class DispatcherRequest {
    private final String requestType;
    private final SpeechletRequest request;
    private final Session session;

    public DispatcherRequest(String requestType, SpeechletRequest request, Session session) {
      this.requestType = requestType;
      this.request = request;
      this.session = session;
    }
  }

  public static class DispatcherResponse {
    private String speech;
    private boolean shouldEndSession;

    public String getSpeech() {
      return speech;
    }

    public void setSpeech(String speech) {
      this.speech = speech;
    }

    public boolean isShouldEndSession() {
      return shouldEndSession;
    }

    public void setShouldEndSession(boolean shouldEndSession) {
      this.shouldEndSession = shouldEndSession;
    }

    public DispatcherResponse withShouldEndSession(boolean shouldEndSession) {
      this.shouldEndSession = shouldEndSession;
      return this;
    }

    public DispatcherResponse withSpeech(String speech) {
      this.speech = speech;
      return this;
    }
  }

  public DispatcherSpeechlet(ApplicationRouter applicationRouter) {
    this.applicationRouter = applicationRouter;
    this.httpClient = HttpClientBuilder
        .create()
        .disableAutomaticRetries()
        .setMaxConnTotal(30)
        .evictExpiredConnections()
        .evictIdleConnections(1000L, TimeUnit.MILLISECONDS)
        .setConnectionManager(new PoolingHttpClientConnectionManager(1000L, TimeUnit.MILLISECONDS))
        .build();
    this.gson = new Gson();
  }

  @Override
  public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
    handleRequest(sessionStartedRequest, session);
  }

  @Override
  public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
    return handleRequest(launchRequest, session);
  }

  @Override
  public SpeechletResponse onIntent(IntentRequest intentRequest, Session session) throws SpeechletException {
    return handleRequest(intentRequest, session);
  }

  @Override
  public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {
    handleRequest(sessionEndedRequest, session);
  }

  private SpeechletResponse handleRequest(SpeechletRequest request, Session session) {
    try {
      final DispatcherResponse dispatcherResponse = postRequest(request, session);
      final SpeechletResponse response = buildResponse(dispatcherResponse.getSpeech());
      response.setShouldEndSession(dispatcherResponse.isShouldEndSession());

      return response;
    } catch (IOException e) {
      LOG.error("Error processing request", e);
      return buildResponse("Error communicating with remote proxy server");
    }
  }

  private DispatcherResponse postRequest(SpeechletRequest request, Session session) throws IOException {
    final String requestJson = gson.toJson(new DispatcherRequest(request.getClass().getSimpleName(), request, session));
    try {
      final String endpoint = applicationRouter.resolveEndpoint(session.getApplication().getApplicationId());
      final HttpPost proxyRequest = new HttpPost(endpoint);
      proxyRequest.setEntity(new StringEntity(requestJson));

      final HttpResponse dispatcherResponse = httpClient.execute(proxyRequest);
      final int statusCode = dispatcherResponse.getStatusLine().getStatusCode();

      if (statusCode < 200 || statusCode > 299) {
        // Read response body to trigger connection close.
        final String error = EntityUtils.toString(dispatcherResponse.getEntity());

        LOG.error("Error handling response from remote server. Response code: {}, error: {}", statusCode, error);

        return new DispatcherResponse()
            .withSpeech("Error handling request. Response code was " + statusCode);
      }

      final HttpEntity entity = dispatcherResponse.getEntity();
      final String json = EntityUtils.toString(entity, "UTF-8");

      return gson.fromJson(json, DispatcherResponse.class);
    } catch (ApplicationRouter.ApplicationNotHandledException e) {
      throw new RuntimeException(e);
    }
  }

  private static SpeechletResponse buildResponse(String text) {
    final PlainTextOutputSpeech output = new PlainTextOutputSpeech();
    output.setText(text);

    final SpeechletResponse response = new SpeechletResponse();
    response.setOutputSpeech(output);

    return response;
  }
}
