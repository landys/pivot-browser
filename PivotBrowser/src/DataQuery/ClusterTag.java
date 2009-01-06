package DataQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.QueryExpension;

import DataIndex.IndexService;

import utils.Constants;
import utils.KMeans;
import utils.PointND;
import utils.Utils;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Sorting;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;

public class ClusterTag {
    
    private int k;
    
    private int kmin;
    
    private int kmax; 
    
    //���ⲿref������
    //����selectService���������ƾ���
    private DoubleMatrix2D weightMatrix;
    
    private DoubleMatrix2D traMatrix;
    
    private DoubleMatrix2D EigenMatrix;
    
    //�����set�е�id��simMatrix�����id
    private Map<Set<Long>,Double> clusters;
    
    private DoubleMatrix2D createTraMatrix() {
        
        int dim = weightMatrix.rows();
        DoubleMatrix2D diaMatrix = new DenseDoubleMatrix2D(dim,dim);
        
        for(int i = 0 ; i < dim ; i++ ) {
            
            DoubleMatrix1D rowView = weightMatrix.viewRow(i);
            double sum = 0;
            for(int j = 0 ; j < dim ; j++) {
                sum += rowView.getQuick(j);
            }
            if(sum != 0)
                diaMatrix.setQuick(i, i, 1/sum);
            else 
                diaMatrix.setQuick(i, i, 0);
            
        }
        Algebra algebra = new Algebra();
        
        return algebra.mult(diaMatrix, weightMatrix);
        
    }
    
    
    private DoubleMatrix2D computeEigenMatrix() {
        EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(traMatrix);
        DoubleMatrix1D real = eigenvalueDecomposition.getRealEigenvalues();
        DoubleMatrix1D image = eigenvalueDecomposition.getImagEigenvalues();
        DoubleMatrix2D eigVectorMatrix =  eigenvalueDecomposition.getV();
        Algebra algebra = new Algebra();
        
        //for test 
        //System.out.println("raw eigVectorMatrix");
        //Utils.printMatrix(eigVectorMatrix);
        //ת��
        eigVectorMatrix = algebra.transpose(eigVectorMatrix);
        
        //System.out.println("transpose eigVectorMatrix");
        //Utils.printMatrix(eigVectorMatrix);
        
                
        //�Ȱ�����ֵ��ģ�����
        double [] realArray = real.toArray();
        double [] imageArray = image.toArray();
        double [] magnitude = new double [realArray.length];
        
        for(int i = 0 ; i < realArray.length; i++) {
            magnitude[i] = Math.pow(realArray[i], 2) + Math.pow(imageArray[i], 2);
        }
        
        //for test
        //for(int i = 0;i < real.toArray().length;i++)
            //System.out.println(real.toArray()[i]);
        
        //�������õ��������������� ����ֻ��ʵ��
        eigVectorMatrix = Sorting.quickSort.sort(eigVectorMatrix, realArray);
        // ��magnitude
        //eigVectorMatrix = Sorting.quickSort.sort(eigVectorMatrix, magnitude);
        
        //System.out.println("sort eigVectorMatrix");
        //Utils.printMatrix(eigVectorMatrix);
        
        //����λ��
        int end = eigVectorMatrix.columns() - 1;
        int start = end - (k - 1) + 1;
        
        //ѡȡ�Ӿ���
        eigVectorMatrix = algebra.subMatrix(eigVectorMatrix, start, end , 0, end);
        
        //System.out.println("sub eigVectorMatrix");
        //Utils.printMatrix(eigVectorMatrix);
        
        //�Ȱ����л�˳��
        eigVectorMatrix = eigVectorMatrix.viewRowFlip();
        
        //System.out.println("flip sub eigVectorMatrix");
        //Utils.printMatrix(eigVectorMatrix);
        
        //ת�ƻ���
        eigVectorMatrix = algebra.transpose(eigVectorMatrix);
        
        //System.out.println("result eigVectorMatrix");
        //Utils.printMatrix(eigVectorMatrix);
        
        return eigVectorMatrix;
    }
    
    
    /*
     * 
     * ���㷨
     * 
     */
    private Map<Set<Long>,Double> spectralCluster() {       
    
        Algebra algebra = new Algebra();
        
        int totalRows = EigenMatrix.rows();

        //��ʼ ȡ�ڣ��С�Ҳ���ǡ���һά������k mean
        DoubleMatrix2D ukMatrix = algebra.subMatrix(EigenMatrix, 0, totalRows - 1 , 0, 0);      
        
        //for test
        //System.out.println("ukMatrix");
        //Utils.printMatrix(ukMatrix);
        
        //��ΪҪ�ı�����value ����Ҫ����һ����
        ukMatrix = ukMatrix.copy();
        
        //����row ��һ�� 
        rowNormalMatrix(ukMatrix);  
        
        //for test
        //System.out.println("normal ukMatrix");
        //Utils.printMatrix(ukMatrix);
        
        List<Long> idList =  new ArrayList<Long> ();
        Set<Long> wholeCluster = new HashSet<Long> ();
        //��ʼΪȫ�������id
        for(int i = 0 ; i < totalRows; i++) {
            Long id = new Long(i);
            idList.add(id);
            wholeCluster.add(id);
        }
        
        //3
        //DoubleMatrix2D ucMatrix = ukMatrix.viewSelection(null, null);     
        //��ʼ���� Ҳ���Ƿ�����
        //List<Set<Long>> p = splitCluster(ucMatrix, idList);
        
        DoubleMatrix2D ucMatrix;
        
        //List<Set<Long>> p = new ArrayList<Set<Long>> ();
        Map<Set<Long>,Double> p = new HashMap<Set<Long>,Double>();
        
        //p.add(wholeCluster);      
        p.put(wholeCluster, new Double(0));
        
        //��ǰcluster����Ŀ
        int currK = kmin;
        boolean isSpilit = true;
        
        while(currK <= k && isSpilit) {
            
            //a
            //List<Set<Long>> pNew = new ArrayList<Set<Long>>();
            Map<Set<Long>,Double> pNew = new HashMap<Set<Long>,Double>();
            pNew.putAll(p);
            //pNew.addAll(p);
            
            isSpilit = false;
            
            //b
            for(Set<Long> vC : p.keySet()){
                
                //�����clusterΪ�ա���ô����Ҫ����ֽ�
                if(vC.size() == 0) {
                    p.remove(vC);
                    continue;
                }
                    
                
                int colIndex = ( currK ) - 1 ;  //�����Ǹ���һ��Ϊ�Ǵ�0��ʼ��index
                
                if(colIndex >= EigenMatrix.columns()) {
                    isSpilit = false;
                    break;
                }
                    
                //System.out.println("EigenMatrix");
                //Utils.printMatrix(EigenMatrix);
                
                //1
                ukMatrix =  algebra.subMatrix(EigenMatrix, 0, totalRows - 1, 0, colIndex);              
                ukMatrix = ukMatrix.copy(); 
                //System.out.println("normal ukMatrix");
                //Utils.printMatrix(ukMatrix);
                rowNormalMatrix(ukMatrix);  
                
                //System.out.println("normal ukMatrix");
                //Utils.printMatrix(ukMatrix);
                
                //2
                idList = new ArrayList<Long> ();
                for(int i = 0; i < totalRows; i++ ) {
                    Long id = new Long(i);
                    //�����id��cluster�� �Ը�cluster����spilit
                    if(vC.contains(id)) {
                        idList.add(id);
                    }
                }
                int[] rowIndexes = new int [idList.size()];
                for(int i = 0; i < rowIndexes.length; i++) {
                    rowIndexes[i] = idList.get(i).intValue();
                }
                
                //3from uk to uc
                ucMatrix = ukMatrix.viewSelection(rowIndexes, null);
                //System.out.println("ucMatrix");
                //Utils.printMatrix(ucMatrix);
                
                //add for select initial center
//                int[] startingCenterRawId = selectInitialCenter(rowIndexes);                
//                List<Set<Long>> pp = splitCluster(ucMatrix, idList, startingCenterRawId);
                //�������Ҫѡ���� ��ô����startingCenterRawId == null
                List<Set<Long>> pp = splitCluster(ucMatrix, idList,null);
                
                
                Set<Long> cluster1 = pp.get(0);
                Set<Long> cluster2 = pp.get(1);
                
                //4
                double qBefore = computeQForOneCluster(vC,wholeCluster);
                
                //for test
                //System.out.println("qBefore: " + qBefore);
                
                double q1 = computeQForOneCluster(cluster1,wholeCluster);
                double q2 = computeQForOneCluster(cluster2,wholeCluster);               
                double qAfter = q1 + q2;

                //for test
                //System.out.println("q1: " + q1 + " q2: " + q2 + " qAfter: " + qAfter);
                //���Qֵ������ ɾ��ԭ��cluster �����µ�cluster
                if(qAfter > qBefore) {
                    pNew.remove(vC);                    
                    pNew.put(cluster1,q1);
                    pNew.put(cluster2,q2);  
                    isSpilit = true;
                    currK++;
                }               
            }           
            //c
            p = new HashMap<Set<Long>,Double>();
            p.putAll(pNew); 
        }       
        return p;
    }


