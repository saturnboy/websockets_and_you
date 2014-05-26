# Websockets and You

## 1. Chat

Chat is the canonical websocket example, so here's a simple chat server and clients.

### Chat Server

The chat server is based on Jetty 9 and uses its fantastic built-in WebSocket support (which requires Java 7 to build and run).  Start the server like this:

```
> cd chat-server
> mvn clean compile exec:java
```

### Chat Client - HTML5

The chat server includes a dead simple one page HTML5 client (found at `chat-server/web/index.html`).  Simply start the server on your local machine, then point any modern browser to: <http://localhost:7006/>

### Chat Client - iOS

The iOS client uses the excellent [SocketRocket](https://github.com/square/SocketRocket) library, which provides full [RFC 6455](http://tools.ietf.org/html/rfc6455) WebSocket support for iOS.  Just build with Xcode and run on the iOS Simulator to see the iOS client work.

## 2. Feed : a realtime multiplayer SpriteKit game

coming soon

### Feed Server

coming soon

### Feed Client

coming soon