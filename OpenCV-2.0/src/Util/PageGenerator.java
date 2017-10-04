package Util;

import bean.resume;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

/*
 * PageGenerator：根据Dashboard & Benchmark模板，针对每一份简历产生JSP文件
 */
public class PageGenerator {
    public static double SIMILARITY_THRESHOLD = 0.75;
    
	/* Presentation samples used in creating presentation benchmark page */
	private List<String> goodPresentationSamples;
	private List<String> badPresentationSamples;
	
	/* Dashboard, Benchmark template filename and upload resume image name */
	private int PageNum;
	private String Dashboard_Template;
	private String Benchmark_Template;
	private String UploadResumeImageName;
	private String Username;
	private String UserEmail;

	/* Path information */
	private String BenchmarkFolder;       // benchmark在服务器中的真实路径
	
	public PageGenerator(){
	    PageNum = -1;
		Dashboard_Template = "";
		Benchmark_Template = "";
		UploadResumeImageName = "";
		BenchmarkFolder = "";
		
		Username = "";
		UserEmail = "";
	}
	
	/*
	 * setter for upload reume image name
	 */
	public void setUploadResumeImageName(String name){
		UploadResumeImageName = name;
	}
	
	public void setPageNum(int num){
	    this.PageNum = num;
	}
	
	/*
	 * getter and setter for benchmark template filename
	 */
	public void setBenchmark_Template(String template){
		this.Benchmark_Template = template;
	}
	public String getBenchmark_Template(){
		return this.Benchmark_Template;
	}
	
	/*
	 * getter and setter for dashboard template filename
	 */
	public void setDashboard_Template(String template){
		this.Dashboard_Template = template;
	}
	public String getDashboard_Template(){
		return this.Dashboard_Template;
	}
	
	/*
	 * setter for Benchmark folder
	 */
	public void setBenchmarkFolder(String path){
		this.BenchmarkFolder = path;
	}
		
	public String GetJSPLabel(){
		return "<%@ page language=\"java\" contentType=\"text/html; charset=utf-8\" pageEncoding=\"utf-8\"%>\n";
	}
	
	public String FilterGeneratedPage(String original_page){
	   // Change Image Path
	    String newPage = original_page.replace("@IMAGE", PathManager.GetImagePath());
	    
	   // Change CSS and JS path
	    newPage = newPage.replace("@CSS", PathManager.GetCSSPath());
	    newPage = newPage.replace("@JS", PathManager.GetJSPath());
	    
	    // Process JSP scripts label
	    newPage = newPage.replace("@LEFT_BRACKET@", "<");
	    newPage = newPage.replace("@RIGHT_BRACKET@", ">");
	    
	    // Process ROOT label
	    return newPage.replace("@ROOT@", PathManager.WebsitePath);
	}
	
	/*
     * getter and setter for user name
     */
	public void setUserName(String name){
		this.Username = name;
	}
	public String getUserName(){
		String usernameInPage = this.Username;
		if (usernameInPage.length()>16){
			usernameInPage = usernameInPage.substring(0, 16);
		}
		return usernameInPage + "&nbsp;<b class=\"caret\">";
	}
	
	/*
     * getter and setter for user email
     */
	public void setUserEmail(String email){
		this.UserEmail = email;
	}
	public String getUserEmail(){
	    return this.UserEmail;
	}

