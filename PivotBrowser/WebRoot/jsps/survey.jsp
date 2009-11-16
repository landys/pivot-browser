<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<script type="text/javascript" src="scripts/jquery.js"></script>
	<script type="text/javascript" src="scripts/survey.js"></script>
	<title>问卷调查</title>
</head>

<body>
<form id="frmsurvey" method="post" action="save-survey.htm">
<p>
ID：<input type="text" name="user_id" id="user_id"/><br />
性别：<input type="radio" name="sex" value="男" />男
<input type="radio" name="sex" value="女"/>女<br />
年龄：<input type="text" name="age" id="age"/><br />
<ol>
<li>
1. 开始上网有多久了？（单选）：<input type="radio" name="www_expr" value="不上网"/>不上网、
<input type="radio" name="www_expr" value="小于6个月"/>小于6个月、
<input type="radio" name="www_expr" value="6个月到1年"/>6个月到1年、
<input type="radio" name="www_expr" value="超过1年" />超过1年
</li>
<li>
平时多长时间上一次网（单选）：<input type="radio" name="www_freq" value="超过1年" />每天、
<input type="radio" name="www_freq" value="每星期"/>每星期、
<input type="radio" name="www_freq" value="每月"/>每月、
<input type="radio" name="www_freq" value="每年"/>每年
</li>
<li>
列出你常用的图像搜索引擎：<input type="text" name="prefer_ses" id="prefer_ses"/></li>
<li>
你上网搜图片的主要目的（多选）：<input type="checkbox" name="goal1" value="专业需求" />专业需求、
<input type="checkbox" name="goal2" value="信息获取（人物、新闻等）"/>信息获取（人物、新闻等）、
<input type="checkbox" name="goal3" value="浏览（搜索趣味、漂亮的图片）"/>浏览（搜索趣味、漂亮的图片）、
<input type="checkbox" name="goal4" value="其他"/>其他</li>
<li>
你对一般的图像搜索服务是否满意（单选）？<input type="radio" name="satisfaction" value="1"/>(非常不满意)1、
<input type="radio" name="satisfaction" value="2"/>2、
<input type="radio" name="satisfaction" value="3"/>3、
<input type="radio" name="satisfaction" value="4"/>4、
<input type="radio" name="satisfaction" value="5" />(非常满意)5</li>
<li>
你使用Flickr多久了（单选）：<input type="radio" name="flickr_expr" value="没有用过"/>没有用过、
<input type="radio" name="flickr_expr" value="小于6个月"/>小于6个月、
<input type="radio" name="flickr_expr" value="6个月到1年"/>6个月到1年、
<input type="radio" name="flickr_expr" value="超过1年" />超过1年</li>
<li>
你觉得网上通过其他搜索引擎搜索图片，最让你觉得不满意的是什么？（多选）<br />
<input type="checkbox" name="unsatisfaction1" value="输入检索词" />输入检索词；
<input type="checkbox" name="unsatisfaction2" value="结果杂乱不相关"/>结果杂乱不相关；
<input type="checkbox" name="unsatisfaction3" value="结果相关，但不一定是想要的"/>结果相关，但不一定是想要的；
<input type="checkbox" name="unsatisfaction4" value="其他"/>其他（请注明）<input type="text" name="unsatisfaction_text" />
</li></ol>
========================================================================================<br/>
对本系统的相关评价：</br>
<ol>
<li>
Satisfactory for the whole search process(对整个检索过程是否满意) （单选）：
<input type="radio" name="pb_satis" value="1"/>(很不满意)1、
<input type="radio" name="pb_satis" value="2"/>2、
<input type="radio" name="pb_satis" value="3"/>3、
<input type="radio" name="pb_satis" value="4"/>4、
<input type="radio" name="pb_satis" value="5" />(很满意)5</li><li>
Simplicity of use(操作简单吗？) （单选）：
<input type="radio" name="pb_use" value="1"/>(很不满意)1、
<input type="radio" name="pb_use" value="2"/>2、
<input type="radio" name="pb_use" value="3"/>3、
<input type="radio" name="pb_use" value="4"/>4、
<input type="radio" name="pb_use" value="5" />(很满意)5</li><li>
Flexibility(操作灵活吗？)（单选）：
<input type="radio" name="pb_flex" value="1"/>(很不满意)1、
<input type="radio" name="pb_flex" value="2"/>2、
<input type="radio" name="pb_flex" value="3"/>3、
<input type="radio" name="pb_flex" value="4"/>4、
<input type="radio" name="pb_flex" value="5" />(很满意)5</li><li>
Interesting to use(功能有趣吗？) （单选）：
<input type="radio" name="pb_interest" value="1"/>(很不满意)1、
<input type="radio" name="pb_interest" value="2"/>2、
<input type="radio" name="pb_interest" value="3"/>3、
<input type="radio" name="pb_interest" value="4"/>4、
<input type="radio" name="pb_interest" value="5" />(很满意)5</li><li>
Easy to browse(浏览方便吗？) （单选）：
<input type="radio" name="pb_easy" value="1"/>(很不满意)1、
<input type="radio" name="pb_easy" value="2"/>2、
<input type="radio" name="pb_easy" value="3"/>3、
<input type="radio" name="pb_easy" value="4"/>4、
<input type="radio" name="pb_easy" value="5" />(很满意)5</li><li>
Effective for search(检索有效吗？) （单选）：
<input type="radio" name="pb_effective" value="1"/>(很不满意)1、
<input type="radio" name="pb_effective" value="2"/>2、
<input type="radio" name="pb_effective" value="3"/>3、
<input type="radio" name="pb_effective" value="4"/>4、
<input type="radio" name="pb_effective" value="5" />(很满意)5</li><li>
Functionality(功能强大吗？) （单选）：
<input type="radio" name="pb_func" value="1"/>(很不满意)1、
<input type="radio" name="pb_func" value="2"/>2、
<input type="radio" name="pb_func" value="3"/>3、
<input type="radio" name="pb_func" value="4"/>4、
<input type="radio" name="pb_func" value="5" />(很满意)5</li><li>
Satisfactory for final results(对最终结果满意吗？) （单选）：
<input type="radio" name="pb_final_satic" value="1"/>(很不满意)1、
<input type="radio" name="pb_final_satic" value="2"/>2、
<input type="radio" name="pb_final_satic" value="3"/>3、
<input type="radio" name="pb_final_satic" value="4"/>4、
<input type="radio" name="pb_final_satic" value="5" />(很满意)5</li><li>
Quality of tag recommendation(标签推荐的质量如何?) （单选）：
<input type="radio" name="pb_tag_quality" value="1"/>(很不满意)1、
<input type="radio" name="pb_tag_quality" value="2"/>2、
<input type="radio" name="pb_tag_quality" value="3"/>3、
<input type="radio" name="pb_tag_quality" value="4"/>4、
<input type="radio" name="pb_tag_quality" value="5" />(很满意)5</li><li>
Quality of clustering(聚类效果好吗？similarity or homogeneity within cluster）（单选）：
<input type="radio" name="pb_cluster" value="1"/>(很不满意)1、
<input type="radio" name="pb_cluster" value="2"/>2、
<input type="radio" name="pb_cluster" value="3"/>3、
<input type="radio" name="pb_cluster" value="4"/>4、
<input type="radio" name="pb_cluster" value="5" />(很满意)5</li><li>
Comprehension of cluster name(聚类的描述意义大吗？) （单选）：
<input type="radio" name="pb_comprehension" value="1"/>(很不满意)1、
<input type="radio" name="pb_comprehension" value="2"/>2、
<input type="radio" name="pb_comprehension" value="3"/>3、
<input type="radio" name="pb_comprehension" value="4"/>4、
<input type="radio" name="pb_comprehension" value="5" />(很满意)5</li><li>
Relevance of results(结果的相关度) （单选）：
<input type="radio" name="pb_relevance" value="1"/>(很不满意)1、
<input type="radio" name="pb_relevance" value="2"/>2、
<input type="radio" name="pb_relevance" value="3"/>3、
<input type="radio" name="pb_relevance" value="4"/>4、
<input type="radio" name="pb_relevance" value="5" />(很满意)5</li><li>
The overall image coverage in the results(结果涵盖的信息量) （单选）：
<input type="radio" name="pb_coverage" value="1"/>(很不满意)1、
<input type="radio" name="pb_coverage" value="2"/>2、
<input type="radio" name="pb_coverage" value="3"/>3、
<input type="radio" name="pb_coverage" value="4"/>4、
<input type="radio" name="pb_coverage" value="5" />(很满意)5</li><li>
Do you think the representative thumbnails and tags serve as informative labels for the images in each cluster? （单选） <br/>
<input type="radio" name="pb_infor" value="1"/>(很不满意)1、
<input type="radio" name="pb_infor" value="2"/>2、
<input type="radio" name="pb_infor" value="3"/>3、
<input type="radio" name="pb_infor" value="4"/>4、
<input type="radio" name="pb_infor" value="5" />(很满意)5</li><li>
Do you think the result cluster of PivotBrowser allows for more effective browsing? （单选）<br/>
<input type="radio" name="pb_more_eff" value="1"/>(很不满意)1、
<input type="radio" name="pb_more_eff" value="2"/>2、
<input type="radio" name="pb_more_eff" value="3"/>3、
<input type="radio" name="pb_more_eff" value="4"/>4、
<input type="radio" name="pb_more_eff" value="5" />(很满意)5</li><li>
Do you think PivotBrowser has addressed the inconsistency problem（比如对同一个概念的文字表达可以多种形式）？（单选）<br/>
<input type="radio" name="pb_hit_inconsist" value="1"/>(很不满意)1、
<input type="radio" name="pb_hit_inconsist" value="2"/>2、
<input type="radio" name="pb_hit_inconsist" value="3"/>3、
<input type="radio" name="pb_hit_inconsist" value="4"/>4、
<input type="radio" name="pb_hit_inconsist" value="5" />(很满意)5</li><li>
Do you think PivotBrowser has addressed the disambiguation problem（比如一个词可以代表多种含义）(or succeeds in semantics discrimination and division)? （单选）<br/>
<input type="radio" name="pb_hit_disamb" value="1"/>(很不满意)1、
<input type="radio" name="pb_hit_disamb" value="2"/>2、
<input type="radio" name="pb_hit_disamb" value="3"/>3、
<input type="radio" name="pb_hit_disamb" value="4"/>4、
<input type="radio" name="pb_hit_disamb" value="5" />(很满意)5</li><li>
Do you think the visual reranking（选择图片对结果再排序） is effective? （单选）<br/>
<input type="radio" name="pb_rerank" value="1"/>(很不满意)1、
<input type="radio" name="pb_rerank" value="2"/>2、
<input type="radio" name="pb_rerank" value="3"/>3、
<input type="radio" name="pb_rerank" value="4"/>4、
<input type="radio" name="pb_rerank" value="5" />(很满意)5</li></ol>
 <br />
根据你的操作习惯，你对以下组件的看法（多选）：<br />
<img src="images/pb.png" /> <br/>
Input Box:
<input type="checkbox" name="inputbox1" value="实用" />实用、
<input type="checkbox" name="inputbox2" value="有趣"/>有趣、
<input type="checkbox" name="inputbox3" value="布局合理"/>布局合理<br />
Navigation Bar: 
<input type="checkbox" name="nav_bar1" value="实用" />实用、
<input type="checkbox" name="nav_bar2" value="有趣"/>有趣、
<input type="checkbox" name="nav_bar3" value="布局合理"/>布局合理<br />
Picture Box:
<input type="checkbox" name="pic_box1" value="实用" />实用、
<input type="checkbox" name="pic_box2" value="有趣"/>有趣、
<input type="checkbox" name="pic_box3" value="布局合理"/>布局合理<br />
Cluster-view Sidebar:
<input type="checkbox" name="cluster_sidebar1" value="实用" />实用、
<input type="checkbox" name="cluster_sidebar2" value="有趣"/>有趣、
<input type="checkbox" name="cluster_sidebar3" value="布局合理"/>布局合理<br />
Result Window: 
<input type="checkbox" name="result_window1" value="实用" />实用、
<input type="checkbox" name="result_window2" value="有趣"/>有趣、
<input type="checkbox" name="result_window3" value="布局合理"/>布局合理<br />
 <br />
Open-ended questions<br />
从人机交互的角度上考虑，使用本系统你最满意的地方是什么？<br />
<textarea id="like_best" name="like_best" cols="80" rows="3"></textarea><br />
从人机交互的角度上考虑，使用本系统你最不满意的地方是什么？<br />
<textarea id="unlike_best" name="unlike_best" cols="80" rows="3"></textarea><br />
你对PivotBrowser还有其他的意见和建议吗？<br />
<textarea id="suggestion" name="suggestion" cols="80" rows="5"></textarea><br />
 <br />
<label id="lblinfo" style="color: red"></label> <br />
<input type="button" value="提交问卷" onclick="submit_survey()"/>
</p>
</form>
</body>

</html>