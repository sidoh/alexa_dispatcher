package org.sidoh.alexa_dispatcher.router;

public interface ApplicationRouter {
  /**
   * Resolves an endpoint for a given Alexa application.
   *
   * @param applicationId
   * @return
   */
  String resolveEndpoint(String applicationId) throws ApplicationNotHandledException;

  class ApplicationNotHandledException extends Exception {
    public ApplicationNotHandledException(String message) {
      super(message);
    }

    public ApplicationNotHandledException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
