package com.coupa.cloud.operations.ce.sand;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class SandIntrospectTokenResponse {


  @JsonProperty("active")
  private boolean active;

  @JsonAlias( {"exp", "expires_in"})
  private long expiresIn;

  @JsonProperty("scope")
  private String scope;

  @JsonProperty("client_id")
  private String clientId;

  @JsonAlias({"sub", "username"})
  private String subject;

  @JsonProperty("iat")
  private long issuedAt;

  @JsonProperty("iss")
  private String issuer;

  @JsonProperty("aud")
  private String audiance;


  public long getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(long issuedAt) {
    this.issuedAt = issuedAt;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public String getAudiance() {
    return audiance;
  }

  public void setAudiance(String audiance) {
    this.audiance = audiance;
  }


  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
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


  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }


  @Override
  public String toString() {
    return "active: " + isActive()
        + " expires: " + getExpiresIn()
        + " scope: " + getScope()
        + " client_id: " + getClientId();
  }
}