	private int[] selectInitialCenter(int[] rowIndexes) {
		//ȡ��һ������
		DoubleMatrix2D matrix = weightMatrix.viewSelection(rowIndexes,rowIndexes);
		
		//here we just use binary split    
		int [] startingCenterRawId = new int [2];       
		//��ʼ��
		double minValue = Double.MAX_VALUE;
		int rowId = -1;
		int colId = -1;
		for(int i = 0; i < matrix.rows(); i++)
			for(int j = i + 1; j < matrix.columns(); j++) {                		
				double value = matrix.get(i, j);
				if(value < minValue) {
					minValue = value;
					rowId = i;
					colId = j;
				}
			}
		
		startingCenterRawId[0] = rowId;
		startingCenterRawId[1] = colId;
		//���ѡ������
		if(startingCenterRawId[0] == -1 || startingCenterRawId[1] == -1) 
			return null;
		else
			return startingCenterRawId;
	}


    /**
     * @param ukMatrix
     * @param normalValues
     */
    private void rowNormalMatrix(DoubleMatrix2D ukMatrix) {
        
        //�½���һ�����ÿһrow�� normal value������
        double [] normalValues = new double [ukMatrix.rows()];
        //�����norm ֵ         
        for(int i = 0 ; i < ukMatrix.rows(); i++) {
            double sum = 0;
            for(int j = 0; j < ukMatrix.columns(); j++) {
                sum += Math.pow(ukMatrix.getQuick(i, j), 2);
            }
            sum = Math.sqrt(sum);
            normalValues[i] = sum; 
        }
        
        //norm ����
        for(int i = 0; i < ukMatrix.rows(); i++) {
            for(int j = 0 ; j < ukMatrix.columns(); j++) {
                double rawValue = ukMatrix.getQuick(i, j);
                if(normalValues[i] != 0) {
                    ukMatrix.setQuick(i, j, rawValue/normalValues[i]);
                }                   
            }
        }
    }
    
