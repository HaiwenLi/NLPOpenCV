<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh-CN">
<head>
<title>OpenCV 简历评测与提升</title>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" href="images/icon/logo.ico" type="image/x-icon>" />

<link rel="stylesheet" href="css/bootstrap.min.css" />
<link rel="stylesheet" href="css/font-awesome.min.css" />
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/signin.css" />
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
	    zhuge.track('index-page');
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
				<p style="font-size: 26px; font-weight: 600;">OpenCV</p>
				<p>人工智能&nbsp;|&nbsp;测简历&nbsp;改简历</p>
			</a>
		</div>
		<div id="navbar" class="navbar-collapse collapse">
			<ul class="nav navbar-nav navbar-right">
				<li>
					<button id="nav-uploadbtn" class="nav-uploadbtn" type="button">一键免费评测</button>
				</li>
				<li><a href="team.jsp" target="_blank">我们</a></li>
				<li><a class="nav-signin" style="margin-right: 0">登录</a></li>
				<li><a class="nav-logout hide" href="index.jsp">退出</a></li>
			</ul>
		</div>
	</div>
	</nav> </header>

	<!-- signin modal -->
	<div class="modal fade" id="signin-modal" tabindex="-1" role="dialog"
		aria-labelledby="signin" aria-hidden="true">
		<div class="modal-dialog modal-narrow">
			<div class="modal-content">
				<div class="modal-body">
					<div class="login-wrapper">
						<div class="box">
							<div class="content-wrap">
								<h6 class="signin-header">登录</h6>
								<p class="signin-welcome hide">请输入您的电子邮件地址，方便我们向您及时反馈</p>
								<input id="signin-email" class="form-control" type="text"
									placeholder="电子邮件地址">
								<p id="signin-email-errorbar"
									class="validation-error-class hide">邮箱格式不正确，请重新输入</p>
								<input id="signin-passwd" class="form-control hide"
									type="password" placeholder="请输入您密码">
								<p id="signin-passwd-errorbar"
									class="validation-error-class hide">电子邮件地址或密码错误</p>
								<a id="signin-reset-passwd" class="hide"
									href="forgot-password.jsp">忘记密码？</a>
							</div>
							<div class="action">
								<button id="signin-submit" type="button"
									class="btn btn-default signup" name="next">下一步</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- signup modal -->
	<div class="modal fade" id="signup-modal" tabindex="-1" role="dialog"
		aria-labelledby="signup" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-body">
					<div class="login-wrapper">
						<div class="box">
							<div class="content-wrap">
								<h6>欢迎使用OpenCV</h6>
								<p class="signup-welcome">
									你好!</br>请输入以下内容，即将开始使用OpenCV
								</p>
								<input id="signup-name" class="form-control" type="text"
									placeholder="用户名">
								<p id="signup-name-errorbar" class="validation-error-class hide">用户名为6-20位的英文字母、数字</p>
								<input id="signup-passwd" class="form-control" type="password"
									placeholder="创建密码">
								<p id="signup-passwd-errorbar"
									class="validation-error-class hide">密码为8-20位的英文字母、数字或符号（点、下划线）</p>
								<input id="signup-passwd-confirm" class="form-control"
									type="password" placeholder="确认密码">
								<p id="signup-passwd-confirm-errorbar"
									class="validation-error-class hide">两次密码不一致</p>
							</div>
							<div class="action">
								<button id="signup-submit" type="button"
									class="btn btn-default signup" href="">开始使用</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- main content -->
	<div class="index-main-content">
		<!-- products presentation-->
		<div class="container">
			<div class="product-pre">
				<h3>
					全国第一款</br> 人工智能&nbsp;<b>“</b>测简历、改简历<b>”</b>&nbsp;工具</br> 提升求职成功率
				</h3>
				<div
					class="col-sm-10 col-md-10 col-sm-offset-1 col-md-offset-1 product-operations">
					<div class="col-xs-4 col-sm-4 col-md-4 operation">
						<img src="images/icon/upload.png"></img>
						<h3>上传</h3>
						<p>
							一键上传</br> Word版简历</br> 超简单！
						</p>
					</div>

					<div class="col-xs-4 col-sm-4 col-md-4 operation">
						<img src="images/icon/evaluate.png"></img>
						<h3>评测</h3>
						<p>
							立刻获取</br> 简历评测综合打分</br> 及各分项打分
						</p>
					</div>

					<div class="col-xs-4 col-sm-4 col-md-4 operation">
						<img src="images/icon/improve.png"></img>
						<h3>提升</h3>
						<p>
							逐行细化修改意见</br> 提升你的</br> 求职成功率！
						</p>
					</div>
					<div class="clearfix"></div>
				</div>

				<div class="col-xs-12 col-sm-10 col-xs-offset-0 col-sm-offset-1">
					<img id="laptop" class="laptop" src="images/pre/content-1.png"></img>
				</div>
			</div>
		</div>

		<!-- Tools -->
		<div class="tools">
			<div class="container">
				<h3>
					就业党、留学党</br> 党党必备
				</h3>
				<div class="row" style="padding-top: 30px">
					<div class="col-xs-3 col-sm-2 col-xs-offset-0 col-sm-offset-1">
						<div class="img-wrapper">
							<img src="images/icon/Top50.png"></img>
						</div>
						<p>
							匹配全球</br> 前50名校简历要求
						</p>
					</div>
					<div class="col-xs-3 col-sm-3">
						<div class="img-wrapper">
							<img src="images/icon/Top500.png"></img>
						</div>
						<p>
							通过全球</br> 500强招聘简历测试
						</p>
					</div>
					<div class="col-xs-3 col-sm-3 separator">
						<div class="img-wrapper">
							<img src="images/icon/interview-pass.png"></img>
						</div>
						<div class="result">
							<p>
								<b>67%</b></br> 提高进入面试概率
							</p>
						</div>
					</div>
					<div class="col-sm-2 col-md-2">
						<div class="img-wrapper">
							<img src="images/icon/offer.png"></img>
						</div>
						<div class="result">
							<p>
								<b>52%</b></br> 提高收获Offer概率
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- products principles -->
		<div class="product-principles">
			<div class="container">
				<h3>
					算法与数据支持</br> 比中介改简历更智能
				</h3>
				<div class="col-xs-12 col-sm-12">
					<div
						class="col-xs-10 col-sm-4 col-xs-offset-1 col-md-offset-0 product">
						<div class="block">
							<img src="images/icon/AI.png" />
							<h4>
								人工智能</br>Artificial Intelligence
							</h4>
							<p>应用自然语言处理 (Natural Language Processing) 和机器学习 (Machine
								Learning)。OpenCV进行简历语义解读，识别出你的领域、优势、能力，通过尖端算法产生简历提升的个性化意见。</p>
						</div>
					</div>

					<div
						class="col-xs-10 col-sm-4 col-xs-offset-1 col-md-offset-0 product">
						<div class="block">
							<img src="images/icon/resume_db.png" />
							<h4>
								简历数据库</br>Resume Dataset
							</h4>
							<p>建立名校名企简历数据库。OpenCV对简历文句进行上百项参数的分类、标记和分析，帮助用户匹配与自己相关度最高的范本简历，产出细化到句的提升方案。</p>
						</div>
					</div>

					<div
						class="col-xs-10 col-sm-4 col-xs-offset-1 col-md-offset-0 product">
						<div class="block">
							<img src="images/icon/benchmark.png" />
							<h4>
								标杆分析</br>Benchmark Analysis
							</h4>
							<p>横向进行标杆分析。OpenCV采取浮动式评测打分，周期性回顾个体简历与全体简历在多项维度上的对比。使用的用户越多，标杆整体用户的评测越精准，越能够帮助用户了解自身所处的水平及待提高的能力项。</p>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- Customer feedbacks and logos -->
		<div class="customers-feedback">
			<div class="container">
				<h3>我们都在使用OpenCV</h3>
				<div class="row">
					<div class="col-sm-12" style="padding-top: 25px;">
						<div
							class="col-xs-4 col-sm-3 col-sm-offset-1 customers-feedback-userinfo">
							<img class="customer-pic" src="images/zhangxiao.jpg"></img>
							<div class="customer-info">
								<span class="name">张霄</span></br> <span class="education">北大本/斯坦福博</span>
							</div>
						</div>
						<div class="col-xs-8 col-sm-8 customers-feedback-content">
							<p>
								“OpenCV是我用过的最好的简历修改产品。我在OpenCV的帮助下申请并进入了斯坦福的PHD项目。这个暑假我申请实习，我又通过OpenCV完善了简历，拿到了5家公司的面试和2个Offer。但是最后我选择加入OpenCV实习。我喜欢这里的极客精神和工程师精神。”
							</p>
						</div>
					</div>
				</div>
				<div class="clearfix"></div>
				<div class="col-xs-12 col-sm-12">
					<img class="customer-logos" src="images/customer-logos.png"></img>
				</div>
			</div>
		</div>

		<!-- Welcome to use OpenCV-->
		<div class="container">
			<div class="welcome">
				<a id="uploadPage" class="hide" href="upload.jsp" target="_blank"></a>
				<p class="banner">立刻使用OpenCV，提升第一竞争力！</p>
				<div class="upload-block">
					<button id="uploadbtn" class="uploadbtn" data-toggle="modal">一键免费评测</button>
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
	<script type="text/javascript" src="js/index.js"></script>
	<p id="SigninState" class="hide"><%= session.getAttribute("SigninState") %></p>
</body>
</html>