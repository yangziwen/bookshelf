<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>index</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css" />
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrapPageBar.js"></script>
</head>
<body>
<div style="width: 700px; margin: 20px auto 10px;">
	<table style="width: 100%; margin: 0px auto;">
		<tr>
			<td style="text-align: right">
				<label><strong>书名:&nbsp;&nbsp;</strong></label>
			</td>
			<td style="width: 25%;">
				<input id="J_name" name="name" type="text" class="input-medium" />
			</td>
			<td style="text-align: right">
				<label><strong>作者:&nbsp;&nbsp;</strong></label>
			</td>
			<td style="width: 35%">
				<input id="J_authorName" name="authorName" type="text" class="input-medium" />
			</td>
		</tr>
		<tr>
			<td style="text-align: right;">
				<label><strong>出版社:&nbsp;&nbsp;</strong></label>
			</td>
			<td>
				<select id="J_publisher" name="publisher" class="input-medium" style="width: 165px;">
					<option value="">请选择...</option>
					<option>O'Reilly Media</option>
					<option>Manning</option>
					<option>Appress</option>
					<option>Wrox</option>
					<option>Wiley</option>
					<option>Packt Publishing</option>
					<option>The Pragmatic Programmers</option>
					<option>Cisco Press</option>
					<option>SAMS Publishing</option>
					<option>McGraw-Hill</option>
					<option>Addison-Wesley</option>
					<option>SitePoint</option>
					<option>Cisco Press</option>
					<option>Prentice Hall</option>
					<option>MicrosoftPress</option>
				</select>
			</td>
			<td style="text-align: right;">
				<label><strong>年份:&nbsp;&nbsp;</strong></label>
			</td>
			<td>
				<select id="J_year" name="year" class="input-medium" style="width: 165px;">
					<c:forEach begin="0" end="19"  var="year">
						<option>${2013-year}</option>
					</c:forEach>
				</select>
			</td>
		</tr>
	</table>
</div>
<hr/>
<div style="width:1000px; margin: 10px auto 20px;">
	<table class="table table-bordered table-condensed table-hover" style="width: 100%;">
		<tbody>
			<c:forEach items="${list}" var="book">
				<tr>
					<td style="width:200px;height: 245px;">
						<img width="200" src="http://it-ebooks.info/${book.coverImgUrl}"/>
					</td>
					<td>
						<h4>书&nbsp;&nbsp;&nbsp;&nbsp;名:&nbsp;&nbsp;&nbsp;&nbsp;${book.name}</h4>
						<h4>出版社:&nbsp;&nbsp;&nbsp;&nbsp;${book.publisher}</h4>
						<h4>作&nbsp;&nbsp;&nbsp;&nbsp;者:&nbsp;&nbsp;&nbsp;&nbsp;${book.authorName}</h4>
						<h4>年&nbsp;&nbsp;&nbsp;&nbsp;份:&nbsp;&nbsp;&nbsp;&nbsp;${book.year}</h4>
						<h4>页&nbsp;&nbsp;&nbsp;&nbsp;数:&nbsp;&nbsp;&nbsp;&nbsp;${book.pages }</h4>
						<h4>
							<a href="###">下&nbsp;&nbsp;&nbsp;&nbsp;载</a>
						</h4>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>
</body>
</html>