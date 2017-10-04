package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jspsmart.upload.Request;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;

import bean.resume;
import Dao.SaveResumeDao;
import Dao.userDao;

import Util.MailUtil;
import Util.ResumeBenchmark;
import Util.ConvertPDFToPng;
import Util.BenchmarkScore;
import Util.ConvertPDFToHtml;
import Util.PageGenerator;
import Util.PathManager;

/**
 * Servlet implementation class EmailServlet
 */
@WebServlet("/EmailServlet")
public class EmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final int MAX_RESUME_NUM = 3;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailServlet() {
        super();
    }
        
    // Load benchmark score from JSON file
    public BenchmarkScore LoadBenchmarkScoreFromJSON(String jsonFilename){
    	if (jsonFilename == null || !jsonFilename.endsWith(".json")){
    		return null;
    	}
    	BenchmarkScore benchmarkScore = new BenchmarkScore();
    	benchmarkScore.parseBenchmarkScoreJSONFile(jsonFilename);
    	return benchmarkScore;
    }
    
    public void SaveBenchmarkScores(List<BenchmarkScore> allBenchmarkScores, String dashBoardFilename){
    	if (allBenchmarkScores.isEmpty()){
    		return;
    	}
    	BenchmarkScore.saveBenchmarkScoreList(allBenchmarkScores, dashBoardFilename);
    }
    
    // Generate resume thumbnails
    public void GenerateUserResumeThumbnails(String serverRootPath,List<resume> resumeList){
    	if (resumeList.isEmpty()){ return; }
    	
    	String separator = PathManager.SystemPathSeparator;
    	long user_id = resumeList.get(0).getUserid();
    	PathManager pathManager  = new PathManager(serverRootPath);
   
    	String thumbnail_path = pathManager.GetUserFolderPath() + separator + String.valueOf(user_id) +  separator + "thumbnail";
    	String thumbnail_large_path = pathManager.GetUserFolderPath() + separator + String.valueOf(user_id) + separator + "thumbnail-large";
    	
    	// Check whether the thumbnail folder exists
    	File thumbnail_file = new File(thumbnail_path);
    	if (!thumbnail_file.exists()){
    		thumbnail_file.mkdirs();
    	}
    	File thumbnail_large_file = new File(thumbnail_large_path);
    	if (!thumbnail_large_file.exists()){
    		thumbnail_large_file.mkdirs();
    	}
    	
    	ConvertPDFToPng converter = new ConvertPDFToPng();
    	for (int i=0; i<resumeList.size(); ++i){
    		String resume_filename = resumeList.get(i).getFilepath(); 
    		
    		// Start to generate thumbnails for resume file
    		if (resume_filename.endsWith(".pdf") || resume_filename.endsWith(".PDF")){
    			long resume_id = resumeList.get(i).getId();
    			String thumbnail_filename = thumbnail_path + separator + "resume-" + String.valueOf(resume_id) + ".png";
    			String thumbnail_large_filename = thumbnail_large_path + separator + "resume-" + String.valueOf(resume_id) + ".png";
    			converter.GenerateThumbnails(resume_filename, thumbnail_filename, 0);
    			converter.GenerateThumbnails(resume_filename, thumbnail_large_filename, 1);
    		}
    	}
    }
        
    // Generate resume benchmark page
    public List<BenchmarkScore> GenerateResumeBenchmarkPage(String serverRootPath, String userEmail, List<resume> resumeList){
    	if (resumeList.isEmpty()){ return null; }
    	
    	// Get user id and initialize benchmark scores list
    	long user_id = resumeList.get(0).getUserid();
    	String separator = PathManager.SystemPathSeparator;
    	List<BenchmarkScore> allBenchmarkScores = new ArrayList<BenchmarkScore>();
    	
    	// Process each PDF resume file, and compute the benchmark score
    	BenchmarkScore benchmarkScore = null;
    	PathManager pathManager = new PathManager(serverRootPath);
    	
    	for (int i=0; i<resumeList.size(); ++i){
    	    // 初始化HTML版简历存储位置及文件名
    	    String dest_dir = pathManager.GetBenchmarkFolderPath() + separator + String.valueOf(resumeList.get(i).getId());
    		String htmlResumeFilename = "resume.html";
    		
    		// 获取简历ID
            long resume_id = resumeList.get(i).getId();
            
    		File file = new File((dest_dir + separator + htmlResumeFilename));
    		if (!file.exists()){ // If the resume wasn't analyzed
    			// Check whether the folder has been created
    		    File htmlResumeFolder = new File(dest_dir);
    		    if (!htmlResumeFolder.exists()){
    		        htmlResumeFolder.mkdirs();
    		    }
    			
    			// Start to analyzing the resume file (PDF file)
    			String resumeFilename = resumeList.get(i).getFilepath();
    			
    			if (resumeFilename.endsWith(".pdf") || resumeFilename.endsWith(".PDF")){
    				// Convert PDF file into HTML page
    			    System.out.println("Convert PDF resume into HTML pages");
    				ConvertPDFToHtml.PDF2HtmlEx(resumeFilename, dest_dir, htmlResumeFilename);

    				// Waring: check the resume filename to prevent from the complex resume filename;
    				//         If the resume filename contains some special characters, WE SHOULD TEST !!!
    				ResumeBenchmark resumeBenchmark = new ResumeBenchmark(resumeFilename);
    				
    				System.out.println("Start to compute resume " + resume_id + " benchmark");
    				benchmarkScore = resumeBenchmark.ComputeResumeBenchmarkScore(serverRootPath);
    				benchmarkScore.setUserId(user_id);
    				benchmarkScore.setResumeId(resume_id);
    				benchmarkScore.setResumeFilename(resumeList.get(i).getFilename());
    				benchmarkScore.setResumePath(resumeFilename);
    				
    				String benchmarkScoreOutputJSONFilename = pathManager.GetBenchmarkFolderPath() + separator + 
    				        String.valueOf(resume_id) + separator + "benchmark.json";
    				benchmarkScore.saveBenchmarkScore(benchmarkScoreOutputJSONFilename);
    				
    				// Generate resume benchmark page
    				resumeBenchmark.setUsername(userEmail);
    				resumeBenchmark.setUserEmail(userEmail);
    				resumeBenchmark.GenerateBenchmarkPage(serverRootPath, resume_id);
    				
    				allBenchmarkScores.add(benchmarkScore);
    			}
    		}
    		else { // If the resume had been analyzed
    			// Load benchmark score from the JSON file
    			String jsonFilename = pathManager.GetBenchmarkFolderPath() + separator +  String.valueOf(resume_id) + separator + "benchmark.json";
    			benchmarkScore = LoadBenchmarkScoreFromJSON(jsonFilename);
    			
    			if (benchmarkScore != null){
    				allBenchmarkScores.add(benchmarkScore);
    			}
    		}
    	}
    	return allBenchmarkScores;
    }
        
    // Update user dashboard page
    public void UpdateDashboardPage(String serverRootPath, String userEmail, List<resume> resumeList){
    	if (resumeList.isEmpty()){ return; }
    	long user_id = resumeList.get(0).getUserid();
    	
    	// Generate user resume thumbnails
    	String separator = PathManager.SystemPathSeparator;
    	PathManager pathManager = new PathManager(serverRootPath);
    	GenerateUserResumeThumbnails(serverRootPath,resumeList);
    	
    	// Update resume scores
    	String userPath = pathManager.GetUserFolderPath();
    	String dashboardJSONName = userPath + separator + String.valueOf(user_id) + separator + "dashboard.json";
    	List<BenchmarkScore> allBenchmarkScores = GenerateResumeBenchmarkPage(serverRootPath, userEmail, resumeList);
    	SaveBenchmarkScores(allBenchmarkScores, dashboardJSONName);
    	
    	// Use dashboard template to generate a new dashboard page
    	System.out.println("Start to generate benchmark page");
    	
    	PageGenerator pageGenerator = new PageGenerator();
    	String dashboard_template = pathManager.GetTemplateFolderPath() + separator + "dashboard-template.html";
    	String addButtonImage = PathManager.GetImagePath() + "/add.png";
    	pageGenerator.setDashboard_Template(dashboard_template);
    	pageGenerator.setUploadResumeImageName(addButtonImage);
    	
    	String dashboardPageName = userPath + separator + String.valueOf(user_id) + separator + "dashboard.jsp";
    	try {
    		String dashboardScoresForClient = BenchmarkScore.GenerateBenchmarkScoreForClient(allBenchmarkScores);
    		pageGenerator.setUserName(userEmail);
    		pageGenerator.setUserEmail(userEmail);
			pageGenerator.CreateDashboardPage(resumeList, dashboardScoresForClient, dashboardPageName);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	System.out.println("Finish to generate benchmark page");
    }
    
    // Send response email to user for receiving the upload resume
    public void SendReceiveResumeResponseEmailToUser(String userEmail, String resumeFilename) throws UnsupportedEncodingException{
        String subject = "opencv.expert已收到您上传的简历";
        String content = "<div style=\"width: 650px; margin: 0 auto; padding: 40px 20px; background-color: #f5f5f5; color: #000; text-align: center; font-size: 16px\">" +
                "<h3 style=\"font-size: 25px; line-height: 27px; text-align: center;\">" +
                "全国第一款</br>人工智能 “测简历、改简历” 工具" + 
                "</h3>" +
                "<p>" + userEmail + "，你好！<br/>我们已经收到您上传的简历" + "。<br/>" +
                "</p>"+
                "</div>";
        
        try {
            // The from must be empty
            MailUtil.SendHtmlMessage("", userEmail, subject, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {       
	    // 变量初始化
        String userEmail = "";
        String userName = "";
        long user_id = -1;
		boolean success = false;
		PrintWriter out = response.getWriter(); 
		
		// 获取用户邮箱
		SmartUpload upload = new SmartUpload(); 
		Request req = upload.getRequest();
		String email = "";
		String resumeFilename = "";
		String separator = PathManager.SystemPathSeparator;
		
		// 获取上传的简历文件
		String serverRootPath = getServletContext().getRealPath("/");
		System.out.println(serverRootPath); // output the web site path
		PathManager pathManager = new PathManager(serverRootPath);
		
		try{
    		  upload.initialize(getServletConfig(), request, response);  
    		  upload.setAllowedFilesList("doc,docx,pdf");// 允许上传的文件类型  
    		  upload.setDeniedFilesList("exe,bat,jsp"); // 拒绝上传的文件类型  
    		  upload.setMaxFileSize(1024*1024);      // 允许上传文件的单个最大大小 为1M
    		  upload.setTotalMaxFileSize(1024*1024*2); // 允许上传文件的最大大小总和为2M
    		  
    		  // 上传数据
    		  upload.upload();
    		  
    		  email = req.getParameter("email");
    		  String filename = upload.getFiles().getFile(0).getFileName(); // 获取简历文件名
    		  resumeFilename = new String(filename.getBytes("GBK"),"utf-8");
    		  if (email == null || email.isEmpty()){
    			  System.out.println("Email Servlet: user email is empty");
    			  return;
    		  }
    		  
    		  userDao userfindid = new userDao();
    		  user_id = userfindid.Find_UserId(email);
    		  if (user_id == -1){ // The user doesn't exist
    		      System.out.println("Email Servlet: user is not existed");
    		      return;
    		  }
    		 
    		 // Get user info
    		  userEmail = email;
    		 userName = userfindid.QueryUserName(user_id);
    		 
    		 // Query the upload reumses
    		 SaveResumeDao resumeDB = new SaveResumeDao();
    	     List<resume> uploadedResumeList = resumeDB.GetUserAllUploadedResumes((int)user_id);
    	     if (uploadedResumeList != null && uploadedResumeList.size()>MAX_RESUME_NUM){
    	         success = false;
	             String str ="{"+ "\"state\":" + success +", \"reason\":\"" + "每个用户最多只能上传三份简历！" + "\"}";
	             out.println(str);
	             return;
    	     }
    	     
    		 SaveResumeDao saveresumedao = new SaveResumeDao();
    		 Date date = new Date();
    		 SimpleDateFormat upload_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHMMSS");  
    		
    		 String mappedResumeFilename = sdf.format(date) + "-" + String.valueOf(user_id) + ".pdf";
    		 String actualResumeFilePath = pathManager.GetResumeFolderPath() + separator + mappedResumeFilename;
    		 
    		 // Insert resume information into database
    		 saveresumedao.SaveResume(user_id, upload_time.format(date), resumeFilename, actualResumeFilePath);
    		 
    		 upload.getFiles().getFile(0).saveAs(actualResumeFilePath);
    		 System.out.println("Email Servlet: insert user resume record in DB successfully");
    		
    		// Send file into QQ mail box
	        com.jspsmart.upload.File tempFile = upload.getFiles().getFile(0);
	        //MailUtil.SendResumeFileToQQMailBox(tempFile, actualResumeFilePath, email, "1922884165@qq.com", "1922884165@qq.com");
	        //SendReceiveResumeResponseEmailToUser(email, upload.getFiles().getFile(0).getFileName());
	        System.out.println("Send email successfully");
    	        
		} catch (SQLException e) {
           e.printStackTrace();
           // Output
           success = false;
           String str ="{"+ "\"state\":" + success +", \"reason\":\"" + "无法连接到服务器，请稍后再试！" + "\"}";
           out.println(str);
           return;
        } catch (SmartUploadException e){  
		    e.printStackTrace(); 
		    // Output
		    success = false;
            String str ="{"+ "\"state\":" + success +", \"reason\":\"" + "无法连接到服务器，请稍后再试！" + "\"}";
            out.println(str);
		    return;
		} catch(UnsupportedEncodingException e){
		    e.printStackTrace();
		}

		// 获取UserID的所有简历
		SaveResumeDao resumeDB = new SaveResumeDao();
		List<resume> resumeList = resumeDB.GetUserAllUploadedResumes((int)user_id);
					  
		// 生成用户 & 简历测评文件夹
		String userPath = pathManager.GetUserFolderPath() + separator + String.valueOf(user_id);
		File userFile = new File(userPath);
		if (!userFile.exists()){
			userFile.mkdirs();
		}
		  
		// Update user dashboard page and corresponding benchmark pages
		System.out.println("Start to update user dashboard");
		UpdateDashboardPage(serverRootPath, userEmail, resumeList);
		success = true;
		
		String dashboard_url = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) 
		        + "/dashboard.jsp?tabtype=dashboard";
        String str ="{"+ "\"state\":" + success +", \"transferPage\":\"" + dashboard_url + "\"}";
        System.out.println(str);
        out.println(str);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
