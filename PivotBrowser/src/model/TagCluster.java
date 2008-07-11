package model;

import java.util.List;


public class TagCluster implements Comparable<TagCluster> {
	
	//该cluster内的tag
	//排好序的
	private List<TagModel> tagList;
	
	//该cluster的得分
	//目前用qValue
	private double score;
	
	
	
	//该cluster内部的pic url
	
	private List<String> picUrlList;

	public List<String> getPicUrlList() {
		return picUrlList;
	}

	public void setPicUrlList(List<String> picUrlList) {
		this.picUrlList = picUrlList;
	}

	public List<TagModel> getTagList() {
		return tagList;
	}

	public void setTagList(List<TagModel> tagList) {
		this.tagList = tagList;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int compareTo(TagCluster o) {
		if(score > o.score)
			return -1;
		else if(score < o.score)
			return 1;
		else
			return 0;
	}
	
	
	
	
	
	
	
	
	
	
	

}
