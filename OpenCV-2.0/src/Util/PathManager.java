package Util;

/*
 * Manage the paths in server
 * user path :     contains dashboard pages and thumbnails of resumes
 * benchmark path: contains competency, presentation and format parts
 * resume path:    contains all resumes uploaded by users
 * template path:  contains dashboard and benchmark HTML page template
 * dict path:      contains all dictionaries used in computing scores of one PDF resume
 */
public class PathManager {
    // The default is: http://opencv.expert
    public static String WebsitePath = "http://localhost:8080/OpenCV-tomcat7.0/";
    //"http://opencv.expert/";
      
    public static String SystemPathSeparator = "\\";
    public static String UserFolder = "user";
    public static String BenchmarkFolder = "benchmark";
    public static String DictFolder = "dict";
    public static String TemplateFolder = "template";
    
	private String ServerPath;
	private String ResumePath;
	
	public PathManager(String path){
		this.ServerPath = path;
		// Resume path in server
		this.ResumePath = "E:\\home\\kevin\\resume";//"/home/ec2-user/resume";
	}
	
	// The path for server
	public String GetTemplateFolderPath(){
		// template�ڷ������е���ʵ·��
		return (this.ServerPath + TemplateFolder);
	}
	
	public String GetDictFolderPath(){
		// dictionary�ڷ������е���ʵ·��
		return (this.ServerPath + DictFolder);
	}
	
	public String GetUserFolderPath(){
		// user�ڷ������е���ʵ·��
		return (this.ServerPath + UserFolder);
	}

	public String GetBenchmarkFolderPath(){
		// benchmark�ڷ������е���ʵ·��
		return (this.ServerPath + BenchmarkFolder);
	}
	
	public String GetResumeFolderPath(){
		// resume�ڷ������е���ʵ·��������������·��Ϊ�˱���PDF2HTML��������
		return this.ResumePath;
	}
	
	/*************************************** The path for contents in web pages ****************************************/
	public static String GetWebSiteUserPath(){
		return (WebsitePath + UserFolder);
	}
	
	public static String GetWebsiteBenchmarkPath(){
		return (WebsitePath + BenchmarkFolder);
	}
	
	public static String GetCSSPath(){
	    return (WebsitePath + "css");
	}
	
	public static String GetJSPath(){
        return (WebsitePath + "js");
    }
	
	public static String GetImagePath(){
        return (WebsitePath + "images");
    }
}
