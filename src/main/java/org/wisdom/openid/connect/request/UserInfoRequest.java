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

package org.wisdom.openid.connect.request;

import static java.lang.String.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by guillaume on 07/08/14.
 */
public class UserInfoRequest {
    private final URL userInfoEndpoint;
    private String accessToken;

    public UserInfoRequest(final String userInfoEndpoint) throws MalformedURLException {
        this(new URL(userInfoEndpoint));
    }

    public UserInfoRequest(final URL userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    public UserInfoResponse execute() throws IOException {

        HttpURLConnection connection = (HttpURLConnection) userInfoEndpoint.openConnection();
        connection.addRequestProperty("Authorization", format("Bearer %s", accessToken));

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //Get Response
        JsonNode node = new ObjectMapper().readTree(connection.getInputStream());
        return new UserInfoResponse(node.get("sub").asText(), node);

    }
}
