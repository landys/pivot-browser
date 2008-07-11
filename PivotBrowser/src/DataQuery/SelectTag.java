package DataQuery;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import model.QueryExpension;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
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

import utils.Constants;
import utils.Utils;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.RCDoubleMatrix2D;

public class SelectTag {

	private int k ;	
	
	private DataInput dataInput; 
	
	//top k
	//old
	//private List<TagFrequency> topKTagsList;
	
	private List<TagFrequency2> topKTagsList;
	
	//back up
	private Map<Long,TagFrequency2> topkMap;
	
	//all
	//old
	//private List<TagFrequency> allCoTagsList;
	private List<TagFrequency2> allCoTagsList;
	
	//查询的列表
	//注意两层
	//里面是一个Map<String,List>的key是同义词集合的查询词,value是同义集合list 同义集合内部是一个or关系 
	//外面的list是一个and的关系
	private List<QueryExpension> queryTagList;
	
	//pivot集合
	//从查询list里面的queryWord选出来
	private Set<String> pivotSet;
	
	private int docNum;
	
	//id影射关系 对应于simMatrix矩阵
	//前面一个id为原始的id 后面一个id为simMatrix矩阵的id
	private Map<Long,Long> rawIdMapId; 
	
	//id反影射关系 对应于simMatrix矩阵
	//前面一个id为simMatrix矩阵的id 后面一个位原始的id
	private Map<Long,Long> idMapRawId; 
	
	//top k相似度矩阵
	//可能为稀疏　也肯能为密集　
	//取决于　topKTagsList　里面的个数
	private DoubleMatrix2D simMatrix;
	
	
	//new
	private Map<String,Long> searchCoTagMap;
	
	//测试时间用的
	//给访问外存所用的时间
	public static long time1;
	
	//给访问内存取topK所用的时间
	public static long time2;
	
	
	
	
	public int getDocNum() {
		return docNum;
	}

	public void setDocNum(int docNum) {
		this.docNum = docNum;
	}


	public Map<String, Long> getSearchCoTagMap() {
		return searchCoTagMap;
	}

	public void setSearchCoTagMap(Map<String, Long> searchCoTagMap) {
		this.searchCoTagMap = searchCoTagMap;
	}

	public List<TagFrequency2> getAllCoTagsList() {
		return allCoTagsList;
	}

	public void setAllCoTagsList(List<TagFrequency2> allCoTagsList) {
		this.allCoTagsList = allCoTagsList;
	}

	public DataInput getDataInput() {
		return dataInput;
	}

	public void setDataInput(DataInput dataInput) {
		this.dataInput = dataInput;
	}

	public Map<Long, Long> getIdMapRawId() {
		return idMapRawId;
	}

	public void setIdMapRawId(Map<Long, Long> idMapRawId) {
		this.idMapRawId = idMapRawId;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}
	
	


	public Map<Long, Long> getRawIdMapId() {
		return rawIdMapId;
	}

	public void setRawIdMapId(Map<Long, Long> rawIdMapId) {
		this.rawIdMapId = rawIdMapId;
	}

	public DoubleMatrix2D getSimMatrix() {
		return simMatrix;
	}

	public void setSimMatrix(DoubleMatrix2D simMatrix) {
		this.simMatrix = simMatrix;
	}

	public List<TagFrequency2> getTopKTagsList() {
		return topKTagsList;
	}

	public void setTopKTagsList(List<TagFrequency2> topKTagsList) {
		this.topKTagsList = topKTagsList;
	}

	/*
	 * 编号方式为根据在topk list里面的顺序
	 * 
	 * 
	 */
	private void createIdMap() {	
		Map<Long,Long> rawIdMapId = new HashMap<Long,Long>();
		Map<Long,Long> idMapRawId = new HashMap<Long,Long>();
		long id = 0;
		for(TagFrequency2 tf : topKTagsList) {
			Long newId = new Long(id);
			rawIdMapId.put(tf.getTagId(), newId);
			idMapRawId.put(newId, tf.getTagId());
			id++;
		}
		
		this.rawIdMapId = rawIdMapId;
		this.idMapRawId = idMapRawId;
		
	}
	
