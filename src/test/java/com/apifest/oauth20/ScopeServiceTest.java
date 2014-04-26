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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class ScopeServiceTest {

    ScopeService service;

    @BeforeMethod
    public void setup() {
        service = spy(new ScopeService());
        MockDBManagerFactory.install();
    }

    @Test
    public void when_client_id_query_param_invoke_get_scopes_by_clientId() throws Exception {
        // GIVEN
        String clientId = "826064099791766";
        HttpRequest req = mock(HttpRequest.class);
        willReturn("http://localhost:8080/oauth20/scope?client_id=" + clientId).given(req).getUri();
        willReturn(null).given(service).getScopes(clientId);

        // WHEN
        service.getScopes(req);

        // THEN
        verify(service).getScopes(clientId);
    }

    @Test
    public void when_no_client_id_query_param_get_all_scopes() throws Exception {
        // GIVEN
        HttpRequest req = mock(HttpRequest.class);
        willReturn("http://localhost:8080/oauth20/scope").given(req).getUri();

        // WHEN
        service.getScopes(req);

        // THEN
        verify(service, never()).getScopes(anyString());
    }

    @Test
    public void when_get_all_scopes_invoke_load_all_scopes() throws Exception {
        // GIVEN
        HttpRequest req = mock(HttpRequest.class);
        willReturn("http://localhost:8080/oauth20/scope").given(req).getUri();

        // WHEN
        service.getScopes(req);

        // THEN
        verify(DBManagerFactory.dbManager).getAllScopes();
    }

    @Test
    public void when_get_client_scopes_invoke_scopes_for_that_client_only() throws Exception {
        // GIVEN
        String clientId = "826064099791766";
        HttpRequest req = mock(HttpRequest.class);
        willReturn("http://localhost:8080/oauth20/scope?client_id=" + clientId).given(req).getUri();
        String scope = "basic";
        ClientCredentials creds = new ClientCredentials("appName", scope);
        willReturn(creds).given(DBManagerFactory.dbManager).findClientCredentials(clientId);

        // WHEN
        service.getScopes(req);

        // THEN
        verify(DBManagerFactory.dbManager).findClientCredentials(clientId);
        verify(DBManagerFactory.dbManager).findScope("basic");
    }

    @Test
    public void when_client_has_several_scopes_invoke_get_scope_details_for_each_scope()
            throws Exception {
        // GIVEN
        String clientId = "826064099791766";
        HttpRequest req = mock(HttpRequest.class);
        willReturn("http://localhost:8080/oauth20/scope?client_id=" + clientId).given(req).getUri();
        String scope = "basic,extended";
        ClientCredentials creds = new ClientCredentials("appName", scope);
        willReturn(creds).given(DBManagerFactory.dbManager).findClientCredentials(clientId);

        // WHEN
        service.getScopes(req);

        // THEN
        verify(DBManagerFactory.dbManager).findClientCredentials(clientId);
        verify(DBManagerFactory.dbManager).findScope("basic");
        verify(DBManagerFactory.dbManager).findScope("extended");
    }

    @Test
    public void when_scope_is_null_get_client_app_scope() throws Exception {
        // GIVEN
        String clientId = "826064099791766";
        MockDBManagerFactory.install();
        ClientCredentials creds = mock(ClientCredentials.class);
        willReturn("basic").given(creds).getScope();
        willReturn(creds).given(DBManagerFactory.getInstance()).findClientCredentials(clientId);

        // WHEN
        String scope = service.getValidScope(null, clientId);

        // THEN
        assertEquals(scope, "basic");
    }

    @Test
    public void when_scope_is_valid_check_client_app_scope_contains_it() throws Exception {
        // GIVEN
        String clientId = "826064099791766";
        MockDBManagerFactory.install();
        ClientCredentials creds = mock(ClientCredentials.class);
        willReturn("extended,basic").given(creds).getScope();
        willReturn(creds).given(DBManagerFactory.getInstance()).findClientCredentials(clientId);

        // WHEN
        String scope = service.getValidScope("basic", clientId);

        // THEN
        assertEquals(scope, "basic");
    }

    @Test
    public void when_scope_is_not_contained_in_client_app_scope_return_null() throws Exception {
        // GIVEN
        String clientId = "826064099791766";
        MockDBManagerFactory.install();
        ClientCredentials creds = mock(ClientCredentials.class);
        willReturn("basic").given(creds).getScope();
        willReturn(creds).given(DBManagerFactory.getInstance()).findClientCredentials(clientId);

        // WHEN
        String scope = service.getValidScope("extended", clientId);

        // THEN
        assertNull(scope);
    }

    @Test
    public void when_scope_is_contained_return_true() throws Exception {
        // GIVEN
        String scope = "extended";
        String scopeList = "basic,extended";

        // WHEN
        boolean allowed = service.scopeAllowed(scope, scopeList);

        // THEN
        assertTrue(allowed);
    }

    @Test
    public void when_scope_is_not_contained_return_false() throws Exception {
        // GIVEN
        String scope = "payment";
        String scopeList = "basic,extended";

        // WHEN
        boolean allowed = service.scopeAllowed(scope, scopeList);

        // THEN
        assertFalse(allowed);
    }
}
