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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.classes.Comment;
import com.google.gson.Gson;
import java.util.*;
import java.text.*;

/** Servlet that returns user comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  PreparedQuery loadedComments;
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    loadedComments = datastore.prepare(query);

    int commentLimit = getNumberOfCommentsToDisplay(request);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : loadedComments.asList(FetchOptions.Builder.withLimit(commentLimit))) {
      long id = entity.getKey().getId();
      String text = (String) entity.getProperty("text");
      String timestamp = (String) entity.getProperty("timestamp");
      String email = (String) entity.getProperty("email");

      Comment comment = new Comment(id, text, timestamp, email);
      comments.add(comment);
    }

    String json = convertToJson(comments);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newCommentText = getComment(request);
    UserService userService = UserServiceFactory.getUserService();

    if (newCommentText != null) {
        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("text", newCommentText);
        commentEntity.setProperty("timestamp", getTimestamp());
        if (userService.isUserLoggedIn()) {
            commentEntity.setProperty("email", userService.getCurrentUser().getEmail());
        } else {
            commentEntity.setProperty("email", "guest");
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);
    }

    response.sendRedirect("/index.html");
  }

  // Convert List to JSON using Gson library.
  private String convertToJson(List<Comment> msgs) {
    Gson gson = new Gson();
    String json = gson.toJson(msgs);
    return json;
  }
 
 // Extracts comment text from request and returns it.
  private String getComment(HttpServletRequest request) {
    String newComment = request.getParameter("comment");

    // Prevent accidental/blank submissions.
    if (newComment.equals("")) {
        return null;
    }
    return newComment;
  }

// Extracts user-defined limit for number of comments to display.
  private int getNumberOfCommentsToDisplay(HttpServletRequest request) {
    String requestValue = request.getParameter("commentLimit");
    int numComments;

    // Null or empty check.
    if (requestValue == null || requestValue.isEmpty()) {
      numComments = loadedComments.countEntities();
      return numComments;
    }
    
    // Parse `commentLimit` as a number, otherwise default to number of comments in Datastore.
    try {
        numComments = Integer.parseInt(requestValue);
    } catch (NumberFormatException e) {
        System.err.println("Cannot parse user-defined comment limit; default to maximum.");
        numComments = loadedComments.countEntities();
    }

    return numComments;
  }

    // Get formatted timestamp for current date and time.
    private String getTimestamp() {
        DateFormat timestamp = SimpleDateFormat.getDateTimeInstance();
        return timestamp.format(Calendar.getInstance().getTime());

    }
}
