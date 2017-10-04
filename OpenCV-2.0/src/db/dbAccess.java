/**
 * 
 */
package db;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * @author jjh
 *访问数据库类
 *
 */
public class dbAccess {

	public SqlSession getSqlSession() throws IOException{
		//通过配置文件获取数据库连接信息
		Reader reader=Resources.getResourceAsReader("config/Configuration.xml");
		//通过配置嘻嘻构建一个sqlsessionfactory
		SqlSessionFactory sqlSessionFactory=new SqlSessionFactoryBuilder().build(reader);
		//通过sqlsessionfactory打开一个数据库回话
		SqlSession sqlSession=sqlSessionFactory.openSession(false);		
		return sqlSession ;
		 
	}
}
