package org.atmosphere.sockjs.transport;

import java.io.IOException;

import org.atmosphere.cpr.Action;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.sockjs.SockjsSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IFrameTransport extends AbstractTransport {
    private static final Logger logger = LoggerFactory.getLogger(IFrameTransport.class);
    public static final String TRANSPORT_NAME = "iframe";
	
    @Override
	public String getName() {
		return TRANSPORT_NAME;
	}

	@Override
	public Action handle(AtmosphereResourceImpl resource,
			AtmosphereHandler atmosphereHandler,
			SockjsSessionFactory sessionFactory) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStreamingConnection() {
		// TODO Auto-generated method stub
		return false;
	}
	
    
}
