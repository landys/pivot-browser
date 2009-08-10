package pb.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pb.dao.ActionDao;
import pb.meta.Action;

import utils.PbUtil;

public class ActionService
{
    private ActionDao actionDao = new ActionDao();

    private static enum Operations {
        SEARCH, PIC_PAGE, CLUSTER_CLICK, VISUAL_RERANK
    }

    private static Map<Operations, String> actionNames;
    static
    {
        actionNames = new HashMap<Operations, String>();
        actionNames.put(Operations.SEARCH, "ËÑË÷");
        actionNames.put(Operations.PIC_PAGE, "·­Ò³");
        actionNames.put(Operations.CLUSTER_CLICK, "µã»÷Cluster");
        actionNames.put(Operations.VISUAL_RERANK, "VisualReranking");
    }

    public void saveSearchAction(final List<String> tags, final String sessionId)
    {
        final String strTags = PbUtil.mergeStrings(tags, ':');
        final Action action = new Action(sessionId, actionNames
                .get(Operations.SEARCH), strTags, System.currentTimeMillis());
        actionDao.save(action);
    }

    public void savePicPageAction(final int page, final String sessionId)
    {
        final Action action = new Action(sessionId, actionNames
                .get(Operations.PIC_PAGE), page, System.currentTimeMillis());
        actionDao.save(action);
    }

    public void saveClusterClickAction(final List<String> cluster,
            final List<String> tags, final int page, final String sessionId)
    {
        final String strCluter = PbUtil.mergeStrings(cluster, ':');
        final String strTags = PbUtil.mergeStrings(tags, ':');
        final Action action = new Action(sessionId, actionNames
                .get(Operations.CLUSTER_CLICK), strTags, page, strCluter, System
                .currentTimeMillis());
        actionDao.save(action);
    }

    /**
     * 
     * @param pic
     * @param sortBy 0-random, 1-color, 2-wavelet
     * @param sessionId
     */
    public void saveVisualRerankAction(final String pic, final int sortBy,
            final String sessionId)
    {
        final Action action = new Action(sessionId, actionNames
                .get(Operations.VISUAL_RERANK), pic, sortBy, System
                .currentTimeMillis());
        actionDao.save(action);
    }
}
