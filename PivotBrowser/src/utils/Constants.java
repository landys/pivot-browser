package utils;

public class Constants {
	
	static public String lucencePath = "F:/PivotBrowser_Index/origin/lucence/";
	
	static public String lucenceTagFieldName = "tags";
	
	static public String lucenceTitleFieldName = "titles";
	
	static public String lucenceColorFeatureFieldName = "color";
	
	static public String lucenceWaveLetFeatureFieldName = "wavelet";
	
	
	static public String lucenceSiftFeatureFieldName = "shape";
	
	static public String lucenceTagFilePathFieldName = "tagfilepaths";
	
	static public String lucencePicFilePathFieldName = "picfilepaths";
	
	static public String rawDataPath = "F:/apache-tomcat-6.0.16/webapps/PivotBrowser/data";
	
	static public String image = "data";
	
	static public String synsetIndexDir = "F:/PivotBrowser_Index/origin/synsetindex";
	
	static public String mergeIndexDir = "./mergedir/";
	
	static public String synsetIndexPathFieldName = "SynsetIndexId";
	
	static public String synsetIndexContentPathFieldName = "SynsetIndexContent";
	
	public enum SearchOperator {
		AND_OPERATOR,
		OR_OPERATOR
	};
	
	static public int numPerPage = 100;
	
	static public String dataInputName = "F:/PivotBrowser_Index/origin/dataInputObject";	
	
	static public int topK = 100;
	
	//内部用的
	static public int maxClusterNum = 20;
	
	static public int maxClusterNumForPage = 20;
	
	static public int topKForTagRank = 500;
	
	static public String tagTimesFileName = "./taglist.txt";
	
	static public String unsortTagTimesFileName = "./unsorttaglist.txt";
	
	static public int distributedLoadNum = 12;
	   
    static public int sampleSize = 3;
    
    static public int minFreqTime = 20;
    
    static public int topKForExpension = 5;
    
	public enum FieldType {
		Tag,
		Color,
		WaveLet
	}
	
	/*
	 * 计算Ｌ１距离
	 * 
	 */
	static public double computeDist(double [] d1, double d2 []) {
		double value = 0; 
		for(int i = 0 ; i < d1.length; i++) {
			double dif = d1[i] - d2[i];
			dif = Math.abs(dif);
			value += dif;
		}
		return value;
	}
	
	
	
	

}