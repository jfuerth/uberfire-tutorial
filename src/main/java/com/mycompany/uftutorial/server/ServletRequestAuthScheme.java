package com.mycompany.uftutorial.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Credentials.Status;


public class ServletRequestAuthScheme implements HTTPAuthenticationScheme {

  public static final String HOST_PAGE_INIT_PARAM = "host-page";
  public static final String PROBE_ROLES_INIT_PARAM = "probe-for-roles";

  @Inject
  private AuthenticationService authenticationService;

  /**
   * URI of the GWT host page, relative to the servlet container root (so it starts with '/' and includes the context
   * path).
   */
  private String hostPageUri;

  private String[] probeRoles;

  @Override
  public void initialize(FilterConfig filterConfig) {
    String contextRelativeHostPageUri = filterConfig.getInitParameter(HOST_PAGE_INIT_PARAM);
    if (contextRelativeHostPageUri == null) {
      throw new IllegalStateException(
              getClass().getSimpleName() + " requires that you set the filter init parameter \""
                      + HOST_PAGE_INIT_PARAM + "\" to the context-relative URI of the host page.");
    }
    hostPageUri = filterConfig.getServletContext().getContextPath() + contextRelativeHostPageUri;

    String commaSeparatedRoles = filterConfig.getInitParameter(PROBE_ROLES_INIT_PARAM);
    if (commaSeparatedRoles == null) {
      throw new IllegalStateException(
              getClass().getSimpleName() + " requires that you set a comma-separated list of role names your "
                      + "application cares about in the init parameter \"" + PROBE_ROLES_INIT_PARAM + "\".");
    }
    probeRoles = commaSeparatedRoles.split(",");

    // this ensures Errai Marshalling has been set up (for encoding the cookie)
    MappingContextSingleton.get();
  }

  @Override
  public void extractCredential( HttpServletRequest request, DefaultLoginCredentials creds ) {
    System.out.println(getClass().getSimpleName() + ".extractCredential()" + request.getRequestURI());
    Principal authenticatedUser = request.getUserPrincipal();
    if ( authenticatedUser != null ) {
      System.out.println("Found authenticated servlet user " + authenticatedUser.getName());
      creds.setCredential( authenticatedUser );
      creds.setStatus( Status.VALID );
      creds.setUserId( authenticatedUser.getName() );

      // TODO probe for group membership and save in credential
    } else {
      System.out.println("No user in request");
    }
  }

  @Override
  public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println(getClass().getSimpleName() + ".challengeClient()");
    // don't need to do anything; servlet container will show login page when necessary
  }

  @Override
  public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println(getClass().getSimpleName() + ".postAuthentication()");

    // if this request already has the correct user cookie, we're done
    String rawCookies = request.getHeader("Cookie");
    //errai-active-user={"^EncodedType":"org.jboss.errai.security.shared.api.identity.UserImpl","^ObjectID":"1","name":"bob","roles":{"^EncodedType":"java.util.Collections$UnmodifiableSet","^ObjectID":"2","^Value":[]},"groups":{"^EncodedType":"java.util.Collections$UnmodifiableSet","^ObjectID":"3","^Value":[]},"properties":{"^EncodedType":"java.util.Collections$UnmodifiableMap","^ObjectID":"4","^Value":{"org.jboss.errai.security.FIRST_NAME":null,"org.jboss.errai.security.EMAIL":null,"org.jboss.errai.security.LAST_NAME":null}}}; JSESSIONID=XQLixU2bkUaBOVeci3Fi1RCI.dhcp-10-15-16-114; JSESSIONID=1t9x87pjgz2ir
    // FIXME resorting to raw header because unquoted cookie value from firefox confuses the Java Cookie parser; we only get the first bit of the value via request.getCookies()
    if (rawCookies.matches(UserCookieEncoder.USER_COOKIE_NAME + "=.*\\Q\"name\":\"" + authenticationService.getUser().getIdentifier() + "\"\\E.*;.*" )) {
      return true;
    }

    // looks like we need to (re)set the user cookie and send a redirect to the host page
    Cookie erraiUserCacheCookie = new Cookie(
            UserCookieEncoder.USER_COOKIE_NAME,
            UserCookieEncoder.toCookieValue(authenticationService.getUser()));
    response.addCookie(erraiUserCacheCookie);

    StringBuilder redirectTarget = new StringBuilder(hostPageUri);
    String extraParams = extractParameters(request);
    if (extraParams.length() > 0) {
      redirectTarget.append("?").append(extraParams);
    }

    response.sendRedirect(redirectTarget.toString());
    return false;
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

  @Override
  public boolean isProtected(HttpServletRequest request) {
    System.out.println(getClass().getSimpleName() + ".isProtected() " + request.getRequestURI() );
    return true;
  }

}
