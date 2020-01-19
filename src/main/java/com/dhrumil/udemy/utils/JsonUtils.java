package com.dhrumil.udemy.utils;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

  private static JsonParser parser = new JsonParser();
  private static Gson gson = new Gson();

  public static JsonObject parseToJson(String dataStr) {
    if (dataStr == null) {
      LOGGER.warn("Provided data is not in proper form, not able to parse the data");
      return new JsonObject();
    }
    try {
      return parser.parse(dataStr).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      LOGGER.error(
          "Error while parsing string data [{}] to json data errorMessage : [{}], errorStackTrace : [{}], errorCause : [{}]",
          dataStr, e.getMessage(), e.getStackTrace(), e.getCause());
      return new JsonObject();
    }
  }

  public static <T> String parseObjectToString(T object) {
    return gson.toJson(object);
  }

  @SuppressWarnings("unchecked")
  public static <T> T stringToColletion(String data, Class<T> classOfT) {
    return (T) gson.fromJson(data, classOfT);
  }


  public static List<?> jsonToList(JsonObject data, Class<?> classOfT) {
    return (List<?>) gson.fromJson(data, classOfT);
  }

  public static List<?> jsonArrayToList(JsonArray data, Class<?> classOfT) {
    return (List<?>) gson.fromJson(data, classOfT);
  }

  public static String getNotNullString(JsonObject jsondata, String field) {
    String rtn = jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsString() : null)
        : null;

    if (rtn == null) {
      return new String();
    } else {
      return rtn;
    }
  }

  public static int getNotNullInteger(JsonObject jsondata, String field) {
    return jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsInt() : -1)
        : -1;
  }

  public static JsonObject getNotNullJson(JsonObject jsondata, String field) {
    JsonObject rtn = jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsJsonObject() : null)
        : null;

    if (rtn == null) {
      return new JsonObject();
    } else {
      return rtn;
    }
  }

  public static JsonArray getNotNullJsonArray(JsonObject jsondata, String field) {
    JsonArray rtn = jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsJsonArray() : null)
        : null;

    if (rtn == null) {
      return new JsonArray();
    } else {
      return rtn;
    }
  }


}
