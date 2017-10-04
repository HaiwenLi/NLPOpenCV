package Util;

/*
 * Competency score of one sub item
 * id:    indicate the original index after sorting (对能力分数进行排序后，记录原先在列表中的位置，以便于提取有关信息)
 * value: competency score of the sub item
 */
public class CompetencyItemScore {
	private int id;
	private Double value;
	
	public CompetencyItemScore(int _id, double _value){
		this.id = _id;
		this.value = _value;
	}
	
	public void setId(int _id){
		this.id = _id;
	}
	public int getId(){
		return this.id;
	}
	
	public void setScore(double score){
		this.value = score;
	}
	public Double getScore(){
		return this.value;
	}
}
