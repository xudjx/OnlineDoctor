package com.onlinedoctor.pojo.tools.survey;

import com.onlinedoctor.pojo.Common;

import org.json.JSONException;
import org.json.JSONObject;


public class IMG extends BaseQuestionDTO {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5301595318164742179L;

	public IMG(){
		this.setType(Common.TYPE_IMAGE);
	}
	
	public static IMG fromJO(JSONObject jo) throws JSONException
	{
		IMG q = new IMG();
		q.setId(jo.getInt("id"));
		q.setTitle(jo.getString("title"));
		return q;
	}
	
	public static IMG fromBQ(BaseQuestionDO bq){
		IMG q = new IMG();
		q.setTitle(bq.getQuestion());
		return q;
	}
}
