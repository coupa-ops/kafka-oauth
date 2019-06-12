/*
 * Copyright (c) 2019 Coupa Inc, All Rights Reserved.
 * Author: Jeet Parmar
 * Email: jeet@coupa.com
 * Created: June 06, 2019
 */

package com.coupa.cloud.operations.ce.sand;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerToken;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class SandOAuthToken implements OAuthBearerToken {

  private String value;
  private long lifetimeMs;
  private String principalName;
  private Long startTimeMs;
  private Set<String> scope;
  private long expirationTime;
  private String jti;

  public SandOAuthToken(SandCreateTokenResponse token, long startTime) {
    super();
    this.value = token.getAccessToken();
    this.principalName = token.getSubject();
    this.startTimeMs = startTime;
    this.lifetimeMs = startTime + (token.getExpiresIn() * 1000);
    this.expirationTime = startTime + (token.getExpiresIn() * 1000);
    this.jti = token.getJti();
    this.scope = new TreeSet<>(Arrays.asList(token.getScope().split(" ")));
  }

  public SandOAuthToken(SandIntrospectTokenResponse token, String accessToken) {
    super();
    this.value = accessToken;
    this.principalName = token.getSubject();
    this.lifetimeMs = token.getExpiresIn();
    this.expirationTime = token.getExpiresIn();
    this.scope = new TreeSet<>(Arrays.asList(token.getScope().split(" ")));
  }


  @Override
  public String value() {
    return value;
  }

  @Override
  public Set<String> scope() {
    return scope;
  }

  @Override
  public long lifetimeMs() {
    return lifetimeMs;
  }

  @Override
  public String principalName() {
    return principalName;
  }

  @Override
  public Long startTimeMs() {
    return startTimeMs != null ? startTimeMs : 0;
  }

  public long expirationTime() {
    return expirationTime;
  }

  public String jti() {
    return jti;
  }

  @Override
  public String toString() {
    return "SandOAuthToken{" +
        "value='" + value + '\'' +
        ", lifetimeMs=" + lifetimeMs +
        ", principalName='" + principalName + '\'' +
        ", startTimeMs=" + startTimeMs +
        ", scope=" + scope +
        ", expirationTime=" + expirationTime +
        ", jti='" + jti + '\'' +
        '}';
  }
}