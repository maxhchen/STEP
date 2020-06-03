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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import java.util.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    List<String> comments = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = convertToJson(comments);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  // Convert List to JSON using Gson library.
  private String convertToJson(List<String> msgs) {
    Gson gson = new Gson();
    String json = gson.toJson(msgs);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = getComment(request);
    if (newComment != null) {
        comments.add(newComment);

        Entity commentEntity = new Entity("comment");
        commentEntity.setProperty("text", newComment);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
    }

    response.sendRedirect("/index.html");
  }
 
 // Extracts comment text from request and returns it.
  private String getComment(HttpServletRequest request) {
    String newComment = request.getParameter("comment");
    // Add handling for newComment = null?
    return newComment;
  }
}
