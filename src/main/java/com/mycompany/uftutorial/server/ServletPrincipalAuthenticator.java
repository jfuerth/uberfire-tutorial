package com.mycompany.uftutorial.server;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.auth.Subject;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

@ApplicationScoped
@PicketLink
public class ServletPrincipalAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject IdentityManager identityManager;
    @Inject RelationshipManager relationshipManager;

    @Override
    public void authenticate() {
      if ( !(credentials.getCredential() instanceof Principal) ) {
        setAccount( null );
        setStatus( AuthenticationStatus.FAILURE );
        return;
      }

      Principal servletPrincipal = (Principal) credentials.getCredential();

      // FIXME These are persistent operations in PicketLink. Copying JAAS users into the PL identity store is not a good idea.
      // We probably need to change this class to be an IdentityStore rather than an Authenticator.
      User persistedUser = queryForUser( credentials.getUserId() );

      if ( persistedUser == null ) {
        identityManager.add( new User( credentials.getUserId() ) );
        persistedUser = queryForUser( credentials.getUserId() );
      }

//      for ( String roleName : getJaasRoles( servletPrincipal.loginContext.getSubject(), settings ) ) {
//        Role persistedRole = queryForRole( roleName );
//        if ( persistedRole == null ) {
//          identityManager.add( new Role( roleName ) );
//          persistedRole = queryForRole( roleName );
//        }
//        if ( !BasicModel.hasRole( relationshipManager, persistedUser, persistedRole ) ) {
//          relationshipManager.add( new Grant( persistedUser, persistedRole ) );
//        }
//      }
      setAccount( persistedUser );
      setStatus( AuthenticationStatus.SUCCESS );
    }

    private User queryForUser( String loginName ) {
        List<User> resultList = identityManager
                .createIdentityQuery( User.class )
                .setParameter( User.LOGIN_NAME, loginName )
                .getResultList();
        if ( resultList.isEmpty() ) {
            return null;
        }
        return resultList.get(0);
    }

    private Role queryForRole( String roleName ) {
        List<Role> resultList = identityManager
                .createIdentityQuery( Role.class )
                .setParameter( Role.NAME, roleName )
                .getResultList();
        if ( resultList.isEmpty() ) {
            return null;
        }
        return resultList.get(0);
    }

    PicketLinkJaasSettings resolveSettings() {
        // TODO
        return new PicketLinkJaasSettings();
    }

    /**
     * Extracts all roles from the given subject, returning them as a list of PicketLink Role objects.
     *
     * @param subject
     *            The JAAS subject to extract role names from
     * @param settings
     *            Settings that influence the way the roles are looked up. See
     *            {@link PicketLinkJaasSettings#getRolePrincipalName()}.
     * @return the list of roles the given subject belongs to.
     */
    private static List<String> getJaasRoles( final Subject subject, final PicketLinkJaasSettings settings ) {
        final List<String> roles = new ArrayList<String>();
        try {
            if ( subject != null ) {
                final Set<java.security.Principal> principals = subject.getPrincipals();

                if ( principals != null ) {
                    for ( java.security.Principal p : principals ) {
                        if ( p instanceof Group && settings.getRolePrincipalName().equalsIgnoreCase( p.getName() ) ) {
                            final Enumeration<? extends java.security.Principal> groups = ( (Group) p ).members();
                            while ( groups.hasMoreElements() ) {
                                final java.security.Principal groupPrincipal = groups.nextElement();
                                roles.add( groupPrincipal.getName() );
                            }
                            break;
                        }
                    }
                }

                // TODO implement this (needed for WebLogic support)
//                if ( rolesAdapterServiceLoader != null && rolesAdapterServiceLoader.iterator().hasNext() ) {
//                    for ( final RolesAdapter rolesAdapter : rolesAdapterServiceLoader ) {
//                        rolesAdapter.getRoles( principal, securityContext, mode );
//                    }
//                }
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
        return roles;
    }

}