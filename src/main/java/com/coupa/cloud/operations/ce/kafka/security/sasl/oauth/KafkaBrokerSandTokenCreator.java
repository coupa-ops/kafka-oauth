/*
 * Copyright (c) 2019 Coupa Inc, All Rights Reserved.
 * Author: Jeet Parmar
 * Email: jeet@coupa.com
 * Created: June 06, 2019
 */

package com.coupa.cloud.operations.ce.kafka.security.sasl.oauth;

import com.coupa.cloud.operations.ce.sand.SandConfig;
import com.coupa.cloud.operations.ce.sand.SandClient;
import com.coupa.cloud.operations.ce.sand.SandOAuthToken;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_GRANT_TYPE;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_ID;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_SCOPES;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_SECRET;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_SERVER_URL;

public class KafkaBrokerSandTokenCreator implements AuthenticateCallbackHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaBrokerSandTokenCreator.class);

  private boolean configured = false;



  @Override
  public void configure(Map<String, ?> configs, String saslMechanism,
                        List<AppConfigurationEntry> jaasConfigEntries)  {

    configured = SandConfig.configure(saslMechanism, jaasConfigEntries);

    LOGGER.info("Coupa: Configured Kafka broker oauth token creator successfully");

  }

  private boolean isConfigured() {
    return this.configured;
  }

  @Override
  public void close() {
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

    if (!isConfigured()) {
      throw new IllegalStateException("Coupa: Kafka broker oauth token creator not configured");
    }

    for (Callback callback : callbacks) {

      if (callback instanceof OAuthBearerTokenCallback) {
        tryToSetCoupaSandToken((OAuthBearerTokenCallback) callback);
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }

  }

  private void tryToSetCoupaSandToken(OAuthBearerTokenCallback callback) throws IOException {

    if (callback.token() != null) {
      throw new IllegalArgumentException("Coupa: sand token is already set.");
    }

    LOGGER.info("Coupa: Getting token from SAND server.");
    Map<String, String> sandConfig = SandConfig.sandConfig;

    SandOAuthToken token = SandClient.builder()
        .setSandBaseUrl(sandConfig.get(SAND_SERVER_URL))
        .setSandClientGrantType(sandConfig.get(SAND_CLIENT_GRANT_TYPE))
        .setSandClientId(sandConfig.get(SAND_CLIENT_ID))
        .setSandClientSecret(sandConfig.get(SAND_CLIENT_SECRET))
        .setClientScopes(Arrays
            .asList(sandConfig.get(SAND_CLIENT_SCOPES)
                .split(",")))
        .build().createToken();


    LOGGER.debug("Coupa: {} Token received from SAND server", token);

    if (token == null) {
      throw new IllegalArgumentException("Coupa: Failed to get token from SAND server.");
    }
    callback.token(token);
  }

}
