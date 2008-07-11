package DataQuery;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.collections.SetUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitIterator;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;

import com.thoughtworks.xstream.XStream;


import DataIndex.DataInput;
import DataIndex.IndexService;

import model.QueryExpension;
import model.TagCluster;
import model.TagModel;

import utils.Constants;
import utils.Utils;
import utils.Constants.FieldType;


//���ڲ������ȡ�
public class SearchService {
	
	static private IndexService indexService = new IndexService();
	
	private SelectTag select;
	
	private ClusterTag tagsCluster;
	
	//for test;
	static double averageTagRankTime;
	
	//for test
	static double averageColorRankTime;
	
	static double averageWaveLetRankTime;
	
	//��ǰû��rank��ȫ��WrapDocument
	private List<WrapDocument> wrapDocumentList;
	

	//·��
	static private String pathUrl = "../data/"; 
	
	
	
	
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
	//��ҳ ȡ��tag cluster
	//���� k ΪҪ���ص���Ŀ���ĵ�tag
	public List<TagModel> getTagCloud(int k ) {
		return indexService.getDataInput().getTagCloud().subList(0, k-1);
	}
	
	/*
	 * default parameter's interface
	 * 
	 */
	public List<TagCluster> searchTag (List<String> rawList) throws Exception {
		return searchTag(rawList,Constants.topK, Constants.maxClusterNum,true,true);
	}
	
