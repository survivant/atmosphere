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
package org.atmosphere.websocket;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AsynchronousProcessor;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEventImpl;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.HeaderConfig;
import org.atmosphere.util.VoidExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.atmosphere.cpr.ApplicationConfig.RECYCLE_ATMOSPHERE_REQUEST_RESPONSE;
import static org.atmosphere.cpr.ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID;
import static org.atmosphere.cpr.ApplicationConfig.WEBSOCKET_PROTOCOL_EXECUTION;
import static org.atmosphere.cpr.FrameworkConfig.ASYNCHRONOUS_HOOK;
import static org.atmosphere.cpr.FrameworkConfig.INJECTED_ATMOSPHERE_RESOURCE;
import static org.atmosphere.websocket.WebSocketEventListener.WebSocketEvent.TYPE.CLOSE;
import static org.atmosphere.websocket.WebSocketEventListener.WebSocketEvent.TYPE.CONNECT;
import static org.atmosphere.websocket.WebSocketEventListener.WebSocketEvent.TYPE.MESSAGE;

/**
 * Like the {@link org.atmosphere.cpr.AsynchronousProcessor} class, this class is responsible for dispatching WebSocket request to the
 * proper {@link org.atmosphere.websocket.WebSocket} implementation. This class can be extended in order to support any protocol
 * running on top  websocket.
 *
 * @author Jeanfrancois Arcand
 */
