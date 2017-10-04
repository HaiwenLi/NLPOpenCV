package Util;

import java.util.ArrayList;
import java.util.List;

/*
 * The complete presentation score for one resume text
 * itemText: the resume text
 * score:    the presentation score for 5 sub items
 */

public class PresentationPageItem {
	private int[] score;
	private String itemText;
	private ArrayList<String> repeatWordsList;
	
	public PresentationPageItem(){
		score = new int[5];
		itemText = "";
		repeatWordsList = null;
		for(int i=0; i<5; ++i){
			this.score[i] = 0;
		}
	}
	
	public PresentationPageItem(int[] _score){
		score = new int[5];
		itemText = "";
		for(int i=0; i<5; ++i){
			this.score[i] = 0;
		}
		for (int i=0; i<_score.length; ++i){
			this.score[i] = _score[i];
		}
	}
	
	public int[] getScore(){
		return this.score;
	}
	public void setScore(int[] _score){
		for(int i=0; i<5; ++i){
			this.score[i] = 0;
		}
		for (int i=0; i<_score.length; ++i){
			this.score[i] = _score[i];
		}
	}
	
	public String getItemText(){
		return this.itemText;
	}
	public void setItemText(String text){
		this.itemText = text;
	}
	
	public List<String> getRepeatWordsList(){
	    return repeatWordsList;
	}
	public void setRepeatWordsList(List<String> _repeatWordsList){
	    if (_repeatWordsList.size()<=0){
	        this.repeatWordsList = null;
	    } else{
	        this.repeatWordsList = new ArrayList<String>();
	        for (String word : _repeatWordsList){
	            this.repeatWordsList.add(word);
	        }
	    }
	}
}