    private double computeClusterWeights(Set<Long> set1, Set<Long> set2 ) {
        double sum = 0;
        for(Long v1 : set1) 
            for(Long v2 : set2 ) {
                sum += weightMatrix.getQuick(v1.intValue(), v2.intValue());
            }
        return sum;
    }
    
    //����Q ֵ
    @SuppressWarnings("unused")
    private double computeQForAll(List<Set<Long>> clusters, Set<Long> wholeCluster)  {
        double sum = 0;
        double allWeights = computeClusterWeights(wholeCluster,wholeCluster);
        //��� ��ÿ��cluster
        for(Set<Long> cluster : clusters) {
            double part1 = computeClusterWeights(cluster,cluster)/allWeights;
            double part2 = Math.pow(computeClusterWeights(cluster,wholeCluster)/allWeights,2);
            sum += part1 - part2;           
        }
        
        return sum;
    }
    
    //����һ��cluster��Qֵ
    private double computeQForOneCluster(Set<Long> cluster,Set<Long> wholeCluster) {
        double allWeights = computeClusterWeights(wholeCluster,wholeCluster);
        double part1 = computeClusterWeights(cluster,cluster)/allWeights;
        double part2 = Math.pow(computeClusterWeights(cluster,wholeCluster)/allWeights,2);
        return part1 - part2;            
    }
    
    
    /*
     * ��������㷨
     * 
     * ������������
     * ����һ��uc����
     * �������Ǹþ����Ӧ��ԭʼid ����
     * 
     * int [] startingCenterId ��ucMatrix�����id
     */
    private List<Set<Long>> splitCluster(DoubleMatrix2D ucMatrix,List<Long> rawIdList, int [] startingCenterRawId) {
        
        List<Set<Long>> list = new ArrayList<Set<Long>>();
        
        int length = ucMatrix.rows();
        int dim = ucMatrix.columns();
        //���������ݵ�
        PointND[] points = new PointND[length];
        
        for(int i = 0 ; i < length ; i++ ){
            points[i] = new PointND(ucMatrix.viewRow(i).toArray());
        }
        //�ֳ��������
        KMeans kmeans = new KMeans(points,dim,2);
        
        //�������
        PointND[] centers;
        if(startingCenterRawId != null) {//ʹ�ó�ʼ����
        	PointND[] startingCenters = new PointND[2];
        	startingCenters[0] = points[startingCenterRawId[0]];
        	startingCenters[1] = points[startingCenterRawId[1]];      
        	centers = kmeans.getCenters(0.01, 50, startingCenters);
        }
        else						//��ʹ�ó�ʼ����
        	centers = kmeans.getCenters(0.01, 50);
        
        
        //���� length�϶�Ϊ2
        for(int i = 0; i < centers.length; i++) {
            Set<Long> set = new HashSet<Long>();
            list.add(set);
        }
        
        
        //����ֳ�����
        for(int i = 0; i < length ; i++) {
            int index = kmeans.getClosestCenterIndex(i);
            Set<Long> set = list.get(index);
            long id = rawIdList.get(i);
            set.add(id);
        }
        return list;
    }


    

