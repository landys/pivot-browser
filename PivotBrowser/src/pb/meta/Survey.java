package pb.meta;

/**
 * @author wjd
 */
public class Survey
{
    private long id;

    private String sessionId;

    private String userId;

    private int sex;

    private int age;

    private String wwwExpr;

    private String wwwFreq;

    private String preferSes;

    private String goal;

    private int satisfaction;

    private String flickrExpr;

    private String unsatisfaction;

    private int pbSatis;

    private int pbUse;

    private int pbFlex;

    private int pbInterest;

    private int pbEasy;

    private int pbEffective;

    private int pbFunc;

    private int pbFinalSatic;

    private int pbTagQuality;

    private int pbCluster;

    private int pbComprehension;

    private int pbRelevance;

    private int pbCoverage;

    private int pbInfor;

    private int pbMoreEff;

    private int pbHitInconsist;

    private int pbHitDisamb;

    private int pbRerank;

    private String inputbox;

    private String navBar;

    private String picBox;

    private String clusterSidebar;

    private String resultWindow;

    private String likeBest;

    private String unlikeBest;

    private String suggestion;

    private long updateTime;

    public Survey()
    {

    }

    /**
     * @param sessionId
     * @param userId
     * @param sex
     * @param age
     * @param wwwExpr
     * @param wwwFreq
     * @param preferSes
     * @param goal
     * @param satisfaction
     * @param flickrExpr
     * @param unsatisfaction
     * @param pbSatis
     * @param pbUse
     * @param pbFlex
     * @param pbInterest
     * @param pbEasy
     * @param pbEffective
     * @param pbFunc
     * @param pbFinalSatic
     * @param pbTagQuality
     * @param pbCluster
     * @param pbComprehension
     * @param pbRelevance
     * @param pbCoverage
     * @param pbInfor
     * @param pbMoreEff
     * @param pbHitInconsist
     * @param pbHitDisamb
     * @param pbRerank
     * @param inputbox
     * @param navBar
     * @param picBox
     * @param clusterSidebar
     * @param resultWindow
     * @param likeBest
     * @param unlikeBest
     * @param suggestion
     * @param updateTime
     */
    public Survey(String sessionId, String userId, int sex, int age,
            String wwwExpr, String wwwFreq, String preferSes, String goal,
            int satisfaction, String flickrExpr, String unsatisfaction,
            int pbSatis, int pbUse, int pbFlex, int pbInterest, int pbEasy,
            int pbEffective, int pbFunc, int pbFinalSatic, int pbTagQuality,
            int pbCluster, int pbComprehension, int pbRelevance,
            int pbCoverage, int pbInfor, int pbMoreEff, int pbHitInconsist,
            int pbHitDisamb, int pbRerank, String inputbox, String navBar,
            String picBox, String clusterSidebar, String resultWindow,
            String likeBest, String unlikeBest, String suggestion,
            long updateTime)
    {
        this.sessionId = sessionId;
        this.userId = userId;
        this.sex = sex;
        this.age = age;
        this.wwwExpr = wwwExpr;
        this.wwwFreq = wwwFreq;
        this.preferSes = preferSes;
        this.goal = goal;
        this.satisfaction = satisfaction;
        this.flickrExpr = flickrExpr;
        this.unsatisfaction = unsatisfaction;
        this.pbSatis = pbSatis;
        this.pbUse = pbUse;
        this.pbFlex = pbFlex;
        this.pbInterest = pbInterest;
        this.pbEasy = pbEasy;
        this.pbEffective = pbEffective;
        this.pbFunc = pbFunc;
        this.pbFinalSatic = pbFinalSatic;
        this.pbTagQuality = pbTagQuality;
        this.pbCluster = pbCluster;
        this.pbComprehension = pbComprehension;
        this.pbRelevance = pbRelevance;
        this.pbCoverage = pbCoverage;
        this.pbInfor = pbInfor;
        this.pbMoreEff = pbMoreEff;
        this.pbHitInconsist = pbHitInconsist;
        this.pbHitDisamb = pbHitDisamb;
        this.pbRerank = pbRerank;
        this.inputbox = inputbox;
        this.navBar = navBar;
        this.picBox = picBox;
        this.clusterSidebar = clusterSidebar;
        this.resultWindow = resultWindow;
        this.likeBest = likeBest;
        this.unlikeBest = unlikeBest;
        this.suggestion = suggestion;
        this.updateTime = updateTime;
    }

    /**
     * @return Returns the age.
     */
    public int getAge()
    {
        return age;
    }

    /**
     * @param age The age to set.
     */
    public void setAge(int age)
    {
        this.age = age;
    }

    /**
     * @return Returns the clusterSidebar.
     */
    public String getClusterSidebar()
    {
        return clusterSidebar;
    }

    /**
     * @param clusterSidebar The clusterSidebar to set.
     */
    public void setClusterSidebar(String clusterSidebar)
    {
        this.clusterSidebar = clusterSidebar;
    }

