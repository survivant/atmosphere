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

package org.atmosphere.gwt.server.impl;


import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import org.atmosphere.gwt.server.SerializationException;

/**
 * @author p.havelaar
 */
abstract public class ManagedStreamResponseWriter extends GwtResponseWriterImpl {

    private CountOutputStream countOutputStream;
    private boolean refresh;

    protected Integer length;

    protected final boolean chrome;

    public ManagedStreamResponseWriter(GwtAtmosphereResourceImpl resource) {
        super(resource);

        String userAgent = resource.getAtmosphereResource().getRequest().getHeader("User-Agent");
        chrome = userAgent != null && userAgent.contains("Chrome");
    }

    @Override
    protected OutputStream getOutputStream(OutputStream outputStream) {
        countOutputStream = new CountOutputStream(outputStream);
        return countOutputStream;
    }

    @Override
    protected void doSuspend() throws IOException {
        int paddingRequired;
        String paddingParameter = getRequest().getParameter("padding");
        if (paddingParameter != null) {
            paddingRequired = Integer.parseInt(paddingParameter);
        } else {
            paddingRequired = getPaddingRequired();
        }

        String lengthParameter = getRequest().getParameter("length");
        if (lengthParameter != null) {
            length = Integer.parseInt(lengthParameter);
        }

        if (paddingRequired > 0) {
            countOutputStream.setIgnoreFlush(true);
            writer.flush();

            int written = countOutputStream.getCount();

            if (paddingRequired > written) {
                CharSequence padding = getPadding(paddingRequired - written);
                if (padding != null) {
                    writer.append(padding);
                }
            }

            countOutputStream.setIgnoreFlush(false);
        }
    }

    @Override
    public synchronized void write(List<? extends Serializable> messages, boolean flush) 
                    throws IOException, SerializationException {
        super.write(messages, flush);
        checkLength();
    }

    @Override
    public synchronized void heartbeat() throws IOException {
        super.heartbeat();
        checkLength();
    }

    private void checkLength() throws IOException {
        int count = countOutputStream.getCount();
        // Chrome seems to have a problem with lots of small messages consuming lots of memory.
        // I'm guessing for each readyState = 3 event it copies the responseText from its IO system to its
        // JavaScript
        // engine and does not clean up all the events until the HTTP request is finished.
        if (chrome) {
            count = 2 * count;
        }
        if (!refresh && isOverRefreshLength(count)) {
            refresh = true;
            doRefresh();
        }
    }

    protected abstract void doRefresh() throws IOException;

    protected abstract int getPaddingRequired();

    protected abstract CharSequence getPadding(int padding);

    protected abstract boolean isOverRefreshLength(int written);
}
