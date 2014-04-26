/*
 * Copyright 2013-2014, ApiFest project
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

package com.apifest.oauth20;

import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class ResponseTest {

    @Test
    public void when_get_not_found_response_return_response_with_404_status() throws Exception {
        // WHEN
        HttpResponse response = Response.createNotFoundResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.NOT_FOUND);
        assertEquals(response.headers().get(HttpHeaders.Names.CACHE_CONTROL),
                HttpHeaders.Values.NO_STORE);
        assertEquals(response.headers().get(HttpHeaders.Names.PRAGMA), HttpHeaders.Values.NO_CACHE);
    }

    @Test
    public void when_get_exception_response_get_exception_HTTP_status() throws Exception {
        // GIVEN
        OAuthException ex = mock(OAuthException.class);
        willReturn(Response.APPNAME_OR_SCOPE_IS_NULL).given(ex).getMessage();
        willReturn(HttpResponseStatus.BAD_REQUEST).given(ex).getHttpStatus();

        // WHEN
        HttpResponse response = Response.createOAuthExceptionResponse(ex);

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.BAD_REQUEST);
        assertEquals(response.headers().get(HttpHeaders.Names.CACHE_CONTROL),
                HttpHeaders.Values.NO_STORE);
        assertEquals(response.headers().get(HttpHeaders.Names.PRAGMA), HttpHeaders.Values.NO_CACHE);
        verify(ex).getMessage();
    }
}
