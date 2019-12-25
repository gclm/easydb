package club.gclmit.easydb.db;

import java.util.List;

/**
 * Copyright (C), 2016-2018, 孤城落寞的博客
 *
 * @program: club.gclmit.dao
 * @author: gclm
 * @date: 2019-03-27 12:47
 * @description:  QueryRuner的 简单封装
 */
public interface SimpleQueryRuner<T> {

    public List<T> selectList();

    public List<T> selectLike(String param, String value);

    public T selectOne(String param, String value);

    public T selectById(String id);

    public Long count();

    public boolean insert(T t) ;

    public boolean updateById(T t);

    public boolean deleteById(String id);


}
