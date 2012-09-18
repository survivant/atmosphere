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

import java.util.ArrayList;
import java.util.List;

import org.atmosphere.sockjs.SockjsException;
import org.atmosphere.sockjs.SockjsPacket;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 */
public class SockjsPacketImpl implements SockjsPacket {

    private final String data;

    public SockjsPacketImpl(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public String toString() {
        return data;
    }

    public static List<SockjsPacketImpl> parse(String data) throws SockjsException {
        List<SockjsPacketImpl> messages = new ArrayList<SockjsPacketImpl>();

        if (data == null || data.length() == 0) {
            return messages;
        }

    	messages.add(new SockjsPacketImpl(data));
    

        return messages;
    }

}
