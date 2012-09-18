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
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastien Dionne  : sebastien.dionne@gmail.com
 */
public class XHRStreamingTransport extends XHRTransport {

    private static final Logger logger = LoggerFactory.getLogger(XHRStreamingTransport.class);

    public static final String TRANSPORT_NAME = "xhr_streaming";

    public XHRStreamingTransport(int bufferSize) {
        super(bufferSize, true);
    }

    @Override
    public String getName() {
        return TRANSPORT_NAME;
    }

    protected XHRStreamingSessionHelper createHelper(SockjsSession session) {
        return new XHRStreamingSessionHelper(session);
    }

    protected class XHRStreamingSessionHelper extends XHRSessionHelper {

    	XHRStreamingSessionHelper(SockjsSession session) {
            super(session, true);
        }

        protected void startSend(AtmosphereResponse response) throws IOException {
        	//setCORS(null, response);
        	
        	response.addHeader("Content-Type", CONTENT_TYPE_JAVASCRIPT);
            response.setContentType(CONTENT_TYPE_JAVASCRIPT);
            response.setCharacterEncoding("UTF-8");
            
            response.addHeader("Transfer-Encoding", "chunked");
            response.setHeader("Transfer-Encoding", "chunked");
            
            response.addHeader("Connection", "keep-alive");
            response.setHeader("Connection", "keep-alive");
            
            
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
        };

        protected void customConnect(AtmosphereRequest request, AtmosphereResponse response) throws IOException {
            startSend(response);
            
    		byte[] bytes = new byte[2048 + 1];
    	    for (int i = 0; i < bytes.length; i++) {
    	      bytes[i] = (byte)'h';
    	    }
    	    bytes[bytes.length - 1] = (byte)'\n';
    	    
    	    
    	    writeData(response, bytes);
    	    writeData(response, "o\n");
        }

    }

}
