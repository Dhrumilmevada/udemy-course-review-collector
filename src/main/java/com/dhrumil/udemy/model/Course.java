package com.dhrumil.udemy.model;

import java.util.List;

public class Course {

  private long courseId;
  private String title;
  private String url;
  private boolean paid;
  private int amount;
  private String currency;
  private List<Instructor> instructors;
  private String headline;
  private int subscriberCount;
  private boolean isDiscountAvailable;
  private int discountedPrice;
  private int savingAmount;
  private int savingPercentages;
  private int lectureCount;
  private int quizzesCount;
  private int practiceTestCount;
  private String category;
  private String subcategory;
  private String createdOn;
  private String publishedOn;
  private double contentLength;
  private String contentLengthUnit;
  private List<String> prerequisites;
  private List<String> objectives;
  private List<String> targetAudiences;
  private String updatedOn;
  private boolean isPreviewAvailable;
  private String previewUrl;
  private List<String> metadata;
  private String description;
  private String discountStartTime;
  private String discountEndTime;

  public Course() {
    super();
  }

  // not recommended to use... it is not good practice
  public Course(long courseId, String title, String url, boolean paid, int amount, String currency,
      List<Instructor> instructors, String headline, int subscriberCount,
      boolean isDiscountAvailable, int discountedPrice, int savingAmount, int savingPercentages,
      int lectureCount, int quizzesCount, int practiceTestCount, String category,
      String subcategory, String createdOn, String publishedOn, double contentLength,
      String contentLengthUnit, List<String> prerequisites, List<String> objectives,
      List<String> targetAudiences, String updatedOn, boolean isPreviewAvailable, String previewUrl,
      List<String> metadata, String description, String discountStartTime, String discountEndTime) {
    super();
    this.courseId = courseId;
    this.title = title;
    this.url = url;
    this.paid = paid;
    this.amount = amount;
    this.currency = currency;
    this.instructors = instructors;
    this.headline = headline;
    this.subscriberCount = subscriberCount;
    this.isDiscountAvailable = isDiscountAvailable;
    this.discountedPrice = discountedPrice;
    this.savingAmount = savingAmount;
    this.savingPercentages = savingPercentages;
    this.lectureCount = lectureCount;
    this.quizzesCount = quizzesCount;
    this.practiceTestCount = practiceTestCount;
    this.category = category;
    this.subcategory = subcategory;
    this.createdOn = createdOn;
    this.publishedOn = publishedOn;
    this.contentLength = contentLength;
    this.contentLengthUnit = contentLengthUnit;
    this.prerequisites = prerequisites;
    this.objectives = objectives;
    this.targetAudiences = targetAudiences;
    this.updatedOn = updatedOn;
    this.isPreviewAvailable = isPreviewAvailable;
    this.previewUrl = previewUrl;
    this.metadata = metadata;
    this.description = description;
    this.discountStartTime = discountStartTime;
    this.discountEndTime = discountEndTime;
  }

  public long getCourseId() {
    return courseId;
  }

