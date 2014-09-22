package com.mycompany.uftutorial.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class SecurityIntegrationFilter implements Filter {

  public static final String PROBE_ROLES_INIT_PARAM = "probe-for-roles";

  private static final ThreadLocal<HttpServletRequest> requests = new ThreadLocal<HttpServletRequest>();

  private static Collection<String> rolesToProbe;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String commaSeparatedRoles = filterConfig.getInitParameter(PROBE_ROLES_INIT_PARAM);
    if (commaSeparatedRoles == null) {
      throw new IllegalStateException(
              getClass().getSimpleName() + " requires that you set a comma-separated list of role names your "
                      + "application cares about in the init parameter \"" + PROBE_ROLES_INIT_PARAM + "\".");
    }
    rolesToProbe = Collections.unmodifiableList(Arrays.asList(commaSeparatedRoles.split(",")));

  }

  @Override
  public void destroy() {
    // no op
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    requests.set((HttpServletRequest) request);
    try {
      chain.doFilter(request, response);
    }
    finally {
      requests.remove();
    }
  }

  /**
   * Returns the current servlet request that this thread is handling, or null if this thread is not currently handling
   * a servlet request.
   */
  public static HttpServletRequest getRequest() {
    return requests.get();
  }

  public static Collection<String> getRolesToProbe() {
    if (rolesToProbe == null) {
      throw new IllegalStateException("Filter is not initialized yet");
    }
    return rolesToProbe;
  }
}
