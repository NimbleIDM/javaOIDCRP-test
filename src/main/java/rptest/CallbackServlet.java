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
  
  private String responseType;
  
  public CallbackServlet(String testName, String respType) {
    super();
    this.test = testName;
    this.responseType = respType;
  }
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RPHandler rpHandler = rpHandlers.get(responseType).get(test);
    String state = request.getParameter("state");
    StringBuilder html = new StringBuilder();
    StateRecord stateRecord = rpHandler.getStateDb().getState(state);
    if (state == null || stateRecord == null) {
      html.append("<script>"
          + "var hash = window.location.hash.substr(1);\n" + 
           "var url = window.location.href;\n" +
           "window.location.replace(url.replace('#','?'));"
           + "</script>");
      writeHtmlBodyOutput(response, html.toString());
      return;
    }
    if (stateRecord != null) {
    try {
      FinalizeResponse resp = rpHandler.finalize((String) stateRecord.getClaims().get("iss"), 
          request.getRequestURL() + "?" + request.getQueryString());
      html.append("<p>Response state: " + resp.getState() + "</p>");
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
    } catch (MissingRequiredAttributeException | DeserializationException | ValueException 
        | InvalidClaimException | RequestArgumentProcessingException e) {
      html.append("<h1>Catched error</h1>");
      html.append("<p>" + e.getMessage() + "</p>");
    }
    }
    String result = (String) request.getSession().getAttribute(PARAM_NAME_RESULT_PREFIX + "." 
        + responseType + "." + test);
    if ("YES".equals(result)) {
      html.append("<p style=\"background-color:green;\">" + test + " result is stored to be SUCCESS</p>");
    } else if ("NO".equals(result)) {
      html.append("<p style=\"background-color:red;\">" + test + " result is stored to be FAILED</p>");        
    } else {
      html.append("<p style=\"background-color:yellow;\">" + test + " result is not stored</p>");
      
    }
    String rpId = (String) request.getServletContext().getAttribute(ServletConfiguration.ATTR_NAME_RP_ID);
    html.append("<p><a href=\"https://rp.certification.openid.net:8080/log/" + rpId + "." + responseType.replace(" ", "-") + "/" + test + ".txt" +"\">OP-side log</a></p>");
    html.append("<p>Store result as "
      + "<a style=\"color:green;\" href=\"" + request.getContextPath() 
      + ServletConfiguration.HOME_SERVLET_MAPPING + "?" + PARAM_NAME_RESPONSE_TYPE + "="
      + responseType + "&" + PARAM_NAME_CONFIG + "=" + test + "&store=YES\">SUCCESS</a> or "
      + "<a style=\"color:red;\" href=\"" + request.getContextPath() 
      + ServletConfiguration.HOME_SERVLET_MAPPING + "?" + PARAM_NAME_RESPONSE_TYPE + "=" 
      + responseType + "&" + PARAM_NAME_CONFIG + "=" + test + "&store=NO\">FAILED</a></p>");
    writeHtmlBodyOutput(response, html.toString());
  }
}
