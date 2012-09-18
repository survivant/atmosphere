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
package org.atmosphere.gwt.client.extra;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Timer;

/**
 * @author p.havelaar
 */
public class WindowSocket {
    
    private final static String DOM_NAME = WindowSocket.class.getName();

    public static class MessageEvent extends GwtEvent<MessageHandler> {

        private static Type TYPE;
        private String message;
        private Window source;

        public static Type getType() {
            return TYPE != null ? TYPE : (TYPE = new Type());
        }

        public MessageEvent(Window source, String message) {
            this.source = source;
            this.message = message;
        }

        @Override
        public Type<MessageHandler> getAssociatedType() {
            return getType();
        }

        @Override
        protected void dispatch(MessageHandler handler) {
            handler.onMessage(source, message);
        }

    }

    public static interface MessageHandler extends EventHandler {
        public void onMessage(Window source, String message);
    }

    public static boolean exists(Window w, String socketName) {
        Sockets sockets = w.getObject(DOM_NAME);
        if (sockets == null) {
            return false;
        }
        return sockets.isSet(socketName);
    }

    public static void post(Window w, String socketName, String message) {
        SocketImpl s = getSocket(w, socketName);
        if (s != null) {
            s.post(Window.current(), message);
        }
    }
    
    public static void crossPost(Window source, Window target, String socketName, String message) {
        SocketImpl s = getSocket(target, socketName);
        if (s != null) {
            s.post(source, message);
        }
    }

    /**
     * This will create a socket in the target window to receive the message
     * even if the target window did not bind a WindowSocket yet.
     * This will create a queue that can be picket up when the target window will bind.
     * 
     * @param w
     * @param socketName
     * @param message 
     */
    public static void forcePost(Window w, String socketName, String message) {
        SocketImpl s = getSocket(w, socketName);
        if (s == null) {
            s = createSocket(w, socketName);
        }
        s.post(Window.current(), message);
    }

    public void bind(String name) {
        unbind();
        Window w = Window.current();
        socket = getSocket(w, name);
        if (socket == null) {
            socket = createSocket(w, name);
        }
        queueTimer.scheduleRepeating(250);
    }

    public void unbind() {
        if (socket != null) {
            Sockets sockets = Window.current().getObject(DOM_NAME);
            sockets.remove(socket);
            queueTimer.cancel();
            // run once to empty queue
            queueTimer.run();
            socket = null;
        }
    }

    public HandlerRegistration addHandler(MessageHandler handler) {
        return listeners.addHandler(MessageEvent.getType(), handler);
    }
    
    private static SocketImpl createSocket(Window w, String name) {
        Sockets sockets = w.getObject(DOM_NAME);
        if (sockets == null) {
            sockets = Sockets.create();
            w.set(DOM_NAME, sockets);
        }
        SocketImpl socket = SocketImpl.create(name);
        sockets.set(socket);
        return socket;
    }

    private static SocketImpl getSocket(Window w, String name) {
        Sockets sockets = w.getObject(DOM_NAME);
        if (sockets == null) {
            return null;
        }
        return sockets.get(name);
    }

    private SocketImpl socket;
    private EventBus listeners = new SimpleEventBus();
    private Timer queueTimer = new Timer() {
        @Override
        public void run() {
            if (socket != null) {
                SocketImpl.SocketMsg m;
                while ((m = socket.poll()) != null) {
                    listeners.fireEvent(new MessageEvent(m.source(), m.message()));
                }
            }
        }
    };

    private final static class SocketImpl extends JavaScriptObject {
        
        public final static class SocketMsg extends JavaScriptObject {
            protected SocketMsg() {};
            public native final String message() /*-{
                return this.value;
            }-*/;
            public native final Window source() /*-{
                return this.source;
            }-*/;
        }
        
        private static native SocketImpl create(String xname) /*-{
            return {
                messages: null,
                last: null,
                name: xname
            };
        }-*/;

        public native void post(Window xsource, String message) /*-{
            var head = this.messages;
            var msg = { };
            msg.next = null;
            msg.value = message;
            msg.source = xsource;
            this.last = msg;
            if (head != null) {
                head.next = msg;
            } else {
                this.messages = msg;
            }
        }-*/;

        public native SocketMsg poll() /*-{
            if (this.messages == null) {
                return null;
            }
            var msg = this.messages;
            this.messages = msg.next;
            if (this.messages == null) {
                this.last = null;
            }
            return msg;
        }-*/;

        protected SocketImpl() {
        }
    }

    private final static class Sockets extends JavaScriptObject {
        public static native Sockets create() /*-{
            return {
                sockets: {}
            };
        }-*/;

        public native SocketImpl set(SocketImpl socket) /*-{
            var t = this.sockets[socket.name];
            this.sockets[socket.name] = socket;
            return t;
        }-*/;

        public native SocketImpl get(String name) /*-{
            return this.sockets[name];
        }-*/;
        
        public native boolean isSet(String name) /*-{
            return typeof this.sockets[name] !== 'undefined';
        }-*/;

        public native void remove(SocketImpl socket) /*-{
            delete this.sockets[socket.name];
        }-*/;

        protected Sockets() {

        }
    }

}
