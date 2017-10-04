'use strict';
(function(){function b(a,b,e,f){var c=(a.className||"").split(/\s+/g);""===c[0]&&c.shift();var d=c.indexOf(b);0>d&&e&&c.push(b);0<=d&&f&&c.splice(d,1);a.className=c.join(" ");return 0<=d}if(!("classList"in document.createElement("div"))){var e={add:function(a){b(this.element,a,!0,!1)},contains:function(a){return b(this.element,a,!1,!1)},remove:function(a){b(this.element,a,!1,!0)},toggle:function(a){b(this.element,a,!0,!0)}};Object.defineProperty(HTMLElement.prototype,"classList",{get:function(){if(this._classList)return this._classList;
var a=Object.create(e,{element:{value:this,writable:!1,enumerable:!0}});Object.defineProperty(this,"_classList",{value:a,writable:!1,enumerable:!1});return a},enumerable:!0})}})();

var CSS_CLASS_NAMES = {
  page_frame       : 'pf',
  page_content_box : 'pc',
  page_data        : 'pi',
  background_image : 'bi',
  link             : 'l',
  input_radio      : 'ir',
  __dummy__        : 'no comma'
};

var DEFAULT_CONFIG = {
  // id of the element to put the pages in
  'container_id' : 'page-container',
  // How many page shall we preload that are below the last visible page
  'preload_pages' : 3,
  // how many ms should we wait before actually rendering the pages and after a scroll event
  'render_timeout' : 100,
  // zoom ratio step for each zoom in/out event
  'scale_step' : 0.9,
  // register global key handler, allowing navigation by keyboard
  'key_handler' : true,
  // register hashchange handler, navigate to the location specified by the hash
  'hashchange_handler' : true,
  // register view history handler, allowing going back to the previous location
  'view_history_handler' : true,

  '__dummy__'        : 'no comma'
};

/** @const */
var EPS = 1e-6;

/************************************/
function invert(ctm) {
  var det = ctm[0] * ctm[3] - ctm[1] * ctm[2];
  return [ ctm[3] / det
          ,-ctm[1] / det
          ,-ctm[2] / det
          ,ctm[0] / det
          ,(ctm[2] * ctm[5] - ctm[3] * ctm[4]) / det
          ,(ctm[1] * ctm[4] - ctm[0] * ctm[5]) / det
        ];
};
/**
 * @param{Array.<number>} ctm
 * @param{Array.<number>} pos
 */
function transform(ctm, pos) {
  return [ctm[0] * pos[0] + ctm[2] * pos[1] + ctm[4]
         ,ctm[1] * pos[0] + ctm[3] * pos[1] + ctm[5]];
};

/**
 * @param{Element} ele
 */
function get_page_number(ele) {
  return parseInt(ele.getAttribute('data-page-no'), 16);
};

/**
 * @param{NodeList} eles
 */
function disable_dragstart(eles) {
  for (var i = 0, l = eles.length; i < l; ++i) {
    eles[i].addEventListener('dragstart', function() {
      return false;
    }, false);
  }
};

/**
 * @param{...Object} var_args
 */
function clone_and_extend_objs(var_args) {
  var result_obj = {};
  for (var i = 0, l = arguments.length; i < l; ++i) {
    var cur_obj = arguments[i];
    for (var k in cur_obj) {
      if (cur_obj.hasOwnProperty(k)) {
        result_obj[k] = cur_obj[k];
      }
    }
  }
  return result_obj;
};

/** 
 * @constructor 
 * @param{Element} page The element for the page
 */
