package com.onlinedoctor.pojo.tools.survey;

import com.onlinedoctor.pojo.Common;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SingleChoiceDTO extends BaseQuestionDTO {
    /**
     *
     */
    private static final long serialVersionUID = 7094869886814436456L;

    public SingleChoiceDTO() {
        this.setType(Common.TYPE_SINGLE_CHOICE);
    }


    public static SingleChoiceDTO fromJO(JSONObject jo) throws JSONException {
        SingleChoiceDTO q = new SingleChoiceDTO();
        q.setId(jo.getInt("id"));
        q.setTitle(jo.getString("title"));
        JSONArray ja = jo.getJSONArray("choicesList");
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < ja.length(); i++) {
            list.add(ja.getString(i));
        }
        q.setChoicesList(list);
        return q;
    }

    public static SingleChoiceDTO fromBQ(BaseQuestionDO bq) {
        SingleChoiceDTO q = new SingleChoiceDTO();
        q.setChoicesList(bq.options);
        q.setTitle(bq.getQuestion());
        return q;
    }

}