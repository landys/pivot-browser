package hisa.dataQueryVO
{
	import mx.collections.ArrayCollection;
	
	[Managed]
	[RemoteClass(alias="model.TagCluster")]
	public class TagClusterVO
	{
		//该cluster内的tag
		//排好序的
		public tagList:ArrayCollection;
		
		//该cluster内部的pic url
		
		public picUrlList:ArrayCollection;
		
		public function TagClusterVO(cluster:Object=null)
		{
			if (cluster != null)
			{
				fill(cluster);
			}
		}
		
		public function fill(cluster:Object):void 
		{
			this.tagList = cluster.tagList;
			this.picUrlList = cluster.picUrlList;
		}
	}
}