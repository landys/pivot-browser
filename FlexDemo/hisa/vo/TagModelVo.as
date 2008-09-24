package hisa.vo
{
	[Managed]
	[RemoteClass(alias="model.TagModel")]
	public class TagModelVo
	{
		//tag名字
		public tagName:String;
		
		//该tag出现次数
		public times:int;	
	}
}