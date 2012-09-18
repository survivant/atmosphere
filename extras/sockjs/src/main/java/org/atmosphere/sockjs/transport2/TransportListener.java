package org.atmosphere.sockjs.transport2;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
interface TransportListener {

	void sendFrame(String body);

	void close();

	void sessionClosed();
}
