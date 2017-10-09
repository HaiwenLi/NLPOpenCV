
var PreSignin = true;
var UserSignin = false;
var UserEmail = "";
var Current_Window = "Main";
var current_url = "";

$(document).on("ready",function(){
	// Set signin and signup modal property
	$('#signin-modal').modal({backdrop: 'static', keyboard: false, show: false});
	$('#signup-modal').modal({backdrop: 'static', keyboard: false, show: false});

	Current_Window = "Main";
	$("a.nav-signin").on("click",function(){
		PreSignin = false;
		Current_Window = "Signin";
		clearSigninAndSignupContent();
		$("h6.signin-header").html("登录");
		$("p.signin-welcome").addClass("hide");
		$("#signin-modal").modal("show");
	});
	
	$("a.nav-logout").on("click", signout); //sign out
	
	// sign in prompt
	$("#signin-email").change(function(){
		$("#signin-email-errorbar").addClass("hide");
	});
	$("#signin-email").on("input",function(){
		$("#signin-email-errorbar").addClass("hide");
	});
	$("#signin-passwd").change(function(){
		$("#signin-passwd-errorbar").addClass("hide");
	});
	$("#signin-passwd").on("input", function(){
		$("#signin-passwd-errorbar").addClass("hide");
	});
	
	// sign up prompt
	$("#sigup-name").change(function(){
		$("#signup-name-errorbar").addClass("hide");
	});
	$("#sigup-name").on("input", function(){
		$("#signup-name-errorbar").addClass("hide");
	});
	$("#signup-passwd").change(function(){
		$("#signup-passwd-errorbar").addClass("hide");
	});
	$("#signup-passwd").on("input", function(){
		$("#signup-passwd-errorbar").addClass("hide");
	});
	$("#sigup-passwd-confirm").change(function(){
		$("#sigup-passwd-confirm-errorbar").addClass("hide");
	});
	$("#sigup-passwd-confirm").on("input", function(){
		$("#sigup-passwd-confirm-errorbar").addClass("hide");
	});
		
	$("#nav-uploadbtn").on("click", preSignin);
	$("#uploadbtn").on("click", preSignin);

	$("#signin-submit").on("click", login);
	$("#signup-submit").on("click", signup);
	
	// Adjust customer feedback section height
	if ($("div.customers-feedback-userinfo").height() > $("div.customers-feedback-content").height()){
		var top = $("div.customers-feedback-userinfo").height() - $("div.customers-feedback-content").height();
		$("div.customers-feedback-content").css("margin-top",(top/2).toString()+"px");
	}
	else{
		var top = $("div.customers-feedback-content").height() - $("div.customers-feedback-userinfo").height();
		$("div.customers-feedback-userinfo").css("margin-top",(top/2).toString()+"px");
	}
});

$(document).keyup(function(event){
	if(event.keyCode == 13){
		if (Current_Window.localeCompare("Main") == 0){
			return;
		}
		else if(Current_Window.localeCompare("Signin") == 0 && $("body").hasClass("modal-open")){
			$("#signin-submit").trigger("click");
		}
		else if(Current_Window.localeCompare("Signup") == 0 && $("body").hasClass("modal-open")){
			$("#signup-submit").trigger("click");
		}
	}
});

