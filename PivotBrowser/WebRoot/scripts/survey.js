var showMessage = function(message) {
	$("#lblinfo").text(message);
}

var submit_survey = function() {
	if ($.trim($("#user_id").val()) == '') {
		showMessage("无法提交, ID不能为空.");
		return false;
	}
	
	if ($.trim($("#age").val()) == '') {
		showMessage("无法提交, 年龄不能为空.");
		return false;
	}
	
	if ($.trim($("#prefer_ses").val()) == '') {
		showMessage("无法提交, 你常用的图像搜索引擎不能为空.");
		return false;
	}
	
	if ($.trim($("#like_best").val()) == '') {
		showMessage("无法提交, 试验中你最喜欢的部分不能为空.");
		return false;
	}
	
	if ($.trim($("#unlike_best").val()) == '') {
		showMessage("无法提交, 试验中你最不喜欢的部分不能为空.");
		return false;
	}
	
	if ($.trim($("#suggestion").val()) == '') {
		showMessage("无法提交, 其他的意见和建议不能为空.");
		return false;
	}
	
	$("#frmsurvey").submit();
};