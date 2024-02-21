package site.duqian.spring.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 数据库管理
 * @author Dusan-杜乾 Created on 2022.02.21
 * E-mail:duqian2010@gmail.com
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String driver = "org.gjt.mm.mysql.Driver";
    //private static final String url = "jdbc:mysql://server2.duqian.cn:5432/mysql";
    private static final String url = "jdbc:mysql://10.255.217.193:3306/test-mysql";
    private static final String user = "root";//"postgres";
    private static final String password = "dq20241";//"cc@";

    public static void connect() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            //Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            String sql = "SELECT * FROM mytable";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString(1);
                String name = rs.getString(2);
                logger.debug("title:" + title + ",name:" + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}