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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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
  
 
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    StringBuilder html = new StringBuilder();

    String config = request.getParameter(PARAM_NAME_CONFIG);
    String responseType = request.getParameter(PARAM_NAME_RESPONSE_TYPE);
    String store = request.getParameter("store");

    if (store != null && config != null && responseType != null) {
      request.getSession().setAttribute(PARAM_NAME_RESULT_PREFIX + "." + responseType + "." + config, store);
      config = null;
    }
    
    if (config == null || responseType == null) {
      html.append(listConfigurations(request, response, ""));
    } else {
      config = URLDecoder.decode(config, "UTF-8");
      if (!rpHandlers.containsKey(responseType)) {
        html.append(listConfigurations(request, response, "<p><b>Response type " + responseType + "</b> not found!</p>"));        
      } else if (!rpHandlers.get(responseType).containsKey(config)) {
        html.append(listConfigurations(request, response, "<p><b>" + config + "</b> not found!</p>"));
      } else {
        request.getSession().setAttribute(PARAM_NAME_RESPONSE_TYPE, responseType);
        RPHandler rpHandler = rpHandlers.get(responseType).get(config);
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
  
  protected String listConfigurations(HttpServletRequest request, HttpServletResponse response, String header) throws ServletException {
    StringBuilder html = new StringBuilder(header);
    html.append("<h1>Available configurations</h1>");

    String responseType = request.getParameter(PARAM_NAME_RESPONSE_TYPE);
    html.append("<div id=\"quick-book\">\n" + 
        "    <form name=\"frmRadio\" id=\"radio-buttons\" action=\"\">\n" + 
        makeRadioButton("code", responseType) + 
        makeRadioButton("code id_token", responseType) +
        makeRadioButton("code id_token token", responseType) +
        makeRadioButton("code token", responseType) +
        makeRadioButton("id_token", responseType) +
        makeRadioButton("id_token token", responseType) +
        "    </form>\n");
    html.append("    <div id =\"button\">"); 
    if (responseType != null) {
      html.append(makeTestList(request, responseType));
    }
    html.append("    </div>\n" + 
        "</div>\n" + 
        "<script>\n" + 
        "    function change(radio) { \n" + 
        "        if (radio.checked && radio.id === \"code\") {\n" + 
        "            document.getElementById(\"button\").innerHTML = \"" + makeTestList(request, "code") + "\"\n" + 
        "        } else if (radio.checked && radio.id === \"code id_token\") {\n" +
        "            document.getElementById(\"button\").innerHTML = \"" + makeTestList(request, "code id_token") + "\"\n" + 
        "        } else if (radio.checked && radio.id === \"code id_token token\") {\n" +
        "            document.getElementById(\"button\").innerHTML = \"" + makeTestList(request, "code id_token token") + "\"\n" + 
        "        } else if (radio.checked && radio.id === \"code token\") {\n" +
        "            document.getElementById(\"button\").innerHTML = \"" + makeTestList(request, "code token") + "\"\n" + 
        "        } else if (radio.checked && radio.id === \"id_token\") {\n" +
        "            document.getElementById(\"button\").innerHTML = \"" + makeTestList(request, "id_token") + "\"\n" + 
        "        } else if (radio.checked && radio.id === \"id_token token\") {\n" +
        "            document.getElementById(\"button\").innerHTML = \"" + makeTestList(request, "id_token token") + "\"\n" + 
        "        } else {\n" + 
        "            document.getElementById(\"button\").innerHTML = \"\"\n" + 
        "        }\n" + 
        "    }\n" + 
        "</script>");
    html.append("</form>");
    return html.toString();
  }

  protected String makeTestList(HttpServletRequest request, String responseType) throws ServletException {
    StringBuilder html = new StringBuilder();
    
    for (String configuration : rpHandlers.get(responseType).keySet()) {
      String result = (String) request.getSession().getAttribute(PARAM_NAME_RESULT_PREFIX + "." + responseType + "." + configuration);
      if ("YES".equals(result)) {
        html.append("<p style='background-color:chartreuse;'>");
      } else if ("NO".equals(result)) {
        html.append("<p style='background-color:tomato;'>");        
      } else {
        html.append("<p style='background-color:yellow;'>");
      }
      
      try {
        html.append("<a href='?" + PARAM_NAME_CONFIG + "=" + URLEncoder.encode(configuration, "UTF-8") + "&" + PARAM_NAME_RESPONSE_TYPE + "=" + responseType + "'>");
        if (TestCases.TEST_DEFINITIONS.get(responseType).get(Boolean.FALSE).contains(configuration)) {
          html.append("(OPTIONAL) ");
        }
        html.append(configuration + "</a></p>");
      } catch (UnsupportedEncodingException e) {
        throw new ServletException(e);
      }
    }
    return html.toString();
  }
  
  protected String makeRadioButton(String value, String responseType) {
    String checked = value.equals(responseType) ? " checked=\"checked\" " : " ";
    return "<input type=\"radio\" name=\"" + PARAM_NAME_RESPONSE_TYPE + "\" value=\"" 
        + value + "\"" + checked + "id=\"" 
            + value +"\" onclick=\"change(this)\">" + value + "<br/></input>";
  }

}
