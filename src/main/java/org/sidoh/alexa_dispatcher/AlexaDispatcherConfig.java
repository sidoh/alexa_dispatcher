package org.sidoh.alexa_dispatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public final class AlexaDispatcherConfig {
  public static class Application {
    public final String endpoint;
    public final String applicationId;

    public Application() {
      this.endpoint = "";
      this.applicationId = "";
    }
  }

  public final String endpoint;
  public final int port;
  public final boolean verifySignatures;
  public final boolean verifyTimestamps;
  public final List<Application> applications;

  public AlexaDispatcherConfig() {
    this.endpoint = "";
    this.port = 8888;
    this.verifySignatures = true;
    this.verifyTimestamps = true;
    this.applications = Collections.emptyList();
  }

  public static AlexaDispatcherConfig fromYaml(File yamlFile) throws FileNotFoundException {
    final InputStream configInputStream = new FileInputStream(yamlFile);
    final Yaml yaml = new Yaml(new Constructor(AlexaDispatcherConfig.class));
    return (AlexaDispatcherConfig)yaml.load(configInputStream);
  }
}
