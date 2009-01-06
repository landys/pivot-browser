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
 * 建立好lucence的index
 * 建立好共同出现频率的矩阵
 * 
 */
public class DataInput implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -521571573588297315L;

	//tag共同出现频率矩阵	
	//使用了colt里面的sparse raw compress matrix类
	private DoubleMatrix2D coMatrix;
	
	//如果是分布式来运行的 
	//这个是sub矩阵,它的大小为 subsize * tagIndex.size();
	private DoubleMatrix2D subCoMatrix;
	
	//分布式处理时候使用到的
	private int startRow;
	
	private int endRow;
	
	//tag以及跟它对应的matrix的index
	private Map<String,Long> tagIndex;
	
	//tag出现的数目中前面n个
	private List<TagModel> tagCloud;	
	
	//map tagName to times
	private Map<String,Long> tagTimes;
	
	//id的index
	private Map<Long,String> idIndex;
	
	//doc 的总数
	private int totalDoc;
	
	private int coMatrixSize;
	
	/*
	 * 分布式处理方法
	 * 保存了分块的矩阵
	 */
	private List<DataInput> subDataInputList;
	
	

	public List<DataInput> getSubDataInputList() {
		return subDataInputList;
	}

	public void setSubDataInputList(List<DataInput> subDataInputList) {
		this.subDataInputList = subDataInputList;
	}

	/*
	 * 集中式环境下的函数
	 * 
	 */
	public double getCoMatrixValue(int row, int col) {
		return coMatrix.getQuick(row, col);
	}
	
//	/*
//	 * 用分布式的处理方法来处理
//	 * 代替原来的getQuick函数
//	 */
//	public double getCoMatrixValue(int row, int col) {
//		//对每个进行遍历,看是否存在于该sub的范围
//		for(int i = 0; i < subDataInputList.size(); i++) {
//			DataInput subDataInput = subDataInputList.get(i);
//			int startRow = subDataInput.getStartRow();
//			int endRow = subDataInput.getEndRow();
//			if(row >= startRow && row < endRow) { //在这个之间
//				return subDataInput.getSubCoMatrix().getQuick(row - startRow, col);
//			}
//		}
//		return 0;  //默认值
//	}
//	
	
	/*
	 * 集中式环境下对应的函数
	 * 
	 */
	public DoubleMatrix1D getCoMatrixOneRow(int row) {
		return coMatrix.viewRow(row);
	}
	
	
