package org.sidoh.alexa_dispatcher;

import java.io.File;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.sidoh.alexa_dispatcher.router.ApplicationRouter;
import org.sidoh.alexa_dispatcher.router.InMemoryApplicationRouter;
import org.sidoh.alexa_dispatcher.speechlet.DispatcherSpeechlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlexaDispatcherServer {
  private static final Logger LOG = LoggerFactory.getLogger(AlexaDispatcherServer.class);

  public static void main(String[] args) throws Exception {
    final File configFile;
    if (args.length > 0) {
      configFile = new File(args[0]);
    } else {
      configFile = new File("config/config.yml");
    }

    final AlexaDispatcherConfig config = AlexaDispatcherConfig.fromYaml(configFile);

    // Apply system settings used by SpeechletServlet
    if (config.verifyTimestamps) {
      System.setProperty("com.amazon.speech.speechlet.servlet.timestampTolerance", "150");
    }

    if (! config.verifySignatures) {
      System.setProperty("com.amazon.speech.speechlet.servlet.disableRequestSignatureCheck", "true");
    }

    final ApplicationRouter applicationRouter = new InMemoryApplicationRouter(config.applications);

    // Configure server and its associated servlet
    final Server server = new Server();

    final ServerConnector serverConnector = new ServerConnector(server);
    serverConnector.setPort(config.port);
    server.setConnectors(new Connector[]{serverConnector});

    final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);

    context.addServlet(new ServletHolder(createServlet(new DispatcherSpeechlet(applicationRouter))), "/");

    LOG.info("Starting server");

    server.start();
    server.join();

    LOG.info("Shutting down");
  }

  private static SpeechletServlet createServlet(final Speechlet speechlet) {
    SpeechletServlet servlet = new SpeechletServlet();
    servlet.setSpeechlet(speechlet);
    return servlet;
  }
}
