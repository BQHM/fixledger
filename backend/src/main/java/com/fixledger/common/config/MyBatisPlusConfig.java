package com.fixledger.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 文件功能说明：基础配置类，集中声明 Spring 容器中的基础 Bean。
 * </p>
 *
 * @Author FixLedger
 */
@Configuration
@MapperScan(basePackages = "com.fixledger.modules", annotationClass = Mapper.class)
public class MyBatisPlusConfig {
  /**
   * @Author FixLedger
   * <p>
   * 功能说明：完成声明 MyBatis Plus 插件配置。
   * </p>
   * @return 处理结果
   */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
  }
}
