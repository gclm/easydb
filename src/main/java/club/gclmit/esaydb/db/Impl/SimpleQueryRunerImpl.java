package club.gclmit.esaydb.db.Impl;


import club.gclmit.esaydb.db.SimpleQueryRuner;
import club.gclmit.esaydb.util.ObjectUtils;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2016-2018, 孤城落寞的博客
 *
 * @program: club.gclmit.dao.Impl
 * @author: gclm
 * @date: 2019-03-27 12:47
 * @description: QueryRun 的简单封装，解决常见数据查询操作
 */
public class SimpleQueryRunerImpl<T> extends ChaosQueryRunerImpl implements SimpleQueryRuner<T> {

    private Logger logger = null;
    private Class<T> baseClass;
    private String columnName = null;

    /**
     * SELECT * FROM imooc_user WHERE nickname = ?
     * SELECT * FROM imooc_user WHERE id = ?
     * SELECT * FROM imooc_user  WHERE nickname LIKE '%XXX%'
     * SELECT * FROM imooc_user
     * INSERT INTO users(username,password,gender,phonenumber,email)VALUES(?,?,?,?,?)
     * UPDATE users SET username=?,password=?,realname=?,card=?,nickname=?,position=?,country=?,gender=?,school=?,admission=?,education=?,email=?,phonenumber=?,address=?,autograph=?,head=? WHERE id= ?;
     * DELETE FROM imooc_user WHERE id = ?
     * explain select * from user
     */
    public static String[] sqls = {
            "INSERT INTO %s  (%s) VALUES (%s)",
            "SELECT * FROM %s ",
            "SELECT * FROM %s WHERE %s",
            "UPDATE %s SET %s WHERE id = '%s'",
            "DELETE FROM %s WHERE id = %s",
            "SELECT COUNT(1) FROM %s"
    };

    /**
     * 构造方法
     * 通过Java的发射机制获取子类传过来的实体类类型
     *
     * @details 孤城落寞 2019-03-27 20:59
     * @param
     * @return
     */
    public SimpleQueryRunerImpl() {

        /**
         * 通过类加载机制 获取当前泛型T的class
         */
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.baseClass = (Class<T>) type.getActualTypeArguments()[0];

        /**
         * 日志配置
         */
        logger = LoggerFactory.getLogger(getClass());

        /**
         * 获取当前数据库表名
         */
        columnName = baseClass.getSimpleName().toLowerCase();
    }

    /**
     *  统计表中的数据条数
     * @author gclm
     * @date 2019/12/24 2:04 下午
     * @return: java.lang.Long
     * @throws
     */
    @Override
    public Long count(){
        String sql = String.format(sqls[5], columnName);
        Long start = Clock.systemDefaultZone().millis();
        Long count = query(sql, new ScalarHandler<Long>());
        logger.info("执行SQL：{}\n执行时间：{}", sql, Clock.systemDefaultZone().millis() - start);
        return count;
    }

    /**
     * 查询所有用户
     * @author gclm
     * @date 2019/12/24 1:24 下午
     * @return: java.util.List<T>
     * @throws
     */
    @Override
    public List<T> selectList(){
        String sql = String.format(sqls[1],columnName);
        List<T> list = new ArrayList<T>();
        Long start = Clock.systemDefaultZone().millis();
        list = query(sql, new BeanListHandler<T>(baseClass));
        logger.info("执行SQL：{}\n执行时间：{}", sql, Clock.systemDefaultZone().millis() - start);

        return list;
    }

    /**
     *  单属性模糊查询
     *
     * @author gclm
     * @param: param 表列名
     * @param: value 值
     * @date 2019/12/24 1:24 下午
     * @return: java.util.List<T>
     * @throws
     */
    @Override
    public List<T> selectLike(String param, String value){
        String sql = String.format(sqls[2],columnName, param + " LIKE '%"+value+"%'");
        List<T> list = new ArrayList<>();

        Long start = Clock.systemDefaultZone().millis();
        list = query(sql, new BeanListHandler<T>(baseClass));

        logger.info("执行SQL：{}\n执行时间：{}", sql, Clock.systemDefaultZone().millis() - start);
        return list;
    }

