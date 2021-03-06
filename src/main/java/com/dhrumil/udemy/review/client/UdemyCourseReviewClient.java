package com.dhrumil.udemy.review.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Constant;
import com.dhrumil.udemy.model.Review;
import com.dhrumil.udemy.model.User;
import com.dhrumil.udemy.review.collector.main.AppConfig;
import com.dhrumil.udemy.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class UdemyCourseReviewClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdemyCourseReviewClient.class);

  private final String OAUTH_USER = AppConfig.CONFIG.getString("app.udemy.oauth.user");
  private final String OAUTH_PASS = AppConfig.CONFIG.getString("app.udemy.oauth.pass");
  private final String COURSE_REVIEW_URL =
      AppConfig.CONFIG.getString("app.udemy.course.review.url");

  private String courseId;
  private int pageSize;
  private int numOfPage;
  private int currentPage;

  public UdemyCourseReviewClient(String courseId, int pageSize) {
    this.courseId = courseId;
    this.pageSize = pageSize;
    this.numOfPage = 0;
    this.currentPage = 0;
  }

  private void init() throws InterruptedException {
    JsonObject reviewResponse = this.getCourseReviewRes(1, 1);
    if (reviewResponse != null) {
      this.numOfPage = JsonUtils.getInteger(reviewResponse, Constant.COUNT);
    } else {
      this.numOfPage = 0;
    }

    if (numOfPage >= this.pageSize) {
      if (this.numOfPage % this.pageSize == 0) {
        this.currentPage = this.numOfPage / this.pageSize;
      } else {
        this.currentPage = (this.numOfPage / this.pageSize) + 1;
      }
    } else if (this.numOfPage < this.pageSize && this.numOfPage != 0) {
      this.currentPage = 1;
    } else {
      this.currentPage = 0;
    }
    LOGGER.info("Need to fetch review of [{}] pages with page size of [{}] for course id [{}]",
        this.currentPage, this.pageSize, this.courseId);

  }

  public List<Review> getNextReview() throws InterruptedException {

    if (numOfPage == 0) {
      init();
    }
    if (this.currentPage >= 1) {
      JsonObject reviewJson = getCourseReviewRes(this.currentPage, this.pageSize);
      JsonArray reviewList = JsonUtils.getJsonArray(reviewJson, Constant.RESULTS);
      List<Review> reviews = getReviewList(reviewList);
      this.currentPage--;
      return reviews;
    } else {
      return Collections.emptyList();
    }
  }

  private List<Review> getReviewList(JsonArray reviews) {
    if (reviews == null) {
      return null;
    }

    List<Review> reviewList = new ArrayList<Review>();

    for (JsonElement reviewEle : reviews) {
      JsonObject reviewJson = reviewEle.getAsJsonObject();
      Review review = new Review();
      review.setId(Long.parseLong(JsonUtils.getString(reviewJson, Constant.ID)));
      review.setContent(JsonUtils.getString(reviewJson, Constant.CONTENT));
      review.setRating(Double.parseDouble(JsonUtils.getString(reviewJson, Constant.RATING)));
      review.setCreated(JsonUtils.getString(reviewJson, Constant.CREATED));
      review.setModified(JsonUtils.getString(reviewJson, Constant.MODIFIED));
      review.setUserModified(JsonUtils.getString(reviewJson, Constant.USER_MODIFIED));
      review.setUser(getUserInfo(JsonUtils.getJson(reviewJson, Constant.USER)));
      review.setCourseId(Long.parseLong(courseId));

      reviewList.add(review);
      review = null;
    }
    return reviewList;
  }

  private User getUserInfo(JsonObject userInfo) {
    if (userInfo == null) {
      return null;
    }

    if (userInfo.size() != 0) {
      User user = new User();
      user.setTitle(JsonUtils.getString(userInfo, Constant.TITLE));
      user.setName(JsonUtils.getString(userInfo, Constant.NAME));
      user.setDisplayName(JsonUtils.getString(userInfo, Constant.DISPLAY_NAME));
      return user;
    }
    return null;
  }

  private JsonObject getCourseReviewRes(int page, int pagesize) throws InterruptedException {

    String url = String.format(COURSE_REVIEW_URL, this.courseId);

    Client restClient = Client.create();
    WebResource resource = restClient.resource(url).queryParam("page", String.valueOf(page))
        .queryParam("page_size", String.valueOf(pagesize));
    resource.addFilter(new HTTPBasicAuthFilter(OAUTH_USER, OAUTH_PASS));

    ClientResponse response = null;

    try {
      LOGGER.info("Sending request to get course review to udemy rest URL : [{}]", url);

      boolean gotResponse = true;
      int failedCount = 1;
      while (gotResponse) {
        if (RateLimitUdemyRequest.rateLimitUdemyRestReq.acquire() >= 0L) {
          response = resource.get(ClientResponse.class);
          if (response.getStatus() == 429) {
            Thread.sleep(500 * failedCount);
            gotResponse = true;
            failedCount++;
          } else {
            gotResponse = false;
            failedCount = 1;
          }
        }
      }
    } catch (UniformInterfaceException | ClientHandlerException e) {
      LOGGER.error(
          "Exception while calling udemy rest-api to get course review for course [{}] on page : [{}] errorMessage:[{}], errorStackTrace:[{}], errorCause:[{}]",
          this.courseId, page, e.getMessage(), e.getStackTrace(), e.getCause());
    } finally {
      restClient.destroy();
    }

    if (response.getStatus() == 200) {
      String responseStr = response.getEntity(String.class);
      JsonObject responseJson = JsonUtils.parseToJson(responseStr);
      JsonArray reviewList = JsonUtils.getJsonArray(responseJson, Constant.RESULTS);

      if (responseJson.isJsonNull() || responseJson == null) {
        LOGGER.warn("Udemy review api [{}]'s response [{}] is null for page : [{}]",
            this.COURSE_REVIEW_URL, responseJson.toString(), page);
        return null;
      } else if (reviewList.isJsonNull() || reviewList.size() == 0 || reviewList == null) {
        LOGGER.warn("Udemy review api [{}]'s response [{}] is not in valid form for page : [{}]",
            this.COURSE_REVIEW_URL, reviewList.toString(), page);
        return null;
      }

      LOGGER.info(
          "Got course review [{}] for course id [{}] from udemy rest api with response status :[{}]",
          reviewList.size(), this.courseId, response.getStatus());
      return responseJson;
    } else {
      LOGGER.error(
          "Fail to get course review for course id [{}] from udemy rest url [{}] with response status code [{}]",
          this.courseId, COURSE_REVIEW_URL, response.getStatus());
      return null;
    }
  }

}