  public void setCourseId(long courseId) {
    this.courseId = courseId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isPaid() {
    return paid;
  }

  public void setPaid(boolean paid) {
    this.paid = paid;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public List<Instructor> getInstructors() {
    return instructors;
  }

  public void setInstructors(List<Instructor> instructors) {
    this.instructors = instructors;
  }

  public String getHeadline() {
    return headline;
  }

  public void setHeadline(String headline) {
    this.headline = headline;
  }

  public int getSubscriberCount() {
    return subscriberCount;
  }

  public void setSubscriberCount(int subscriberCount) {
    this.subscriberCount = subscriberCount;
  }

  public boolean isDiscountAvailable() {
    return isDiscountAvailable;
  }

  public void setDiscountAvailable(boolean isDiscountAvailable) {
    this.isDiscountAvailable = isDiscountAvailable;
  }

  public int getDiscountedPrice() {
    return discountedPrice;
  }

  public void setDiscountedPrice(int discountedPrice) {
    this.discountedPrice = discountedPrice;
  }

  public int getSavingAmount() {
    return savingAmount;
  }

  public void setSavingAmount(int savingAmount) {
    this.savingAmount = savingAmount;
  }

  public int getSavingPercentages() {
    return savingPercentages;
  }

  public void setSavingPercentages(int savingPercentages) {
    this.savingPercentages = savingPercentages;
  }

  public int getLectureCount() {
    return lectureCount;
  }

  public void setLectureCount(int lectureCount) {
    this.lectureCount = lectureCount;
  }

  public int getQuizzesCount() {
    return quizzesCount;
  }

  public void setQuizzesCount(int quizzesCount) {
    this.quizzesCount = quizzesCount;
  }

  public int getPracticeTestCount() {
    return practiceTestCount;
  }

  public void setPracticeTestCount(int practiceTestCount) {
    this.practiceTestCount = practiceTestCount;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubcategory() {
    return subcategory;
  }

  public void setSubcategory(String subcategory) {
    this.subcategory = subcategory;
  }

  public String getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(String createdOn) {
    this.createdOn = createdOn;
  }

  public String getPublishedOn() {
    return publishedOn;
  }

  public void setPublishedOn(String publishedOn) {
    this.publishedOn = publishedOn;
  }

  public double getContentLength() {
    return contentLength;
  }

  public void setContentLength(double contentLength) {
    this.contentLength = contentLength;
  }

  public String getContentLengthUnit() {
    return contentLengthUnit;
  }

  public void setContentLengthUnit(String contentLengthUnit) {
    this.contentLengthUnit = contentLengthUnit;
  }

  public List<String> getPrerequisites() {
    return prerequisites;
  }

  public void setPrerequisites(List<String> prerequisites) {
    this.prerequisites = prerequisites;
  }

  public List<String> getObjectives() {
    return objectives;
  }

  public void setObjectives(List<String> objectives) {
    this.objectives = objectives;
  }

  public List<String> getTargetAudiences() {
    return targetAudiences;
  }

  public void setTargetAudiences(List<String> targetAudiences) {
    this.targetAudiences = targetAudiences;
  }

  public String getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(String updatedOn) {
    this.updatedOn = updatedOn;
  }

  public boolean isPreviewAvailable() {
    return isPreviewAvailable;
  }

  public void setPreviewAvailable(boolean isPreviewAvailable) {
    this.isPreviewAvailable = isPreviewAvailable;
  }

  public String getPreviewUrl() {
    return previewUrl;
  }

  public void setPreviewUrl(String previewUrl) {
    this.previewUrl = previewUrl;
  }

  public List<String> getMetadata() {
    return metadata;
  }

  public void setMetadata(List<String> metadata) {
    this.metadata = metadata;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDiscountStartTime() {
    return discountStartTime;
  }

  public void setDiscountStartTime(String discountStartTime) {
    this.discountStartTime = discountStartTime;
  }

  public String getDiscountEndTime() {
    return discountEndTime;
  }

  public void setDiscountEndTime(String discountEndTime) {
    this.discountEndTime = discountEndTime;
  }

  @Override
  public String toString() {
    return "Course [courseId=" + courseId + ", title=" + title + ", url=" + url + ", paid=" + paid
        + ", amount=" + amount + ", currency=" + currency + ", instructors=" + instructors
        + ", headline=" + headline + ", subscriberCount=" + subscriberCount
        + ", isDiscountAvailable=" + isDiscountAvailable + ", discountedPrice=" + discountedPrice
        + ", savingAmount=" + savingAmount + ", savingPercentages=" + savingPercentages
        + ", lectureCount=" + lectureCount + ", quizzesCount=" + quizzesCount
        + ", practiceTestCount=" + practiceTestCount + ", category=" + category + ", subcategory="
        + subcategory + ", createdOn=" + createdOn + ", publishedOn=" + publishedOn
        + ", contentLength=" + contentLength + ", contentLengthUnit=" + contentLengthUnit
        + ", prerequisites=" + prerequisites + ", objectives=" + objectives + ", targetAudiences="
        + targetAudiences + ", updatedOn=" + updatedOn + ", isPreviewAvailable="
        + isPreviewAvailable + ", previewUrl=" + previewUrl + ", metadata=" + metadata
        + ", description=" + description + ", discountStartTime=" + discountStartTime
        + ", discountEndTime=" + discountEndTime + "]";
  }
}
