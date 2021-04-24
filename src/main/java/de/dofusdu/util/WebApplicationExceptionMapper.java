/*
 * Copyright 2021 Christopher Sieh (stelzo@steado.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dofusdu.util;


import org.jboss.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger log = Logger.getLogger(WebApplicationExceptionMapper.class.getName());

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        if (status >= 400 && status < 500) {
            log.warn("Client error " + status + ": " + exception.getMessage());
        } else {
            log.error("Error " + status, exception);
        }
        return exception.getResponse();
    }

}