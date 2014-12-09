package com.mycompany.uftutorial.server;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;

import com.mycompany.uftutorial.shared.InvitationService;
import com.mycompany.uftutorial.shared.Rsvp;

@ApplicationScoped
@Service
public class InvitationServiceImpl implements InvitationService {

  @Override
  public Rsvp getInvitation(long id) {
    Rsvp rsvp = new Rsvp();
    rsvp.setId(id);
    rsvp.setInviteeName("Person " + id);
    rsvp.setNumberOfGuests(1);
    System.out.println("Sending invitation to client: " + rsvp);
    return rsvp;
  }

  @Override
  public void saveResponse(Rsvp response) {
    System.out.println("Saving response: " + response);
  }

}
