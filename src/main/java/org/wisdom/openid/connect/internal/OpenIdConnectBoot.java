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

package org.wisdom.openid.connect.internal;

import static java.lang.String.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.extender.DeclarationBuilderService;
import org.apache.felix.ipojo.extender.DeclarationHandle;
import org.apache.felix.ipojo.extender.InstanceBuilder;
import org.wisdom.api.configuration.ApplicationConfiguration;
import org.wisdom.api.configuration.Configuration;
import org.wisdom.openid.connect.service.internal.DefaultIssuer;

/**
 * <pre>
 *     {@code
 *     openid.google.well-known https://.....
 *     openid.openam.well-known https://.....
 *     }
 * </pre>
 */
@Component
@Instantiate
public class OpenIdConnectBoot {

    private final static Pattern PROVIDER_ID = Pattern.compile("^(.*)\\.well-known");

    private final Configuration config;
    private final DeclarationBuilderService declarationBuilderService;
    private final List<DeclarationHandle> handles = new ArrayList<>();

    public OpenIdConnectBoot(@Requires ApplicationConfiguration app,
                             @Requires DeclarationBuilderService declarationBuilderService) {
        config = app.getConfiguration("openid");
        this.declarationBuilderService = declarationBuilderService;
    }

    @Validate
    public void start() {
        // Creates well known issuer
        if (config != null) {
            for (String key : config.asMap().keySet()) {
                if (key.endsWith(".well-known")) {
                    provisionInstance(key, config);
                }
            }
        }
    }

    @Invalidate
    public void stop() {
        // Retract created handles
        for (DeclarationHandle handle : handles) {
            handle.retract();
        }
    }

    private void provisionInstance(final String key, final Configuration config) {

        String id = getId(key);
        String endpoint = config.get(key);
        String clientId = config.get(format("%s.client_id", id));
        String clientSecret = config.get(format("%s.client_secret", id));
        // Get a fresh instance builder
        InstanceBuilder builder = declarationBuilderService.newInstance(DefaultIssuer.class.getName());

        DeclarationHandle handle = builder.name(format("openid-connect-provider-%s", id))
                                          .configure()
                                          .property("well-known-endpoint", endpoint)
                                          .property("client-id", clientId)
                                          .property("client-secret", clientSecret)
                                          .build();

        // Push the InstanceDeclaration service in the registry
        handle.publish();

    }

    private String getId(final String key) {
        Matcher matcher = PROVIDER_ID.matcher(key);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException();
    }
}