    /**
     *  单属性查询
     * @author gclm
     * @param: param  表属性
     * @param: value  名字
     * @date 2019/12/24 1:25 下午
     * @return: T
     * @throws
     */
    @Override
    public T selectOne(String param, String value) {
        String sql = String.format(sqls[2],columnName, param + " = '" +value +"'");
        Long start = Clock.systemDefaultZone().millis();
        T t = query(sql, new BeanHandler<T>(baseClass));
        logger.info("执行SQL：{}\n执行时间：{}", sql, Clock.systemDefaultZone().millis() - start);
        return t;
    }

    /**
     *  根据ID 执行查询操作
     *
     * @author gclm
     * @param: id
     * @date 2019/12/24 1:25 下午
     * @return: T
     * @throws
     */
    @Override
    public T selectById (String id) {
        String sql = String.format(sqls[2], columnName, "id =" + id);
        Long start = Clock.systemDefaultZone().millis();
        T t = query(sql, new BeanHandler<T>(baseClass));
        logger.info("执行SQL：{}\n执行时间：{}", sql, Clock.systemDefaultZone().millis() - start);
        return t;
    }


    /**
     *  依据反射自动插入SQL
     * @author gclm
     * @param: t 泛型T
     * @date 2019/12/24 1:25 下午
     * @return: boolean
     * @throws
     */
    @Override
    public boolean insert(T t){
        Field[] fields = baseClass.getDeclaredFields();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for (Field field : fields){
            field.setAccessible(true);
            try {
                logger.info("当前属性：[{}]\t值：[{}]\t类型：[{}]",field.getName(),field.get(t),field.get(t).getClass().getName());
                if(field.get(t) != null  &&  field.get(t).toString().trim().length() > 0) {
                    key.append(field.getName()).append(", ");
                    value.append("'").append(field.get(t)).append("', ");
                }
            } catch (IllegalAccessException e) {
               logger.error("插入数据失败\t{}",e);
            }

            field.setAccessible(false);
        }

        String sql = String.format(sqls[0],columnName,StringUtils.substringBeforeLast(key.toString(), ","),StringUtils.substringBeforeLast(value.toString(), ","));
        Long start = Clock.systemDefaultZone().millis();
        return isSuccess(insert(sql),sql,start);
    }

    /**
     *  根据 ID 执行更新
     *
     * @author gclm
     * @param: t 泛型T
     * @date 2019/12/24 1:26 下午
     * @return: boolean
     * @throws
     */
    @Override
    public boolean updateById(T t){
        Field[] fields = baseClass.getDeclaredFields();
        StringBuilder key = new StringBuilder();

        String id = null;

        for (Field field : fields){
            field.setAccessible(true);
            try {
                if(!ObjectUtils.isEmpty(field.get(t)) ){
                    logger.info("当前属性：[{}]\t值：[{}]\t类型：[{}]",field.getName(),field.get(t),field.get(t).getClass());
                    if(!"id".equals(field.getName())){
                        key.append(field.getName()).append(" = '").append(field.get(t)).append("' ,");
                    } else {
                        id = String.valueOf(field.get(t));
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error("更新数据失败\t{}",e);
            }
            field.setAccessible(false);
        }

        String sql = String.format(sqls[3], columnName, key.substring(0, key.length() - 1), id);
        Long startTime = Clock.systemDefaultZone().millis();
        return isSuccess(update(sql),sql,startTime);
    }

    /**
     *  根据ID 执行删除操作
     *
     * @author gclm
     * @param: id
     * @date 2019/12/24 1:26 下午
     * @return: boolean
     * @throws
     */
    @Override
    public boolean deleteById(String id){
        String sql = String.format(sqls[4],columnName,id);
        Long startTime = Clock.systemDefaultZone().millis();
        return isSuccess(update(sql),sql,startTime);
    }


    /**
     *  插入\修改\删除操作效验器
     *
     * @author gclm
     * @param: count 修改数据条数
     * @param: sql   sql 数据
     * @param: startTime 开始执行时间
     * @date 2019/12/24 1:27 下午
     * @return: boolean
     * @throws
     */
    private boolean isSuccess(int count,String sql,Long startTime){
        logger.info("执行SQL：{}\n执行时间：{}", sql, Clock.systemDefaultZone().millis() - startTime);
        if (1 == count) {
            return true;
        }
        return false;
    }

}