	/*
	 * 构造相似矩阵 top k的相似矩阵
	 * 
	 */
	private DoubleMatrix2D createSimMatrix() {
		int size = topKTagsList.size();
		DoubleMatrix2D matrix = new DenseDoubleMatrix2D(size, size);		
		
		//DoubleMatrix2D coMatrix = dataInput.getCoMatrix();		
	
		for(TagFrequency2 tf : topKTagsList) {
			Long row = tf.getTagId();
			int simRow = rawIdMapId.get(row).intValue();
//			现在的方法
			for(Long id : rawIdMapId.keySet()) {
				int simCol = rawIdMapId.get(id).intValue();
				//使用 (a交b)/(a并b)获得相似度
				//double f1 = coMatrix.getQuick(row.intValue(), id.intValue());
				//coMatrix分块处理
				double f1 = dataInput.getCoMatrixValue(row.intValue(), id.intValue());
				//double f2 = coMatrix.getQuick(row.intValue(), row.intValue()) + coMatrix.getQuick(id.intValue(), id.intValue());
				double f2 = dataInput.getCoMatrixValue(row.intValue(), row.intValue()) + dataInput.getCoMatrixValue(id.intValue(), id.intValue());
				matrix.setQuick(simRow,simCol, f1/f2);
			}
//			原来的方法			
//			for(int i = 0; i < viewRow.size(); i++) {
//				//只处理包含在top k中的相似				
//				if(rawIdMapId.containsKey(new Long(i))) {
//					int simRow = rawIdMapId.get(row).intValue();
//					int simCol = rawIdMapId.get((long)i).intValue();
//					
//					//使用 (a交b)/(a并b)获得相似度
//					double f1 = coMatrix.getQuick((int)row, (int)i);
//					//有点问题
//					double f2 = coMatrix.getQuick((int)row, (int)row) + coMatrix.getQuick((int)i, (int)i);
//					matrix.setQuick(simRow,simCol, f1/f2);
//				}else
//					continue;
//			}			
		}
		return matrix;
	}
	
	private List<TagFrequency2> getTopKTags () throws Exception{
		if(k < allCoTagsList.size())
			return allCoTagsList.subList(0, k);
		else
			return allCoTagsList;
	}
	
	
	/*
	 * 旧算法
	 * 取前面的top k个
	 * 如果存在的话
	 * 否则返回全部List
	 */
	/*private List<TagFrequency> getAllCoTags () throws Exception{
		RCDoubleMatrix2D coMatrix = dataInput.getCoMatrix();
		Map<String, Long> tagIndex = dataInput.getTagIndex();
		// for test
		//Map<Long, String> idIndex = dataInput.getIdIndex();
		
		Map<Long,TagFrequency> map = new HashMap<Long,TagFrequency>();
		List<TagFrequency> list = new ArrayList<TagFrequency>();
		
		int pivotNum = pivotTags.size();
		int j = 0;
		//pivot的关系应该是一个 and的关系
		for(String s : pivotTags) {
			Long rowId = tagIndex.get(s);
			
			// 如果该pivot 不存在, 目前情况下是  继续处理下一个 ,这需要做扩展的
			if(rowId == null) {
				System.out.println("tag: " + s + " is not exist");
				continue; 
			}
			
			int row = rowId.intValue();
			DoubleMatrix1D viewRow = coMatrix.viewRow(row);
			for(int i = 0 ; i < viewRow.size() ; i++) {				
				
				if(i == row)
					continue;
				//将其与pivot的相似度算出来 添加到list中先
				if(viewRow.get(i) != 0) {
					TagFrequency tf = map.get(new Long(i));
					double selfFreq = coMatrix.getQuick(i, i);
					double coFreq = coMatrix.getQuick(row, i);
					//原来是用　cof/self 作为　score
					//double score = coFreq /selfFreq;
					//目前用这个　做好之后用totalDoc
					float totalDoc = dataInput.getTotalDoc();
					double score = coFreq/totalDoc * Math.log(totalDoc/selfFreq);
					//double score = coFreq * Math.log(100000/selfFreq);
					//如果没有存在
					if( tf == null) {
						tf = new TagFrequency(pivotNum);						
						tf.setTagId(i);
						//for test
						//tf.setTag(idIndex.get((long)i));						
						tf.setSelfFreq(selfFreq);
						tf.setCoFreq(coFreq, j);
						tf.setScore(score, j);
						map.put(new Long(i), tf);
					} else { //如果已经存在
						//这里有两种算法
						//下面是算法一
						//如果已经存在 比较score 高的话更新原来的cofreq 以及 score
//						if(tf.getScore() < score) {
//							tf.setCoFreq(coFreq);						
//							tf.setScore(score);
//						}
						//算法二 直接设置进去
						tf.setCoFreq(coFreq, j);
						tf.setScore(score, j);						
					}
				}
			}
			j++;
		}

		//转化成list
		for(Long id : map.keySet()) {
			TagFrequency tf = map.get(id);
			double finalScore = 1;
			//  计算 总分 总分为 各个相关度的 乘积
			for(int i = 0; i < tf.getPivotNum(); i++) {
				finalScore *= tf.getScore()[i];
			}
			if(finalScore != 0) {   //如果得分不为0
				tf.setFinalScore(finalScore);			
				list.add(tf);
			}
		}		
		//排序, 按照finalscore
		Collections.sort(list);		
		return list;
		
		
	}*/
	
	
	/*
	 * 新算法
	 * 从查询的pivot里面选
	 * 算法一
	 * 
	 * 使用 Tc * log(totalDoc/selfFreq)
	 * selfFreq 为库中的
	 * 
	 */
	private List<TagFrequency2> getAllCoTags1 () throws Exception {
		//测试时间用的
		long startTime = System.currentTimeMillis();
		searchCoTagMap = searchCoTagTimes2();
//		if(this.queryTagList.size() == 1) { //speedup 如果pivot只有一个
//			searchCoTagMap = searchCoTagTimes2();
//		} else { //有多个
//			searchCoTagMap = searchCoTagTimes1();	
//		}
			
		//测试时间
		long endTime = System.currentTimeMillis();
		SelectTag.time1 = endTime - startTime;
		startTime = System.currentTimeMillis();
		Map<String, Long> tagIndex = dataInput.getTagIndex();
		//DoubleMatrix2D coMatrix = dataInput.getCoMatrix();
		List<TagFrequency2> list = new ArrayList<TagFrequency2>();		
		
		for(String key : searchCoTagMap.keySet()) {
			TagFrequency2 tf = new TagFrequency2();
			long coTimes = searchCoTagMap.get(key);
			Long rowId = tagIndex.get(key);
			if(rowId == null) {  // 不管它了　
				continue;
			}
			//分块处理
			//double selfFreq = coMatrix.getQuick(rowId.intValue(), rowId.intValue());
			double selfFreq = dataInput.getCoMatrixValue(rowId.intValue(), rowId.intValue());
			double totalDoc = dataInput.getTotalDoc();
			double score = coTimes * Math.log(totalDoc / selfFreq);
			
			tf.setScore(score);
			tf.setSelfFreq(selfFreq);
			tf.setTagId(rowId);
			tf.setTag(dataInput.getIdIndex().get(rowId));
			list.add(tf);			
		}
		
		Collections.sort(list);	
		//for test
		endTime = System.currentTimeMillis();
		SelectTag.time2 = endTime - startTime;
		
		return list;
	}
	
