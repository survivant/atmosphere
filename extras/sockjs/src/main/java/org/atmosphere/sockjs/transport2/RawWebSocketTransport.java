package org.atmosphere.sockjs.transport2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RawWebSocketTransport {

	private final Logger logger = LoggerFactory.getLogger(RawWebSocketTransport.class);

	private class RawWSSockJSSocket {

		private Object ws;

		RawWSSockJSSocket(Object vertx, Object ws) {
			this.ws = ws;
		}

		public void dataHandler(Object handler) {
			// ws.dataHandler(handler);
		}

		public void pause() {
			// ws.pause();
		}

		public void resume() {
			// ws.resume();
		}

		public void writeBuffer(Object data) {
			// ws.writeBuffer(data);
		}

		public void setWriteQueueMaxSize(int maxQueueSize) {
			// ws.setWriteQueueMaxSize(maxQueueSize);
		}

		public boolean writeQueueFull() {
			return true;// ws.writeQueueFull();
		}

		public void drainHandler(Object handler) {
			// ws.drainHandler(handler);
		}

		public void exceptionHandler(Object handler) {
			// ws.exceptionHandler(handler);
		}

		public void endHandler(Object endHandler) {
			// ws.endHandler(endHandler);
		}

		public void close() {
			// super.close();
			// ws.close();
		}

	}

	RawWebSocketTransport(Object vertx, Object wsMatcher, Object rm, String basePath, Object sockHandler) {

		String wsRE = basePath + "/websocket";
		/*
		 * wsMatcher.addRegEx(wsRE, new Handler<WebSocketMatcher.Match>() {
		 * 
		 * public void handle(final WebSocketMatcher.Match match) { SockJSSocket sock = new RawWSSockJSSocket(vertx, match.ws); sockHandler.handle(sock); } });
		 * 
		 * rm.getWithRegEx(wsRE, new Handler<HttpServerRequest>() { public void handle(HttpServerRequest request) { request.response.statusCode = 400; request.response.end("Can \"Upgrade\" only to \"WebSocket\"."); } });
		 * 
		 * rm.allWithRegEx(wsRE, new Handler<HttpServerRequest>() { public void handle(HttpServerRequest request) { request.response.headers().put("Allow", "GET"); request.response.statusCode = 405; request.response.end(); } });
		 */
	}

}
