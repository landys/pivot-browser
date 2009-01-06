package DataIndex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import model.NeteaseVedioData;
import model.TagModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.HitIterator;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import utils.Constants;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.RCDoubleMatrix2D;

/*
 * ������lucence��index
 * �����ù�ͬ����Ƶ�ʵľ���
 * 
 */
public class DataInput implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -521571573588297315L;

	//tag��ͬ����Ƶ�ʾ���	
	//ʹ����colt�����sparse raw compress matrix��
	private DoubleMatrix2D coMatrix;
	
	//����Ƿֲ�ʽ�����е� 
	//�����sub����,���Ĵ�СΪ subsize * tagIndex.size();
	private DoubleMatrix2D subCoMatrix;
	
	//�ֲ�ʽ����ʱ��ʹ�õ���
	private int startRow;
	
	private int endRow;
	
	//tag�Լ�������Ӧ��matrix��index
	private Map<String,Long> tagIndex;
	
	//tag���ֵ���Ŀ��ǰ��n��
	private List<TagModel> tagCloud;	
	
	//map tagName to times
	private Map<String,Long> tagTimes;
	
	//id��index
	private Map<Long,String> idIndex;
	
	//doc ������
	private int totalDoc;
	
	private int coMatrixSize;
	
	/*
	 * �ֲ�ʽ������
	 * �����˷ֿ�ľ���
	 */
	private List<DataInput> subDataInputList;
	
	

	public List<DataInput> getSubDataInputList() {
		return subDataInputList;
	}

	public void setSubDataInputList(List<DataInput> subDataInputList) {
		this.subDataInputList = subDataInputList;
	}

	/*
	 * ����ʽ�����µĺ���
	 * 
	 */
	public double getCoMatrixValue(int row, int col) {
		return coMatrix.getQuick(row, col);
	}
	
//	/*
//	 * �÷ֲ�ʽ�Ĵ�����������
//	 * ����ԭ����getQuick����
//	 */
//	public double getCoMatrixValue(int row, int col) {
//		//��ÿ�����б���,���Ƿ�����ڸ�sub�ķ�Χ
//		for(int i = 0; i < subDataInputList.size(); i++) {
//			DataInput subDataInput = subDataInputList.get(i);
//			int startRow = subDataInput.getStartRow();
//			int endRow = subDataInput.getEndRow();
//			if(row >= startRow && row < endRow) { //�����֮��
//				return subDataInput.getSubCoMatrix().getQuick(row - startRow, col);
//			}
//		}
//		return 0;  //Ĭ��ֵ
//	}
//	
	
	/*
	 * ����ʽ�����¶�Ӧ�ĺ���
	 * 
	 */
	public DoubleMatrix1D getCoMatrixOneRow(int row) {
		return coMatrix.viewRow(row);
	}
	
	
