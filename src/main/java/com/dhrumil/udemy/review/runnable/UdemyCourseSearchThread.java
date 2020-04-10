package com.dhrumil.udemy.review.runnable;

import java.util.concurrent.ArrayBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.collector.main.AppConfig;

public class UdemyCourseSearchThread implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdemyCourseSearchThread.class);

  private static final int COURSELIST_QUEUE_CAPACITY =
      AppConfig.CONFIG.getInt("app.udemy.courselist.queue.size");
  private static final int COURSELIST_PAGESIZE =
      AppConfig.CONFIG.getInt("app.udemy.course.list.pagesize");

  private UdemyCourseListThread courseListThread = null;
  private UdemyReviewThread reviewThread = null;
  private ArrayBlockingQueue<String> courseListQueue = null;
  private String searchTopic = null;

  public UdemyCourseSearchThread(String searchTopic) {
    this.searchTopic = searchTopic;
    courseListQueue = new ArrayBlockingQueue<>(COURSELIST_QUEUE_CAPACITY);
    this.courseListThread =
        new UdemyCourseListThread(this.searchTopic, COURSELIST_PAGESIZE, this.courseListQueue);
    this.reviewThread = new UdemyReviewThread(courseListQueue, courseListThread);

  }


  public String getSearchTopic() {
    return this.searchTopic;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return this.searchTopic;
  }


  @Override
  public void run() {
    Thread courseList = new Thread(this.courseListThread);
    Thread coursereview = new Thread(this.reviewThread);
    courseList.setName(searchTopic + "-course-list");
    coursereview.setName(searchTopic + "-course-review");
    courseList.start();
    coursereview.start();
  }

}
