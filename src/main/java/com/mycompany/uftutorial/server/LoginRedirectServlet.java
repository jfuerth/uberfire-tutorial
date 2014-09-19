package com.mycompany.uftutorial.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginRedirectServlet extends HttpServlet {

  private final String hostPage = "tutorial.html";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    System.out.println(getClass().getSimpleName() + " is redirecting " + req.getUserPrincipal() + " to " + hostPage);
//    if ( req.getUserPrincipal() == null ) {
//      // no user; need the browser to stay at our location so we can handle post-auth
//      req.getRequestDispatcher(hostPage).forward(req, resp);
//    } else {
//      // user logged in. do a full redirect to host page.
//      resp.sendRedirect(hostPage);
//    }
    StringBuilder redirectTarget = new StringBuilder(hostPage);
    String extraParams = extractParameters(req);
    if (extraParams.length() > 0) {
      redirectTarget.append("?").append(extraParams);
    }

    resp.sendRedirect(redirectTarget.toString());
  }

  /**
   * Extracts all parameters except the username and password into a URL-encoded query string. The string does not begin
   * or end with a "&amp;".
   */
  private static String extractParameters(HttpServletRequest fromRequest) {
    try {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String[]> param : (Set<Map.Entry<String,String[]>>) fromRequest.getParameterMap().entrySet()) {
        String paramName = URLEncoder.encode(param.getKey(), "UTF-8");
        if (paramName.equals("j_username") || paramName.equals("j_password")) {
          continue;
        }
        for (String value : param.getValue()) {
          if (sb.length() != 0) {
            sb.append("&");
          }
          sb.append(paramName).append("=").append(URLEncoder.encode(value, "UTF-8"));
        }
      }
      return sb.toString();
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 not supported on this JVM?");
    }
  }

}
