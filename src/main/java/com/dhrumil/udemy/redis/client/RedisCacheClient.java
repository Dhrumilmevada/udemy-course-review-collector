package com.dhrumil.udemy.redis.client;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dhrumil.udemy.review.collector.main.AppConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class RedisCacheClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheClient.class);
  private static final String REDIS_COURSE_KEY =
      AppConfig.CONFIG.getString("app.redis.course.cache.key");
  private static final int COURSE_CACHE_EXPIRY =
      AppConfig.CONFIG.getInt("app.redis.course.cache.expiry.time");
  private static final long REDIS_COURSE_JOB_INITIAL_DELAY =
      AppConfig.CONFIG.getLong("app.redis.course.cache.scheduleJob.initial.delay");
  private static final long REDIS_COURSE_JOB_INTERVAL =
      AppConfig.CONFIG.getLong("app.redis.course.cache.scheduleJob.interval");
  private static final String REDIS_HOST = AppConfig.CONFIG.getString("app.redis.host.name");
  private static final int REDIS_PORT = AppConfig.CONFIG.getInt("app.redis.host.port");

  private static RedisCacheClient instance;
  private JedisPool jedisPool;
  private ScheduledExecutorService scheduledExecutorService = null;
  private ScheduledFuture<?> scheduledFuture = null;

  private RedisCacheClient() {
    super();
    this.jedisPool = new JedisPool(this.buildJedispoolconfig(), REDIS_HOST, REDIS_PORT);
    startExpiryTimeCheckingJob();
  }

  public static RedisCacheClient getInstance() {

    if (instance == null) {
      synchronized (RedisCacheClient.class) {
        if (instance == null) {
          instance = new RedisCacheClient();
        }
      }
    }
    return instance;
  }

  private JedisPoolConfig buildJedispoolconfig() {

    JedisPoolConfig poolconfig = new JedisPoolConfig();

    poolconfig.setMaxTotal(128);
    poolconfig.setMaxIdle(128);
    poolconfig.setMinIdle(16);
    poolconfig.setTestOnBorrow(true);
    poolconfig.setTestOnReturn(true);
    poolconfig.setTestWhileIdle(true);
    poolconfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
    poolconfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
    poolconfig.setNumTestsPerEvictionRun(3);
    poolconfig.setBlockWhenExhausted(true);
    return poolconfig;
  }

  public Jedis getJedisThread() {
    return this.jedisPool.getResource();
  }

  public <K> boolean isHashExists(Jedis jedis, String keyToWrite, K key) {
    return jedis.hexists(keyToWrite, key.toString());
  }

  public <K> Long delKey(Jedis jedis, String key) {
    return jedis.del(key);
  }

  public <K, V> Long setHash(Jedis jedis, String keyToWrite, K key, V value) {
    return jedis.hset(keyToWrite, key.toString(), value.toString());
  }

  public <K> String getHash(Jedis jedis, String keyToWrite, K key) {
    return jedis.hget(keyToWrite, key.toString());

  }

  public Long setExpiryTime(Jedis jedis, String keyToWrite, int expiryTime) {
    return jedis.expire(keyToWrite, expiryTime);
  }

  public Long getTimeToLive(Jedis jedis, String keyToCheck) {
    return jedis.ttl(keyToCheck);
  }

  private void doCheckExpiryTime(Jedis jedis) {
    Set<String> keys = jedis.keys("*");

    keys.stream().forEach(new Consumer<String>() {

      @Override
      public void accept(String t) {
        if (t.equalsIgnoreCase(REDIS_COURSE_KEY) && jedis.ttl(t) == -1) {
          setExpiryTime(jedis, t, COURSE_CACHE_EXPIRY);
        }

      }
    });
  }

  private void startExpiryTimeCheckingJob() {
    scheduledExecutorService = Executors.newScheduledThreadPool(1);
    Runnable r = new Runnable() {

      @Override
      public void run() {
        doCheckExpiryTime(getJedisThread());

      }
    };

    this.scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(r,
        REDIS_COURSE_JOB_INITIAL_DELAY, REDIS_COURSE_JOB_INTERVAL, TimeUnit.MINUTES);
  }

  public void close() {
    if (!this.scheduledExecutorService.isShutdown()) {
      this.scheduledExecutorService.shutdown();
    }
    this.jedisPool.close();
  }

}
