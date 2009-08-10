package pb.command;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import pb.dao.SurveyDao;
import pb.meta.Survey;
import utils.PbUtil;

public class SaveSurveyCommand implements ICommand
{
    private SurveyDao surveyDao = new SurveyDao();

    private static final Logger logger = Logger
            .getLogger(SaveSurveyCommand.class);

    private static String[] goalKeys = { "goal1", "goal2", "goal3", "goal4" };

    private static String[] unsatisfactionKeys = { "unsatisfaction1",
            "unsatisfaction2", "unsatisfaction3", "unsatisfaction4",
            "unsatisfaction_text" };

    private static String[] inputboxKeys = { "inputbox1", "inputbox2",
            "inputbox3" };

    private static String[] navBarKeys = { "nav_bar1", "nav_bar2", "nav_bar3" };

    private static String[] picBoxKeys = { "pic_box1", "pic_box2", "pic_box3" };

    private static String[] clusterSidebarKeys = { "cluster_sidebar1",
            "cluster_sidebar2", "cluster_sidebar3" };

    private static String[] resultWindowKeys = { "result_window1",
            "result_window2", "result_window3" };

    private String mergeUnsatisfactionResults(final String[] keys,
            final HttpServletRequest request)
    {
        String[] subKeys = { unsatisfactionKeys[0], unsatisfactionKeys[1],
                unsatisfactionKeys[2] };
        final StringBuilder sb = new StringBuilder(mergeChkboxResults(subKeys,
                request));
        final String value = request.getParameter(unsatisfactionKeys[3]);
        if (null != value)
        {
            final String detail = request.getParameter(unsatisfactionKeys[4])
                    .trim();
            if (sb.length() > 0)
            {
                sb.append(":");
            }
            sb.append(detail.length() == 0 ? value : detail);
        }

        return sb.toString();
    }

    private String mergeChkboxResults(final String[] keys,
            final HttpServletRequest request)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; ++i)
        {
            final String value = request.getParameter(keys[i]);
            if (null != value)
            {
                if (sb.length() > 0)
                {
                    sb.append(":");
                }
                sb.append(value);
            }
        }

        return sb.toString();
    }

    public void execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        final Survey survey = new Survey(request.getSession().getId(), request
                .getParameter("user_id"), PbUtil.parseInt(request
                .getParameter("sex")), PbUtil.parseInt(request
                .getParameter("age")), request.getParameter("www_expr"),
                request.getParameter("www_freq"), request
                        .getParameter("prefer_ses"), mergeChkboxResults(
                        goalKeys, request), PbUtil.parseInt(request
                        .getParameter("satisfaction")), request
                        .getParameter("flickr_expr"),
                mergeUnsatisfactionResults(unsatisfactionKeys, request), PbUtil
                        .parseInt(request.getParameter("pb_satis")), PbUtil
                        .parseInt(request.getParameter("pb_use")), PbUtil
                        .parseInt(request.getParameter("pb_flex")), PbUtil
                        .parseInt(request.getParameter("pb_interest")), PbUtil
                        .parseInt(request.getParameter("pb_easy")), PbUtil
                        .parseInt(request.getParameter("pb_effective")), PbUtil
                        .parseInt(request.getParameter("pb_func")), PbUtil
                        .parseInt(request.getParameter("pb_final_satic")),
                PbUtil.parseInt(request.getParameter("pb_tag_quality")), PbUtil
                        .parseInt(request.getParameter("pb_cluster")), PbUtil
                        .parseInt(request.getParameter("pb_comprehension")),
                PbUtil.parseInt(request.getParameter("pb_relevance")), PbUtil
                        .parseInt(request.getParameter("pb_coverage")), PbUtil
                        .parseInt(request.getParameter("pb_infor")), PbUtil
                        .parseInt(request.getParameter("pb_more_eff")), PbUtil
                        .parseInt(request.getParameter("pb_hit_inconsist")),
                PbUtil.parseInt(request.getParameter("pb_hit_disamb")), PbUtil
                        .parseInt(request.getParameter("pb_rerank")),
                mergeChkboxResults(inputboxKeys, request), mergeChkboxResults(
                        navBarKeys, request), mergeChkboxResults(picBoxKeys,
                        request), mergeChkboxResults(clusterSidebarKeys,
                        request),
                mergeChkboxResults(resultWindowKeys, request), request
                        .getParameter("like_best"), request
                        .getParameter("unlike_best"), request
                        .getParameter("suggestion"), System.currentTimeMillis());
        try
        {
            surveyDao.save(survey);
            request.setAttribute("executeResult", "true");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            request.setAttribute("executeResult", "false");
        }
    }

}
