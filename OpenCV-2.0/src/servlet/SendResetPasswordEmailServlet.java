package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Dao.userDao;
import Util.MD5Util;
import Util.MailUtil;

/**
 * Servlet implementation class SendResetPasswordEmailServlet
 */
@WebServlet("/SendResetPasswordEmailServlet")
public class SendResetPasswordEmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendResetPasswordEmailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public String CreateLink(String email){
    	String secretKey = UUID.randomUUID().toString();                  //������Կ
    	Date outDate = new Date(System.currentTimeMillis() + 30*60*1000); //30���Ӻ����  
    	long date = outDate.getTime() / 1000 * 1000;
    	String key = date + "$" + secretKey;
    	String digitalSignature = MD5Util.MD5(key);
    	return digitalSignature;
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
		
		// Check whether the user has registered
		userDao usedao = new userDao();
		Boolean hasEmail = usedao.MailRegistered(email);
		PrintWriter out = response.getWriter(); 
		if (!hasEmail){
			 String str ="{"+ "\"state\":0" +"}";
			 out.println(str);
			 return;
		}
		
		// Send Email to user email box
		String from = "no-reply@opencv.expert";
	    String to = email;
	    String subject = "����������Ϣ";
	    
	    String body = "<div style=\"width: 650px; margin: 0 auto; padding: 40px 20px; background-color: #f5f5f5; color: #000; text-align: center; font-size: 16px\">" +
	    "<h3 style=\"font-size: 25px; line-height: 27px; text-align: center;\">" +
	    "ȫ����һ��</br>�˹����� ����������ļ����� ����" + 
	    "</h3>" +
	    "<p>" + to + "����ã�<br/>�����޸����������ѱ����ܣ������û�з�������������������ʼ���" + 
	    "</p>"+
	    "<p style=\"margin-bottom: 25px;\">���ʼ�����30���ӣ����ӽ���ʧЧ���������Ҫ�޸����룬������������ӣ�" +
	    "</p>" +
	    "<p>" + "http://opencv.expert/reset-password.jsp?useremail=" + email + "&key=" + CreateLink(email) + "</p>" +
	    "</div>";
	    
	    // Send HTML message
	    try {
	    	MailUtil.SendHtmlMessage(from, to, subject, body);
	    	String str ="{"+ "\"state\":1" + "}";
			out.println(str);
			 
        } catch (Exception exc) {
            exc.printStackTrace();
            
            // Fail to send email to user
    	    String str ="{"+ "\"state\":2" + "}";
    		out.println(str);
        }
	}
}