    public Map<Set<Long>, Double> getClusters() {
        return clusters;
    }


    public void setClusters(Map<Set<Long>, Double> clusters) {
        this.clusters = clusters;
    }


    public DoubleMatrix2D getEigenMatrix() {
        return EigenMatrix;
    }


    public void setEigenMatrix(DoubleMatrix2D eigenMatrix) {
        EigenMatrix = eigenMatrix;
    }


    public int getK() {
        return k;
    }


    public void setK(int k) {
        this.k = k;
    }


    public int getKmax() {
        return kmax;
    }


    public void setKmax(int kmax) {
        this.kmax = kmax;
    }


    public int getKmin() {
        return kmin;
    }


    public void setKmin(int kmin) {
        this.kmin = kmin;
    }


    public DoubleMatrix2D getTraMatrix() {
        return traMatrix;
    }


    public void setTraMatrix(DoubleMatrix2D traMatrix) {
        this.traMatrix = traMatrix;
    }


    public DoubleMatrix2D getWeightMatrix() {
        return weightMatrix;
    }


    public void setWeightMatrix(DoubleMatrix2D weightMatrix) {
        this.weightMatrix = weightMatrix;
    }


    public ClusterTag(int k, int kmax ,SelectTag select) {
        super();

        this.kmax = k;
        this.kmin = 2;
        this.weightMatrix = select.getSimMatrix();
        
        if(k >= weightMatrix.rows()) {
            this.k = weightMatrix.rows();
        } else
            this.k = k;
        
        this.traMatrix = createTraMatrix();
        
        //for test
        //Utils.printMatrix(traMatrix);
        
        this.EigenMatrix = computeEigenMatrix();
        
        //for test
        //Utils.printMatrix(EigenMatrix);
        
        this.clusters = spectralCluster();
    }
    
    
    private double printCluster(Map<Set<Long>,Double> clusters, SelectTag select) {       
        
        //����ÿһ��cluster
        int i = 1;
        double totalQvalue = 0;
        for(Set<Long> cluster : clusters.keySet()) {
            double qValue = clusters.get(cluster);
            System.out.println("cluster : " + i + " Qvalue : " + qValue);  
            totalQvalue += qValue;
            for(Long id : cluster) {
                Long rawId = select.getIdMapRawId().get(id);
                String tag = select.getDataInput().getIdIndex().get(rawId);
                //System.out.print(tag + "(" + rawId +")" + " ");
                System.out.print(tag + " ");
            }
            i++;
            System.out.println();
        }    
        System.out.println("Num of Cluster: " + i + "	Total Qvalue For This Query: " + totalQvalue);
        return totalQvalue;
    }
    
    public static void main(String[] args) throws Exception {       
        IndexService indexService = new IndexService();
        
        //��������list
    	BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("./samplequery.txt")));
    	
    	BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("./result.txt")));
    	
    	List<String> inputStrList = new ArrayList<String>();
    	
        while(true) {
   			String line = br.readLine();
   			if(line == null)
   				break;
   			String [] value = line.split("\\s");
   			inputStrList.add(value[0]);
        }
        	
        br.close();
        
        double totalQvalue = 0;
        int i = 1;
        for(String str : inputStrList) {
        	System.out.println("Processing	" + i);
        	List<String> list = new ArrayList<String>();
        	list.add(str);
        	List<QueryExpension> pivotTagList = Utils
            .convertRawListToPivotTagList(list, indexService
                    .getDataInput(), false, true, Constants.minFreqTime, Constants.topKForExpension);
        	if (pivotTagList.size() == 0)
        		continue;
        	 SelectTag select = new SelectTag(Constants.topK, pivotTagList,
                     indexService.getDataInput());   
        	 ClusterTag tagsCluster = new ClusterTag(Constants.maxClusterNum,
                     Constants.maxClusterNum, select);
        	 
        	 double currentQvalue = tagsCluster.printCluster(tagsCluster.getClusters(), select);
        	 
        	 wr.write(currentQvalue + "		" + tagsCluster.getClusters().size() + "\n");
        	 totalQvalue += currentQvalue;
        	 i++;
        }
        
        wr.write("total Qvalue: " + totalQvalue);
        System.out.println("total Qvalue: " + totalQvalue);
        wr.close();
        
    }
    
    
