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
import java.util.HashSet;
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

import com.auth0.jwt.exceptions.oicmsg_exceptions.ImportException;
import com.auth0.jwt.exceptions.oicmsg_exceptions.JWKException;
import com.auth0.jwt.exceptions.oicmsg_exceptions.UnknownKeyType;
import com.auth0.jwt.exceptions.oicmsg_exceptions.ValueError;
import com.auth0.msg.KeyBundle;
import com.google.common.base.Strings;

public class ServletConfiguration implements ServletContainerInitializer {  
  
  public static final String PROPERTY_NAME_RP_ID = "rpId";
  
  public static final String PROPERTY_NAME_HTTPS_PORT = "httpsPort";
  
  public static final String PROPERTY_NAME_HOST = "hostName";
  
  public static final String PROPERTY_NAME_PUBLIC_KEY = "publicKey";
  
  public static final String PROPERTY_NAME_PRIVATE_KEY = "privateKey";
  
  public static final String DEFAULT_RP_ID = "mockJavaRp";
  
  public static final String DEFAULT_PRIVATE_KEY = "src/main/resources/jwks.json.private";

  public static final String DEFAULT_PUBLIC_KEY = "src/main/resources/jwks.json.public";

  public static final String JSON_CONFIGURATION_FOLDER = "src/main/resources/json";
  
  public static final String HOME_SERVLET_MAPPING = "/Home";
  
  public static final String ATTR_NAME_RP_HANDLERS = "oidcRpHandlers";
  
  public static final String ATTR_NAME_RP_ID = "rpId";

  @Override
  public void onStartup(Set<Class<?>> c, ServletContext servletContext) throws ServletException {
    
    String rpId = getSystemProperty(PROPERTY_NAME_RP_ID) == null ? DEFAULT_RP_ID : getSystemProperty(PROPERTY_NAME_RP_ID);
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

    String publicKeyFile = getSystemProperty(PROPERTY_NAME_PUBLIC_KEY) == null ? DEFAULT_PUBLIC_KEY : getSystemProperty(PROPERTY_NAME_PUBLIC_KEY);
    String defaultPublicKey = getFileContents(publicKeyFile);
    
    String privateKeyFile = getSystemProperty(PROPERTY_NAME_PRIVATE_KEY) == null ? DEFAULT_PRIVATE_KEY : getSystemProperty(PROPERTY_NAME_PRIVATE_KEY);
    KeyBundle defaultKeyBundle = initializeKeyBundle(privateKeyFile);
    
    Set<String> registeredJwkUris = new HashSet<>();
    
    for (String responseType : rpHandlers.keySet()) {
      for (String config : rpHandlers.get(responseType).keySet()) {
        RPHandler rpHandler = rpHandlers.get(responseType).get(config);
        List<String> uris = rpHandler.getOpConfigurations().get(0).getServiceContext().getRedirectUris();
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
        OpConfiguration opConfiguration = rpHandler.getOpConfigurations().get(0);
        String jwksUri = opConfiguration.getServiceContext().getJwksUri();
        if (!Strings.isNullOrEmpty(jwksUri)) {
          //add keybundle only if jwks_uri is defined
          if (opConfiguration.getConfigurationClaims().containsKey("PRIVATE_JWKS_PATH")) {
            KeyBundle keyBundle = initializeKeyBundle((String) opConfiguration.getConfigurationClaims().get("PRIVATE_JWKS_PATH"));
            opConfiguration.getServiceContext().getKeyJar().addKeyBundle("", keyBundle);
          } else {
            opConfiguration.getServiceContext().getKeyJar().addKeyBundle("", defaultKeyBundle);            
          }
          String uri = jwksUri.replace(baseUrl, "");
          if (!registeredJwkUris.contains(uri)) {
            System.out.println("Registering jwks_uri " + uri);
            String publicKey;
            if (opConfiguration.getConfigurationClaims().containsKey("PUBLIC_JWKS_PATH")) {
              publicKey = getFileContents((String) opConfiguration.getConfigurationClaims().get("PUBLIC_JWKS_PATH"));
            } else {
              publicKey = defaultPublicKey;
            }
            ServletRegistration.Dynamic jwksRegistration =
                servletContext.addServlet(uri, new EchoServlet(publicKey));
            jwksRegistration.addMapping(uri);
            registeredJwkUris.add(uri);
          }
        }
      }
      ServletRegistration.Dynamic fileRegistration =
          servletContext.addServlet("file" + responseType, new FileServlet());
      fileRegistration.addMapping("/" + responseType.replace(" ", "-") + "/requests/*");
    }
    
    File file = new File("requests");
    if (!file.exists()) {
      if (!file.mkdir()) {
        throw new ServletException("Could not create a directory for requests!");
      }
    } else {
      if (!file.isDirectory()) {
        throw new ServletException("File 'requests' is not a directory!");
      }
    }
        
    servletContext.setAttribute(ATTR_NAME_RP_HANDLERS, rpHandlers);
    
    ServletRegistration.Dynamic homeRegistration =
        servletContext.addServlet("home", new StartServlet());
    homeRegistration.addMapping(HOME_SERVLET_MAPPING);

  }
  
  protected KeyBundle initializeKeyBundle(String filename) throws ServletException {
    try {
      return KeyBundle.keyBundleFromLocalFile(filename, "jwks", Arrays.asList("enc","sig"));
    } catch (ImportException | UnknownKeyType | IOException | JWKException | ValueError e) {
      throw new ServletException("Could not load key bundle from " + filename);
    }

  }
  
  protected String getFileContents(String filename) throws ServletException {
    try {
      return new String(Files.readAllBytes(Paths.get(filename)));
    } catch (IOException e) {
      throw new ServletException("Could not read the contents of file in " + filename);
    }
    
  }
  
  protected String getSystemProperty(String key) {
    String property = System.getProperty(key);
    if (property == null || "".equals(property)) {
      return null;
    }
    return property;
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