	/*
	 * pivotTag Ҫ��ѯ��pivot
	 * ��Щcluster��Ҫ���������
	 * ���յ÷�����
	 * rawList 
	 * k 
	 * 
	 * 
	 */	
	public List<TagCluster> searchTag (List<String> rawList,int topKInSelectTag, int maxClusterNum, boolean isExpansion, boolean isCluster) throws Exception, Exception {
		System.out.println("topKInSelectTag=" + topKInSelectTag + ", isExpansion=" + isExpansion + ", isCluster=" + isCluster);
		List<QueryExpension> pivotTagList = Utils.convertRawListToPivotTagList(rawList,indexService.getDataInput().getTagIndex(),isExpansion);
		List<TagCluster> list = new ArrayList<TagCluster>();
		
		if(pivotTagList.size() == 0)
			return list;
		
		select = new SelectTag(topKInSelectTag,pivotTagList,indexService.getDataInput());
		
		if (isCluster) {
			tagsCluster = new ClusterTag(maxClusterNum,
					Constants.maxClusterNum, select);
			Map<Set<Long>, Double> clusters = tagsCluster.getClusters();
			// ��ÿ��cluster
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

				list.add(tagCluster);
			}

			// ����
			Collections.sort(list);

			// ������Ŀ����
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
			list.add(tagCluster);
			return list;
		}		
	}
	
	
	/*
	 * ����
	 * List<WrapDocument> ����urlList
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
	 * ����tagList
	 * ֻ��tag��
	 * ��pic��������url
	 * 
	 */
	public List<String> getPicUrlForTagsRank(List<String> tagList, List<String> rawPivotList,int page) throws Exception, Exception {
		List<QueryExpension> currentPivot = Utils.convertRawListToPivotTagList(rawPivotList,indexService.getDataInput().getTagIndex(),false);
		long start = System.currentTimeMillis();
		wrapDocumentList = getWrapDocumentListForTagsRank(tagList,currentPivot);
		long end = System.currentTimeMillis();
		averageTagRankTime += end - start; 
		List<String> picUrlList = getPicUrlForWrapDocumentList(wrapDocumentList);
		return splitPage(picUrlList,page);
	}
	
	/*
	 * ����tagList
	 * ����tag�� ȡ topkForTagRank��
	 * Ȼ����color��
	 * ��pic��������url
	 * 
	 */
	public List<String> getPicUrlForColorRank(List<String> tagList, int page, String queryPicUrl) throws Exception {
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
		//ȡ�ڼ�ҳ		
		return picUrlList.subList(start,end);
	}
		
	
	private String getRelativeUrl(String queryPicUrl) {
		int position = queryPicUrl.indexOf(Constants.image);
		position += Constants.image.length() + 1;
		return queryPicUrl.substring(position);
	}
	
	/*
	 * ����tagList
	 * ����tag�� ȡ topkForTagRank��
	 * Ȼ����waveLet��
	 * ��pic��������url
	 * 
	 */
	public List<String> getPicUrlForWaveLetRank(List<String> tagList, int page, String queryPicUrl) throws Exception {
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
		//�����
		if(wrapDocumentList.size() <= Constants.topKForTagRank) 
			return wrapDocumentList;
		else
			return wrapDocumentList.subList(0, Constants.topKForTagRank);
	}
	
	/*
	 * ���ݲ�ѯ��picurl String 
	 * �Լ�rank type
	 * �õ���query�� byte []
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
	 * �������ѯ���
	 * color�ľ���������
	 * 
	 */
	private  List<WrapDocument> rankWrapDocumentForColor(List<WrapDocument> wrapDocumentList,byte [] queryByteArray) throws Exception {
		
		//������ɫ����
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
	 * �������ѯ���
	 * waveLet�ľ���������
	 * 
	 */
	private  List<WrapDocument> rankWrapDocumentForWaveLet(List<WrapDocument> wrapDocumentList,byte [] queryByteArray) throws Exception {
				
		//����wavelet����
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
	 * ����tagListĬ��ʹ��tag�Է��ص�document��������
	 * Ĭ��ȡǰ��� topKForTagRank �� ������ڵĻ�
	 * ����ȡȫ��
	 * 
	 */
	private List<WrapDocument> getWrapDocumentListForTagsRank(List<String> tagList, List<QueryExpension> currentPivot) {
		
		List<WrapDocument> wrapDocList = new ArrayList<WrapDocument>();		
		try {
			IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
			
			//ĳ��cluster�е�tag
			BooleanQuery orQuery = new BooleanQuery();
			for (String tag : tagList) {
				Term t = new Term(Constants.lucenceTagFieldName, tag);
				TermQuery q = new TermQuery(t);
				orQuery.add(q, BooleanClause.Occur.SHOULD);
			}
			
			//��ǰ��pivot���Ĳ�ѯtag
			BooleanQuery andQuery = Utils.convertQueryListToQuery(currentPivot);
			
			//��һ��and
			BooleanQuery query = new BooleanQuery();
			query.add(orQuery, BooleanClause.Occur.MUST);
			query.add(andQuery, BooleanClause.Occur.MUST);
			
			Hits hits = searcher.search(query);		
//			Analyzer aAnalyzer = new StandardAnalyzer();
			for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext();) {
				Hit hit = (Hit) it.next();
				WrapDocument wrapDoc = new WrapDocument();
				Document doc = hit.getDocument();
				wrapDoc.setDoc(doc);
				Field field = doc.getField(Constants.lucenceTagFieldName);				
				String tags = field.stringValue();
				String [] tagArray = tags.split("\n");
				// ���ĵ��е�ÿһ��token
				double tagScore = 0;
				for(int i = 0; i < tagArray.length; i++) {
						String key = tagArray[i];
						Map<String, Long> tagIndex = indexService.getDataInput().getTagIndex();
						Long rawId = tagIndex.get(
								key);
						if(rawId == null) {  // �������ˡ�
							continue;
						}
						// ��topk ���ƾ��󿴿��Ƿ��id���� ������ڰ�weight�ó���
						// ���õ�����similar matrix�����id
						Map<Long, TagFrequency2> map = select.getTopkMap();
						TagFrequency2 tf = map.get(rawId);
						if (tf == null) {
							continue;
						} else {
							double weight = tf.getScore();
							// �ۼӸ�pic��tag �÷�
							tagScore += weight;
						}
				}
				
				
				
				//bug
//				StringReader sr = new StringReader(tags);
//				TokenStream tokenStream = aAnalyzer.tokenStream(
//						Constants.lucenceTagFieldName, sr);
//				// ���ĵ��е�ÿһ��token
//				double tagScore = 0;
//				while (true) {
//					Token token = tokenStream.next();
//					if (token == null) {
//						break;
//					}
//					String key = token.termText();
//					String key = 
//					long rawId = indexService.getDataInput().getTagIndex().get(
//							key);
//					// ��topk ���ƾ��󿴿��Ƿ��id���� ������ڰ�weight�ó���
//					// ���õ�����similar matrix�����id
//					Map<Long, TagFrequency2> map = select.getTopkMap();
//					TagFrequency2 tf = map.get(rawId);
//					if (tf == null) {
//						continue;
//					} else {
//						double weight = tf.getScore();
//						// �ۼӸ�pic��tag �÷�
//						tagScore += weight;
//					}
//				}
				wrapDoc.setTagScore(tagScore);
				wrapDocList.add(wrapDoc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//����
		Collections.sort(wrapDocList,new TagComparator());	
		System.out.println("total pic num for these tag (in getWrapDocumentListForTagsRank): " + wrapDocList.size());
		
		return wrapDocList;		
		
		
	}
	
	/*
	 * �Ƚ���
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
	 * ��doc�а�byte�ó���
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
	 * ������ɫ���뺯��
	 * ����doc֮���
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
		
	}
	
	/*
	 * ����tagList
	 * ��lucene�õ�pic
	 * Ŀǰֻ����� --- ʹ����Щtag �� or�Ĺ�ϵ���ؼ��ʲ�ѯ ��lucence��
	 * Ȼ��������ǰ���
	 * �������pageĿǰ��û���õ� һ�η���Constants.numPerPage��pic
	 * 
	 */
	public List<String> getPicUrlForTagsRandom(List<String> rawPivotList,int page) {
		List<String> picUrlList = new ArrayList<String>();
		try {
			IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
			
			BooleanQuery query = new BooleanQuery();
			//pivot���� and ��ϵ
			BooleanQuery query1 = new BooleanQuery();
			for(String tag : rawPivotList) {
				Term t = new Term(Constants.lucenceTagFieldName, tag);
				TermQuery andQuery = new TermQuery(t);
				query1.add(andQuery, BooleanClause.Occur.MUST);
			}
			
		
			query.add(query1,BooleanClause.Occur.MUST);
		
			Hits hits = searcher.search(query);
			// i ��������ÿҳ��item��
			int i = 0;
			for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext()
					&& i < Constants.numPerPage; i++) {
				Hit hit = (Hit) it.next();
				Document doc = hit.getDocument();
				Field field = doc
						.getField(Constants.lucencePicFilePathFieldName);
				String picPath = field.stringValue();
                picPath = pathUrl + picPath; 
				picUrlList.add(picPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return splitPage(picUrlList,page);
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
			list.add("flower");
//			list.add("window");
//			list.add("baby");
//			list.add("movie");
//			list.add("film");
//			list.add("poodle");
//			list.add("tv");
//			list.add("flower");
//			list.add("dog");
//			list.add("puppy");
//			list.add("weimaraner");
//			list.add("flower");
//			list.add("flowers");
//			list.add("bloom");
//			list.add("blooms");
//			list.add("blossom");
//			list.add("blossoms");
//			list.add("garden");
//			list.add("red");
//			list.add("white");

			for (String str : list) {
				List<String> rawList = new ArrayList<String>();
				rawList.add(str);
				List<TagCluster> clusterList = searchService.searchTag(rawList,20,20,true,false);
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
					
//					picUrlList = searchService.getPicUrlForColorRank(queryTagList, 0, "/data/293/175681613.jpg");
//					picUrlList = searchService.getPicUrlForWaveLetRank(queryTagList, 0, "/data/186/144644760.jpg");
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
