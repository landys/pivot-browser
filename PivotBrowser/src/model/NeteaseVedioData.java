package model;

import java.io.Serializable;
import java.util.List;

public class NeteaseVedioData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5398705561874115666L;
	
	private String title;
	private String description;
	private String tags;
	private String videourl;
	private String snapshot;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	

	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getVideourl() {
		return videourl;
	}
	public void setVideourl(String videourl) {
		this.videourl = videourl;
	}
	public String getSnapshot() {
		return snapshot;
	}
	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}	
}
