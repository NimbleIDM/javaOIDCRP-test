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
import org.oidc.common.UnsupportedSerializationTypeException;
import org.oidc.common.ValueException;
import org.oidc.msg.InvalidClaimException;
import org.oidc.msg.SerializationException;
import org.oidc.rp.BeginResponse;
import org.oidc.rp.RPHandler;
import org.oidc.service.base.RequestArgumentProcessingException;

public class StartServlet extends AbstractServlet {
  
  public static final String PARAM_NAME_CONFIG = "jsonConfig";
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    StringBuilder html = new StringBuilder();

    String config = request.getParameter(PARAM_NAME_CONFIG);
    
    if (config == null) {
      html.append(listConfigurations(response, ""));
    } else {
      if (!rpHandlers.containsKey(config)) {
        html.append(listConfigurations(response, "<p><b>" + config + "</b> not found!</p>"));
      } else {
        RPHandler rpHandler = rpHandlers.get(config);
        String issuer = rpHandler.getOpConfiguration().getServiceContext().getIssuer();
        try {
          BeginResponse beginResponse = rpHandler.begin(issuer, null);
          if (beginResponse.getRedirectUri() != null) {
            response.sendRedirect(beginResponse.getRedirectUri());
          }
        } catch (MissingRequiredAttributeException | UnsupportedSerializationTypeException
            | RequestArgumentProcessingException | SerializationException | ValueException 
            | InvalidClaimException e) {
          e.printStackTrace();
          writeHtmlBodyOutput(response, "Error: " + e.getMessage());
        }
      }
    }
    
    writeHtmlBodyOutput(response, html.toString());
  }
  
  protected String listConfigurations(HttpServletResponse response, String header) {
    StringBuilder html = new StringBuilder(header);
    html.append("<h1>Available configurations</h1>");
    for (String configuration : rpHandlers.keySet()) {
      html.append("<p><a href=\"?" + PARAM_NAME_CONFIG + "=" + configuration + "\">" 
          + configuration + "</a></p>");      
    }
    return html.toString();
  }

}
