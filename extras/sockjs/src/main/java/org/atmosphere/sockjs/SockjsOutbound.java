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
package org.atmosphere.sockjs;

import java.util.List;

import org.atmosphere.sockjs.transport.SockjsPacketImpl;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 */
public interface SockjsOutbound {

    /**
     * disconnect the current connection
     */
    void disconnect();

    /**
     * force close connection
     */
    void close();


    /**
     * Send a message to the client. If the session is still active, the message will be cached if the connection is closed.
     *
     * @param message The message to send
     * @throws SockjsException
     */
    void sendMessage(String message) throws SockjsException;

    /**
     * Send a message to the client formatted is SocketIO format. If the session is still active, the message will be cached if the connection is closed.
     *
     * @param packet The message to send
     * @throws SockjsException
     */
    void sendMessage(SockjsPacket packet) throws SockjsException;

    /**
     * Send messages to the client. If the session is still active, the messages will be cached if the connection is closed.
     *
     * @param packet The message to send
     * @throws SockjsException
     */
    void sendMessage(List<SockjsPacketImpl> messages) throws SockjsException;

}
