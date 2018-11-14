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
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oidc.rp.RPHandler;

@SuppressWarnings("serial")
public abstract class AbstractServlet extends HttpServlet {

  public static final String PARAM_NAME_CONFIG = "jsonConfig";
  
  public static final String PARAM_NAME_RESPONSE_TYPE = "responseType";
  
  public static final String PARAM_NAME_RESULT_PREFIX = "oidcTestResult";
  
  public  Map<String, Map<String, RPHandler>> rpHandlers;
  
  @SuppressWarnings("unchecked")
  @Override
  public void init() throws ServletException {
    rpHandlers = (Map<String, Map<String, RPHandler>>) getServletConfig().getServletContext().
        getAttribute(ServletConfiguration.ATTR_NAME_RP_HANDLERS);
    if (rpHandlers == null || rpHandlers.isEmpty()) {
      throw new ServletException("Could not find any configurations");
    }
  }

  protected void writeHtmlBodyOutput(HttpServletResponse response, String output) 
      throws IOException {
    PrintWriter writer = response.getWriter();
    writer.println("<html><body>");
    writer.println(output);
    writer.println("</body></html>");
    writer.close();    
  }
  
  protected String getResultAndStoreOptions(HttpServletRequest request, String responseType, String test) {
    StringBuilder html = new StringBuilder();
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
    return html.toString();
  }
  
}
