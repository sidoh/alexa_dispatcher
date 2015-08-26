# alexa_dispatcher
Tiny server to dispatch requests sent from Amazon's Alexa service to configurable endpoints based on the application identifier

Closely related to [echo_proxy](http://github.com/sidoh/echo_proxy), but handles routing each application to a separate endpoint.

## Why would I need this?

To use an Alexa skill (if you're using Echo), you have two options:

1. Enable officially approved skills through the Echo UI.
2. Register as a developer and create your own skills.

This helps you if you're going with (2). When you register your own skill, Amazon requires that you provide an endpoint (an address for a server hosting your skill). `alexa_dispatcher` is an endpoint server you can point Amazon at. When it receives a request from a skill, it forwards it to the endpoint configured to receive traffic for the skill. There are a couple of advantages to using this project as your endpoint:

1. It handles cryptographic signature verification for you. If you're hosting your own skills, it can be really important that this happens somewhere. If it doesn't, anyone can pretend to be Amazon and send fake requests to your skill!
2. If you're hosting many skills running as separate servers, this lets you bring them together behind a single endpoint. Since it runs signature verification on every request it receives, none of the downstream skill servers have to worry about it.
3. If you're hosting skills on a home server, you'll only have to open one port for Amazon: whichever one `alexa_dispatcher` is running on. It will take care of proxying requests to local servers and forwarding responses to Amazon.

## Using it

#### Check out the project:

```
$ git clone git@github.com:sidoh/alexa_dispatcher.git ./alexa_dispatcher
```

#### Edit the configuration:

Edit `./alexa_dispatcher/config/config.yml` to suit your needs. In particular, make sure you update the `applications` property to include all of the endpoints you want to configure.

#### Run it!

You should be able to start the server with the provided `run.sh` script:

```
$ ./alexa_dispatcher/bin/run.sh
```

#### Run it! (As a daemon)

You can also use `./alexa_dispatcher/bin/start` to run `alexa_dispatcher` as a daemon. Output will be redirected to `./alexa_dispatcher/log/alexa_dispatcher.out`. You can use `./alexa_dispatcher/bin/stop` to stop the daemon.

## Integrating

The server sends a `POST` request to the provided URL. The request information sent by Amazon is stuffed into a single object, and put in the body of the `POST` request. Here's a sample request:

```json
{
  "requestType": "IntentRequest",
  "request": {
    "intent": {
      "name": "NextBus",
      "slots": {
        "Route": {
          "name": "Route",
          "value": "thirty eight"
        }
      }
    },
    "requestId": "amzn1.echo-api.request.157f3045-9c73-41af-982d-2a25d1b7208c",
    "timestamp": "Aug 4, 2015 10:53:12 PM"
  },
  "session": {
    "isNew": true,
    "sessionId": "<session id>",
    "application": {
      "applicationId": "<app id>"
    },
    "attributes": {
    },
    "user": {
      "userId": "<user id>"
    }
  }
}
```

## SSL

This servlet doesn't handle SSL. Setting up SSL with jetty is really annoying, and I much prefer to do it with nginx. My nginx config looks like this:

```
server {
  listen 443 ssl;

  ssl_certificate /etc/nginx/ssl/alexa-dispatcher.sidoh.org/nginx.crt;
  ssl_certificate_key /etc/nginx/ssl/alexa-dispatcher.sidoh.org/nginx.key;

  location /{
    proxy_pass  http://127.0.0.1:8888;
  }
}
```
