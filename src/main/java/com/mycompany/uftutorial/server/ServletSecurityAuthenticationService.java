package com.mycompany.uftutorial.server;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;

@Service
@ApplicationScoped
public class ServletSecurityAuthenticationService implements AuthenticationService {

  @Override
  public User login(String username, String password) {
    throw new UnsupportedOperationException("Logins must be handled by the servlet container (use login-config in web.xml).");
  }

  @Override
  public boolean isLoggedIn() {
    HttpServletRequest request = getRequestForThread();
    return request.getUserPrincipal() != null;
  }

  @Override
  public void logout() {
    HttpServletRequest request = getRequestForThread();
    request.getSession().invalidate();
  }

  @Override
  public User getUser() {
    HttpServletRequest request = getRequestForThread();

    // TODO probe for group membership and save in credential
    List<Role> userRoles = new ArrayList<Role>();
    for (String checkRole : SecurityIntegrationFilter.getRolesToProbe()) {
      if (request.isUserInRole(checkRole)) {
        userRoles.add(new RoleImpl(checkRole));
      }
    }

    List<Group> userGroups = new ArrayList<Group>();
    // TODO extract groups (special code for WAS is in UberFire 0.4)

    return new UserImpl(request.getUserPrincipal().getName(), userRoles, userGroups);
  }

  private HttpServletRequest getRequestForThread() {
    HttpServletRequest request = SecurityIntegrationFilter.getRequest();
    if (request == null) {
      throw new IllegalStateException("This service only works from threads that are handling HTTP servlet requests");
    }
    return request;
  }

}
