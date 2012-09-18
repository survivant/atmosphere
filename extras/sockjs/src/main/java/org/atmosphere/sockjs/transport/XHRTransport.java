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
import java.util.List;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.BroadcasterCache;
import org.atmosphere.cpr.DefaultBroadcaster;
import org.atmosphere.sockjs.SockjsClosedException;
import org.atmosphere.sockjs.SockjsException;
import org.atmosphere.sockjs.SockjsPacket;
import org.atmosphere.sockjs.SockjsSession;
import org.atmosphere.sockjs.SockjsSessionFactory;
import org.atmosphere.sockjs.SockjsSessionOutbound;
import org.atmosphere.sockjs.cpr.SockjsAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 * @author Jeanfrancois Arcand
 */
public abstract class XHRTransport extends AbstractTransport {

    private static final Logger logger = LoggerFactory.getLogger(XHRTransport.class);
    private final int bufferSize;
    
    protected boolean isStreamingConnection;
    
    public boolean isStreamingConnection(){
    	return isStreamingConnection;
    }

    protected abstract class XHRSessionHelper implements SockjsSessionOutbound {
        protected final SockjsSession session;
        private volatile boolean is_open = false;
        private final boolean isStreamingConnection;

        XHRSessionHelper(SockjsSession session, boolean isConnectionPersistant) {
            this.session = session;
            this.isStreamingConnection = isConnectionPersistant;
        }
        
        public boolean isStreamingConnection(){
        	return isStreamingConnection;
        }

        protected abstract void startSend(AtmosphereResponse response) throws IOException;

        protected abstract void writeData(AtmosphereResponse response, String data) throws IOException;
        
        protected abstract void writeData(AtmosphereResponse response, byte[] data) throws IOException;

        protected abstract void finishSend(AtmosphereResponse response) throws IOException;

        @Override
        public void disconnect() {
            synchronized (this) {
                session.onDisconnect(DisconnectReason.DISCONNECT);
                abort();
            }
        }

