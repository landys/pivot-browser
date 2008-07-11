package DataQuery;


public class TagFrequency1 implements Comparable<TagFrequency1> {
	
	
	private long tagId;
	
	//for test
	private String tag;

	private double selfFreq;
	
	private int pivotNum;
	
	private double [] coFreq;
	
	private double [] score;
	
	private double finalScore;


	public TagFrequency1(int pivotNum) {
		super();
		this.pivotNum = pivotNum;
		this.coFreq = new double [pivotNum];
		this.score = new double [pivotNum];
	}


   public void setCoFreq(double value,int index) {
	   coFreq[index] = value;
   }
   
   public void setScore(double value, int index) {
	   score[index] = value;
   }

	public int getPivotNum() {
		return pivotNum;
	}




	public void setPivotNum(int pivotNum) {
		this.pivotNum = pivotNum;
	}




	public double[] getCoFreq() {
		return coFreq;
	}




	public void setCoFreq(double[] coFreq) {
		this.coFreq = coFreq;
	}




	public double getFinalScore() {
		return finalScore;
	}




	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}




	public double[] getScore() {
		return score;
	}




	public void setScore(double[] score) {
		this.score = score;
	}




	public String getTag() {
		return tag;
	}




	public void setTag(String tag) {
		this.tag = tag;
	}



	public double getSelfFreq() {
		return selfFreq;
	}




	public void setSelfFreq(double selfFreq) {
		this.selfFreq = selfFreq;
	}




	public long getTagId() {
		return tagId;
	}




	public void setTagId(long tagId) {
		this.tagId = tagId;
	}


	/*
	 * 按照得分和
	 * 
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */


	public int compareTo(TagFrequency1 o) {	
		
		if(this.finalScore > o.finalScore)
			return -1;
		else if(this.finalScore < o.finalScore)
			return 1;		
		else if(this.selfFreq > o.selfFreq)
			return -1;
		else if(this.selfFreq < o.selfFreq)
			return 1;
		else
			return 0;
	}
	
	
}
