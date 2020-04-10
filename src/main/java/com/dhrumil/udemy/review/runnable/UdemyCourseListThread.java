package com.dhrumil.udemy.review.runnable;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.redis.client.RedisCacheClient;
import com.dhrumil.udemy.review.client.UdemyCourseListClient;
import com.dhrumil.udemy.review.collector.main.AppConfig;
import redis.clients.jedis.Jedis;

public class UdemyCourseListThread extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdemyCourseListThread.class);
  private final int COURSELIST_QUEUE_SIZE =
      AppConfig.CONFIG.getInt("app.udemy.courselist.queue.size");
  private static final String COURSE_CACHE_KEY =
      AppConfig.CONFIG.getString("app.redis.course.cache.key");
  private Jedis jedis = null;

  private UdemyCourseListClient searchCourses = null;
  private ArrayBlockingQueue<String> courseQueue = null;
  private String searchTopic = null;
  private int pageSize = 0;
  private AtomicBoolean isCourseListThreadRunnig = null;

  public UdemyCourseListThread(String searchTopic, int pageSize,
      ArrayBlockingQueue<String> courseList) {
    super();
    this.searchTopic = searchTopic;
    this.pageSize = pageSize;
    this.courseQueue = courseList;
    this.isCourseListThreadRunnig = new AtomicBoolean(true);
    this.searchCourses = new UdemyCourseListClient(this.searchTopic, this.pageSize);
    this.jedis = RedisCacheClient.getInstance().getJedisThread();
  }

  public boolean getIsCourseListThreadRunnig() {
    return isCourseListThreadRunnig.get();
  }

  @Override
  public void run() {

    while (this.isCourseListThreadRunnig.get()) {
      List<String> courseList = null;
      try {
        courseList = searchCourses.getNextCourses();
      } catch (InterruptedException e1) {
        LOGGER.error(
            "Got InterruptedException errorCause: [{}] errorMessage: [{}] errorStackTrace: [{}]",
            e1.getCause(), e1.getMessage(), e1.getStackTrace());
      }

      if (courseList == null) {
        continue;
      }

      if (courseList.size() == 0) {
        LOGGER.warn("Course list is empty for for searched topic :[{}]", this.searchTopic);
        this.isCourseListThreadRunnig.getAndSet(false);
      } else if (this.courseQueue.size() == COURSELIST_QUEUE_SIZE) {
        LOGGER.warn("Course list queue of size [{}] is full... Waiting for 2s",
            COURSELIST_QUEUE_SIZE);
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          LOGGER.error(
              "Error while thread is in sleep state errorMessage:[{}], errorCause:[{}], errorStackTrace:[{}]",
              e.getMessage(), e.getCause(), e.getStackTrace());
        }
      } else {
        while ((courseQueue.size() + courseList.size()) >= COURSELIST_QUEUE_SIZE) {
          LOGGER.info(
              "Course list queue is about to full, slowing down the process and waiting for enough space in queue");
          try {
            Thread.sleep(60000);
          } catch (InterruptedException e) {
            LOGGER.error(
                "Exception while waiting for enough space in queue errorMessage : [{}], errorStackTrace : [{}],errorCause : [{}]",
                e.getMessage(), e.getStackTrace(), e.getCause());
          }
        }
        courseList.stream().forEach(item -> {
          if (isCourseAlreadyProcessed(item)) {
            LOGGER.info("Course with id : [{}] is already processed, no need to process again",
                item);
          } else {
            boolean isCourseAdded = courseQueue.add(item);
            if (isCourseAdded) {
              RedisCacheClient.getInstance().setHash(this.jedis, COURSE_CACHE_KEY, item,
                  System.currentTimeMillis());
              LOGGER.info(
                  "Successfully added course : [{}] to queue ,queue size:[{}], queue capacity:[{}]",
                  item, courseQueue.size(), COURSELIST_QUEUE_SIZE);
            }
          }
        });
        courseList.clear();

        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          LOGGER.error(
              "Exception while thread in sleeping state errorMessage : [{}], errorStackTrace : [{}],errorCause : [{}]",
              e.getMessage(), e.getStackTrace(), e.getCause());
        }
      }
    }
  }

  private boolean isCourseAlreadyProcessed(String courseid) {
    if (RedisCacheClient.getInstance().isHashExists(this.jedis, COURSE_CACHE_KEY, courseid)) {
      String courseCache =
          RedisCacheClient.getInstance().getHash(this.jedis, COURSE_CACHE_KEY, courseid);
      if (!courseCache.equals(null)) {
        return true;
      } else {
        return false;
      }
    }
    return false;
  }
}
