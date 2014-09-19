package com.mycompany.uftutorial.server;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Credentials.Status;


public class ServletRequestAuthScheme implements HTTPAuthenticationScheme {

  public static final String PROBE_ROLES_INIT_PARAM = "probe-for-roles";

  private String[] probeRoles;

  @Override
  public void initialize(FilterConfig filterConfig) {
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
  public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
    System.out.println(getClass().getSimpleName() + ".extractCredential()" + request.getRequestURI());
    Principal authenticatedUser = request.getUserPrincipal();
    if ( authenticatedUser != null ) {
      System.out.println("Found authenticated servlet user " + authenticatedUser.getName());

      List<Role> userRoles = new ArrayList<Role>();
      for (String checkRole : probeRoles) {
        if (request.isUserInRole(checkRole)) {
          userRoles.add(new RoleImpl(checkRole));
        }
      }

      List<Group> userGroups = new ArrayList<Group>();
      // TODO extract groups (special code for WAS is in UberFire 0.4)

      User user = new UserImpl(authenticatedUser.getName(), userRoles, userGroups);

      creds.setCredential(user);
      creds.setStatus(Status.VALID);
      creds.setUserId(authenticatedUser.getName());

      request.getSession().setAttribute(User.class.getName(), user);
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
    System.out.println(getClass().getSimpleName() + ".postAuthentication() " + request.getRequestURI());
    return true;
  }

  @Override
  public boolean isProtected(HttpServletRequest request) {
    System.out.println(getClass().getSimpleName() + ".isProtected() " + request.getRequestURI() );
    return true;
  }

}
