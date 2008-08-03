package hisa.vo
{
	[Bindable]
	public class PhotoVo
	{
		public var source:String;
		public var toolTip:String;
		
		public function PhotoVo(source:String, toolTip:String)
		{
			this.source = source;
			this.toolTip = toolTip;
		}
	}
}