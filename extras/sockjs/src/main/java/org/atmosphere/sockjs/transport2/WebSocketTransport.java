package org.atmosphere.sockjs.transport2;

import java.io.IOException;
import java.util.Map;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class WebSocketTransport extends AbstractTransport {

	private final Logger logger = LoggerFactory.getLogger(WebSocketTransport.class);

	WebSocketTransport(final Object vertx, Object wsMatcher, Object rm, String basePath, final Map<String, Session> sessions, final Object config, final Object sockHandler) {
		/*
		 * super(vertx, sessions, config); String wsRE = basePath + COMMON_PATH_ELEMENT_RE + "websocket";
		 * 
		 * wsMatcher.addRegEx(wsRE, new Handler<WebSocketMatcher.Match>() {
		 * 
		 * public void handle(final WebSocketMatcher.Match match) { if (logger.isTraceEnabled()) logger.trace("WS, handler"); final Session session = new Session(vertx, sessions, config .getLong("heartbeat_period"), sockHandler); session.register(new WebSocketListener(match.ws, session)); } });
		 * 
		 * rm.getWithRegEx(wsRE, new Handler<HttpServerRequest>() { public void handle(HttpServerRequest request) { if (logger.isTraceEnabled()) logger.trace("WS, get: " + request.uri); request.response.statusCode = 400; request.response.end("Can \"Upgrade\" only to \"WebSocket\"."); } });
		 * 
		 * rm.allWithRegEx(wsRE, new Handler<HttpServerRequest>() { public void handle(HttpServerRequest request) { if (logger.isTraceEnabled()) logger.trace("WS, all: " + request.uri); request.response.headers().put("Allow", "GET"); request.response.statusCode = 405; request.response.end(); } });
		 */
	}

	private static class WebSocketListener implements TransportListener {

		// final WebSocket ws;
		Session session;
		boolean closed;

		WebSocketListener(Object ws, Session session) {
			/*
			 * this.ws = ws; this.session = session; ws.dataHandler(new Handler<Buffer>() { public void handle(Buffer data) { if (!session.isClosed()) { String msgs = data.toString(); if (msgs.equals("")) { // Ignore empty frames } else if ((msgs.startsWith("[\"") && msgs .endsWith("\"]")) || (msgs.startsWith("\"") && msgs .endsWith("\""))) { session.handleMessages(msgs); } else { // Invalid JSON - we close the connection close(); } } } }); ws.closedHandler(new SimpleHandler() { public void
			 * handle() { closed = true; session.shutdown(); } });
			 */
		}

		public void sendFrame(final String body) {
			/*
			 * if (logger.isTraceEnabled()) logger.trace("WS, sending frame"); if (!closed) { ws.writeTextFrame(body); }
			 */
		}

		public void close() {
			/*
			 * if (!closed) { ws.close(); session.shutdown(); closed = true; }
			 */
		}

		public void sessionClosed() {
			/*
			 * session.writeClosed(this); closed = true; ws.close();
			 */
		}

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Action handle(AtmosphereResourceImpl resource, AtmosphereHandler atmosphereHandler, Object sessionFactory) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
