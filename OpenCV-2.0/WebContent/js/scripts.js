
// ----------------------------------------------
// Select file and display preload
// ----------------------------------------------
// 获取浏览器尺寸
var winWidth = 0; 
var winHeight = 0;

$(document).on("ready",function(){
	// Re-arrange the positions of competency improvements
	updateImprovementPosition();
	
	// add event listener for elements
	$("#select-file").on("click", function() {
		$("#input-file").click();
	});

	// for uploading file
	$("#input-file").on("change",uploadFile);
	$('#upload-modal').modal({backdrop: 'static', keyboard: false});
	
	// for resume dashboard, competency and format
	$("div.comment").on("click", showCompetencyImprovement);
	$("div.resume-content").on("click", hideCompetencyImprovement);

	findDimensions();
	// Set min-height for .content-container in index.html
	$(".content-container").css("min-height",winHeight-180);
	$(document).trigger("scroll");

	var winHeight = $(window).height();
	var format_evaluation_height = $("#format-evaluation").height();
	var offset = (winHeight - 90 - format_evaluation_height)/2;
	$("#format-evaluation").css("top",document.documentElement.scrollTop||document.body.scrollTop + offset);

	// Make the format scroll
	$(document).on("scroll",function(){
		var winHeight = $(window).height();
		var format_evaluation_height = $("#format-evaluation").height();
		var offset = (winHeight - 90 - format_evaluation_height)/2;
		$("#format-evaluation").css("top",document.documentElement.scrollTop||document.body.scrollTop + offset);
	});

	// Make footer display well
	if(winWidth <= 420){
		$("div.copyright").removeClass("col-xs-6 col-sm-6");
		$("div.copyright").css("padding-left", 20);
	}
	else{
		$("div.copyright").addClass("col-xs-6 col-sm-6");
	}
});

// ----------------------------------------------
// Get window (browser) sizes
// ----------------------------------------------
function findDimensions(){
	//获取窗口宽度
	if (window.innerWidth){
		winWidth = window.innerWidth;
	}
	else if ((document.body) && (document.body.clientWidth)){
		winWidth = document.body.clientWidth; 
	}

	//获取窗口高度 
	if (window.innerHeight){
		winHeight = window.innerHeight; 
	}
	else if ((document.body) && (document.body.clientHeight)){
		winHeight = document.body.clientHeight; 
	}

	//通过深入Document内部对body进行检测，获取窗口大小 
	if (document.documentElement && document.documentElement.clientHeight && document.documentElement.clientWidth) { 
		winHeight = document.documentElement.clientHeight; 
		winWidth = document.documentElement.clientWidth; 
	} 
}

// ----------------------------------------------
// Validate the resume uploaded is word file
// ----------------------------------------------
function validate_ext(filename){
	if (typeof filename != "string"){
		return false;
	}
	else{
		filename = filename.toLowerCase();
		var res = filename.lastIndexOf(".pdf");
		return (res >= 0);
	}
}

// ----------------------------------------------
// Validate email
// ----------------------------------------------
function validate_email(email){
	if (typeof email != "string"){
		return false;
	}
	else{
		if (!email.match(/^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/)){
			return false;
		}
	}
	return true;
}

// ----------------------------------------------
// Select file and delay for some seconds
// ----------------------------------------------
function uploadFile(){
	// Add statistics for uploading file page
	zhuge.track("try_to_upload_file");
	
	// Extract the user email
	var UserEmail = "";
	var strState = $("#SigninState").html();
	if (strState.length > 0 && strState.indexOf("email")>=0){
		var state = jQuery.parseJSON(strState);
		if (state != null || state != undefined){
			if (state.email != null){
				UserEmail = state.email;
			}
		}
	}
	else{
		alert(null, "您还没有登录OpenCV，无法上传文件！", null, {type: "error"});
		return;
	}
	
	// Select file
	var filename = document.getElementById("input-file").value;
	if (!validate_ext(filename)){
		alert(null,"请选择PDF版本简历！",null,{type: "warning"});
	}
	$("#input-file").unbind("change",uploadFile);
	$("#input-file").bind("change",uploadFile);

	// Upload File
	$("#upload-modal").modal("show"); // show upload-modal

	var formData = new FormData();
	formData.append("file", $("#input-file")[0].files[0]);
	 
	if (validate_email(UserEmail)){
		formData.append("email", UserEmail);
		$.ajax({
				url: "EmailServlet",
				type: "POST",
				contentType: false,				
				processData: false,
				cache: false,
				dataType: "json",
				data: formData,
				success: function (status) {
					$("#upload-modal").modal("hide");
					if (status.state ==  0x000){
						$("#PageTransferLink").attr("href",status.transferPage);
						document.getElementById("PageTransferLink").click();
					}else{ // modified by Lantao on 2017-10-01
						alert(null, status.reason, null, {type: "error"});
					}
				},
				error: function(){
					$("#upload-modal").modal("hide");
					alert(null, "文件上传失败", null, {type: "error"});
				}
		}).done(function(res) {
		}).fail(function(res) {
			alert(null, "简历上传失败!", null, {type: "error"});
		});
	}
}


