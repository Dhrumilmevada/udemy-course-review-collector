package com.dhrumil.udemy.redis.client;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class RedisCacheClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheClient.class);

  private static RedisCacheClient instance;
  private JedisPool jedisPool;

  private RedisCacheClient() {
    super();
    this.jedisPool = new JedisPool(this.buildJedispoolconfig(), "localhost");
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
}
