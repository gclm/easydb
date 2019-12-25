package club.gclmit.esaydb.db.Impl;

import club.gclmit.esaydb.db.ChaosDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;

/**
 * <p>
 *  对DBUtils 的简单封装
 * </p>
 *
 * @author: gclm
 * @date: 2019/12/23 6:09 下午
 * @version: V1.0
 * @since 1.8
 */
public class ChaosQueryRunerImpl {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static QueryRunner queryRunner;


    static {
        /**
         * 在初始化过程中就执行实例化数据源，生成 QueryRunner
         */
        queryRunner = new QueryRunner(ChaosDataSource.getInstance());
    }

    /**
     *  通用查询封装，原则上支持所有查询
     *  https://www.cnblogs.com/ieayoio/p/5253568.html
     * @author gclm
     * @param: sql                sql 语句
     * @param: resultSetHandler   返回处理器
     * @param: params             参数集合
     * @date 2019/12/25 9:34 上午
     * @return:  <T> T 支持范围所有类型
     * @throws
     */
    public static <T> T query(String sql, ResultSetHandler<T> resultSetHandler, Object... params) {
        T result = null;
        try {
            result = queryRunner.query(sql, resultSetHandler, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  更新/删除操作封装
     *
     * @author gclm
     * @param: sql      sql 语句
     * @param: params   参数集合
     * @date 2019/12/25 9:35 上午
     * @return: int
     * @throws
     */
    public static int update(String sql, Object... params) {
        int result = 0;
        try {
            result = queryRunner.update(sql, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *  插入操作的封装
     *
     * @author gclm
     * @param: sql      sql 语句
     * @param: params   参数集合
     * @date 2019/12/25 9:37 上午
     * @return: int
     * @throws
     */
    public static int insert(String sql, Object... params) {

        int result = 0 ;
        try {
            result = queryRunner.execute(sql, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
