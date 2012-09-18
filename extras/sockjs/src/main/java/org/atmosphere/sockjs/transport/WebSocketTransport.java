/*
 * Copyright 2012 Sebastien Dionne
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.sockjs.transport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.sockjs.SockjsException;
import org.atmosphere.sockjs.SockjsPacket;
import org.atmosphere.sockjs.SockjsSession;
import org.atmosphere.sockjs.SockjsSessionFactory;
import org.atmosphere.sockjs.SockjsWebSocketSessionWrapper;
import org.atmosphere.sockjs.cpr.SockjsAtmosphereHandler;
import org.atmosphere.sockjs.cpr.SockjsWebSocketEventListener;
import org.atmosphere.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 * @author Jeanfrancois Arcand
 */
public class WebSocketTransport extends AbstractTransport {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketTransport.class);
    public static final String TRANSPORT_NAME = "websocket";

    public WebSocketTransport() {
    }

    @Override
    public String getName() {
        return TRANSPORT_NAME;
    }

    @Override
    public Action handle(AtmosphereResourceImpl resource, AtmosphereHandler atmosphereHandler, SockjsSessionFactory sessionFactory) throws IOException {

        AtmosphereRequest request = resource.getRequest();

        Object obj = request.getAttribute(SESSION_KEY);
        SockjsSession session = null;
        String sessionId = null;
        if (obj != null) {
            session = (SockjsSession) obj;
        } else {
            sessionId = extractSessionId(request);
            if (sessionId != null && sessionId.length() > 0) {
                session = sessionFactory.getSession(sessionId);
            }
        }

        boolean isDisconnectRequest = isDisconnectRequest(request);
        if (!isDisconnectRequest) {
            if ("GET".equals(request.getMethod()) && "WebSocket".equalsIgnoreCase(request.getHeader("Upgrade"))) {
                session = sessionFactory.getSession(sessionId);

                request.setAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_ID, session.getSessionId());

                // add a default websocketListener
                SockjsWebSocketEventListener socketioEventListener = new SockjsWebSocketEventListener();
                resource.addEventListener(socketioEventListener);

                SocketIOWebSocketSessionWrapperImpl sessionWrapper = new SocketIOWebSocketSessionWrapperImpl(session, socketioEventListener);
                socketioEventListener.setSessionWrapper(sessionWrapper);
                request.setAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_OUTBOUND, sessionWrapper);
                resource.suspend(-1, false);
            }
        } else {
            session = sessionFactory.getSession(sessionId);
            session.getTransportHandler().disconnect();
        }

        return Action.CANCELLED;
    }

    public class SocketIOWebSocketSessionWrapperImpl implements SockjsWebSocketSessionWrapper {
        public final SockjsSession session;
        public final SockjsWebSocketEventListener socketioEventListener;
        public boolean initiated = false;
        public WebSocket webSocket;

        SocketIOWebSocketSessionWrapperImpl(SockjsSession session, SockjsWebSocketEventListener socketioEventListener) {
            this.session = session;
            this.socketioEventListener = socketioEventListener;
            this.socketioEventListener.setSessionWrapper(this);
        }

        /*
        * (non-Javadoc)
        * @see org.eclipse.jetty.websocket.WebSocket#onDisconnect()
        */
        public void onDisconnect() {
            logger.trace("calling from " + this.getClass().getName() + " : " + "onDisconnect");
            session.onShutdown();
        }

        /*
           * (non-Javadoc)
           * @see org.eclipse.jetty.websocket.WebSocket#onMessage(byte, java.lang.String)
           */
        public void onMessage(byte frame, String message) {
            logger.trace("calling from " + this.getClass().getName() + " : " + "onMessage");
            throw new RuntimeException();
        }

        /*
           * (non-Javadoc)
           * @see org.eclipse.jetty.websocket.WebSocket#onMessage(byte, byte[], int, int)
           */
        public void onMessage(byte frame, byte[] data, int offset, int length) {
            logger.trace("calling from " + this.getClass().getName() + " : " + "onMessage frame, data, offest, length");
            try {
                onMessage(frame, new String(data, offset, length, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Do nothing for now.
            }
        }

        /*
           * (non-Javadoc)
           * @see com.glines.socketio.SocketIOInbound.SocketIOOutbound#disconnect()
           */
        @Override
        public void disconnect() {
            logger.trace("calling from " + this.getClass().getName() + " : " + "disconnect");
            session.onDisconnect(DisconnectReason.DISCONNECT);
            webSocket.close();
        }

        @Override
        public void close() {
            logger.trace("calling from " + this.getClass().getName() + " : " + "close");
            session.startClose();
        }

        @Override
        public void sendMessage(SockjsPacket packet) throws SockjsException {
            if (packet != null) {
                sendMessage(packet.toString());
            }
        }

        @Override
        public void sendMessage(List<SockjsPacketImpl> messages) throws SockjsException {
            if (messages != null) {
                for (SockjsPacketImpl msg : messages) {
                     sendMessage(msg.toString());
                }
            }
        }

        /*
           * (non-Javadoc)
           * @see com.glines.socketio.SocketIOInbound.SocketIOOutbound#sendMessage(java.lang.String)
           */
        @Override
        public void sendMessage(String message) throws SockjsException {
            logger.trace("calling from " + this.getClass().getName() + " : " + "sendMessage(string) = " + message);

            if (webSocket != null) {
                try {
                    webSocket.write(message);
                    logger.trace("WRITE SUCCESS : calling from " + this.getClass().getName() + " : " + "sendMessage(string) = " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.warn("WebSOCKET NULL");
            }


        }

        /*
           * (non-Javadoc)
           * @see com.glines.socketio.SocketIOSession.SocketIOSessionOutbound#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, com.glines.socketio.SocketIOSession)
           */
        @Override
        public Action handle(AtmosphereRequest request, AtmosphereResponse response, SockjsSession session) throws IOException {

            logger.trace("calling from " + this.getClass().getName() + " : " + "handle");

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unexpected request on upgraded WebSocket connection");
            return Action.CONTINUE;
        }

        @Override
        public void abort() {
            logger.trace("calling from " + this.getClass().getName() + " : " + "abort");
            webSocket.close();
            webSocket = null;
            session.onShutdown();
        }

        @Override
        public SockjsSession getSession() {
            return session;
        }

        @Override
        public boolean isInitiated() {
            return initiated;
        }

        @Override
        public WebSocket webSocket() {
            return webSocket;
        }

        @Override
        public void setWebSocket(WebSocket websocket) {
            this.webSocket = websocket;
        }

        @Override
        public void initiated(boolean initiated) {
            this.initiated = initiated;
        }

        @Override
        public String getSessionId() {
            return session.getSessionId();
        }

    }

	@Override
	public boolean isStreamingConnection() {
		return true;
	}
}
