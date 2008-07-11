package DataQuery;

public class TagFrequency2 implements Comparable<TagFrequency2> {
	
	private long tagId;
	
	//for test
	private String tag;

	private double selfFreq;
	
	private double score;

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getSelfFreq() {
		return selfFreq;
	}

	public void setSelfFreq(double selfFreq) {
		this.selfFreq = selfFreq;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public long getTagId() {
		return tagId;
	}

	public void setTagId(long tagId) {
		this.tagId = tagId;
	}

	/*
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TagFrequency2 o) {
		if(this.score > o.score)
			return -1;
		else if(this.score < o.score)
			return 1;		
		else if(this.selfFreq > o.selfFreq)
			return -1;
		else if(this.selfFreq < o.selfFreq)
			return 1;
		else
			return 0;
	}
	
	

}
