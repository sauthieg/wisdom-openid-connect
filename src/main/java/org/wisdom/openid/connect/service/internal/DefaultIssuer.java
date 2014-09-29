/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 Guillaume Sauthier
 */

package org.wisdom.openid.connect.service.internal;

import static java.lang.String.*;
import static java.util.Arrays.*;
import static org.wisdom.openid.connect.util.Urls.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.wisdom.api.http.Result;
import org.wisdom.api.http.Results;
import org.wisdom.openid.connect.request.TokenResponse;
import org.wisdom.openid.connect.service.Issuer;
import org.wisdom.openid.connect.service.ProviderConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by guillaume on 28/09/14.
 */
@Component
@Provides
public class DefaultIssuer implements Issuer {

    public static final String OPENID_SCOPE = "openid";
    private final String endpointUrl;
    private final String clientId;
    private final String clientSecret;
    private WellKnownOpenIdConfiguration config;

    @ServiceProperty(name = Issuer.ID)
    private String issuerId;

    public DefaultIssuer(@Property(name = "well-known-endpoint") String url,
                         @Property(name = "client-id") String clientId,
                         @Property(name = "client-secret") String clientSecret) {
        this.endpointUrl = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Validate
    public void start() throws Exception {
        URL endpoint = new URL(endpointUrl);
        URLConnection connection = endpoint.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        InputStream stream = connection.getInputStream();
        config = new ObjectMapper().readValue(stream, WellKnownOpenIdConfiguration.class);
        stream.close();
        issuerId = config.getIssuer();
    }

    @Override
    public Result authorize(final String redirectUrl, final String state, final String... scopes) {
        List<String> tokens = new ArrayList<>(asList(scopes));
        if (!tokens.contains(OPENID_SCOPE)) {
            tokens.add(OPENID_SCOPE);
        }
        return authorize(redirectUrl, state, tokens);
    }

    private Result authorize(final String redirectUrl, final String state, final List<String> scopes) {
        StringBuilder sb = new StringBuilder();
        sb.append(config.getAuthorizationEndpoint());
        sb.append("?response_type=code");
        sb.append(format("&scope=%s", encode(join(" ", scopes))));
        sb.append(format("&client_id=%s", clientId));
        sb.append(format("&state=%s", state));
        sb.append(format("&redirect_uri=%s", encode(redirectUrl)));
        return Results.redirect(sb.toString());
    }

    @Override
    public TokenResponse authorizationToken(final String redirectUrl, final String code) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("grant_type=authorization_code");
        sb.append(format("&code=%s", code));
        sb.append(format("&client_id=%s", clientId));
        sb.append(format("&client_secret=%s", clientSecret));
        sb.append(format("&redirect_uri=%s", redirectUrl));

        String parameters = sb.toString();

        URL tokenEndpoint = new URL(config.getTokenEndpoint());
        HttpURLConnection connection = (HttpURLConnection) tokenEndpoint.openConnection();
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //connection.addRequestProperty("Authorization", format("Basic %s", credentials()));
        connection.setRequestProperty("Content-Length", valueOf(parameters.getBytes().length));

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Send request
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(parameters);
        wr.flush();
        wr.close();

        //Get Response
        return buildTokenResponse(connection.getInputStream());
    }

    private TokenResponse buildTokenResponse(final InputStream stream) throws IOException {
        return new ObjectMapper().readValue(stream, TokenResponse.class);
    }

    @Override
    public ProviderConfiguration getConfiguration() {
        return config;
    }
}
