<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>谢谢参与问卷调查</title>
</head>

<body>
<%
if ("true".equalsIgnoreCase(request.getParameter("re")))
{
	out.print("提交问卷调查成功。谢谢。");
}
else
{
	out.print("<font color='red'>提交问卷调查失败。请确认填写无误。</font>");
}
%>
</body>
</html>