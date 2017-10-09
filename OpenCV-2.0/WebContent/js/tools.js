/*
 Tools for display dashboard and benchmark pages
*/
var Gaussian_Data_X = [];
var Gaussian_Data_Y = [];
var Sample_Points_Num = 27;

// States
var LastTab = "";
var CurrentTab = "";
var Current_Resume_Index = 0;
var DashboardItems = [];

for (var i=0; i<=Sample_Points_Num; ++i){
  var x_val = 8*(i/Sample_Points_Num - 0.5);// Map to [-4,4]
  var value = Math.exp(-(x_val*x_val)/2);
  Gaussian_Data_X.push(x_val);
  Gaussian_Data_Y.push(value);
}

var TOGGLE_TABS = ['dashboard-wrapper','competency-wrapper','presentation-wrapper',
'format-wrapper','services-wrapper'];
var DASHBOARD_GROUPS = ['competency-score','presentation-score','format-score'];
var COMPETENCY_GROUPS = ['competency-analytical','competency-leadership','competency-teamwork',
'competency-communication','competency-initiative'];
var COMPETENCY = ['analytical','leadership','teamwork','communication','initiative'];
var FORMAT_GROUPS = ['format-pages','format-standards','format-margins',
'format-keysections','format-concise'];
var SERVICES_GROUPS = ['service-1','service-2','service-3'];
var HIGHLIGHT_COLOR = "#a4caf8";//"#3290fe";
var DEFAULT_HINTTEXT = "很抱歉，您的简历多我们来说有点困难。暂时无法分析和处理。您可以申请我们的人工服务！";

$(document).ready(function() {
  Current_Resume_Index = 0;
  $('#uploaded-resumes').carousel('pause');

  // Add animation for competency and presentation scores in dashboard page
  $("div.graph-bar-grid").on("mouseover",function(){
     $(this).siblings("li").css("background-color","#428D92");
  });
  $("div.graph-bar-grid").on("mouseout",function(){
    $(this).siblings("li").css("background-color","#59bac0");
  });

  // Add event listener for elements
  $("div.tabs").children().on("click", switchToggle);
  addClickListener();
  CurrentTab = getUrlParam("tabtype").toLowerCase();
  LastTab = CurrentTab;

  $("#preload-modal").modal("hide");
  if (CurrentTab == "dashboard"){
    var strDashboard = $("#DashboardItems").html();
    if (strDashboard.length < 10){
      DashboardItems = [];
      drawFinalScore(0);
    } else{
      DashboardItems = JSON.parse($("#DashboardItems").html());
      showDashboard(DashboardItems[0]);
    }
  }
  else if (CurrentTab == "benchmark"){
    var tabValue = parseInt(getUrlParam("tabvalue"));
    // set height of resume wrapper
    if (tabValue == 1 || tabValue == 3){
      setResumeHeight(0);
    } else if(tabValue == 2){
      setResumeHeight(1);
    }
    tabToggle(tabValue);// show the specific tab content
  }

  // For upload button
  $("#nav-uploadbtn").on("click", uploadResume);
  $("#uploadResumeLink").on("click", uploadResume);

  // For sign out
  $("#signout").on("click", signout);

  // Setting the height for left and right panels
  AdjustLeftAndRightPanelHeight();

  // Arrange the presentation positions
  ArrangePresentationPosition();
});

