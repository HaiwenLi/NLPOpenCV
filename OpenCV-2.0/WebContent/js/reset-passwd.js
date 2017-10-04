$(document).on("ready",function(){
	$("#user-email").change(function(){
		$("#user-email-errorbar").addClass("hide");
	});
	$("#user-email").on("input",function(){
		$("#user-email-errorbar").addClass("hide");
	});

	// For new password
	$("#reset-passwd").change(function(){
		$("#reset-passwd-errorbar").addClass("hide");
	});
	$("#reset-passwd").on("input", function(){
		$("#reset-passwd-errorbar").addClass("hide");
	});
	$("#reset-passwd-confirm").change(function(){
		$("#reset-passwd-confirm-errorbar").addClass("hide");
	});
	$("#reset-passwd-confirm").on("input", function(){
		$("#reset-passwd-confirm-errorbar").addClass("hide");
	});

	$("#forgotpasswd-submit").on("click",forgot_password);
	$("#resetpasswd-submit").on("click",reset_password);
});

$(document).keyup(function(event){
	if(event.keyCode == 13){
		if ($("#forgotpasswd-submit").hasClass("signup")){
			$("#forgotpasswd-submit").trigger("click");
		}
		if ($("resetpasswd-submit").hasClass("signup")){
			$("resetpasswd-submit").trigger("click");
		}
	}
});

// ----------------------------------------------
// Validate email
// ----------------------------------------------
function validate_email(email){
	if (typeof email != "string" || email.length <= 4){
		return false;
	}
	else{
		if (!email.match(/^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/)){
			return false;
		}
	}
	return true;
}

function validate_passwd(passwd){
	if (typeof passwd != "string" || passwd.length < 8){
		return false;
	}
	else{
		if (!passwd.match(/^([\.a-zA-Z0-9_-])/)){
			return false;
		}
	}
	return true;
}

// ----------------------------------------------
// Forgot Password
// ----------------------------------------------
function forgot_password(){
	var email = $("#user-email").val();
	if (!validate_email(email)){
		$("#user-email-errorbar").html("邮箱格式不正确，请重新输入");
		$("#user-email-errorbar").removeClass("hide");
		return;
	}

	// Using Ajax to submit the form
	$.ajax({
		url: "SendResetPasswordEmailServlet",
		type: "POST",
		dataType: "json",
		data: {"email": email},
		success: function (status) {
			if (status.state == 1){
				alert("","重置密码信息已发送至您的邮箱，请查收！");
			}
			else if(status.state == 0){
				$("#user-email-errorbar").html("该邮件地址不存在，请重新输入");
				$("#user-email-errorbar").removeClass("hide");
			}
			else if (status.state == 2){
				$("#user-email-errorbar").html("很抱歉，因为服务器罢工了，无法发送邮件。请稍后再试！");
				$("#user-email-errorbar").removeClass("hide");
			}
		},
		error: function(){
		}
	}).done(function(res) {
	}).fail(function(res) {});
}

//----------------------------------------------
// Get params in url
// From: http://blog.csdn.net/kongjiea/article/details/39644623
//----------------------------------------------
function getUrlParam(paramName) {  
    var paramValue = "", isFound = false;  
    if (this.location.search.indexOf("?") == 0 && this.location.search.indexOf("=") > 1) {  
        var arrSource = unescape(this.location.search).substring(1, this.location.search.length).split("&"), i = 0;  
        while (i < arrSource.length && !isFound) arrSource[i].indexOf("=") > 0 && arrSource[i].split("=")[0].toLowerCase() == paramName.toLowerCase() && (paramValue = arrSource[i].split("=")[1], isFound = !0), i++  
    }  
    return paramValue == "" && (paramValue = null), paramValue
} 

// ----------------------------------------------
// Reset Password
// ----------------------------------------------
function reset_password(){
	var email = getUrlParam("useremail");
	var passwd = $("#reset-passwd").val();
	var confirmed_passwd = $("#reset-passwd-confirm").val();

	if (!validate_email(email)){
		$("#user-email-errorbar").removeClass("hide");
		return;
	}
	if (!validate_passwd(passwd)){
		$("#reset-passwd-errorbar").removeClass("hide");
		return;
	}
	if (typeof confirmed_passwd != "string" || passwd.localeCompare(confirmed_passwd) != 0){
		$("#reset-passwd-confirm-errorbar").removeClass("hide");
		return;
	}
	
	// Using Ajax to submit the form
	$.ajax({
		url: "UpdatePasswordServlet",
		type: "POST",
		dataType: "json",
		data: {"email": email, "password": passwd},
		success: function (status) {
			if (status.state == true){
				alert("","您的密码已更新！",function(){
					document.getElementById("indexPage").click();}
				);
			}
			else{
				alert("","很抱歉，因为服务器罢工了，无法更新您的密码。请稍后再试！");
			}
		},
		error: function(){
			alert("","很抱歉，因为服务器罢工了，无法更新您的密码。请稍后再试！");
		}
	}).done(function(res) {
	}).fail(function(res) {});
}