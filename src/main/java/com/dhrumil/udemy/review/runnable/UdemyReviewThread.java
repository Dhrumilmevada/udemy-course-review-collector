package com.dhrumil.udemy.review.runnable;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Course;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.mongodb.client.MongoDBClient;
import com.dhrumil.udemy.review.client.UdemyCourseDetailClient;
import com.dhrumil.udemy.review.client.UdemyCourseReviewClient;
import com.dhrumil.udemy.review.collector.main.AppConfig;

public class UdemyReviewThread implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdemyReviewThread.class);

  private final int COURSEREVIEW_QUEUE_SIZE =
      AppConfig.CONFIG.getInt("app.udemy.coursereview.queue.size");

  private ArrayBlockingQueue<String> courseQueue = null;
  private ArrayBlockingQueue<Review> courseReview = null;
  private UdemyCourseListThread courseListRunnable = null;
  private AtomicBoolean isCourseReviewThreadRunning = null;
  private MongoDBClient dbClient = null;

  public UdemyReviewThread(ArrayBlockingQueue<String> courseList,
      ArrayBlockingQueue<Review> courseReview, UdemyCourseListThread courseListRunnable) {
    super();
    this.courseQueue = courseList;
    this.courseReview = courseReview;
    this.courseListRunnable = courseListRunnable;
    this.isCourseReviewThreadRunning = new AtomicBoolean(true);
    dbClient = MongoDBClient.getInstance();
  }

  @Override
  public void run() {
    UdemyCourseDetailClient courseDetailClient = null;
    UdemyCourseReviewClient courseReviewClient = null;
    while (isCourseReviewThreadRunning.get()) {
      if (this.courseQueue.size() == 0) {
        LOGGER.warn("Course queue is empty ... wait for some time, let come some courses in queue");
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOGGER.error(
              "Error while thread is in sleep state errorMessage:[{}], errorCause:[{}],errorStackTrace:[{}]",
              e.getMessage(), e.getCause(), e.getStackTrace());
        }
        if (!this.courseListRunnable.getIsCourseListThreadRunnig()) {
          this.isCourseReviewThreadRunning.getAndSet(false);
        }
        continue;
      } else {
        String courseId = courseQueue.poll();

        if (courseId == null) {
          LOGGER.warn("Course queue is either empty or course data is null");
          continue;
        } else {
          courseDetailClient = new UdemyCourseDetailClient(courseId);
          Course course = courseDetailClient.getCourseDetail();
          this.dbClient.insert(course);
          courseReviewClient = new UdemyCourseReviewClient(courseId, 1000);
          getAllReviews(courseReviewClient, courseId);
        }
      }
    }
  }

  private void getAllReviews(UdemyCourseReviewClient courseReviewClient, String courseId) {
    boolean gotAllReview = false;
    while (!gotAllReview) {
      List<Review> reviewList = courseReviewClient.getNextReview();

      if (reviewList.size() == 0) {
        LOGGER.warn("Course list is empty for course id :[{}]", courseId);
        gotAllReview = true;
      } else if (courseReview.size() == COURSEREVIEW_QUEUE_SIZE) {
        LOGGER.warn("Course review queue of size [{}] is full... Waiting for 2s",
            COURSEREVIEW_QUEUE_SIZE);
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          LOGGER.error(
              "Error while thread is in sleep state errorMessage:[{}], errorCause:[{}], errorStackTrace:[{}]",
              e.getMessage(), e.getCause(), e.getStackTrace());
        }
      } else {
        while ((courseReview.size() + reviewList.size()) >= COURSEREVIEW_QUEUE_SIZE) {
          LOGGER.info(
              "Course review queue is about to full, slowing down the process and waiting for enough space in queue");
          try {
            Thread.sleep(60000);
          } catch (InterruptedException e) {
            LOGGER.error(
                "Exception while waiting for enough space in queue errorMessage : [{}], errorStackTrace : [{}],errorCause : [{}]",
                e.getMessage(), e.getStackTrace(), e.getCause());
          }
        }
        boolean isReviewAdded = this.courseReview.addAll(reviewList);
        if (isReviewAdded) {
          LOGGER.info(
              "Successfully added [{}] reviews to queue ,queue size:[{}], queue capacity:[{}]",
              reviewList.size(), courseReview.size(), COURSEREVIEW_QUEUE_SIZE);
        }
      }
    }
  }
}