	/*
	 * 算法二
	 * 
	 * 使用 库中的selfFreq　
	 * 
	 */
	private List<TagFrequency2> getAllCoTags2 () throws Exception {		
		searchCoTagMap = searchCoTagTimes1();		
		Map<String, Long> tagIndex = dataInput.getTagIndex();
		//DoubleMatrix2D coMatrix = dataInput.getCoMatrix();
		List<TagFrequency2> list = new ArrayList<TagFrequency2>();		
		
		for(String key : searchCoTagMap.keySet()) {
			TagFrequency2 tf = new TagFrequency2();
			Long rowId = tagIndex.get(key);
			//分块处理
//			double selfFreq = coMatrix.getQuick(rowId.intValue(), rowId.intValue());
			double selfFreq = dataInput.getCoMatrixValue(rowId.intValue(), rowId.intValue());
			double score = selfFreq;			
			tf.setScore(score);
			tf.setSelfFreq(selfFreq);
			tf.setTagId(rowId);
			tf.setTag(dataInput.getIdIndex().get(rowId));
			list.add(tf);			
		}
		
		Collections.sort(list);		
		return list;
	}
	
	/*
	 * 算法三
	 * 用coTimes
	 */
	
	private List<TagFrequency2> getAllCoTags3 () throws Exception {		
		searchCoTagMap = searchCoTagTimes1();		
		Map<String, Long> tagIndex = dataInput.getTagIndex();
//		DoubleMatrix2D coMatrix = dataInput.getCoMatrix();
		List<TagFrequency2> list = new ArrayList<TagFrequency2>();		
		
		for(String key : searchCoTagMap.keySet()) {
			TagFrequency2 tf = new TagFrequency2();
			long coTimes = searchCoTagMap.get(key);
			Long rowId = tagIndex.get(key);
			//分块处理
//			double selfFreq = coMatrix.getQuick(rowId.intValue(), rowId.intValue());
			double selfFreq = dataInput.getCoMatrixValue(rowId.intValue(), rowId.intValue());
			double score = coTimes; 
			
			tf.setScore(score);
			tf.setSelfFreq(selfFreq);
			tf.setTagId(rowId);
			tf.setTag(dataInput.getIdIndex().get(rowId));
			list.add(tf);			
		}
		
		Collections.sort(list);		
		return list;
	}
	
