package com.dhrumil.udemy.review.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Constant;
import com.dhrumil.udemy.model.Course;
import com.dhrumil.udemy.model.Instructor;
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

public class UdemyCourseDetailClient {

  private final static Logger LOGGER = LoggerFactory.getLogger(UdemyCourseDetailClient.class);

  private final String COURSE_DETAIL = AppConfig.CONFIG.getString("app.udemy.course.detail.url");
  private final String OAUTH_USER = AppConfig.CONFIG.getString("app.udemy.oauth.user");
  private final String OAUTH_PASS = AppConfig.CONFIG.getString("app.udemy.oauth.pass");

  private final String REGEX_NO_SPECIAL_CHAR = "[^a-zA-Z0-9]";
  private final String REGEX_NO_HTML_TAG = "\\<.*?\\>";
  private final String REGEX_NO_AMPERSAND = "&amp;";
  private final String REGEX_NO_SPACE = "\\s+";
  private final String REGEX_NO_NEW_LINE = "\\n";
  private String courseID = null;

  public UdemyCourseDetailClient(String courseID) {
    super();
    this.courseID = courseID;
  }

  public Course getCourseDetail() {
    String URL = String.format(COURSE_DETAIL, this.courseID);

    Client restClient = Client.create();

    WebResource resource = restClient.resource(URL).queryParam("fields[course]", "@all");
    resource.addFilter(new HTTPBasicAuthFilter(OAUTH_USER, OAUTH_PASS));

    ClientResponse response = null;

    try {
      LOGGER.info("Sending request to get course detail to udemy rest URL : [{}]", COURSE_DETAIL);

      boolean gotResponse = true;
      int failedCount = 1;
      while (gotResponse) {
        if (RateLimitUdemyRequest.rateLimitUdemyRestReq.acquire() > 0L) {
          response = resource.get(ClientResponse.class);
          if(response.getStatus() == 429) {
            try {
              Thread.sleep(500 * failedCount);
            } catch (InterruptedException e) {
              LOGGER.error(
                  "Got InterruptedException errorCause: [{}] errorMessage: [{}] errorStackTrace: [{}]",
                  e.getCause(), e.getMessage(), e.getStackTrace());
            }
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
          "Exception while calling udemy rest-api to get course Detail for courseID :[{}] errorMessage:[{}], errorStackTrace:[{}], errorCause:[{}]",
          this.courseID, e.getMessage(), e.getStackTrace(), e.getCause());
    } finally {
      restClient.destroy();
    }

    if (response.getStatus() == 200) {
      String resStr = response.getEntity(String.class);
      JsonObject resJson = JsonUtils.parseToJson(resStr);

      if (resJson.isJsonNull() || resJson.size() == 0 || resJson == null) {
        LOGGER.warn("Course data [{}] is either null or empty", resJson.toString());
        return null;
      }
      LOGGER.info("Found course Detail for course id : [{}] with response status code : [{}]",
          this.courseID, response.getStatus());
      Course coursedetail = getCourseInfo(resJson);
      return coursedetail;
    } else {
      LOGGER.error(
          "Fail to get course information for course id [{}] from udemy rest url [{}] with response status code [{}]",
          this.courseID, COURSE_DETAIL, response.getStatus());
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private Course getCourseInfo(JsonObject courseData) {
    Course course = new Course();

    course.setCourseId(JsonUtils.getLong(courseData, Constant.ID));
    course.setTitle(JsonUtils.getString(courseData, Constant.TITLE));
    course.setUrl(JsonUtils.getString(courseData, Constant.URL));
    course.setPaid(JsonUtils.getBoolean(courseData, Constant.IS_PAID));

    JsonArray instructorArray =
        JsonUtils.getJsonArray(courseData, Constant.VISIBLE_INSTRUCTORS);
    if (instructorArray != null) {
      course.setInstructors(getCourseInstructor(instructorArray));
    }

    course.setHeadline(JsonUtils.getString(courseData, Constant.HEADLINE));
    course.setSubscriberCount(
        Integer.parseInt(JsonUtils.getString(courseData, Constant.NUM_SUBCRIBERS)));

    if (course.isPaid()) {

      JsonObject priceDetail = JsonUtils.getJson(courseData, Constant.PRICE_DETAIL);

      if (priceDetail != null) {
        course.setAmount(JsonUtils.getInteger(priceDetail, Constant.AMOUNT));
        course.setCurrency(JsonUtils.getString(priceDetail, Constant.CURRENCY));
      }

      JsonObject discountJson = JsonUtils.getJson(courseData, Constant.DISCOUNT);

      if (discountJson != null) {
        JsonObject campaign = JsonUtils.getJson(discountJson, Constant.CAMPAIGN);
        if (campaign != null) {
          course.setDiscountStartTime(JsonUtils.getString(campaign, Constant.START_TIME));
          course.setDiscountEndTime(JsonUtils.getString(campaign, Constant.END_TIME));
        }

        course.setDiscountedPrice(JsonUtils
            .getInteger((JsonUtils.getJson(discountJson, Constant.PRICE)), Constant.AMOUNT));
        course.setSavingAmount(JsonUtils
            .getInteger((JsonUtils.getJson(discountJson, Constant.SAVING_PRICE)), Constant.AMOUNT));
        course.setSavingPercentages(
            Integer.parseInt(JsonUtils.getString(discountJson, Constant.DISCOUNT_PERCENT)));
        course.setDiscountAvailable(Boolean
            .parseBoolean(JsonUtils.getString(discountJson, Constant.HAS_DISCOUNT_SAVING)));
      }
    }

    course.setLectureCount(JsonUtils.getInteger(courseData, Constant.NUM_PUBLISHED_LECTURES));
    course.setQuizzesCount(JsonUtils.getInteger(courseData, Constant.NUM_PUBLISHED_QUIZZES));
    course.setPracticeTestCount(
        JsonUtils.getInteger(courseData, Constant.NUM_PUBLISHED_PRACTICE_TESTS));

    course.setCategory(JsonUtils.getString(JsonUtils.getJson(courseData, Constant.PRIMARY_CATEGORY),
        Constant.TITLE));
    course.setSubcategory(JsonUtils
        .getString(JsonUtils.getJson(courseData, Constant.PRIMARY_SUBCATEGORY), Constant.TITLE));
    course.setCreatedOn(JsonUtils.getString(courseData, Constant.CREATED));
    course.setPublishedOn(JsonUtils.getString(courseData, Constant.PUBLISHED_TIME));
    course.setContentLength(JsonUtils.getDouble(courseData, Constant.CONTENT_LENGTH_VIDEO));
    course.setContentLengthUnit("Second");

    course.setPrerequisites((List<String>) JsonUtils.jsonArrayToList(
        JsonUtils.getJsonArray(courseData, Constant.PREREQUISITES), List.class));
    course.setObjectives((List<String>) JsonUtils.jsonArrayToList(
        JsonUtils.getJsonArray(courseData, Constant.OBJECTIVES), List.class));
    course.setTargetAudiences((List<String>) JsonUtils.jsonArrayToList(
        JsonUtils.getJsonArray(courseData, Constant.TARGET_AUDIENCES), List.class));
    course.setUpdatedOn(JsonUtils.getString(courseData, Constant.LAST_UPDATE_DATE));
    course.setPreviewUrl(JsonUtils.getString(courseData, Constant.PREVIEW_URL));
    course.setPreviewAvailable(course.getPreviewUrl() != null);

    course.setDescription(
        getCourseDescription(JsonUtils.getString(courseData, Constant.DESCRIPTION)));

    StringBuilder metadataStr = new StringBuilder().append(course.getTitle()).append(" ")
        .append(course.getHeadline()).append(" ").append(course.getCategory()).append(" ")
        .append(course.getSubcategory()).append(" ").append(course.getDescription());
    course.setMetadata(getCourseMetadata(metadataStr.toString()));

    return course;
  }

  private String getCourseDescription(String description) {
    return description.replaceAll(REGEX_NO_HTML_TAG, " ").replaceAll(REGEX_NO_AMPERSAND, " ")
        .replaceAll(REGEX_NO_NEW_LINE, " ").trim();
  }

  private List<String> getCourseMetadata(String metadata) {
    return Arrays
        .asList(metadata.replaceAll(REGEX_NO_SPECIAL_CHAR, " ").trim().split(REGEX_NO_SPACE));
  }

  private List<Instructor> getCourseInstructor(JsonArray instructorArray) {
    List<Instructor> instructors = new ArrayList<Instructor>();

    for (JsonElement instructorElement : instructorArray) {
      JsonObject instructorJson = instructorElement.getAsJsonObject();

      Instructor instructor = new Instructor();
      instructor.setTitle(JsonUtils.getString(instructorJson, Constant.TITLE));
      instructor.setName(JsonUtils.getString(instructorJson, Constant.NAME));
      instructor.setDisplayName(JsonUtils.getString(instructorJson, Constant.DISPLAY_NAME));
      instructor.setJobTitle(JsonUtils.getString(instructorJson, Constant.JOB_TITLE));
      instructor.setUrl(JsonUtils.getString(instructorJson, Constant.URL));
      instructors.add(instructor);
      instructor = null;
    }
    return instructors;
  }
}
