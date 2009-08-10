package test;

import pb.dao.ActionDao;
import pb.dao.SurveyDao;
import pb.meta.Action;
import pb.meta.Survey;
import utils.PbUtil;

public class DaoTest
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ActionDao ad = new ActionDao();
        Action action = new Action("aa", "bb", "cc", 11, "dd", "ee", 1, 33);

        ad.save(action);

        SurveyDao sd = new SurveyDao();
        Survey survey = new Survey("1", "2", 3, 4, "5", "6", "7", "8", 9, "10",
                "11", 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
                26, 27, 28, 29, "30", "31", "32", "33", "34", "35", "36", "37", 38);

        sd.save(survey);
        
        System.out.println(PbUtil.getUriName("/servey.htm"));
        System.out.println(PbUtil.getUriName("/ser/vey.htm"));
        System.out.println(PbUtil.getUriName(".htm"));
        System.out.println(PbUtil.getUriName("servey.htm"));
        System.out.println(PbUtil.getUriName("servey"));
        System.out.println(PbUtil.getUriName("/servey"));
        
        System.out.println(PbUtil.parseInt("10"));
        System.out.println(PbUtil.parseInt("1a0"));
        System.out.println(PbUtil.parseInt("a10"));
        System.out.println(PbUtil.parseInt(""));
        System.out.println(PbUtil.parseInt(null));
        
        System.out.println(PbUtil.parseLong("10"));
        System.out.println(PbUtil.parseLong("1a0"));
        System.out.println(PbUtil.parseLong("a10"));
        System.out.println(PbUtil.parseLong(""));
        System.out.println(PbUtil.parseLong(null));
    }

}
