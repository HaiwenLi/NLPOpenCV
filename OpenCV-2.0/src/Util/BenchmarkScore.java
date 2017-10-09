package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

/*
 * BenchamarkScore class: save and process competency, presentation and format scores of one resume
 */
public class BenchmarkScore {
	/* Basic info */
    public static int MIN_SCORE = 25; // Must be less than 50, all scores less than this will be treated as this value
	private long user_id;
	private String username;
	private long resume_id;
	private String resumePath;
	private String resumeFilename;
	private int resumeScore;
	
	/* Competency, presentation and format scores 
	 * X_score: the actual score including 5 sub items
	 * X_rank:  the grade for sub items. There are 5 grades for each item.
	 */
	private double competency_totalScore;
	private double[]competency_score;
	
	private double presentation_totalScore;
	private double[] presentation_score;
	
	private double format_totalScore;
	private int[] format_score;
	
	/*
	 * Competency Ranking Rule
	 */
	private static double Competency_Range [][] = {
			{-0.1, 0.04, 0.06, 0.09, 0.12}, // competency 1
			{-0.1, 0.04, 0.06, 0.08, 0.10}, // competency 2
			{-0.1, 0.03, 0.05, 0.07, 0.10}, // competency 3
			{-0.1, 0.03, 0.04, 0.05, 0.06}, // competency 4
			{-0.1, 0.03, 0.05, 0.07, 0.09}, // competency 5
			{-0.1, 0.032, 0.046, 0.062, 0.11} // total
	};
	
	/*
	 * Presentation Ranking Rule
	 */
	private static double Presentation_Range [][] = {
			{-0.1, 0.1, 0.2, 0.3, 0.4}, // presentation 1
			{-0.1, 0.2, 0.4, 0.6, 0.8}, // presentation 2
			{-0.1, 0.2, 0.4, 0.6, 0.8}, // presentation 3
			{-0.1, 0.2, 0.4, 0.6, 0.8}, // presentation 4
			{-0.1, 0.2, 0.4, 0.6, 0.8}, // presentation 5
			{-0.1, 0.15, 0.3, 0.45, 0.7} // total
	};

	public BenchmarkScore(){
		user_id = -1;
		resume_id = -1;
		username = "";
		resumePath = "";
		resumeFilename = "";
		resumeScore = 0;
		
		// Competency scores and ranks
		competency_totalScore = 0;
		competency_score = new double[5];
		
		// Presentation scores and ranks
		presentation_totalScore = 0;
		presentation_score = new double[5];
		
		// Format scores and ranks
		format_totalScore = 0;
		format_score = new int[5];
	}
	
	public void reset(){
		user_id = -1;
		resume_id = -1;
		username = "";
		resumePath = "";
		resumeFilename = "";
		resumeScore = 0;
		
		competency_totalScore = 0;
		presentation_totalScore = 0;
		format_totalScore = 0;
		
		for (int i=0; i<5; ++i){
			competency_score[i] = 0;
			presentation_score[i] = 0;
			format_score[i] = 0;
		}
	}
	
	/*
	 * getter and setter for user id
	 */
	public void setUserId(long _id){
		this.user_id = _id;
	}
	public long getUserId(){
		return this.user_id;
	}
	
	/*
	 * getter and setter for user name
	 */
	public void setUsername(String name){
		this.username = name;
	}
	public String getUsername(){
		return this.username;
	}
	
	/*
	 * getter and setter for resume id
	 */
	public void setResumeId(long _id){
		this.resume_id = _id;
	}
	public long getResumeId(){
		return this.resume_id;
	}
	
	/*
	 * getter and setter for resume path, which is the resume folder
	 */
	public void setResumePath(String path){
		this.resumePath = path;
	}
	public String getResumePath(){
		return this.resumePath;
	}
	
	/*
	 * getter and setter for resume filename
	 */
	public void setResumeFilename(String filename){
		this.resumeFilename = filename;
	}
	public String getResumeFilename(){
		return this.resumeFilename;
	}
	
