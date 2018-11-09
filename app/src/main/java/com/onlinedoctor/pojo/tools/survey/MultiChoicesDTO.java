package com.onlinedoctor.pojo.tools.survey;

import com.onlinedoctor.pojo.Common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MultiChoicesDTO extends BaseQuestionDTO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4829433232405593592L;
	
	public MultiChoicesDTO(){
		this.setType(Common.TYPE_MUTIL_CHOICES);
	}
	
	public static MultiChoicesDTO fromJO(JSONObject jo) throws JSONException
	{
		MultiChoicesDTO q = new MultiChoicesDTO();
		q.setId(jo.getInt("id"));
		q.setTitle(jo.getString("title"));
		JSONArray ja = jo.getJSONArray("choicesList");
		List<String> list = new ArrayList<String>();
		for(int i=0;i<ja.length();i++){
			list.add(ja.getString(i));
		}
		q.setChoicesList(list);
		return q;
	}
	
	public static MultiChoicesDTO fromBQ(BaseQuestionDO bq){
		MultiChoicesDTO q = new MultiChoicesDTO();
		q.setTitle(bq.getQuestion());
		q.setChoicesList(bq.options);
		return q;
	}
	
	
}