function Page(page) {
  if (!page) return;

  this.loaded = false;
  this.shown = false;
  this.page = page; // page frame element

  this.num = get_page_number(page);

  // page size
  // Need to make rescale work when page_content_box is not loaded, yet
  this.original_height = page.clientHeight;     
  this.original_width = page.clientWidth;

  // content box
  var content_box = page.getElementsByClassName(CSS_CLASS_NAMES.page_content_box)[0];

  // if page is loaded
  if (content_box) {
    this.content_box = content_box;
    /*
     * scale ratios
     *
     * original_scale : the first one
     * cur_scale : currently using
     */
    this.original_scale = this.cur_scale = this.original_height / content_box.clientHeight;
    this.page_data = JSON.parse(page.getElementsByClassName(CSS_CLASS_NAMES.page_data)[0].getAttribute('data-data'));

    this.ctm = this.page_data['ctm'];
    this.ictm = invert(this.ctm);

    this.loaded = true;
  }
};
Page.prototype = {
  /* hide & show are for contents, the page frame is still there */
  hide : function(){
    if (this.loaded && this.shown) {
      this.content_box.classList.remove('opened');
      this.shown = false;
    }
  },
  show : function(){
    if (this.loaded && !this.shown) {
      this.content_box.classList.add('opened');
      this.shown = true;
    }
  },
  /**
   * @param{number} ratio
   */
  rescale : function(ratio) {
    if (ratio === 0) {
      // reset scale
      this.cur_scale = this.original_scale;
    } else {
      this.cur_scale = ratio;
    }

    // scale the content box
    if (this.loaded) {
      var cbs = this.content_box.style;
      cbs.msTransform = cbs.webkitTransform = cbs.transform = 'scale('+this.cur_scale.toFixed(3)+')';
    }

    // stretch the page frame to hold the place
    {
      var ps = this.page.style;
      ps.height = (this.original_height * this.cur_scale) + 'px';
      ps.width = (this.original_width * this.cur_scale) + 'px';
    }
  },
  /*
   * return the coordinate of the top-left corner of container
   * in our coordinate system
   * assuming that p.parentNode === p.offsetParent
   */
  view_position : function () {
    var p = this.page;
    var c = p.parentNode;
    return [c.scrollLeft - p.offsetLeft - p.clientLeft
           ,c.scrollTop - p.offsetTop - p.clientTop];
  },
  height : function () {
    return this.page.clientHeight;
  },
  width : function () {
    return this.page.clientWidth;
  }
};

/** 
 * @constructor
 * @param{Object=} config
 */
function Viewer(config) {
  this.config = clone_and_extend_objs(DEFAULT_CONFIG, (arguments.length > 0 ? config : {}));
  var self = this;
  this.container = document.getElementById(this.config['container_id']);
};

