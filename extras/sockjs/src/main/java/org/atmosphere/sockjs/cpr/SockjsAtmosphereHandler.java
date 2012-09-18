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
package org.atmosphere.sockjs.cpr;

import java.io.IOException;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.sockjs.SockjsSessionOutbound;
import org.atmosphere.sockjs.transport.DisconnectReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 */
public abstract class SockjsAtmosphereHandler implements AtmosphereHandler {

    private final Logger logger = LoggerFactory.getLogger(SockjsAtmosphereHandler.class);
    public static final String SOCKJS_SESSION_OUTBOUND = "SockjsSessionOutbound";
    public static final String SOCKJS_SESSION_ID = SockjsAtmosphereHandler.class.getPackage().getName() + ".sessionid";


    /**
     * Called when the connection is established.
     *
     * @param handler The SocketOutbound associated with the connection
     */
    abstract public void onConnect(AtmosphereResource event, SockjsSessionOutbound handler) throws IOException;

    /**
     * Called when the socket connection is disconnected.
     *
     * @param event   AtmosphereResource
     * @param handler outbound handler to broadcast response
     * @param reason  The reason for the disconnect.
     */
    abstract public void onDisconnect(AtmosphereResource event, SockjsSessionOutbound handler, DisconnectReason reason);

    /**
     * Called for each message received.
     *
     * @param event   AtmosphereResource
     * @param handler outbound handler to broadcast response
     * @param message message received
     */
    abstract public void onMessage(AtmosphereResource event, SockjsSessionOutbound handler, String message);

    
    /**
     * {@inheritDoc}
     */
    public final void onRequest(AtmosphereResource event) throws IOException {
        logger.trace("onRequest");
    }

    /**
     * {@inheritDoc}
     */
    public final void onStateChange(AtmosphereResourceEvent event) throws IOException {

        if (event.isResuming() || event.isResumedOnTimeout()) {
            return;
        }

        AtmosphereRequest request = event.getResource().getRequest();
        logger.trace("onStateChange on SessionID=" + request.getAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_ID) + "  Method=" + request.getMethod());
        
        System.out.println("onStateChange message = " + event.getMessage().toString());
    }

    public void destroy() {
        logger.trace("destroy");
    }
}
