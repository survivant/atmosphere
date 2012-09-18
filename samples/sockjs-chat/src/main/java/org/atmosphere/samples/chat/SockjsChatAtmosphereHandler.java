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
package org.atmosphere.samples.chat;

import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.BroadcastOnPostAtmosphereInterceptor;
import org.atmosphere.sockjs.SockjsException;
import org.atmosphere.sockjs.SockjsSessionOutbound;
import org.atmosphere.sockjs.cpr.SockjsAtmosphereHandler;
import org.atmosphere.sockjs.transport.DisconnectReason;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Date;

/**
 * Simple SocketIO Implementation of the Atmosphere Chat
 *
 * @author Sebastien Dionne : sebastien.dionne@gmail.com
 */
@AtmosphereHandlerService(path = "/chat", interceptors= {AtmosphereResourceLifecycleInterceptor.class
/*, BroadcastOnPostAtmosphereInterceptor.class*/})
public class SockjsChatAtmosphereHandler extends SockjsAtmosphereHandler {

	class Answer {
		
		private String a = null;
		
		Answer(String message){
			a = message;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}
		
	}

	private final ObjectMapper mapper = new ObjectMapper();
	
    @Override
    public void destroy() {
    }

	@Override
	public void onConnect(AtmosphereResource event, SockjsSessionOutbound handler) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect(AtmosphereResource event, SockjsSessionOutbound handler, DisconnectReason reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(AtmosphereResource event, SockjsSessionOutbound handler, String message) {
    	
    	System.out.println("onMessage message = " + message);
    	
    	if(message!=null){
    		String output = null;
			try {
				output = mapper.writeValueAsString(new Answer(message));
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	// a["{\"x\":188,\"y\":25,\"t\":1347800393967,\"id\":\"14217492053298475\"}"]
        	
            try {
            	output = "a" + message + "\n";
				//handler.sendMessage("a" + message);
            	System.out.println("Sending on suspended connection this message = " + output);
            	handler.sendMessage(output);
			} catch (SockjsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		
	}

}
