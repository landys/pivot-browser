package utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.DataFormatException;

import model.QueryExpension;

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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import DataIndex.DataInput;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class Utils {
	
	public static void printMatrix(DoubleMatrix2D matrix) {
		for(int i = 0; i < matrix.rows(); i++) {
			for(int j = 0; j < matrix.columns(); j++) {
				DecimalFormat df = new DecimalFormat("#.##");
				df.setMinimumFractionDigits(3);
				df.setMaximumFractionDigits(3);
				System.out.print(df.format(matrix.getQuick(i, j)) + " ");
			}
			System.out.println();
		}
	}
	
	/*
	 * 用来扩展接口
	 * 
	 * 如果rawList中有tagIndex找不到的tag
	 * 那么自动返回空的 pivotTagList
	 * 
	 */
	public static List<QueryExpension> convertRawListToPivotTagList(List<String> rawList, DataInput dataInput, boolean withSynWordExpension, boolean withCoMapExpension, int minFreq, int topKForExpension) throws Exception, IOException {
		List<QueryExpension> pivotTagList = new ArrayList<QueryExpension>();
		Map<String,Long> tagIdMap = dataInput.getTagIndex();
	
		
		//for 同义词扩展
		IndexSearcher searcher = null;
		if(withSynWordExpension)  //开关
			searcher = new IndexSearcher(Constants.synsetIndexDir);
		for(String rawTag : rawList) {
			String queryWord = rawTag;	
			if(!tagIdMap.containsKey(queryWord)) { //如果不存在该queryWord
				return new ArrayList<QueryExpension>() ;	//继续下一个
			}			
			QueryExpension querExpension = new QueryExpension();
			querExpension.setQueryWord(queryWord);
			Set<String>  synWordSet = new HashSet<String> ();
			//先添加好该查询词本身先
			synWordSet.add(queryWord);
			
			//如果需要同义词扩展
			if (withSynWordExpension) {
				// 根据同义词set获取该word的所有同义词set
				Term term = new Term(Constants.synsetIndexContentPathFieldName,
						queryWord);
				Query synsetQuery = new TermQuery(term);
				Hits hits = searcher.search(synsetQuery);
				// 添加所有的同义词
				for (HitIterator it = (HitIterator) hits.iterator(); it
						.hasNext();) {
					Hit hit = (Hit) it.next();
					Document doc = hit.getDocument();
					Field field = doc
							.getField(Constants.synsetIndexContentPathFieldName);
					String str = field.stringValue();
					String[] tagArray = str.split("\n");
					for (int i = 0; i < tagArray.length; i++) {
						synWordSet.add(tagArray[i]);
					}
				}				
			} 
			
			
						
			//如果需要coMap 作为 扩展
			//注意 这里扩展是那种  
			if(withCoMapExpension) {
				Set<String>  coMapExpensionSet = queryExpensionWithCoMap(dataInput, minFreq, topKForExpension,
						queryWord); 
				//加入 coMapExpensionSet 到同义词扩展中 
				//暂时这个时候做 
				synWordSet.addAll(coMapExpensionSet);
			}
			
			querExpension.setSynWordSet(synWordSet);
			pivotTagList.add(querExpension);
		}
		if(withSynWordExpension) 
			searcher.close();
		
		
		
		return pivotTagList;
	}

	/*
	 * 
	 * 对单个查询词 queryWord
	 * 进行 根据 CoMap 扩展 
	 * 
	 * 返回 扩展后的词 
	 */
	private static Set<String> queryExpensionWithCoMap(DataInput dataInput,
			int minFreq, int topKForExpension, String queryWord) {
		Map<String,Long> tagIdMap = dataInput.getTagIndex();
		Map<Long,String> idIndex = dataInput.getIdIndex();
		
		Set<String>  coMapExpensionSet = new HashSet<String> ();
		Long rowId = tagIdMap.get(queryWord);
		double selfFreq = dataInput.getCoMatrixValue(rowId.intValue(), rowId.intValue());
		
		//如果该词是低频词,哪么需要用CoMap来扩展
		//要扩展多少个词呢? 
		//topKForExpension应该是一个系统参数
		if(selfFreq < minFreq) {
			DoubleMatrix1D row = dataInput.getCoMatrixOneRow(rowId.intValue());
			//取得所有非0行
			IntArrayList indexList = new IntArrayList();
		    DoubleArrayList valueList = new DoubleArrayList();
			row.getNonZeros(indexList, valueList);
			int [] elements = indexList.elements();
			double [] values = valueList.elements();
			//创建一个倒序的比较器
			TreeMap<Double,String> map = new TreeMap<Double,String>(new Comparator<Double> (){

				@Override
				public int compare(Double o1, Double o2) {
					if(o1.doubleValue() < o2.doubleValue()) {
						return 1;
					} 
					else if (o1.doubleValue() > o2.doubleValue()){
						return -1;
					} else 
						return 0;
				}});
			
			//插入 
			for(int i = 0; i < elements.length; i++) {
				String tag = idIndex.get(new Long(elements[i]));
				Double key = new Double(values[i]);
				map.put(key, tag);
			}
			
			//枚举 出 前面 topK
			for(Double key :map.keySet()) {
				String tag = map.get(key);	
				if(tag.equals(queryWord)) {
					continue;
				}
				coMapExpensionSet.add(tag);
				if(coMapExpensionSet.size() == topKForExpension)
					break;
			}				
		}
		
		return coMapExpensionSet;
	}
	
	public static BooleanQuery convertQueryListToQuery(List<QueryExpension> queryTagList) {
		BooleanQuery andQuery = new BooleanQuery();
		//外层是一个and关系		
		for (QueryExpension queryExpension : queryTagList) {
			Set<String> orSet = queryExpension.getSynWordSet();			
			BooleanQuery orQuery = new BooleanQuery();
			//内层是一个or的关系
			for (String tag : orSet) {
				Term t = new Term(Constants.lucenceTagFieldName, tag);
				TermQuery q = new TermQuery(t);
				orQuery.add(q, BooleanClause.Occur.SHOULD);
			}
			andQuery.add(orQuery,BooleanClause.Occur.MUST);
		}
		
		return andQuery;
	}

}
