Learning Chat
=============

Here's a simple chat server and clients (iOS and HTML5).

## Chat Server

The chat server use the solid WebSocket support built into Jetty 9. Thus is requires Java 7 to build and run.  A pre-built jar can be found in the `server/` folder.  Start the server like this (requires Java 7):

> java -jar chat-server-1.0.jar

## Chat Client - HTML5

Included in the chat server jar file is a fully functional HTML5 chat client (found in the `server/web/` folder).  To run the HTML5 client, first start the chat server, then simply point a modern browser to:

> [http://localhost:7117/](http://localhost:7117/)

The HTML5 client is just a single index.html file that is served by Jetty directly from the jar.  The [Bootstrap](http://getbootstrap.com/) framework is used to provide a responsive design, so the client looks great on a desktop browser or a mobile browser.  It only works great as long as the mobile browser provides WebSocket support -- like Mobile Safari or Chrome for Mobile.

## Chat Client - iOS

The iOS client uses the excellent [SocketRocket](/square/SocketRocket) library, which provides full [RFC 6455](http://tools.ietf.org/html/rfc6455) WebSocket support for iOS.  Just build and run on the iOS Simulator with Xcode to see the iOS client work