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
  
}
