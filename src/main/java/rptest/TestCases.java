/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rptest;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ListUtils;

public final class TestCases {

  public static final List<String> C_MANDATORY = Arrays.asList("rp-response_type-code",
      "rp-scope-userinfo-claims",
      "rp-nonce-invalid",
      "rp-token_endpoint-client_secret_basic",
      "rp-id_token-kid-absent-single-jwks",
      "rp-id_token-iat",
      "rp-id_token-aud",
      "rp-id_token-kid-absent-multiple-jwks",
      "rp-id_token-sig-none",
      "rp-id_token-sig-rs256",
      "rp-id_token-sub",
      "rp-id_token-bad-sig-rs256",
      "rp-id_token-issuer-mismatch",
      "rp-userinfo-bad-sub-claim",
      "rp-userinfo-bearer-header");
  
  public static final List<String> CI_MANDATORY = Arrays.asList("rp-response_type-code+id_token",
      "rp-scope-userinfo-claims",
      "rp-nonce-invalid",
      "rp-nonce-unless-code-flow",
      "rp-token_endpoint-client_secret_basic",
      "rp-id_token-kid-absent-single-jwks",
      "rp-id_token-iat",
      "rp-id_token-aud",
      "rp-id_token-kid-absent-multiple-jwks",
      "rp-id_token-missing-c_hash",
      "rp-id_token-sig-rs256",
      "rp-id_token-sub",
      "rp-id_token-bad-c_hash",
      "rp-id_token-bad-sig-rs256",
      "rp-id_token-issuer-mismatch",
      "rp-userinfo-bad-sub-claim",
      "rp-userinfo-bearer-header");
  
  public static final List<String> CIT_MANDATORY = Arrays.asList("rp-response_type-code+id_token+token",
      "rp-scope-userinfo-claims",
      "rp-nonce-invalid",
      "rp-nonce-unless-code-flow",
      "rp-token_endpoint-client_secret_basic",
      "rp-id_token-kid-absent-single-jwks",
      "rp-id_token-iat",
      "rp-id_token-aud",
      "rp-id_token-kid-absent-multiple-jwks",
      "rp-id_token-missing-c_hash",
      "rp-id_token-missing-at_hash",
      "rp-id_token-sig-rs256",
      "rp-id_token-bad-at_hash",
      "rp-id_token-sub",
      "rp-id_token-bad-c_hash",
      "rp-id_token-bad-sig-rs256",
      "rp-id_token-issuer-mismatch",
      "rp-userinfo-bad-sub-claim",
      "rp-userinfo-bearer-header");
  
  public static final List<String> CT_MANDATORY = Arrays.asList("rp-response_type-code+token",
      "rp-scope-userinfo-claims",
      "rp-nonce-invalid",
      "rp-nonce-unless-code-flow",
      "rp-token_endpoint-client_secret_basic",
      "rp-id_token-kid-absent-single-jwks",
      "rp-id_token-iat",
      "rp-id_token-aud",
      "rp-id_token-kid-absent-multiple-jwks",
      "rp-id_token-sig-rs256",
      "rp-id_token-sub",
      "rp-id_token-bad-sig-rs256",
      "rp-id_token-issuer-mismatch",
      "rp-userinfo-bad-sub-claim",
      "rp-userinfo-bearer-header");
  
  public static final List<String> I_MANDATORY = Arrays.asList("rp-response_type-id_token",
      "rp-scope-userinfo-claims",
      "rp-nonce-invalid",
      "rp-nonce-unless-code-flow",
      "rp-id_token-kid-absent-single-jwks",
      "rp-id_token-iat",
      "rp-id_token-aud",
      "rp-id_token-kid-absent-multiple-jwks",
      "rp-id_token-sig-rs256",
      "rp-id_token-sub",
      "rp-id_token-bad-sig-rs256",
      "rp-id_token-issuer-mismatch");
  
  public static final List<String> IT_MANDATORY = Arrays.asList("rp-response_type-id_token+token",
      "rp-scope-userinfo-claims",
      "rp-nonce-invalid",
      "rp-nonce-unless-code-flow",
      "rp-id_token-kid-absent-single-jwks",
      "rp-id_token-iat",
      "rp-id_token-aud",
      "rp-id_token-kid-absent-multiple-jwks",
      "rp-id_token-missing-at_hash",
      "rp-id_token-sig-rs256",
      "rp-id_token-bad-at_hash",
      "rp-id_token-sub",
      "rp-id_token-bad-sig-rs256",
      "rp-id_token-issuer-mismatch",
      "rp-userinfo-bad-sub-claim",
      "rp-userinfo-bearer-header");
  
  public static final List<String> OPTIONAL_FOR_ALL = Arrays.asList("rp-discovery-webfinger-acct",
      "rp-discovery-webfinger-http-href",
      "rp-discovery-webfinger-url",
      "rp-discovery-openid-configuration",
      "rp-discovery-jwks_uri-keys",
      "rp-discovery-issuer-not-matching-config",
      "rp-discovery-webfinger-unknown-member",
      "rp-registration-dynamic",
      "rp-response_mode-form_post-error",
      "rp-response_mode-form_post",
      "rp-request_uri-enc",
      "rp-request_uri-sig",
      "rp-request_uri-sig+enc",
      "rp-request_uri-unsigned",
      "rp-id_token-sig+enc",
      "rp-id_token-sig-hs256",
      "rp-id_token-sig-es256",
      "rp-id_token-sig+enc-a128kw",
      "rp-id_token-bad-sig-hs256",
      "rp-id_token-bad-sig-es256",
      "rp-key-rotation-op-sign-key-native",
      "rp-key-rotation-op-sign-key",
      "rp-key-rotation-op-enc-key",
      "rp-3rd_party-init-login");
  
  @SuppressWarnings("unchecked")
  public static final List<String> C_OPTIONAL = ListUtils.union(OPTIONAL_FOR_ALL, Arrays.asList(
      "rp-token_endpoint-private_key_jwt",
      "rp-token_endpoint-client_secret_post",
      "rp-token_endpoint-client_secret_jwt",
      "rp-claims-distributed",
      "rp-claims-aggregated",
      "rp-userinfo-sig",
      "rp-userinfo-bearer-body",
      "rp-userinfo-enc",
      "rp-userinfo-sig+enc"));

  public static final List<String> CI_OPTIONAL = C_OPTIONAL;

  public static final List<String> CIT_OPTIONAL = C_OPTIONAL;

  @SuppressWarnings("unchecked")
  public static final List<String> CT_OPTIONAL = ListUtils.union(OPTIONAL_FOR_ALL, Arrays.asList(
      "rp-claims-distributed",
      "rp-claims-aggregated",
      "rp-userinfo-sig",
      "rp-userinfo-bearer-body",
      "rp-userinfo-enc",
      "rp-userinfo-sig+enc"));

  public static final List<String> I_OPTIONAL = OPTIONAL_FOR_ALL;

  public static final List<String> IT_OPTIONAL = CT_OPTIONAL;
  
}