	/*
	 * 排序
	 * 比较器
	 * 
	 */
	public class MapComparator implements Comparator<Map<String,Long>> {

		public int compare(Map<String, Long> o1, Map<String, Long> o2) {
			if(o1.size() > o2.size())
				return 1;
			else if(o1.size() < o2.size())
				return -1;
			else
				return 0;
		}
		
	}

		


	
	
	/*
	 * 得到共同出现次数的map 使用走coMatrix的方法
	 * 
	 * 这个只限于有一个pivot的情况 同义词集合里面选最大 and关系集合里面选最小
	 * 
	 * 这样ms会有不少问题
	 */
	private Map<String,Long> searchCoTagTimes2() {			
		Map<String,Long> tagIndex = dataInput.getTagIndex();
		Map<Long,String> idIndex = dataInput.getIdIndex();
		List<Map<String,Long>> mapList = new ArrayList<Map<String,Long>>();
		for (QueryExpension queryExpension : queryTagList) { //对每个 查询的　扩展
			Set<String> pivotSynWordSet = queryExpension.getSynWordSet();//同义词集合
			Map<String,Long> map = new HashMap<String,Long>();
			for (String pivotSynWord : pivotSynWordSet) {
				Long id = tagIndex.get(pivotSynWord);
				// 取出该行
				DoubleMatrix1D vector = dataInput.getCoMatrixOneRow(id
						.intValue());
				// 对该行的每个非0的元素 遍历
				for (int i = 0; i < vector.size(); i++) {
					Double value = vector.getQuick(i);
					if (value != 0) { // 对value不等于０的每一个
						String key = idIndex.get(new Long(i)); // 取得该id的对应tag
						Long times = map.get(key);
						if (times == null) { // 如果没有出现过
							map.put(key, new Long(value.longValue()));
						} else { // 如果已经出现过 加入现在的到原来的上面
							times = times.longValue();
							if(value > times) {  //取最大的次数那个
								times = value.longValue();
								map.put(key, new Long(times));
							}							
						}
					}
				}
			}
			mapList.add(map);
		}
		//按照大小排序
		Collections.sort(mapList, new MapComparator());
		
		//取最小的那个作为base 这里保证至少有一个
		Map<String,Long> baseMap = mapList.get(0);
		Map<String,Long> map = new HashMap<String, Long>();
		//Merge成为一个set
		for(String tag : baseMap.keySet()) {
			Long minTimes = baseMap.get(tag);
			int i;
			for(i = 1; i < mapList.size(); i++) { //对剩下的
				Map<String,Long> currentMap = mapList.get(i);
				Long currentTimes = currentMap.get(tag);
				if(currentTimes == null) //如果找不到
					break;
				else if(currentTimes.longValue() < minTimes.longValue()) {  //如果包含 而且　小于　当前的
					minTimes = currentTimes;  //记录好最小的值
				}				
			}
			if(i == mapList.size()) {//是正常跳出的,该tag同时与这些pivot一起出现
				if(!pivotSet.contains(tag)) //
					map.put(tag, minTimes); //添加到map中
			}			
		}
		
		return map;
	}
	
	

	/**
	 * 得到共同出现次数的map
	 * 使用lucence index的方法
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	private Map<String,Long> searchCoTagTimes1() throws CorruptIndexException, IOException {
		IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
		BooleanQuery andQuery = Utils.convertQueryListToQuery(this.queryTagList);
		
		Hits hits = searcher.search(andQuery);
		
		//统计该查询命中的
		this.docNum = hits.length();
		
		Map<String,Long> map = new HashMap<String,Long>();
		for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext();) {
			Hit hit = (Hit)it.next();
    		Document doc = hit.getDocument();
    		Field field = doc.getField(Constants.lucenceTagFieldName);
      		String str = field.stringValue();        		
      		String [] tagArray = str.split("\n");
    		//对文档中的每一个token
    		for(int i = 0; i < tagArray.length; i++) {
    			 String key = tagArray[i];
    			 if(pivotSet.contains(key)) {  //把pivot过滤掉
    				 continue;
    			 }
    			 Long times = map.get(key);
    			 if(times == null) {  //如果没有出现过
    				 Long value = new Long(1);
    				 map.put(key, value);
    			 } else {   //如果已经出现过 修改值
    				times = times.longValue();
    				times++;
    				map.put(key, new Long(times));
    			 }        
    		}        	
		}
		return map;
	}
	
	/*
	 * 备份好top K list的
	 * weight
	 */
	private Map<Long,TagFrequency2> getTopKTagMap() {
		Map<Long,TagFrequency2> map = new HashMap<Long,TagFrequency2>();
		
		for(TagFrequency2 tf : topKTagsList) {
			map.put(tf.getTagId(), tf);
		}
		
		return map;
	}
	

