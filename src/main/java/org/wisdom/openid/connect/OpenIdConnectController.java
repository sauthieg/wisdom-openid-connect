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

package org.wisdom.openid.connect;

import java.io.IOException;
import java.util.Random;

import org.apache.felix.ipojo.annotations.Bind;
import org.forgerock.json.jose.common.JwtReconstruction;
import org.forgerock.json.jose.jws.SignedJwt;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.QueryParameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.openid.connect.request.TokenResponse;
import org.wisdom.openid.connect.request.UserInfoRequest;
import org.wisdom.openid.connect.request.UserInfoResponse;
import org.wisdom.openid.connect.service.Issuer;
import org.wisdom.openid.connect.service.IssuerService;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by guillaume on 27/07/14.
 */
@Controller
public class OpenIdConnectController extends DefaultController {

    private String callbackUrl = "https://localhost:9000/openid/callback";
    private Random random = new Random();

    private IssuerService issuerService;

    @Bind
    public void setIssuerService(final IssuerService issuerService) {
        this.issuerService = issuerService;
    }

    @Route(method = HttpMethod.GET, uri = "/openid/login")
    public Result login(@QueryParameter("iss") String issuerId) {
        session("issuerId", issuerId);
        Issuer issuer = issuerService.getIssuer(issuerId);
        String state = String.valueOf(random.nextInt());
        flash("state", state);

        return issuer.authorize(callbackUrl, state, "openid", "profile");
    }

    @Route(method = HttpMethod.GET, uri = "/openid/callback")
    public Result callback() {
        if (request().parameter("code") != null) {
            return successfulAuthorizationResponse();
        } else {
            return errorAuthorizationResponse();
        }
    }

    @Route(method = HttpMethod.GET, uri = "/openid/logout")
    public Result logout() {
        session().getData().clear();
        return redirect("/");
    }

    private Result successfulAuthorizationResponse() {
        String issuerId = session("issuerId");
        String stateOld = flash("state");
        String state = request().parameter("state");
        /*
        TODO Add this change back
        if (!stateOld.equals(state)) {
            return internalServerError("States are different");
        }*/
        Issuer issuer = issuerService.getIssuer(issuerId);
        String code = request().parameter("code");

        TokenResponse tokenResponse = null;
        try {
            tokenResponse = issuer.authorizationToken(callbackUrl, code);
        } catch (IOException e) {
            return internalServerError(e);
        }

        // Store the token in the session for later use
        session("oauth2_access_token", tokenResponse.getAccessToken());
        session("oauth2_refresh_token", tokenResponse.getRefreshToken());
        session("oauth2_id_token", tokenResponse.getIdToken());

        UserInfoResponse response = null;
        try {
            UserInfoRequest userInfoRequest = new UserInfoRequest(issuer.getConfiguration().getUserInfoEndpoint());
            userInfoRequest.setAccessToken(tokenResponse.getAccessToken());
            response = userInfoRequest.execute();
        } catch (IOException e) {
            return internalServerError(e);
        }

        // TODO Check the subject against the id_token
        JwtReconstruction builder = new JwtReconstruction();
        SignedJwt jwt = builder.reconstructJwt(tokenResponse.getIdToken(), SignedJwt.class);
        String subject = jwt.getClaimsSet().getClaim("sub", String.class);

        if (!subject.equals(response.getSubject())) {
            return internalServerError("Subject is not the same");
        }

        saveUserInfoAttributes(response.getNode());

        // TODO store the original URI and re-use it here
        return redirect("/");
    }

    private void saveUserInfoAttributes(final JsonNode node) {
        if (node.has("name")) {
            session("oauth2_name", node.get("name").asText());
        }
        if (node.has("given_name")) {
            session("oauth2_given_name", node.get("given_name").asText());
        }
        if (node.has("family_name")) {
            session("oauth2_family_name", node.get("family_name").asText());
        }
        if (node.has("picture")) {
            session("oauth2_picture", node.get("picture").asText());
        }
        if (node.has("email")) {
            session("oauth2_email", node.get("email").asText());
        }
    }


    private Result errorAuthorizationResponse() {
        return badRequest();
    }

}
