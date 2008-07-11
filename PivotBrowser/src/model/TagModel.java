package model;

import java.io.Serializable;



public class TagModel implements Comparable<TagModel> , Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//tag名字
	private String tagName;
	
	//该tag出现次数
	private int times;	
	

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}


	public int compareTo(TagModel o) {
		if(times > o.times)
			return -1;
		else if(times < o.times)
			return 1;
		else
			return 0;
	}
	
	
	
	
	
	
	
}