Viewer.prototype = {
  scale : 1,
  cur_page_idx : 0,
  first_page_idx : 0,

  find_pages : function() {
    var new_pages = [];
    var new_page_map = {};
    var nodes = this.container.childNodes;
    for (var i = 0, l = nodes.length; i < l; ++i) {
      var cur_node = nodes[i];
      if ((cur_node.nodeType === Node.ELEMENT_NODE)
          && cur_node.classList.contains(CSS_CLASS_NAMES.page_frame)) {
        var p = new Page(cur_node);
        new_pages.push(p);
        new_page_map[p.num] = new_pages.length - 1;
      }
    }
    this.pages = new_pages;
    this.page_map = new_page_map;
  },

  /**
   * @param{number} idx
   * @param{number=} pages_to_preload
   * @param{function(Page)=} callback
   *
   * TODO: remove callback -> promise ?
   */
  load_page : function(idx, pages_to_preload, callback) {
    var pages = this.pages;
    if (idx >= pages.length)
      return;  // Page does not exist

    var cur_page = pages[idx];
    if (cur_page.loaded)
      return;  // Page is loaded

    if (this.pages_loading[idx])
      return;  // Page is already loading

    var cur_page_ele = cur_page.page;
    var url = cur_page_ele.getAttribute('data-page-url');
    if (url) {
      this.pages_loading[idx] = true;       // set semaphore

      // add a copy of the loading indicator if not already present
      var new_loading_indicator = cur_page_ele.getElementsByClassName(this.config['loading_indicator_cls'])[0];
      if (typeof new_loading_indicator === 'undefined'){
        new_loading_indicator = this.loading_indicator.cloneNode(true);
        new_loading_indicator.classList.add('active');
        cur_page_ele.appendChild(new_loading_indicator);
      }

      // load data
      {
        var self = this;
        var _idx = idx;
        var xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.onload = function(){
          if (xhr.status === 200 || xhr.status === 0) {
            // find the page element in the data
            var div = document.createElement('div');
            div.innerHTML = xhr.responseText;

            var new_page = null;
            var nodes = div.childNodes;
            for (var i = 0, l = nodes.length; i < l; ++i) {
              var cur_node = nodes[i];
              if ((cur_node.nodeType === Node.ELEMENT_NODE)
                  && cur_node.classList.contains(CSS_CLASS_NAMES.page_frame)) {
                new_page = cur_node;
                break;
              }
            }

            // replace the old page with loaded data
            // the loading indicator on this page should also be destroyed
            var p = self.pages[_idx];
            self.container.replaceChild(new_page, p.page);
            p = new Page(new_page);
            self.pages[_idx] = p;

            p.hide();
            p.rescale(self.scale);

            // disable background image dragging
            disable_dragstart(new_page.getElementsByClassName(CSS_CLASS_NAMES.background_image));

            self.schedule_render(false);

            if (callback){ callback(p); }
          }

          // Reset loading token
          delete self.pages_loading[_idx];
        };
        xhr.send(null);
      }
    }
    // Concurrent prefetch of the next pages
    if (pages_to_preload === undefined)
      pages_to_preload = this.config['preload_pages'];

    if (--pages_to_preload > 0) {
      var self = this;
      setTimeout(function() {
        self.load_page(idx+1, pages_to_preload);
      },0);
    }
  },

  /*
   * Hide all pages that have no 'opened' class
   * The 'opened' class will be added to visible pages by JavaScript
   * We cannot add this in the default CSS because JavaScript may be disabled
   */
  pre_hide_pages : function() {
    /* pages might have not been loaded yet, so add a CSS rule */
    var s = '@media screen{.'+CSS_CLASS_NAMES.page_content_box+'{display:none;}}';
    var n = document.createElement('style');
    if (n.styleSheet) {
      n.styleSheet.cssText = s;
    } else {
      n.appendChild(document.createTextNode(s));
    }
    document.head.appendChild(n);
  },

  /*
   * show visible pages and hide invisible pages
   */
  render : function () {
    var container = this.container;
    /* 
     * show the pages that are 'nearly' visible -- it's right above or below the container
     *
     * all the y values are in the all-page element's coordinate system
     */
    var container_min_y = container.scrollTop;
    var container_height = container.clientHeight;
    var container_max_y = container_min_y + container_height;
    var visible_min_y = container_min_y - container_height;
    var visible_max_y = container_max_y + container_height;

    var cur_page_fully_visible = false;
    var cur_page_idx = this.cur_page_idx;
    var max_visible_page_idx = cur_page_idx;
    var max_visible_ratio = 0.0;

    var pl = this.pages;
    for (var i = 0, l = pl.length; i < l; ++i) {
      var cur_page = pl[i];
      var cur_page_ele = cur_page.page;
      var page_min_y = cur_page_ele.offsetTop + cur_page_ele.clientTop;
      var page_height = cur_page_ele.clientHeight;
      var page_max_y = page_min_y + page_height;
      if ((page_min_y <= visible_max_y) && (page_max_y >= visible_min_y))
      {
        // cur_page is 'nearly' visible, show it or load it
        if (cur_page.loaded) {
          cur_page.show();
        } else {
          this.load_page(i);
        }
      } else {
        cur_page.hide();
      }
    }
  },
  
  /**
   * @param{boolean} renew renew the existing schedule instead of using the old one
   */
  schedule_render : function(renew) {
    if (this.render_timer !== undefined) {
      if (!renew) return;
      clearTimeout(this.render_timer);
    }

    var self = this;
    this.render_timer = setTimeout(function () {
      /*
       * render() may trigger load_page(), which may in turn trigger another render()
       * so delete render_timer first
       */
      delete self.render_timer;
      self.render();
    }, this.config['render_timeout']);
  },

  /**
   * @param{number} ratio
   * @param{boolean} is_relative
   * @param{Array.<number>=} fixed_point preserve the position (relative to the top-left corner of the viewer) after rescaling
   */
  rescale : function (ratio, is_relative, fixed_point) {
    var old_scale = this.scale;
    var new_scale = old_scale;
    // set new scale
    if (ratio === 0) {
      new_scale = 1;
      is_relative = false;
    } else if (is_relative)
      new_scale *= ratio;
    else
      new_scale = ratio;

    this.scale = new_scale;

    if (!fixed_point)
      fixed_point = [0,0];

    // translate fixed_point to the coordinate system of all pages
    var container = this.container;
    fixed_point[0] += container.scrollLeft;
    fixed_point[1] += container.scrollTop;

    // find the visible page that contains the fixed point
    // if the fixed point lies between two pages (including their borders), it's contained in the first one
    var pl = this.pages;
    var pl_len = pl.length;
    for (var i = this.first_page_idx; i < pl_len; ++i) {
      var p = pl[i].page;
      if (p.offsetTop + p.clientTop >= fixed_point[1])
        break;
    }
    var fixed_point_page_idx = i - 1;

    // determine the new scroll position
    // each-value consists of two parts, one inside the page, which is affected by rescaling,
    // the other is outside, (e.g. borders and margins), which is not affected

    // if the fixed_point is above the first page, use the first page as the reference
    if (fixed_point_page_idx < 0) 
      fixed_point_page_idx = 0;

    var fp_p = pl[fixed_point_page_idx].page;
    var fp_p_width = fp_p.clientWidth;
    var fp_p_height = fp_p.clientHeight;

    var fp_x_ref = fp_p.offsetLeft + fp_p.clientLeft;
    var fp_x_inside = fixed_point[0] - fp_x_ref;
    if (fp_x_inside < 0)
      fp_x_inside = 0;
    else if (fp_x_inside > fp_p_width)
      fp_x_inside = fp_p_width;

    var fp_y_ref = fp_p.offsetTop + fp_p.clientTop;
    var fp_y_inside = fixed_point[1] - fp_y_ref;
    if (fp_y_inside < 0)
      fp_y_inside = 0;
    else if (fp_y_inside > fp_p_height)
      fp_y_inside = fp_p_height;

    // Rescale pages
    for (var i = 0; i < pl_len; ++i) 
        pl[i].rescale(new_scale);  

    // Correct container scroll to keep view aligned while zooming
    container.scrollLeft += fp_x_inside / old_scale * new_scale + fp_p.offsetLeft + fp_p.clientLeft - fp_x_inside - fp_x_ref;
    container.scrollTop += fp_y_inside / old_scale * new_scale + fp_p.offsetTop + fp_p.clientTop - fp_y_inside - fp_y_ref;

    // some pages' visibility may be toggled, wait for next render()
    // renew old schedules since rescale() may be called frequently
    this.schedule_render(true);
  },

  fit_width : function () {
    var page_idx = this.cur_page_idx;
    this.rescale(this.container.clientWidth / this.pages[page_idx].width(), true);
    this.scroll_to(page_idx);
  },

  fit_height : function () {
    var page_idx = this.cur_page_idx;
    this.rescale(this.container.clientHeight / this.pages[page_idx].height(), true);
    this.scroll_to(page_idx);
  },

  /**
   * @param{number} page_idx
   * @param{Array.<number>=} pos [x,y] where (0,0) is the top-left corner
   */
  scroll_to : function(page_idx, pos) {
    var pl = this.pages;
    if ((page_idx < 0) || (page_idx >= pl.length)) return;
    var target_page = pl[page_idx];
    var cur_target_pos = target_page.view_position();

    if (pos === undefined)
      pos = [0,0];

    var container = this.container;
    container.scrollLeft += pos[0] - cur_target_pos[0];
    container.scrollTop += pos[1] - cur_target_pos[1];
  }
};