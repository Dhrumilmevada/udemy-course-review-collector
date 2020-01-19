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

  private final static Logger LOGGER = LoggerFactory.getLogger(UdemyCourseListClient.class);

  private final String COURSE_DETAIL = AppConfig.CONFIG.getString("app.udemy.course.detail.url");
  private final String OAUTH_USER = AppConfig.CONFIG.getString("app.udemy.oauth.user");
  private final String OAUTH_PASS = AppConfig.CONFIG.getString("app.udemy.oauth.pass");

  private final String REGEX_NO_SPECIAL_CHAR = "[^a-zA-Z0-9]";
  private final String REGEX_NO_HTML_TAG = "\\<.*?\\>";
  private final String REGEX_NO_AMPERSAND = "&amp;";
  private final String REGEX_NO_SPACE = "\\s+";
  private final String REGEX_NO_NEW_LINE = "\\n";
  private String courseID = null;

  static int count = 1;

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
      response = resource.get(ClientResponse.class);
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

      if (resJson.isJsonNull() || resJson.size() == 0) {
        LOGGER.warn("Course data [{}] is either null or empty", resJson.toString());
        return null;
      }
      System.out.println("===================================== COURSE :" + (count++)
          + "========================================");
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

    course.setCourseId(Long.parseLong(JsonUtils.getNotNullString(courseData, Constant.ID)));
    course.setTitle(JsonUtils.getNotNullString(courseData, Constant.TITLE));
    course.setUrl(JsonUtils.getNotNullString(courseData, Constant.URL));
    course.setPaid(Boolean.parseBoolean(JsonUtils.getNotNullString(courseData, Constant.IS_PAID)));

    JsonArray instructorArray =
        JsonUtils.getNotNullJsonArray(courseData, Constant.VISIBLE_INSTRUCTORS);
    if (instructorArray.size() != 0) {
      course.setInstructors(getCourseInstructor(instructorArray));
    }

    course.setHeadline(JsonUtils.getNotNullString(courseData, Constant.HEADLINE));
    course.setSubscriberCount(
        Integer.parseInt(JsonUtils.getNotNullString(courseData, Constant.NUM_SUBCRIBERS)));

    if (course.isPaid()) {

      JsonObject priceDetail = JsonUtils.getNotNullJson(courseData, Constant.PRICE_DETAIL);

      if (priceDetail.size() != 0) {
        course.setAmount(JsonUtils.getNotNullInteger(priceDetail, Constant.AMOUNT));
        course.setCurrency(JsonUtils.getNotNullString(priceDetail, Constant.CURRENCY));
      }

      JsonObject discountJson = JsonUtils.getNotNullJson(courseData, Constant.DISCOUNT);

      if (discountJson.size() != 0) {
        JsonObject campaign = JsonUtils.getNotNullJson(discountJson, Constant.CAMPAIGN);
        if (campaign.size() != 0) {
          course.setDiscountStartTime(JsonUtils.getNotNullString(campaign, Constant.START_TIME));
          course.setDiscountEndTime(JsonUtils.getNotNullString(campaign, Constant.END_TIME));
        }

        course.setDiscountedPrice(JsonUtils.getNotNullInteger(
            (JsonUtils.getNotNullJson(discountJson, Constant.PRICE)), Constant.AMOUNT));
        course.setSavingAmount(JsonUtils.getNotNullInteger(
            (JsonUtils.getNotNullJson(discountJson, Constant.SAVING_PRICE)), Constant.AMOUNT));
        course.setSavingPercentages(
            Integer.parseInt(JsonUtils.getNotNullString(discountJson, Constant.DISCOUNT_PERCENT)));
        course.setDiscountAvailable(Boolean
            .parseBoolean(JsonUtils.getNotNullString(discountJson, Constant.HAS_DISCOUNT_SAVING)));
      }
    }

    course.setLectureCount(
        Integer.parseInt(JsonUtils.getNotNullString(courseData, Constant.NUM_PUBLISHED_LECTURES)));
    course.setQuizzesCount(
        Integer.parseInt(JsonUtils.getNotNullString(courseData, Constant.NUM_PUBLISHED_QUIZZES)));
    course.setPracticeTestCount(Integer
        .parseInt(JsonUtils.getNotNullString(courseData, Constant.NUM_PUBLISHED_PRACTICE_TESTS)));

    course.setCategory(JsonUtils.getNotNullString(
        JsonUtils.getNotNullJson(courseData, Constant.PRIMARY_CATEGORY), Constant.TITLE));
    course.setSubcategory(JsonUtils.getNotNullString(
        JsonUtils.getNotNullJson(courseData, Constant.PRIMARY_SUBCATEGORY), Constant.TITLE));
    course.setCreatedOn(JsonUtils.getNotNullString(courseData, Constant.CREATED));
    course.setPublishedOn(JsonUtils.getNotNullString(courseData, Constant.PUBLISHED_TIME));
    course.setContentLength(
        Double.parseDouble(JsonUtils.getNotNullString(courseData, Constant.CONTENT_LENGTH_VIDEO)));
    course.setContentLengthUnit("Second");

    course.setPrerequisites((List<String>) JsonUtils.jsonArrayToList(
        JsonUtils.getNotNullJsonArray(courseData, Constant.PREREQUISITES), List.class));
    course.setObjectives((List<String>) JsonUtils.jsonArrayToList(
        JsonUtils.getNotNullJsonArray(courseData, Constant.OBJECTIVES), List.class));
    course.setTargetAudiences((List<String>) JsonUtils.jsonArrayToList(
        JsonUtils.getNotNullJsonArray(courseData, Constant.TARGET_AUDIENCES), List.class));
    course.setUpdatedOn(JsonUtils.getNotNullString(courseData, Constant.LAST_UPDATE_DATE));
    course.setPreviewUrl(JsonUtils.getNotNullString(courseData, Constant.PREVIEW_URL));
    course.setPreviewAvailable(course.getPreviewUrl() != null);

    course.setDescription(
        getCourseDescription(JsonUtils.getNotNullString(courseData, Constant.DESCRIPTION)));

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
      instructor.setTitle(JsonUtils.getNotNullString(instructorJson, Constant.TITLE));
      instructor.setName(JsonUtils.getNotNullString(instructorJson, Constant.NAME));
      instructor.setDisplayName(JsonUtils.getNotNullString(instructorJson, Constant.DISPLAY_NAME));
      instructor.setJobTitle(JsonUtils.getNotNullString(instructorJson, Constant.JOB_TITLE));
      instructor.setUrl(JsonUtils.getNotNullString(instructorJson, Constant.URL));
      instructors.add(instructor);
      instructor = null;
    }
    return instructors;
  }
}
