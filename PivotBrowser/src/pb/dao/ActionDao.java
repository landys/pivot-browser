package pb.dao;

import java.util.ArrayList;
import java.util.List;

import pb.meta.Action;

public class ActionDao
{
    public void save(final Action action)
    {
        final String sql = "INSERT INTO action(session_id,operation,tags,"
                + "pic_page,cluster,visual_pic,sort_by,update_time) values(?,?,?,?,?,?,?,?)";
        final List<Object> paras = new ArrayList<Object>();
        paras.add(action.getSessionId());
        paras.add(action.getOperation());
        paras.add(action.getTags());
        paras.add(action.getPicPage());
        paras.add(action.getCluster());
        paras.add(action.getVisualPic());
        paras.add(action.getSortBy());
        paras.add(action.getUpdateTime());

        DbHelper.instance().updateSql(sql, paras);
    }
}
