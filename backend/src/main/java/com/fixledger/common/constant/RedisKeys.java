package com.fixledger.common.constant;

import java.time.LocalDate;

public final class RedisKeys {

  public static final String PREFIX = "fixledger:";

  private RedisKeys() {
  }

  public static String reminderDedupe(String type, Long bizId, LocalDate date) {
    return PREFIX + "reminder:dedupe:" + type + ":" + bizId + ":" + date;
  }

  public static String authBlacklist(String tokenId) {
    return PREFIX + "auth:blacklist:" + tokenId;
  }

  public static String dashboardSummary(Long familyId) {
    return PREFIX + "dashboard:summary:" + familyId;
  }
}
