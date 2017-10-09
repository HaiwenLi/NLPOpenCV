package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Dao.userDao;
import Util.PathManager;

/**
 * Servlet implementation class UpdatePasswordServlet
 */
@WebServlet("/WebPageTransferServlet")
public class WebPageTransferServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebPageTransferServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String email = request.getParameter("userEmail");
		String targetPage = request.getParameter("targetPage");
		boolean success = false;
		
		String url = "";		
		if (targetPage != null && !targetPage.isEmpty()){
			if (targetPage.compareTo("dashboard") == 0){
				// Get user id
				userDao usedao = new userDao();
				long user_id = usedao.Find_UserId(email);
				System.out.println("to dashbaord page, email: "+email);
				if (user_id != -1){
					success = true;
					url = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) + "/dashboard.jsp?tabtype=dashboard";
				}
			} else if (targetPage.compareTo("benchmark") == 0){
				int resume_id = Integer.parseInt(request.getParameter("resumeId"));
			    int tab_index = Integer.parseInt(request.getParameter("tabValue"));
			    
				// Benchmark page url
			    System.out.println("to benchmark page, email: " + email);
				success = true;
				url = PathManager.GetWebsiteBenchmarkPath() + "/"+ String.valueOf(resume_id) + "/benchmark.jsp?tabtype=benchmark&tabvalue=" +
				                    String.valueOf(tab_index);
			}
		}
		
		//将结果传到前台
	    PrintWriter out = response.getWriter(); 
	    String str ="{"+ "\"state\":" + success + ", \"transferPage\":\"" + url + "\"}";
	    System.out.println(str);
		out.println(str);
	}
}
