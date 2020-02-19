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
      return null;
    }
    try {
      return parser.parse(dataStr).getAsJsonObject();
    } catch (JsonSyntaxException e) {
      LOGGER.error(
          "Error while parsing string data [{}] to json data errorMessage : [{}], errorStackTrace : [{}], errorCause : [{}]",
          dataStr, e.getMessage(), e.getStackTrace(), e.getCause());
    }
    return null;
  }

  public static <T> String parseObjectToString(T object) {
    if (object == null) {
      return null;
    }
    return gson.toJson(object);
  }

  public static <T> T stringToColletion(String data, Class<T> classOfT) {
    if (data == null) {
      return null;
    }
    return (T) gson.fromJson(data, classOfT);
  }


  public static List<?> jsonToList(JsonObject data, Class<?> classOfT) {
    if (data == null) {
      return null;
    }
    return (List<?>) gson.fromJson(data, classOfT);
  }

  public static List<?> jsonArrayToList(JsonArray data, Class<?> classOfT) {
    if (data == null) {
      return null;
    }
    return (List<?>) gson.fromJson(data, classOfT);
  }

  public static String getString(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return null;
    }

    String rtn = jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsString() : null)
        : null;

    return rtn;
  }

  public static int getInteger(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return -1;
    }
    return jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsInt() : -1)
        : -1;
  }

  public static long getLong(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return -1L;
    }
    return jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsLong() : -1L)
        : -1L;
  }

  public static boolean getBoolean(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return false;
    }
    return jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsBoolean() : false)
        : false;
  }

  public static double getDouble(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return -1;
    }
    return jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsDouble() : -1)
        : -1;
  }

  public static JsonObject getJson(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return null;
    }
    JsonObject rtn = jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsJsonObject() : null)
        : null;

    return rtn;
  }

  public static JsonArray getJsonArray(JsonObject jsondata, String field) {
    if (jsondata == null || jsondata.size() == 0) {
      return null;
    }
    JsonArray rtn = jsondata.has(field)
        ? (!jsondata.get(field).isJsonNull() ? jsondata.get(field).getAsJsonArray() : null)
        : null;

    return rtn;
  }


}
