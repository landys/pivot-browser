package test;

import java.util.ArrayList;
import java.util.List;

import model.TagCluster;
import model.TagModel;
import DataQuery.SearchService;

import com.thoughtworks.xstream.XStream;

public class test {

public static void main(String[] args) throws Exception {
		
		SearchService searchService = new SearchService();	
		
		System.out.println("total pic num: " + SearchService.getIndexService().getDataInput().getTotalDoc());
		
		System.out.println("total tag num: " + SearchService.getIndexService().getDataInput().getIdIndex().size());
				
		System.out.println("top 100 tag :");
		List<TagModel> top100Tag = new ArrayList<TagModel> ();
		top100Tag.addAll(searchService.getTagCloud(100));
		XStream xstream = new XStream();
		xstream.alias("tag", TagModel.class);
		xstream.alias("cluster", TagCluster.class);	
		String result = xstream.toXML(top100Tag);		
		System.out.println(result);
		
		
		/*List<TagModel> tagModelList = searchService.getTagCloud(10);
		
		for(TagModel tagModel : tagModelList) {
			System.out.println(tagModel.inspect());
		}*/
		/*List<String> pivotList = new ArrayList<String>();
		//pivotList.add("purple");
		pivotList.add("apple");
		List<TagCluster> clusterList = searchService.searchTag(pivotList);		
		xstream = new XStream();
		xstream.alias("tag", TagModel.class);
		xstream.alias("cluster", TagCluster.class);		
		result = xstream.toXML(clusterList);		
		System.out.println(result);
		
		TagCluster cluster = clusterList.get(0);
		
		List<TagModel> list = cluster.getTagList();
		List<String> queryTagList = new ArrayList<String>();
		for(TagModel tm : list) {
			queryTagList.add(tm.getTagName());
		}
		
		List<String> picUrlList = searchService.getPicUrlForTagsRank(queryTagList,0);
		for(String url : picUrlList) {
			System.out.println(url);
		}
		System.out.println("");
		if(searchService.getWrapDocumentList() == null)
			searchService.setWrapDocumentList(searchService.getWrapDocumentListForTagsRank(queryTagList));
		picUrlList = searchService.getPicUrlForWaveLetRank(queryTagList, 0, "1/2147279266.jpg");
		for(String url : picUrlList) {
			System.out.println(url);
		}
		
		/*XStream xstream = new XStream();
		
		List<String> picUrlList = searchService.getPicUrlForTags(pivotList, 0);
		String result = xstream.toXML(picUrlList);
		
		System.out.println(result);*/
	}

}
