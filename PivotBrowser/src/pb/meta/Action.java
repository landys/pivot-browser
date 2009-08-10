package pb.meta;

/**
 * @author wjd
 */
public class Action
{
    private long id;

    private String sessionId;

    private String operation;

    private String tags;

    private int picPage;

    private String cluster;

    private String visualPic;

    private int sortBy;

    private long updateTime;

    public Action()
    {

    }

    /**
     * @param sessionId
     * @param operation
     * @param tags
     * @param picPage
     * @param cluster
     * @param visualPic
     * @param sortBy
     * @param updateTime
     */
    public Action(String sessionId, String operation, String tags, int picPage,
            String cluster, String visualPic, int sortBy, long updateTime)
    {
        this.sessionId = sessionId;
        this.operation = operation;
        this.tags = tags;
        this.picPage = picPage;
        this.cluster = cluster;
        this.visualPic = visualPic;
        this.sortBy = sortBy;
        this.updateTime = updateTime;
    }

    public Action(String sessionId, String operation, String tags,
            long updateTime)
    {
        this.sessionId = sessionId;
        this.operation = operation;
        this.tags = tags;
        this.updateTime = updateTime;
    }

    public Action(String sessionId, String operation, String tags, int picPage,
            String cluster, long updateTime)
    {
        this.sessionId = sessionId;
        this.operation = operation;
        this.tags = tags;
        this.picPage = picPage;
        this.cluster = cluster;
        this.updateTime = updateTime;
    }

    public Action(String sessionId, String operation, int picPage,
            long updateTime)
    {
        this.sessionId = sessionId;
        this.operation = operation;
        this.picPage = picPage;
        this.updateTime = updateTime;
    }

    public Action(String sessionId, String operation, String visualPic,
            int sortBy, long updateTime)
    {
        this.sessionId = sessionId;
        this.operation = operation;
        this.visualPic = visualPic;
        this.sortBy = sortBy;
        this.updateTime = updateTime;
    }

    /**
     * @return Returns the cluster.
     */
    public String getCluster()
    {
        return cluster;
    }

    /**
     * @param cluster The cluster to set.
     */
    public void setCluster(String cluster)
    {
        this.cluster = cluster;
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
     * @return Returns the operation.
     */
    public String getOperation()
    {
        return operation;
    }

    /**
     * @param operation The operation to set.
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    /**
     * @return Returns the picPage.
     */
    public int getPicPage()
    {
        return picPage;
    }

    /**
     * @param picPage The picPage to set.
     */
    public void setPicPage(int picPage)
    {
        this.picPage = picPage;
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
     * @return Returns the tags.
     */
    public String getTags()
    {
        return tags;
    }

    /**
     * @param tags The tags to set.
     */
    public void setTags(String tags)
    {
        this.tags = tags;
    }

    /**
     * @return Returns the visualPic.
     */
    public String getVisualPic()
    {
        return visualPic;
    }

    /**
     * @param visualPic The visualPic to set.
     */
    public void setVisualPic(String visualPic)
    {
        this.visualPic = visualPic;
    }

    /**
     * @return Returns the sortBy.
     */
    public int getSortBy()
    {
        return sortBy;
    }

    /**
     * @param sortBy The sortBy to set.
     */
    public void setSortBy(int sortBy)
    {
        this.sortBy = sortBy;
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
