package com.fixledger.common.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  private static final String DATE_PATTERN = "yyyy-MM-dd";
  private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    return builder -> builder
        .serializerByType(LocalDate.class, new LocalDateSerializer(dateFormatter))
        .deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormatter))
        .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
        .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
  }
}
