package com.dhrumil.udemy.model;

public class CourseCache {

  private long courseId;
  private boolean isCourseProcessed;
  private long courseProcessedOn;
  private boolean isReviewProcessed;
  private long revieweProcessedOn;

  public CourseCache() {
    super();
    this.courseId = 0L;
    this.isCourseProcessed = false;
    this.courseProcessedOn = 0L;
    this.isReviewProcessed = false;
    this.revieweProcessedOn = 0L;

  }

  public CourseCache(long courseId, boolean isCourseProcessed, long courseProcessedOn,
      boolean isReviewProcessed, long revieweProcessedOn) {
    super();
    this.courseId = courseId;
    this.isCourseProcessed = isCourseProcessed;
    this.courseProcessedOn = courseProcessedOn;
    this.isReviewProcessed = isReviewProcessed;
    this.revieweProcessedOn = revieweProcessedOn;
  }

  public long getCourseId() {
    return courseId;
  }

  public void setCourseId(long courseId) {
    this.courseId = courseId;
  }

  public boolean isCourseProcessed() {
    return isCourseProcessed;
  }

  public void setCourseProcessed(boolean isCourseProcessed) {
    this.isCourseProcessed = isCourseProcessed;
  }

  public long getCourseProcessedOn() {
    return courseProcessedOn;
  }

  public void setCourseProcessedOn(long courseProcessedOn) {
    this.courseProcessedOn = courseProcessedOn;
  }

  public boolean isReviewProcessed() {
    return isReviewProcessed;
  }

  public void setReviewProcessed(boolean isReviewProcessed) {
    this.isReviewProcessed = isReviewProcessed;
  }

  public long getRevieweProcessedOn() {
    return revieweProcessedOn;
  }

  public void setRevieweProcessedOn(long revieweProcessedOn) {
    this.revieweProcessedOn = revieweProcessedOn;
  }

  @Override
  public String toString() {
    return "CourseCache [courseId=" + courseId + ", isCourseProcessed=" + isCourseProcessed
        + ", courseProcessedOn=" + courseProcessedOn + ", isReviewProcessed=" + isReviewProcessed
        + ", revieweProcessedOn=" + revieweProcessedOn + "]";
  }


}