/**********************************************
  Draw final score
**********************************************/
function drawFinalScore(score){
  if (score == null || score == undefined){
    return;
  }

  // Color
  var blue = "rgb(54,162,235)";
  var red = "rgb(255,99,132)";
  var score_point_color = red;

  var score_x = 8*(score/100 - 0.5);// Map to [-4,4]
  var final_score_y = [];
  var tooltip_score_y = [];
  var x_index = Math.floor(score_x/(Gaussian_Data_X[1]-Gaussian_Data_X[0])+Sample_Points_Num/2);
  x_index = Math.min(x_index, Sample_Points_Num);
  x_index = Math.max(x_index, 0);

  var value = 0;
  for (var i=0; i<=Sample_Points_Num; ++i){
    if (i <= x_index){
       value = Gaussian_Data_Y[i];
       final_score_y.push(value);
    }
    else{
       final_score_y.push(NaN);
    }

    // set tooltip score y
    if (i == x_index){
      tooltip_score_y.push(Gaussian_Data_Y[i]);
    } else{  tooltip_score_y.push(NaN); }
  }

  // set tooltip callback
  var tooltip_callback = {
    title: function(tooltipItems, data){ return ""; },
    label: function(tooltipItems, data){ return ("你的分数：" + score.toString()); },
  };

  var ctx = document.getElementById("dashboard-score").getContext('2d');
  var myChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: Gaussian_Data_X,
      datasets: [{
          borderColor: red,
          pointRadius: 0,
          pointHitRadius: 0,
          borderWidth: 3,
          data: Gaussian_Data_Y,
          fill: false,
          lineTesion: 0.6,
        },
        {
          backgroundColor: blue,
          borderColor: blue,
          pointRadius: 0,
          pointHitRadius: 0,
          borderWidth: 2,
          data: final_score_y,
          fill: true,
          lineTesion: 0,
        },
        {
          label: "",
          backgroundColor: score_point_color,
          borderColor: score_point_color,
          pointRadius: 4,
          pointHitRadius: 6,
          borderWidth: 2,
          data: tooltip_score_y,
          fill: false,
          lineTesion: 0,
        },
      ]
    },
    options: {
      title:  { display: false },
      legend: { display: false },
      scales: {
        xAxes: [{
          display: true,
          ticks: { min: -4, max: 4, display: false, },
          gridLines:{ display: false, }
        }],
        yAxes: [{
          display: true,
          ticks:{ min: -0.002, max: 1.01, display: false, },
          gridLines:{ display: false, },
          scaleLabel:{ display:false, }
        }]
      },
      animation: { duration: 2000, },
      tooltips: {
        position: "nearest",
        displayColors: false,
        callbacks: tooltip_callback,
      },
      responsiveAnimationDuration: 0, // animation duration after a resize
    }
  });
}

/*********************************************
  Produces width of .barChart;
  Update the score for each item in Competency, Presentation and Format
**********************************************/
function clearScoreData(parent){
  // Clear display result, re-render
  $(parent + ' li.graph-bar').each(function() {
     $(this).data("value",0);
     $(this).css("width", "0%");
  });
}

/*********************************************
  Produces width of .barChart;
  Update the score for each item in Competency, Presentation and Format
**********************************************/
function renderScoreData(parent){
  $(parent + ' li.graph-bar').each(function() {
     var dataWidth = $(this).data('value');
     $(this).css("width", dataWidth + "%");
  });
}

function renderFormatScoreData(parent){
    $(parent + ' li.graph-check').each(function() {
     var value = $(this).data('value');
     if (value == 0){
      $(this).children("i").removeClass();
      $(this).children("i").addClass("fa fa-close background-bad");
     } else if (value == 1){
      $(this).children("i").removeClass();
      $(this).children("i").addClass("fa fa-check background-good");
     }
  });
}

/**********************************************
  Clear some tabs background
**********************************************/
function clearTabsBackground(){
  $("label.tab1").css('background-color','#e0e0e0').css('color',"#000");
  $("label.tab2").css('background-color','#e0e0e0').css('color',"#000");
  $("label.tab3").css('background-color','#e0e0e0').css('color',"#000");
}

/**********************************************
 Tab Toogle
**********************************************/
function tabToggle(tab_index){
  clearTabsBackground();
  for (var i=0; i<TOGGLE_TABS.length; ++i){
    if (i == tab_index){
      $("[data-id='"+TOGGLE_TABS[i]+"']").removeClass('hide');
      if (tab_index>0 && tab_index<4){
        $("label.tab"+tab_index.toString()).css('background-color','#5cb85c').css('color','#fff');
      }
    }
    else{
      $("[data-id='"+TOGGLE_TABS[i]+"']").addClass('hide');
    }
  }
}

