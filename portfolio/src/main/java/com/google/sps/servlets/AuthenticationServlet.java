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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import java.util.*;

/** Servlet for User Authentication. **/
@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        UserService userService = UserServiceFactory.getUserService();

        if (userService.isUserLoggedIn()) {
            // String email = userService.getCurrentUser().getEmail();
            String logoutUrl = userService.createLogoutURL("/");
            List<String> status = new ArrayList<>();
            status.add("true");
            status.add(logoutUrl);
            response.getWriter().println(convertToJson(status));

        } else {
            String loginUrl = userService.createLoginURL("/");
            List<String> status = new ArrayList<>();
            status.add("false");
            status.add(loginUrl);
            response.getWriter().println(convertToJson(status));
        }
    }
    
    // Convert List to JSON using Gson library.
    private String convertToJson(List<String> response) {
        Gson gson = new Gson();
        String json = gson.toJson(response);
        return json;
    }
}
