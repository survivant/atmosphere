package org.atmosphere.sockjs.transport2;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransport implements Transport {

	private final Logger logger = LoggerFactory.getLogger(AbstractTransport.class);

	protected final Map<String, Session> sessions = null;
	// protected JsonObject config;
	
	protected ObjectMapper mapper = new ObjectMapper();
	

	protected static final String COMMON_PATH_ELEMENT_RE = "\\/[^\\/\\.]+\\/([^\\/\\.]+)\\/";

	private static final long RAND_OFFSET = 2l << 30;

	protected Session getSession(final long timeout, final long heartbeatPeriod, final String sessionID, Object sockHandler) {
		Session session = sessions.get(sessionID);
		if (session == null) {
			session = new Session(null, sessions, sessionID, timeout, heartbeatPeriod, sockHandler);
			sessions.put(sessionID, session);
		}
		return session;
	}

	protected void sendInvalidJSON(Object response) {
		/*
		 * if (logger.isTraceEnabled()) logger.trace("Broken JSON"); response.statusCode = 500; response.end("Broken JSON encoding.");
		 */
	}

	protected String escapeForJavaScript(String str) {
		/*
		 * try { str = StringEscapeUtils.escapeJavaScript(str); } catch (Exception e) { logger.error("Failed to escape", e); str = null; } return str;
		 */
		return null;
	}

	protected static abstract class BaseListener implements TransportListener {

		protected void addCloseHandler(Object resp, final Session session) {
			/*
			 * resp.closeHandler(new SimpleHandler() { public void handle() { if (logger.isTraceEnabled()) logger.trace("Connection closed (from client?), closing session"); // Connection has been closed fron the client or network // error so // we remove the session session.shutdown(); close(); } });
			 */
		}

		public void sessionClosed() {
		}
	}

	static void setJSESSIONID(Object config, Object req) {
		/*
		 * String cookies = req.headers().get("cookie"); if (config.getBoolean("insert_JSESSIONID")) { // Preserve existing JSESSIONID, if any if (cookies != null) { String[] parts; if (cookies.contains(";")) { parts = cookies.split(";"); } else { parts = new String[] { cookies }; } for (String part : parts) { if (part.startsWith("JSESSIONID")) { cookies = part + "; path=/"; break; } } } if (cookies == null) { cookies = "JSESSIONID=dummy; path=/"; } req.response.headers().put("Set-Cookie",
		 * cookies); }
		 */
	}

	static void setCORS(Object req) {
		/*
		 * String origin = req.headers().get("origin"); if (origin == null) { origin = "*"; } req.response.headers().put("Access-Control-Allow-Origin", origin); req.response.headers().put("Access-Control-Allow-Credentials", "true"); req.response.headers().put("Access-Control-Allow-Headers", "Content-Type");
		 */
	}

	static Object createInfoHandler(final Object config) {
		/*
		 * return new Handler<HttpServerRequest>() { boolean websocket = !config.getArray("disabled_transports") .contains(Transport.WEBSOCKET.toString());
		 * 
		 * public void handle(HttpServerRequest req) { 
		 * 	if (logger.isTraceEnabled()) 
		 * 		logger.trace("In Info handler"); 
		 req.response.headers().put("Content-Type", "application/json; charset=UTF-8");
        req.response.headers().put("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        JsonObject json = new JsonObject();
        json.putBoolean("websocket", websocket);
        json.putBoolean("cookie_needed", config.getBoolean("insert_JSESSIONID"));
        json.putArray("origins", new JsonArray().add("*:*"));
        // Java ints are signed, so we need to use a long and add the offset so
        // the result is not negative
        json.putNumber("entropy", RAND_OFFSET + new Random().nextInt());
        setCORS(req);
        req.response.end(json.encode());
        
		 */
		return null;
	}

	static Object createCORSOptionsHandler(final Object config, final String methods) {
		/*
		 * return new Handler<HttpServerRequest>() { public void handle(HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("In CORS options handler"); req.response.headers().put("Cache-Control", "public,max-age=31536000"); long oneYearSeconds = 365 * 24 * 60 * 60; long oneYearms = oneYearSeconds * 1000; String expires = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date(System .currentTimeMillis() + oneYearms)); req.response.headers().put("Expires", expires);
		 * req.response.headers().put("Access-Control-Allow-Methods", methods); req.response.headers().put("Access-Control-Max-Age", String.valueOf(oneYearSeconds)); setCORS(req); setJSESSIONID(config, req); req.response.statusCode = 204; req.response.end(); } };
		 */
		return null;
	}
	
	@Override
    public void destroy() {
    }
}
