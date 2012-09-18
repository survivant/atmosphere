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
class EventSourceTransport extends AbstractTransport {

	private final Logger logger = LoggerFactory.getLogger(EventSourceTransport.class);

	EventSourceTransport(Object vertx, Object rm, String basePath, Map<String, Session> sessions, final Object config, final Object sockHandler) {
		// super(vertx, sessions, config);

		String eventSourceRE = basePath + COMMON_PATH_ELEMENT_RE + "eventsource";

		/*
		 * rm.getWithRegEx(eventSourceRE, new Handler<HttpServerRequest>() { public void handle(final HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("EventSource transport, get: " + req.uri); String sessionID = req.params().get("param0"); Session session = getSession(config.getLong("session_timeout"), config.getLong("heartbeat_period"), sessionID, sockHandler); session.register(new EventSourceListener(config .getInteger("max_bytes_streaming"), req, session)); } });
		 */
	}

	private class EventSourceListener extends BaseListener {

		final int maxBytesStreaming;
		final Object req;
		final Session session;

		boolean headersWritten;
		int bytesSent;
		boolean closed;

		EventSourceListener(int maxBytesStreaming, Object req, Session session) {
			this.maxBytesStreaming = maxBytesStreaming;
			this.req = req;
			this.session = session;
			// addCloseHandler(req.response, session);
		}

		public void sendFrame(String body) {
			/*
			 * if (logger.isTraceEnabled()) logger.trace("EventSource, sending frame"); if (!headersWritten) { req.response.headers().put("Content-Type", "text/event-stream; charset=UTF-8"); req.response.headers().put("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0"); setJSESSIONID(config, req); req.response.setChunked(true); req.response.write("\r\n"); headersWritten = true; } StringBuilder sb = new StringBuilder(); sb.append("data: "); sb.append(body); sb.append("\r\n\r\n");
			 * Buffer buff = new Buffer(sb.toString()); req.response.write(buff); bytesSent += buff.length(); if (bytesSent >= maxBytesStreaming) { if (logger.isTraceEnabled()) logger.trace("More than maxBytes sent so closing connection"); // Reset and close the connection close(); }
			 */
		}

		public void close() {
			/*
			 * if (!closed) { try { session.resetListener(); req.response.end(); req.response.close(); } catch (IllegalStateException e) { // Underlying connection might alreadu be closed - that's // fine } closed = true; }
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
