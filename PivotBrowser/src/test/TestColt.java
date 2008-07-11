package test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.RCDoubleMatrix2D;

public class TestColt {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception, IOException {
		// TODO Auto-generated method stub
		int row = 3000;
		int col = 2000;
		int times = 300;
		double value = 0;
		Random radom = new Random();
		DoubleMatrix2D testMatrix = new RCDoubleMatrix2D(30,30);
		DoubleMatrix2D matrix = new RCDoubleMatrix2D(2800,300000);
		DoubleMatrix2D matrix1 = new RCDoubleMatrix2D(2800,300000);
		DoubleFactory2D F1 = DoubleFactory2D.rowCompressed;
		DoubleMatrix2D[][] parts2 = {{matrix},{matrix1}};
		DoubleMatrix2D m = F1.compose(parts2);
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("./tempobject"));
		
		os.writeObject(matrix);  
		
		os.close();
		
		/*
		System.out.println(testMatrix.toString());
		DoubleFactory1D F = DoubleFactory1D.sparse;
		DoubleMatrix1D vector2 = F.make(300000);
		for(int i = 0; i < times; i++) {
			int pos = radom.nextInt(300000);
			vector2.setQuick(pos, 5.0);
		}
		System.out.println("set over");
		for(int i = 0; i < row; i++) {
			System.out.println("set the " + i + "-th row");
			DoubleMatrix1D vector1 = matrix.viewRow(i);
//			for(int j = 0; j < vector2.size(); j++)
//				vector1.setQuick(j, vector2.getQuick(j));
//			vector1.assign(vector2.toArray());
		}
//		for(int i = 0; i < row; i++) { 
//			System.out.println("set the " + i + "-th row");
//			for(int j = 0; j < col; j++) {
//				matrix.setQuick(i, j, value);
//				value++;
//			}
//		}*/
		
		/*for(int i = 0; i < row; i++) { 
			for(int j = 0; j < col; j++) {
				System.out.println(matrix.getQuick(i, j));				
			}
		}*/
	}

}
