<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh-CN">
<head>
<title>简历评测与提升</title>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" href="images/icon/logo.ico" type="image/x-icon>" />

<link rel="stylesheet" href="css/bootstrap.min.css" />
<link rel="stylesheet" href="css/font-awesome.min.css" />
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/normalize.css" />
<link rel="stylesheet" href="css/BeAlert.css" />

<script type="text/javascript">
	    window.zhuge = window.zhuge || [];window.zhuge.methods = "_init debug identify track trackLink trackForm page".split(" ");
	    window.zhuge.factory = function(b) {return function() {var a = Array.prototype.slice.call(arguments);a.unshift(b);
	    window.zhuge.push(a);return window.zhuge;}};for (var i = 0; i < window.zhuge.methods.length; i++) {
	    var key = window.zhuge.methods[i];window.zhuge[key] = window.zhuge.factory(key);}window.zhuge.load = function(b, x) {
	    if (!document.getElementById("zhuge-js")) {var a = document.createElement("script");var verDate = new Date();
	    var verStr = verDate.getFullYear().toString()+ verDate.getMonth().toString() + verDate.getDate().toString();
	    a.type = "text/javascript";a.id = "zhuge-js";a.async = !0;a.src = (location.protocol == 'http:' ? "http://sdk.zhugeio.com/zhuge.min.js?v=" : 'https://zgsdk.zhugeio.com/zhuge.min.js?v=') + verStr;
	    a.onerror = function(){window.zhuge.identify = window.zhuge.track = function(ename, props, callback){if(callback && Object.prototype.toString.call(callback) === '[object Function]')callback();};};
	    var c = document.getElementsByTagName("script")[0];c.parentNode.insertBefore(a, c);window.zhuge._init(b, x)}};
	    window.zhuge.load('db9e0a44c861492cbbe6754f78b74f9c',{debug:true});//配置应用的AppKey
	    zhuge.track('upload-page');
	</script>

<script>
		var _hmt = _hmt || [];
		(function() {
		  var hm = document.createElement("script");
		  hm.src = "https://hm.baidu.com/hm.js?8c03f40bbf9e3b5954a65dc4fe1b2303";
		  var s = document.getElementsByTagName("script")[0]; 
		  s.parentNode.insertBefore(hm, s);
		})();
	</script>
</head>

<body>
	<header id="header"> <nav
		class="navbar navbar-default st-navbar">
	<div class="container">
		<div class="navbar-header logo">
			<button type="button" class="navbar-toggle collapsed"
				data-toggle="collapse" data-target="#navbar" aria-expanded="false"
				aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a id="indexPage" href="index.jsp">
				<p style="font-size: 26px; font-weight: 600;">简历帮</p>
				<p>人工智能&nbsp;|&nbsp;测简历&nbsp;改简历</p>
			</a> <a id="indexPage" class="hide" href="index.jsp"></a>
		</div>
	</div>
	</nav> </header>

	<section id="st-inputFile">
	<div class="container content-container">
		<div class="col-md-12 file-input-box">
			<div class="box-inner">
				<img src="images/icon/file.png" class="file-icon"></img>
				<p style="font-size: 19px; margin-bottom: 0">选择背景</p>
				<div style="font-size: 15px; line-height: 20px; margin-bottom: 20px">
					<label class="radio-inline"> <input type="radio"
						name="business" id="business-option" checked> 商科/管理
					</label> <label class="radio-inline" style="color: gray"> <input
						type="radio" name="academy" id="academy-option" disabled>科研/开发
						(<small>开发中</small>)
					</label>
				</div>
				<button id="select-file" type="button" name="select-file"
					style="font-size: 18px">点击上传简历</button>
				<input id="input-file" type="file" name="input-file" value=""
					accept="application/pdf" />
				<!-- pplication/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document -->
				<p style="font-size: 12px; margin-top: -8px;">支持：中文、PDF</p>
			</div>
		</div>

		<div class="col-sm-12 col-md-12 product-operations"
			style="margin-top: 50px">
			<div class="col-sm-4 col-md-4 operation">
				<img src="images/icon/upload.png"></img>
				<h3>上传</h3>
				<p>
					一键上传</br> PDF版简历</br> 超简单！
				</p>
			</div>

			<div class="col-sm-4 col-md-4 operation">
				<img src="images/icon/evaluate.png"></img>
				<h3>评测</h3>
				<p>
					立刻获取</br> 简历评测综合打分</br> 及各分项打分
				</p>
			</div>

			<div class="col-sm-4 col-md-4 operation">
				<img src="images/icon/improve.png"></img>
				<h3>提升</h3>
				<p>
					逐行细化修改意见</br> 提升你的</br> 第一竞争力！
				</p>
			</div>
			<div class="clearfix"></div>
		</div>
	</div>
	</section>

	<!-- Modal -->
	<div class="modal fade" id="upload-modal" tabindex="-1" role="dialog"
		aria-labelledby="resume-upload">
		<div class="modal-dialog" role="document">
			<div class="modal-content loader-content">
				<div class="modal-body">
					<div style="margin-bottom: 30px;">
						<button type="button" class="close" data-dismiss="modal"
							aria-hidden="true"
							stype="position: relative; right: 0; margin-right: 20px">&times;</button>
					</div>
					<div class="row">
						<div>
							<p>同学，欢迎使用简历帮，测评、提升你的简历；</p>
							<p>
								系统正在上传和分析你的简历，请稍后。<a id="showServerDetails"
									href="javascript:void(0)">查看详细处理流程</a>
							</p>
							<ul id="serverDetailsList" class="hide">
								<li>上传简历至服务器；</li>
								<li>分析简历排版结构；</li>
								<li>提取简历内容；</li>
								<li>使用NLP分析、测评简历内容；</li>
								<li>生成简历测评页面。</li>
							</ul>
							<div class="loader">
								<img src="images/icon/upload_loader.gif"></img>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<footer id="footer">
	<div class="container">
		<div class="row">
			<div class="col-xs-6 col-sm-6 copyright">
				<p>
					&copy; 2017 <a href="#header"></a>. All Rights Reserved.
				</p>
			</div>
			<div class="col-xs-6 col-sm-6 footer-social-icons">
				<span>Follow us:</span> <a href=""><i class="fa fa-facebook"></i></a>
				<a href=""><i class="fa fa-twitter"></i></a> <a href=""><i
					class="fa fa-google-plus"></i></a> <a href=""><i
					class="fa fa-pinterest-p"></i></a>
			</div>
		</div>
	</div>
	</footer>

	<script type="text/javascript" src="js/jquery.min.js"></script>
	<script type="text/javascript" src="js/bootstrap.min.js"></script>
	<script type="text/javascript" src="js/BeAlert.js"></script>
	<script type="text/javascript" src="js/scripts.js"></script>
	<p id="SigninState" class="hide"><%= session.getAttribute("SigninState") %></p>
	<a id="PageTransferLink" class="hide" href=""></a>
</body>
</html>