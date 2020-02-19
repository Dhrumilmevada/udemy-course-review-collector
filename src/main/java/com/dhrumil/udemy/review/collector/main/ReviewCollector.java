package com.dhrumil.udemy.review.collector.main;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import com.dhrumil.udemy.review.runnable.UdemyCourseSearchThread;

public class ReviewCollector {

  public static void main(String[] args) {
    ExecutorService threadpool = Executors.newFixedThreadPool(3, new ThreadFactory() {
      
      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, r.toString());
      }
    });

    List<String> search = Arrays.asList("docker", "kafka", "python", "java", "javascript");
    search.stream().forEach(topic -> {
      UdemyCourseSearchThread courseSearchThread = new UdemyCourseSearchThread(topic);
      threadpool.execute(courseSearchThread);
//      Thread newThread = new Thread(courseSearchThread);
//      newThread.setName(topic);
//      newThread.start();
    });
    threadpool.shutdown();
	  
	  
//		final int COURSELIST_QUEUE_CAPACITY = AppConfig.CONFIG.getInt("app.udemy.courselist.queue.size");
//		final int COURSELIST_PAGESIZE = AppConfig.CONFIG.getInt("app.udemy.course.list.pagesize");
//		ArrayBlockingQueue<String> courseListQueue = new ArrayBlockingQueue<>(COURSELIST_QUEUE_CAPACITY);
//		List<String> search = Arrays.asList("python", "kafka");
//
//		search.stream().forEach(item -> {
//			UdemyCourseListThread courseListThread = new UdemyCourseListThread(item, COURSELIST_PAGESIZE,
//					courseListQueue);
//			Thread courseList = new Thread(courseListThread);
//			courseList.setName(item + "-course-list");
//			courseList.start();
//		});
  }

}
