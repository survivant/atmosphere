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

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.sockjs.SockjsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 */
public class XHRPollingTransport extends XHRTransport {

    private static final Logger logger = LoggerFactory.getLogger(XHRPollingTransport.class);

    public static final String TRANSPORT_NAME = "xhr_send";

    public XHRPollingTransport(int bufferSize) {
        super(bufferSize, false);
    }

    @Override
    public String getName() {
        return TRANSPORT_NAME;
    }

    protected XHRPollingSessionHelper createHelper(SockjsSession session) {
        return new XHRPollingSessionHelper(session);
    }

    protected class XHRPollingSessionHelper extends XHRSessionHelper {

        XHRPollingSessionHelper(SockjsSession session) {
            super(session, false);
        }

        protected void startSend(AtmosphereResponse response) throws IOException {
        	//response.addHeader("Content-Type", "text/plain; charset=UTF-8");
        	setCORS(null, response);
        	
            response.setContentType(CONTENT_TYPE_PLAIN);
            response.setCharacterEncoding("UTF-8");
        }

        @Override
        protected void writeData(AtmosphereResponse response, String data) throws IOException {
            logger.trace("calling from " + this.getClass().getName() + " : " + "writeData(string) = " + data);
            response.getOutputStream().write(data.getBytes("UTF-8"));
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
            response.getOutputStream().flush();
            response.setStatus(204); // pas sur que ca sert a de quoi ici
            
        };

        protected void customConnect(AtmosphereRequest request, AtmosphereResponse response) throws IOException {
            startSend(response);
            
        }

    }

}
