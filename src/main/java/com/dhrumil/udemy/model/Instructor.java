package com.dhrumil.udemy.model;

public class Instructor {

  private String title;
  private String name;
  private String displayName;
  private String jobTitle;
  private String url;

  public Instructor() {
    super();
  }

  public Instructor(String title, String name, String displayName, String jobTitle, String url) {
    super();
    this.title = title;
    this.name = name;
    this.displayName = displayName;
    this.jobTitle = jobTitle;
    this.url = url;
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

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public String toString() {
    return "Instructor [title=" + title + ", name=" + name + ", displayName=" + displayName
        + ", jobTitle=" + jobTitle + ", url=" + url + "]";
  }
}
