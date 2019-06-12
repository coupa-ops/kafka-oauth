package com.coupa.cloud.operations.ce.sand;


import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.kafka.common.errors.AuthenticationException;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerValidationResult;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.coupa.cloud.operations.ce.sand.SandConfig.getHttpClient;
import static java.util.Objects.isNull;

public class SandClient {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SandClient.class);

  private String sandBaseUrl;
  private String sandClientId;
  private String sandClientSecret;
  private String sandClientGrantType;
  private List<String> clientScopes;




  public SandClient(Builder sandConfigBuilder) {
    this.sandBaseUrl = sandConfigBuilder.getSandBaseUrl();
    this.sandClientId = sandConfigBuilder.getSandClientId();
    this.sandClientSecret = sandConfigBuilder.getSandClientSecret();
    this.clientScopes = sandConfigBuilder.getClientScopes();
    this.sandClientGrantType = sandConfigBuilder.getSandClientGrantType();
  }

  public static Builder builder() {
    return new Builder();
  }


  public SandOAuthToken verifyToken(String accessToken) throws IOException {

    SandIntrospectTokenResponse cachedResponse = SandConfig.cache.get(accessToken);
    if(!isNull(cachedResponse)){
      LOGGER.debug("Coupa: verified from cache");
       verifyInternal(cachedResponse);
    }

    Request verifySandToken = new Request.Builder()
        .url(this.sandBaseUrl + SandConfig.SAND_API_PATH_TOKEN_INTROSPECT)
        .post(buildVerifyTokenBody(accessToken))
        .header("Authorization", Credentials.basic(this.sandClientId,  this.sandClientSecret))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("charset", "utf-8")
        .build();

    Response verifyResponse = getHttpClient().newCall(verifySandToken).execute();

    if (!verifyResponse.isSuccessful()) {

      LOGGER.error("Coupa: error introspecting token from sand server" +
          " response code: {} response: {}", verifyResponse.code(), verifyResponse.body().string());

      throw new AuthenticationException("Coupa: error introspecting token from sand server");
    }

    if (isNull(verifyResponse.body())) {
      throw new AuthenticationException("Coupa: server did not send any response to introspect " +
          "request");
    }

    ObjectMapper objectMapper = new ObjectMapper();
    SandIntrospectTokenResponse response = objectMapper.readValue(verifyResponse.body().string(),
        SandIntrospectTokenResponse.class);

    LOGGER.debug("Coupa : SandIntrospectTokenResponse: {}",
        response.toString());

    SandConfig.cache.put(accessToken, response);

    verifyInternal(response);

    return new SandOAuthToken(response, accessToken);
  }

  private void verifyInternal(SandIntrospectTokenResponse response) {

    if (!response.getIssuer().equals(this.sandBaseUrl)) {
      throw new AuthenticationException("Coupa: Invalid token issuer");
    }

    if (!response.isActive()) {
      throw new AuthenticationException("Coupa: sand token is not active");
    }


    if ((Time.SYSTEM.milliseconds()) > (response.getExpiresIn() * 1000)) {
      OAuthBearerValidationResult.newFailure("Coupa: sand token expired!");
      throw new AuthenticationException("Coupa: sand token expired at " + response.getExpiresIn());
    }
  }


  public SandOAuthToken createToken() throws IOException {
    Request createSandToken = new Request.Builder()
        .url(this.sandBaseUrl + SandConfig.SAND_API_PATH_TOKEN)
        .post(buildCreateTokenBody())
        .header("Authorization", Credentials.basic(this.sandClientId,  this.sandClientSecret))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .header("charset", "utf-8")
        .build();

    long startTime = Time.SYSTEM.milliseconds();
    Response sandResponse = getHttpClient().newCall(createSandToken).execute();
    if (!sandResponse.isSuccessful()) {

      LOGGER.error("Coupa: error creating token from sand server" +
          " response code: {} response: {}", sandResponse.code(), sandResponse.body().string());

      throw new AuthenticationException("Coupa: error creating token from sand server");
    }

    if (isNull(sandResponse.body())) {
      throw new AuthenticationException("Coupa: server did not send any response");
    }

/*    LOGGER.debug("Coupa : raw response from sand server {}" ,
        sandResponse.body().string());*/

    ObjectMapper objectMapper = new ObjectMapper();

    SandCreateTokenResponse response = objectMapper.readValue(sandResponse.body().string(),
        SandCreateTokenResponse.class);
    response.setSubject(this.sandClientId);


    LOGGER.debug("Coupa : SandCreateTokenResponse {}",
        response.toString());

    return new SandOAuthToken(response, startTime);

  }

  private RequestBody buildCreateTokenBody() {
    return new FormBody.Builder()
        .add("grant_type", this.sandClientGrantType)
        .add("scope", String.join(" ", this.clientScopes))
        .build();

  }

  private RequestBody buildVerifyTokenBody(String access_token) {
    return new FormBody.Builder()
        .add("token", access_token)
        .build();
  }

  public static final class Builder {

    private String sandBaseUrl;
    private String sandClientId;
    private String sandClientSecret;
    private String sandClientTokenPath;
    private String sandClientGrantType;

    private List<String> clientScopes;

    private String getSandBaseUrl() {
      return sandBaseUrl;
    }

    private String getSandClientId() {
      return sandClientId;
    }

    private String getSandClientSecret() {
      return sandClientSecret;
    }


    private String getSandClientTokenPath() {
      return sandClientTokenPath;
    }

    private String getSandClientGrantType() {
      return sandClientGrantType;
    }


    private List<String> getClientScopes() {
      return clientScopes;
    }

    public Builder setSandBaseUrl(String sandBaseUrl) {
      this.sandBaseUrl = sandBaseUrl;
      return this;
    }

    public Builder setSandClientId(String sandClientId) {
      this.sandClientId = sandClientId;
      return this;

    }

    public Builder setSandClientGrantType(String sandClientGrantType) {
      this.sandClientGrantType = sandClientGrantType;
      return this;
    }


    public Builder setSandClientSecret(String sandClientSecret) {
      this.sandClientSecret = sandClientSecret;
      return this;

    }

    public Builder setSandClientTokenPath(String sandClientTokenPath) {
      this.sandClientTokenPath = sandClientTokenPath;
      return this;
    }

    public Builder setClientScopes(List<String> clientScopes) {
      this.clientScopes = clientScopes;
      return this;
    }


    public SandClient build() {
      return new SandClient(this);
    }

  }

}
