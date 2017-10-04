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
 *�������ݿ���
 *
 */
public class dbAccess {

	public SqlSession getSqlSession() throws IOException{
		//ͨ�������ļ���ȡ���ݿ�������Ϣ
		Reader reader=Resources.getResourceAsReader("config/Configuration.xml");
		//ͨ��������������һ��sqlsessionfactory
		SqlSessionFactory sqlSessionFactory=new SqlSessionFactoryBuilder().build(reader);
		//ͨ��sqlsessionfactory��һ�����ݿ�ػ�
		SqlSession sqlSession=sqlSessionFactory.openSession(false);		
		return sqlSession ;
		 
	}
}