/**********************************************
  Tab Switch
**********************************************/
function switchToggle(){
  var tab_index = $(this).data('value');
  var tab_type = "";

  LastTab = CurrentTab;
  if (tab_index == 0){
    tab_type = "dashboard";
  } else if (tab_index>0 && tab_index<4){
    tab_type = "benchmark";
  } else{
    tab_type = "service";
    tabToggle(tab_index);
    return;
  }

  // Switch Pages
  var user_email = $("#UserEmail").html();
  if (LastTab == "dashboard" && tab_type == "benchmark"){
    DashboardItems = JSON.parse($("#DashboardItems").html());
    if (DashboardItems[Current_Resume_Index] != null && DashboardItems[Current_Resume_Index] != undefined){
      var resumeId = DashboardItems[Current_Resume_Index].resumeId;
      var data = {"userEmail": user_email,"resumeId": resumeId, "targetPage": "benchmark", "tabValue": tab_index};
      transferPage(data);
    }
  } else if (LastTab == "benchmark" && tab_type == "dashboard"){
    var data = {"userEmail": user_email,"targetPage": "dashboard"};
    transferPage(data);
  } else{
    clearHighlight();
    // set height of resume wrapper
    if (tab_index == 1 || tab_index == 3){
      setResumeHeight(0);
    } else if(tab_index == 2){
      setResumeHeight(1);
      AdjustLeftAndRightPanelHeight(); // correct the presentation height
    }
    tabToggle(tab_index);
  }
}

function transferPage(data){
  $("#preload-modal").modal("show");

  // Using Ajax to submit the form
  $.ajax({
    url: "/WebPageTransferServlet",
    type: "POST",
    dataType: "json",
    data: data,
    success: function (status) {
      $("#preload-modal").modal("hide");
      if (status.state == true){
         $("#PageTransferLink").attr("href",status.transferPage);
         document.getElementById("PageTransferLink").click();
      }
      else{
        alert("","很抱歉，目前无法连接到服务器，请稍后再试！");
      }
    },
    error: function(){
      $("#preload-modal").modal("hide");
      alert("","很抱歉，目前无法连接到服务器，请稍后再试！");
    }
  }).done(function(res) {
  }).fail(function(res) {});
}

/**********************************************
  Add event linsener for each items
**********************************************/
function addClickListener(){
  // For Dashboard Thumbnail Items
  for (var i=0; i<$("div.thumbnail").length; ++i){
    var resume_index =  $("div.thumbnail").eq(i).data("resumeindex");
    if (resume_index == null || resume_index == undefined){
      continue;
    }
    $("div.thumbnail").eq(i).on('click',selectDashboardItem);
  }

  // For Comeptency Items
  for (var i=0; i<COMPETENCY_GROUPS.length; ++i){
    var selector = "div.group[data-id='" + COMPETENCY_GROUPS[i] + "']";
    if(($(selector).length > 0) && $(selector).children('div').hasClass('header')){
      $(selector).children('div').on('click',toggleCompetencySubItem);
      $(selector).children('div').nextAll().addClass('hide');
    }
  }

  // For Presentation Items
  var presentation_num = parseInt($("div[data-id='presentation-wrapper']").data("prenum"));
  for (var i=0; i<presentation_num; ++i){
    var selector = "div.group[data-id='presentation-" + i.toString() + "']";
    if(($(selector).length > 0) && $(selector).children('div').hasClass('header')){
      $(selector).css("padding-top",0);
      $(selector).children('div').on('click',togglePresentationSubItem);
      $(selector).children('div').nextAll().addClass('hide');
    }
  }

  // For Format Items
  for (var i=0; i<FORMAT_GROUPS.length; ++i){
    var selector = "div.group[data-id='" + FORMAT_GROUPS[i] + "']";
    if ($(selector).length > 0){
      $(selector).children('div.format-item').on('click',toggleFormatSubItem);
      $(selector).children('div.format-item').nextAll().addClass('hide');
    }
  }
}

