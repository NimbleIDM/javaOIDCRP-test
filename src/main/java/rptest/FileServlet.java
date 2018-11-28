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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileServlet extends AbstractServlet {
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    String rawUri = request.getRequestURI();
    String filename = rawUri.substring(rawUri.indexOf("/requests/") + 1);

    File file = new File(filename);
    if (file.exists() && file.canRead()) {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      String line;
      while((line = bufferedReader.readLine()) != null) {
        response.getWriter().println(line);
      }   
      bufferedReader.close();
      System.out.println("Successfully published file " + filename);
    } else {
      System.err.println("Cannot read a file " + filename);
    }
    response.getWriter().close();
  }

}
