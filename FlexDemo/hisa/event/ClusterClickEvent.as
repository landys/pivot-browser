package hisa.event
{
	import flash.events.Event;

	public class ClusterClickEvent extends Event
	{
		public static const CLUSTER_CLICK:String = "clusterClick";
		
		public static const TAG_TARGET:String = "tag";
		
		public static const PIC_TARGET:String = "pic";
		
		public static const VACANCY_TARGET:String = "vacancy";
		
		public static const MORE_TARGET:String = "more";
		
		private var _detailTarget:String;
		
		private var _index:int;
		
		private var _data:Object;
		
		public function get detailTarget():String
		{
			return _detailTarget;
		}
		
		public function get index():int
		{
			return _index;
		}
		
		public function get data():Object
		{
			return _data;
		}
		
		public function ClusterClickEvent(type:String, index:int, detailTarget:String=VACANCY_TARGET,
			data:Object=null, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this._index = index;
			this._detailTarget = detailTarget;
			this._data = data;
		}
		
	}
}