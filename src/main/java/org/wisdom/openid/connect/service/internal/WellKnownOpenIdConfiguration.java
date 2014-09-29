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

import org.wisdom.openid.connect.service.ProviderConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by guillaume on 06/08/14.
 */
//@JsonIgnoreProperties({"revocation_endpoint"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class WellKnownOpenIdConfiguration implements ProviderConfiguration {
    private String issuer;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String registrationEndpoint;

    @JsonCreator
    public WellKnownOpenIdConfiguration(@JsonProperty("issuer") final String issuer,
                                        @JsonProperty("authorization_endpoint") final String authorizationEndpoint,
                                        @JsonProperty("token_endpoint") final String tokenEndpoint,
                                        @JsonProperty("userinfo_endpoint") final String userInfoEndpoint,
                                        @JsonProperty("registration_endpoint") final String registrationEndpoint) {
        this.issuer = issuer;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.userInfoEndpoint = userInfoEndpoint;
        this.registrationEndpoint = registrationEndpoint;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    @Override
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    @Override
    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    @Override
    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }
}
