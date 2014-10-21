/**
 * 调用bootstrap样式的弹出框
 */
function alertMsg(msg) {
	var deferred = $.Deferred();
	var width = 350;
	if($.isPlainObject(msg)) {
		width = msg.width || width;
		msg = msg.message;
	}
	var $modal = $('#J_alertModal');
	if($modal.size() == 0) {
		alert(msg);
		return deferred.resolve().promise();
	}
	msg = ('' + msg).replace(/\n/g, '<br/>');
	$modal.find('.modal-body p').html(msg);
	$modal.modal().css({
		width: width,
		'margin-left': function() {
			return - $(this).width() / 2;
		},
		'margin-top': function() {
			return ( $(window).height() - $(this).height() ) / 3;	 // 乱诌的一句，完全没有道理，太神奇了
		}
	});
	$modal.on('hidden', function(){
		$(this).off('hidden');
		deferred.resolve();
	});
	return deferred.promise();
}