// ----------------------------------------------
// Show competency improvement
// ----------------------------------------------
function showCompetencyImprovement(){
	var data_id = $(this).data("id");
	var comments_num = $("div.comment").length;
	var index = -1;

	if (typeof data_id == "string" && data_id.length > 0){
		for(var i=0; i<comments_num;++i){
			if (data_id.localeCompare("comment-"+(i+1).toString()) == 0){
				index = i;
				break;
			}
		}
	}
	else{
		return;
	}

	if (index >= 0){
		var para_selector = null;

		// Clear other label and improvements
		for (var i=0; i<comments_num; ++i){
			if (i == index){
				continue;
			}

			para_selector = "[data-id='label-"+(i+1).toString()+"']";
			$(para_selector).css("text-decoration","underline");
			$(para_selector).removeClass("selected");
			$("div.comment").eq(i).find("ul.items-nav").addClass("hide");
			$("div.comment").eq(i).css("z-index",1000);
			if ($("div.comment").eq(i).find("div.samples") != null && $("div.comment").eq(i).find("div.samples") != undefined){
				$("div.comment").eq(i).find("div.samples").addClass("hide");
			}
		}

		// Set the style of children
		para_selector = "[data-id='label-"+(index+1).toString()+"']";
		$(para_selector).css("text-decoration","none");
		$(para_selector).addClass("selected");
		$(this).find("ul.items-nav").removeClass("hide");
		$(this).eq(i).css("z-index",1001);
		if ($(this).find("div.samples") != null && $(this).find("samples") != undefined){
			$(this).find("div.samples").removeClass("hide");
		}
		$("div.resume-content").addClass("hide-content");
	}
	//updateImprovementPosition();
}

// ----------------------------------------------
// Hide competency improvement
// ----------------------------------------------
function hideCompetencyImprovement(){
	if ($("p.selected") != null && $("p.selected") != undefined){
		var data_id = $("p.selected").data("id");
		var index = -1;

		if (typeof data_id == "string" && data_id.length > 0){
			for(var i=0; i<$("div.comment").length;++i){
				if (data_id.localeCompare("label-"+(i+1).toString()) == 0){
					index = i;
					break;
				}
			}
		}
		else{
			return;
		}

		// Set the style of competency part
		if (index >= 0){
			var comment_selector = "[data-id=comment-"+(index+1).toString()+"]";
			$("p.selected").css("text-decoration","underline");
			$("p.selected").removeClass("selected");
			$(comment_selector).find("ul.items-nav").addClass("hide");
			$(comment_selector).css("z-index", 1000);
			if ($(comment_selector).find("div.samples") != null && $(comment_selector).find("div.samples") != undefined){
				$(comment_selector).find("div.samples").addClass("hide");
			}
			$("div.resume-content").removeClass("hide-content");
		}
		updateImprovementPosition();
	}
}

// ----------------------------------------------
// Arrange the improvements in competency page
// ----------------------------------------------
function updateImprovementPosition(){
	var comments_length = $("div.comment").length;
	var height = 0;

	// Move each comment block to the position of corresponding label
	for (var i=0; i<comments_length; ++i){
		var para_selector = "[data-id='label-"+(i+1).toString()+"']";

		// Underline the paragraph
		$(para_selector).css("text-decoration","underline");

		// Move the improvement to the specific position
		$("div.comment").eq(i).css("top", $(para_selector).position().top - height);
		height = height + $("div.comment").eq(i).height();
	}
}
