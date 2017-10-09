package servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONException;

import com.jspsmart.upload.Request;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;

import bean.resume;
import Dao.SaveResumeDao;
import Dao.userDao;

import Util.MailUtil;
import Util.ResumeBenchmark;
import Util.ConvertPDFToPng;
import Util.DebugConfig;
import Util.BenchmarkScore;
import Util.BenchmarkState;
import Util.ConvertPDFToHtml;
import Util.PageGenerator;
import Util.PathManager;

/**
 * Servlet implementation class EmailServlet
 */
@WebServlet("/EmailServlet")
public class EmailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final int MAX_RESUME_NUM = 7;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmailServlet() {
        super();
    }
        
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {       
	    // 变量初始化
        long user_id = -1;
		int success = 0x000;
		PrintWriter out = response.getWriter(); 
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

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
		      // 检查上传的文件类型和大小
    		  upload.initialize(getServletConfig(), request, response);
    		  try{
        		  upload.setAllowedFilesList("pdf");         // 允许上传的文件类型  
        		  upload.setDeniedFilesList("exe,bat,,jsp"); // 拒绝上传的文件类型  
        		  upload.setMaxFileSize(1024*1024);          // 允许上传文件的单个最大大小 为1M
        		  upload.setTotalMaxFileSize(1024*1024*2);   // 允许上传文件的最大大小总和为2M
        		  upload.upload(); // 上传数据
    		  } catch (Exception e){
    		      success = BenchmarkState.InvalidFileCode;
    		      String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.InvalidFileDescription + "\"}";
                  out.println(str);
                  return;
    		  }
    		  
    		  // 检查文件是否上传成功
    		  email = req.getParameter("email");
    		  if (upload.getFiles().getCount() <= 0 || (email == null || email.isEmpty())){
    		      success = BenchmarkState.FailtoUplaodFileCode;
                  String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.FailtoUplaodFileDescription + "\"}";
                  out.println(str);
                  return;
    		  }
    		  
    		  // 获取简历文件名
    		  String filename = upload.getFiles().getFile(0).getFileName(); // 获取简历文件名
    		  if (DebugConfig.DEBUG_OUTPUT){
    		      System.out.println(filename);
    		  }
    		  resumeFilename = filename;
    		  
    		  // 检测该邮箱对应的用户是否已经注册
    		  userDao userfindid = new userDao();
    		  user_id = userfindid.Find_UserId(email);
    		  if (user_id == -1){ // The user doesn't exist
    		      success = BenchmarkState.NoUserCode;
    		      String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.NoUserCode + "\"}";
                  out.println(str);
    		      return;
    		  }
    		  
    		  // 保存简历至 PathManager->ResumePath
    		  SaveResumeDao saveresumedao = new SaveResumeDao();
    		  Date date = new Date();
    		  TimeZone chinaTimezone = TimeZone.getTimeZone("Asia/Shanghai");
    		  SimpleDateFormat upload_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		  upload_time.setTimeZone(chinaTimezone);
    		  if (DebugConfig.DEBUG_OUTPUT){
    		      System.out.println(upload_time.format(date));
    		  }
    		  
    		  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHMMSS");  
    		  sdf.setTimeZone(chinaTimezone);
    		  String mappedResumeFilename = sdf.format(date) + "-" + String.valueOf(user_id) + ".pdf";
    		  String actualResumeFilePath = pathManager.GetResumeFolderPath() + separator + mappedResumeFilename;

    		  // 保存简历文件
    		  try {
    		      upload.getFiles().getFile(0).saveAs(actualResumeFilePath);
    		  } catch (SmartUploadException e) {
    		      e.printStackTrace();
    		      
    		      // Output
    		      success = BenchmarkState.FailtoUplaodFileCode;
    		      String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.FailtoUplaodFileDescription + "\"}";
    		      out.println(str);
    		      return;
    		  }
    		  
    		  // 检测简历是否为中文
    		  if (!IsChineseResume(actualResumeFilePath)){
    		      success = BenchmarkState.NonChineseFileCode;
    		      String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.NonChineseFileCode + "\"}";
                  out.println(str);
                  return;
    		  }
    		 
    		 // 查询该用户之前上传的简历文件
    		 SaveResumeDao resumeDB = new SaveResumeDao();
    	     List<resume> uploadedResumeList = resumeDB.GetUserAllUploadedResumes((int)user_id);
    	     FilterResumeList(uploadedResumeList, -1l, pathManager.GetBenchmarkFolderPath(), separator); // 过滤resume list
    	     for (resume r : uploadedResumeList){
    	         System.out.println("Uploaded resume id: " + r.getId());
    	     }
 
    	     if (uploadedResumeList != null && uploadedResumeList.size()>MAX_RESUME_NUM){
    	         success = BenchmarkState.ExceedMaxFreeUploadTimesCode;
	             String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.ExceedMaxFreeUploadTimesDescription + "\"}";
	             out.println(str);
	             return;
    	     }
    	     
    		// Backup the file with QQ Mailbox and send response email to user
	        com.jspsmart.upload.File tempFile = upload.getFiles().getFile(0);
	        SendReceiveResumeResponseEmailToUser(email, resumeFilename);
	        MailUtil.SendResumeFileToQQMailBox(tempFile, actualResumeFilePath, email, "1922884165@qq.com", "1922884165@qq.com");
	        System.out.println("Send email successfully");
	        
	        // Insert resume information into database
	        saveresumedao.SaveResume(user_id, upload_time.format(date), resumeFilename, actualResumeFilePath);
	        System.out.println("Email Servlet: insert user resume record in DB successfully");
		} catch (Exception e){
		    e.printStackTrace(); 
		    
		    // Output
		    success = BenchmarkState.FailtoUplaodFileCode;
            String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.FailtoUplaodFileDescription + "\"}";
            out.println(str);
		    return;
		}
		
		// 获取UserID的所有简历，过滤之前上传但分析失败的文件
		SaveResumeDao resumeDB = new SaveResumeDao();
		List<resume> resumeList = resumeDB.GetUserAllUploadedResumes((int)user_id);
		long latest_uploaded_index = 0;
        for (int i=0; i<resumeList.size(); ++i){
            long resume_id = resumeList.get(i).getId();
            if (resume_id > latest_uploaded_index){
                latest_uploaded_index = resume_id;
            }
        }
		FilterResumeList(resumeList, latest_uploaded_index, pathManager.GetBenchmarkFolderPath(),separator); // 过滤resume list
        // Sort the resume by resume id
        Collections.sort(resumeList,new Comparator<resume>(){  
            public int compare(resume arg0, resume arg1) {  
                return (new Long(arg1.getId())).compareTo(new Long(arg0.getId()));
            }  
        });
		for (resume r : resumeList){
            System.out.println("Uploaded resume id: " + r.getId());
        }
		
		// 生成用户 & 简历测评文件夹
		String userPath = pathManager.GetUserFolderPath() + separator + String.valueOf(user_id);
		File userFile = new File(userPath);
		if (!userFile.exists()){
			userFile.mkdirs();
		}
		  
		// Update user dashboard page and corresponding benchmark pages
		try{
    		System.out.println("Start to update user dashboard");
    		UpdateDashboardPage(serverRootPath, email, resumeList);
    		success = BenchmarkState.SuccessCode;
    		
    		String dashboard_url = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) 
    		        + "/dashboard.jsp?tabtype=dashboard";
            String str ="{"+ "\"state\":" + success +", \"transferPage\":\"" + dashboard_url + "\"}";
            System.out.println(str);
            out.println(str);
		} catch (Exception e) {
            e.printStackTrace();
            
            // Ouput 
            success = BenchmarkState.FailtoAnalyseResumeCode;
            String str ="{"+ "\"state\":" + success +", \"reason\":\"" + BenchmarkState.FailtoAnalyseResumeDescription + "\"}";
            out.println(str);
            return;
        }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
	
	// 过滤简历列表，去除没有成功测评的简历
	void FilterResumeList(List<resume> resumeList, Long new_resume_id, String benchmark_dir, String separator){
	    Iterator<resume> resume_iter = resumeList.iterator();
        while(resume_iter.hasNext()){
            long resume_id = resume_iter.next().getId();
            if (new_resume_id != -1 && resume_id == new_resume_id){
                continue;
            }
            String benchmark_page = benchmark_dir + separator + String.valueOf(resume_id) +
                    separator + "benchmark.jsp";
            File benchmark_pageFile = new File(benchmark_page);
            if(!benchmark_pageFile.exists()){
                resume_iter.remove();
            }
        }
	}
	
	// 获取一段文本中的中文字数
	long GetChineseWordsNum(String str){
	    long wordsNum = 0;
	    Pattern zhCharPattern = Pattern.compile("[\u4e00-\u9fa5]");
	    Matcher zhCharMatcher = zhCharPattern.matcher(str);
        while (zhCharMatcher.find()){ wordsNum += 1; }
        
        return wordsNum;
	}
	
	// 判断上传的文件是否为中文简历
	public boolean IsChineseResume(String resumeFilename){
	    if (!(resumeFilename.endsWith(".pdf") || resumeFilename.endsWith(".PDF"))){
	        return false;
	    }
	    
	    try {
            PDDocument document = PDDocument.load(new File(resumeFilename));
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String resumeWords = stripper.getText(document);
            document.close();
            
            // 统计中文字数
            long words_num = GetChineseWordsNum(resumeWords);
            if (words_num <= ResumeBenchmark.WordNumber_Threshold1){ // threshold_1 is the minimum words of resume
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
    // Load benchmark score from JSON file
    public BenchmarkScore LoadBenchmarkScoreFromJSON(String jsonFilename) throws FileNotFoundException, JSONException, IOException{
        if (jsonFilename == null || !jsonFilename.endsWith(".json")){
            return null;
        }
        BenchmarkScore benchmarkScore = new BenchmarkScore();
        benchmarkScore.parseBenchmarkScoreJSONFile(jsonFilename);
        return benchmarkScore;
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
    public List<BenchmarkScore> GenerateResumeBenchmarkPage(String serverRootPath, String userEmail, List<resume> resumeList) throws FileNotFoundException, JSONException, IOException{
        if (resumeList.isEmpty()){ return null; }
        
        // Get user id and initialize benchmark scores list
        long user_id = resumeList.get(0).getUserid();
        String separator = PathManager.SystemPathSeparator;
        List<BenchmarkScore> allBenchmarkScores = new ArrayList<BenchmarkScore>();
        
        // Process each PDF resume file, and compute the benchmark score
        BenchmarkScore benchmarkScore = null;
        PathManager pathManager = new PathManager(serverRootPath);
        
        // Find the latest uploaded file
        long latest_uploaded_index = 0;
        for (int i=0; i<resumeList.size(); ++i){
            long resume_id = resumeList.get(i).getId();
            if (resume_id > latest_uploaded_index){
                latest_uploaded_index = resume_id;
            }
        }
        
        // 遍历所有简历，获取所有简历的评测结果
        for (int i=0; i<resumeList.size(); ++i){
            long resume_id = resumeList.get(i).getId();
            String dest_dir = pathManager.GetBenchmarkFolderPath() + separator + String.valueOf(resume_id);
            
            // 判断该简历是否是最新上传的简历
            if (resume_id != latest_uploaded_index){
                // 检查是否成功生成了 benchmark.jsp文件
                String benchmarkPageName = dest_dir + separator + "benchmark.jsp";
                File benchmarkPageFile = new File(benchmarkPageName);
                if (benchmarkPageFile.exists()){
                    // 如果文件存在，加载benchmark json
                    String jsonFilename = dest_dir + separator + "benchmark.json";
                    benchmarkScore = LoadBenchmarkScoreFromJSON(jsonFilename);
                    
                    if (benchmarkScore != null){
                        allBenchmarkScores.add(benchmarkScore);
                    }
                } else{
                    continue;
                }
            }
            // 如果该文件是新上传的简历
            else{
                File htmlResumeFolder = new File(dest_dir);
                if (!htmlResumeFolder.exists()){
                    htmlResumeFolder.mkdirs();
                }
                String resumeFilename = resumeList.get(i).getFilepath();
                
                // 使用pdf2htmlEx将pdf转化为html
                System.out.println("Convert PDF resume into HTML pages");
                ConvertPDFToHtml.PDF2HtmlEx(resumeFilename, dest_dir, "resume.html");
                
                // 进行简历评测
                System.out.println("Start to compute resume " + resume_id + " benchmark");
                // Waring: check the resume filename to prevent from the complex resume filename;
                //         If the resume filename contains some special characters, WE SHOULD TEST !!!
                ResumeBenchmark resumeBenchmark = new ResumeBenchmark(resumeFilename);
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
        return allBenchmarkScores;
    }
        
    // Update user dashboard page
    public void UpdateDashboardPage(String serverRootPath, String userEmail, List<resume> resumeList) throws FileNotFoundException, JSONException, IOException{
        if (resumeList.isEmpty()){ return; }
        long user_id = resumeList.get(0).getUserid();
        
        // Generate user resume thumbnails
        String separator = PathManager.SystemPathSeparator;
        PathManager pathManager = new PathManager(serverRootPath);
        GenerateUserResumeThumbnails(serverRootPath,resumeList);
        
        // Generate resume benchmark page
        String userPath = pathManager.GetUserFolderPath();
        String dashboardJSONName = userPath + separator + String.valueOf(user_id) + separator + "dashboard.json";
        List<BenchmarkScore> allBenchmarkScores = GenerateResumeBenchmarkPage(serverRootPath, userEmail, resumeList);
        if (!allBenchmarkScores.isEmpty()){
            BenchmarkScore.saveBenchmarkScoreList(allBenchmarkScores, dashboardJSONName);
        }
        System.out.println("Start to generate benchmark page");
        
        // Use dashboard template to generate a new dashboard page
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
    public void SendReceiveResumeResponseEmailToUser(String userEmail, String resumeFilename) throws Exception{
        String subject = "opencv.expert已收到您上传的简历";
        String content = "<div style=\"width: 650px; margin: 0 auto; padding: 40px 20px; background-color: #f5f5f5; color: #000; text-align: center; font-size: 17px\">" +
                "<h3 style=\"font-size: 20px; line-height: 20px; text-align: center;\">" +
                "简历帮<br/>全国第一款人工智能 “测简历、改简历” 工具" + 
                "</h3>" +
                "<p>" + userEmail + "，你好！我们已经收到您上传的简历，评测系统正在分析你的简历，稍后即可进入评测页面查看详细信息" + "。<br/>" +
                "</p>"+
                "</div>";
        MailUtil.SendHtmlMessage("", userEmail, subject, content);
    }
}