    /**
     * @return Returns the flickrExpr.
     */
    public String getFlickrExpr()
    {
        return flickrExpr;
    }

    /**
     * @param flickrExpr The flickrExpr to set.
     */
    public void setFlickrExpr(String flickrExpr)
    {
        this.flickrExpr = flickrExpr;
    }

    /**
     * @return Returns the goal.
     */
    public String getGoal()
    {
        return goal;
    }

    /**
     * @param goal The goal to set.
     */
    public void setGoal(String goal)
    {
        this.goal = goal;
    }

    /**
     * @return Returns the id.
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * @return Returns the inputbox.
     */
    public String getInputbox()
    {
        return inputbox;
    }

    /**
     * @param inputbox The inputbox to set.
     */
    public void setInputbox(String inputbox)
    {
        this.inputbox = inputbox;
    }

    /**
     * @return Returns the likeBest.
     */
    public String getLikeBest()
    {
        return likeBest;
    }

    /**
     * @param likeBest The likeBest to set.
     */
    public void setLikeBest(String likeBest)
    {
        this.likeBest = likeBest;
    }

    /**
     * @return Returns the navBar.
     */
    public String getNavBar()
    {
        return navBar;
    }

    /**
     * @param navBar The navBar to set.
     */
    public void setNavBar(String navBar)
    {
        this.navBar = navBar;
    }

    /**
     * @return Returns the pbCluster.
     */
    public int getPbCluster()
    {
        return pbCluster;
    }

    /**
     * @param pbCluster The pbCluster to set.
     */
    public void setPbCluster(int pbCluster)
    {
        this.pbCluster = pbCluster;
    }

    /**
     * @return Returns the pbComprehension.
     */
    public int getPbComprehension()
    {
        return pbComprehension;
    }

    /**
     * @param pbComprehension The pbComprehension to set.
     */
    public void setPbComprehension(int pbComprehension)
    {
        this.pbComprehension = pbComprehension;
    }

    /**
     * @return Returns the pbCoverage.
     */
    public int getPbCoverage()
    {
        return pbCoverage;
    }

    /**
     * @param pbCoverage The pbCoverage to set.
     */
    public void setPbCoverage(int pbCoverage)
    {
        this.pbCoverage = pbCoverage;
    }

    /**
     * @return Returns the pbEasy.
     */
    public int getPbEasy()
    {
        return pbEasy;
    }

    /**
     * @param pbEasy The pbEasy to set.
     */
    public void setPbEasy(int pbEasy)
    {
        this.pbEasy = pbEasy;
    }

    /**
     * @return Returns the pbEffective.
     */
    public int getPbEffective()
    {
        return pbEffective;
    }

    /**
     * @param pbEffective The pbEffective to set.
     */
    public void setPbEffective(int pbEffective)
    {
        this.pbEffective = pbEffective;
    }

    /**
     * @return Returns the pbFinalSatic.
     */
    public int getPbFinalSatic()
    {
        return pbFinalSatic;
    }

    /**
     * @param pbFinalSatic The pbFinalSatic to set.
     */
    public void setPbFinalSatic(int pbFinalSatic)
    {
        this.pbFinalSatic = pbFinalSatic;
    }

    /**
     * @return Returns the pbFlex.
     */
    public int getPbFlex()
    {
        return pbFlex;
    }

    /**
     * @param pbFlex The pbFlex to set.
     */
    public void setPbFlex(int pbFlex)
    {
        this.pbFlex = pbFlex;
    }

    /**
     * @return Returns the pbFunc.
     */
    public int getPbFunc()
    {
        return pbFunc;
    }

    /**
     * @param pbFunc The pbFunc to set.
     */
    public void setPbFunc(int pbFunc)
    {
        this.pbFunc = pbFunc;
    }

    /**
     * @return Returns the pbHitDisamb.
     */
    public int getPbHitDisamb()
    {
        return pbHitDisamb;
    }

    /**
     * @param pbHitDisamb The pbHitDisamb to set.
     */
    public void setPbHitDisamb(int pbHitDisamb)
    {
        this.pbHitDisamb = pbHitDisamb;
    }

    /**
     * @return Returns the pbHitInconsist.
     */
    public int getPbHitInconsist()
    {
        return pbHitInconsist;
    }

    /**
     * @param pbHitInconsist The pbHitInconsist to set.
     */
    public void setPbHitInconsist(int pbHitInconsist)
    {
        this.pbHitInconsist = pbHitInconsist;
    }

    /**
     * @return Returns the pbInfor.
     */
    public int getPbInfor()
    {
        return pbInfor;
    }

    /**
     * @param pbInfor The pbInfor to set.
     */
    public void setPbInfor(int pbInfor)
    {
        this.pbInfor = pbInfor;
    }

    /**
     * @return Returns the pbInterest.
     */
    public int getPbInterest()
    {
        return pbInterest;
    }

    /**
     * @param pbInterest The pbInterest to set.
     */
    public void setPbInterest(int pbInterest)
    {
        this.pbInterest = pbInterest;
    }

