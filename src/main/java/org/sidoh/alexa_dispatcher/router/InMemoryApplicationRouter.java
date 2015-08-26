package org.sidoh.alexa_dispatcher.router;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.sidoh.alexa_dispatcher.AlexaDispatcherConfig;

public class InMemoryApplicationRouter implements ApplicationRouter {
  private final Map<String, String> endpointsByApplicationId;

  public InMemoryApplicationRouter(List<AlexaDispatcherConfig.Application> applications) {
    Preconditions.checkNotNull(applications);

    final Map<String, String> endpointsByApplicationId = Maps.newHashMap();
    for (AlexaDispatcherConfig.Application application : applications) {
      endpointsByApplicationId.put(application.applicationId, application.endpoint);
    }
    this.endpointsByApplicationId = Collections.unmodifiableMap(endpointsByApplicationId);
  }

  @Override
  public String resolveEndpoint(String applicationId) throws ApplicationNotHandledException {
    if (!endpointsByApplicationId.containsKey(applicationId)) {
      throw new ApplicationNotHandledException("Application ID not handled: " + applicationId);
    }

    return endpointsByApplicationId.get(applicationId);
  }
}
