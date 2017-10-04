package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Dao.SaveResumeDao;
import Dao.userDao;
import Util.PathManager;
import bean.resume;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// TODO Auto-generated method stub
	String email = request.getParameter("email");
	String password = request.getParameter("passwd");
	HttpSession session = request.getSession(); 
	
	if (email != null && password != null){
		userDao usedao = new userDao();	
		if (password == ""){
			Boolean hasEmail = usedao.MailRegistered(email);
			PrintWriter out = response.getWriter(); 
		    String str ="{"+ "\"hasEmail\"" + ":" + hasEmail +"}";
		    out.println(str);
		}
		else{
			Boolean isLogin = usedao.Login(email, password);
			PrintWriter out = response.getWriter(); 
			
			long user_id = -1;
			userDao userfindid = new userDao();
            user_id = userfindid.Find_UserId(email);
            String status = "";
            if (user_id == -1){ // The user doesn't exist
                String url = PathManager.WebsitePath + "index.jsp";
                status ="{"+ "\"isLogin\"" + ":" + isLogin + ", \"transferPage\":\"" + url + "\"}";
            } else {
                SaveResumeDao resumeDB = new SaveResumeDao();
                List<resume> uploadedResumeList = resumeDB.GetUserAllUploadedResumes((int)user_id);
                if (uploadedResumeList == null || uploadedResumeList.isEmpty()){
                    String url = PathManager.WebsitePath + "index.jsp";
                    status ="{"+ "\"isLogin\"" + ":" + isLogin + ", \"transferPage\":\"" + url + "\"}";
                }
                else {
                    String url = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) 
                                 + "/dashboard.jsp?tabtype=dashboard";
                    status ="{"+ "\"isLogin\"" + ":" + isLogin + ", \"transferPage\":\"" + url + "\"}";
                }
                System.out.println(status);
            }
		    out.println(status);
		    
		    if (isLogin){
		    	String state = "{\"email\":\"" + email + "\"}";
				session.setAttribute("SigninState",state);
			}
		}
	 }
  }
}
