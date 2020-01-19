package com.dhrumil.udemy.model;

public class Review {

  private long id;
  private String content;
  private double rating;
  private String created;
  private String modified;
  private String userModified;
  private long courseId;
  private User user;

  public Review() {
    super();
  }

  public Review(long id, String content, double rating, String created, String modified,
      String userModified, long courseId, User user) {
    super();
    this.id = id;
    this.content = content;
    this.rating = rating;
    this.created = created;
    this.modified = modified;
    this.userModified = userModified;
    this.courseId = courseId;
    this.user = user;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getUserModified() {
    return userModified;
  }

  public void setUserModified(String userModified) {
    this.userModified = userModified;
  }

  public long getCourseId() {
    return courseId;
  }

  public void setCourseId(long courseId) {
    this.courseId = courseId;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return "Review [id=" + id + ", content=" + content + ", rating=" + rating + ", created="
        + created + ", modified=" + modified + ", userModified=" + userModified + ", courseId="
        + courseId + ", user=" + user + "]";
  }


}
