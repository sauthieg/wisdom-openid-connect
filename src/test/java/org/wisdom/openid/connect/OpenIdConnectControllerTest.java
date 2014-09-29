package org.wisdom.openid.connect;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.wisdom.api.http.Context;
import org.wisdom.openid.connect.service.Issuer;
import org.wisdom.openid.connect.service.IssuerService;
import org.wisdom.test.parents.FakeContext;

public class OpenIdConnectControllerTest {

    @Mock
    private IssuerService issuerService;

    @Mock
    private Issuer issuer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Context.CONTEXT.set(new FakeContext());
    }

    @Test
    public void testLoginEndpoint() throws Exception {

        when(issuerService.getIssuer("issuer")).thenReturn(issuer);

        OpenIdConnectController controller = new OpenIdConnectController();
        controller.setIssuerService(issuerService);
        controller.login("issuer");
        verify(issuer).authorize(eq("https://localhost:9000/openid/callback"),
                                 anyString(),
                                 Matchers.<String>anyVararg());
    }
}