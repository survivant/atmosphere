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
class JsonPTransport extends AbstractTransport {

	private final Logger logger = LoggerFactory.getLogger(JsonPTransport.class);

	JsonPTransport(Object vertx, Object rm, String basePath, final Map<String, Session> sessions, Object config, final Object sockHandler) {

		String jsonpRE = basePath + COMMON_PATH_ELEMENT_RE + "jsonp";
		/*
		 * rm.getWithRegEx(jsonpRE, new Handler<HttpServerRequest>() { public void handle(final HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("JsonP, get: " + req.uri); String callback = req.params().get("callback"); if (callback == null) { callback = req.params().get("c"); if (callback == null) { req.response.statusCode = 500; req.response.end("\"callback\" parameter required\n"); return; } }
		 * 
		 * String sessionID = req.params().get("param0"); Session session = getSession(config.getLong("session_timeout"), config.getLong("heartbeat_period"), sessionID, sockHandler); session.register(new JsonPListener(req, session, callback)); } });
		 * 
		 * String jsonpSendRE = basePath + COMMON_PATH_ELEMENT_RE + "jsonp_send";
		 * 
		 * rm.postWithRegEx(jsonpSendRE, new Handler<HttpServerRequest>() { public void handle(final HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("JsonP, post: " + req.uri); String sessionID = req.params().get("param0"); final Session session = sessions.get(sessionID); if (session != null) { handleSend(req, session); } else { req.response.statusCode = 404; setJSESSIONID(config, req); req.response.end(); } } });
		 */
	}

	private void handleSend(final Object req, final Session session) {
		/*
		 * req.bodyHandler(new Handler<Buffer>() {
		 * 
		 * public void handle(Buffer buff) { String body = buff.toString();
		 * 
		 * boolean urlEncoded; String ct = req.headers().get("content-type"); if ("application/x-www-form-urlencoded".equalsIgnoreCase(ct)) { urlEncoded = true; } else if ("text/plain".equalsIgnoreCase(ct)) { urlEncoded = false; } else { req.response.statusCode = 500; req.response.end("Invalid Content-Type"); return; }
		 * 
		 * if (body.equals("") || urlEncoded && (!body.startsWith("d=") || body.length() <= 2)) { req.response.statusCode = 500; req.response.end("Payload expected."); return; }
		 * 
		 * if (urlEncoded) { try { body = URLDecoder.decode(body, "UTF-8"); } catch (UnsupportedEncodingException e) { throw new IllegalStateException("No UTF-8!"); } body = body.substring(2); }
		 * 
		 * if (!session.handleMessages(body)) { sendInvalidJSON(req.response); } else { setJSESSIONID(config, req); req.response.headers().put("Content-Type", "text/plain; charset=UTF-8"); req.response.end("ok"); if (logger.isTraceEnabled()) logger.trace("send handled ok"); } } });
		 */
	}

	private class JsonPListener extends BaseListener {

		Object req;
		Session session;
		String callback;
		boolean headersWritten;
		boolean closed;

		JsonPListener(Object req, Session session, String callback) {
			this.req = req;
			this.session = session;
			this.callback = callback;
			// addCloseHandler(req.response, session);
		}

		public void sendFrame(String body) {
			/*
			 * if (logger.isTraceEnabled()) logger.trace("JsonP, sending frame");
			 * 
			 * if (!headersWritten) { req.response.setChunked(true); req.response.headers().put("Content-Type", "application/javascript; charset=UTF-8"); req.response.headers().put("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0"); setJSESSIONID(config, req); headersWritten = true; }
			 * 
			 * body = escapeForJavaScript(body);
			 * 
			 * StringBuilder sb = new StringBuilder(); sb.append(callback).append("(\""); sb.append(body); sb.append("\");\r\n");
			 * 
			 * // End the response and close the HTTP connection
			 * 
			 * req.response.write(sb.toString()); close();
			 */
		}

		public void close() {
			/*
			 * if (!closed) { try { session.resetListener(); req.response.end(); req.response.close(); closed = true; } catch (IllegalStateException e) { // Underlying connection might alreadu be closed - that's // fine } }
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
