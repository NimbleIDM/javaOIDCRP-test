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

}