//    public static void main(String[] args) throws Exception {       
//        IndexService indexService = new IndexService();
//        /*List<QueryExpension> queryList = new ArrayList<QueryExpension>();
//        List<String>  synWordList = new ArrayList<String>();
//        String queryWord = "flower";
//        synWordList.add("flower");
//        synWordList.add("flowers");
//        synWordList.add("bloom");
//        synWordList.add("blooms");
//        synWordList.add("blossom");
//        synWordList.add("blossoms");        
//        QueryExpension querExpension = new QueryExpension();
//        querExpension.setQueryWord(queryWord);
//        querExpension.setSynWordList(synWordList);
//        queryList.add(querExpension);
//        
//        synWordList = new ArrayList<String>();
//        queryWord = "girl";
//        synWordList.add("girl");
//        querExpension = new QueryExpension();
//        querExpension.setQueryWord(queryWord);
//        querExpension.setSynWordList(synWordList);
//        //queryList.add(querExpension);*/
//        double averageClusterTime = 0;
//        double averageSelectTime1 = 0;
//        double averageSelectTime2 = 0;
//        double averageSelectTimeTotal = 0;
//        int round = 0;
//        for (int i = 0; i < 1; i++) {
//            List<String> list = new ArrayList<String>();
//            list.add("dog");
////          list.add("window");
////          list.add("baby");
////          list.add("movie");
//            list.add("film");
////          list.add("poodle");
////          list.add("tv");
////          list.add("flower");
////          list.add("dog");
////          list.add("puppy");
////          list.add("weimaraner");
////          list.add("flower");
////          list.add("flowers");
////          list.add("bloom");
////          list.add("blooms");
////          list.add("blossom");
////          list.add("blossoms");
////          list.add("garden");
////          list.add("red");
////          list.add("white");
//            List<QueryExpension> pivotTagList = Utils
//                    .convertRawListToPivotTagList(list, indexService
//                            .getDataInput(), true, true, Constants.minFreqTime, Constants.topKForExpension);
//            if (pivotTagList.size() == 0)
//                continue;
//            long startTime = System.currentTimeMillis();
//            SelectTag select = new SelectTag(Constants.topK, pivotTagList,
//                    indexService.getDataInput());
//            long endTime = System.currentTimeMillis();
//            averageSelectTimeTotal += endTime - startTime;
//
//            averageSelectTime1 += SelectTag.time1;
//            averageSelectTime2 += SelectTag.time2;
//
//            startTime = System.currentTimeMillis();
//            ClusterTag tagsCluster = new ClusterTag(Constants.maxClusterNum,
//                    Constants.maxClusterNum, select);
//            endTime = System.currentTimeMillis();
//            averageClusterTime += endTime - startTime;
//
//            tagsCluster.printCluster(tagsCluster.getClusters(), select);
//
//            round++;            
//        }
//        
//        System.out.println("average select total time: " + averageSelectTimeTotal / round);
//        
//        System.out.println("average select 1 time: " + averageSelectTime1 / round);
//        
//        System.out.println("average select 2 time: " + averageSelectTime2 / round);
//        
//        System.out.println("average cluster time: " + averageClusterTime / round);
//        
//        //SelectTag select = new SelectTag(Constants.topK,queryList,indexService.getDataInput());
//        // 
//        
//        
//        //System.out.println("tranMatrix");
//        
//        //Utils.printMatrix(tagsCluster.getTraMatrix());
//        
//        //System.out.println("EigenMatrix");
//        
//        //Utils.printMatrix(tagsCluster.getEigenMatrix());
//        
//        
//        //System.out.println("Cluster");
//        
//        //tagsCluster.printCluster(tagsCluster.getClusters(),select);       
//        
//        
//    }
    
    
    
    
    
    
    
    

}