/**********************************************
  Show dashboard subitem
**********************************************/
function showDashboardSubItem(){
  renderScoreData("div[data-id='" + DASHBOARD_GROUPS[0] + "']");// Competency
  renderScoreData("div[data-id='" + DASHBOARD_GROUPS[1] + "']");// Presentation
  renderFormatScoreData("div[data-id='" + DASHBOARD_GROUPS[2] + "']");// Format
}

/**********************************************
 Clear highlight
**********************************************/
function clearHighlight(){
  var para_selector = "";

  // Clear competecny highlight
  for (var i=0; i<COMPETENCY.length; ++i){
    for (var j=0; j<5; ++j){
      para_selector = "div[data-"+COMPETENCY[i]+"='" + j.toString() + "']";
      if ($(para_selector).length > 0){
        $(para_selector).css("background-color","transparent");
      }
    }
  }

  // Clear presentation highlight
  var presentation_label_num = parseInt($("div[data-id='presentation-wrapper']").data("prenum"));
  if ((presentation_label_num != null) && (presentation_label_num != undefined)){
    for (var i=0; i<presentation_label_num; ++i){
      para_selector = "div[data-presentation='"+i.toString()+"']";
      if ($(para_selector).length > 0){
        $(para_selector).css("background-color","transparent");
      }
    }
  }
}

/**********************************************
  Toggle competency subitem
**********************************************/
function toggleCompetencySubItem(){
  if ($(this).next().hasClass('hide')){
    hideExpandedCompetencyItems();
    $(this).nextAll().removeClass('hide');
    $(this).removeClass("addline");

    // Change the next-icon
    $(this).children("span").each(function(){
      if($(this).hasClass("next-icon")){
        $(this).removeClass("fa-chevron-down");
        $(this).addClass("fa-chevron-up");
      }
    });

    // Get competency ID
    var competency_id = $(this).parent("div.group").data("id");

    // Highlight competency texts
    highlightCompetency(competency_id,true);
  } else{
    $(this).nextAll().addClass('hide');
    $(this).addClass("addline");

    // Change the next-icon
    $(this).children("span").each(function(){
      if($(this).hasClass("next-icon")){
        $(this).removeClass("fa-chevron-up");
        $(this).addClass("fa-chevron-down");
      }
    });

    // Get competency ID
    var competency_id = $(this).parent("div.group").data("id");

    // De-Highlight competency texts
    highlightCompetency(competency_id,false);
  }
  AdjustLeftAndRightPanelHeight();
}

/**********************************************
  Toggle format subitem
**********************************************/
function toggleFormatSubItem(){
  if ($(this).next().hasClass('hide')){
    hideExpandedFormatItems();
    $(this).nextAll().removeClass('hide');
    $(this).removeClass("addline");

    // Change the next-icon
    $(this).children("span").each(function(){
      if($(this).hasClass("next-icon")){
        $(this).removeClass("fa-chevron-down");
        $(this).addClass("fa-chevron-up");
      }
    });
  } else{
    $(this).nextAll().addClass('hide');
    $(this).addClass("addline");

    // Change the next-icon
    $(this).children("span").each(function(){
      if($(this).hasClass("next-icon")){
        $(this).removeClass("fa-chevron-up");
        $(this).addClass("fa-chevron-down");
      }
    });
  }
  AdjustLeftAndRightPanelHeight();
}

