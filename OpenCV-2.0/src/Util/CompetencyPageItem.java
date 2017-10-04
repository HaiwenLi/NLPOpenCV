package Util;

/*
 * The complete competency score for one resume text
 * text:   the resume text
 * score:  the competency score for 5 sub items
 */
public class CompetencyPageItem {
	private String text;
	private double[] score;
	
	public CompetencyPageItem(){
		score = new double[5];
		text = "";
	}
	
	public String getItemText(){
		return text;
	}
	public void setItemText(String _text){
		this.text = _text;
	}
	
	public void setScore(double[] _score){
		for(int i=0; i<5; ++i){
			score[i] = _score[i];
		}
	}
	public double[] getScore(){
		return score;
	}
}
