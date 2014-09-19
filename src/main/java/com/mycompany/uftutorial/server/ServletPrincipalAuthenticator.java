package com.mycompany.uftutorial.server;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

/**
 * A PicketLink Authenticator that pairs with {@link ServletRequestAuthScheme} to integrate PicketLink logins with
 * standard Servlet API logins. This class is automatically picked up and used by PicketLink as long as it is on the
 * classpath, but it will only function properly on web requests that were intercepted by PicketLink's
 * AuthenticationFilter configured with the ServletRequestAuthScheme.
 * <p>
 * Design note: it would probably be a better design if this class was an IdentityStore rather than an Authenticator.
 * The way this Authenticator works is to insert new users and roles into whatever IdentityStore is currently in use
 * within PicketLink, which introduces unnecessary statefulness into the process and is fertile ground for lurking bugs.
 *
 * @see org.picketlink.authentication.web.AuthenticationFilter
 * @see ServletRequestAuthScheme
 */
@ApplicationScoped
@PicketLink
public class ServletPrincipalAuthenticator extends BaseAuthenticator {

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject IdentityManager identityManager;
    @Inject RelationshipManager relationshipManager;

    @Override
    public void authenticate() {
      if ( !(credentials.getCredential() instanceof org.jboss.errai.security.shared.api.identity.User) ) {
        setAccount( null );
        setStatus( AuthenticationStatus.FAILURE );
        return;
      }

      org.jboss.errai.security.shared.api.identity.User erraiUser =
              (org.jboss.errai.security.shared.api.identity.User) credentials.getCredential();

      // FIXME These are persistent operations in PicketLink. Copying JAAS users into the PL identity store is not a good idea.
      // We probably need to change this class to be an IdentityStore rather than an Authenticator.
      User persistedUser = queryForUser( erraiUser.getIdentifier() );

      if ( persistedUser == null ) {
        identityManager.add( new User( credentials.getUserId() ) );
        persistedUser = queryForUser( credentials.getUserId() );
      }

      for ( org.jboss.errai.security.shared.api.Role erraiRole : erraiUser.getRoles() ) {
        Role persistedRole = queryForRole( erraiRole.getName() );
        if ( persistedRole == null ) {
          identityManager.add( new Role( erraiRole.getName() ) );
          persistedRole = queryForRole( erraiRole.getName() );
        }
        if ( !BasicModel.hasRole( relationshipManager, persistedUser, persistedRole ) ) {
          relationshipManager.add( new Grant( persistedUser, persistedRole ) );
        }
      }

      for ( org.jboss.errai.security.shared.api.Group erraiGroup : erraiUser.getGroups() ) {
        Group persistedGroup = queryForGroup( erraiGroup.getName() );
        if ( persistedGroup == null ) {
          identityManager.add( new Group( erraiGroup.getName() ) );
          persistedGroup = queryForGroup( erraiGroup.getName() );
        }
        if ( !BasicModel.isMember( relationshipManager, persistedUser, persistedGroup ) ) {
          relationshipManager.add( new GroupMembership( persistedUser, persistedGroup ) );
        }
      }

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

    private Group queryForGroup( String groupName ) {
      List<Group> resultList = identityManager
              .createIdentityQuery( Group.class )
              .setParameter( Group.NAME, groupName )
              .getResultList();
      if ( resultList.isEmpty() ) {
          return null;
      }
      return resultList.get(0);
  }

}