	/*
	 * Load presentation samples from Excel
	 */
	public void LoadPresentationSamples(String sampleFilename) throws FileNotFoundException, IOException{
		goodPresentationSamples = new ArrayList<String>();
		badPresentationSamples = new ArrayList<String>();
		
		FileInputStream fis = new FileInputStream(sampleFilename);// Excel file
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(0);
		    int rowStart = sheet.getFirstRowNum()+1;
		    int rowEnd = sheet.getLastRowNum();
		    
		    for (int i=rowStart; i<=rowEnd; ++i){
		    	goodPresentationSamples.add(sheet.getRow(i).getCell(0).getStringCellValue());
		    	badPresentationSamples.add(sheet.getRow(i).getCell(1).getStringCellValue());
		    	//System.out.println(goodPresentationSamples.get(i-rowStart));
		    }
		    workbook.close();
		} catch (IOException e){
			System.out.println(e.getMessage());
		} finally{
			fis.close();
		}
	}
	
	   /*
     * Extract the Chinese characters, English characters and numbers
     */
    public String WordFilter(String text){
        if (text == null || text.isEmpty()){
            return "";
        }
        String regEx = "[a-zA-Z0-9\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);     
        Matcher m = p.matcher(text);
        StringBuffer strBuffer = new StringBuffer();
        while(m.find()){
            strBuffer.append(m.group());
        }
        return strBuffer.toString();
    }
    
    /*
     * 在refTexts中寻找与tartgetText最相似的文本，并返回在refTexts中的位置
     * 通过寻找字符串之间的最长公共字串来计算文本之间的相似度
     */
    public int FindMatchIndex(String targetText, List<String> refTexts){
        int match_index = -1;
        double text_similarity = -1;
        
        for (int i=0; i<refTexts.size(); ++i){
            if (refTexts.get(i).isEmpty()){
                continue;
            }
            String refText = refTexts.get(i);
            double similarDegree = TextSimilarity.SimilarDegree(refText, targetText);
            if ( similarDegree > text_similarity){
                text_similarity = similarDegree;
                match_index = i;
            }
        }
        
        if (text_similarity > SIMILARITY_THRESHOLD){
            return match_index;
        } else{
            return -1;
        }
    }
    
	/*
	 * Generate HTML version node about thumbnail of resume
	 */
	public String GenerateOneThumbnailElement(String image_name, String update_time, int resumeIndex){
		String element = "<div class=\"thumbnail\" data-resumeIndex=\"" + String.valueOf(resumeIndex) + "\">\n" +
						 "    <div class=\"resume-page\">\n" +
						 "        <img class=\"resume-thumbnail\" src=\"" + image_name + "\"></img>\n"+
						 "            </div>\n"+
						 "    <div class=\"resume-status\">\n"+
						 "        <span class=\"lang\"><b class=\"lang-type\">语言：</b><i class=\"lang-icon\">中文</i></span>\n" +
						 "        <span class=\"status\"><i class=\"color-good fa fa-check-circle-o\"></i>&nbsp;已生成</span>\n" +
						 "    </div>\n"+
						 "    <p class=\"update-time\">更新于：" + update_time + "</p>\n" +
						 "</div>\n";
		return element;
	}
	
	/*
	 *  Create dashboard page
	 */
	public void CreateDashboardPage(List<resume> resumeList, String dashboardScoresForClient, String outputName) throws IOException{
		if (resumeList == null || resumeList.isEmpty()){
			return;
		}
		
		// 首先需要设定template的路径 
		String template = this.Dashboard_Template;
		String addButtonImage = this.UploadResumeImageName;
		File file = new File(template);
		Document doc = Jsoup.parse(file, "UTF-8");
		
		// Create thumbnail list contents，there 3 images in one row
		int num = resumeList.size();
		int rows = (int)Math.floor(num/3);
		int remains = num - rows*3;
		long resume_id = -1;
		long user_id = resumeList.get(0).getUserid();
		String thumbnail_list = "";
		
		for (int i=0; i<rows; ++i){
			if (i == 0){
				thumbnail_list +=  "<div class=\"item active\">\n" +
								   "    <div class=\"row\">\n" ;
			}
			else{
				thumbnail_list +=  "<div class=\"item\">\n" +
						   "    <div class=\"row\">\n" ;
			}
			for (int j=0; j<3; ++j){
				// 获取在页面显示时，简历缩略图(小)所在路径
				resume_id = resumeList.get(i*3 + j).getId();
				String thumbnail_name = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) + "/thumbnail/resume-" + 
				        String.valueOf(resume_id) + ".png";
				String update_time = resumeList.get(i*3 + j).getDate();
				
				thumbnail_list +=	"         <div class=\"col-md-4\">\n" +
							        "             " + GenerateOneThumbnailElement(thumbnail_name, update_time, i*3+j) +
							        "         </div>\n";
			}
			thumbnail_list +=  "    </div>\n"+
							   "</div>\n";
		}
		
		// Add remain contents
		if (remains > 0){
			if (rows == 0){
				thumbnail_list +=  "<div class=\"item active\">\n" +
						           "    <div class=\"row\">\n"; 
			}
			else{
				thumbnail_list +=  "<div class=\"item\">\n" +
						           "    <div class=\"row\">\n" ;
			}
			for (int i=0; i<remains; ++i){
				// 获取在页面显示时，简历缩略图(小)所在路径
				resume_id = resumeList.get(rows*3 + i).getId();
				String thumbnail_name = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) + "/thumbnail/resume-" +
                        String.valueOf(resume_id) + ".png";
				String update_time = resumeList.get(rows*3 + i).getDate();
				
				thumbnail_list +=	"         <div class=\"col-md-4\">\n" +
							        "             " + GenerateOneThumbnailElement(thumbnail_name, update_time, rows*3+i) +
							        "         </div>\n";
			}
			// Add button image in the same row
			thumbnail_list +=  "         <div class=\"col-md-4\">\n"+
			                   "             <div id=\"uploadResumeLink\" class=\"thumbnail\">\n"+
			                   "                 <div class=\"resume-page\" style=\"background-color: transparent;padding-top: 30px\">\n" +
							   "			         <img class=\"resume-thumbnail\" src=\""+ addButtonImage + "\"></img>\n" +
							   " 		         </div>\n" +
							   " 		     </div>\n" +
			                   "         </div>\n";
			thumbnail_list +=  "    </div>\n"+
					           "</div>\n";
		} else{
			// Add button image in new row
			if (rows == 0){
				thumbnail_list +=  "<div class=\"item active\">\n" +
						           "    <div class=\"row\">\n"; 
			}
			else{
				thumbnail_list +=  "<div class=\"item\">\n" +
						           "    <div class=\"row\">\n" ;
			}
			thumbnail_list +=  "         <div class=\"col-md-4\">\n"+
			                   "             <div id=\"uploadResumeLink\" class=\"thumbnail\">\n"+
			                   "                 <div class=\"resume-page\" style=\"background-color: transparent;padding-top: 30px\">\n" +
							   "			         <img class=\"resume-thumbnail\" src=\""+ addButtonImage + "\"></img>\n" +
							   " 		         </div>\n" +
							   " 		     </div>\n" +
			                   "         </div>\n";
			thumbnail_list +=  "    </div>\n"+
					           "</div>\n";
		}
		
		// Create thumbnail large list contents
		String thumbnail_large_list = "";
		for (int i=0; i<num; ++i){
			// 获取在页面显示时，简历缩略图(大)所在路径
			resume_id = resumeList.get(i).getId();
			String thumbnail_large_name = PathManager.GetWebSiteUserPath() + "/" + String.valueOf(user_id) + "/thumbnail-large/resume-" +
                    String.valueOf(resume_id) + ".png";
			if (i == 0){
				thumbnail_large_list += "<img class=\"thumbnail-large\" src=\"" + thumbnail_large_name + "\"></img>\n";
			}
			else{
				thumbnail_large_list += "<img class=\"thumbnail-large hide\" src=\"" + thumbnail_large_name + "\"></img>\n";
			}
		}
		
		// Add user name in page
		String user_name = getUserName();
		doc.getElementById("username").html(user_name);
		doc.getElementById("UserEmail").text(this.UserEmail);
		
		// Add the created content into HTML page
		Elements thumbnail_list_div = doc.select("div.carousel-inner");
		thumbnail_list_div.get(0).append(thumbnail_list);
		Element thumbnail_large_div = doc.getElementById("uploaded-resumes-large");
		thumbnail_large_div.append(thumbnail_large_list);
		
		// Insert dashboard score (client version) into page
		doc.getElementById("DashboardItems").text(dashboardScoresForClient);
		
		// Save the page into file
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputName),"UTF-8");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(this.GetJSPLabel() + FilterGeneratedPage(doc.toString()));
        bw.flush();
        bw.close();
	}
	
	/*
	 * Generate presentation label element
	 * pre_item_score: the score for one experience text, the value of the score is 0/1
	 * label_index:    the label for highlight
	 */
	public String GeneratePresentationLabelElement(int [] pre_item_score, int label_index, List<String> repeatWords){
		String presentationLabel = "", state = "", color = "";
		int goodNum = 0;
		String repeatWordsText = "";
		if (repeatWords != null){
    		for (int i=0; i<repeatWords.size()-1;++i){
    		    repeatWordsText += (repeatWords.get(i) + "、");
    		}
    		repeatWordsText += repeatWords.get(repeatWords.size()-1);
		}
		
		for (int i=0; i<pre_item_score.length; ++i){
			goodNum += pre_item_score[i];
		}
		
		if (goodNum == 5){
			state = "很好";
			color = "color-good";
		}
		else if( goodNum >=2 ){
			state = "一般";
			color = "color-soso";
		}
		else{
			state = "需要提升";
			color = "color-bad";
		}
		
		presentationLabel += "<div class=\"group\" data-id=\"presentation-" + String.valueOf(label_index) + "\">\n"+
							 "    <div class=\"header addline\">\n"+
							 "        <p class=\"presentation-header " + color + "\">" + state + "</p>\n"+
							 "        <span class=\"next-icon fa fa-chevron-down\"></span>\n"+
							 "    </div>\n"+
							 "    \n"+
							 "    <ul class=\"items-nav\" style=\"list-style: none; padding: 0\">\n"+
							 "        <p class=\"summary-text-presentation\">提升意见</p>\n";
		if (pre_item_score[0] == 1){
			presentationLabel += "        <li><span class=\"fa fa-check color-good\"></span><p class=\"text\">使用了强有力的动词领导本句；句子语态积极</p></li>\n";
		} else{
			presentationLabel += "        <li><span class=\"fa fa-close color-bad\"></span><p class=\"text\">建议本句第一个词用强有力的动词，"+
		                         "体现出积极的语态</p></li>\n";
		}
		
		if (pre_item_score[1] == 1){
			presentationLabel += "        <li><span class=\"fa fa-check color-good\"></span><p class=\"text\">使用了数字量化</p></li>\n";
		} else{
			presentationLabel += "        <li><span class=\"fa fa-close color-bad\"></span><p class=\"text\">建议在本句中通过数字来量化工作、挑战或成果</p></li>\n";
		}
		
		if (pre_item_score[2] == 1){
			presentationLabel += "        <li><span class=\"fa fa-check color-good\"></span><p class=\"text\">句子长度合适</p></li>\n";
		} else{
			presentationLabel += "        <li><span class=\"fa fa-close color-bad\"></span><p class=\"text\">建议每句不超过2行，控制在30-80个字</p></li>\n";
		}
		
		if (pre_item_score[3] == 1){
			presentationLabel += "        <li><span class=\"fa fa-check color-good\"></span><p class=\"text\">文体专业，句子简洁有力</p></li>\n";
		} else{
			presentationLabel += "        <li><span class=\"fa fa-close color-bad\"></span><p class=\"text\">简历是专业文本，建议不要使用口语体："+
		                         "避免我、多次、良好、积极、圆满、善于、一致好评等拉低分数的词汇</p></li>\n";
		}
		
		if (pre_item_score[4] == 1){
			presentationLabel += "        <li style=\"border-width: 0\"><span class=\"fa fa-check color-good\"></span><p class=\"text\">没有单纯复述工作职责，更体现了做了什么</p></li>\n";
		} else{
			presentationLabel += "        <li style=\"border-width: 0\"><span class=\"fa fa-close color-bad\"></span><p class=\"text\">建议避免在简历中使用关键词汇多次"+
		                         "本句如下词汇在全文中重复出现：" + repeatWordsText + "</p></li>\n";
		}

		if (goodNum == 5){
			presentationLabel += "    </ul>\n" +  "</div>\n";
		}
		else {
			// Randomly select one presentation sample from both good and bad presentation samples
	        Random random = new Random();
	        int max = goodPresentationSamples.size() - 1, min = 0;
			int randIndex = random.nextInt(max)%(max-min+1) + min;
			String goodStatement = goodPresentationSamples.get(randIndex);
			String badStatement = badPresentationSamples.get(randIndex);
			
			presentationLabel += "    </ul>\n"+
					             "    \n"+
					             "    <div class=\"samples\">\n"+
					             "        <p class=\"samples-header color-good\">优秀表达</p>\n"+
					             "            <li style=\"border-width: 0\">" + goodStatement + "</li>\n"+
								 "        </ul>\n"+
								 "    </div>\n"+
								 "    \n"+
								 "    <div class=\"samples\">\n"+
								 "        <p class=\"samples-header color-bad\">错误表达</p>\n"+
								 "        <ul class=\"samples-list\">\n"+
								 "            <li style=\"border-width: 0\">" + badStatement + "</li>\n"+
								 "        </ul>\n"+
								 "    </div>\n"+
								 "</div>\n";
		}
		
		return presentationLabel;
	}
		
	/* 生成Benchmark页面
	 * 参数说明：
	 * competencyItems   : 对应能力五项内容的文本
	 * presentationItems : 测评的表达项，包括表达项文本和单项分数，每个分数取值为0/1；根据每项正确数目设置好，一般和不好
	 * formatRank        : 格式五项内容分数，每个分数取值0/1
	 */
	public void CreateBenchmarkPage(String[][] competencyItems, List<PresentationPageItem> presentationItems, 
			                        int[] formatRank, long resume_id, String outputName) throws IOException {
		String template = this.Benchmark_Template;
		
		File file = new File(template);
		Document doc = Jsoup.parse(file, "UTF-8");
				
		// Add format content
		String[] Format_Group = {"format-pages", "format-standards", "format-margins",
				"format-keysections", "format-concise"};
		String[] Format_Advices = {
				// format pages advice
				"<ul class=\"items-nav\"\n>"+
	            "    <p class=\"summary-text color-bad\">提升意见</p>\n"+
	            "    <li style=\"border-width: 0\">建议简历控制在一页</li>\n"+
                "</ul>\n",
                
                // format pages advice
				"<ul class=\"items-nav\">\n" +
				"	<p class=\"summary-text color-bad\">提升意见</p>\n" +
				"	<li style=\"border-width: 0\">建议统一格式，字号在11号上下，字体统一</li>\n" +
				"</ul>\n",
				
				// format margins advice
				"<ul class=\"items-nav\">\n" +
				"	<p class=\"summary-text color-bad\">提升意见</p>\n" +
				"	<li style=\"border-width: 0\">建议保持一定的页边距，不至于页面拥挤或疏松</li>\n" +
				"</ul>\n",
				
				// format key sections advice
				"<ul class=\"items-nav\">\n" +
				"	<p class=\"summary-text color-bad\">提升意见</p>\n" +
				"	<li style=\"border-width: 0\">建议有教育、经历、其他3个板块</li>\n" +
				"</ul>\n",
				
				// format concise advice
				"<ul class=\"items-nav\">\n" +
				"	<p class=\"summary-text color-bad\">提升意见</p>\n" +
				"	<li style=\"border-width: 0\">建议不适用太多格式，不用表格、不添加图片、不分栏</li>\n" +
				"</ul>\n"
		};
		String[] Format_Element_Header = {"format-pages-header", "format-standards-header", "format-margins-header",
				"format-keysections-header", "format-concise-header"};
		
		for (int i=0; i<5; ++i){
			Elements items = doc.select("div[data-id=\"" + Format_Element_Header[i] + "\"] > span");
			if (formatRank[i] == 1){
				for (int j=0; j<items.size(); ++j){
					if (items.get(j).hasClass("icon-img")){
						items.get(j).removeClass("background-bad");
						items.get(j).addClass("background-good");
						break;
					}
				}
			}
			else{
				Elements format_group_divs = doc.select("div[data-id=\""+ Format_Group[i] + "\"]");
				Element format_group_div = format_group_divs.first();
				
				for (int j=0; j<items.size(); ++j){
					if (items.get(j).hasClass("icon-img")){
						items.get(j).removeClass("background-good");
						items.get(j).addClass("background-bad");
						break;
					}
				}
				format_group_div.append(Format_Advices[i]);
			}
		}
		
		/* Add highlight labels in HTML version resume */
		String htmlFilename = BenchmarkFolder + PathManager.SystemPathSeparator + String.valueOf(resume_id) + 
		        PathManager.SystemPathSeparator + "resume.html";
		File htmlResumeFile = new File(htmlFilename);
		
		// 判断HTML版本简历是否存在
		if (htmlResumeFile.exists()){
			// If the HTML version resume exists
			Document htmlDoc = Jsoup.parse(htmlResumeFile, "UTF-8");
			htmlDoc.outputSettings().prettyPrint(false);
			htmlDoc.outputSettings().indentAmount(0);
			Elements style_elements = htmlDoc.getElementsByTag("style");
			Element html_resume_content = htmlDoc.getElementById("page-container");
			html_resume_content.attr("data-pagenum",String.valueOf(PageNum));
			
			// Insert the pdfFont into benchmark page			
			doc.head().append(style_elements.get(0).toString());
			
			// Extract line text from HTML version resume text
			List<String> htmlResumeLineText = new ArrayList<String>();
			Elements html_resume_divs = html_resume_content.select("div");
			ArrayList<Element> highlightElement = new ArrayList<Element>();
			
			// 遍历HTML简历中的所有DIV标签，提取每一行的简历文本
			for (int i=0; i<html_resume_divs.size(); ++i){
				Element text_element = html_resume_divs.get(i);
				
				if (text_element.hasClass("t")){
					// Extract all text in DIV element
					String lineText = text_element.text();
					String filtered_lineText = WordFilter(lineText);
					if (filtered_lineText.length()>ResumeBenchmark.BENCHMARK_MIN_WORDS){
						htmlResumeLineText.add(filtered_lineText);
						highlightElement.add(text_element);
					}
				}
			}
			
			if (DebugConfig.DEBUG_OUTPUT){
    			System.out.println("HTML line texts are: ");
    			for (int i=0; i<htmlResumeLineText.size(); ++i){
    			    System.out.println(htmlResumeLineText.get(i));
    			}
			}
			
			// Add competency highlights into HTML version resume
			// IMP: the length of competencyItems must be 5
			// We just highlight the sentence with more than BENCHMARK_MIN_WORDS words
			String[] CompetencyValue = {"analytical", "leadership", "teamwork", "communication", "initiative"};
			for (int i=0; i<competencyItems.length; ++i){
			    String[] competencyText = competencyItems[i];
				
				// 在HTML简历中匹配每一项能力
				for (int j=0; j<competencyText.length; ++j){
					String t = WordFilter(competencyText[j]);
					if (t.length()>ResumeBenchmark.BENCHMARK_MIN_WORDS){
    					int match_index = FindMatchIndex(t,htmlResumeLineText);
    					if (match_index != -1){
    					    if (DebugConfig.DEBUG_OUTPUT){
    					        System.out.println(CompetencyValue[i] + ", " + match_index + ": " + htmlResumeLineText.get(match_index));
    					    }
        					highlightElement.get(match_index).attr("data-"+CompetencyValue[i], String.valueOf(j));
    					}
					}
				}
			}
			
			// Create presentation contens
			ArrayList<PresentationPageItem> filteredPresentationItems = new ArrayList<PresentationPageItem>();
			int presentation_num = 0;
			for (int i=0; i<presentationItems.size(); ++i){
			    // Add presentation highlights into HTML version resume
				String t = WordFilter(presentationItems.get(i).getItemText());
				if (t.length()>ResumeBenchmark.BENCHMARK_MIN_WORDS){
    				int match_index = FindMatchIndex(t,htmlResumeLineText);
    				if (match_index != -1){
    				    if (DebugConfig.DEBUG_OUTPUT){
                            System.out.println("Presentation-" + i + ", " + match_index + ": " + htmlResumeLineText.get(match_index));
                        }
    				    filteredPresentationItems.add(presentationItems.get(i));
    				    highlightElement.get(match_index).attr("data-presentation", String.valueOf(presentation_num));
    				    ++presentation_num;
    				}
				}
			}
			
			if (DebugConfig.DEBUG_OUTPUT){
			    System.out.println("Presentation Highlighting...");
			    for (int i=0; i<filteredPresentationItems.size(); ++i){
			        System.out.println(i + ": " + filteredPresentationItems.get(i).getItemText());
			    }
			}
			
		    // Create presentation contents
	        String presentationLabelList = "";
	        for (int i=0; i<filteredPresentationItems.size(); ++i){
	            List<String> repeatWordsList = filteredPresentationItems.get(i).getRepeatWordsList();
	            presentationLabelList += GeneratePresentationLabelElement(filteredPresentationItems.get(i).getScore(), i, repeatWordsList);
	        }
	        
	        /* Add the created content into HTML page */
	        Elements presentation_wrapper_div = doc.select("div[data-id=\"presentation-wrapper\"]");
	        presentation_wrapper_div.get(0).append(presentationLabelList);
	        presentation_wrapper_div.attr("data-prenum", String.valueOf(filteredPresentationItems.size()));
			
			// Insert HTML resume into benchmark page
			Element benchmark_resume_div = doc.getElementById("resume-wrapper");
			benchmark_resume_div.html(html_resume_content.toString());
		}
		
		// Add user name in page
		doc.getElementById("username").html(getUserName());
		doc.getElementById("UserEmail").text(getUserEmail());
        
		// Set hTML output mode
		doc.outputSettings().prettyPrint(false);
		doc.outputSettings().indentAmount(0);
		
		// Save the page into file
		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputName),"UTF-8");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(this.GetJSPLabel() + FilterGeneratedPage(doc.toString()));
        bw.flush();
        bw.close();
	}
}

