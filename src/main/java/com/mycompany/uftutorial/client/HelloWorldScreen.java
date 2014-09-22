package com.mycompany.uftutorial.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.annotations.WorkbenchToolBar;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.toolbar.IconType;
import org.uberfire.workbench.model.toolbar.ToolBar;
import org.uberfire.workbench.model.toolbar.impl.DefaultToolBar;
import org.uberfire.workbench.model.toolbar.impl.DefaultToolBarItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.mycompany.uftutorial.shared.Mood;

@Dependent
@WorkbenchScreen(identifier = "com.mycompany.uftutorial.client.HelloWorldScreen")
public class HelloWorldScreen {

  @Inject
  private SecurityContext securityContext;

  @Inject
  private Caller<AuthenticationService> securityService;

  private final VerticalPanel panel = new VerticalPanel();
  private final Label helloLabel = new Label();
  private final Button checkSecurityServiceButton = new Button("Ask server about current user");
  private final Label securityServiceResponseLabel = new Label();

  @PostConstruct
  private void init() {
    helloLabel.setText(getInitialLabelText());
    checkSecurityServiceButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        securityService.call(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            securityServiceResponseLabel.setText("Current user on server is " + response);
          }
        }).getUser();
      }
    });

    panel.add(helloLabel);
    panel.add(checkSecurityServiceButton);
    panel.add(securityServiceResponseLabel);
  }

  @WorkbenchPartTitle
  public String getTitle() {
    return "Greetings";
  }

  @WorkbenchPartView
  public IsWidget getView() {
    return panel;
  }

  private String getInitialLabelText() {
    StringBuilder sb = new StringBuilder();
    sb.append("Hello, ").append(securityContext.getCachedUser().getIdentifier());
    sb.append(". Welcome to UberFire!");
    sb.append(" Your roles: ");
    for (Role role : securityContext.getCachedUser().getRoles()) {
      sb.append(role.getName()).append(" ");
    }
    sb.append(" Your groups: ");
    for (Group group : securityContext.getCachedUser().getGroups()) {
      sb.append(group.getName()).append(" ");
    }
    return sb.toString();
  }

  @WorkbenchToolBar
  public ToolBar getToolBar() {
    ToolBar tb = new DefaultToolBar("hello-world-toolbar");
    tb.addItem(new DefaultToolBarItem(IconType.ASTERISK, "Reset Hello Screen", new Command() {
      @Override
      public void execute() {
        helloLabel.setText(getInitialLabelText());
      }
    }));
    return tb;
  }

  public void onMoodChange(@Observes Mood mood) {
    helloLabel.setText("I understand you are feeling " + mood.getText());
  }
}
