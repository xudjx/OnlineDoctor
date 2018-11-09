package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.tools.survey.BaseQuestionDO;
import com.onlinedoctor.pojo.tools.survey.DiseaseSurvey;


public interface DiseaseSurveyService {
	public long addDiseaseSurvey(DiseaseSurvey ds);
	public boolean updateDiseaseSurvey(DiseaseSurvey ds);
	public boolean deleteDiseaseSurvey(DiseaseSurvey ds);
	public boolean deleteDiseaseSurveyRelatedQuestions(int title_id);
	
	public long addQuestion(int title_id, BaseQuestionDO q);
	public boolean updateQuestion(int title_id, BaseQuestionDO q);
	public boolean deleteQuestion(BaseQuestionDO q);
	
	public List<DiseaseSurvey> getAllDiseaseSurvey();
	public DiseaseSurvey getDiseaseSurvey(int title_id);
	
	public List<BaseQuestionDO> getQuestions(int title_id);
	
	
}
