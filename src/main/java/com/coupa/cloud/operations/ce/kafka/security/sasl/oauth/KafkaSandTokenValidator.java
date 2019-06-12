/*
 * Copyright (c) 2019 Coupa Inc, All Rights Reserved.
 * Author: Jeet Parmar
 * Email: jeet@coupa.com
 * Created: June 06, 2019
 */

package com.coupa.cloud.operations.ce.kafka.security.sasl.oauth;

import com.coupa.cloud.operations.ce.sand.SandClient;
import com.coupa.cloud.operations.ce.sand.SandConfig;
import com.coupa.cloud.operations.ce.sand.SandOAuthToken;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerValidatorCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_GRANT_TYPE;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_ID;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_SCOPES;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_CLIENT_SECRET;
import static com.coupa.cloud.operations.ce.sand.SandConfig.SAND_SERVER_URL;
import static java.util.Objects.isNull;

public class KafkaSandTokenValidator implements AuthenticateCallbackHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(KafkaSandTokenValidator.class);

  private boolean configured = false;

  @Override
  public void configure(Map<String, ?> configs, String saslMechanism,
                        List<AppConfigurationEntry> jaasConfigEntries) {

    configured = SandConfig.configure(saslMechanism, jaasConfigEntries);
    LOGGER.info("Coupa: Configured Kafka client and  broker authenticator successfully");

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
      throw new IllegalStateException("Kafka client and  broker authenticator not configured");
    }

    for (Callback callback : callbacks) {
      if (callback instanceof OAuthBearerValidatorCallback) {
        try {
          OAuthBearerValidatorCallback validationCallback = (OAuthBearerValidatorCallback) callback;
          introspectSandToken(validationCallback);
        } catch (KafkaException e) {
          throw new IOException(e.getMessage(), e);
        }
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }

  private void introspectSandToken(OAuthBearerValidatorCallback callback) throws IOException{
    String accessToken = callback.tokenValue();
    if (isNull(accessToken) || accessToken.isEmpty()) {
      throw new IllegalArgumentException("Coupa: no sand token to introspect present");
    }

    LOGGER.debug("Coupa: Send token to sand server for introspection");

    Map<String, String> sandConfig = SandConfig.sandConfig;


    SandOAuthToken verified = SandClient.builder()
        .setSandBaseUrl(sandConfig.get(SAND_SERVER_URL))
        .setSandClientGrantType(sandConfig.get(SAND_CLIENT_GRANT_TYPE))
        .setSandClientId(sandConfig.get(SAND_CLIENT_ID))
        .setSandClientSecret(sandConfig.get(SAND_CLIENT_SECRET))
        .setClientScopes(Arrays
            .asList(sandConfig.get(SAND_CLIENT_SCOPES)
                .split(",")))
        .build().verifyToken(accessToken);

    LOGGER.info("Coupa: sand token verified successfully");
    callback.token(verified);
  }

}