//	/*
//	 * 用分布式的处理方法
//	 * 取得CoMatrix里面
//	 * 对应某个词的一个行
//	 * 
//	 */
//	public DoubleMatrix1D getCoMatrixOneRow(int row) {
//		//对每个进行遍历,看是否存在于该sub的范围
//		for(int i = 0; i < subDataInputList.size(); i++) {
//			DataInput subDataInput = subDataInputList.get(i);
//			int startRow = subDataInput.getStartRow();
//			int endRow = subDataInput.getEndRow();			
//			if((row >= startRow && row < endRow)) { //在这个之间
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
			//只处理中文情况			
			String tag = tm.getTagName();			
			int time = tm.getTimes();
			wt.write(tag + " " + time + "\n");
		}
		wt.close();
	}
	
	
	/*
	 * 取得出现某
	 * tag次数的数目
	 * 当前抽样的为出现次数
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
			// 返回值
			List<NeteaseVedioData> result;
			// dom
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(file);

			// xpath解析xml文件
			XPath xpath = XPathFactory.newInstance().newXPath();

			// 获取数据
			result = retrieveContents(xpath, doc, "//orz/item");

			// 构造NeteaseVedioData对象
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

				// 创建数据
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
	 * 建立好lucence的index
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
		//分割 空格
		String [] waveLet = line.split(" ");
		//读入每一dim的值
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
			//读入一行
			String line = colorBr.readLine();
			//分割 空格
			String [] color = line.split(" ");
			//读入每一个bin的值
			for(int j = 0 ; j < color.length ; j++) {
				//colorArray[i][j] = Double.parseDouble(color[j]);
				double value = Double.parseDouble(color[j]);
				bb.putDouble(value);
			}			
		}
		colorBr.close();		
	}
		
	
	/*
	 * 取得tag 自己出现的次数
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
	 * 创建tagName 和 times的map
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
	 * 创建好按照times出现次数排序的list
	 * 
	 */
	private List<TagModel> createtagCloudList() throws Exception {
		List<TagModel> list = new ArrayList<TagModel>();
		IndexReader reader = IndexReader.open(Constants.lucencePath);
        TermEnum termEnum = reader.terms();		
		while (termEnum.next()) {
			Term t = termEnum.term();
			if(!t.field().equals(Constants.lucenceTagFieldName)) {  //如果不是tag field
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
	 * tag以及跟它对应的Map的index
	 * 
	 */
	
	private Map<String,Long> createTagIdMap() throws Exception {
		Map<String,Long> map = new HashMap<String,Long>();
		long id = 0;
        IndexReader reader = IndexReader.open(Constants.lucencePath);
        TermEnum termEnum = reader.terms();		
		while (termEnum.next()) {
			Term t = termEnum.term();
			//只有Constants.lucenceTagFieldName才加入到map
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
	 * 创建reverse map
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
	 * tag共同出现频率矩阵
	 * 有两种算法,下面是算法一
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
        		//如果没有设置过
        		if(matrix.getQuick(row, col) == 0) {
        			int coTimes = getTwoTagFrequency(tag,s);
        			if(coTimes == 0)  //如果共同出现次数为 0 不设置之
        				continue;
        			//对称设置
        			matrix.setQuick(row, col, coTimes);
        			matrix.setQuick(col, row, coTimes);
        		}
        	}
        	//自己出现的频率
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
	 * 完成除相似矩阵构建外的一系列功能
	 * 
	 */
	public void init() throws Exception, IOException {
		//该函数已经被修改成static 在 DataInput构造前执行
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
	 * 集中式运行的
	 * 构造函数
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
	 * 分布式运行的构造函数
	 * 参数分别是
	 * 要处理的coMatrix的startRow 和 endRow
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
	 * 算法二
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
	 * 该函数是为了能够分布式处理才使用的
	 * 目的是将目标分块开处理 
	 * 处理 [startRow,endRow)的行 
	 * 
	 */
	private DoubleMatrix2D createCoFreqMatrix2(int startRow, int endRow) throws Exception {
		int size = tagIndex.size();
		int rowInterval = endRow - startRow;
		//该matrix是一个 rowInterval * size大小的
		RCDoubleMatrix2D matrix = new RCDoubleMatrix2D(rowInterval,size);	
        
		int i = 0;
		IndexSearcher searcher = new IndexSearcher(Constants.lucencePath);			
        for(Long row : idIndex.keySet()) { //因为这个tagId顺序是固定的也就是所有的字典序列    	
        	if(row < startRow)  //如果当前的tagId小于要处理的Id开始值
        		continue;
        	if(row >= endRow)	  //如果当前的tagId大于要处理的Id结束值
        		break;
        	String tagName = idIndex.get(row); 
        	Term t = new Term(Constants.lucenceTagFieldName,tagName);
        	Query query = new TermQuery(t);
        	Hits hits = searcher.search(query);        	
        	System.out.println("CoFreqMatrix row(" + row + ")  " + tagName + " is created "  );
        	//对每个hit 的doc 加入其tags到map中
        	for(HitIterator  it = (HitIterator)hits.iterator(); it.hasNext();) {
        		Hit hit = (Hit)it.next();
        		Document doc = hit.getDocument();
        		Field field = doc.getField(Constants.lucenceTagFieldName);
        		Analyzer aAnalyzer = new WhitespaceAnalyzer();
        		String str = field.stringValue();        		
        		StringReader sr = new StringReader(str);
        		TokenStream tokenStream = aAnalyzer.tokenStream(Constants.lucenceTagFieldName,sr);
        		//对文档中的每一个token
        		while(true) {
        			 Token token = tokenStream.next();
        			 if(token == null) {
        				 break;
        			 }
        			 String tag = token.termText();
        			 int col = tagIndex.get(tag).intValue();
        			 //共同出现次数
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
	 * 该函数是集中式处理的
	 * 它没有制定startId和endId
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