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

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.sockjs.SockjsSession;
import org.atmosphere.sockjs.SockjsSessionFactory;
import org.atmosphere.sockjs.cpr.SockjsAtmosphereHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 */
public class JSONPPollingTransport extends XHRTransport {
    public static final String TRANSPORT_NAME = "jsonp-polling";

    private static final Logger logger = LoggerFactory.getLogger(JSONPPollingTransport.class);

    private long jsonpIndex = 0;

    protected class XHRPollingSessionHelper extends XHRSessionHelper {

        XHRPollingSessionHelper(SockjsSession session) {
            super(session, false);
        }

        protected void startSend(AtmosphereResponse response) throws IOException {
        }

        @Override
        protected void writeData(AtmosphereResponse response, String data) throws IOException {
            logger.trace("calling from " + this.getClass().getName() + " : " + "writeData(string) = " + data);

            response.setContentType("text/javascript; charset=UTF-8");
            response.getOutputStream().print("io.j[" + jsonpIndex + "](\"" + data + "\");");

            logger.trace("WRITE SUCCESS calling from " + this.getClass().getName() + " : " + "writeData(string) = " + data);

        }
        
        @Override
		protected void writeData(AtmosphereResponse response, byte[] data) throws IOException {
        	logger.trace("calling from " + this.getClass().getName() + " : " + "writeData(byte) = " + new String(data, "UTF-8"));
        	response.getOutputStream().write(data);
            logger.trace("WRITE SUCCESS calling from " + this.getClass().getName() + " : " + "writeData(byte) = " + data);
		}

        protected void finishSend(AtmosphereResponse response) throws IOException {
            response.flushBuffer();
        }

        protected void customConnect(AtmosphereRequest request,
                                     AtmosphereResponse response) throws IOException {

            if (request.getParameter("i") != null) {
                jsonpIndex = Integer.parseInt(request.getParameter("i"));
            } else {
                jsonpIndex = 0;
            }

            writeData(response, new SockjsPacketImpl("-3").toString());
        }
    }

    public JSONPPollingTransport(int bufferSize) {
        super(bufferSize, false);
    }

    @Override
    public String getName() {
        return TRANSPORT_NAME;
    }


    protected XHRPollingSessionHelper createHelper(SockjsSession session) {
        return new XHRPollingSessionHelper(session);
    }

    @Override
    protected SockjsSession connect(SockjsSession session, AtmosphereResourceImpl resource, AtmosphereHandler atmosphereHandler, SockjsSessionFactory sessionFactory, String sessionId) throws IOException {
    	
        if (session == null) {
            session = sessionFactory.createSession(resource, atmosphereHandler, sessionId);
            resource.getRequest().setAttribute(SockjsAtmosphereHandler.SOCKJS_SESSION_ID, session.getSessionId());
        }
        

        XHRPollingSessionHelper handler = createHelper(session);
        handler.connect(resource);
        return session;
    }

    @Override
    protected SockjsSession connect(AtmosphereResourceImpl resource, AtmosphereHandler atmosphereHandler, SockjsSessionFactory sessionFactory, String sessionId) throws IOException {
        return connect(null, resource, atmosphereHandler, sessionFactory, sessionId);
    }
}
