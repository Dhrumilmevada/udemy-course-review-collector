package com.dhrumil.udemy.review.runnable;

import java.util.ArrayList;
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

public class UdemyReviewThread implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdemyReviewThread.class);

  private ArrayBlockingQueue<String> courseQueue = null;
  private UdemyCourseListThread courseListRunnable = null;
  private AtomicBoolean isCourseReviewThreadRunning = null;
  private MongoDBClient dbClient = null;

  public UdemyReviewThread(ArrayBlockingQueue<String> courseList,
      UdemyCourseListThread courseListRunnable) {
    super();
    this.courseQueue = courseList;
    this.courseListRunnable = courseListRunnable;
    this.isCourseReviewThreadRunning = new AtomicBoolean(true);
    dbClient = MongoDBClient.getInstance();
  }

  @Override
  public void run() {

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
        int courseBulkProcess = getBulkCourseCount(courseQueue);
        List<String> courseIdList = new ArrayList<String>();
        courseQueue.drainTo(courseIdList, courseBulkProcess);

        courseIdList.parallelStream().forEach(courseID -> {
          if (courseID == null) {
            LOGGER.warn("Course queue is either empty or course data is null");
          } else {
            UdemyCourseDetailClient courseDetailClient = new UdemyCourseDetailClient(courseID);
            Course course = courseDetailClient.getCourseDetail();
            this.dbClient.insert(course);
            courseDetailClient = null;
            UdemyCourseReviewClient courseReviewClient =
                new UdemyCourseReviewClient(courseID, 1000);
            getAllReviews(courseReviewClient, courseID);
            courseReviewClient = null;
          }
        });
        courseIdList.clear();
      }
    }
  }

  private int getBulkCourseCount(ArrayBlockingQueue<String> courseQueue) {
    if (courseQueue.size() >= 4) {
      int bulkCourseCount = (int) (courseQueue.size() * 0.25);
      return bulkCourseCount > 100 ? 100 : bulkCourseCount;
    } else {
      return courseQueue.size();
    }
  }

  private void getAllReviews(UdemyCourseReviewClient courseReviewClient, String courseId) {
    boolean gotAllReview = false;
    while (!gotAllReview) {
      List<Review> reviewList = null;
      try {
        reviewList = courseReviewClient.getNextReview();
      } catch (InterruptedException e) {
        LOGGER.error(
            "Got InterruptedException errorCause: [{}] errorMessage: [{}] errorStackTrace: [{}]",
            e.getCause(), e.getMessage(), e.getStackTrace());
      }

      if (reviewList == null) {
        continue;
      }

      if (reviewList.size() == 0) {
        LOGGER.warn("Course review list is empty for course id :[{}]", courseId);
        gotAllReview = true;
      }

      else {
        MongoDBClient.getInstance().insertMany(reviewList);
      }
    }
  }
}
