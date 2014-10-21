<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>编辑定时任务</title>
<link type="image/x-icon" rel="shortcut icon" href="${ctx_path}/img/favicon.ico" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css" />
<style>
.title {
	text-align: center;
	font-family: 微软雅黑;
}
.btn-sep {
	width: 15px; display: inline-block;
}

.table thead {
	background-color: #eee;
}

.wrapper {
	margin-bottom: 100px;
}

.modal-backdrop, .modal-backdrop.fade.in {
	opacity: 0.5;
	filter: alpha(opacity=50);
}

#J_alertModal .modal-header, #J_confirmModal .modal-header {
	font-size: 16px;
	font-weight: bold;
}

#J_alertModal .modal-body, #J_confirmModal .modal-body {
	text-align: center;
	font-size: 16px;
	font-weight: bold;
}
.edit-wrapper {
	margin: 0px auto; width: 400px;
}
.label-wrapper {
	width: 120px;
}
.label-wrapper label {
	font-size: 20px;
	line-height: 25px;
	text-align: right;
}
.input-wrapper input[type="text"]{
	font-size:16px;
	margin-bottom:0px;
	width: 95%;
}
.input-wrapper select {
	font-size:16px;
	margin-bottom:0px;
	width: 100%;
}
.btn-wrapper {
	text-align:center !important;
}
h3 {
	text-align: center;
}
</style>
</head>
<body>
<div>
	<h3 class="title">
		<c:choose>
			<c:when test="${cronJob != null}">修改定时任务</c:when>
			<c:otherwise>新增定时任务</c:otherwise>
		</c:choose>
	</h3>
	<div class="edit-wrapper">
		<table class="table table-bordered table-condensed" style="width: 100%">
			<tbody id="J_tbody">
				<c:if test="${cronJob != null}">
				<tr>
					<td class="label-wrapper">
						<label>id:</label>
					</td>
					<td class="input-wrapper">
						<input type="text" name="id" disabled="disabled" value="${cronJob.id}"/>
					</td>
				</tr>
				</c:if>
				<tr>
					<td class="label-wrapper">
						<label>名称:</label>
					</td>
					<td class="input-wrapper">
						<input type="text" name="name" value="${cronJob.name}"/>
					</td>
				</tr>
				<tr>
					<td class="label-wrapper">
						<label>cron表达式:</label>
					</td>
					<td class="input-wrapper">
						<input type="text" name="cron" value="${cronJob.cron}"/>
					</td>
				</tr>
				<tr>
					<td class="label-wrapper">
						<label>状态:</label>
					</td>
					<td class="input-wrapper">
						<select name="enabled" id="J_enabledSel">
							<option value="true">开启</option>
							<option value="false">关闭</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="btn-wrapper" colspan="2">
						<button id="J_saveBtn" class="btn btn-primary">保存</button>
						<div class="btn-sep">&nbsp;</div>
						<button id="J_closeBtn" class="btn">关闭</button>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>
<div id="J_alertModal" class="modal hide" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		提示
	</div>
	<div class="modal-body">
		<p></p>
	</div>
	<div class="modal-footer">
		<button class="btn" data-dismiss="modal" aria-hidden="true">确定</button>
	</div>
</div>
</body>
<script type="text/javascript" src="${ctx_path}/js/jquery.js"></script>
<script type="text/javascript" src="${ctx_path}/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${ctx_path}/js/util/alertMsg.js"></script>
<script>
var CTX_PATH = '${ctx_path}';
$(function(){
	initEnabledSel();
	initSaveBtn();
	initCloseBtn();
});

function initEnabledSel() {
	$('#J_enabledSel').val('${cronJob.enabled}');
}

function initSaveBtn() {
	$('#J_saveBtn').on('click', function(){
		var params = collectParams('#J_tbody input[type=text],#J_tbody input[type=hidden], #J_tbody select'),
			url = CTX_PATH + '/crawler/editJob.do';
		$.post(url, params, function(data){
			if(data.success === true) {
				alertMsg({message: '操作成功!', width: 250}).done(function(){
					//opener.reloadPage();
					window.close();
				});
			} else {
				alertMsg(data.message);
			}
		});
	});
}

function collectParams(selector) {
	var params = {};
	if(!selector) {
		return params;
	}
	$(selector).each(function(i, input){
		var $input = $(input);
		var key = $input.attr('name'),
			value = $input.val();
		key && (params[key] = value);
	});
	return params;
}

function initCloseBtn() {
	$('#J_closeBtn').on('click', function(){
		window.close();
	});
}
</script>
</html>