	public SelectTag(int k, List<QueryExpension> queryTagList, DataInput dataInput) {
		super();
		this.k = k;
		this.queryTagList = queryTagList;
		//构造真实的pivot 集合 用来过滤
		this.pivotSet = new HashSet<String> ();
		for(QueryExpension q : queryTagList) {
			this.pivotSet.add(q.getQueryWord());
		}
		
		this.dataInput = dataInput;
		try {
			this.allCoTagsList = getAllCoTags1();
			this.topKTagsList = getTopKTags();
			this.topkMap = getTopKTagMap();
		} catch (Exception e) {
			System.out.println("select tag ");
			e.printStackTrace();
		}
		
		//设置好idMap的关系
		createIdMap();
		
		this.simMatrix = createSimMatrix();		
		//for test
		System.out.println("the total num of co Tags is:" + allCoTagsList.size());
		System.out.println("the actual num of co Tags is:" + topKTagsList.size());
		//统计该查询命中的
		//System.out.println("image num for this query: " + docNum);
		
	}
	
	
	
	public Map<Long, TagFrequency2> getTopkMap() {
		return topkMap;
	}

	public void setTopkMap(Map<Long, TagFrequency2> topkMap) {
		this.topkMap = topkMap;
	}

	public static void main(String[] args) throws Exception {
		IndexService indexService = new IndexService();
		List<String> list = new ArrayList<String>();
		list.add("dog");
//		list.add("window");
//		list.add("baby");
//		list.add("movie");
		list.add("film");
//		list.add("poodle");
//		list.add("tv");
//		list.add("flower");
//		list.add("dog");
//		list.add("puppy");
//		list.add("weimaraner");
//		list.add("flower");
//		list.add("flowers");
//		list.add("bloom");
//		list.add("blooms");
//		list.add("blossom");
//		list.add("blossoms");
//		list.add("garden");
//		list.add("red");
//		list.add("white");
		double averageTime = 0;
		List<QueryExpension> pivotTagList = Utils.convertRawListToPivotTagList(list,indexService.getDataInput().getTagIndex(),true);
		if (pivotTagList.size() == 0) {
			System.out.println("can't find word ");	
			return ;
		}
		else {
			for (QueryExpension queryExpension : pivotTagList) {
				System.out.println("Query pivot word: "
						+ queryExpension.getQueryWord());
				System.out.print("Synset: ");
				for (String synTag : queryExpension.getSynWordSet()) {
					System.out.print(synTag + " ");
				}
				System.out.println("");
			}
			
			long startTime = System.currentTimeMillis();
			SelectTag select = new SelectTag(Constants.topK, pivotTagList,
					indexService.getDataInput());
			long endTime = System.currentTimeMillis();

			// print
			List<TagFrequency2> kList = select.getTopKTagsList();

			List<TagFrequency2> newList = new ArrayList<TagFrequency2>(kList);

			XStream xstream = new XStream();

			xstream.alias("tag", TagFrequency2.class);

			String result = xstream.toXML(newList);

			System.out.println(result);
			averageTime += endTime - startTime;

			System.out.println("average time: " + averageTime);
		}
		
		 
		/*
		 * List<TagFrequency2> kList = select.getTopKTagsList();
		 * 
		 * List<TagFrequency2> newList = new ArrayList<TagFrequency2>(kList);
		 * 
		 * XStream xstream = new XStream();
		 * 
		 * xstream.alias("tag", TagFrequency2.class);
		 * 
		 * String result = xstream.toXML(newList);
		 * 
		 * System.out.println(result);
		 */
		/*for(TagFrequency tf : kList) {
			System.out.println("");
		}*/
		
		
		//System.out.println("true: " + indexService.getDataInput().getTagCoFrequency(pivotTags));
		
		
		
		//DoubleMatrix2D matrix = select.getSimMatrix();
		
		//Utils.printMatrix(matrix);
		
		/*for(int i = 0; i < matrix.rows(); i++) {
			for(int j = 0; j < matrix.columns(); j++) {
				System.out.print(matrix.getQuick(i, j) + " ");
			}
			System.out.println();
		}*/
		
		//System.out.println(matrix.cardinality());
		
	}

	
	
}
