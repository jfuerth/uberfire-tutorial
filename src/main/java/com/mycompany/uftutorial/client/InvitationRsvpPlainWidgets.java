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
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.lifecycle.OnMayClose;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.mycompany.uftutorial.shared.InvitationService;
import com.mycompany.uftutorial.shared.Rsvp;

@Dependent
@WorkbenchScreen(identifier = "com.mycompany.uftutorial.client.InvitationRsvpPlainWidgets")
public class InvitationRsvpPlainWidgets {

  @Inject
  private Caller<InvitationService> invitationService;

  @Inject @AutoBound
  private DataBinder<Rsvp> rsvpBinder;

  private final VerticalPanel panel = new VerticalPanel();

  @Bound
  private final TextBox inviteeName = new TextBox();

  @Bound
  private final ValueListBox<Integer> numberOfGuests = new ValueListBox<Integer>(IntegerRenderer.instance());

  @Bound
  private final TextArea comments = new TextArea();

  private final Button save = new Button("Save");

  private boolean hasChanges;

  @OnStartup
  public void setup(PlaceRequest place) {
    numberOfGuests.setAcceptableValues(Arrays.asList(1, 2, 3, 4));

    final int id = Integer.parseInt(place.getParameter("id", "-1"));
    invitationService.call(new RemoteCallback<Rsvp>() {
      @Override
      public void callback(Rsvp response) {
        rsvpBinder.setModel(response);
        hasChanges = false;
      }
    }).getInvitation(id);

    ValueChangeHandler dirtyStateHandler = new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        hasChanges = true;
      }
    };

    inviteeName.addValueChangeHandler(dirtyStateHandler);
    numberOfGuests.addValueChangeHandler(dirtyStateHandler);
    comments.addValueChangeHandler(dirtyStateHandler);

    panel.add(new Label("Name:"));
    panel.add(inviteeName);

    panel.add(new Label("Number of Guests:"));
    panel.add(numberOfGuests);

    panel.add(new Label("Comments:"));
    panel.add(comments);

    panel.add(save);

    save.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        invitationService.call(new ResponseCallback() {
          @Override
          public void callback(Response response) {
            hasChanges = false;
          }
        }).saveResponse(rsvpBinder.getModel());
      }
    });
  }

  @WorkbenchPartTitle
  public String getPartTitle() {
    return "RSVP (Plain Widgets)";
  }

  @WorkbenchPartView
  public Panel getView() {
    return panel;
  }

  @OnMayClose
  public boolean mayClose() {
    if (hasChanges) {
      return Window.confirm("Changes to your RSVP have not been saved.\n"
                            + "Close without saving?");
    }
    return true;
  }
}
