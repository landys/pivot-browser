package DataQuery;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.HitIterator;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.PriorityQueue;

import pb.filter.RoFilter;
import pb.service.ActionService;

import com.thoughtworks.xstream.XStream;

import DataIndex.IndexService;

import model.QueryExpension;
import model.TagCluster;
import model.TagModel;

import utils.Constants;
import utils.Utils;
import utils.Constants.FieldType;


//先内部测试先　
public class SearchService {
    
    static private IndexService indexService = new IndexService();
    
    private SelectTag select;
    
    private ClusterTag tagsCluster;
    
    private ActionService actionService = new ActionService();
    
    //for test;
    static double averageTagRankTime;
    
    //for test
    static double averageColorRankTime;
    
    static double averageWaveLetRankTime;
    
    //当前没有rank的全部WrapDocument
    private List<WrapDocument> wrapDocumentList;
    
    private static final int MAX_SIZE = 100000;
    
    private static final int MAX_TOP_PICS = 1000;
    
    private int topKInSelectTag = Constants.topK;
    private int maxClusterNum = Constants.maxClusterNum;
    private boolean isExpansionWithSynSet = true;
    private boolean isExpansionWithCoMap = true;
    private boolean isCluster = true;
    private int minFreqTime = Constants.minFreqTime;
    private int topKForExpension = Constants.topKForExpension;
    

    //路径
    static private String pathUrl = "../data/"; 
    
    //含有当前的pivot的图片的总数
    //只有 重新调用 serachTag 才改变 
    private int pivotNumPic; 
    
    
    
    public int getPivotNumPic() {
		return pivotNumPic;
	}


	public void setPivotNumPic(int pivotNumPic) {
		this.pivotNumPic = pivotNumPic;
	}


	public static IndexService getIndexService() {
        return indexService;
    }


    public static void setIndexService(IndexService indexService) {
        SearchService.indexService = indexService;
    }


    public static String getPathUrl() {
        return pathUrl;
    }


    public static void setPathUrl(String pathUrl) {
        SearchService.pathUrl = pathUrl;
    }


    
    public SelectTag getSelect() {
        return select;
    }


    public void setSelect(SelectTag select) {
        this.select = select;
    }


    public ClusterTag getTagsCluster() {
        return tagsCluster;
    }


    public void setTagsCluster(ClusterTag tagsCluster) {
        this.tagsCluster = tagsCluster;
    }


    public List<WrapDocument> getWrapDocumentList() {
        return wrapDocumentList;
    }


    public void setWrapDocumentList(List<WrapDocument> wrapDocumentList) {
        this.wrapDocumentList = wrapDocumentList;
    }


    public SearchService() {
        super();        
    }


    //
    //首页 取得tag cluster
    //参数 k 为要返回的数目最多的的tag
    public List<TagModel> getTagCloud(int k ) {
        return indexService.getDataInput().getTagCloud().subList(0, k-1);
    }
    
