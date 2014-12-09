package com.mycompany.uftutorial.client;

import static org.uberfire.commons.validation.PortablePreconditions.*;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.client.IntegerRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

@Dependent
public class InvitationRsvpView implements InvitationRsvpPresenter.View {

  private InvitationRsvpPresenter presenter;

  private final VerticalPanel panel = new VerticalPanel();
  private final TextBox inviteeName = new TextBox();
  private final ValueListBox<Integer> numberOfGuests = new ValueListBox<Integer>(IntegerRenderer.instance());
  private final TextArea comments = new TextArea();
  private final Button save = new Button("Save");

  @PostConstruct
  private void setupUi() {
    numberOfGuests.setAcceptableValues(Arrays.asList(1, 2, 3, 4));

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
        presenter.save();
      }
    });
  }

  @Override
  public void init(InvitationRsvpPresenter presenter) {
    this.presenter = checkNotNull("presenter", presenter);
  }

  @Override
  public Widget asWidget() {
    return panel;
  }

  @Override
  public String getInviteeName() {
    return inviteeName.getText();
  }

  @Override
  public void setInviteeName(String inviteeName) {
    this.inviteeName.setText(inviteeName);
  }

  @Override
  public int getNumberOfGuests() {
    return numberOfGuests.getValue();
  }

  @Override
  public void setNumberOfGuests(int numberOfGuests) {
    this.numberOfGuests.setValue(numberOfGuests);
  }

  @Override
  public String getComments() {
    return comments.getText();
  }

  @Override
  public void setComments(String comments) {
    this.comments.setText(comments);
  }

  @Override
  public boolean confirmSave() {
    return Window.confirm("Changes to your RSVP have not been saved.\n"
            + "Close without saving?");
  }
}