/**********************************************
  Toggle presentation subitem
**********************************************/
function togglePresentationSubItem(){
  if ($(this).next().hasClass('hide')){
    hideExpandedPresentationItems();
    $(this).nextAll().removeClass('hide');
    $(this).removeClass("addline");

    // Change the next-icon
    $(this).children("span").each(function(){
      if($(this).hasClass("next-icon")){
        $(this).removeClass("fa-chevron-down");
        $(this).addClass("fa-chevron-up");
      }
    });

    // Get the presentation ID
    var pre_id = $(this).parent("div.group").data("id");
    $(this).parent("div.group").css("z-index",2);

    // Highlight presentation texts
    highlightPresentation(pre_id, true);
  } else{
    $(this).nextAll().addClass('hide');
    $(this).addClass("addline");

    // Change the next-icon
    $(this).children("span").each(function(){
      if($(this).hasClass("next-icon")){
        $(this).removeClass("fa-chevron-up");
        $(this).addClass("fa-chevron-down");
      }
    });

    // Get the presentation ID
    var pre_id = $(this).parent("div.group").data("id");

    // De-Highlight presentation texts
    highlightPresentation(pre_id,false);
  }
  AdjustLeftAndRightPanelHeight();
}

/**********************************************
  Highlight competency
**********************************************/
function highlightCompetency(competency_id,show){
  var data_id = competency_id;
  var competency_value = "";
  var index = -1;

  if (typeof data_id == "string" && data_id.length > 0){
    for (var i=0; i<COMPETENCY_GROUPS.length; ++i){
      if (data_id.localeCompare(COMPETENCY_GROUPS[i]) == 0){
        competency_value = COMPETENCY[i];
        index = i;
        break;
      }
    }
  }
  else{
    return;
  }

  // Select all competency texts and highlight them by setting its background color
  if (index >= 0){
    var para_selector = null;

    // Clear other label and improvements
    for (var i=0; i<COMPETENCY.length; ++i){
      for (var j=0; j<5; ++j){
        para_selector = "div[data-"+COMPETENCY[i]+"='" + j.toString() + "']";
        if ($(para_selector).length > 0){
          $(para_selector).css("background-color","transparent");
        }
      }
    }

    if (show){
      // Set the style of children
      for (var j=0; j<5; ++j){
        para_selector = "div[data-"+COMPETENCY[index]+"='" + j.toString() + "']";
        if ($(para_selector).length > 0){
          $(para_selector).css("background-color",HIGHLIGHT_COLOR);
        }
      }
    }    
  }
}

/**********************************************
  Highlight presentation
**********************************************/
function highlightPresentation(presentation_id,show){
  var data_id = presentation_id;
  var presentation_label = "";
  var index = -1;

  if (typeof data_id == "string" && data_id.length > 0){
    presentation_label = data_id.substr("presentation-".length);
    index = parseInt(presentation_label);
  }
  else{
    return;
  }

  // Select all presentation texts and highlight them by setting its background color
  presentation_label_num = parseInt($("div[data-id='presentation-wrapper']").data("prenum"));
  if (index >= 0){
    var para_selector = null;
    // Clear other label and improvements
    for (var i=0; i<presentation_label_num; ++i){
      if (show && i == index){
        // Set the style of children
        para_selector = "div[data-presentation='"+index.toString()+"']";
        if ($(para_selector).length > 0){
          $(para_selector).css("background-color",HIGHLIGHT_COLOR);
        }
        continue;
      }

      para_selector = "div[data-presentation='"+i.toString()+"']";
      if ($(para_selector).length > 0){
        $(para_selector).css("background-color","transparent");
      }
    }
  }
}

/**********************************************
  Arrange presentation positions
**********************************************/
function ArrangePresentationPosition(){
  var presentation_label_num = parseInt($("div[data-id='presentation-wrapper']").data("prenum"));
  var header_height = 0;
  var offset = 0;

  // Move each presentation item to the position of corresponding highlighted resume
  for (var i=0; i<presentation_label_num; ++i){
    if (i==0){
      header_height = $("div.group[data-id='presentation-0']").children("div").height();
      offset = -(80 + $("div.row.subnav").height() + 23 + header_height/2);
    }
    var para_selector = "div[data-presentation='"+i.toString()+"']";
    var presentation_selector = "div.group[data-id='presentation-" + i.toString() + "']";

    // Move the improvement to the specific position
    if ($(para_selector).length > 0 && $(presentation_selector).length > 0){
      $(presentation_selector).css("z-index",1);
      var top_pos = $(para_selector).offset().top + offset - i*header_height;
      $(presentation_selector).css("top", top_pos);
    }
  }
}

