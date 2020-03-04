package com.dhrumil.udemy.review.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Constant;
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

public class UdemyCourseListClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdemyCourseListClient.class);

  private static final String COURSE_LIST_URL =
      AppConfig.CONFIG.getString("app.udemy.course.list.url");
  private static final String OAUTH_USER = AppConfig.CONFIG.getString("app.udemy.oauth.user");
  private static final String OAUTH_PASS = AppConfig.CONFIG.getString("app.udemy.oauth.pass");

  private String searchTopic;
  private int pageSize;
  private int totalRecord;
  private int totalPage;
  private int currentPage;

  public UdemyCourseListClient(String searchTopic, int pageSize) {
    super();
    this.searchTopic = searchTopic;
    this.pageSize = pageSize;
    this.totalRecord = 0;
    this.currentPage = 1;
  }

  private void init() throws InterruptedException {
    this.totalRecord = JsonUtils.getInteger(this.getCourseResponse(1, 1), Constant.COUNT);
    if (totalRecord % this.pageSize == 0) {
      this.totalPage = totalRecord / pageSize;
    } else {
      this.totalPage = (totalRecord / pageSize) + 1;
    }
    LOGGER.info("Need to fetch course of [{}] pages with page size of [{}] for search [{}]",
        this.totalPage, this.pageSize, this.searchTopic);
  }

  public List<String> getNextCourses() throws InterruptedException {
    if (this.totalRecord == 0) {
      init();
    }

    if (this.currentPage <= this.totalPage) {
      JsonObject courseRes = getCourseResponse(this.currentPage, this.pageSize);
      JsonArray courseListJson = JsonUtils.getJsonArray(courseRes, Constant.RESULTS);
      List<String> courseList = getCourseIDListFromResponse(courseListJson);

      this.currentPage++;

      return courseList;
    }
    return Collections.emptyList();
  }

  private JsonObject getCourseResponse(int page, int pagesize) throws InterruptedException {

    Client restClient = Client.create();

    WebResource resource = restClient.resource(COURSE_LIST_URL)
        .queryParam("search", this.searchTopic).queryParam("page", Integer.toString(page))
        .queryParam("page_size", Integer.toString(pagesize));
    resource.addFilter(new HTTPBasicAuthFilter(OAUTH_USER, OAUTH_PASS));

    ClientResponse response = null;
    try {
      LOGGER.info("Sending request to get list of courses to udemy rest URL : [{}]",
          COURSE_LIST_URL);

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
          "Exception while calling udemy rest-api to get course List for search :[{}] on page : [{}] errorMessage:[{}], errorStackTrace:[{}], errorCause:[{}]",
          searchTopic, page, e.getMessage(), e.getStackTrace(), e.getCause());
    } finally {
      restClient.destroy();
    }

    if (response.getStatus() == 200) {
      String resStr = response.getEntity(String.class);
      JsonObject resJson = JsonUtils.parseToJson(resStr);
      JsonArray courseListJson = JsonUtils.getJsonArray(resJson, Constant.RESULTS);

      if (resJson.isJsonNull() || resJson == null) {
        LOGGER.warn("Udemy api response [{}] is null", resJson.toString());
        return null;
      } else if (courseListJson.isJsonNull() || courseListJson.size() == 0
          || courseListJson == null) {
        LOGGER.warn("Udemy api response [{}] is not in valid form", resJson.toString());
        return null;
      }

      LOGGER.info("Got list of [{}] courses from udemy rest api with response status :[{}]",
          courseListJson.size(), response.getStatus());
      return resJson;
    } else {
      LOGGER.error(
          "Fail to get course list from udemy rest url [{}] with response status code [{}]",
          COURSE_LIST_URL, response.getStatus());
      return null;
    }
  }

  private List<String> getCourseIDListFromResponse(JsonArray courseListJson) {

    if (courseListJson == null) {
      return null;
    }
    List<String> courseList = new ArrayList<String>();
    for (JsonElement course : courseListJson) {
      courseList.add(JsonUtils.getString(course.getAsJsonObject(), Constant.ID));
    }

    return courseList;
  }
}
