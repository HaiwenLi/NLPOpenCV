package Util;

/*
 * ConvertPDFToHtml class: 采用开源项目 PDF2HTMLEX将PDF简历转化为HTML页面
 */
public class ConvertPDFToHtml {
    public static String PDF2HTMLEX_PATH = "pdf2htmlEX";//"E:\\pdf2htmlEX\\pdf2htmlEX";
    
	// Convert PDF file into HTML page
	public static boolean PDF2HtmlEx(String pdfFilename, String dest_dir, String htmlFilename){
		Runtime runtime = Runtime.getRuntime();
		String cmd = "";
		if (!dest_dir.isEmpty()){
		    cmd = PDF2HTMLEX_PATH + " --dest-dir " + dest_dir +  " " + pdfFilename + " " + htmlFilename;
		} else {
		    cmd = PDF2HTMLEX_PATH + " " + pdfFilename + " " + htmlFilename;
		}
		
		try {
			System.out.println(cmd);
			Process process = runtime.exec(cmd);
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR"); 
			errorGobbler.start();
			StreamGobbler outGobbler = new StreamGobbler(process.getInputStream(), "STDOUT");    
			outGobbler.start();
			
			process.waitFor();
			process.destroy();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		//System.out.println("Successfully convert PDF into HTML file");
	}
}
