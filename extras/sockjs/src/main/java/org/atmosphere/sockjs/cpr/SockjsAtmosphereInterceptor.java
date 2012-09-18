package org.atmosphere.sockjs.cpr;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.atmosphere.config.service.AtmosphereInterceptorService;
import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AsyncIOWriter;
import org.atmosphere.cpr.AsyncIOWriterAdapter;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereInterceptor;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.sockjs.SockjsSessionManager;
import org.atmosphere.sockjs.SockjsSessionOutbound;
import org.atmosphere.sockjs.transport.IFrameTransport;
import org.atmosphere.sockjs.transport.SockjsPacketImpl;
import org.atmosphere.sockjs.transport.SockjsSessionManagerImpl;
import org.atmosphere.sockjs.transport.Transport;
import org.atmosphere.sockjs.transport.WebSocketTransport;
import org.atmosphere.sockjs.transport.XHRPollingTransport;
import org.atmosphere.sockjs.transport.XHRStreamingTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sockjs implementation.
 *
 * @author Sebastien Dionne
 */
@AtmosphereInterceptorService
public class SockjsAtmosphereInterceptor implements AtmosphereInterceptor {

	public final static String SOCKJS_PACKET = SockjsSessionManagerImpl.SockjsProtocol.class.getName();
	
    private static final Logger logger = LoggerFactory.getLogger(SockjsAtmosphereInterceptor.class);
    private SockjsSessionManager sessionManager = null;
    private static final int BUFFER_SIZE_DEFAULT = 8192;
    private static int heartbeatInterval = 15000;
    private static int timeout = 25000;
    private static int suspendTime = 20000;
    private final Map<String, Transport> transports = new HashMap<String, Transport>();
    private String availableTransports = "WEBSOCKET, EVENT_SOURCE, HTML_FILE, JSON_P, XHR";
    
    
    private static final Pattern SERVER_SESSION = Pattern.compile("^/([^/.]+)/([^/.]+)/");
    
    @Override
    public String toString() {
        return "Sockjs-Support";
    }

