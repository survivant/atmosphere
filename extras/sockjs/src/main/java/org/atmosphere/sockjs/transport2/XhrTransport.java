package org.atmosphere.sockjs.transport2;

import java.io.IOException;
import java.util.Map;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XhrTransport extends AbstractTransport {

	private final Logger logger = LoggerFactory.getLogger(XhrTransport.class);

	private static final Object H_BLOCK = null;

	static {
		byte[] bytes = new byte[2048 + 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) 'h';
		}
		bytes[bytes.length - 1] = (byte) '\n';
		// H_BLOCK = new Object(bytes);
	}

	XhrTransport(Object vertx, Object rm, String basePath, final Map<String, Session> sessions, Object config, final Object sockHandler) {

		// super(vertx, sessions, config);

		String xhrBase = basePath + COMMON_PATH_ELEMENT_RE;
		String xhrRE = xhrBase + "xhr";
		String xhrStreamRE = xhrBase + "xhr_streaming";

		/*
		 * Object xhrOptionsHandler = createCORSOptionsHandler(config, "OPTIONS, POST");
		 * 
		 * rm.optionsWithRegEx(xhrRE, xhrOptionsHandler); rm.optionsWithRegEx(xhrStreamRE, xhrOptionsHandler);
		 * 
		 * registerHandler(rm, sockHandler, xhrRE, false, config); registerHandler(rm, sockHandler, xhrStreamRE, true, config);
		 * 
		 * String xhrSendRE = basePath + COMMON_PATH_ELEMENT_RE + "xhr_send";
		 * 
		 * rm.optionsWithRegEx(xhrSendRE, xhrOptionsHandler);
		 * 
		 * rm.postWithRegEx(xhrSendRE, new Handler<HttpServerRequest>() { public void handle(final HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("XHR send, post, " + req.uri); String sessionID = req.params().get("param0"); final Session session = sessions.get(sessionID); if (session != null) { handleSend(req, session); } else { req.response.statusCode = 404; setJSESSIONID(config, req); req.response.end(); } } });
		 */
	}

	private void registerHandler(Object rm, final Object sockHandler, String re, final boolean streaming, Object config) {
		/*
		 * rm.postWithRegEx(re, new Handler<HttpServerRequest>() { public void handle(final HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("XHR, post, " + req.uri); String sessionID = req.params().get("param0"); Session session = getSession(config.getLong("session_timeout"), config.getLong("heartbeat_period"), sessionID, sockHandler);
		 * 
		 * session.register(streaming ? new XhrStreamingListener(config .getInteger("max_bytes_streaming"), req, session) : new XhrPollingListener(req, session)); } });
		 */
	}

	private void handleSend(final Object req, final Session session) {
		/*
		 * req.bodyHandler(new Handler<Buffer>() { public void handle(Buffer buff) { String msgs = buff.toString();
		 * 
		 * if (msgs.equals("")) { req.response.statusCode = 500; req.response.end("Payload expected."); return; }
		 * 
		 * if (!session.handleMessages(msgs)) { sendInvalidJSON(req.response); } else { req.response.headers().put("Content-Type", "text/plain; charset=UTF-8"); setJSESSIONID(config, req); setCORS(req); req.response.statusCode = 204; req.response.end(); } if (logger.isTraceEnabled()) logger.trace("XHR send processed ok"); } });
		 */
	}

	private abstract class BaseXhrListener extends BaseListener {
		final Object req;
		final Session session;

		boolean headersWritten;

		BaseXhrListener(Object req, Session session) {
			this.req = req;
			this.session = session;
		}

		public void sendFrame(String body) {
			/*
			 * if (logger.isTraceEnabled()) logger.trace("XHR sending frame"); if (!headersWritten) { req.response.headers().put("Content-Type", "application/javascript; charset=UTF-8"); setJSESSIONID(config, req); setCORS(req); req.response.setChunked(true); headersWritten = true; }
			 */
		}

		public void close() {
		}
	}

	private class XhrPollingListener extends BaseXhrListener {

		XhrPollingListener(Object req, final Session session) {
			super(req, session);
			// addCloseHandler(req.response, session);
		}

		boolean closed;

		public void sendFrame(String body) {
			super.sendFrame(body);
			// req.response.write(body + "\n");
			close();
		}

		public void close() {
			if (logger.isTraceEnabled())
				logger.trace("XHR poll closing listener");
			if (!closed) {
				try {
					session.resetListener();
					// req.response.end();
					// req.response.close();
					closed = true;
				} catch (IllegalStateException e) {
					// Underlying connection might alreadu be closed - that's
					// fine
				}
			}
		}
	}

	private class XhrStreamingListener extends BaseXhrListener {

		int bytesSent;
		int maxBytesStreaming;
		boolean closed;

		XhrStreamingListener(int maxBytesStreaming, Object req, final Session session) {
			super(req, session);
			/*
			 * this.maxBytesStreaming = maxBytesStreaming; addCloseHandler(req.response, session);
			 */
		}

		public void sendFrame(String body) {
			/*
			 * boolean hr = headersWritten; super.sendFrame(body); if (!hr) { req.response.write(H_BLOCK); } String sbody = body + "\n"; Buffer buff = new Buffer(sbody); req.response.write(buff); bytesSent += buff.length(); if (bytesSent >= maxBytesStreaming) { close(); }
			 */
		}

		public void close() {
			/*
			 * if (logger.isTraceEnabled()) logger.trace("XHR stream closing listener"); if (!closed) { session.resetListener(); try { req.response.end(); req.response.close(); closed = true; } catch (IllegalStateException e) { // Underlying connection might alreadu be closed - that's // fine } }
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