//	/*
//	 * �÷ֲ�ʽ�Ĵ�����
//	 * ȡ��CoMatrix����
//	 * ��Ӧĳ���ʵ�һ����
//	 * 
//	 */
//	public DoubleMatrix1D getCoMatrixOneRow(int row) {
//		//��ÿ�����б���,���Ƿ�����ڸ�sub�ķ�Χ
//		for(int i = 0; i < subDataInputList.size(); i++) {
//			DataInput subDataInput = subDataInputList.get(i);
//			int startRow = subDataInput.getStartRow();
//			int endRow = subDataInput.getEndRow();			
//			if((row >= startRow && row < endRow)) { //�����֮��
//				return subDataInput.getSubCoMatrix().viewRow(row - startRow);
//			}
//			
//		}
//		return null;
//	}
	
	public int getCoMatrixSize() {
		return coMatrixSize;
	}


	public void setCoMatrixSize(int coMatrixSize) {
		this.coMatrixSize = coMatrixSize;
	}


	public DoubleMatrix2D getSubCoMatrix() {
		return subCoMatrix;
	}


	public void setSubCoMatrix(DoubleMatrix2D subCoMatrix) {
		this.subCoMatrix = subCoMatrix;
	}


	public void createTagsTimeFile() throws Exception {
		BufferedWriter wt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Constants.tagTimesFileName)));
		for(TagModel tm : tagCloud) {			
			//ֻ�����������			
			String tag = tm.getTagName();			
			int time = tm.getTimes();
			wt.write(tag + " " + time + "\n");
		}
		wt.close();
	}
	
	
	/*
	 * ȡ�ó���ĳ
	 * tag��������Ŀ
	 * ��ǰ������Ϊ���ִ���
	 */
	public Map<Long,Long> getSampleTagTimes()  {
		Map<Long,Long> map = new TreeMap<Long,Long>();
		
		for(Long time : tagTimes.values()) {
			Long value = map.get(time);
			if(value == null) {
				map.put(time, new Long(1));
			} else {
				int num = value.intValue();
				num++;
				map.put(time, new Long(num));
			}			
		}			
		
		return map;		
	}
	
	public int getTotalTagNum() {
		int num = 0;
		for(Long time : tagTimes.values()) {
			num += time.intValue();
		}
		return num;
	}
	
	
	public List<TagModel> getTagCloud() {
		return tagCloud;
	}


	public void setTagCloud(List<TagModel> tagCloud) {
		this.tagCloud = tagCloud;
	}
	
	
	//
	public static List<NeteaseVedioData> parseData(File file) {
		try {
			// ����ֵ
			List<NeteaseVedioData> result;
			// dom
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(file);

			// xpath����xml�ļ�
			XPath xpath = XPathFactory.newInstance().newXPath();

			// ��ȡ����
			result = retrieveContents(xpath, doc, "//orz/item");

			// ����NeteaseVedioData����
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ArrayList<NeteaseVedioData> retrieveContents(XPath xpath,
			org.w3c.dom.Document document, String xPathQuery) {
		try {
			XPathExpression expression = xpath.compile(xPathQuery);
			NodeList nodes = (NodeList) expression.evaluate(document,
					XPathConstants.NODESET);

			ArrayList<NeteaseVedioData> contents = new ArrayList<NeteaseVedioData>();
			for (int i = 0; i < nodes.getLength(); i++) {
				NeteaseVedioData data = new NeteaseVedioData();
				Node item = nodes.item(i);

				// ��������
				NodeList children = item.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node n = children.item(j);
					String nodeName = n.getNodeName();
					if (nodeName.equals("title")) {
						data.setTitle(n.getTextContent());
					} else if (nodeName.equals("description")) {
						data.setDescription(n.getTextContent());
					} else if (nodeName.equals("tags")) {
						data.setTags(n.getTextContent());
					} else if (nodeName.equals("videourl")) {
						data.setVideourl(n.getTextContent());
					} else if (nodeName.equals("snapshot")) {
						data.setSnapshot(n.getTextContent());
					}
				}
				contents.add(data);
			}
			return contents;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	


	/*
	 * ������lucence��index
	 * 
	 * 
	 */
	public static void createLuceneIndex() throws Exception {
		File  file = new File("orz.xml");
		List<NeteaseVedioData> list = parseData(file);
		
		//StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
		WhitespaceAnalyzer whiteSpaceAnalyzer = new WhitespaceAnalyzer(); 
		IndexWriter writer = new IndexWriter(Constants.lucencePath,whiteSpaceAnalyzer,true);
		writer.setUseCompoundFile(false);
		
		int i = 0;
		for(NeteaseVedioData videoData : list) {
			String tags = videoData.getTags();
			String description = videoData.getDescription();
			String title = videoData.getTitle();
			String videourl = videoData.getVideourl();
			String snapshot = videoData.getSnapshot();
			
			Document doc = new Document();
			
			doc.add( new Field(Constants.lucenceTagFieldName, tags,Field.Store.YES,Field.Index.TOKENIZED, Field.TermVector.YES));
			
			doc.add( new Field(Constants.lucenceDescriptionFieldName, description ,Field.Store.YES,Field.Index.UN_TOKENIZED));	
		    
			doc.add( new Field(Constants.lucenceTitleFieldName, title ,Field.Store.YES,Field.Index.UN_TOKENIZED));	
		    
			doc.add( new Field(Constants.lucenceVideourlFieldName, videourl ,Field.Store.YES,Field.Index.UN_TOKENIZED));	
		    
			doc.add( new Field(Constants.lucenceSnapshotFieldName, snapshot ,Field.Store.YES,Field.Index.UN_TOKENIZED));	    
			    
			writer.addDocument(doc);
			i++;
			System.out.println("precessing " + i + " doc ");
			
		}	
		writer.close();
		System.out.println("lucence index are created");
	}	
	
	public static void readWaveLetFile(String waveLetFeaturePath , ByteBuffer bb) throws Exception {
		//int dim = 64;
		//double [] waveLetArray = new double [dim];
		File waveLetFile = new File(waveLetFeaturePath);
		BufferedReader waveLetBr = new BufferedReader(new InputStreamReader(new FileInputStream(waveLetFile)));
		String line = waveLetBr.readLine();
		//�ָ� �ո�
		String [] waveLet = line.split(" ");
		//����ÿһdim��ֵ
		for(int j = 0 ; j < waveLet.length ; j++) {
			double value = Double.parseDouble(waveLet[j]);
			bb.putDouble(value);
		}			
		waveLetBr.close();
	}
	
	public static void readColorFile(String colorFeaturePath, ByteBuffer bb) throws Exception  {
		int dim = 3;
		//int binNum = 16;
		//double [][] colorArray = new double [dim][binNum];
		File colorFile = new File(colorFeaturePath);
	    BufferedReader colorBr = new BufferedReader(new InputStreamReader(new FileInputStream(colorFile)));
		for(int i = 0; i < dim ; i++) {
			//����һ��
			String line = colorBr.readLine();
			//�ָ� �ո�
			String [] color = line.split(" ");
			//����ÿһ��bin��ֵ
			for(int j = 0 ; j < color.length ; j++) {
				//colorArray[i][j] = Double.parseDouble(color[j]);
				double value = Double.parseDouble(color[j]);
				bb.putDouble(value);
			}			
		}
		colorBr.close();		
	}
		
	
	/*
	 * ȡ��tag �Լ����ֵĴ���
	 * 
	 */
	public int getTagSelfFrequency(String tag) throws Exception {
		IndexReader reader = IndexReader.open(Constants.lucencePath);
		Term term = new Term(Constants.lucenceTagFieldName,tag);		
        int freq = reader.docFreq(term);
        reader.close();
        return freq;
	}
	
	public int getTwoTagFrequency(String tagA, String tagB) throws Exception {		
		IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);		
        Hits hits = null;  
        Term t1 = new Term(Constants.lucenceTagFieldName,tagA);
        Term t2 = new Term(Constants.lucenceTagFieldName,tagB);
        TermQuery q1 = new TermQuery(t1);
        TermQuery q2 = new TermQuery(t2);
        BooleanQuery query = new BooleanQuery(); 
        query.add(q1,BooleanClause.Occur.MUST);
        query.add(q2,BooleanClause.Occur.MUST);
    	hits = searcher.search(query);        
		return hits.length();
	}
	
	public int getTagCoFrequency(List<String> tagList) {
		try {
			IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);
			BooleanQuery query = new BooleanQuery();
			for (String tag : tagList) {
				Term t = new Term(Constants.lucenceTagFieldName, tag);
				TermQuery q = new TermQuery(t);
				query.add(q, BooleanClause.Occur.MUST);
			}
			Hits hits = searcher.search(query);
			return hits.length();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	private int getTotalDocsNum() throws CorruptIndexException, IOException {
		IndexReader reader = IndexReader.open(Constants.lucencePath);
		int totalNum = reader.numDocs();
		reader.close();
		return totalNum;
	}
	
	/*
	 * ����tagName �� times��map
	 * 
	 */
	private Map<String,Long> createtagTimesMap() throws Exception {
		Map<String,Long> map = new HashMap<String,Long>();
		IndexReader reader = IndexReader.open(Constants.lucencePath);		
        TermEnum termEnum = reader.terms();		
		while (termEnum.next()) {
			Term t = termEnum.term();
			String tagName = t.text();
			int times = termEnum.docFreq();
			if(t.field().equals(Constants.lucenceTagFieldName))
				map.put(tagName, new Long(times));
		}	
		reader.close();
		return map;
	}
	
	/*
	 * �����ð���times���ִ��������list
	 * 
	 */
	private List<TagModel> createtagCloudList() throws Exception {
		List<TagModel> list = new ArrayList<TagModel>();
		IndexReader reader = IndexReader.open(Constants.lucencePath);
        TermEnum termEnum = reader.terms();		
		while (termEnum.next()) {
			Term t = termEnum.term();
			if(!t.field().equals(Constants.lucenceTagFieldName)) {  //�������tag field
				continue;
			}
			String tagName = t.text();
			int times = termEnum.docFreq();
			TagModel tagModel = new TagModel();
			tagModel.setTagName(tagName);
			tagModel.setTimes(times);
			list.add(tagModel);
		}	
		reader.close();
		Collections.sort(list);
		return list;
	}
	
	/*
	 * 
	 * tag�Լ�������Ӧ��Map��index
	 * 
	 */
	
	private Map<String,Long> createTagIdMap() throws Exception {
		Map<String,Long> map = new HashMap<String,Long>();
		long id = 0;
        IndexReader reader = IndexReader.open(Constants.lucencePath);
        TermEnum termEnum = reader.terms();		
		while (termEnum.next()) {
			Term t = termEnum.term();
			//ֻ��Constants.lucenceTagFieldName�ż��뵽map
			if(t.field().equals(Constants.lucenceTagFieldName)) {
				map.put(t.text(), id);
				id++;
			}
		}		
		reader.close();
		System.out.println("TagIdMap is created");
		return map;	
	}
	
	/*
	 * 
	 * ����reverse map
	 * 
	 */
	
	private Map<Long,String> createIdTagMap() throws Exception {
		Map<Long,String> map = new TreeMap<Long,String>();		
		for(String s: tagIndex.keySet()) {
			map.put(tagIndex.get(s), s);
		}		
		System.out.println("IdTagMap is created");
		return map;	
	}
	
	private List<String> getOtherTag(String tag, Set<String> tagSet) { 
		List<String> list = new ArrayList<String> ();
		for(String s : tagSet) {
			if(!s.equals(tag)) {
				list.add(s);
			}
		}
		return list;
	}
	/*
	 * tag��ͬ����Ƶ�ʾ���
	 * �������㷨,�������㷨һ
	 * 
	 */
	@SuppressWarnings("unused")
	private RCDoubleMatrix2D createCoFreqMatrix1() throws Exception {
		int size = tagIndex.size();
		RCDoubleMatrix2D matrix = new RCDoubleMatrix2D(size,size);		
		
        for(String tag : tagIndex.keySet()) {
        	List<String> list = getOtherTag(tag,tagIndex.keySet());
        	int row = tagIndex.get(tag).intValue();
        	System.out.println("CoFreqMatrix row(" + row + ")  " + tag + " is created "  );
        	for(String s : list) {	
        		int col = tagIndex.get(s).intValue();
        		//���û�����ù�
        		if(matrix.getQuick(row, col) == 0) {
        			int coTimes = getTwoTagFrequency(tag,s);
        			if(coTimes == 0)  //�����ͬ���ִ���Ϊ 0 ������֮
        				continue;
        			//�Գ�����
        			matrix.setQuick(row, col, coTimes);
        			matrix.setQuick(col, row, coTimes);
        		}
        	}
        	//�Լ����ֵ�Ƶ��
        	int times = getTagSelfFrequency(tag);
        	matrix.setQuick(row, row, times);        	
        }
        System.out.println("CoFreqMatrix is created");
        return matrix;
		
	}

	

//	public DoubleMatrix2D getCoMatrix() {
//		return coMatrix;
//	}


	public void setCoMatrix(DoubleMatrix2D coMatrix) {
		this.coMatrix = coMatrix;
	}


	public Map<String, Long> getTagIndex() {
		return tagIndex;
	}


	public void setTagIndex(Map<String, Long> tagIndex) {
		this.tagIndex = tagIndex;
	}


	public Map<String, Long> getTagTimes() {
		return tagTimes;
	}


	public void setTagTimes(Map<String, Long> tagTimes) {
		this.tagTimes = tagTimes;
	}


	/*
	 * ��ɳ����ƾ��󹹽����һϵ�й���
	 * 
	 */
	public void init() throws Exception, IOException {
		//�ú����Ѿ����޸ĳ�static �� DataInput����ǰִ��
		createLuceneIndex();	
		this.totalDoc = getTotalDocsNum();
		System.out.println("total docs: " + totalDoc);
		System.out.println("create tagCloud start");
		this.tagCloud = createtagCloudList();
		System.out.println("create tagCloud end");
		createTagsTimeFile();
		System.out.println("create tagTimes start");
		this.tagTimes = createtagTimesMap();
		System.out.println("create tagTimes end");
		System.out.println("create tagIndex start");
		this.tagIndex = createTagIdMap();
		System.out.println("create tagIndex end");
		System.out.println("create idIndex start");
		this.idIndex = createIdTagMap();
		System.out.println("create idIndex end");
		this.coMatrixSize = tagCloud.size();
	}
	/*
	 * ����ʽ���е�
	 * ���캯��
	 */
	public DataInput(boolean isNew) throws Exception {
		init();
		if(isNew == false) {
			System.out.println("create coMatrix start");
			this.coMatrix = createCoFreqMatrix2();		
			System.out.println("create coMatrix end");
		}
	}
	
	
	
	/*
	 * �ֲ�ʽ���еĹ��캯��
	 * �����ֱ���
	 * Ҫ�����coMatrix��startRow �� endRow
	 * [startRow,endRow)
	 */
	public DataInput(int startRow, int endRow) throws Exception {
		init();
		System.out.println("create subCoMatrix [" + startRow + "," + endRow + ") " +  " start");
		this.startRow = startRow;
		this.endRow = endRow;
		this.subCoMatrix = createCoFreqMatrix2(startRow,endRow);		
		System.out.println("create subCoMatrix [" + startRow + "," + endRow + ") " +  " end");
	}
	

	public Map<Long, String> getIdIndex() {
		return idIndex;
	}


	public void setIdIndex(Map<Long, String> idIndex) {
		this.idIndex = idIndex;
	}
	
	
	/*
	 * 
	 * �㷨��
	 * 
	 */
	
	public int getEndRow() {
		return endRow;
	}


	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}


	public int getStartRow() {
		return startRow;
	}


	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}


	/*
	 * �ú�����Ϊ���ܹ��ֲ�ʽ�����ʹ�õ�
	 * Ŀ���ǽ�Ŀ��ֿ鿪���� 
	 * ���� [startRow,endRow)���� 
	 * 
	 */
	private DoubleMatrix2D createCoFreqMatrix2(int startRow, int endRow) throws Exception {
		int size = tagIndex.size();
		int rowInterval = endRow - startRow;
		//��matrix��һ�� rowInterval * size��С��
		RCDoubleMatrix2D matrix = new RCDoubleMatrix2D(rowInterval,size);	
        
		int i = 0;
		IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);			
        for(Long row : idIndex.keySet()) { //��Ϊ���tagId˳���ǹ̶���Ҳ�������е��ֵ�����    	
        	if(row < startRow)  //�����ǰ��tagIdС��Ҫ�����Id��ʼֵ
        		continue;
        	if(row >= endRow)	  //�����ǰ��tagId����Ҫ�����Id����ֵ
        		break;
        	String tagName = idIndex.get(row); 
        	Term t = new Term(Constants.lucenceTagFieldName,tagName);
        	Query query = new TermQuery(t);
        	Hits hits = searcher.search(query);        	
        	System.out.println("CoFreqMatrix row(" + row + ")  " + tagName + " is created "  );
        	//��ÿ��hit ��doc ������tags��map��
        	for(HitIterator  it = (HitIterator)hits.iterator(); it.hasNext();) {
        		Hit hit = (Hit)it.next();
        		Document doc = hit.getDocument();
        		Field field = doc.getField(Constants.lucenceTagFieldName);
        		Analyzer aAnalyzer = new WhitespaceAnalyzer();
        		String str = field.stringValue();        		
        		StringReader sr = new StringReader(str);
        		TokenStream tokenStream = aAnalyzer.tokenStream(Constants.lucenceTagFieldName,sr);
        		//���ĵ��е�ÿһ��token
        		while(true) {
        			 Token token = tokenStream.next();
        			 if(token == null) {
        				 break;
        			 }
        			 String tag = token.termText();
        			 int col = tagIndex.get(tag).intValue();
        			 //��ͬ���ִ���
        			 double times = matrix.getQuick(row.intValue() - startRow, col);
        			 times++;
        			 matrix.setQuick(row.intValue() - startRow, col, times);        
        		}        		
        	}
        	i++;        	
        } 
        return matrix;
	}
	
	
	/*
	 * �ú����Ǽ���ʽ�����
	 * ��û���ƶ�startId��endId
	 */
	private DoubleMatrix2D createCoFreqMatrix2() throws Exception {
		int startRow = 0;
		int endRow = tagIndex.size();
		return createCoFreqMatrix2(startRow,endRow);
	}


	public int getTotalDoc() {
		return totalDoc;
	}


	public void setTotalDoc(int totalDoc) {
		this.totalDoc = totalDoc;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("start create lucene index");
		long startTime = System.currentTimeMillis();
		createLuceneIndex();
		long endTime = System.currentTimeMillis();
		System.out.println("create lucene index end ");
		System.out.println("the total time is: " + (endTime -startTime) + " ms");
	}	

}

class FileNameFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {			
		if(name.endsWith(".txt"))
			return true;
		else
			return false;
	}		
}