	/* Competency Score */
	public void setCompetencyScore(double[] score){
		this.competency_totalScore = 0;
		for (int i=0; i<score.length; ++i){
			this.competency_score[i] = score[i];
			competency_totalScore += score[i];
		}
		competency_totalScore /= score.length;
		
		// Map the competency score to [0,50]
		competency_totalScore = 50*competency_totalScore/Competency_Range[5][4];
		if (competency_totalScore > 50.0){
			competency_totalScore = 50;
		}
	}
	public int[] rankCompetencyScore(){
	    int[] competency_rank = new int[5];
		for (int i=0; i<5; ++i){
			if (competency_score[i] > Competency_Range[i][0] && competency_score[i] <= Competency_Range[i][1]){
				competency_rank[i] = 1;
			}
			else if (competency_score[i] > Competency_Range[i][1] && competency_score[i] <= Competency_Range[i][2]){
				competency_rank[i] = 2;
			}
			else if (competency_score[i] > Competency_Range[i][2] && competency_score[i] <= Competency_Range[i][3]){
				competency_rank[i] = 3;
			}
			else if (competency_score[i] > Competency_Range[i][3] && competency_score[i] <= Competency_Range[i][4]){
				competency_rank[i] = 4;
			}
			else{
				competency_rank[i] = 5;
			}
		}
		return competency_rank;
	}
	
	/* Presentation Score */
	public void setPresentationScore(double[] score){
		this.presentation_totalScore = 0;
		for (int i=0; i<score.length; ++i){
			this.presentation_score[i] = score[i];
			presentation_totalScore += score[i];
		}
		presentation_totalScore /= score.length;
		
		// Map the competency score to [0,50]
		presentation_totalScore = 50*presentation_totalScore/Presentation_Range[5][4];
		if (presentation_totalScore > 50.0){
			presentation_totalScore = 50;
		}
	}
	public int[] rankPresentationScore(){
	    int[] presentation_rank = new int[5];
		for (int i=0; i<5; ++i){
			if (presentation_score[i] > Presentation_Range[i][0] && presentation_score[i] <= Presentation_Range[i][1]){
				presentation_rank[i] = 1;
			}
			else if (presentation_score[i] > Presentation_Range[i][1] && presentation_score[i] <= Presentation_Range[i][2]){
				presentation_rank[i] = 2;
			}
			else if (presentation_score[i] > Presentation_Range[i][2] && presentation_score[i] <= Presentation_Range[i][3]){
				presentation_rank[i] = 3;
			}
			else if (presentation_score[i] > Presentation_Range[i][3] && presentation_score[i] <= Presentation_Range[i][4]){
				presentation_rank[i] = 4;
			}
			else{
				presentation_rank[i] = 5;
			}
		}
		return presentation_rank;
	}
	
	/* Format score: value of format score is 0/1 */
	public void setFormatScore(double[] score){
	    this.format_totalScore = 0;
		for (int i=0; i<score.length; ++i){
			this.format_score[i] = (int)score[i];
			format_totalScore += 0.2*format_score[i];
		}
	}
	public int[] rankFormatScore(){
		int[] format_rank = new int[5];
		for (int i=0; i<format_score.length; ++i){
			format_rank[i] = (int)format_score[i];
		}
		return format_rank;
	}
	
	/*
	 * Return the resume score
	 */
	public int getResumeScore(){
		this.resumeScore = (int)((competency_totalScore + presentation_totalScore) * format_totalScore);
		if (this.resumeScore < MIN_SCORE){
		    this.resumeScore= MIN_SCORE;
		}
		return this.resumeScore;
	}
	
