package com.mycompany.uftutorial.server;

import java.io.IOException;
import java.security.Principal;

import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Credentials.Status;


public class ServletRequestAuthScheme implements HTTPAuthenticationScheme {

  public static final String PROBE_ROLES_INIT_PARAM = "probe-for-roles";

  @Inject
  private AuthenticationService authenticationService;

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
    System.out.println(getClass().getSimpleName() + ".postAuthentication() " + request.getRequestURI());
    return true;
  }

  @Override
  public boolean isProtected(HttpServletRequest request) {
    System.out.println(getClass().getSimpleName() + ".isProtected() " + request.getRequestURI() );
    return true;
  }

}
