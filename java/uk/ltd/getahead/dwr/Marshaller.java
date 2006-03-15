/*
 * Copyright 2005 Joe Walker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ltd.getahead.dwr;

/**
 * A Marshaller is responsible for all the on-the-wire communication between
 * DWR on the server and the HTTP channel. DWREngine does the corresponding
 * work on thhe Javascript side.
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public interface Marshaller
{
    /**
     * Marshall an incomming HttpRequest into an abstract Calls POJO that
     * defines the functions that we need to call.
     * @param request The incoming Http request
     * @return Data specifying the methods to call
     * @throws SecurityException If the requests are not allowed
     * @throws MarshallException If the data can not be understood.
     */
    Calls marshallInbound(HttpRequest request) throws SecurityException, MarshallException;

    /**
     * Marshall the return values from executing this batch of requests.
     * @param replies
     * @return An Ajax response, XML, JSON, Javascript, etc.
     * @throws MarshallException If the marshallinng process fails
     */
    HttpResponse marshallOutbound(Replies replies) throws MarshallException;

    /**
     * Check if we can coerce the given type
     * @param paramType The type to check
     * @return true iff <code>paramType</code> is coercable
     */
    boolean isConvertable(Class paramType);
}