        @Override
        public void close() {
            synchronized (this) {
                session.startClose();
            }
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
                    try {
                        sendMessage(msg.toString());
                    } catch (Exception e) {

                        AtmosphereResourceImpl resource = session.getAtmosphereResourceImpl();
                        // if BroadcastCache is available, add the message to the cache
                        if (resource != null && DefaultBroadcaster.class.isAssignableFrom(resource.getBroadcaster().getClass())) {
                            DefaultBroadcaster.class.cast(resource.getBroadcaster()).
                                    broadcasterCache.addToCache(resource.getBroadcaster().getID(), resource,
                                    new BroadcasterCache.Message(msg));
                        }
                    }
                }
            }
        }

        @Override
        public void sendMessage(String message) throws SockjsException {
            logger.trace("Session[" + session.getSessionId() + "]: " + "sendMessage(String): " + message);

            synchronized (this) {
                if (is_open) {

                    AtmosphereResourceImpl resource = session.getAtmosphereResourceImpl();

                    logger.trace("Session[" + session.getSessionId() + "]: " + resource.getRequest().getMethod() + "sendMessage");

                    try {
                        writeData(resource.getResponse(), message);
                    } catch (Exception e) {
                        if (!resource.isCancelled()) {
                            logger.trace("calling from " + this.getClass().getName() + " : " + "sendMessage ON FORCE UN RESUME");
                            try {
                                finishSend(resource.getResponse());
                            } catch (IOException ex) {
                                logger.trace("", ex);
                            }

                            resource.resume();
                        }
                        throw new SockjsException(e);
                    }
                    if (!isStreamingConnection) {
                        try {
                            finishSend(resource.getResponse());
                        } catch (IOException e) {
                            logger.trace("", e);
                        }
                        resource.resume();
                    } else {
                        logger.trace("calling from " + this.getClass().getName() + " : " + "sendMessage");
                        session.startHeartbeatTimer();
                    }
                } else {
                    logger.trace("calling from " + this.getClass().getName() + " : " + "SocketIOClosedException sendMessage");
                    throw new SockjsClosedException();
                }
            }

        }

        @Override
        public Action handle(AtmosphereRequest request, final AtmosphereResponse response, SockjsSession session) throws IOException {
            logger.trace("Session id[" + session.getSessionId() + "] method=" + request.getMethod() + "  response HashCode=" + response.hashCode());
            System.out.println("Session id[" + session.getSessionId() + "] method=" + request.getMethod() + "  response HashCode=" + response.hashCode());

            AtmosphereResourceImpl resource = (AtmosphereResourceImpl) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);

            if ("GET".equals(request.getMethod())) {
                synchronized (this) {
                    if (!is_open) {
                        response.sendError(AtmosphereResponse.SC_NOT_FOUND);
                    } else {
                        if (!isStreamingConnection) {
                            if (resource != null) {
                                resource.getRequest().setAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_ID, session.getSessionId());
                                resource.getRequest().setAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_OUTBOUND, session.getTransportHandler());
                                session.setAtmosphereResourceImpl(resource);

                                resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
                                    @Override
                                    public void onResume(AtmosphereResourceEvent event) {
                                        if (event.isResumedOnTimeout()) {
                                            try {
                                                event.getResource().write(response.getOutputStream(), new SockjsPacketImpl("-1").toString());
                                            } catch (IOException e) {
                                                logger.trace("", e);
                                            }
                                        }
                                    }
                                });

                                session.clearTimeoutTimer();
                                request.setAttribute(SESSION_KEY, session);

                                StringBuilder data = new StringBuilder();
                                // if there is a Broadcaster cache, retrieve the messages from the cache, and send them
                                if (DefaultBroadcaster.class.isAssignableFrom(resource.getBroadcaster().getClass())) {

                                    List<Object> cachedMessages = DefaultBroadcaster.class.cast(resource.getBroadcaster())
                                            .broadcasterCache.retrieveFromCache(resource.getBroadcaster().getID(), resource);

                                    if (cachedMessages != null) {
                                        if (cachedMessages.size() > 1) {
                                            for (Object object : cachedMessages) {
                                                String msg = object.toString();
                                                data.append("-2a")
                                                        .append(msg.length())
                                                        .append("-2b").append(msg);
                                            }
                                        } else if (cachedMessages.size() == 1) {
                                            data.append(cachedMessages.get(0));
                                        }
                                    }

                                    // something to send ?
                                    if (data.toString().length() > 0) {
                                        startSend(response);
                                        writeData(response, data.toString());
                                        finishSend(response);

                                        // force a resume, because we sent data
                                        resource.resume();
                                    }
                                }

                                resource.disableSuspend(false);
                                resource.suspend(session.getRequestSuspendTime(), false);
                                resource.disableSuspend(true);
                            }
                        } else {
                            // won't happend, by should be for xhr-streaming transport
                            response.sendError(AtmosphereResponse.SC_NOT_FOUND);
                        }
                    }
                }
            } else if ("POST".equals(request.getMethod())) {
                if (is_open) {
                    int size = request.getContentLength();
                    if (size == 0) {
                        response.sendError(AtmosphereResponse.SC_BAD_REQUEST);
                    } else {
                        String data = (String) request.getAttribute(POST_MESSAGE_RECEIVED);
                        if (data == null) {
                            data = decodePostData(request.getContentType(), extractString(request.getReader()));
                        }
                        if (data != null && data.length() > 0) {

                            List<SockjsPacketImpl> list = SockjsPacketImpl.parse(data);

                            synchronized (session) {
                                for (SockjsPacketImpl msg : list) {

                                    // send message on the suspended request
                                    session.onMessage(session.getAtmosphereResourceImpl(), session.getTransportHandler(), msg.getData());

                                }
                            }
                        }
                    }
                    // force a resume on a POST request
                    if (!isStreamingConnection) {
                    	resource.resume();
                    }
                }
            } else {
                response.sendError(AtmosphereResponse.SC_BAD_REQUEST);
            }
            return Action.CANCELLED;
        }

        protected abstract void customConnect(AtmosphereRequest request, AtmosphereResponse response) throws IOException;

        public void connect(AtmosphereResourceImpl resource) throws IOException {

            AtmosphereRequest request = resource.getRequest();
            AtmosphereResponse response = resource.getResponse();
            
            if (isStreamingConnection) {
                resource.suspend(-1, false);
            }
            
            request.setAttribute(SESSION_KEY, session);
            response.setBufferSize(bufferSize);

            customConnect(request, response);
            is_open = true;
            session.onConnect(resource, this);
            finishSend(response);

            
        }

        @Override
        public void abort() {
            logger.error("calling from " + this.getClass().getName() + " : " + "abort");
            session.clearHeartbeatTimer();
            session.clearTimeoutTimer();
            is_open = false;
            session.onShutdown();

            // force a resume
            session.getAtmosphereResourceImpl().resume();
        }

        @Override
        public String getSessionId() {
            return session.getSessionId();
        }
    }

    public XHRTransport(int bufferSize, boolean isStreamingConnection) {
        this.bufferSize = bufferSize;
        this.isStreamingConnection = isStreamingConnection;
    }

    /**
     * This method should only be called within the context of an active HTTP request.
     */
    protected abstract XHRSessionHelper createHelper(SockjsSession session);


    protected SockjsSession connect(SockjsSession session, AtmosphereResourceImpl resource,
                                      AtmosphereHandler atmosphereHandler, SockjsSessionFactory sessionFactory, String sessionid) throws IOException {
    	
        if (session == null) {
            session = sessionFactory.createSession(resource, atmosphereHandler, sessionid);
            resource.getRequest().setAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_ID, session.getSessionId());
        }

        XHRSessionHelper handler = createHelper(session);
        handler.connect(resource);
        return session;
    }

    protected SockjsSession connect(AtmosphereResourceImpl resource, AtmosphereHandler atmosphereHandler, SockjsSessionFactory sessionFactory, String sessionid) throws IOException {
        return connect(null, resource, atmosphereHandler, sessionFactory, sessionid);
    }

    @Override
    public Action handle(AtmosphereResourceImpl resource, AtmosphereHandler atmosphereHandler, SockjsSessionFactory sessionFactory) throws IOException {

        AtmosphereRequest request = resource.getRequest();
        AtmosphereResponse response = resource.getResponse();

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
        
        //DEBUG
        //sessionId = null;

        boolean isDisconnectRequest = isDisconnectRequest(request);
        Action action = Action.CONTINUE;
        if (session != null) {
            SockjsSessionOutbound handler = session.getTransportHandler();
            if (handler != null) {
                if (!isDisconnectRequest) {
                    action = handler.handle(request, response, session);
                    response.setStatus(204);
                } else {
                    handler.disconnect();
                    response.setStatus(200);
                }
            } else {
                if (!isDisconnectRequest) {
                    // handle the Connect
                    session = connect(session, resource, atmosphereHandler, sessionFactory, sessionId);
                    if (session == null) {
                        response.sendError(AtmosphereResponse.SC_SERVICE_UNAVAILABLE);
                    } else {
                    	response.setStatus(204);
                    }
                } else {
                    response.setStatus(200);
                }
            }
        } else {
            if ("GET".equals(request.getMethod()) || "POST".equals(request.getMethod())) {
                session = connect(resource, atmosphereHandler, sessionFactory, sessionId);
                if (session == null) {
                    response.sendError(AtmosphereResponse.SC_SERVICE_UNAVAILABLE);
                } else if(!isStreamingConnection()){
                	response.setStatus(204);
                }
                
                if(isStreamingConnection()){
                	action = Action.SUSPEND;
                }
                
            } else {
                response.sendError(AtmosphereResponse.SC_BAD_REQUEST);
            }
        }

        return action;
    }

}