    /*
     * default parameter's interface
     * 
     */
    public List<TagCluster> searchTag (List<String> rawList) throws Exception {
        return searchTag(rawList, topKInSelectTag, maxClusterNum, isExpansionWithSynSet, isExpansionWithCoMap, isCluster, minFreqTime, topKForExpension);
        //return searchTag(rawList,Constants.topK,Constants.maxClusterNum,true,true,true, Constants.minFreqTime, Constants.topKForExpension);
    }
    
    
    /*
     * 
     * 比较搓一点
     * 的方法
     * 
     */
    private List<String> getSamplePicListForClusterRandom(List<String> tagList, List<QueryExpension> currentPivot) {
        
        List<String> picStringList = new ArrayList<String>();
        
        try {
            IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);

            // 某个cluster中的tag
            BooleanQuery orQuery = new BooleanQuery();
            for (String tag : tagList) {
                Term t = new Term(Constants.lucenceTagFieldName, tag);
                TermQuery q = new TermQuery(t);
                orQuery.add(q, BooleanClause.Occur.SHOULD);
            }

            // 当前的pivot中心查询tag
            BooleanQuery andQuery = Utils.convertQueryListToQuery(currentPivot);

            // 做一个and
            BooleanQuery query = new BooleanQuery();
            query.add(orQuery, BooleanClause.Occur.MUST);
            query.add(andQuery, BooleanClause.Occur.MUST);

            Hits hits = searcher.search(query);
            for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext();) {
                if(picStringList.size() >= Constants.sampleSize)
                    break;
                Hit hit = (Hit) it.next();              
                Document doc = hit.getDocument();
                Field field = doc.getField(Constants.lucencePicFilePathFieldName);
                String picPath = field.stringValue(); 
                picStringList.add(pathUrl + picPath);
            }
            searcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        return picStringList;   
            
    }

    /*
     * pivotTag 要查询的pivot
     * 这些cluster是要经过排序的
     * 按照得分排序
     * rawList 
     * k 
     * 
     */ 
    public List<TagCluster> searchTag (List<String> rawList,int topKInSelectTag, int maxClusterNum, boolean isExpansionWithSynSet, boolean isExpansionWithCoMap, boolean isCluster, int minFreqTime, int topKForExpension) throws Exception, Exception {
        this.topKInSelectTag = topKInSelectTag;
        this.maxClusterNum = maxClusterNum;
        this.isExpansionWithSynSet = isExpansionWithSynSet;
        this.isExpansionWithCoMap = isExpansionWithCoMap;
        this.isCluster = isCluster;
        this.minFreqTime = minFreqTime;
        this.topKForExpension = topKForExpension;
        
        // save action
        actionService.saveSearchAction(rawList, RoFilter.sessionIds.get());
        
        // do 
        List<QueryExpension> pivotTagList = Utils.convertRawListToPivotTagList(rawList,indexService.getDataInput(),isExpansionWithSynSet, isExpansionWithCoMap, minFreqTime, topKForExpension);
        List<TagCluster> list = new ArrayList<TagCluster>();
        
        if(pivotTagList.size() == 0) {
        	//for record 含有 pivot的 pic数目  
        	pivotNumPic = 0;
            return list;
        }        
      
        //计算 含有 pivot的 pic数目
        BooleanQuery query = Utils.convertQueryListToQuery(pivotTagList);        
        IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
        Hits hits = searcher.search(query);
        pivotNumPic = hits.length();
        searcher.close();
       
        select = new SelectTag(topKInSelectTag,pivotTagList,indexService.getDataInput());
        
        if (isCluster && pivotNumPic > Constants.MIN_CLUSTER_PICS) {
            int clusterNum = (pivotNumPic + Constants.MIN_CLUSTER_PICS - 1) / Constants.MIN_CLUSTER_PICS;
            
            tagsCluster = new ClusterTag(clusterNum > maxClusterNum ? maxClusterNum : clusterNum,
                    Constants.maxClusterNum, select);
            Map<Set<Long>, Double> clusters = tagsCluster.getClusters();
            // 对每个cluster
            for (Set<Long> cluster : clusters.keySet()) {
                TagCluster tagCluster = new TagCluster();
                double qvalue = clusters.get(cluster);

                List<TagModel> tagModelList = new ArrayList<TagModel>();
                for (Long id : cluster) {
                    TagModel tagModel = new TagModel();
                    Long rawId = select.getIdMapRawId().get(id);
                    String tagName = select.getDataInput().getIdIndex().get(
                            rawId);
                    tagModel.setTagName(tagName);
                    Map<String, Long> tagTimesMap = indexService.getDataInput()
                            .getTagTimes();
                    tagModel.setTimes(tagTimesMap.get(tagName).intValue());
                    tagModelList.add(tagModel);
                }
                Collections.sort(tagModelList);
                tagCluster.setTagList(tagModelList);
                double score = qvalue
                        * Math.log(Constants.topK / tagModelList.size());
                tagCluster.setScore(score);

                List<String> queryTagList = new ArrayList<String>();
                for (TagModel tm : tagModelList) {
                    queryTagList.add(tm.getTagName());
                }
                List<String> picUrlList = getSamplePicListForClusterRandom(queryTagList,pivotTagList);
                tagCluster.setPicUrlList(picUrlList);       
                
                list.add(tagCluster);
            }

            // 排序
            Collections.sort(list);

            // 根据数目返回
            if (list.size() < Constants.maxClusterNumForPage)
                return list;
            else
                return list.subList(0, Constants.maxClusterNumForPage);
        } else {  //don't cluster
            TagCluster tagCluster = new TagCluster();
            List<TagModel> tagModelList = new ArrayList<TagModel>();
            for(TagFrequency2 tagFreq : select.getTopKTagsList()) { //tagFreq
                TagModel tagModel = new TagModel();
                tagModel.setTagName(tagFreq.getTag());
                tagModel.setTimes((int)(tagFreq.getScore()));
                tagModelList.add(tagModel);
            }
            Collections.sort(tagModelList);
            
            tagCluster.setTagList(tagModelList);

            List<String> queryTagList = new ArrayList<String>();
            for (TagModel tm : tagModelList) {
                queryTagList.add(tm.getTagName());
            }
            List<String> picUrlList = getSamplePicListForClusterRandom(queryTagList,pivotTagList);
            tagCluster.setPicUrlList(picUrlList);       
            
            list.add(tagCluster);
            return list;
        }       
    }
    
    
    /*
     * 根据
     * List<WrapDocument> 生成urlList
     */
    private List<String> getPicUrlForWrapDocumentList(List<WrapDocument> wrapDocList) {
        List<String> list = new ArrayList<String>();
        
        for(WrapDocument wrapDoc : wrapDocList) {
            Document doc = wrapDoc.getDoc();
            Field field = doc.getField(Constants.lucencePicFilePathFieldName);
            String picPath = field.stringValue(); 
            list.add(pathUrl + picPath);
        }

        return list;
    }
    
    /*
     * 根据tagList
     * 随机排
     * @param act 用来统计用户操作，而不是用来检索，同其他函数. 0-log nothing, 1-cluster click, 2-pic page, 3-visual reranking
     */
    public List<String> getPicUrlForTagsRandom(List<String> tagList, List<String> rawPivotList,int page, int act) throws Exception, Exception {
        // save action
        switch (act)
        {
        case 1:
            actionService.saveClusterClickAction(tagList, rawPivotList, page, RoFilter.sessionIds.get());
            break;
        case 2:
            actionService.savePicPageAction(page, RoFilter.sessionIds.get());
            break;
        }
        
        // do 
        List<QueryExpension> currentPivot = Utils.convertRawListToPivotTagList(rawPivotList,indexService.getDataInput(), isExpansionWithSynSet, isExpansionWithCoMap, minFreqTime, topKForExpension);
        long start = System.currentTimeMillis();
        wrapDocumentList = getWrapDocumentListForTagsRandom(tagList,currentPivot);
        long end = System.currentTimeMillis();
        averageTagRankTime += end - start; 
        List<String> picUrlList = getPicUrlForWrapDocumentList(wrapDocumentList);
        return splitPage(picUrlList,page);
    }
    
    /*
     * 根据tagList
     * 只用tag排
     * 拿pic排序过后的url
     * 
     */
    @Deprecated
    public List<String> getPicUrlForTagsRank(List<String> tagList, List<String> rawPivotList,int page) throws Exception, Exception {
        List<QueryExpension> currentPivot = Utils.convertRawListToPivotTagList(rawPivotList,indexService.getDataInput(), isExpansionWithSynSet, isExpansionWithCoMap, minFreqTime, topKForExpension);
        long start = System.currentTimeMillis();
        wrapDocumentList = getWrapDocumentListForTagsRank(tagList,currentPivot);
        long end = System.currentTimeMillis();
        averageTagRankTime += end - start; 
        List<String> picUrlList = getPicUrlForWrapDocumentList(wrapDocumentList);
        return splitPage(picUrlList,page);
    }
    
    /*
     * 根据tagList
     * 先用tag排 取 topkForTagRank个
     * 然后用color排
     * 拿pic排序过后的url
     * 
     */
    public List<String> getPicUrlForColorRank(List<String> tagList, int page, String queryPicUrl, int act) throws Exception {
        // save action
        switch (act)
        {
        case 2:
            actionService.savePicPageAction(page, RoFilter.sessionIds.get());
            break;
        case 3:
            actionService.saveVisualRerankAction(queryPicUrl, 1, RoFilter.sessionIds.get());
            break;
        }
        
        // do 
        //List<WrapDocument> wrapDocumentList = getWrapDocumentListForTagsRank(tagList);
        String url = getRelativeUrl(queryPicUrl);
        System.out.println(url + " page " + page);      
        byte [] queryByteArray = getQueryByteArrayByUrl(url,FieldType.Color);
        List<WrapDocument> subWrapDocList = getTopKWrapDocList();
        long start = System.currentTimeMillis();
        List<WrapDocument> list = rankWrapDocumentForColor(subWrapDocList, queryByteArray);
        long end = System.currentTimeMillis();
        averageColorRankTime += end - start;
        List<String> picUrlList = getPicUrlForWrapDocumentList(list);
        return splitPage(picUrlList,page);
    }   
    
    private List<String> splitPage(List<String> picUrlList, int page) {
        int size = picUrlList.size();       
        int start = page*Constants.numPerPage;
        int end = (page+1)*Constants.numPerPage;
        if(start >= size) {
            start = size - Constants.numPerPage;
            if(start < 0)
                start = 0;
        }
        if(end >= size) {
            end = size;
        }
        //取第几页      
        return picUrlList.subList(start,end);
    }
        
    
    private String getRelativeUrl(String queryPicUrl) {
        int position = queryPicUrl.indexOf(Constants.image);
        position += Constants.image.length() + 1;
        return queryPicUrl.substring(position);
    }
    
    /*
     * 根据tagList
     * 先用tag排 取 topkForTagRank个
     * 然后用waveLet排
     * 拿pic排序过后的url
     * 
     */
    public List<String> getPicUrlForWaveLetRank(List<String> tagList, int page, String queryPicUrl, int act) throws Exception {
        // save action
        switch (act)
        {
        case 2:
            actionService.savePicPageAction(page, RoFilter.sessionIds.get());
            break;
        case 3:
            actionService.saveVisualRerankAction(queryPicUrl, 2, RoFilter.sessionIds.get());
            break;
        }
        
        // do 
        //List<WrapDocument> wrapDocumentList = getWrapDocumentListForTagsRank(tagList);
        String url = getRelativeUrl(queryPicUrl);
        byte [] queryByteArray = getQueryByteArrayByUrl(url,FieldType.WaveLet); 
        List<WrapDocument> subWrapDocList = getTopKWrapDocList();       
        long start = System.currentTimeMillis();
        List<WrapDocument> list = rankWrapDocumentForWaveLet(subWrapDocList,queryByteArray);
        long end = System.currentTimeMillis();
        averageWaveLetRankTime += end - start;
        List<String> picUrlList = getPicUrlForWrapDocumentList(list);
        return splitPage(picUrlList,page);
    }
    
    private List<WrapDocument> getTopKWrapDocList() {
        //分情况
        if(wrapDocumentList.size() <= Constants.topKForTagRank) 
            return wrapDocumentList;
        else
            return wrapDocumentList.subList(0, Constants.topKForTagRank);
    }
    
    /*
     * 根据查询的picurl String 
     * 以及rank type
     * 拿到该query的 byte []
     * 
     */
    private byte [] getQueryByteArrayByUrl(String queryPicUrl,FieldType type) throws Exception  {
        IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);      
        Term t = new Term(Constants.lucencePicFilePathFieldName,queryPicUrl);
        TermQuery query = new TermQuery(t);
        Hits hits = searcher.search(query);
        return getByteFromLucenceIndex(hits.doc(0),type);
    }
    
    
    /*
     * 
     * 根据与查询点的
     * color的距离来排序
     * 
     */
    private  List<WrapDocument> rankWrapDocumentForColor(List<WrapDocument> wrapDocumentList,byte [] queryByteArray) throws Exception {
        
        //计算颜色距离
        for(WrapDocument wrapDoc : wrapDocumentList) {
            byte [] byteArray = getByteFromLucenceIndex(wrapDoc.getDoc(),FieldType.Color);
            double dist = distFunction(queryByteArray,byteArray);
            double colorScore = dist;
            wrapDoc.setColorScore(colorScore);
        }
        
        Collections.sort(wrapDocumentList,new ColorComparator());
        
        return wrapDocumentList;
    }
    
    /*
     * 
     * 根据与查询点的
     * waveLet的距离来排序
     * 
     */
    private  List<WrapDocument> rankWrapDocumentForWaveLet(List<WrapDocument> wrapDocumentList,byte [] queryByteArray) throws Exception {
                
        //计算wavelet距离
        for(WrapDocument wrapDoc : wrapDocumentList) {
            byte [] byteArray = getByteFromLucenceIndex(wrapDoc.getDoc(),FieldType.WaveLet);
            double dist = distFunction(queryByteArray,byteArray);
            double waveLetScore = dist;
            wrapDoc.setWaveLetScore(waveLetScore);
        }
        
        Collections.sort(wrapDocumentList,new WaveLetComparator());
        
        return wrapDocumentList;
    }
    
    /*
     * 
     * 随机进行random
     * 不sort
     * 
     */
    private List<WrapDocument> getWrapDocumentListForTagsRandom(List<String> tagList, List<QueryExpension> currentPivot) {
        
        final List<WrapDocument> wrapDocList = new ArrayList<WrapDocument>();     
        int pqSize = 0;
        try {
            final IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);

            // 某个cluster中的tag
            BooleanQuery orQuery = new BooleanQuery();
            for (String tag : tagList) {
                Term t = new Term(Constants.lucenceTagFieldName, tag);
                TermQuery q = new TermQuery(t);
                orQuery.add(q, BooleanClause.Occur.SHOULD);
            }

            // 当前的pivot中心查询tag
            BooleanQuery andQuery = Utils.convertQueryListToQuery(currentPivot);

            // 做一个and
            BooleanQuery query = new BooleanQuery();
            query.add(orQuery, BooleanClause.Occur.MUST);
            query.add(andQuery, BooleanClause.Occur.MUST);

            final PriorityQueue pq = new MyPriorityQueue(MAX_SIZE);
            
            searcher.search(query, new HitCollector() {
                @Override
                public void collect(int docId, float score) {
                    try {
                        if (pq.size() < MAX_SIZE) {
                            pq.put(new ScoreDoc(docId, score));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            pqSize = pq.size();
            int n = pqSize < MAX_TOP_PICS ? pqSize : MAX_TOP_PICS;
            for (int i=0; i<n; ++i) {
                final ScoreDoc sd = (ScoreDoc)pq.top();
                final Document doc = searcher.doc(sd.doc);
                final WrapDocument wrapDoc = new WrapDocument();
                wrapDoc.setDoc(doc);
                wrapDocList.add(wrapDoc);
                pq.pop();
            }
            
            /*Hits hits = searcher.search(query);
            for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext();) {
                Hit hit = (Hit) it.next();
                WrapDocument wrapDoc = new WrapDocument();
                Document doc = hit.getDocument();
                wrapDoc.setDoc(doc);
                wrapDocList.add(wrapDoc);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        System.out.println("total pic num for these tag: " + pqSize);
            
            
        return wrapDocList; 
            
    }

    /*
     * 
     * 根据tagList默认使用tag对返回的document进行排序
     * 默认取前面的 topKForTagRank 个 如果存在的话
     * 否则取全部
     * 
     */
    private List<WrapDocument> getWrapDocumentListForTagsRank(List<String> tagList, List<QueryExpension> currentPivot) {
        
        List<WrapDocument> wrapDocList = new ArrayList<WrapDocument>();     
        try {
            IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
            
            //某个cluster中的tag
            BooleanQuery orQuery = new BooleanQuery();
            for (String tag : tagList) {
                Term t = new Term(Constants.lucenceTagFieldName, tag);
                TermQuery q = new TermQuery(t);
                orQuery.add(q, BooleanClause.Occur.SHOULD);
            }
            
            //当前的pivot中心查询tag
            BooleanQuery andQuery = Utils.convertQueryListToQuery(currentPivot);
            
            //做一个and
            BooleanQuery query = new BooleanQuery();
            query.add(orQuery, BooleanClause.Occur.MUST);
            query.add(andQuery, BooleanClause.Occur.MUST);
            
            Hits hits = searcher.search(query);     
//          Analyzer aAnalyzer = new StandardAnalyzer();
            for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext();) {
                Hit hit = (Hit) it.next();
                WrapDocument wrapDoc = new WrapDocument();
                Document doc = hit.getDocument();
                wrapDoc.setDoc(doc);
                Field field = doc.getField(Constants.lucenceTagFieldName);              
                String tags = field.stringValue();
                String [] tagArray = tags.split("\n");
                // 对文档中的每一个token
                double tagScore = 0;
                for(int i = 0; i < tagArray.length; i++) {
                        String key = tagArray[i];
                        Map<String, Long> tagIndex = indexService.getDataInput().getTagIndex();
                        Long rawId = tagIndex.get(
                                key);
                        if(rawId == null) {  // 不管它了　
                            continue;
                        }
                        // 到topk 相似矩阵看看是否该id存在 如果存在把weight拿出来
                        // 先拿到它的similar matrix里面的id
                        Map<Long, TagFrequency2> map = select.getTopkMap();
                        TagFrequency2 tf = map.get(rawId);
                        if (tf == null) {
                            continue;
                        } else {
                            double weight = tf.getScore();
                            // 累加该pic的tag 得分
                            tagScore += weight;
                        }
                }
                
                
                
                //bug
//              StringReader sr = new StringReader(tags);
//              TokenStream tokenStream = aAnalyzer.tokenStream(
//                      Constants.lucenceTagFieldName, sr);
//              // 对文档中的每一个token
//              double tagScore = 0;
//              while (true) {
//                  Token token = tokenStream.next();
//                  if (token == null) {
//                      break;
//                  }
//                  String key = token.termText();
//                  String key = 
//                  long rawId = indexService.getDataInput().getTagIndex().get(
//                          key);
//                  // 到topk 相似矩阵看看是否该id存在 如果存在把weight拿出来
//                  // 先拿到它的similar matrix里面的id
//                  Map<Long, TagFrequency2> map = select.getTopkMap();
//                  TagFrequency2 tf = map.get(rawId);
//                  if (tf == null) {
//                      continue;
//                  } else {
//                      double weight = tf.getScore();
//                      // 累加该pic的tag 得分
//                      tagScore += weight;
//                  }
//              }
                wrapDoc.setTagScore(tagScore);
                wrapDocList.add(wrapDoc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //排序
        Collections.sort(wrapDocList,new TagComparator());  
        System.out.println("total pic num for these tag: " + wrapDocList.size());
        
        return wrapDocList;     
        
    }
    
    /*
     * 比较器
     * 
     */
    class ColorComparator implements Comparator<WrapDocument> {

        public int compare(WrapDocument o1, WrapDocument o2) {
            if(o1.colorScore < o2.colorScore)
                return -1;
            else if(o1.colorScore > o2.colorScore)
                return 1;
            else
                return 0;
            /*if(o1.tagScore > o2.tagScore)
                return -1;
            else if(o1.tagScore < o2.tagScore)
                return 1;
            else if(o1.colorScore < o2.colorScore)
                return -1;
            else if(o1.colorScore > o2.colorScore)
                return 1;
            else
                return 0;*/
        }
        
    }
    
    class WaveLetComparator implements Comparator<WrapDocument> {

        public int compare(WrapDocument o1, WrapDocument o2) {
            if(o1.waveLetScore < o2.waveLetScore)
                return -1;
            else if(o1.waveLetScore > o2.waveLetScore)
                return 1;
            else
                return 0;   
            /*if(o1.tagScore > o2.tagScore)
                return -1;
            else if(o1.tagScore < o2.tagScore)
                return 1;
            else if(o1.waveLetScore < o2.waveLetScore)
                return -1;
            else if(o1.waveLetScore > o2.waveLetScore)
                return 1;
            else
                return 0;*/         
        }
        
    }
    
    class TagComparator implements Comparator<WrapDocument> {

        public int compare(WrapDocument o1, WrapDocument o2) {
            if(o1.tagScore > o2.tagScore)
                return -1;
            else if(o1.tagScore < o2.tagScore)
                return 1;
            else
                return 0;
        }
        
    }
    
    /*
     * 从doc中把byte拿出来
     * 
     * 
     */
    private byte [] getByteFromLucenceIndex(Document doc, FieldType type) {
        Field field = null;
        switch(type) {
        case Color:
            field = doc.getField(Constants.lucenceColorFeatureFieldName);
            break;
        case WaveLet:
            field = doc.getField(Constants.lucenceWaveLetFeatureFieldName);
            break;
        }
        return field.binaryValue();
    }
    
    /*
     * 计算颜色距离函数
     * 两个doc之间的
     * 
     */
    private double distFunction(byte [] o1, byte [] o2) throws Exception {
        if(o1.length != o2.length)
            throw new Exception();
        ByteBuffer b1 = ByteBuffer.wrap(o1);
        ByteBuffer b2 = ByteBuffer.wrap(o2);        
        DoubleBuffer db1 = b1.asDoubleBuffer();
        DoubleBuffer db2 = b2.asDoubleBuffer();
        double [] point1 = new double [db1.capacity()];
        double [] point2 = new double [db2.capacity()];
        db1.get(point1);
        db2.get(point2);
        return Constants.computeDist(point1,point2);
    }
    
    class WrapDocument  {
        
        private Document doc;
        
        private double tagScore;
        
        private double colorScore;
        
        private double waveLetScore;
        
        private double totalScore;
        
        private int docId;
        
        public WrapDocument() {
            doc = null;
            tagScore = 0.0;
            colorScore = 0.0;
            waveLetScore = 0.0;
            totalScore = 0.0;
            docId = 0;
        }

        public double getColorScore() {
            return colorScore;
        }

        public void setColorScore(double colorScore) {
            this.colorScore = colorScore;
        }

        public double getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(double totalScore) {
            this.totalScore = totalScore;
        }   

        public double getWaveLetScore() {
            return waveLetScore;
        }

        public void setWaveLetScore(double waveLetScore) {
            this.waveLetScore = waveLetScore;
        }

        public Document getDoc() {
            return doc;
        }

        public void setDoc(Document doc) {
            this.doc = doc;
        }

        public double getTagScore() {
            return tagScore;
        }

        public void setTagScore(double tagScore) {
            this.tagScore = tagScore;
        }

        /**
         * @return Returns the docId.
         */
        public int getDocId()
        {
            return docId;
        }

        /**
         * @param docId The docId to set.
         */
        public void setDocId(int docId)
        {
            this.docId = docId;
        }       
        
    }
    
    class MyPriorityQueue extends PriorityQueue {
        
        public MyPriorityQueue(int maxSize) {
            initialize(maxSize);
        }
        
        /**
         * sort in descending order, so lessThan is in the opposite way.
         */
        @Override
        protected boolean lessThan(Object obj1, Object obj2)
        {
            boolean re = false;
            try {
                final ScoreDoc sd1 = (ScoreDoc)obj1;
                final ScoreDoc sd2 = (ScoreDoc)obj2;
                // correct here
                if (sd1.score > sd2.score
                        || (sd1.score == sd2.score && sd1.doc < sd2.doc)) {
                    re = true;
                }
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
            return re;
        }
  
    }
    

    public static void main(String[] args) throws Exception {
        
        SearchService searchService = new SearchService();  
        
        /*Map<Long,Long> map = indexService.getDataInput().getSampleTagTimes();
        
        int i = 0;
        for(Long id : map.keySet()) {
            System.out.println(id + " " + map.get(id));
            if(i > 10)
                break;
            i++;
        }
        
        System.out.println("total tag num: " + indexService.getDataInput().getTotalTagNum());*/
        int i = 0;
        for (int n = 0; n < 1; n++) {
            List<String> list = new ArrayList<String>();
            list.add("sexta");
            list.add("bluedoor");
//          list.add("window");
//          list.add("baby");
//          list.add("movie");
//          list.add("film");
//          list.add("poodle");
//          list.add("tv");
//          list.add("flower");
//          list.add("dog");
//          list.add("puppy");
//          list.add("weimaraner");
//          list.add("flower");
//          list.add("flowers");
//          list.add("bloom");
//          list.add("blooms");
//          list.add("blossom");
//          list.add("blossoms");
//          list.add("garden");
//          list.add("red");
//          list.add("white");

            for (String str : list) {
                List<String> rawList = new ArrayList<String>();
                rawList.add(str);
                List<TagCluster> clusterList = searchService.searchTag(rawList);
                for (TagCluster cluster : clusterList) {
                    List<TagModel> list1 = cluster.getTagList();
                    List<String> queryTagList = new ArrayList<String>();
                    for (TagModel tm : list1) {
                        queryTagList.add(tm.getTagName());
                    }
                    List<String> picUrlList = searchService
                            .getPicUrlForTagsRank(queryTagList, rawList, 0);
                    //print
                    XStream xstream = new XStream();
                    String result = xstream.toXML(picUrlList);
                    
                    System.out.println(result);
                    
                    picUrlList = searchService.getPicUrlForColorRank(queryTagList, 0, "/data/293/175681613.jpg", 0);
                    picUrlList = searchService.getPicUrlForWaveLetRank(queryTagList, 0, "/data/186/144644760.jpg", 0);
                    i++;
                }
            }
        }
        
        System.out.println("average tag rank time: " + SearchService.averageTagRankTime / i);
        
        System.out.println("average color rank time: " + SearchService.averageColorRankTime / i);
        
        System.out.println("average waveLet rank time: " + SearchService.averageWaveLetRankTime / i);
        
        /*synWordList = new ArrayList<String>();
        queryWord = "film";
        synWordList.add("film");
        querExpension = new QueryExpension();
        querExpension.setQueryWord(queryWord);
        querExpension.setSynWordList(synWordList);
        queryList.add(querExpension);*/
        
            
        
        /*List<TagFrequency2> kList = searchService.getSelect().getTopKTagsList();      
        List<TagFrequency2> newList = new ArrayList<TagFrequency2>(kList);      
        XStream xstream = new XStream();    
        System.out.println("top " + kList.size() + " tag:");
        xstream.alias("tag", TagFrequency2.class);  
        String result = xstream.toXML(newList);
        System.out.println(result);
        
        xstream = new XStream();
        System.out.println("cluster :");
        xstream.alias("tag", TagModel.class);
        xstream.alias("cluster", TagCluster.class);     
        result = xstream.toXML(clusterList);        
        System.out.println(result);*/
        
        /*for(TagCluster cluster : clusterList) {
            List<TagModel> list1 = cluster.getTagList();
            List<String> queryTagList = new ArrayList<String>();
            for(TagModel tm : list1) {
                queryTagList.add(tm.getTagName());
            }
            List<String> picUrlList = searchService.getPicUrlForTagsRank(queryTagList,list,0);
            for(String url : picUrlList) {
                System.out.println(url);
            }
            //System.out.println("cluster " + i + "pic Num: " + searchService.getWrapDocumentList().size());
        }*/
        
        /*TagCluster cluster = clusterList.get(0);
        
        List<TagModel> list = cluster.getTagList();
        List<String> queryTagList = new ArrayList<String>();
        for(TagModel tm : list) {
            queryTagList.add(tm.getTagName());
        }
        
        List<String> picUrlList = searchService.getPicUrlForTagsRank(queryTagList,0);
        for(String url : picUrlList) {
            System.out.println(url);
        }
        System.out.println("");
        if(searchService.getWrapDocumentList() == null)
            searchService.setWrapDocumentList(searchService.getWrapDocumentListForTagsRank(queryTagList));
        picUrlList = searchService.getPicUrlForWaveLetRank(queryTagList, 0, "1/2147279266.jpg");
        for(String url : picUrlList) {
            System.out.println(url);
        }*/
        
        /*XStream xstream = new XStream();
        
        List<String> picUrlList = searchService.getPicUrlForTags(pivotList, 0);
        String result = xstream.toXML(picUrlList);
        
        System.out.println(result);*/
    }
    
    
    
    
    
    

}
