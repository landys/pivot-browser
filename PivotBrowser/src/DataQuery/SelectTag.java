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
	
	//��ѯ���б�
	//ע������
	//������һ��Map<String,List>��key��ͬ��ʼ��ϵĲ�ѯ��,value��ͬ�弯��list ͬ�弯���ڲ���һ��or��ϵ 
	//�����list��һ��and�Ĺ�ϵ
	private List<QueryExpension> queryTagList;
	
	//pivot����
	//�Ӳ�ѯlist�����queryWordѡ����
	private Set<String> pivotSet;
	
	private int docNum;
	
	//idӰ���ϵ ��Ӧ��simMatrix����
	//ǰ��һ��idΪԭʼ��id ����һ��idΪsimMatrix�����id
	private Map<Long,Long> rawIdMapId; 
	
	//id��Ӱ���ϵ ��Ӧ��simMatrix����
	//ǰ��һ��idΪsimMatrix�����id ����һ��λԭʼ��id
	private Map<Long,Long> idMapRawId; 
	
	//top k���ƶȾ���
	//����Ϊϡ�衡Ҳ����Ϊ�ܼ���
	//ȡ���ڡ�topKTagsList������ĸ���
	private DoubleMatrix2D simMatrix;
	
	
	//new
	private Map<String,Long> searchCoTagMap;
	
	//����ʱ���õ�
	//������������õ�ʱ��
	public static long time1;
	
	//�������ڴ�ȡtopK���õ�ʱ��
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
	 * ��ŷ�ʽΪ������topk list�����˳��
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
	 * �������ƾ��� top k�����ƾ���
	 * 
	 */
	private DoubleMatrix2D createSimMatrix() {
		int size = topKTagsList.size();
		DoubleMatrix2D matrix = new DenseDoubleMatrix2D(size, size);		
		
		//DoubleMatrix2D coMatrix = dataInput.getCoMatrix();		
	
		for(TagFrequency2 tf : topKTagsList) {
			Long row = tf.getTagId();
			int simRow = rawIdMapId.get(row).intValue();
//			���ڵķ���
			for(Long id : rawIdMapId.keySet()) {
				int simCol = rawIdMapId.get(id).intValue();
				//ʹ�� (a��b)/(a��b)������ƶ�
				//double f1 = coMatrix.getQuick(row.intValue(), id.intValue());
				//coMatrix�ֿ鴦��
				double f1 = dataInput.getCoMatrixValue(row.intValue(), id.intValue());
				//double f2 = coMatrix.getQuick(row.intValue(), row.intValue()) + coMatrix.getQuick(id.intValue(), id.intValue());
				double f2 = dataInput.getCoMatrixValue(row.intValue(), row.intValue()) + dataInput.getCoMatrixValue(id.intValue(), id.intValue());
				matrix.setQuick(simRow,simCol, f1/f2);
			}
//			ԭ���ķ���			
//			for(int i = 0; i < viewRow.size(); i++) {
//				//ֻ���������top k�е�����				
//				if(rawIdMapId.containsKey(new Long(i))) {
//					int simRow = rawIdMapId.get(row).intValue();
//					int simCol = rawIdMapId.get((long)i).intValue();
//					
//					//ʹ�� (a��b)/(a��b)������ƶ�
//					double f1 = coMatrix.getQuick((int)row, (int)i);
//					//�е�����
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
	 * ���㷨
	 * ȡǰ���top k��
	 * ������ڵĻ�
	 * ���򷵻�ȫ��List
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
		//pivot�Ĺ�ϵӦ����һ�� and�Ĺ�ϵ
		for(String s : pivotTags) {
			Long rowId = tagIndex.get(s);
			
			// �����pivot ������, Ŀǰ�������  ����������һ�� ,����Ҫ����չ��
			if(rowId == null) {
				System.out.println("tag: " + s + " is not exist");
				continue; 
			}
			
			int row = rowId.intValue();
			DoubleMatrix1D viewRow = coMatrix.viewRow(row);
			for(int i = 0 ; i < viewRow.size() ; i++) {				
				
				if(i == row)
					continue;
				//������pivot�����ƶ������ ��ӵ�list����
				if(viewRow.get(i) != 0) {
					TagFrequency tf = map.get(new Long(i));
					double selfFreq = coMatrix.getQuick(i, i);
					double coFreq = coMatrix.getQuick(row, i);
					//ԭ�����á�cof/self ��Ϊ��score
					//double score = coFreq /selfFreq;
					//Ŀǰ�����������֮����totalDoc
					float totalDoc = dataInput.getTotalDoc();
					double score = coFreq/totalDoc * Math.log(totalDoc/selfFreq);
					//double score = coFreq * Math.log(100000/selfFreq);
					//���û�д���
					if( tf == null) {
						tf = new TagFrequency(pivotNum);						
						tf.setTagId(i);
						//for test
						//tf.setTag(idIndex.get((long)i));						
						tf.setSelfFreq(selfFreq);
						tf.setCoFreq(coFreq, j);
						tf.setScore(score, j);
						map.put(new Long(i), tf);
					} else { //����Ѿ�����
						//�����������㷨
						//�������㷨һ
						//����Ѿ����� �Ƚ�score �ߵĻ�����ԭ����cofreq �Լ� score
//						if(tf.getScore() < score) {
//							tf.setCoFreq(coFreq);						
//							tf.setScore(score);
//						}
						//�㷨�� ֱ�����ý�ȥ
						tf.setCoFreq(coFreq, j);
						tf.setScore(score, j);						
					}
				}
			}
			j++;
		}

		//ת����list
		for(Long id : map.keySet()) {
			TagFrequency tf = map.get(id);
			double finalScore = 1;
			//  ���� �ܷ� �ܷ�Ϊ ������ضȵ� �˻�
			for(int i = 0; i < tf.getPivotNum(); i++) {
				finalScore *= tf.getScore()[i];
			}
			if(finalScore != 0) {   //����÷ֲ�Ϊ0
				tf.setFinalScore(finalScore);			
				list.add(tf);
			}
		}		
		//����, ����finalscore
		Collections.sort(list);		
		return list;
		
		
	}*/
	
	
	/*
	 * ���㷨
	 * �Ӳ�ѯ��pivot����ѡ
	 * �㷨һ
	 * 
	 * ʹ�� Tc * log(totalDoc/selfFreq)
	 * selfFreq Ϊ���е�
	 * 
	 */
	private List<TagFrequency2> getAllCoTags1 () throws Exception {
		//����ʱ���õ�
		long startTime = System.currentTimeMillis();
		searchCoTagMap = searchCoTagTimes2();
//		if(this.queryTagList.size() == 1) { //speedup ���pivotֻ��һ��
//			searchCoTagMap = searchCoTagTimes2();
//		} else { //�ж��
//			searchCoTagMap = searchCoTagTimes1();	
//		}
			
		//����ʱ��
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
			if(rowId == null) {  // �������ˡ�
				continue;
			}
			//�ֿ鴦��
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
	 * �㷨��
	 * 
	 * ʹ�� ���е�selfFreq��
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
			//�ֿ鴦��
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
	 * �㷨��
	 * ��coTimes
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
			//�ֿ鴦��
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
	 * ����
	 * �Ƚ���
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
	 * �õ���ͬ���ִ�����map ʹ����coMatrix�ķ���
	 * 
	 * ���ֻ������һ��pivot����� ͬ��ʼ�������ѡ��� and��ϵ��������ѡ��С
	 * 
	 * ����ms���в�������
	 */
	private Map<String,Long> searchCoTagTimes2() {			
		Map<String,Long> tagIndex = dataInput.getTagIndex();
		Map<Long,String> idIndex = dataInput.getIdIndex();
		List<Map<String,Long>> mapList = new ArrayList<Map<String,Long>>();
		for (QueryExpension queryExpension : queryTagList) { //��ÿ�� ��ѯ�ġ���չ
			Set<String> pivotSynWordSet = queryExpension.getSynWordSet();//ͬ��ʼ���
			Map<String,Long> map = new HashMap<String,Long>();
			for (String pivotSynWord : pivotSynWordSet) {
				Long id = tagIndex.get(pivotSynWord);
				// ȡ������
				DoubleMatrix1D vector = dataInput.getCoMatrixOneRow(id
						.intValue());
				// �Ը��е�ÿ����0��Ԫ�� ����
				for (int i = 0; i < vector.size(); i++) {
					Double value = vector.getQuick(i);
					if (value != 0) { // ��value�����ڣ���ÿһ��
						String key = idIndex.get(new Long(i)); // ȡ�ø�id�Ķ�Ӧtag
						Long times = map.get(key);
						if (times == null) { // ���û�г��ֹ�
							map.put(key, new Long(value.longValue()));
						} else { // ����Ѿ����ֹ� �������ڵĵ�ԭ��������
							times = times.longValue();
							if(value > times) {  //ȡ���Ĵ����Ǹ�
								times = value.longValue();
								map.put(key, new Long(times));
							}							
						}
					}
				}
			}
			mapList.add(map);
		}
		//���մ�С����
		Collections.sort(mapList, new MapComparator());
		
		//ȡ��С���Ǹ���Ϊbase ���ﱣ֤������һ��
		Map<String,Long> baseMap = mapList.get(0);
		Map<String,Long> map = new HashMap<String, Long>();
		//Merge��Ϊһ��set
		for(String tag : baseMap.keySet()) {
			Long minTimes = baseMap.get(tag);
			int i;
			for(i = 1; i < mapList.size(); i++) { //��ʣ�µ�
				Map<String,Long> currentMap = mapList.get(i);
				Long currentTimes = currentMap.get(tag);
				if(currentTimes == null) //����Ҳ���
					break;
				else if(currentTimes.longValue() < minTimes.longValue()) {  //������� ���ҡ�С�ڡ���ǰ��
					minTimes = currentTimes;  //��¼����С��ֵ
				}				
			}
			if(i == mapList.size()) {//������������,��tagͬʱ����Щpivotһ�����
				if(!pivotSet.contains(tag)) //
					map.put(tag, minTimes); //��ӵ�map��
			}			
		}
		
		return map;
	}
	
	

	/**
	 * �õ���ͬ���ִ�����map
	 * ʹ��lucence index�ķ���
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	private Map<String,Long> searchCoTagTimes1() throws CorruptIndexException, IOException {
		IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
		BooleanQuery andQuery = Utils.convertQueryListToQuery(this.queryTagList);
		
		Hits hits = searcher.search(andQuery);
		
		//ͳ�Ƹò�ѯ���е�
		this.docNum = hits.length();
		
		Map<String,Long> map = new HashMap<String,Long>();
		for (HitIterator it = (HitIterator) hits.iterator(); it.hasNext();) {
			Hit hit = (Hit)it.next();
    		Document doc = hit.getDocument();
    		Field field = doc.getField(Constants.lucenceTagFieldName);
      		String str = field.stringValue();        		
      		String [] tagArray = str.split("\n");
    		//���ĵ��е�ÿһ��token
    		for(int i = 0; i < tagArray.length; i++) {
    			 String key = tagArray[i];
    			 if(pivotSet.contains(key)) {  //��pivot���˵�
    				 continue;
    			 }
    			 Long times = map.get(key);
    			 if(times == null) {  //���û�г��ֹ�
    				 Long value = new Long(1);
    				 map.put(key, value);
    			 } else {   //����Ѿ����ֹ� �޸�ֵ
    				times = times.longValue();
    				times++;
    				map.put(key, new Long(times));
    			 }        
    		}        	
		}
		return map;
	}
	
	/*
	 * ���ݺ�top K list��
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
		//������ʵ��pivot ���� ��������
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
		
		//���ú�idMap�Ĺ�ϵ
		createIdMap();
		
		this.simMatrix = createSimMatrix();		
		//for test
		System.out.println("the total num of co Tags is:" + allCoTagsList.size());
		System.out.println("the actual num of co Tags is:" + topKTagsList.size());
		//ͳ�Ƹò�ѯ���е�
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
