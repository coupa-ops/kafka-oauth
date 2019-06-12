/*
 * Copyright (c) 2019 Coupa Inc, All Rights Reserved.
 * Author: Jeet Parmar
 * Email: jeet@coupa.com
 * Created: June 06, 2019
 */

package com.coupa.cloud.operations.ce.sand;


import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule.OAUTHBEARER_MECHANISM;

public class SandConfig {

  public static final String SAND_SERVER_URL = "coupa.cloud.sand.server.url";
  public static final String SAND_CLIENT_SCOPES = "coupa.cloud.sand.client.scopes";
  public static final String SAND_CLIENT_ID = "coupa.cloud.sand.client.id";
  public static final String SAND_CLIENT_SECRET = "coupa.cloud.sand.client.secret";
  public static final String SAND_CLIENT_GRANT_TYPE = "coupa.cloud.sand.client.grant_type";

  static final String SAND_API_PATH_TOKEN = "/oauth2/token";
  static final String SAND_API_PATH_TOKEN_INTROSPECT = "/oauth2/introspect";

  private final static String COUPA_SAND_CONFIG_PATH = "/etc/coupa/sand.properties";

  public static Map<String, String> sandConfig = new HashMap<>();

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SandConfig.class);

  private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
      .retryOnConnectionFailure(true)
      .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
      .readTimeout(5, TimeUnit.SECONDS)
      .build();


  static SelfExpiringCache<String, SandIntrospectTokenResponse> cache =
      new SelfExpiringCache<>(59 * 60 * 1000); // cached for 59 mins

  private SandConfig() {

  }

  public static OkHttpClient getHttpClient() {
    return okHttpClient;
  }

  public static boolean configure(String saslMechanism,
                                  List<AppConfigurationEntry> jaasConfigEntries) {

    if (!OAUTHBEARER_MECHANISM.equals(saslMechanism)) {
      LOGGER.error("Coupa: Unsupported SASL mechanism for Sand server");

      throw new IllegalArgumentException(
          String.format("Unsupported SASL mechanism for Sand server: %s please use %s ",
              saslMechanism, OAUTHBEARER_MECHANISM));
    }

    if (Objects.requireNonNull(jaasConfigEntries).size() != 1 ||
        jaasConfigEntries.get(0) == null) {

      LOGGER.error("Coupa: Must supply exactly 1 non-null JAAS mechanism configuration");

      throw new IllegalArgumentException(
          String.format("Must supply exactly 1 non-null JAAS mechanism configuration (size was %d)",
              jaasConfigEntries.size()));
    }

    readSandConfig();

    return true;
  }


  private static void readSandConfig() {

    String env = System.getenv("COUPA_SAND_CONFIG_PATH");

    if (!isNull(env) && !env.isEmpty()) {
      File configFile = new File(env);
      if (configFile.exists() && !configFile.isDirectory()) {
        putConfig(configFile);
        return;
      } else {
        LOGGER.error("Coupa: could not read sand config file from  COUPA_SAND_CONFIG_PATH" +
                "exists or has wrong permissions try setting COUPA_SAND_CONFIG_PATH to valid path" +
                " {} "
            , env);
        throw new IllegalArgumentException("Coupa: could not read sand config file from env var");
      }
    }

    LOGGER.debug("Coupa: Sand env config not set falling back to default path");

    File configFile = new File(COUPA_SAND_CONFIG_PATH);
    if (configFile.exists() && !configFile.isDirectory()) {
      putConfig(configFile);
      return;
    }

    LOGGER.error("Coupa: could not read sand config file from  COUPA_SAND_CONFIG_PATH or {} may " +
        "be file does not " +
        "exists or has wrong permissions try setting COUPA_SAND_CONFIG_PATH in env or configure " +
        "file at {} ", COUPA_SAND_CONFIG_PATH, COUPA_SAND_CONFIG_PATH);

    throw new IllegalArgumentException("Coupa: could not read sand config file ");
  }

  private static void putConfig(File configFile) {
    try (InputStream input = new FileInputStream(configFile)) {

      Properties configs = new Properties();
      configs.load(input);

      sandConfig.put(SAND_SERVER_URL,
          checkIfPropertyIsSet(SAND_SERVER_URL, configs.get(SAND_SERVER_URL)));
      sandConfig.put(SAND_CLIENT_GRANT_TYPE,
          checkIfPropertyIsSet(SAND_CLIENT_GRANT_TYPE, configs.get(SAND_CLIENT_GRANT_TYPE)));
      sandConfig.put(SAND_CLIENT_SCOPES,
          checkIfPropertyIsSet(SAND_CLIENT_SCOPES, configs.get(SAND_CLIENT_SCOPES)));
      sandConfig.put(SAND_CLIENT_ID,
          checkIfPropertyIsSet(SAND_CLIENT_ID, configs.get(SAND_CLIENT_ID)));
      sandConfig.put(SAND_CLIENT_SECRET,
          checkIfPropertyIsSet(SAND_CLIENT_SECRET, configs.get(SAND_CLIENT_SECRET)));

    } catch (IOException ex) {
      LOGGER.error("Coupa: could not read prop file", ex);
    }
  }

  private static String checkIfPropertyIsSet(String key, Object prop) {

    if (isNull(prop) || ((String) prop).isEmpty()) {
      throw new IllegalArgumentException("Coupa : property " + key + " is not set in server" +
          ".properties");
    }
    return (String) prop;
  }
}
