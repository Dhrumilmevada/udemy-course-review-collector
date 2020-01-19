package com.dhrumil.udemy.mongodb.client;

import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.model.Course;
import com.dhrumil.udemy.review.collector.main.AppConfig;
import com.dhrumil.udemy.utils.JsonUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoDBClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBClient.class);

  private static final String DATABASE_NAME =
      AppConfig.CONFIG.getString("app.mongodb.database.name");
  private static final String COURSE_COLLECTION =
      AppConfig.CONFIG.getString("app.mongodb.course.collection");
  private static final String REVIEW_COLLECTION =
      AppConfig.CONFIG.getString("app.mongodb.review.collection");

  private MongoClient dbClient;
  private MongoDatabase db;
  private static MongoDBClient instance;

  private MongoDBClient() {
    super();
    this.dbClient = new MongoClient();
    this.db = dbClient.getDatabase(DATABASE_NAME);
    LOGGER.info("Created MongoDB client and initialized database with name:[{}]", DATABASE_NAME);
  }

  public static MongoDBClient getInstance() {

    if (instance == null) {
      synchronized (MongoDBClient.class) {
        if (instance == null) {
          instance = new MongoDBClient();
        }
      }
    }
    return instance;
  }

  public <T> void insert(T document) {
    if (document instanceof Course) {
      MongoCollection<Document> collection = getOrCreateCollection(COURSE_COLLECTION);
      insertDocument(collection, document);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void insertDocument(MongoCollection<Document> collection, T document) {
    String collectionName = collection.getNamespace().getCollectionName();

    if (collectionName.equalsIgnoreCase(COURSE_COLLECTION)) {
      Course course = (Course) document;
      boolean documentExists = documentExists(collection, String.valueOf(course.getCourseId()));

      if (!documentExists) {
        Map<String, Object> courseMap =
            JsonUtils.stringToColletion(JsonUtils.parseObjectToString(course), Map.class);
        courseMap.put("_id", String.valueOf(course.getCourseId()));
        LOGGER.info("Inserting data with _id [{}] in [{}] collection", course.getCourseId(),
            collectionName);
        Document insertDocument = new Document(courseMap);
        collection.insertOne(insertDocument);
      } else {
        LOGGER.info("Data with id [{}] already exists in [{}] collection", course.getCourseId(),
            collectionName);
      }
    }
  }

  private boolean documentExists(MongoCollection<Document> collection, String key) {

    BasicDBObject query = new BasicDBObject();
    query.put("_id", key);

    Document document = collection.find(query).first();

    if (document == null) {
      LOGGER.info("Document with _id:[{}] is not exist in collection [{}]", key,
          collection.getNamespace().getCollectionName());
      return false;
    } else {
      return true;
    }
  }

  private MongoCollection<Document> getOrCreateCollection(String collectionName) {
    boolean isExists = this.collectionExists(collectionName);

    if (!isExists) {
      db.createCollection(collectionName);
      LOGGER.info("Created [{}] collection in [{}] database", COURSE_COLLECTION, DATABASE_NAME);
    }
    return db.getCollection(collectionName);

  }

  private boolean collectionExists(String collectionName) {
    MongoCursor<String> collectionCursor = this.db.listCollectionNames().iterator();
    LOGGER.info("Checking if collection with name [{}] is exists or not", collectionName);

    while (collectionCursor.hasNext()) {
      String collection = collectionCursor.next();
      if (collection.equals(collectionName)) {
        LOGGER.info("Collection [{}] is exists in [{}] in database", collectionName, DATABASE_NAME);
        return true;
      }
    }
    LOGGER.info(
        "Collection [{}] does not exists in [{}] in database , need to create [{}] collection",
        collectionName, DATABASE_NAME, collectionName);
    return false;
  }
}