/**********************************************
  Hide competency, presentation and format items
**********************************************/
function hideExpandedCompetencyItems(){
  for (var i=0; i<COMPETENCY_GROUPS.length; ++i){
    var selector = "div.group[data-id='" + COMPETENCY_GROUPS[i] + "']";
    if(($(selector).length > 0) && $(selector).children('div').hasClass('header')){
      $(selector).children('div').first().addClass("addline");
      $(selector).children('div').nextAll().addClass('hide');
    }
  }
}

function hideExpandedPresentationItems(){
  var presentation_num = parseInt($("div[data-id='presentation-wrapper']").data("prenum"));
  for (var i=0; i<presentation_num; ++i){
    var selector = "div.group[data-id='presentation-" + i.toString() + "']";
    if(($(selector).length > 0) && $(selector).children('div').hasClass('header')){
      $(selector).children('div').first().addClass("addline");
      $(selector).children('div').nextAll().addClass('hide');
    }
  }
  ArrangePresentationPosition();
}

function hideExpandedFormatItems(){
  for (var i=0; i<FORMAT_GROUPS.length; ++i){
    var selector = "div.group[data-id='" + FORMAT_GROUPS[i] + "']";
    if ($(selector).length > 0){
      $(selector).children('div').first().addClass("addline");
      $(selector).children('div').nextAll().addClass('hide');
    }
  }
}

/**********************************************
  Display Dashboard Result
**********************************************/
function showDashboard(dashboard_item){
  if (dashboard_item == null || dashboard_item == undefined){
    return;
  }
  // get dashboard result
  var resume_id = dashboard_item.resumeId;
  var resume_filename = dashboard_item.resumeFilename;
  var resume_score = dashboard_item.resumeScore;
  var competencyRank = dashboard_item.competencyRank;
  var presentationRank = dashboard_item.presentationRank;
  var formatRank = dashboard_item.formatRank;

  // update competency, presentation and format data value
  $("#competency-analytical-score").data("value",parseInt(competencyRank[0])*20);
  $("#competency-leadership-score").data("value",parseInt(competencyRank[1])*20);
  $("#competency-teamwork-score").data("value",parseInt(competencyRank[2])*20);
  $("#competency-communication-score").data("value",parseInt(competencyRank[3])*20);
  $("#competency-initiative-score").data("value",parseInt(competencyRank[4])*20);

  $("#presentation-1-score").data("value",parseInt(presentationRank[0])*20);
  $("#presentation-2-score").data("value",parseInt(presentationRank[1])*20);
  $("#presentation-3-score").data("value",parseInt(presentationRank[2])*20);
  $("#presentation-4-score").data("value",parseInt(presentationRank[3])*20);
  $("#presentation-5-score").data("value",parseInt(presentationRank[4])*20);

  $("#format-pages-score").data("value",formatRank[0]);
  $("#format-standards-score").data("value",formatRank[1]);
  $("#format-margins-score").data("value",formatRank[2]);
  $("#format-keysections-score").data("value",formatRank[3]);
  $("#format-concise-score").data("value",formatRank[4]);

  var comment_text = "";
  if (resume_score>100){ resume_score = 100; }
  if (resume_score>=0 && resume_score<=25){
    comment_text = "表现落后。要加油努力！";
  } else if (resume_score>=25 && resume_score<=50){
     comment_text = "表现一般。要加油努力！";
  } else if (resume_score>=51 && resume_score<=75){
     comment_text = "处于中上游水平。还可以继续提升！";
  } else if (resume_score>=76 && resume_score<=100){
     comment_text = "处于上游水平。祝你一举拿下好Offer！";
  }
  var hintText = "你的简历处为" + resume_score.toString() + "/100分，" + comment_text;

  // show all results in page
  $("#dashboard-summary").text(hintText);
  drawFinalScore(resume_score);
  showDashboardSubItem();
}

