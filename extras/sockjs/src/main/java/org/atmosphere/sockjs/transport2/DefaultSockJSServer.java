package org.atmosphere.sockjs.transport2;

import java.security.MessageDigest;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class DefaultSockJSServer {

	private final Logger logger = LoggerFactory.getLogger(DefaultSockJSServer.class);

	private Object vertx = null;
	private Object rm = new Object();
	private Object wsMatcher = new Object();
	private Map<String, Session> sessions = null;

	private Object bridgeHook = null;

	public DefaultSockJSServer(Object vertx, Object httpServer) {

		this.vertx = vertx;
		/*
		 * this.sessions = vertx.sharedData().getMap("_vertx.sockjssessions"); // Any previous request and websocket handlers will become default // handlers // if nothing else matches rm.noMatch(httpServer.requestHandler()); wsMatcher.noMatch(new Handler<WebSocketMatcher.Match>() { Handler<ServerWebSocket> wsHandler = httpServer.websocketHandler();
		 * 
		 * public void handle(WebSocketMatcher.Match match) { wsHandler.handle(match.ws); } });
		 * 
		 * httpServer.requestHandler(rm); httpServer.websocketHandler(wsMatcher);
		 */
	}

	private Object setDefaults(Object config) {
		/*
		 * config = config.copy(); // Set the defaults if (config.getNumber("session_timeout") == null) { config.putNumber("session_timeout", 5l * 60 * 1000); } if (config.getBoolean("insert_JSESSIONID") == null) { config.putBoolean("insert_JSESSIONID", true); } if (config.getNumber("heartbeat_period") == null) { config.putNumber("heartbeat_period", 25l * 1000); } if (config.getNumber("max_bytes_streaming") == null) { config.putNumber("max_bytes_streaming", 128 * 1024); } if
		 * (config.getString("prefix") == null) { config.putString("prefix", "/"); } if (config.getString("library_url") == null) { config.putString("library_url", "http://cdn.sockjs.org/sockjs-0.2.1.min.js"); } if (config.getArray("disabled_transports") == null) { config.putArray("disabled_transports", new JsonArray()); } return config;
		 */
		return null;
	}

	public void installApp(Object config, final Object sockHandler) {
		/*
		 * config = setDefaults(config);
		 * 
		 * String prefix = config.getString("prefix");
		 * 
		 * if (prefix == null || prefix.equals("") || prefix.endsWith("/")) { throw new IllegalArgumentException("Invalid prefix: " + prefix); }
		 * 
		 * // Base handler for app
		 * 
		 * rm.getWithRegEx(prefix + "\\/?", new Handler<HttpServerRequest>() { public void handle(HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("Returning welcome response"); req.response.headers().put("Content-Type", "text/plain; charset=UTF-8"); req.response.end("Welcome to SockJS!\n"); } });
		 * 
		 * // Iframe handlers String iframeHTML = IFRAME_TEMPLATE.replace("{{ sockjs_url }}", config.getString("library_url")); Handler<HttpServerRequest> iframeHandler = createIFrameHandler(iframeHTML);
		 * 
		 * // Request exactly for iframe.html rm.getWithRegEx(prefix + "\\/iframe\\.html", iframeHandler);
		 * 
		 * // Versioned rm.getWithRegEx(prefix + "\\/iframe-[^\\/]*\\.html", iframeHandler);
		 * 
		 * // Chunking test rm.postWithRegEx(prefix + "\\/chunking_test", createChunkingTestHandler()); rm.optionsWithRegEx(prefix + "\\/chunking_test", AbstractTransport.createCORSOptionsHandler(config, "OPTIONS, POST"));
		 * 
		 * // Info rm.getWithRegEx(prefix + "\\/info", AbstractTransport.createInfoHandler(config)); rm.optionsWithRegEx(prefix + "\\/info", AbstractTransport.createCORSOptionsHandler(config, "OPTIONS, GET"));
		 * 
		 * // Transports
		 * 
		 * Set<String> enabledTransports = new HashSet<>(); enabledTransports.add(Transport.EVENT_SOURCE.toString()); enabledTransports.add(Transport.HTML_FILE.toString()); enabledTransports.add(Transport.JSON_P.toString()); enabledTransports.add(Transport.WEBSOCKET.toString()); enabledTransports.add(Transport.XHR.toString()); for (Object tr : config.getArray("disabled_transports", new JsonArray())) { enabledTransports.remove(tr); }
		 * 
		 * if (enabledTransports.contains(Transport.XHR.toString())) { new XhrTransport(vertx, rm, prefix, sessions, config, sockHandler); } if (enabledTransports.contains(Transport.EVENT_SOURCE.toString())) { new EventSourceTransport(vertx, rm, prefix, sessions, config, sockHandler); } if (enabledTransports.contains(Transport.HTML_FILE.toString())) { new HtmlFileTransport(vertx, rm, prefix, sessions, config, sockHandler); } if (enabledTransports.contains(Transport.JSON_P.toString())) { new
		 * JsonPTransport(vertx, rm, prefix, sessions, config, sockHandler); } if (enabledTransports.contains(Transport.WEBSOCKET.toString())) { new WebSocketTransport(vertx, wsMatcher, rm, prefix, sessions, config, sockHandler); new RawWebSocketTransport(vertx, wsMatcher, rm, prefix, sockHandler); } // Catch all for any other requests on this app
		 * 
		 * rm.getWithRegEx(prefix + "\\/.+", new Handler<HttpServerRequest>() { public void handle(HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("Request: " + req.uri + " does not match, returning 404"); req.response.statusCode = 404; req.response.end(); } });
		 */
	}

	private Object createChunkingTestHandler() {

		return null;
		/*
		 * return new Handler<HttpServerRequest>() {
		 * 
		 * class TimeoutInfo { final long timeout; final Buffer buff;
		 * 
		 * TimeoutInfo(long timeout, Buffer buff) { this.timeout = timeout; this.buff = buff; } }
		 * 
		 * private void setTimeout(List<TimeoutInfo> timeouts, long delay, final Object buff) { timeouts.add(new TimeoutInfo(delay, buff)); }
		 * 
		 * private void runTimeouts(List<TimeoutInfo> timeouts, Object response) { final Iterator<TimeoutInfo> iter = timeouts.iterator(); nextTimeout(timeouts, iter, response); }
		 * 
		 * private void nextTimeout(final List<TimeoutInfo> timeouts, final Iterator<TimeoutInfo> iter, final HttpServerResponse response) { if (iter.hasNext()) { final TimeoutInfo timeout = iter.next(); vertx.setTimer(timeout.timeout, new Handler<Long>() { public void handle(Long id) { response.write(timeout.buff); nextTimeout(timeouts, iter, response); } }); } else { timeouts.clear(); } }
		 * 
		 * public void handle(Object req) {
		 * 
		 * req.response.headers().put("Content-Type", "application/javascript; charset=UTF-8");
		 * 
		 * AbstractTransport.setCORS(req); req.response.setChunked(true);
		 * 
		 * Buffer h = new Buffer(2); h.appendString("h\n");
		 * 
		 * Buffer hs = new Buffer(2050); for (int i = 0; i < 2048; i++) { hs.appendByte((byte) ' '); } hs.appendString("h\n");
		 * 
		 * List<TimeoutInfo> timeouts = new ArrayList<>();
		 * 
		 * setTimeout(timeouts, 0, h); setTimeout(timeouts, 1, hs); setTimeout(timeouts, 5, h); setTimeout(timeouts, 25, h); setTimeout(timeouts, 125, h); setTimeout(timeouts, 625, h); setTimeout(timeouts, 3125, h);
		 * 
		 * runTimeouts(timeouts, req.response);
		 * 
		 * } };
		 */
	}

	private Object createIFrameHandler(final String iframeHTML) {
		final String etag = getMD5String(iframeHTML);

		return null;
		/*
		 * return new Handler<HttpServerRequest>() { public void handle(Object req) { try { if (logger.isTraceEnabled()) logger.trace("In Iframe handler"); if (etag != null && etag.equals(req.headers().get("if-none-match"))) { req.response.statusCode = 304; req.response.end(); } else { req.response.headers().put("Content-Type", "text/html; charset=UTF-8"); req.response.headers().put("Cache-Control", "public,max-age=31536000"); long oneYear = 365 * 24 * 60 * 60 * 1000; String expires = new
		 * SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz") .format(new Date(System.currentTimeMillis() + oneYear)); req.response.headers().put("Expires", expires); req.response.headers().put("ETag", etag); req.response.end(iframeHTML); } } catch (Exception e) { logger.error("Failed to server iframe", e); } } };
		 */
	}

	private String getMD5String(final String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(str.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(Integer.toHexString(b + 127));
			}
			return sb.toString();
		} catch (Exception e) {
			logger.error("Failed to generate MD5 for iframe, If-None-Match headers will be ignored");
			return null;
		}
	}

	private static final String IFRAME_TEMPLATE = "<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" + "  <script>\n" + "    document.domain = document.domain;\n" + "    _sockjs_onload = function(){SockJS.bootstrap_iframe();};\n" + "  </script>\n" + "  <script src=\"{{ sockjs_url }}\"></script>\n" + "</head>\n" + "<body>\n"
			+ "  <h2>Don't panic!</h2>\n" + "  <p>This is a SockJS hidden iframe. It's used for cross domain magic.</p>\n" + "</body>\n" + "</html>";

}
