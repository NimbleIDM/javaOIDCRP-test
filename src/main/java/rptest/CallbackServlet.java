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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.oidc.common.MissingRequiredAttributeException;
import org.oidc.common.ValueException;
import org.oidc.msg.DeserializationException;
import org.oidc.msg.InvalidClaimException;
import org.oidc.msg.oidc.OpenIDSchema;
import org.oidc.rp.FinalizeResponse;
import org.oidc.rp.RPHandler;
import org.oidc.service.base.RequestArgumentProcessingException;
import org.oidc.service.data.StateRecord;

public class CallbackServlet extends AbstractServlet {

  private String test;
  
  public CallbackServlet(String testName) {
    super();
    this.test = testName;
  }
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RPHandler rpHandler = rpHandlers.get(test);
    String state = request.getParameter("state");
    StateRecord stateRecord = rpHandler.getStateDb().getState(state);
    try {
      FinalizeResponse resp = rpHandler.finalize((String) stateRecord.getClaims().get("iss"), 
          request.getRequestURL() + "?" + request.getQueryString());
      StringBuilder html = new StringBuilder("<p>Response state: " + resp.getState() + "</p>");
      if (resp.indicatesError()) {
        html.append("<h1>Error response</h1>");
        html.append("<p>Error response code: " + resp.getErrorCode() + "</p>");
      } else {
        html.append("<h1>Authentication succeeded</h1>");
        OpenIDSchema userClaims = resp.getUserClaims();
        html.append("<p>Successful response, got the following claims:</p>");
        for (String key:userClaims.getClaims().keySet()) {
          html.append("<p> - " + key + " = " + userClaims.getClaims().get(key) + "</p>");
        }
      }
      html.append("<p><a href=\"" + request.getContextPath() 
        + ServletConfiguration.HOME_SERVLET_MAPPING + "\">Back to home</a></p>");
      writeHtmlBodyOutput(response, html.toString());
    } catch (MissingRequiredAttributeException | DeserializationException | ValueException 
        | InvalidClaimException | RequestArgumentProcessingException e) {
      throw new ServletException(e.getMessage(), e);
    }
  }
}
