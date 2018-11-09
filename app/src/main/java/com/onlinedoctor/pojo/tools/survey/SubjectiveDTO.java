package com.onlinedoctor.pojo.tools.survey;

import com.onlinedoctor.pojo.Common;

import org.json.JSONException;
import org.json.JSONObject;


public class SubjectiveDTO extends BaseQuestionDTO {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4817183580806297858L;

	public SubjectiveDTO(){
		this.setType(Common.TYPE_SUBJECTIVE);
	}
	
	public static SubjectiveDTO fromJO(JSONObject jo) throws JSONException
	{
		SubjectiveDTO q = new SubjectiveDTO();
		q.setId(jo.getInt("id"));
		q.setTitle(jo.getString("title"));
		return q;
	}
	
	public static SubjectiveDTO fromBQ(BaseQuestionDO bq){
		SubjectiveDTO q = new SubjectiveDTO();
		q.setTitle(bq.getQuestion());
		return q;
	}
}
