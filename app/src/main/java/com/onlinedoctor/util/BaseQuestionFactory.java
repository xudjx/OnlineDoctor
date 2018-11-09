package com.onlinedoctor.util;

import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.tools.survey.BaseQuestionDTO;
import com.onlinedoctor.pojo.tools.survey.IMG;
import com.onlinedoctor.pojo.tools.survey.MultiChoicesDTO;
import com.onlinedoctor.pojo.tools.survey.SingleChoiceDTO;
import com.onlinedoctor.pojo.tools.survey.SubjectiveDTO;

/**
 * Created by wds on 15/11/19.
 */
public class BaseQuestionFactory {
    public static BaseQuestionDTO instance(int type){
        BaseQuestionDTO question;
        switch (type){
            case Common.TYPE_SINGLE_CHOICE:
                question = new SingleChoiceDTO();
                break;
            case Common.TYPE_MUTIL_CHOICES:
                question = new MultiChoicesDTO();
                break;
            case Common.TYPE_SUBJECTIVE:
                question = new SubjectiveDTO();
                break;
            case Common.TYPE_IMAGE:
                question = new IMG();
                break;
            default:
                question = null;
                break;
        }
        return question;
    }
}
