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
class HtmlFileTransport extends AbstractTransport {

	private final Logger logger = LoggerFactory.getLogger(HtmlFileTransport.class);

	private static final String HTML_FILE_TEMPLATE;

	static {
		String str = "<!doctype html>\n" + "<html><head>\n" + "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" + "</head><body><h2>Don't panic!</h2>\n" + "  <script>\n" + "    document.domain = document.domain;\n" + "    var c = parent.{{ callback }};\n" + "    c.start();\n" + "    function p(d) {c.message(d);};\n" + "    window.onload = function() {c.stop();};\n" + "  </script>";

		String str2 = str.replace("{{ callback }}", "");
		StringBuilder sb = new StringBuilder(str);
		int extra = 1024 - str2.length();
		for (int i = 0; i < extra; i++) {
			sb.append(' ');
		}
		sb.append("\r\n");
		HTML_FILE_TEMPLATE = sb.toString();
	}

	HtmlFileTransport(Object vertx, Object rm, String basePath, Map<String, Session> sessions, final Object config, final Object sockHandler) {
		// super(vertx, sessions, config);
		String htmlFileRE = basePath + COMMON_PATH_ELEMENT_RE + "htmlfile";

		/*
		 * rm.getWithRegEx(htmlFileRE, new Handler<HttpServerRequest>() { public void handle(final HttpServerRequest req) { if (logger.isTraceEnabled()) logger.trace("HtmlFile, get: " + req.uri); String callback = req.params().get("callback"); if (callback == null) { callback = req.params().get("c"); if (callback == null) { req.response.statusCode = 500; req.response.end("\"callback\" parameter required\n"); return; } }
		 * 
		 * String sessionID = req.params().get("param0"); Session session = getSession(config.getLong("session_timeout"), config.getLong("heartbeat_period"), sessionID, sockHandler); session.register(new HtmlFileListener(config .getInteger("max_bytes_streaming"), req, callback, session)); } });
		 */
	}

	private class HtmlFileListener extends BaseListener {

		final int maxBytesStreaming;
		final Object req;
		final String callback;
		final Session session;
		boolean headersWritten;
		int bytesSent;
		boolean closed;

		HtmlFileListener(int maxBytesStreaming, Object req, String callback, Session session) {
			this.maxBytesStreaming = maxBytesStreaming;
			this.req = req;
			this.callback = callback;
			this.session = session;
			// addCloseHandler(req.response, session);
		}

		public void sendFrame(String body) {
			/*
			 * if (logger.isTraceEnabled()) logger.trace("HtmlFile, sending frame"); if (!headersWritten) { String htmlFile = HTML_FILE_TEMPLATE.replace("{{ callback }}", callback); req.response.headers().put("Content-Type", "text/html; charset=UTF-8"); req.response.headers().put("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0"); req.response.setChunked(true); setJSESSIONID(config, req); req.response.write(htmlFile); headersWritten = true; } body = escapeForJavaScript(body);
			 * StringBuilder sb = new StringBuilder(); sb.append("<script>\np(\""); sb.append(body); sb.append("\");\n</script>\r\n"); Buffer buff = new Buffer(sb.toString()); req.response.write(buff); bytesSent += buff.length(); if (bytesSent >= maxBytesStreaming) { if (logger.isTraceEnabled()) logger.trace("More than maxBytes sent so closing connection"); // Reset and close the connection close(); }
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