$(window).on("load",function(){
	getSigninState();
	
	var cur_content = 0;
	var timelines = [1000, 100, 100, 100, 1000];
	var delay_scale = 0.7;
	var timer = null;
	var animation_urls = [
		"images/pre/content-1.png",
		"images/pre/content-2.png",
		"images/pre/content-3.png",
		"images/pre/content-4.png",
		"images/pre/content-5.png"
	];

	function auto_next()
	{
		cur_content = (cur_content + 1) % timelines.length
		update_content()
	}

	function update_content()
	{
		set_animation(animation_urls[cur_content], timelines,
			animation_urls[(cur_content + 1) % timelines.length])
	}

	var animate = function(img_element,img_url)
	{
		var i = 0;
		var f = function()
		{
			img_element.src = img_url;
			var frame = i++ % timelines.length
			var delay = timelines[frame] * delay_scale;

			if (i == timelines.length * 2)
				timer = window.setTimeout(auto_next, delay);
			else
				timer = window.setTimeout(f, delay);
		}

		if (timer) window.clearTimeout(timer);
		f();
	}

	function set_animation(img_url, timeline, preload_url)
	{
		var img = new Image();
		var img_element = document.getElementById("laptop");
		img.onload = function()
		{
			animate(img_element,img_url);
			var preload_image = new Image()
			preload_image.src = preload_url;
		}
		img.src = img_url;
		current_url = img_url;
	}

	set_animation(animation_urls[0], timelines[0], animation_urls[1]);
});


// ----------------------------------------------
// Validate email
// ----------------------------------------------
function validate_email(email){
	if (typeof email != "string" || email.length <= 4){
		return false;
	}
	else{
		if (email.match(/^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/)){
			return true;
		}
	}
	return false;
}

function checkUsername(username){
	var str = username;
	var Expression=/^[A-Za-z0-9]{6,20}$/;
	var objExp = new RegExp(Expression); //创建正则表达式对象
	if(objExp.test(str)==true){          //通过正则表达式验证
	  return true;
	}else{
	  return false;
	}
}

function checkPassword(passwd){
	var str=passwd;
	var Expression=/^([A-Za-z0-9]|[._]){8,20}$/;
	var objExp = new RegExp(Expression);  //创建正则表达式对象
	if(objExp.test(str)==true){           //通过正则表达式验证
	  return true;
	}else{
	  return false;
	}
}

function preSignin(){
	PreSignin = true;
	
	// Check whether the user has signed in
	getSigninState();
	if (UserSignin){
		Current_Window = "Main";
		document.getElementById("uploadPage").click();
	}
	else{
		Current_Window = "Signin";
		$("h6.signin-header").html("欢迎使用简历帮");
		$("p.signin-welcome").removeClass("hide");
		clearSigninAndSignupContent();
		$("#signin-modal").modal("show");
	}
}

//----------------------------------------------
// Check whether the email has been registered
//----------------------------------------------
function checkEmailRegistered(email){
	// Submit the email to server
	$.ajax({
		url: "LoginServlet",
		type: "POST",
		dataType: "json",
		data: {"email": email, "passwd": ""},
		success: function (status) {
			if (status.hasEmail == true){
				$("#signin-passwd").removeClass("hide");
				$("#signin-reset-passwd").removeClass("hide");
				if (PreSignin){
					$("#signin-submit").html("开始");
				}
				else{
					$("#signin-submit").html("登录");	
				}
				$("#signin-submit").attr("name","final");
			}
			else if(status.hasEmail == false){
				UserEmail = email;
				$("p.signup-welcome").html(email + "，你好!</br>请输入以下内容，即将开始使用简历帮");
				$("#signin-modal").modal("hide");
				clearSigninAndSignupContent();
				Current_Window = "Signup";
				$("#signup-modal").modal("show");
			}
			PreSignin = true;
		},
		error: function(){
		}
	}).done(function(res) {
	}).fail(function(res) {});
}