public class DefaultWebSocketProcessor implements WebSocketProcessor, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketProcessor.class);

    private final AtmosphereFramework framework;
    private final WebSocket webSocket;
    private final WebSocketProtocol webSocketProtocol;
    private final AtomicBoolean loggedMsg = new AtomicBoolean(false);
    private final boolean destroyable;
    private final boolean executeAsync;
    private final ExecutorService asyncExecutor;
    private final ExecutorService voidExecutor;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public DefaultWebSocketProcessor(AtmosphereFramework framework, WebSocket webSocket, WebSocketProtocol webSocketProtocol) {
        this.webSocket = webSocket;
        this.framework = framework;
        this.webSocketProtocol = webSocketProtocol;

        String s = framework.getAtmosphereConfig().getInitParameter(RECYCLE_ATMOSPHERE_REQUEST_RESPONSE);
        if (s != null && Boolean.valueOf(s)) {
            destroyable = true;
        } else {
            destroyable = false;
        }

        s = framework.getAtmosphereConfig().getInitParameter(WEBSOCKET_PROTOCOL_EXECUTION);
        if (s != null && Boolean.valueOf(s)) {
            executeAsync = true;
        } else {
            executeAsync = false;
        }
        asyncExecutor = Executors.newCachedThreadPool();
        voidExecutor = VoidExecutorService.VOID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void open(final AtmosphereRequest request) throws IOException {
        if (!loggedMsg.getAndSet(true)) {
            logger.debug("Atmosphere detected WebSocket: {}", webSocket.getClass().getName());
        }

        AtmosphereResponse wsr = new AtmosphereResponse(webSocket, request, destroyable);
        request.headers(configureHeader(request)).setAttribute(WebSocket.WEBSOCKET_SUSPEND, true);

        AtmosphereResource r = AtmosphereResourceFactory.getDefault().create(framework.getAtmosphereConfig(),
                wsr,
                framework.getAsyncSupport());

        request.setAttribute(INJECTED_ATMOSPHERE_RESOURCE, r);
        request.setAttribute(SUSPENDED_ATMOSPHERE_RESOURCE_UUID, r.uuid());

        webSocket.resource(r);
        webSocketProtocol.onOpen(webSocket);

        dispatch(request, wsr);
        request.removeAttribute(INJECTED_ATMOSPHERE_RESOURCE);

        if (webSocket.resource() != null) {
            final AsynchronousProcessor.AsynchronousProcessorHook hook =
                    new AsynchronousProcessor.AsynchronousProcessorHook((AtmosphereResourceImpl) webSocket.resource());
            request.setAttribute(ASYNCHRONOUS_HOOK, hook);

            final Action action = ((AtmosphereResourceImpl) webSocket.resource()).action();
            if (action.timeout() != -1 && !framework.getAsyncSupport().getContainerName().contains("Netty")) {
                final AtomicReference<Future<?>> f = new AtomicReference();
                f.set(scheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (WebSocket.class.isAssignableFrom(webSocket.getClass())
                                && System.currentTimeMillis() - WebSocket.class.cast(webSocket).lastWriteTimeStampInMilliseconds() > action.timeout()) {
                            hook.timedOut();
                            f.get().cancel(true);
                        }
                    }
                }, action.timeout(), action.timeout(), TimeUnit.MILLISECONDS));
            }
        } else {
            logger.warn("AtmosphereResource was null");
        }
        notifyListener(new WebSocketEventListener.WebSocketEvent("", CONNECT, webSocket));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeWebSocketProtocol(String webSocketMessage) {
        List<AtmosphereRequest> list = webSocketProtocol.onMessage(webSocket, webSocketMessage);
        dispatch(list);
        notifyListener(new WebSocketEventListener.WebSocketEvent(webSocketMessage, MESSAGE, webSocket));
    }

    private void dispatch(List<AtmosphereRequest> list) {
        if (list == null) return;

        for (final AtmosphereRequest r : list) {
            if (r != null) {

                boolean b = r.dispatchRequestAsynchronously();
                ExecutorService s = (executeAsync || b) ? asyncExecutor : voidExecutor;

                s.execute(new Runnable() {
                    @Override
                    public void run() {
                        AtmosphereResponse w = new AtmosphereResponse(webSocket, r, destroyable);
                        try {
                            dispatch(r, w);
                        } finally {
                            r.destroy();
                            w.destroy();
                        }
                    }
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invokeWebSocketProtocol(byte[] data, int offset, int length) {
        List<AtmosphereRequest> list = webSocketProtocol.onMessage(webSocket, data, offset, length);
        dispatch(list);
        try {
            notifyListener(new WebSocketEventListener.WebSocketEvent(new String(data, offset, length, "UTF-8"), MESSAGE, webSocket));
        } catch (UnsupportedEncodingException e) {
            logger.warn("UnsupportedEncodingException", e);

        }
    }

    /**
     * Dispatch to request/response to the {@link org.atmosphere.cpr.AsyncSupport} implementation as it was a normal HTTP request.
     *
     * @param request a {@link AtmosphereRequest}
     * @param r       a {@link AtmosphereResponse}
     */
    public final void dispatch(final AtmosphereRequest request, final AtmosphereResponse r) {
        if (request == null) return;
        try {
            framework.doCometSupport(request, r);
        } catch (Throwable e) {
            logger.warn("Failed invoking AtmosphereFramework.doCometSupport()", e);
            webSocketProtocol.onError(webSocket, new WebSocketException(e,
                    new AtmosphereResponse.Builder()
                            .request(request)
                            .status(500)
                            .statusMessage("Server Error").build()));
            return;
        }

        if (r.getStatus() >= 400) {
            webSocketProtocol.onError(webSocket, new WebSocketException("Status code higher or equal than 400", r));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebSocket webSocket() {
        return webSocket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close(int closeCode) {
        logger.trace("WebSocket closed with {}", closeCode);
        // A message might be in the process of being processed and the websocket gets closed. In that corner
        // case the webSocket.resource will be set to false and that might cause NPE in some WebSocketProcol implementation
        // We could potentially synchronize on webSocket but since it is a rare case, it is better to not synchronize.
        // synchronized (webSocket) {

        notifyListener(new WebSocketEventListener.WebSocketEvent("", CLOSE, webSocket));
        AtmosphereResourceImpl resource = (AtmosphereResourceImpl) webSocket.resource();

        if (resource == null) {
            logger.warn("Unable to retrieve AtmosphereResource for {}", webSocket);
        } else {
            AtmosphereRequest r = resource.getRequest(false);
            AtmosphereResponse s = resource.getResponse(false);
            try {
                webSocketProtocol.onClose(webSocket);

                if (resource != null && resource.isInScope()) {
                    AsynchronousProcessor.AsynchronousProcessorHook h = (AsynchronousProcessor.AsynchronousProcessorHook)
                            r.getAttribute(ASYNCHRONOUS_HOOK);
                    if (h != null) {
                        if (closeCode == 1000) {
                            h.timedOut();
                        } else {
                            h.closed();
                        }
                    } else {
                        logger.warn("AsynchronousProcessor.AsynchronousProcessorHook was null");
                    }

                    // We must always destroy the root resource (the one created when the websocket was opened
                    // to prevent memory leaks.
                    resource.setIsInScope(false);
                    try {
                        resource.cancel();
                    } catch (IOException e) {
                        logger.trace("", e);
                    }
                    AsynchronousProcessor.destroyResource(resource);
                }
            } finally {
                if (r != null) {
                    r.destroy(true);
                }

                if (s != null) {
                    s.destroy(true);
                }

                if (webSocket != null) {
                    webSocket.resource(null);
                }
            }
        }
        asyncExecutor.shutdown();
        voidExecutor.shutdown();
        scheduler.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "DefaultWebSocketProtocol{ webSocket=" + webSocket + " }";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyListener(WebSocketEventListener.WebSocketEvent event) {
        AtmosphereResource resource = webSocket.resource();
        if (resource == null) return;

        AtmosphereResourceImpl r = AtmosphereResourceImpl.class.cast(resource);

        for (AtmosphereResourceEventListener l : r.atmosphereResourceEventListener()) {
            if (WebSocketEventListener.class.isAssignableFrom(l.getClass())) {
                try {
                    switch (event.type()) {
                        case CONNECT:
                            WebSocketEventListener.class.cast(l).onConnect(event);
                            break;
                        case DISCONNECT:
                            WebSocketEventListener.class.cast(l).onDisconnect(event);
                            break;
                        case CONTROL:
                            WebSocketEventListener.class.cast(l).onControl(event);
                            break;
                        case MESSAGE:
                            WebSocketEventListener.class.cast(l).onMessage(event);
                            break;
                        case HANDSHAKE:
                            WebSocketEventListener.class.cast(l).onHandshake(event);
                            break;
                        case CLOSE:
                            WebSocketEventListener.class.cast(l).onDisconnect(event);
                            WebSocketEventListener.class.cast(l).onClose(event);
                            break;
                    }
                } catch (Throwable t) {
                    logger.debug("Listener error {}", t);
                    try {
                        WebSocketEventListener.class.cast(l).onThrowable(new AtmosphereResourceEventImpl(r, false, false, t));
                    } catch (Throwable t2) {
                        logger.warn("Listener error {}", t2);
                    }
                }
            }
        }
    }

    public static final Map<String, String> configureHeader(AtmosphereRequest request) {
        Map<String, String> headers = new HashMap<String, String>();

        Enumeration<String> e = request.getParameterNames();
        String s;
        while (e.hasMoreElements()) {
            s = e.nextElement();
            headers.put(s, request.getParameter(s));
        }

        headers.put(HeaderConfig.X_ATMOSPHERE_TRANSPORT, HeaderConfig.WEBSOCKET_TRANSPORT);
        return headers;
    }
}
