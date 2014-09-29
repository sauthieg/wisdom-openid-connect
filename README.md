Wisdom OpenID Connect (PoC)
===========================

[![Build Status](https://travis-ci.org/sauthieg/wisdom-openid-connect.png)](https://travis-ci.org/sauthieg/wisdom-openid-connect)

[OpenID Connect](https://openid.net/connect) is an interoperable authentication protocol
based on the [OAuth 2.0](http://tools.ietf.org/html/rfc6749) family of specifications.

It has been designed to be lightweight and very easy to implement by
clients (web applications for example). Just playing with JSON messages and a
few HTTP redirect and you're done for user authentication!

`wisdom-openid-connect` brings the ease of use of OpenID Connect authentication to
your [wisdom framework](http://wisdom-framework.org) projects.

## Set Up

In order to use `wisdom-openid-connect` within your wisdom project, you must follow a few steps.

The first is to add it's dependency in your `pom.xml`.

```xml
<dependency>
    <groupId>org.wisdom.openid</groupId>
    <artifactId>wisdom-openid-connect</artifactId>
    <version>${wisdom-openid-connect.version}</version>
</dependency>
```

The second step is to configure your OpenID Connect Providers (OP) in your `application.conf`

```ini
openid.google.well-known https://accounts.google.com/.well-known/openid-configuration
openid.google.client_id ******************.apps.googleusercontent.com
openid.google.client_secret ******************
```

The example above shows how to declare the [Google OpenID Connect Provider](https://developers.google.com/accounts/docs/OAuth2Login).
Notice that you need to [register your client](https://developers.google.com/accounts/docs/OAuth2Login#getcredentials)
before hand to obtain a valid `client_id` and its corresponding `client_secret`.

Multiple providers can be declared as long as their property prefix are different (this is not their real ID, this one
is provided in the well-known JSON resource):

```ini
openid.google.well-known ....
openid.openam.well-known ....
openid.paypal.well-known ....
```

## Usage

If you have multiple providers, you'll have to write a login [Nascar Page](): a simple page where the user will select
the identity provider he want to use.

The authentication starts when the user access the login OpenID Connect endpoints provided by this extension: `/openid/login`.
This endpoint expects a `iss` parameter that needs to match the selected provider ID.

Example when requesting authentication with google:

```
GET /openid/login?iss=accounts.google.com
```

Usually, the hyperlink is provided using a link on the provider's logo.

### Endpoints

`/openid/login`: Starts the authentication process with the selected provider

`/openid/logout`: Clear the session's attributes related to authentication (effectively remove the markers used to
distinguish authenticated sessions from the others).

`/openid/callback`: OAuth 2.0 authorization callback that receives the provider's authorization code to exchange for
an OAUth 2.0 `access_token` and an OpenId Connect `id_token` (JWT token containing identity claims).

### Session's variables

The following variables are made available in the session's:

* `oauth2_access_token`
* `oauth2_refresh_token`
* `oauth2_id_token`
* `oauth2_name` (if `profile` scope was included in the request and the data was returned from the identity provider)
* `oauth2_given_name` (if `profile` scope was included in the request and the data was returned from the identity provider)
* `oauth2_family_name` (if `profile` scope was included in the request and the data was returned from the identity provider)
* `oauth2_picture` (if `profile` scope was included in the request and the data was returned from the identity provider)
* `oauth2_email` (if `email` scope was included in the request and the data was returned from the identity provider)
