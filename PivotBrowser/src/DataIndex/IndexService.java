package DataIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.RCDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import utils.Constants;

public class IndexService implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 474163043955765930L;
	private DataInput dataInput;
	
	
	
	public static void saveIndex(String fileName,DataInput dataInput) throws Exception {
	
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName));
		
		os.writeObject(dataInput);  
		
		os.close();
		
	}
	
	public static DataInput readIndex(String fileName) throws Exception {
		ObjectInputStream ins = new ObjectInputStream(new FileInputStream(fileName));
		
		DataInput dataInput = (DataInput)ins.readObject();
		
		ins.close();
		
		return dataInput;
		
	}

	public DataInput getDataInput() {
		return dataInput;
	}

	public void setDataInput(DataInput dataInput) {
		this.dataInput = dataInput;
	}

	/*
	 * ����ʽ��index ����
	 * 
	 */
	public IndexService() {
		try {
			File file = new File(Constants.dataInputName);		
			if(file.exists()) {  //����Ѿ�����  ��ȡ�������� 
				System.out.println("read object");
				dataInput = readIndex(Constants.dataInputName);
			} else {   //���� ����һ���µ� Ȼ�󱣴�
				System.out.println("create object");
				this.dataInput = new DataInput(false);
				saveIndex(Constants.dataInputName, dataInput);
			}
			System.out.println("indexservice create over");
		} catch(Exception e) {
			System.out.println("can't build index service");
			//dropIndex();
			e.printStackTrace();
		}
		
	}
	
	/*
	 * �ֲ�ʽ��index ���� ��ÿ̨����������,
	 * ÿ����������һ����dataInputObjectΪ��ͷ���������
	 * �ַ���"--startRow-endRow"
	 * ��������Ϊ��mergeʱ���ͨ��
	 */
	public IndexService(int startRow, int endRow) throws Exception {
		dataInput = new DataInput(startRow,endRow);
		String name = Constants.dataInputName + "--" + String.valueOf(startRow) + "," + String.valueOf(endRow); 
		saveIndex(name,dataInput);
	}
	
	/*
	 * �ֲ�ʽ���������һ���ֶ�
	 * ��merge����
	 * 
	 */
	public static void distributedDataInput() throws Exception {
		File mergeDir = new File(Constants.mergeIndexDir);
		MergeFileNameFilter filter = new MergeFileNameFilter();
		if (!mergeDir.exists() || !mergeDir.isDirectory()) {
			System.out.println("error");
		}
		File[] fileArray = mergeDir.listFiles(filter);
		//����һ��ȫ�µ�dataInput
		//�µ�dataInputû��coMatrix �������ƾ�����ص���Ϣ
		DataInput dataInput = new DataInput(true);
		List<DataInput> subDataInputList = new ArrayList<DataInput>();
		//���ÿ��dataInput
		for(int i = 0; i < fileArray.length; i++) {
			System.out.println("processing the " + (i+1) + "-th dataInput start");
			DataInput subDataInput = readIndex(fileArray[i].getAbsolutePath());
			//����ص������ �������л�����ռ�
			subDataInput.setIdIndex(null);
			subDataInput.setTagCloud(null);
			subDataInput.setTagIndex(null);
			subDataInput.setTagTimes(null);
			subDataInputList.add(subDataInput);
			System.out.println("processing the " + (i+1) + "-th dataInput end");
		}
		dataInput.setSubDataInputList(subDataInputList);	
		//���л�
		System.out.println("write the dataInputObject");

		saveIndex(Constants.dataInputName, dataInput);
		
		System.out.println("merge subMatrix end");
	}
	
	/*
	 * �ֲ�ʽ����������� 
	 * merge ���е���index
	 * ��ʵҲ����merge���е����ƾ���
	 * 
	 */
	public static void MergeDataInput() throws Exception {
		File mergeDir = new File(Constants.mergeIndexDir);
		MergeFileNameFilter filter = new MergeFileNameFilter();
		if (!mergeDir.exists() || !mergeDir.isDirectory()) {
			System.out.println("error");
		}
		System.out.println("merge subMatrix start");		
		File[] fileArray = mergeDir.listFiles(filter);
		// ��һ����ΪMerge��baseFile
		File baseFile = fileArray[0];
		// read ��һ����index ��dataInput
		DataInput baseDataInput = readIndex(baseFile.getAbsolutePath());
		int size = baseDataInput.getCoMatrixSize(); 
		//����������
		DoubleMatrix2D matrix = new RCDoubleMatrix2D(size,size);
		for (int i = 0; i < fileArray.length; i++) {
			DataInput dataInput = readIndex(fileArray[i].getAbsolutePath());	
			DoubleMatrix2D subMatrix = dataInput.getSubCoMatrix();	
//			System.out.println("the " + (i+1) + "-th subMatrix's cardinality is: " + subMatrix.cardinality());
			int startRow = dataInput.getStartRow();
			int endRow = dataInput.getEndRow();	
			System.out.println("processing the " + (i+1) + "-th subMatrix [" + startRow + "," + endRow + ") " + " start");
			for(int j = startRow, k = 0; j < endRow; j++,k++) {
				System.out.println("the " + k + "-th row");
				DoubleMatrix1D vector1 = matrix.viewRow(j);
				DoubleMatrix1D vector2 = subMatrix.viewRow(k);
//				System.out.println(vector2.cardinality());
				vector1.assign(vector2.toArray());				
			}
			System.out.println("processing the " + (i+1) + "-th subMatrix [" + startRow + "," + endRow + ") " + " end");
		}
		//���ú�merge���ľ���
		baseDataInput.setCoMatrix(matrix);
//		for(int i = 0; i < 50; i++)
//			System.out.println("the" + i + "-th row's cardinality is: " + matrix.viewRow(i).cardinality());
		//�ɵ��Ӿ�������Ϊ��
		baseDataInput.setSubCoMatrix(null);
		System.out.println("write the dataInputObject");
		//����֮
		saveIndex(Constants.dataInputName, baseDataInput);
		
		System.out.println("merge subMatrix end");
	}
	
	/*
	 * ������е�index
	 * 
	 */
	public static void dropIndex() {
		File file = new File(Constants.lucencePath);
		File [] fileList = file.listFiles();
		for(int i = 0; i < fileList.length; i++) {
			fileList[i].delete();
		}
		System.out.println("delete all lucence index");
		
		file = new File(Constants.dataInputName);
		file.delete();
		System.out.println("delete the dataInputObject");
	}
	
	public static void main(String[] args) throws Exception {
		
		//IndexService.dropIndex();
//		int startRow = Integer.parseInt(args[0]);
//		int endRow = Integer.parseInt(args[1]);
		
		//System.out.println("[" + startRow + "," + endRow + ")");
		
		IndexService indexService = new IndexService();
//		Long row = indexService.getDataInput().getTagIndex().get("zzzzzzzzzzzzzzzzzzzzzzzzzzzzz");
//		DoubleMatrix1D vector = indexService.getDataInput().getCoMatrixOneRow(row.intValue());
//		System.out.println(vector);
//		System.out.println(indexService.getDataInput().getTotalTagNum());
//		
//		System.out.println("e");
		//MergeDataInput();
		
		//distributedDataInput();
		
		//indexService.getDataInput().createTagsTimeFile();
		
		/*String tag1 = "flower";
		
		String tag2 = "ilovenature";
		
		DataInput dataInput = indexService.getDataInput();
		
		System.out.println(tag1 + " : " + dataInput.getTagSelfFrequency(tag1));
		
		
		
		//System.out.println(tag1 + " " + tag2 + " : " + dataInput.getTwoTagFrequency(tag1, tag2));
		
		
		RCDoubleMatrix2D matrix = dataInput.getCoMatrix();
		
		Map<String,Long> tagIndex = dataInput.getTagIndex();
		
		Long id1 = tagIndex.get(tag1);
		
		Long id2 = tagIndex.get(tag2);
		
		System.out.println(tag2 + " : " + matrix.getQuick(id2.intValue(), id2.intValue()));
		
		*/
		/*RCDoubleMatrix2D matrix = dataInput.getCoMatrix();
		
		Map<String,Long> tagIndex = dataInput.getTagIndex();
		
		Map<Long,String> indexTag = dataInput.getIdIndex();
		
		DoubleMatrix1D viewRow = matrix.viewRow(tagIndex.get(tag1).intValue());
		
		for(int i = 0 ;i < 100; i++) {
			System.out.println(indexTag.get(new Long(i)) + " " + matrix.getQuick(tagIndex.get(tag1).intValue(), i));
		}
		*/
		/*if(id1 == null || id2 == null) {
			System.out.println(tag1 + " " + tag2 + " : " + "0");
		}

		int row = id1.intValue();
		
		int col = id2.intValue();
		
		System.out.println(tag1 + " " + tag2 + " : " + matrix.getQuick(col, row));
		*/
		
		
		
		
		
		
		
	}
	
	
	
	
	

}

class MergeFileNameFilter implements FilenameFilter {

	public boolean accept(File dir, String name) {			
		if(name.startsWith("dataInputObject"))
			return true;
		else
			return false;
	}		
}
