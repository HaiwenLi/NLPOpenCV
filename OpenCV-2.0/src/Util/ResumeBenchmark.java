package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ResumeBenchmark {
    // User info
	private String Username;
	private String UserEmail;
	private String PDFFilename;
	private BenchmarkScore benchmarkScore;
	private List<CompetencyPageItem>   CompetencyPageItemsInResume;
	private List<PresentationPageItem> PresentationPageItemsInResume;
	
	// Parameters for Format Scoring
	public static int MaxKeySectionNum = 5;
	public static int MinKeySectionNum = 3;
	public static int[] FontSizeRange = {8,15};
	public static int MaxFontFamily = 6;
	
	private int ResumePage;
	private int[] FontRange;
	private double[] PageMargins;
	
	// Parameters for Competency
	public static int BENCHMARK_MIN_WORDS = 9;
	public static int COMPETENCY_TOPNUM = 5;
	public static int WordNumber_Threshold1 = 400;
	public static double Score_Ratio1 = 0.4;
	public static int WordNumber_Threshold2 = 600;
	public static double Score_Ratio2 = 0.7;
	
	// Parameters for Presentation
	public static int RepeatWordsNum = 3;
	public static int Min_WordNum_InLine = 30;
	public static int Max_WordNum_InLine = 80;
	
	// Word Dictionaries
	private Map<String,double[]> KeyWordsDict; // word dictionary
	private List<String> ResumeSectionWordsList;
	private List<Integer> ResumeSectionWhiteList;
	private List<String> AvoidWordsList;
		
	public ResumeBenchmark(String pdfFilename){
		this.Username = "";
		this.UserEmail = "";
		this.PDFFilename = pdfFilename;
		this.benchmarkScore = new BenchmarkScore();
		this.PresentationPageItemsInResume = null;
		this.CompetencyPageItemsInResume = null;
	}
	
	public void setUsername(String name){
		this.Username = name;
	}
	public void setUserEmail(String email){
		this.UserEmail = email;
	}
	
	//=================================== Common Methods ====================================//
	
	// 加载PDF文档
	public PDDocument LoadPdfResume(String filename){
		PDDocument document = null;
		
		if (filename != null && !filename.isEmpty()){
			try{
				document = PDDocument.load(new File(filename));
				this.ResumePage = document.getNumberOfPages();
			} catch (IOException e){
				System.out.println(e.getMessage());
			}
		}
		return document;
	}
	
	public void ClosePdfResume(PDDocument document){
		if (document != null){
			try{
				document.close();
			} catch(IOException e){
				System.out.println(e.getMessage());
			}
		}
	}
	
	// 提取PDF中的文本信息
	public List<String> ExtractTextFromPdf(PDDocument document){
		List<String> resumeText = null;
		
		try{
			TextLocations locations = new TextLocations(document);
			resumeText = locations.ExtractText();
			
			if (DebugConfig.DEBUG_OUTPUT){
			    // Output resume text
			    for (int i=0; i<resumeText.size(); ++i){
			        System.out.println(resumeText.get(i));
			    }
			}
			
			this.FontRange = locations.GetFontRange();
			this.PageMargins = locations.GetPageMargin();
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
		
		return resumeText;
	}
	
	// 加载核心板块字典
	public void LoadSectionDict(String sectionDictPath) throws FileNotFoundException, IOException {
		if (ResumeSectionWordsList == null){
			ResumeSectionWordsList = new ArrayList<String>();
		} else{
			ResumeSectionWordsList.clear();
		}
		
		if (ResumeSectionWhiteList == null){
			ResumeSectionWhiteList = new ArrayList<Integer>();
		} else {
			ResumeSectionWhiteList.clear();
		}
		
		FileInputStream fis = new FileInputStream(sectionDictPath);
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(0);
		    int rowStart = sheet.getFirstRowNum()+1;
		    int rowEnd = sheet.getLastRowNum(); 
		    
		    for (int i=rowStart; i<=rowEnd; ++i){
		    	String word = sheet.getRow(i).getCell(0).getStringCellValue();
		    	int whiteValue =  (int)sheet.getRow(i).getCell(1).getNumericCellValue();
		    	ResumeSectionWordsList.add(word);
		    	ResumeSectionWhiteList.add(whiteValue);
		    }
		    workbook.close();
		    
		    if (DebugConfig.DEBUG_OUTPUT){
		        // Output the dictionary
		        System.out.println("Resume section word list num: " + ResumeSectionWordsList.size());
    		    for (int i=0; i<ResumeSectionWordsList.size(); ++i){
    		    	System.out.println(ResumeSectionWordsList.get(i) + ", " + ResumeSectionWhiteList.get(i));
    		    }
		    }
		} catch (IOException e){
			System.out.println(e.getMessage());
		} finally{
			fis.close();
		}
	}
	
	// Load word dictionary (Excel file)
	public void LoadKeyWordsDict(String dictFilename) throws IOException{		
		// Load word dictionary from the excel file
		if (KeyWordsDict == null){
			KeyWordsDict = new HashMap<String,double[]>();
		} else {
			KeyWordsDict.clear();
		}
		
		FileInputStream fis = new FileInputStream(dictFilename);
		try{
			XSSFWorkbook workbook = new XSSFWorkbook(fis); // Using Apache POI package to read/write excel file
			XSSFSheet sheet = workbook.getSheetAt(0);
			int rowStart = sheet.getFirstRowNum()+2;
			int rowEnd = 460;//sheet.getLastRowNum(); 
			
			for (int r = rowStart; r<=rowEnd; ++r){
				if (sheet.getRow(r) == null){
					continue;
				}
				String keyWord = sheet.getRow(r).getCell(0).getStringCellValue();
				double analytical = sheet.getRow(r).getCell(1).getNumericCellValue();
				double communication = sheet.getRow(r).getCell(2).getNumericCellValue();
				double leadership = sheet.getRow(r).getCell(3).getNumericCellValue();
				double teamwork = sheet.getRow(r).getCell(4).getNumericCellValue();
				double initialtive = sheet.getRow(r).getCell(5).getNumericCellValue();
				
				// Set competency value
				double[] competency_value = new double[5];
				competency_value[0] = analytical;
				competency_value[1] = communication;
				competency_value[2] = leadership;
				competency_value[3] = teamwork;
				competency_value[4] = initialtive;
				KeyWordsDict.put(keyWord, competency_value);
			}
			workbook.close();
			
			if (DebugConfig.DEBUG_OUTPUT){
			    // Output key words dictionary
			    System.out.println("Key words dict length: " + KeyWordsDict.size());
			    Set<String> key_words = KeyWordsDict.keySet();
			    for (String word : key_words){
			        double[] score = KeyWordsDict.get(word);
			        String strScore = score[0] + ", " + score[1] + ", " + score[2] + ", " + score[3] + ", " + score[4];
			        System.out.println(word + ": " + strScore);
			    }
			}
		} finally{
			fis.close();
		}
	}
	
	// 加载避免词汇词库
	public void LoadAvoidWordsDict(String avoidWordsPath) throws FileNotFoundException, IOException{
		if (AvoidWordsList == null){
			AvoidWordsList = new ArrayList<String>();
		} else {
			AvoidWordsList.clear();
		}
		
		FileInputStream fis = new FileInputStream(avoidWordsPath);
		try{
			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheet("Avoided Words");
			
			//获取行开始和结尾
		    int rowStart = 0;
		    int rowEnd = 104;//sheet.getLastRowNum(); 
		    
		    //保存AvoidWords
		    String cellvalueTemp = null;
	        for(int i = rowStart; i <= rowEnd; ++i) {  
	        	if(sheet.getRow(i) != null){
				   cellvalueTemp = sheet.getRow(i).getCell(0).getStringCellValue();
				   AvoidWordsList.add(cellvalueTemp);
			    }       
	        }  
	        workbook.close();
	        
	        if (DebugConfig.DEBUG_OUTPUT){
	            // Output the dictionary
	            System.out.println("Avoid words num: " + AvoidWordsList.size());
    		    for (int i=0; i<AvoidWordsList.size(); ++i){
    		    	System.out.println(AvoidWordsList.get(i));
    		    }
	        }
		} finally{
			fis.close();
		}
	}
	
	// 统计一行简历文本字数（包括标点符号）
	// 注：使用时，加载词库需要～3s！
	public int GetWordNumber(String sentence){
		int wordNum = 0;
		sentence.replace("\\r", "");
		sentence.replace("\\n", "");
		sentence.trim();//remove the start and end blank characters
		
		Pattern numberPattern = Pattern.compile("[a-zA-Z0-9.-]+");
		Pattern zhCharPattern = Pattern.compile("[\u4e00-\u9fa5]");
		
		if (sentence.length()>0){
			Result splitRes = ToAnalysis.parse(sentence);
			List<Term> words = splitRes.getTerms();
			
			for (int i=0; i<words.size(); ++i){
				String natureStr = words.get(i).getNatureStr();
				String word = words.get(i).getName();
				
				if (natureStr.startsWith("w")){ // qualification word
					wordNum += 1;
				}
				else if(numberPattern.matcher(word).find()){
					wordNum += 1;
				}
				else{
					Matcher zhCharMatcher = zhCharPattern.matcher(word);
					while (zhCharMatcher.find()){ wordNum += 1; }
				}
			}
		}
		return wordNum;
	}
	
	//=================================== Format Scoring ====================================//
	// 在使用前需要加载词库
	
	public int GetKeySectionNumber(List<String> resumeText){
		int keySectionNum = 0;
		String zh_words = "[^\u4e00-\u9fa5]";
		
		if (!resumeText.isEmpty() && !ResumeSectionWordsList.isEmpty()){
			for (int i=0; i<resumeText.size(); ++i){
				String zhText = resumeText.get(i).replaceAll(zh_words,""); //extract all Chinese characters
				
				// Use length to determine the beginning of one section
				if (zhText.length()>=2 && zhText.length()<=9){
					int textIndex = ResumeSectionWordsList.indexOf(zhText);
					if (textIndex >= 0){
						keySectionNum += 1;
					}
				}
			}
		}
		return keySectionNum;
	}
	
	// 检查简历中的字体名和字体大小
	public boolean CheckFontNames_Sizes(PDDocument document){
		if (document != null){
			Set<String> fontNames = new HashSet<String>();
			this.ResumePage = document.getNumberOfPages();
			
			try{
				for (int i=0; i<this.ResumePage; ++i){
					PDPage page = document.getPage(i);
					PDResources resources = page.getResources();
					
					// Read resources in each page
					for (COSName key: resources.getFontNames()){
						PDFont font = resources.getFont(key);
						PDFontDescriptor fontDescriptor = font.getFontDescriptor();
						fontNames.add(fontDescriptor.getFontName());
						//fontSizes.add((double)fontDescriptor.getAverageWidth());
					}
				}
			} catch (IOException e){
				e.printStackTrace();
			}
			
			//  && (this.FontRange[1] <= FontSizeRange[1] && this.FontRange[0] >= FontSizeRange[0]
			// 因为字体大小目前获取不太准确，初期先不用考虑
			return (fontNames.size()<= MaxFontFamily);
		}
		return false;
	}
	
	// 检查简历页边距是否满足标准
	public boolean CheckPageMargins(){
		// 默认满足，后期可进行更新
		return true;
	}
		
	// 检查简历中是否存在图片
	public int CheckPageContainsPicture(PDDocument document){
		int pictureNum = 0; 
		
		if (document != null){
			int pageNum = document.getNumberOfPages();
			for( int i = 0; i < pageNum; ++i){  
			    PDPage page = document.getPage(i);
			    if( null != page ){
			        PDResources resource = page.getResources();
			        for (COSName key: resource.getXObjectNames()){
			        	if (resource.isImageXObject(key)){
			        		pictureNum += 1; 
			        	}
			        }
			    }  
			}
		}
		return pictureNum;
	}
		
	// 检查简历中是否存在表单
	public boolean CheckPageContainsForm(PDDocument document){
		boolean hasForm = false;
		if (document != null){
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			PDAcroForm acroForm = catalog.getAcroForm();
			hasForm = (acroForm != null && acroForm.getFields().size()>0);
		}
		return hasForm;
	}
	
	// 检查简历中是否存在表格
	public boolean CheckPageContainsTable(PDDocument document){
		// 默认不存在表格。尽管Word通过表格建立，但是在PDF中会丢失表格标签，因此难以判断
		// 后期可根据简历中文本的位置，判断PDF简历中是否存在多栏
		return false;
	}
	
	// 计算Format分数
	public double[] ComputeFormatScoreInResume(PDDocument document, List<String> resumeText){
		double [] score = new double[5];
		for (int i=0; i<5; ++i){
			score[i] = 0;
		}
				
		// Format 1, pages number
		if (ResumePage == 1){
			score[0] = 1;
		}
		
		// Format 2, standards
		if (CheckFontNames_Sizes(document)){
			score[1] = 1;
		}
			
		// Format 3, margins
		if (CheckPageMargins()){
			score[2] = 1;
		}
		
		// Format 4, key sections
		int keySectionsNum = GetKeySectionNumber(resumeText);
		if (keySectionsNum <= MaxKeySectionNum && keySectionsNum >= MinKeySectionNum){
			score[3] = 1;
		}
		
		// Format 5
		boolean hasPic = (CheckPageContainsPicture(document) >= 1);
		boolean hasForm = CheckPageContainsForm(document);
		boolean hasTable = CheckPageContainsTable(document);
		if (!(hasPic || hasForm || hasTable)){
			score[4] = 1;
		}
		benchmarkScore.setFormatScore(score); // 保存格式分数
		
		return score;
	}
	
	//=================================== Presentation Scoring ====================================//	
	// 在使用前需要加载词库
	
	//检查一行简历文本的第一个词是否为动词
	public boolean CheckFirstWordAttr(String sentence){
		if (sentence.length()<=0){
			return false;
		}
		
		Result splitRes = ToAnalysis.parse(sentence);
		List<Term> words = splitRes.getTerms();
		if (words.isEmpty()){
			return false;
		}
		else{
		    if (DebugConfig.DEBUG_OUTPUT){
    		    // Output words attributes
    		    for (Term term : words){
    		        System.out.println(term.getName() + ": " + term.getNatureStr());
    		    }
		    }
		    
			String natureStr = words.get(0).getNatureStr();
			if (natureStr == "v"){ // verb, has 9 classes
				return true;
			} else { return false; }
		}
	}
	
	// 检查一行简历文本中是否存在量化
	public boolean CheckQuantizationInLine(String sentence){
		if (sentence.length()<=0) {
			return false;
		}
		Result splitRes = ToAnalysis.parse(sentence);
		List<Term> words = splitRes.getTerms();
		if (words.isEmpty()){
			return false;
		}
		else{
			for (int i=0; i<words.size(); ++i){
				String natureStr = words.get(i).getNatureStr();
				if (natureStr == "m"){ // qualification word
					return true;
				}
			}
			return false;
		}
	}
	
	// 检查一行文本是否使用了避免词。在使用前是需要先加载AvoidWords词库！
	public boolean CheckAvoidWords(String ReadLine){
		boolean hasAvoidWords = false;
		if (AvoidWordsList != null && !AvoidWordsList.isEmpty()){
			int AvoidWordslength = AvoidWordsList.size();
			for(int i=0;i<AvoidWordslength;++i){
				if(ReadLine.contains(AvoidWordsList.get(i))){
					hasAvoidWords = true;
					break;
				}
			}
		}
		return hasAvoidWords;
	}
	
	// 检查一行文本中是否出现多次出现关键词
	public boolean CheckRepeatWords(String strLine, List<String> repeatWordsList){
		boolean hasRepeatWords = false;

		if (!KeyWordsDict.isEmpty()){
			Set<String> KeyWordsList = KeyWordsDict.keySet();
			
			// Cut the line
			Result splitLineRes = ToAnalysis.parse(strLine);
			List<Term> words = splitLineRes.getTerms();
			
			Map<String, Integer> repeatWords = new HashMap<String, Integer>();
			for(int i=0; i<words.size(); ++i){
				String strWord = words.get(i).getName();
				if (KeyWordsList.contains(strWord)){
					if (repeatWords.containsKey(strWord)){
						int repeattime = (Integer) repeatWords.get(strWord);
						repeatWords.put(strWord,repeattime+1);
					}
				}
			}
			
			// Check the repeatWords. If one key word repeats > 3 times, then return false
			Set<String> _repeatWords = repeatWords.keySet();
			repeatWordsList.clear();
			for (String word : _repeatWords){
			    int times = repeatWords.get(word);
			    if( times > RepeatWordsNum){
			        hasRepeatWords = true;
			        repeatWordsList.add(word);
			    }
			}
		}
	
		return hasRepeatWords;
	}
	
	/*
	 *  提取简历中经历板块
	 *  Parameter:
	 *  resumeText: all line texts in resume
	 */
	public List<String> GetExperienceSection(List<String> resumeText){
		List<String> experienceSection = new ArrayList<String>();
		String zh_words = "[^\u4e00-\u9fa5]";
		boolean record = false;
		
		if (!resumeText.isEmpty() && !ResumeSectionWordsList.isEmpty()){
			for (int i=0; i<resumeText.size(); ++i){
				String zhText = resumeText.get(i).replaceAll(zh_words,""); //extract all Chinese characters
				
				// Use length to determine the beginning of one section
				if (zhText.length()>=2 && zhText.length()<=BENCHMARK_MIN_WORDS){
					int textIndex = ResumeSectionWordsList.indexOf(zhText);
					if (textIndex >= 0){
						if ((zhText.contains("经历")) || zhText.contains("实践")){
							if (!zhText.contains("教育")){
								record = true;
							} else{ record = false; }
						} else { record = false; }
					} 
				}
				else if (record){
					String strLine = resumeText.get(i);
					strLine = strLine.replaceAll("[\\f\\n\\r\\t\\v]+",""); // remove all empty character except for space
					strLine = strLine.trim().replaceAll(" +", "");// 去除连续空格仅保留一个空格
					if (strLine.length() > 0){
						experienceSection.add(strLine);
					}
				}
			}
		}
		return experienceSection;
	}
	
	// 提取简历中的经历板块，循环经历板块的所有行，计算最终得分
	public double[] ComputePresentationScoreInResume(List<String> experienceSection){
		double[] score = new double[5];
		int[] lineScore = new int[5];
		int lineNum = 0;
		ArrayList<String> repeatWordsList = new ArrayList<String>();
		
		// Initialize the score
		for (int i=0; i<5; ++i){
			score[i] = 0;
			lineScore[i] = 0;
		}
		
		// 记录presentation打分中过程状态
		if (this.PresentationPageItemsInResume == null){
			this.PresentationPageItemsInResume = new ArrayList<PresentationPageItem>();
		} else{
			this.PresentationPageItemsInResume.clear();
		}
		
		for(int i=0; i<experienceSection.size(); ++i){
		    repeatWordsList.clear();
			String strLine = experienceSection.get(i);
			
			if (strLine.length()>BENCHMARK_MIN_WORDS){
				lineNum += 1;
				
				// Presentation 1, check whether the first word is a verb
				if (CheckFirstWordAttr(strLine)){
					score[0] += 1;
					lineScore[0] = 1;
				}
				
				// Presentation 2, check whether use numbers to describe work
				if (CheckQuantizationInLine(strLine)){
					score[1] += 1;
					lineScore[1] = 1;
				}
				
				// Presentation 3, check the length of line
				int word_num = GetWordNumber(strLine);
				if ((word_num >= Min_WordNum_InLine) && (word_num <= Max_WordNum_InLine)){
					score[2] += 1;
					lineScore[2] = 1;
				}
				
				// Presentation 4, check whether use avoid words
				if (CheckAvoidWords(strLine)){
					score[3] += 1;
					lineScore[3] = 1;
				}
				
				// Presentation 5, check whether repeat key words
				if (CheckRepeatWords(strLine,repeatWordsList)){
					score[4] += 1;
					lineScore[4] = 1;
				}
				
				PresentationPageItem pageItem = new PresentationPageItem();
				pageItem.setItemText(strLine);
				pageItem.setScore(lineScore);
				pageItem.setRepeatWordsList(repeatWordsList);
				this.PresentationPageItemsInResume.add(pageItem);
			}
		}
		
		// Compute the presentation score
		if (lineNum != 0){
			for (int i=0; i<score.length; ++i){
				score[i] = score[i]/(1.0*lineNum);
			}
		} else{
			for (int i=0; i<score.length; ++i){
				score[i] = 0;
			}
		}
		benchmarkScore.setPresentationScore(score); // 保存Presentation分数
		
		return score;
	}
	
	//=================================== Competency Scoring ====================================//
	// 进行能力打分时，需要先加载词库
	
	// Search key word in one line of resume
	public void SearchWordSequenceInLine(String strLine, ArrayList<String> wordSeq){
		wordSeq.clear();
		Set<String> keyWords = this.KeyWordsDict.keySet();
		Iterator<String> iter = keyWords.iterator();
		
		while (iter.hasNext()){
			String word = iter.next();
			// remove the repeated key word
			if (strLine.indexOf(word)>=0 && !wordSeq.contains(word)){
				wordSeq.add(word);
			}
		}
	}
	
	// Compute five scores of competency in each line
	public double[] ComputeCompetencyScoreInLine(String strLine){
		ArrayList<String> words = new ArrayList<String>();
		double [] score = new double[5];
		for (int i=0; i<5; ++i){
			score[i] = 0;
		}
		
		SearchWordSequenceInLine(strLine,words);
		for (int i=0; i<words.size(); ++i){
			double[] competency_score = this.KeyWordsDict.get(words.get(i));
			for (int j=0; j<competency_score.length; ++j){
				score[j] += competency_score[j];
			}
			if (DebugConfig.DEBUG_OUTPUT){
                System.out.println(words.get(i) + ": (" + competency_score[0] + ", " + competency_score[1] + ", " + 
                        competency_score[2] + ", " + competency_score[3] + ", " + competency_score[4] + ")");
	        }
		}
		
		return score;
	}
	
	// Compute the final score of one resume
	public double[] ComputeResumeCompetencyScore(List<String> lines){
		double[] final_score = new double[5];
		int wordNumber = 0;
		
		// 记录Competency打分中过程状态
		if (this.CompetencyPageItemsInResume == null){
			this.CompetencyPageItemsInResume = new ArrayList<CompetencyPageItem>();
		} else{
			this.CompetencyPageItemsInResume.clear();
		}
		
		for (int i = 0; i<lines.size(); ++i){
			String line = lines.get(i);
			line = line.replaceAll("[\\f\\n\\r\\t\\v]+",""); // remove all empty character except for space
			line = line.trim().replaceAll(" +","");          // remove continuous spaces
			if (line.length()<=BENCHMARK_MIN_WORDS){
				continue;
			}
			double[] score = ComputeCompetencyScoreInLine(line);
			
			final_score[0] += score[0];
			final_score[1] += score[1];
			final_score[2] += score[2];
			final_score[3] += score[3];
			final_score[4] += score[4];
			int line_words_length = GetWordNumber(line);
			wordNumber += line_words_length;
			
			// save the competency item
			CompetencyPageItem pageItem = new CompetencyPageItem();
			pageItem.setItemText(line);
			pageItem.setScore(score);
			CompetencyPageItemsInResume.add(pageItem);
		}
		
		double ratio = 1.0;
		if (wordNumber == 0){
			wordNumber = WordNumber_Threshold1;
			ratio = 0;
		}
		else if( wordNumber <= WordNumber_Threshold1){
			ratio = Score_Ratio1;
		}
		else if(wordNumber <= WordNumber_Threshold2){
			ratio = Score_Ratio2;
		}
		final_score[0] = final_score[0]*ratio/wordNumber;
		final_score[1] = final_score[1]*ratio/wordNumber;
		final_score[2] = final_score[2]*ratio/wordNumber;
		final_score[3] = final_score[3]*ratio/wordNumber;
		final_score[4] = final_score[4]*ratio/wordNumber;
		benchmarkScore.setCompetencyScore(final_score);; // 保存Competency分数
		
	    return final_score;
	}
	
	//=================================== Computing Benchmark Scores ====================================//
	
	// Compute resume benchmark and return it
	public BenchmarkScore ComputeResumeBenchmarkScore(String serverRootPath){
		this.benchmarkScore.reset();
		String separator = PathManager.SystemPathSeparator;
		
		try{
			PathManager pathManager = new PathManager(serverRootPath);
			String keyWordsDictName = pathManager.GetDictFolderPath() + separator + "keywords_dict.xlsx";
			String avoidWordsDictName = pathManager.GetDictFolderPath() + separator + "keywords_dict.xlsx";
			String sectionDictName = pathManager.GetDictFolderPath() + separator + "section_dict.xlsx";
			
			this.LoadKeyWordsDict(keyWordsDictName);
			this.LoadAvoidWordsDict(avoidWordsDictName);
			this.LoadSectionDict(sectionDictName);
			if (DebugConfig.DEBUG_OUTPUT){
			    System.out.println("Dictionaries have been loaded");
			}
			
			PDDocument doc = LoadPdfResume(this.PDFFilename);
			List<String> resumeText = ExtractTextFromPdf(doc);
			if (DebugConfig.DEBUG_OUTPUT){
			    System.out.println("PDF resume has been opened");
			}
			
			if (resumeText != null && !resumeText.isEmpty()){
				ComputeFormatScoreInResume(doc, resumeText);
				if (DebugConfig.DEBUG_OUTPUT){
				    System.out.println("Finish to compute format score");
				}
				
				List<String> experienceSection = GetExperienceSection(resumeText);
				if (DebugConfig.DEBUG_OUTPUT){
				    // Output the extracted experience section text
    				System.out.println("The experience section of resume text is:");
    				for (int i=0; i<experienceSection.size(); ++i){
    				    System.out.println(experienceSection.get(i));
    				}
				}
				
				ComputePresentationScoreInResume(experienceSection);
				if (DebugConfig.DEBUG_OUTPUT){
				    System.out.println("Finish to compute presentation score");
				}
				
				ComputeResumeCompetencyScore(resumeText);
				if (DebugConfig.DEBUG_OUTPUT){
				    System.out.println("Finish to compute competency score");
				}
			}
			
			ClosePdfResume(doc);
			if (DebugConfig.DEBUG_OUTPUT){
			    System.out.println("PDF resume has been closed");
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
		
		return this.benchmarkScore;
	}
	
	// Generate benchmark page and save as JSP file
	public void GenerateBenchmarkPage(String serverRootPath, long resume_id){
		// 获取benchmark页面路径
	    String separator = PathManager.SystemPathSeparator;
		PathManager pathManager = new PathManager(serverRootPath);
		
		String pageFilename = pathManager.GetBenchmarkFolderPath() + separator + String.valueOf(resume_id) + 
				separator + "benchmark.jsp"; 
		File pageFile = new File(pageFilename);
		if (pageFile.exists()){
			return;
		}
		
		String benchmark_template = pathManager.GetTemplateFolderPath() + separator + "benchmark-template.html";
		String sampleFilename = pathManager.GetDictFolderPath() + separator + "ResumeSamples.xlsx";
		try{
			PageGenerator pageGenerator = new PageGenerator();
			pageGenerator.setUserName(Username);
			pageGenerator.setUserEmail(UserEmail);
			pageGenerator.setBenchmark_Template(benchmark_template);
			pageGenerator.LoadPresentationSamples(sampleFilename); // Load presentation samples
			pageGenerator.setBenchmarkFolder(pathManager.GetBenchmarkFolderPath());
			pageGenerator.setPageNum(this.ResumePage);
			
			String[][] competencyItems = new String[5][COMPETENCY_TOPNUM]; //二维数组，第一维表示不同能力项，第二维表示体现该能力的简历文本
			List<CompetencyItemScore> competency_item_scores = new ArrayList<CompetencyItemScore>();
			
			// Intialize competency items
			for (int i=0; i<5; ++i){
			    for (int j=0; j<COMPETENCY_TOPNUM; ++j){
			        competencyItems[i][j] = "";
			    }
			}
			
			if (DebugConfig.DEBUG_OUTPUT){
			    System.out.println("Output competency items");
			    for (int i=0; i<CompetencyPageItemsInResume.size(); ++i){
			        String text = CompetencyPageItemsInResume.get(i).getItemText();
			        System.out.println(i + ": " + text);
			    }
			}
			
			if (CompetencyPageItemsInResume.size()>0){
    			for (int i=0;i<5;++i){
    				// Find the competency texts
    				competency_item_scores.clear();
    				
    				for (int j=0; j<this.CompetencyPageItemsInResume.size(); ++j){
    					double [] score = this.CompetencyPageItemsInResume.get(j).getScore();
    					competency_item_scores.add(new CompetencyItemScore(j,score[i]));
    				}
    				
    				// 降序排列
    				Collections.sort(competency_item_scores,new Comparator<CompetencyItemScore>(){  
    			            public int compare(CompetencyItemScore arg0, CompetencyItemScore arg1) {  
    			                return arg1.getScore().compareTo(arg0.getScore());
    			            }  
    			       });
    				
    				/*if (DebugConfig.DEBUG_OUTPUT){
    				  System.out.println("Output sorting results: ");
    				  for (int j=0; j<competency_item_scores.size(); ++j){
    				      System.out.println(competency_item_scores.get(j).getId() + ": " + 
    				                         competency_item_scores.get(j).getScore());
    				  }
    				}*/
    				
    				// Select top 3
    				for (int j=0; j<COMPETENCY_TOPNUM; ++j){
    					int match_index = competency_item_scores.get(j).getId();
    					competencyItems[i][j] = CompetencyPageItemsInResume.get(match_index).getItemText();
    					if (DebugConfig.DEBUG_OUTPUT){
    					    System.out.println(match_index + ": " + competencyItems[i][j]);
    					}
    				}
    			}
			}
			
			int[] formatRank = benchmarkScore.rankFormatScore();
			pageGenerator.CreateBenchmarkPage(competencyItems, PresentationPageItemsInResume, formatRank,
					resume_id, pageFilename);
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