    /**
     * @return Returns the pbMoreEff.
     */
    public int getPbMoreEff()
    {
        return pbMoreEff;
    }

    /**
     * @param pbMoreEff The pbMoreEff to set.
     */
    public void setPbMoreEff(int pbMoreEff)
    {
        this.pbMoreEff = pbMoreEff;
    }

    /**
     * @return Returns the pbRelevance.
     */
    public int getPbRelevance()
    {
        return pbRelevance;
    }

    /**
     * @param pbRelevance The pbRelevance to set.
     */
    public void setPbRelevance(int pbRelevance)
    {
        this.pbRelevance = pbRelevance;
    }

    /**
     * @return Returns the pbRerank.
     */
    public int getPbRerank()
    {
        return pbRerank;
    }

    /**
     * @param pbRerank The pbRerank to set.
     */
    public void setPbRerank(int pbRerank)
    {
        this.pbRerank = pbRerank;
    }

    /**
     * @return Returns the pbSatis.
     */
    public int getPbSatis()
    {
        return pbSatis;
    }

    /**
     * @param pbSatis The pbSatis to set.
     */
    public void setPbSatis(int pbSatis)
    {
        this.pbSatis = pbSatis;
    }

    /**
     * @return Returns the pbTagQuality.
     */
    public int getPbTagQuality()
    {
        return pbTagQuality;
    }

    /**
     * @param pbTagQuality The pbTagQuality to set.
     */
    public void setPbTagQuality(int pbTagQuality)
    {
        this.pbTagQuality = pbTagQuality;
    }

    /**
     * @return Returns the pbUse.
     */
    public int getPbUse()
    {
        return pbUse;
    }

    /**
     * @param pbUse The pbUse to set.
     */
    public void setPbUse(int pbUse)
    {
        this.pbUse = pbUse;
    }

    /**
     * @return Returns the picBox.
     */
    public String getPicBox()
    {
        return picBox;
    }

    /**
     * @param picBox The picBox to set.
     */
    public void setPicBox(String picBox)
    {
        this.picBox = picBox;
    }

    /**
     * @return Returns the preferSes.
     */
    public String getPreferSes()
    {
        return preferSes;
    }

    /**
     * @param preferSes The preferSes to set.
     */
    public void setPreferSes(String preferSes)
    {
        this.preferSes = preferSes;
    }

    /**
     * @return Returns the resultWindow.
     */
    public String getResultWindow()
    {
        return resultWindow;
    }

    /**
     * @param resultWindow The resultWindow to set.
     */
    public void setResultWindow(String resultWindow)
    {
        this.resultWindow = resultWindow;
    }

    /**
     * @return Returns the satisfaction.
     */
    public int getSatisfaction()
    {
        return satisfaction;
    }

    /**
     * @param satisfaction The satisfaction to set.
     */
    public void setSatisfaction(int satisfaction)
    {
        this.satisfaction = satisfaction;
    }

    /**
     * @return Returns the sessionId.
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * @param sessionId The sessionId to set.
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    /**
     * @return Returns the sex.
     */
    public int getSex()
    {
        return sex;
    }

    /**
     * @param sex The sex to set.
     */
    public void setSex(int sex)
    {
        this.sex = sex;
    }

    /**
     * @return Returns the suggestion.
     */
    public String getSuggestion()
    {
        return suggestion;
    }

    /**
     * @param suggestion The suggestion to set.
     */
    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    /**
     * @return Returns the unlikeBest.
     */
    public String getUnlikeBest()
    {
        return unlikeBest;
    }

    /**
     * @param unlikeBest The unlikeBest to set.
     */
    public void setUnlikeBest(String unlikeBest)
    {
        this.unlikeBest = unlikeBest;
    }

    /**
     * @return Returns the unsatisfaction.
     */
    public String getUnsatisfaction()
    {
        return unsatisfaction;
    }

    /**
     * @param unsatisfaction The unsatisfaction to set.
     */
    public void setUnsatisfaction(String unsatisfaction)
    {
        this.unsatisfaction = unsatisfaction;
    }

    /**
     * @return Returns the userId.
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * @param userId The userId to set.
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    /**
     * @return Returns the wwwExpr.
     */
    public String getWwwExpr()
    {
        return wwwExpr;
    }

    /**
     * @param wwwExpr The wwwExpr to set.
     */
    public void setWwwExpr(String wwwExpr)
    {
        this.wwwExpr = wwwExpr;
    }

    /**
     * @return Returns the wwwFreq.
     */
    public String getWwwFreq()
    {
        return wwwFreq;
    }

    /**
     * @param wwwFreq The wwwFreq to set.
     */
    public void setWwwFreq(String wwwFreq)
    {
        this.wwwFreq = wwwFreq;
    }

    /**
     * @return Returns the updateTime.
     */
    public long getUpdateTime()
    {
        return updateTime;
    }

    /**
     * @param updateTime The updateTime to set.
     */
    public void setUpdateTime(long updateTime)
    {
        this.updateTime = updateTime;
    }

}
