package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Dao.userDao;

/**
 * Servlet implementation class SignUpServlet
 */
@WebServlet("/SignUpServlet")
public class SignUpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignUpServlet() {
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
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("passwd");
		HttpSession session = request.getSession(); 
		
		if (email != null && email.length()>0){
			userDao usedao = new userDao();
			Boolean hasEmail = usedao.MailRegistered(email);
			if (!hasEmail){
				Boolean isRegister = usedao.Register(username, email, password);

				PrintWriter out = response.getWriter(); 
			    String str ="{"+ "\"hasSignUp\"" + ":" + isRegister +"}";
			    out.println(str);
			    
				if(isRegister){
					String state = "{\"username\":\"" + username + "\",\"email\":\"" + email + "\"}";
					session.setAttribute("SigninState", state);
					//request.getRequestDispatcher("/index.jsp").forward(request, response);  
				}
			}
		}
	}
}
