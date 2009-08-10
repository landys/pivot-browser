package pb.dao;

import java.util.ArrayList;
import java.util.List;

import pb.meta.Survey;

public class SurveyDao
{
    public void save(final Survey survey)
    {
        final String sql = "INSERT INTO survey(session_id,user_id,sex,age,www_expr," +
                "www_freq,prefer_ses,goal,satisfaction,flickr_expr,unsatisfaction,pb_satis," +
                "pb_use,pb_flex,pb_interest,pb_easy,pb_effective,pb_func,pb_final_satic," +
                "pb_tag_quality,pb_cluster,pb_comprehension,pb_relevance,pb_coverage," +
                "pb_infor,pb_more_eff,pb_hit_inconsist,pb_hit_disamb,pb_rerank,inputbox," +
                "nav_bar,pic_box,cluster_sidebar,result_window,like_best,unlike_best,suggestion,update_time)" +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        final List<Object> paras = new ArrayList<Object>();
        paras.add(survey.getSessionId());
        paras.add(survey.getUserId());
        paras.add(survey.getSex());
        paras.add(survey.getAge());
        paras.add(survey.getWwwExpr());
        paras.add(survey.getWwwFreq());
        paras.add(survey.getPreferSes());
        paras.add(survey.getGoal());
        paras.add(survey.getSatisfaction());
        paras.add(survey.getFlickrExpr());
        paras.add(survey.getUnsatisfaction());
        paras.add(survey.getPbSatis());
        paras.add(survey.getPbUse());
        paras.add(survey.getPbFlex());
        paras.add(survey.getPbInterest());
        paras.add(survey.getPbEasy());
        paras.add(survey.getPbEffective());
        paras.add(survey.getPbFunc());
        paras.add(survey.getPbFinalSatic());
        paras.add(survey.getPbTagQuality());
        paras.add(survey.getPbCluster());
        paras.add(survey.getPbComprehension());
        paras.add(survey.getPbRelevance());
        paras.add(survey.getPbCoverage());
        paras.add(survey.getPbInfor());
        paras.add(survey.getPbMoreEff());
        paras.add(survey.getPbHitInconsist());
        paras.add(survey.getPbHitDisamb());
        paras.add(survey.getPbRerank());
        paras.add(survey.getInputbox());
        paras.add(survey.getNavBar());
        paras.add(survey.getPicBox());
        paras.add(survey.getClusterSidebar());
        paras.add(survey.getResultWindow());
        paras.add(survey.getLikeBest());
        paras.add(survey.getUnlikeBest());
        paras.add(survey.getSuggestion());
        paras.add(survey.getUpdateTime());
        DbHelper.instance().updateSql(sql, paras);
    }
}
