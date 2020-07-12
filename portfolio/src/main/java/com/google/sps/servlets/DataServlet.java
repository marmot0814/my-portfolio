// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comment")
public class DataServlet extends HttpServlet {

  private ArrayList<String> usernames;
  private ArrayList<String> contents;
  private ArrayList<String> timestamps;

  public void init() {
    usernames = new ArrayList<String>();
    contents = new ArrayList<String>();
    timestamps = new ArrayList<String>();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = convertToJson(usernames, contents, timestamps);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String username = request.getParameter("username");
    String content = request.getParameter("content");

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String timestamp = formatter.format(date);

    usernames.add(username);
    contents.add(content);
    timestamps.add(timestamp);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private String convertToJson(ArrayList<String> usernames, ArrayList<String> contents, ArrayList<String> timestamps) {
    String json = "[";
    int tot = usernames.size();
    for (int i = 0 ; i < tot ; i++) {
      json += "{";
      json += "\"username\": \"" + usernames.get(i) + "\",";
      json += "\"content\": \"" + contents.get(i) + "\",";
      json += "\"timestamp\": \"" + timestamps.get(i) + "\"";
      json += "}";
      if (i + 1 != tot)
        json += ",";
    }
    json += "]";
    return json;
  }
}
