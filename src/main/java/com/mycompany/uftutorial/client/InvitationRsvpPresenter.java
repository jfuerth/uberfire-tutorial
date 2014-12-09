package com.mycompany.uftutorial.client;

import static org.uberfire.commons.validation.PortablePreconditions.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.user.client.ui.IsWidget;
import com.mycompany.uftutorial.shared.InvitationService;
import com.mycompany.uftutorial.shared.Rsvp;

@Dependent
@WorkbenchScreen(identifier = "com.mycompany.uftutorial.client.InvitationRsvpPresenter")
public class InvitationRsvpPresenter {

  @Inject
  private Caller<InvitationService> invitationService;

  private Rsvp rsvp;
  private final View view;

  public interface View extends UberView<InvitationRsvpPresenter> {
    String getInviteeName();
    void setInviteeName(String inviteeName);

    int getNumberOfGuests();
    void setNumberOfGuests(int numberOfGuests);

    String getComments();
    void setComments(String comments);

    boolean confirmSave();
  }

  @Inject
  public InvitationRsvpPresenter(View view) {
    this.view = checkNotNull("view", view);
  }

  @OnStartup
  public void setup(PlaceRequest place) {
    view.init(this);
    final int id = Integer.parseInt(place.getParameter("id", "-1"));
    invitationService.call(new RemoteCallback<Rsvp>() {
      @Override
      public void callback(Rsvp response) {
        rsvp = response;
        updateViewFromModel();
      }
    }).getInvitation(id);
  }

  public void save() {
    updateModelFromView();
    invitationService.call().saveResponse(rsvp);
  }

  @WorkbenchPartTitle
  public String getPartTitle() {
    return "RSVP (MVP)";
  }

  @WorkbenchPartView
  public IsWidget getView() {
    return view;
  }

  @OnMayClose
  public boolean mayClose() {
    if (hasChanges()) {
      return view.confirmSave();
    }
    return true;
  }

  boolean hasChanges() {
    if (rsvp == null) {
      return false;
    }

    return isDifferent(view.getInviteeName(), rsvp.getInviteeName())
           || view.getNumberOfGuests() != rsvp.getNumberOfGuests()
           || isDifferent(view.getComments(), rsvp.getComments());
  }

  private static boolean isDifferent(String s1, String s2) {
    if (s1 == null) {
      return s2 != null && !s2.trim().isEmpty();
    }
    if (s2 == null) {
      return !s1.trim().isEmpty();
    }
    return !s1.trim().equals(s2.trim());
  }

  void updateViewFromModel() {
    view.setInviteeName(rsvp.getInviteeName());
    view.setNumberOfGuests(rsvp.getNumberOfGuests());
    view.setComments(rsvp.getComments());
  }

  void updateModelFromView() {
    rsvp.setInviteeName(view.getInviteeName());
    rsvp.setNumberOfGuests(view.getNumberOfGuests());
    rsvp.setComments(view.getComments());
  }
}
