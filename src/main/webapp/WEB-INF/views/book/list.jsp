<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>电子书列表</title>
<link type="image/x-icon" rel="shortcut icon" href="${ctx_path}/img/favicon.ico" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css" />
<style>
.detail-block {
	overflow: hidden;
}

.detail-block > div {
	float: left;
}

.detail-block h4 {
	margin: 6px auto;
}

.detail-label {
	width: 80px;
}

.detail-content {
	max-width: 490px;
}
</style>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrapPageBar.js"></script>
<script>
var CTX_PATH = '${pageContext.request.contextPath}';
$(function(){
	initQueryForm();
	initPageBar();
});

function initQueryForm(){
	$('#J_year').val('${year}');
	$('#J_publisher').val("${publisher}");
	$('#J_clearQueryBtn').click(function(){
		$('#J_publisher').val('');
		$('#J_name').val('');
		$('#J_authorName').val('');
		$('#J_year').val('');
		$('#J_start').val('0');
		$('#J_queryForm').submit();
	});
}

function initPageBar(){
	var start = parseInt('${start}');
	var limit = parseInt('${limit}');
	var totalCount = parseInt('${totalCount}');
	isNaN(start) && (start = 0);
	isNaN(limit) && (limit = 20);
	isNaN(totalCount) && (totalCount = 1);
	var curPage = Math.floor(start / limit) + 1;
	var totalPage = Math.floor(totalCount / limit) + (totalCount % limit > 0? 1: 0);
	$('#J_pageBarTop, #J_pageBarBottom').bootstrapPageBar({
		curPageNum: curPage,
		totalPageNum: totalPage,
		maxBtnNum: 10,
		pageSize: limit,
		siblingBtnNum: 2,
		paginationCls: 'pagination-right',
		click: function(i, pageNum){
			var recordForm = $('#J_queryForm');
			var start = (pageNum - 1) * limit;
			$('#J_start').val(start);
			recordForm.submit();
			return false;
		}
	});
}

function downloadBook(pageUrl) {
	if(!pageUrl) {
		alert('参数有误!');
		return;
	}
	window.open(CTX_PATH + '/book/download.do?pageUrl=' + encodeURIComponent(pageUrl), 'bookDownloadFrame');
}

</script>
</head>
<body>
<div style="display:none;">
	<iframe name="bookDownloadFrame"></iframe>
</div>
<div id="J_wrapper" style="margin: 30px 0px;">
	<h2 style="text-align: center;">IT-EBOOKS电子书列表</h2>
	<div style="width: 700px; margin: 20px auto 10px;">
		<form id="J_queryForm" action="./list.do">
			<input type="hidden" id="J_start" name="start" value="0" />
			<input type="hidden" id="J_limit" name="limit" value="20" />
			<table style="width: 100%; margin: 0px auto;">
				<tr>
					<td style="text-align: right">
						<label><strong>书名:&nbsp;&nbsp;</strong></label>
					</td>
					<td style="width: 25%;">
						<input id="J_name" name="name" type="text" class="input-medium" value="${name}"/>
					</td>
					<td style="text-align: right">
						<label><strong>作者:&nbsp;&nbsp;</strong></label>
					</td>
					<td style="width: 35%">
						<input id="J_authorName" name="authorName" type="text" class="input-medium" value="${authorName }"/>
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
							<option>Apress</option>
							<option>Wrox</option>
							<option>Wiley</option>
							<option>Packt Publishing</option>
							<option>The Pragmatic Programmers</option>
							<option>Cisco Press</option>
							<option>SAMS Publishing</option>
							<option>McGraw-Hill</option>
							<option>Addison-Wesley</option>
							<option>SitePoint</option>
							<option>Prentice Hall</option>
							<option>No Starch Press</option>
							<option>MicrosoftPress</option>
						</select>
					</td>
					<td style="text-align: right;">
						<label><strong>年份:&nbsp;&nbsp;</strong></label>
					</td>
					<td>
						<select id="J_year" name="year" class="input-medium" style="width: 165px;">
							<option value="">请选择...</option>
							<c:forEach begin="0" end="19"  var="year">
								<option>${2013-year}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="4" style="text-align: center;">
							<button id="J_queryBtn" type="submit" class="btn btn-primary" style="width: 82px;">&nbsp;查&nbsp;&nbsp;询&nbsp;</button>
								&nbsp;&nbsp;&nbsp;&nbsp;
								<button id="J_clearQueryBtn" type="button" class="btn">清除条件</button>
					</td>
				</tr>
			</table>
		</form>
	</div>
	<hr/>
	<div style="width:800px; margin: 10px auto 20px;">
		<div>
			<div id="J_pageBarTop" style="margin-top: 5px;"></div>
		</div>
		<table class="table table-bordered table-condensed table-hover" style="width: 100%;">
			<tbody>
				<c:forEach items="${list}" var="book">
					<tr>
						<td style="width:200px;height: 245px;">
							<img width="200" src="http://it-ebooks.info/${book.coverImgUrl}"/>
						</td>
						<td>
							<div class="detail-block">
								<div class="detail-label">
									<h4>书&nbsp;&nbsp;&nbsp;&nbsp;名:</h4>
								</div>
								<div class="detail-content"><h4>${book.name}</h4></div>
							</div>
							<div class="detail-block">
								<div class="detail-label">
									<h4>出版社:</h4>
								</div>
								<div class="detail-content"><h4>${book.publisher}</h4></div>
							</div>
							<div class="detail-block">
								<div class="detail-label">
									<h4>作&nbsp;&nbsp;&nbsp;&nbsp;者:</h4>
								</div>
								<div class="detail-content"><h4>${book.authorName}</h4></div>
							</div>
							<div class="detail-block">
								<div class="detail-label">
									<h4>年&nbsp;&nbsp;&nbsp;&nbsp;份:</h4>
								</div>
								<div class="detail-content"><h4>${book.year}</h4></div>
							</div>
							<div class="detail-block">
								<div class="detail-label">
									<h4>页&nbsp;&nbsp;&nbsp;&nbsp;数:</h4>
								</div>
								<div class="detail-content"><h4>${book.pages}</h4></div>
							</div>
							<div class="detail-block">
								<div class="detail-label">
									<h4>大&nbsp;&nbsp;&nbsp;&nbsp;小:</h4>
								</div>
								<div class="detail-content"><h4>${book.size}</h4></div>
							</div>
							<div class="detail-block">
								<h4>
									<%-- <a href="${book.pageUrl}" target="_blank">链&nbsp;&nbsp;&nbsp;&nbsp;接</a> --%>
									<a href="javascript:downloadBook('${book.pageUrl}')">下&nbsp;&nbsp;&nbsp;&nbsp;载</a>
								</h4>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<div>
			<div id="J_pageBarBottom" style="margin-top: 5px;"></div>
		</div>
	</div>
</div>
</body>
</html>