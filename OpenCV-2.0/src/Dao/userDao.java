package Dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import bean.user;
import db.dbAccess;

public class userDao {
	
	// Check whether the email has registered
	public Boolean MailRegistered(String mail){
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		List<user> userList = new ArrayList<user>();
		
		try{
			sqlsession = dbaccess.getSqlSession();
			user useone = new user();
			useone.setMailbox(mail);
			userList = sqlsession.selectList("find",useone);
			if(userList.isEmpty()){
				return false;	
			}
			else{
				return true;
			}
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sqlsession !=null){
				sqlsession.close();
			}
		}
		return false;			
	}
		
	// Update user password by email
	public Boolean UpdatePassword(String email,String password){
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		List<user> userList = new ArrayList<user>();
		
		try {
			sqlsession = dbaccess.getSqlSession();
			user useone = new user();
			useone.setMailbox(email);
			userList = sqlsession.selectList("find",useone);
			
			if(userList.isEmpty()){
				return false;
			}
			else{
				// If the user has registered
				user oneUser = new user();
				oneUser.setId(userList.get(0).getId());
				oneUser.setPassword(password);
				sqlsession.update("updateOne", oneUser);
				sqlsession.commit();
				return true;
			}			
		} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(sqlsession != null){
				    sqlsession.close();
				}
			}
		 return false;
	}
	  
	// User login with email address and password
	public Boolean Login(String email,String password){ 
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		List<user> userList = new ArrayList<user>();
		try {
			sqlsession = dbaccess.getSqlSession();
			user useone = new user();
			useone.setMailbox(email);
			useone.setPassword(password);
			userList = sqlsession.selectList("findlogin",useone);
			if(userList.isEmpty()){
				return false;
			}
			else{
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sqlsession !=null){
				sqlsession.close();
			}
		}
	   return false;
	}
	
	// User registers with user name, email (unique) and password
	public Boolean Register(String username,String email,String password)  
	{
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		
		try {
			sqlsession=dbaccess.getSqlSession();
			user useone = new user();
			useone.setUsername(username);
			useone.setPassword(password); 
			useone.setMailbox(email);
			useone.setTel("x");
			
			sqlsession.insert("register",useone);
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
	
	// Find user id by email. if not found, then return -1
	public int Find_UserId(String email)  
	{
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		int id = -1;
		try {
			sqlsession=dbaccess.getSqlSession();
			user useone = new user();
			useone.setMailbox(email);
			user foundUser = sqlsession.selectOne("find_userid",useone);
			id = foundUser.getId();
			sqlsession.commit(); 
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(sqlsession !=null){
			    sqlsession.close();
			}
		}
		 return id;
	}
	
	// Query user name by email address & password
	public String QueryUserName(String emailbox){
		dbAccess dbaccess = new dbAccess();
		SqlSession sqlsession = null;
		String username = null;
		
		if (emailbox == null || emailbox.isEmpty()){
			return null;
		}
		
		try {
			sqlsession = dbaccess.getSqlSession();
			user userone = new user();		
			userone.setMailbox(emailbox);	
			user found_user = sqlsession.selectOne("find_username",userone);
			sqlsession.commit();
			username = found_user.getUsername();
		 } catch (IOException e) {
			e.printStackTrace();
		 }finally{ 
			if(sqlsession !=null){
			    sqlsession.close();
			}
		}
		return username;
   }
	
	// Query user name by email address & password
    public String QueryUserName(long user_id){
        String username = null;
        if (user_id < 0){
            return username;
        }
        
        dbAccess dbaccess = new dbAccess();
        SqlSession sqlsession = null;
        
        try {
            sqlsession = dbaccess.getSqlSession();
            user userone = new user();
            userone.setId((int)user_id);
            user found_user = sqlsession.selectOne("find_usernameByID",userone);
            sqlsession.commit();
            username = found_user.getUsername();
         } catch (IOException e) {
            e.printStackTrace();
         }finally{ 
            if(sqlsession !=null){
                sqlsession.close();
            }
        }
        return username;
   }
}