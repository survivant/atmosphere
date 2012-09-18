/*
 * Copyright 2012 Jeanfrancois Arcand
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
package org.atmosphere.container;

import org.atmosphere.container.version.Jetty8WebSocket;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.WebSocketProcessorFactory;
import org.atmosphere.websocket.WebSocketEventListener;
import org.atmosphere.websocket.WebSocketProcessor;
import org.atmosphere.websocket.WebSocketProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static org.atmosphere.websocket.WebSocketEventListener.WebSocketEvent.TYPE.CONTROL;
import static org.atmosphere.websocket.WebSocketEventListener.WebSocketEvent.TYPE.HANDSHAKE;

/**
 * Jetty 7 & 8 WebSocket support.
 */
public class JettyWebSocketHandler implements org.eclipse.jetty.websocket.WebSocket,
        org.eclipse.jetty.websocket.WebSocket.OnFrame,
        org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage,
        org.eclipse.jetty.websocket.WebSocket.OnTextMessage,
        org.eclipse.jetty.websocket.WebSocket.OnControl {

    private static final Logger logger = LoggerFactory.getLogger(JettyWebSocketHandler.class);

    private WebSocketProcessor webSocketProcessor;
    private final AtmosphereRequest request;
    private final AtmosphereFramework framework;
    private final WebSocketProtocol webSocketProtocol;

    public JettyWebSocketHandler(AtmosphereRequest request, AtmosphereFramework framework, WebSocketProtocol webSocketProtocol) {
        this.request = request;
        this.framework = framework;
        this.webSocketProtocol = webSocketProtocol;
    }

    @Override
    public void onMessage(byte[] data, int offset, int length) {
        logger.trace("WebSocket.onMessage (bytes)");
        webSocketProcessor.invokeWebSocketProtocol(data, offset, length);
    }

    @Override
    public boolean onControl(byte controlCode, byte[] data, int offset, int length) {
        logger.trace("WebSocket.onControl.");
        try {
            webSocketProcessor.notifyListener(new WebSocketEventListener.WebSocketEvent(new String(data, offset, length, "UTF-8"), CONTROL, webSocketProcessor.webSocket()));
        } catch (UnsupportedEncodingException e) {
            logger.warn("UnsupportedEncodingException", e);

        }
        return false;
    }

    @Override
    public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {
        logger.trace("WebSocket.onFrame.");
        // TODO: onMessage is always invoked after that method gets called, so no need to enable for now.
        //       webSocketProcessor.broadcast(data, offset, length);
        /* try {
            webSocketProcessor.notifyListener(new WebSocketEventListener.WebSocketEvent(new String(data, offset, length, "UTF-8"), MESSAGE, webSocketProcessor.webSocket()));
        } catch (UnsupportedEncodingException e) {
            logger.warn("UnsupportedEncodingException", e);

        }*/
        return false;
    }

    @Override
    public void onHandshake(org.eclipse.jetty.websocket.WebSocket.FrameConnection connection) {
        logger.trace("WebSocket.onHandshake");
        try {
            webSocketProcessor = WebSocketProcessorFactory.getDefault()
                    .newWebSocketProcessor(new Jetty8WebSocket(connection, framework.getAtmosphereConfig()));
        } catch (Exception e) {
            logger.warn("failed to connect to web socket", e);
        }

        webSocketProcessor.notifyListener(new WebSocketEventListener.WebSocketEvent("", HANDSHAKE, webSocketProcessor.webSocket()));
    }

    @Override
    public void onMessage(String data) {
        logger.trace("WebSocket.onMessage");
        webSocketProcessor.invokeWebSocketProtocol(data);
    }

    @Override
    public void onOpen(org.eclipse.jetty.websocket.WebSocket.Connection connection) {
        logger.trace("WebSocket.onOpen.");
        try {
            webSocketProcessor = WebSocketProcessorFactory.getDefault()
                    .newWebSocketProcessor(new Jetty8WebSocket(connection, framework.getAtmosphereConfig()));
            webSocketProcessor.open(request);
        } catch (Exception e) {
            logger.warn("Failed to connect to WebSocket", e);
        }
    }

    @Override
    public void onClose(int closeCode, String message) {
        request.destroy();
        if (webSocketProcessor == null) return;

        webSocketProcessor.close(closeCode);

    }
}
