package com.dhrumil.udemy.review.collector.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.review.runnable.UdemyCourseSearchThread;

public class ReviewCollector {

  public static void main(String[] args) {
    ArrayBlockingQueue<Review> review = new ArrayBlockingQueue<Review>(10000);
    List<String> search = Arrays.asList("kafka");

    search.stream().forEach(topic -> {
      UdemyCourseSearchThread courseSearchThread = new UdemyCourseSearchThread(topic, review);
      Thread newThread = new Thread(courseSearchThread);
      newThread.setName(topic);
      newThread.start();
    });

    Thread consumeReview = new Thread(new Runnable() {
      List<Review> consumed = new ArrayList<Review>();

      @Override
      public void run() {

        while (true) {
          if (review.size() > 2000) {
            review.drainTo(consumed, 2000);
            System.out.println(
                "#############################################   CONSUMED 2000    ###########################################");
            consumed.clear();
          }
        }
      }
    });

    consumeReview.setName("consumer");
    consumeReview.start();
  }

}
