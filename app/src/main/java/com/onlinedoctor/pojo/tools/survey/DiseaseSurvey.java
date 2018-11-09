package com.onlinedoctor.pojo.tools.survey;


public class DiseaseSurvey {
	private int id = -1;
	private String title = null;
	private String abstractStr = null;
	
	//public List<BaseQuestionDO> qList = null;
	
	public DiseaseSurvey(int id, String title, String abstractStr) {
		super();
		this.id = id;
		this.title = title;
		this.abstractStr = abstractStr;
	}
	
	public DiseaseSurvey(String title, String abstractStr) {
		super();
		this.title = title;
		this.abstractStr = abstractStr;
	}

	public DiseaseSurvey() {
		super();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractStr() {
		return abstractStr;
	}

	public void setAbstractStr(String abstractStr) {
		this.abstractStr = abstractStr;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}	
}
