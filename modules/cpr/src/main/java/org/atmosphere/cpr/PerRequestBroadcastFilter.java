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
package org.atmosphere.cpr;

/**
 * A markable interface that can be used in conjunction with {@link BroadcastFilter} to filter
 * message per request.
 */
public interface PerRequestBroadcastFilter extends BroadcastFilter {

    /**
     * Transform or Filter a message per request, with V as an indicator. Be careful when setting headers on the
     * {@link AtmosphereResponse} as the headers may have been already sent back to the browser.
     *
     *
     * @param atmosphereResource
     * @param message  Object a message
     * @param originalMessage
     * @return a transformed message.
     */
    BroadcastAction filter(AtmosphereResource atmosphereResource, Object originalMessage, Object message);
}
