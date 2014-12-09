package com.mycompany.uftutorial.client;

import java.util.Arrays;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.http.client.Response;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.mycompany.uftutorial.shared.InvitationService;
import com.mycompany.uftutorial.shared.Rsvp;

@Dependent
@Templated
@WorkbenchScreen(identifier = "com.mycompany.uftutorial.client.InvitationRsvpErraiUI")
public class InvitationRsvpErraiUI extends Composite {

  @Inject
  private Caller<InvitationService> invitationService;

  @Inject @AutoBound
  private DataBinder<Rsvp> rsvpBinder;

  @Inject @Bound @DataField
  private TextBox inviteeName;

  @Bound @DataField
  private final ValueListBox<Integer> numberOfGuests = new ValueListBox<Integer>(IntegerRenderer.instance());

  @Inject @Bound @DataField
  private TextArea comments;

  @Inject @DataField
  private Button save;

  private boolean hasChanges;

  @OnStartup
  public void setup(PlaceRequest place) {
    numberOfGuests.setAcceptableValues(Arrays.asList(1, 2, 3, 4));
    final int id = Integer.parseInt(place.getParameter("id", "-1"));
    invitationService.call(new RemoteCallback<Rsvp>() {
      @Override
      public void callback(Rsvp response) {
        rsvpBinder.setModel(response);
      }
    }).getInvitation(id);
  }

  @WorkbenchPartTitle
  public String getPartTitle() {
    return "RSVP (ErraiUI)";
  }

  @EventHandler
  private void onAnyChange(KeyDownEvent e) {
    hasChanges = true;
  }

  @OnMayClose
  public boolean mayClose() {
    if (hasChanges) {
      return Window.confirm("Changes to your RSVP have not been saved.\n"
                            + "Close without saving?");
    }
    return true;
  }

  @EventHandler("save")
  private void save(ClickEvent e) {
    invitationService.call(new ResponseCallback() {
      @Override
      public void callback(Response response) {
        hasChanges = false;
      }
    }).saveResponse(rsvpBinder.getModel());
  }
}
