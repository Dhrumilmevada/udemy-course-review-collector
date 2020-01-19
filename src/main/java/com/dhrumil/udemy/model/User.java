package com.dhrumil.udemy.model;

public class User {

  private String title;
  private String name;
  private String displayName;

  public User() {
    super();
  }

  public User(String title, String name, String displayName) {
    super();
    this.title = title;
    this.name = name;
    this.displayName = displayName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String toString() {
    return "User [title=" + title + ", name=" + name + ", displayName=" + displayName + "]";
  }
}
