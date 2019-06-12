package com.coupa.cloud.operations.ce.sand;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class SandCreateTokenResponse {


  @JsonProperty("access_token")
  private String accessToken;

  @JsonAlias( {"exp", "expires_in"})
  private long expiresIn;

  @JsonProperty("scope")
  private String scope;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("sub")
  private String subject;

  @JsonProperty("iat")
  private long issuedAt;

  @JsonProperty("jti")
  private String jti;


  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public long getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(long issuedAt) {
    this.issuedAt = issuedAt;
  }

  public String getJti() {
    return jti;
  }

  public void setJti(String jti) {
    this.jti = jti;
  }

  @Override
  public String toString() {
    return "token: " + getAccessToken()
        + "sub: " + getSubject()
        + " expires: " + getExpiresIn()
        + " scope: " + getScope()
        + " type: " + getTokenType();
  }
}
