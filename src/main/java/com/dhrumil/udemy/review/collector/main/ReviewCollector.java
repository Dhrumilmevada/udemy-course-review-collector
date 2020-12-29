package com.dhrumil.udemy.review.collector.main;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.dhrumil.udemy.mongodb.client.MongoDBClient;
import com.dhrumil.udemy.redis.client.RedisCacheClient;
import com.dhrumil.udemy.review.runnable.UdemyCourseSearchThread;

public class ReviewCollector {

  private ExecutorService threadpool = null;
  private ScheduledExecutorService scheduledExecutorService = null;
  private ScheduledFuture<?> scheduledFuture = null;
  private static final long SCHEDULE_JOB_INITIAL_DELAY =
      AppConfig.CONFIG.getLong("app.schedulejob.initial.delay");
  private static final long SCHEDULE_JOB_INTERVAL =
      AppConfig.CONFIG.getLong("app.schedulejob.interval");


  public static void main(String[] args) {

    ReviewCollector collector = new ReviewCollector();
    collector.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    collector.threadpool = Executors.newFixedThreadPool(3, new ThreadFactory() {

      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, r.toString());
      }
    });


    collector.startScheduleTask();

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        collector.threadpool.shutdown();
        collector.scheduledExecutorService.shutdown();
        RedisCacheClient.getInstance().close();
        MongoDBClient.getInstance().close();
      }

    });

  }

  private void doCollectCourses() {
    String topicStr = AppConfig.CONFIG.getString("app.search.topic");
    String[] topicArray = topicStr.trim().split(",");

    List<String> search = Arrays.stream(topicArray).map(new Function<String, String>() {

      @Override
      public String apply(String t) {
        return t.trim();
      }
    }).collect(Collectors.toList());

    search.stream().forEach(topic -> {
      UdemyCourseSearchThread courseSearchThread = new UdemyCourseSearchThread(topic);
      threadpool.execute(courseSearchThread);
    });
  }

  private void startScheduleTask() {
    Runnable r = new Runnable() {

      @Override
      public void run() {
        doCollectCourses();

      }
    };

    this.scheduledFuture = this.scheduledExecutorService.scheduleWithFixedDelay(r,
        SCHEDULE_JOB_INITIAL_DELAY, SCHEDULE_JOB_INTERVAL, TimeUnit.MINUTES);
  }

}
