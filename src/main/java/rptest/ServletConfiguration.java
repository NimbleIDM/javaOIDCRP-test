/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package rptest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.oidc.msg.DeserializationException;
import org.oidc.rp.RPHandler;
import org.oidc.rp.config.OpConfiguration;

public class ServletConfiguration implements ServletContainerInitializer {  
  
  public static final String PROPERTY_NAME_RP_ID = "rpId";
  
  public static final String PROPERTY_NAME_HTTPS_PORT = "httpsPort";
  
  public static final String PROPERTY_NAME_HOST = "hostName";
  
  public static final String DEFAULT_RP_ID = "mockJavaRp";

  public static final String JSON_CONFIGURATION_FOLDER = "src/main/resources/json";
  
  public static final String HOME_SERVLET_MAPPING = "/Home";
  
  public static final String ATTR_NAME_RP_HANDLERS = "oidcRpHandlers";
  
  public static final String ATTR_NAME_RP_ID = "rpId";

  @Override
  public void onStartup(Set<Class<?>> c, ServletContext servletContext) throws ServletException {
    
    Object rpIdProperty = System.getProperty(PROPERTY_NAME_RP_ID);
    String rpId = rpIdProperty == null ? DEFAULT_RP_ID : (String) rpIdProperty;
    System.out.println("Using '" +rpId + "' as the RP identifier for the  tool");
    servletContext.setAttribute(ATTR_NAME_RP_ID, rpId);

    String baseUrl = "https://" + System.getProperty("grettyHost") + ":" 
        + System.getProperty("httpsPort") + "/javaOIDCRP-test";
    Map<String, Map<String, RPHandler>> rpHandlers = new HashMap<>();
    
    File folder = new File(JSON_CONFIGURATION_FOLDER);
    Map<String, Map<String, OpConfiguration>> opConfigs = new HashMap<>();
    for (String responseType : Arrays.asList("code", "code id_token", "code id_token token", 
        "code token", "id_token", "id_token token", "dynamic", "configuration")) {
      opConfigs.put(responseType, new HashMap<String, OpConfiguration>());
      for (final File fileEntry : folder.listFiles()) {
        if (!fileEntry.isDirectory()) {
          String name = fileEntry.getName();
          if (name.contains(".json")) {
            String path = ServletConfiguration.JSON_CONFIGURATION_FOLDER + "/" + name;
            String testName = name.substring(0, name.indexOf(".json"));
            try {
              String contents = new String(Files.readAllBytes(Paths.get(path)), 
                  Charset.forName("UTF-8"));
              contents = fillJsonTemplate(contents, responseType);
              contents = "{ \"" + testName + "\" : " + contents + " }";
              Map<String, OpConfiguration> newConfigs = 
                OpConfiguration.parseFromJson(contents.getBytes(), baseUrl + "/" 
                    + responseType.replace(" ", "-"));
              System.out.println("Found test: " + testName);
              opConfigs.get(responseType).putAll(newConfigs);
            } catch (IOException | DeserializationException e) {
              System.err.println("Could not parse test " + testName + ", ignoring");
            }
          }
        }
      }
    }
    
    for (String responseType : TestCases.TEST_DEFINITIONS.keySet()) {
      rpHandlers.put(responseType, fillMap(TestCases.TEST_DEFINITIONS.get(responseType), opConfigs.get(responseType), servletContext, baseUrl));
    }
    
    for (String responseType : rpHandlers.keySet()) {
      for (String config : rpHandlers.get(responseType).keySet()) {
        List<String> uris = rpHandlers.get(responseType).get(config).getOpConfiguration().getServiceContext().getRedirectUris();
        if (uris != null) {
          for (String uri : uris) {
            System.out.println("URI: " + uri + ", to be replaced to " + uri.replace(baseUrl, "/" + responseType.replace(" ", "-")));
            //uri = uri.replace(baseUrl, "/" + responseType.replace(" ", "-"));
            uri = uri.replace(baseUrl, "");
            ServletRegistration.Dynamic callbackRegistration =
                servletContext.addServlet(uri, new CallbackServlet(config, responseType));
            callbackRegistration.addMapping(uri);
          }
        }
      }
    }
    servletContext.setAttribute(ATTR_NAME_RP_HANDLERS, rpHandlers);
    
    ServletRegistration.Dynamic homeRegistration =
        servletContext.addServlet("home", new StartServlet());
    homeRegistration.addMapping(HOME_SERVLET_MAPPING);    
  }
  
  protected Map<String, RPHandler> fillMap(Map<Boolean, List<String>> testIds, 
      Map<String, OpConfiguration> opConfigs, ServletContext servletContext, String baseUrl) {
    Map<String, RPHandler> rpHandlers = new HashMap<String, RPHandler>();
    for (String config : opConfigs.keySet()) {
      if (testIds.get(Boolean.TRUE).contains(config) || testIds.get(Boolean.FALSE).contains(config)) {
        OpConfiguration opConfig = opConfigs.get(config);
        RPHandler rpHandler = new RPHandler(opConfig);
        rpHandlers.put(config, rpHandler);
      }
    }
    return rpHandlers;
  }
  

  protected String fillJsonTemplate(String template, String responseType) {
    String rpBaseUrl = "https://" + System.getProperty("grettyHost") + ":" 
        + System.getProperty("httpsPort") + "/javaOIDCRP-test";
    template = template.replace("<RP>", rpBaseUrl);
    String opHost = "rp.certification.openid.net:8080";
    template = template.replace("<OP_HOST>", opHost);
    String opBaseUrl = "https://" + opHost;
    template = template.replace("<OP>", opBaseUrl);
    template = template.replace("<RPID>", System.getProperty("rpId") + "." 
        + responseType.replace(" ", "-"));
    if ("configuration".equals(responseType)) {
      template = template.replace("<RESPONSE_TYPE>", "code");      
    } else if ("dynamic".equals(responseType)) {
      template = template.replace("<RESPONSE_TYPE>", "code");
    } else {
      template = template.replace("<RESPONSE_TYPE>", responseType);
    }
    return template;
  }
}