/**********************************************
 Select Dashboard Result
**********************************************/
function selectDashboardItem(){
	if (DashboardItems.length > 0){
    // get the dashboard id
    var dashboard_index = parseInt($(this).data("resumeindex"));
    var dashboard_item = DashboardItems[dashboard_index];

    // show large thumbnail
    for (var i=0; i<DashboardItems.length; ++i){
      if (i == dashboard_index){
        $("#uploaded-resumes-large").find("img").eq(i).removeClass("hide");
      }
      else{
        $("#uploaded-resumes-large").find("img").eq(i).addClass("hide");
      }
    }
    // update current resume index
    Current_Resume_Index = dashboard_index;
    // show dashboard result
    showDashboard(dashboard_item);
	}
}

//----------------------------------------------
// Adjust the heights of left and right panels
//----------------------------------------------
function AdjustLeftAndRightPanelHeight(){
  $("#left-panel").css("height","auto");
  $("#right-panel").css("height","auto");

  // Check the expanded presentation height
  var isHide = $("div[data-id='presentation-wrapper']").hasClass("hide");
  var item_height = 30;
  if (isHide != null && !isHide){
     var presentation_num = parseInt($("div[data-id='presentation-wrapper']").data("prenum"));
     for (var i=0; i<presentation_num; ++i){
      var selector = "div.group[data-id='presentation-" + i.toString() + "']";
      if($(selector).length > 0){
        if(!$(selector).children('ul').first().hasClass('hide')){
          item_height = $(selector).height();
          break;
        }
      }
    }
  }
  
  var left_height = $("#left-panel").height();
  var right_height = $("#right-panel").height();
  var content_height = left_height;
  if (left_height > right_height){
    $("#right-panel").css("height", left_height);
  } else{
    $("#left-panel").css("height", right_height);
    content_height = right_height;
  }
  $("#right-panel").css("height",content_height + item_height);
}

//----------------------------------------------
// Set resume height, where type indicates the
// both scroll or independent scroll for resume and benchmark content.
// type: 0, independent scroll
// type: 1, both scroll
//----------------------------------------------
function getResumeHeight(){
  var resume_page_num = $("#page-container").data("pagenum");
  var resume_height = 0;
  for (var i=0; i<resume_page_num; ++i){
    resume_height += $("#pf"+(i+1).toString()).height();
  }
  return resume_height;
}

function setResumeHeight(type){
  var resume_page_num = $("#page-container").data("pagenum");
  var resume_height = 0;
  for (var i=0; i<resume_page_num; ++i){
    resume_height += $("#pf"+(i+1).toString()).height();
  }

  if (type == 0){
    // sunstract the height of main navigation and sub-nanvigation
    var screen_display_height = $(window).height() - 80 - $("div.row.subnav").height();
    $("#page-container").css("height", screen_display_height);
  } else if (type == 1){
    $("#page-container").css("height", resume_height);
  }
  $("#page-container").css("width", $("#resume-wrapper").width()+20); // hide y scroll
}

//----------------------------------------------
// Get sign in state
//----------------------------------------------
function getSigninState(){
  // Get SigninState
  var strState = $("#UserEmail").html();
  if (strState != null && strState.length > 0){
    return true;
  } else{
    return false;
  }
}

function uploadResume(){
  // Check whether the user has signed in
  var UserSignin = getSigninState();
  if (UserSignin){
    document.getElementById("uploadPage").click();
  }
  else{
    alert(null, "您还没有登录OpenCV，无法上传文件！", null, {type: "error"});
  }
}

//----------------------------------------------
// Sign out
//----------------------------------------------
function signout(){
  $("#UserEmail").html("");
  $("#username").addClass("hide");
  $.ajax({
    url: "/SignOutServlet",
    type: "POST",
    dataType: "json",
    data: "",
    success: function (status) {
      alert("您已登出OpenCV~");
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