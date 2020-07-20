// Copyright 2020 Google LLC
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

import com.google.auth.oauth2.GoogleCredentials;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.Query.Direction;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

import com.google.gson.Gson;

import com.google.sps.data.Comment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comment")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Firestore db = 
      FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("zyang-sps-summer20")
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build().getService();

    try {
      ApiFuture<QuerySnapshot> querySnapshot = 
        db.collection("comments")
          .orderBy("timestamp")
          .get();

      ArrayList<Comment> comments = new ArrayList<>();
      for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
        Comment comment = new Comment(
          document.getId(),
          document.getString("username"),
          document.getString("content"),
          timestampToString(document.getLong("timestamp"))
        );
        comments.add(comment);
      }

      response.setContentType("application/json;");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().println(new Gson().toJson(comments));

    } catch (Exception e) {
      System.out.println (e.getMessage());
    }
    
  }

  private String timestampToString(long timestamp) {
    Timestamp ts = new Timestamp(timestamp);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return formatter.format(ts);
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      // Redirect back to the HTML page.
      response.sendRedirect("/index.html");
      return ;
    }

    Map<String, Object> data = new HashMap<>();
    data.put("username", userService.getCurrentUser().getEmail());
    data.put("content", request.getParameter("content"));
    data.put("timestamp", System.currentTimeMillis());

    Firestore db = 
      FirestoreOptions.getDefaultInstance().toBuilder()
        .setProjectId("zyang-sps-summer20")
        .setCredentials(GoogleCredentials.getApplicationDefault())
        .build().getService();

    DocumentReference docRef = db.collection("comments").document();
    ApiFuture<WriteResult> result = docRef.set(data);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
