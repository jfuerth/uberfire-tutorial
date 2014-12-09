package com.mycompany.uftutorial.shared;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface InvitationService {

  public Rsvp getInvitation(long id);

  public void saveResponse(Rsvp response);

}
