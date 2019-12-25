package club.gclmit.easydb.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (C), 2016-2018, 孤城落寞的博客
 *
 * 单例模式 获取 Hikari连接池
 * @date: 2019-03-26 15:27
 */
public class ChaosDataSource {

     private static Logger logger = LoggerFactory.getLogger(ChaosDataSource.class);

     private static String path = ChaosDataSource.class.getClassLoader().getResource("db.properties").getPath();;

     private static class ChaosDataSourceHandler {
         static HikariDataSource instatce = new HikariDataSource(new HikariConfig(path));
     }

     public static HikariDataSource getInstance() {
         return ChaosDataSourceHandler.instatce;
     }
}
