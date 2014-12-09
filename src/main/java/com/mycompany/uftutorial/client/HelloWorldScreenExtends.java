package com.mycompany.uftutorial.client;

import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.uberfire.client.mvp.AbstractWorkbenchScreenActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Named("com.mycompany.uftutorial.client.HelloWorldScreenExtends")
public class HelloWorldScreenExtends extends AbstractWorkbenchScreenActivity {

  @Inject
  public HelloWorldScreenExtends(PlaceManager placeManager) {
    super(placeManager);
  }

  private final Label label = new Label();

  @Override
  public String getTitle() {
    return "Greetings";
  }

  @Override
  public IsWidget getWidget() {
    return label;
  }

  @Override
  public void onStartup(PlaceRequest place) {
    super.onStartup(place);
    label.setText("Hello " + place.getParameter("name", "World"));
  }

  @Override
  public String getSignatureId() {
    return getClass().getName();
  }

  @Override
  public Collection<String> getRoles() {
    return Collections.emptyList();
  }

  @Override
  public Collection<String> getTraits() {
    return Collections.emptyList();
  }
}
