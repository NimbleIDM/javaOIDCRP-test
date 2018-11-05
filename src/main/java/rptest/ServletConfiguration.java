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
import java.util.HashMap;
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

  @Override
  public void onStartup(Set<Class<?>> c, ServletContext servletContext) throws ServletException {

    Object rpIdProperty = System.getProperty(PROPERTY_NAME_RP_ID);
    String rpId = rpIdProperty == null ? DEFAULT_RP_ID : (String) rpIdProperty;
    System.out.println("Using '" +rpId + "' as the RP identifier for the  tool");

    String baseUrl = "https://" + System.getProperty("grettyHost") + ":" 
        + System.getProperty("httpsPort") + "/javaOIDCRP-test";
    Map<String, RPHandler> rpHandlers = new HashMap<>();
    
    File folder = new File(JSON_CONFIGURATION_FOLDER);
    Map<String, OpConfiguration> opConfigs = new HashMap<>();
    for (final File fileEntry : folder.listFiles()) {
      if (!fileEntry.isDirectory()) {
        String name = fileEntry.getName();
        if (name.contains(".json")) {
          String path = ServletConfiguration.JSON_CONFIGURATION_FOLDER + "/" + name;
          String testName = name.substring(0, name.indexOf(".json"));
          try {
            String contents = new String(Files.readAllBytes(Paths.get(path)), 
                Charset.forName("UTF-8"));
            contents = fillJsonTemplate(contents);
            contents = "{ \"" + testName + "\" : " + contents + " }";
            Map<String, OpConfiguration> newConfigs = 
                OpConfiguration.parseFromJson(contents.getBytes(), baseUrl);
            System.out.println("Found test: " + testName);
            opConfigs.putAll(newConfigs);
          } catch (IOException | DeserializationException e) {
            System.err.println("Could not parse test " + testName + ", ignoring");
          }
          
        }
      }
    }
    for (String config : opConfigs.keySet()) {
      OpConfiguration opConfig = opConfigs.get(config);
      RPHandler rpHandler = new RPHandler(opConfig);
      rpHandlers.put(config, rpHandler);
      for (String uri : opConfig.getServiceContext().getRedirectUris()) {
        System.out.println("URI: " + uri);
        ServletRegistration.Dynamic callbackRegistration =
            servletContext.addServlet(uri, new CallbackServlet(config));
        callbackRegistration.addMapping(uri.replace(baseUrl, ""));
      }
    }
    servletContext.setAttribute(ATTR_NAME_RP_HANDLERS, rpHandlers);
    
    ServletRegistration.Dynamic homeRegistration =
        servletContext.addServlet("home", new StartServlet());
    homeRegistration.addMapping(HOME_SERVLET_MAPPING);    
  }
  

  protected String fillJsonTemplate(String template) {
    String rpBaseUrl = "https://" + System.getProperty("grettyHost") + ":" 
        + System.getProperty("httpsPort") + "/javaOIDCRP-test";
    template = template.replace("<RP>", rpBaseUrl);
    String opBaseUrl = "https://rp.certification.openid.net:8080";
    template = template.replace("<OP>", opBaseUrl);
    template = template.replace("<RPID>", System.getProperty("rpId"));
    template = template.replace("<RESPONSE_TYPE>", "code");
    return template;
  }
}
