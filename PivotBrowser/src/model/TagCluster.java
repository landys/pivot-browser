package model;

import java.util.List;


public class TagCluster implements Comparable<TagCluster> {
	
	//��cluster�ڵ�tag
	//�ź����
	private List<TagModel> tagList;
	
	//��cluster�ĵ÷�
	//Ŀǰ��qValue
	private double score;
	
	
	
	//��cluster�ڲ���pic url
	
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
