package Dao;

import java.io.IOException;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import bean.resume;
import db.dbAccess;

public class SaveResumeDao {
	// Save resume information into database
	public boolean SaveResume( long user_id, String date, String filename,String filepath ){
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		 
		try {
			sqlsession=dbaccess.getSqlSession();
			resume resumeone = new resume();
			 
			resumeone.setUserid(user_id);
			resumeone.setDate(date);
			resumeone.setFilename(filename);
			resumeone.setFilepath(filepath);
			
			sqlsession.insert("insert_resume",resumeone);
			sqlsession.commit();
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sqlsession !=null){
				sqlsession.close();
			}
		}
		return false;
	}
	
	//通过userID获取数据库中的简历信息
	public List<resume> GetUserAllUploadedResumes(int userID){
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		List<resume> resumelist = null;
		
		try {
			sqlsession = dbaccess.getSqlSession();
			resume resumeone = new resume();
			resumeone.setUserid(userID);
			
			resumelist = sqlsession.selectList("find_resumeByUserID",resumeone);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sqlsession != null){
				sqlsession.close();
			}
		}
		return resumelist;
	}
}
