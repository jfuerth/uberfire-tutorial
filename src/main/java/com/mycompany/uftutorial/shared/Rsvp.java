package com.mycompany.uftutorial.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

@Bindable
@Portable
public class Rsvp {

  private long id;
  private String inviteeName;
  private int numberOfGuests;
  private String comments;

  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }

  public String getInviteeName() {
    return inviteeName;
  }
  public void setInviteeName(String inviteeName) {
    this.inviteeName = inviteeName;
  }

  public int getNumberOfGuests() {
    return numberOfGuests;
  }
  public void setNumberOfGuests(int numberOfGuests) {
    this.numberOfGuests = numberOfGuests;
  }

  public String getComments() {
    return comments;
  }
  public void setComments(String comments) {
    this.comments = comments;
  }

  @Override
  public String toString() {
    return "Rsvp [id=" + id + ", inviteeName=" + inviteeName + ", numberOfGuests=" + numberOfGuests + ", comments="
            + comments + "]";
  }


}