    @Override
    public Action inspect(AtmosphereResource r) {
        final AtmosphereRequest request = r.getRequest();
        final AtmosphereResponse response = r.getResponse();

        final AtmosphereHandler atmosphereHandler = (AtmosphereHandler) request.getAttribute(FrameworkConfig.ATMOSPHERE_HANDLER);
        try {
            
        	// find the transport
            String path = request.getPathInfo();
        	
        	String protocol =  null;
        	

        	String test = "http://sockjs-node.cloudfoundry.com/broadcast/827/4uq2et0_/xhr_streaming";
        	
        	System.out.println(" Sockjs Path = " + path);
        	
        	if(path.endsWith("/info")){
        		// pas de session
        		response.write("{\"websocket\":true,\"origins\":[\"*:*\"],\"cookie_needed\":true,\"entropy\":4076681690,\"server_heartbeat_interval\":5000}");
        		
        		setCORS(request, response);
        		
        		response.flushBuffer();
        		return Action.RESUME;
        	} else if(path.endsWith("/iframe")){
        		// pas de session
        		
        		protocol = IFrameTransport.TRANSPORT_NAME;
        		
        	} else if(path.endsWith("/websocket")){
        		
        	} else if(path.endsWith("/xhr")){
        		
        		//protocol = XHRPollingTransport.TRANSPORT_NAME;
        		
        		/*
        		//handle
        		System.out.println("xhr_send received = " + decodePostData(request.getContentType(), extractString(request.getReader())));
        		
        		response.addHeader("Content-Type", "text/plain; charset=UTF-8");
        		setJSESSIONID(request);
                setCORS(request, response);
                response.setStatus(204);
                return Action.CANCELLED;
                */
        	} else if(path.endsWith("/xhr_send")){
        		
        		protocol = XHRPollingTransport.TRANSPORT_NAME;
        		
        		/*
        		//handle
        		System.out.println("xhr_send received = " + decodePostData(request.getContentType(), extractString(request.getReader())));
        		
        		response.addHeader("Content-Type", "text/plain; charset=UTF-8");
        		setJSESSIONID(request);
                setCORS(request, response);
                response.setStatus(204);
                return Action.CANCELLED;
                */
        	} else if(path.endsWith("/xhr_streaming")){
        		
        		protocol = XHRStreamingTransport.TRANSPORT_NAME;
        		/*
        		setCORS(request, response);
        		
        		byte[] bytes = new byte[2048 + 1];
        	    for (int i = 0; i < bytes.length; i++) {
        	      bytes[i] = (byte)'h';
        	    }
        	    bytes[bytes.length - 1] = (byte)'\n';
        	    
        	    response.addHeader("Content-Type", "application/javascript; charset=UTF-8");
        	    
        	    response.write(bytes);
        	    
        	    response.write("o\n");
        	    
        	    response.flushBuffer();
        		
        		r.suspend(-1, false);
        		return Action.SUSPEND;
        		*/
        	} else if(path.endsWith("/xhr")){
        		
        	} else if(path.endsWith("/jsonp_send")){
        		
        	} else if(path.endsWith("/jsonp")){
        		
        	} else if(path.endsWith("/eventsource")){
        		
        	} else if(path.endsWith("/htmlfile")){
        		
        	}
            
            final Transport transport = transports.get(protocol);
            if (transport != null) {
            	
                if (!SockjsAtmosphereHandler.class.isAssignableFrom(atmosphereHandler.getClass())) {
                    response.asyncIOWriter(new AsyncIOWriterAdapter() {
                        @Override
                        public AsyncIOWriter write(AtmosphereResponse r, String data) throws IOException {
                        	SockjsSessionOutbound outbound = (SockjsSessionOutbound)
                                    request.getAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_OUTBOUND);
                            SockjsSessionManagerImpl.SockjsProtocol p = (SockjsSessionManagerImpl.SockjsProtocol)
                                    r.request().getAttribute(SOCKJS_PACKET);

                            String msg = p == null ? data : SockjsSessionManagerImpl.mapper.writeValueAsString(p.clearArgs().addArgs(data));

                            if (outbound != null) {
                                outbound.sendMessage(new SockjsPacketImpl(msg));
                            } else {
                                r.getResponse().getOutputStream().write(msg.getBytes(r.getCharacterEncoding()));
                            }
                            return this;
                        }

                        @Override
                        public AsyncIOWriter write(AtmosphereResponse r, byte[] data) throws IOException {
                            SockjsSessionManagerImpl.SockjsProtocol p = (SockjsSessionManagerImpl.SockjsProtocol)
                                    r.request().getAttribute(SOCKJS_PACKET);
                            if (p == null) {
                                r.getResponse().getOutputStream().write(data);
                            } else {
                            	write(r, new String(data, r.request().getCharacterEncoding()));
                            }
                            return this;
                        }

                        @Override
                        public AsyncIOWriter write(AtmosphereResponse r, byte[] data, int offset, int length) throws IOException {
                            SockjsSessionManagerImpl.SockjsProtocol p = (SockjsSessionManagerImpl.SockjsProtocol)
                                    r.request().getAttribute(SOCKJS_PACKET);
                            if (p == null) {
                                r.getResponse().getOutputStream().write(data, offset, length);
                            } else {
                                write(r, new String(data, offset, length, r.request().getCharacterEncoding()));
                            }
                            return this;
                        }

                        @Override
                        public AsyncIOWriter flush(AtmosphereResponse r) throws IOException {
                            try {
                                r.getResponse().getOutputStream().flush();
                            } catch (IllegalStateException ex) {
                                r.getResponse().getWriter().flush();
                            }
                            return this;
                        }

                        @Override
                        public AsyncIOWriter writeError(AtmosphereResponse r, int errorCode, String message) throws IOException {
                            ((HttpServletResponse) r.getResponse()).sendError(errorCode, message);
                            return this;
                        }

                        @Override
                        public void close(AtmosphereResponse r) throws IOException {
                            try {
                                r.getResponse().getOutputStream().close();
                            } catch (IllegalStateException ex) {
                                r.getResponse().getWriter().close();
                            }
                        }
                    });
                }
                
                return transport.handle((AtmosphereResourceImpl) r, atmosphereHandler, sessionManager);
            } else {
                logger.error("Protocol not supported : " + protocol);
            }
            
        } catch (Exception e) {
            logger.error("", e);
        }
        return Action.CONTINUE;
    }
    
    /*
	  rm.getWithRegEx(prefix + "\\/?", new Handler<HttpServerRequest>() {
public void handle(HttpServerRequest req) {
if (logger.isTraceEnabled()) logger.trace("Returning welcome response");
req.response.headers().put("Content-Type", "text/plain; charset=UTF-8");
req.response.end("Welcome to SockJS!\n");
}
});

// Iframe handlers
String iframeHTML = IFRAME_TEMPLATE.replace("{{ sockjs_url }}", config.getString("library_url"));
Handler<HttpServerRequest> iframeHandler = createIFrameHandler(iframeHTML);

// Request exactly for iframe.html
rm.getWithRegEx(prefix + "\\/iframe\\.html", iframeHandler);

// Versioned
rm.getWithRegEx(prefix + "\\/iframe-[^\\/]*\\.html", iframeHandler);

// Chunking test
rm.postWithRegEx(prefix + "\\/chunking_test", createChunkingTestHandler());
rm.optionsWithRegEx(prefix + "\\/chunking_test", AbstractTransport.createCORSOptionsHandler(config, "OPTIONS, POST"));

// Info
rm.getWithRegEx(prefix + "\\/info", AbstractTransport.createInfoHandler(config));
rm.optionsWithRegEx(prefix + "\\/info", AbstractTransport.createCORSOptionsHandler(config, "OPTIONS, GET"));

	 */
    
    
    static void setJSESSIONID(AtmosphereRequest req) {
        
        /*
        String cookies = req.headers().get("cookie");
        
        if (config.getBoolean("insert_JSESSIONID")) {
          //Preserve existing JSESSIONID, if any
          if (cookies != null) {
            String[] parts;
            if (cookies.contains(";")) {
              parts = cookies.split(";");
            } else {
              parts = new String[] {cookies};
            }
            for (String part: parts) {
              if (part.startsWith("JSESSIONID")) {
                cookies = part + "; path=/";
                break;
              }
            }
          }
          if (cookies == null) {
            cookies = "JSESSIONID=dummy; path=/";
          }
          req.response.headers().put("Set-Cookie", cookies);
        }
        */
      }

      static void setCORS(AtmosphereRequest request, AtmosphereResponse response) {
    	  String origin = request.getHeader("origin"); 
  		
  		if (origin == null) { 
  			origin = "*"; 
  		} 
  		
  		response.addHeader("Content-Type", "application/json; charset=UTF-8");
        response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.addHeader("Access-Control-Allow-Origin", origin); 
  		response.addHeader("Access-Control-Allow-Credentials", "true"); 
  		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
  		
      }
      
      
      public static String extractString(Reader reader) {

          String output = null;
          try {
              Writer writer = new StringWriter();

              char[] buffer = new char[1024];
              int n;
              while ((n = reader.read(buffer)) != -1) {
                  writer.write(buffer, 0, n);
              }
              output = writer.toString();
          } catch (Exception e) {
          }
          return output;

      }
      
      protected String decodePostData(String contentType, String data) {
          if (contentType == null || contentType.startsWith("application/x-www-form-urlencoded")) {
              if (data.length() > 2 && data.substring(0, 2).startsWith("d=")) {
                  String extractedData = data.substring(3);
                  try {
                      extractedData = URLDecoder.decode(extractedData, "UTF-8");
                      if (extractedData != null && extractedData.length() > 2) {
                          // trim and replace \" by "
                          if (extractedData.charAt(0) == '\"' && extractedData.charAt(extractedData.length() - 1) == '\"') {

                              extractedData = extractedData.substring(1, extractedData.length() - 1).replaceAll("\\\\\"", "\"");
                          }
                      }

                  } catch (UnsupportedEncodingException e) {
                      logger.trace("",e);
                  }
                  return extractedData;
              } else {
                  return data;
              }
          } else if (contentType.startsWith("text/plain")) {
              return data;
          } else {
              return data;
          }
      }

    @Override
    public void postInspect(AtmosphereResource r) {
    }

    @Override
    public void configure(AtmosphereConfig config) {
        /*
    	String s = config.getInitParameter(SOCKETIO_TRANSPORT);
        availableTransports = s;

        String timeoutWebXML = config.getInitParameter(SOCKETIO_TIMEOUT);
        if (timeoutWebXML != null) {
            timeout = Integer.parseInt(timeoutWebXML);
        }

        String heartbeatWebXML = config.getInitParameter(SOCKETIO_HEARTBEAT);
        if (heartbeatWebXML != null) {
            heartbeatInterval = Integer.parseInt(heartbeatWebXML);
        }

        String suspendWebXML = config.getInitParameter(SOCKETIO_SUSPEND);
        if (suspendWebXML != null) {
            suspendTime = Integer.parseInt(suspendWebXML);
        }
		*/
    	
    	transports.put(WebSocketTransport.TRANSPORT_NAME, new WebSocketTransport());
    	transports.put(XHRPollingTransport.TRANSPORT_NAME, new XHRPollingTransport(BUFFER_SIZE_DEFAULT));
    	transports.put(XHRStreamingTransport.TRANSPORT_NAME, new XHRStreamingTransport(BUFFER_SIZE_DEFAULT));
    	//transports.put(IFrameTransport.TRANSPORT_NAME, new IFrameTransport());
    	
    	
    	sessionManager = new SockjsSessionManagerImpl();
        sessionManager.setTimeout(timeout);
        sessionManager.setHeartbeatInterval(heartbeatInterval);
        sessionManager.setRequestSuspendTime(suspendTime);
    	
    }
}
