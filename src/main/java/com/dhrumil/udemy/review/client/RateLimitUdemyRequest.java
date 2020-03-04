package com.dhrumil.udemy.review.client;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimitUdemyRequest {
  
  public static final RateLimiter rateLimitUdemyRestReq = RateLimiter.create(10);
}
