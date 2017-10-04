package bean;

public class resume {
	
	private long id;
	private long userid;
	private String date_time;
	private String filename;
	
	// 简历实际保存的路径及文件名，名称为 (/)resume/date-user_id.pdf
	private String filepath; 
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	
	public String getDate() {
		return date_time;
	}
	public void setDate(String date) {
		this.date_time = date;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
}