	/*
	 * Save the benchmark score as JSON file
	 * Parameters:
	 * scoreOuptputJSONName : the JSON filename for benchmark score, the value is BenchmarkFolder/resume_id/benchmark.json
	 */
	public void saveBenchmarkScore(String scoreOuptputJSONName) throws JSONException, IOException{
		JSONObject json = new JSONObject();
		
		// Construct JSON Object
		json.put("userId", this.user_id);
        json.put("username", this.username);
        json.put("resumeId", this.resume_id);
        json.put("resumeFilename", this.resumeFilename);
        json.put("resumePath", this.resumePath);
        json.put("competencyScore", this.competency_score);
        json.put("presentationScore", this.presentation_score);
        json.put("formatScore", this.format_score);
		
		// Write JSON data into file
		File jsonFile = new File(scoreOuptputJSONName);
		if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        }
        FileWriter fw = new FileWriter(jsonFile);
        BufferedWriter bw = new BufferedWriter(fw);
        json.write(bw);
        bw.close();
	}
		
	/*
	 * Generate JSON object for clients
	 */
	public static String GenerateBenchmarkScoreForClient(List<BenchmarkScore> scoreList){
		if (scoreList.isEmpty()){
			return "";
		}
		
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = null;
		for (int i=0; i<scoreList.size(); ++i){
			BenchmarkScore benchmarkScore = scoreList.get(i);
			int[] competency_rank = benchmarkScore.rankCompetencyScore();
			int[] presentation_rank = benchmarkScore.rankPresentationScore();
			int[] format_rank = benchmarkScore.rankFormatScore();
			int resumeScore = benchmarkScore.getResumeScore();
			
			jsonObject = new JSONObject();
			jsonObject.put("resumeId", benchmarkScore.resume_id);
			jsonObject.put("resumeScore", resumeScore);
			jsonObject.put("competencyRank", competency_rank);
			jsonObject.put("presentationRank", presentation_rank);
			jsonObject.put("formatRank", format_rank);
			
			jsonArray.put(i, jsonObject);
		}
	    return jsonArray.toString();
	}
	
	/*
	 * Parse benchmark score JSON file into benchmark score object
	 */
	public void parseBenchmarkScoreJSONFile(String jsonFilename) throws FileNotFoundException, JSONException, IOException{
		this.reset();
		
	// Read JSON file and parse the file
		BufferedReader br = new BufferedReader(new FileReader(jsonFilename));  
		String s = null;
		double [] score = new double[5];
        while ((s = br.readLine()) != null) {  
            // System.out.println(s);
            JSONObject dataJson = new JSONObject(s);
            this.setUserId(dataJson.getLong("userId"));
            this.setUsername(dataJson.getString("username"));
            this.setResumeId(dataJson.getLong("resumeId"));
            this.setResumeFilename(dataJson.getString("resumeFilename"));
            this.setResumePath(dataJson.getString("resumePath"));
            	    			
            // Read competency score
            JSONArray scoreData = dataJson.getJSONArray("competencyScore");
            for (int i = 0; i < scoreData.length(); ++i) {
            	score[i] = scoreData.optDouble(i);
            }
            this.setCompetencyScore(score);
            
            // Read presentation score
            scoreData = dataJson.getJSONArray("presentationScore");
            for (int i = 0; i < scoreData.length(); ++i) {
            	score[i] = scoreData.optDouble(i);
            }
            this.setPresentationScore(score);
            
            // Read format score
            scoreData = dataJson.getJSONArray("formatScore");
            for (int i = 0; i < scoreData.length(); ++i) {
            	score[i] = scoreData.optDouble(i);
            }
            this.setFormatScore(score);  
        }
        br.close();
	}
	
	/*
     * Save the benchmark score list as JSON file
     */
    public static void saveBenchmarkScoreList(List<BenchmarkScore> scoreList, String outputFilename) throws IOException{
        if (outputFilename == null || outputFilename.isEmpty()){
            return;
        }
        
        // Write JSON data into file
        File jsonFile = new File(outputFilename);
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = null;
        for (int i=0; i<scoreList.size(); ++i){
            BenchmarkScore benchmarkScore = scoreList.get(i);
            jsonObject = new JSONObject();
            jsonObject.put("userId", benchmarkScore.user_id);
            jsonObject.put("username", benchmarkScore.username);
            jsonObject.put("resumeId", benchmarkScore.resume_id);
            jsonObject.put("resumeFilename", benchmarkScore.resumeFilename);
            jsonObject.put("resumePath", benchmarkScore.resumePath);
            jsonObject.put("competencyScore", benchmarkScore.competency_score);
            jsonObject.put("presentationScore", benchmarkScore.presentation_score);
            jsonObject.put("formatScore", benchmarkScore.format_score);
            
            jsonArray.put(i, jsonObject);
        }
        
        FileWriter fw = new FileWriter(jsonFile);
        BufferedWriter bw = new BufferedWriter(fw);
        jsonArray.write(bw);
        bw.close();
    }
}