// ----------------------------------------------
// Submit login page
// ----------------------------------------------
function login(){
	var email = $("#signin-email").val();
	if (!validate_email(email)){
		$("#signin-email-errorbar").removeClass("hide");
		return;
	}
	
	// First check whether the email has been registered
	if ($("#signin-submit").attr("name").localeCompare("next") == 0){
		checkEmailRegistered(email);
	}
	else if($("#signin-submit").attr("name").localeCompare("final") == 0){
		var passwd = $("#signin-passwd").val();
		if (typeof passwd == "string" && passwd.length > 0){
			$("#signin-passwd-errorbar").html("电子邮件地址或密码错误");
			$.ajax({
				url: "LoginServlet",
				type: "POST",
				dataType: "json",
				data: {"email": email, "passwd": passwd},
				success: function (status) {
					if (status.isLogin == true){
						// Stay in index page
						$("#signin-modal").modal("hide");
						$("a.nav-signin").addClass("hide");
						$("a.nav-logout").removeClass("hide");
						clearSigninAndSignupContent();
						Current_Window = "Main";
						$("#indexPage").attr("href",status.transferPage);
			    	    document.getElementById("indexPage").click();
					}
					else if(status.isLogin == false){
						$("#signin-passwd-errorbar").removeClass("hide");
					}
				},
				error: function(){
				}
			}).done(function(res) {
			}).fail(function(res) {});
		}
		else{
			$("#signin-passwd-errorbar").html("请输入密码");
			$("#signin-passwd-errorbar").removeClass("hide");
			return;
		}
	}
}

// ----------------------------------------------
// Submit signup page
// ----------------------------------------------
function signup(){
	var name = $("#signup-name").val();
	var passwd = $("#signup-passwd").val();
	var confirmed_passwd = $("#signup-passwd-confirm").val();

	if (typeof name != "string" || (typeof name == "string" && !checkUsername(name))){
		$("#signup-name-errorbar").removeClass("hide");
		return;
	}
	if (!checkPassword(passwd)){
		$("#signup-passwd-errorbar").removeClass("hide");
		return;
	}
	if (typeof confirmed_passwd != "string" || passwd.localeCompare(confirmed_passwd) != 0){
		$("#signup-passwd-confirm-errorbar").removeClass("hide");
		return;
	}
	
	// Using Ajax to submit the form
	$.ajax({
		url: "SignUpServlet",
		type: "POST",
		dataType: "json",
		data: {"username": name, "email": UserEmail, "passwd": passwd},
		success: function (status) {
			if (status.hasSignUp== true){
				$("#signup-modal").modal("hide");
				$("a.nav-signin").addClass("hide");
				$("a.nav-logout").removeClass("hide");
				clearSigninAndSignupContent();
				alert("","欢迎使用简历帮",function(){
					Current_Window = "Main";
					document.getElementById("indexPage").click();}
				);
			}
		},
		error: function(){
		}
	}).done(function(res) {
	}).fail(function(res) {});
	
	// Prompt user to check his/her email
}

//----------------------------------------------
// Clear signin and signup content
//----------------------------------------------
function clearSigninAndSignupContent(){
	$("#signin-email").val("");
	$("#signin-email-errorbar").addClass("hide");
	$("#signin-passwd").val("");
	$("#signin-passwd-errorbar").addClass("hide");
	
	$("#signup-name").val("");
	$("#signup-name-errorbar").addClass("hide");
	$("#signup-passwd").val("");
	$("#signup-passwd-errorbar").addClass("hide");
	$("#signup-passwd-confirm").val("");
	$("#signup-passwd-confirm-errorbar").addClass("hide");
}

//----------------------------------------------
// Get sign in state
//----------------------------------------------
function getSigninState(){
	// Get SigninState
	var strState = $("#SigninState").html();
	if (strState.length > 0 && strState.indexOf("email")>=0){
		var state = jQuery.parseJSON(strState);
		if (state != null || state != undefined){
			if (state.email != null){
				UserSignin = true;
				UserEmail = state.email;
				
				// Hide sign in link and show sign out link
				$("a.nav-signin").addClass("hide");
				$("a.nav-logout").removeClass("hide");
			}
		}
	}
}

//----------------------------------------------
// Sign out
//----------------------------------------------
function signout(){
	$.ajax({
		url: "SignOutServlet",
		type: "POST",
		dataType: "json",
		data: "",
		success: function (status) {
			Current_Window = "Main";
			$("a.nav-logout").click();
		},
		error: function(){
		}
	}).done(function(res) {
	}).fail(function(res) {});
}