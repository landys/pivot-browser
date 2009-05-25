package hisa.vo
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class SearchHistoryVo
	{
		public var tags:ArrayCollection;
		public var maxClusterNum:int;
		
		public function SearchHistoryVo(tags:ArrayCollection, maxClusterNum:int)
		{
			if (tags != null)
			{
				this.tags = new ArrayCollection();
				for (var i:int=0; i<tags.length; ++i)
				{
					this.tags.addItem(tags.getItemAt(i));
				}
			}
			this.maxClusterNum = maxClusterNum;
		}
		
		public function equals(vo:SearchHistoryVo):Boolean
		{
			if (vo == null)
			{
				return false;
			}
			if (maxClusterNum != vo.maxClusterNum)
			{
				return false;
			}
			if (tags == null && vo.tags == null)
			{
				return true;
			}
			if (tags == null || vo.tags == null || tags.length != vo.tags.length)
			{
				return false;
			}
			
			for (var i:int=0; i<tags.length; ++i)
			{
				if (tags.getItemAt(i) != vo.tags.getItemAt(i))
				{
					return false;
				}
			}
			
			return true;
		}
	}
}