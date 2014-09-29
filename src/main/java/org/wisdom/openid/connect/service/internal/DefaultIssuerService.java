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

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.openid.connect.service.Issuer;
import org.wisdom.openid.connect.service.IssuerService;

/**
 * Created by guillaume on 07/08/14.
 */
@Component
@Provides
@Instantiate
public class DefaultIssuerService implements IssuerService {

    private Map<String, Issuer> issuers = new HashMap<>();

    @Override
    public Issuer getIssuer(final String identifier) {
        return issuers.get(identifier);
    }

    @Bind(aggregate = true, optional = true)
    public void addIssuer(final Issuer issuer, final Map<String, Object> properties) {
        String id = (String) properties.get(Issuer.ID);
        issuers.put(id, issuer);
    }
}
