
var isReady=false;var onReadyCallbacks=[];
var isServiceReady=false;var onServiceReadyCallbacks=[];
var __uniConfig = {"pages":["pages/index","pages/work/index","pages/mine/index","pages/mine/avatar/index","pages/mine/info/index","pages/mine/info/edit","pages/mine/pwd/index","pages/mine/setting/index","pages/mine/help/index","pages/mine/about/index","pages/login","pages/common/webview/index","pages/common/textview/index","pages/work/archive/worklog/index","pages/work/archive/worklog/edit","pages/work/archive/worklog/edit-no","pages/work/archive/project/index","pages/work/archive/project/edit","pages/work/business/project/index","pages/work/business/project/edit","pages/work/business/projectTask/index","pages/work/business/projectTask/history","pages/work/daily/message/index","pages/work/daily/message/history"],"window":{"navigationBarTextStyle":"black","navigationBarTitleText":"移动端APP","navigationBarBackgroundColor":"#FFFFFF"},"tabBar":{"color":"#000000","selectedColor":"#000000","borderStyle":"white","backgroundColor":"#ffffff","list":[{"pagePath":"pages/index","iconPath":"static/images/tabbar/home.png","selectedIconPath":"static/images/tabbar/home_.png","text":"首页"},{"pagePath":"pages/work/index","iconPath":"static/images/tabbar/work.png","selectedIconPath":"static/images/tabbar/work_.png","text":"工作台"},{"pagePath":"pages/mine/index","iconPath":"static/images/tabbar/mine.png","selectedIconPath":"static/images/tabbar/mine_.png","text":"我的"}]},"nvueCompiler":"uni-app","nvueStyleCompiler":"weex","renderer":"auto","splashscreen":{"alwaysShowBeforeRender":true,"autoclose":false},"appname":"东一移动端APP","compilerVersion":"3.5.3","entryPagePath":"pages/index","networkTimeout":{"request":60000,"connectSocket":60000,"uploadFile":60000,"downloadFile":60000}};
var __uniRoutes = [{"path":"/pages/index","meta":{"isQuit":true,"isTabBar":true},"window":{"navigationBarTitleText":"首页"}},{"path":"/pages/work/index","meta":{"isQuit":true,"isTabBar":true},"window":{"navigationBarTitleText":"工作台"}},{"path":"/pages/mine/index","meta":{"isQuit":true,"isTabBar":true},"window":{"navigationBarTitleText":"我的"}},{"path":"/pages/mine/avatar/index","meta":{},"window":{"navigationBarTitleText":"修改头像"}},{"path":"/pages/mine/info/index","meta":{},"window":{"navigationBarTitleText":"个人信息"}},{"path":"/pages/mine/info/edit","meta":{},"window":{"navigationBarTitleText":"编辑资料"}},{"path":"/pages/mine/pwd/index","meta":{},"window":{"navigationBarTitleText":"修改密码"}},{"path":"/pages/mine/setting/index","meta":{},"window":{"navigationBarTitleText":"应用设置"}},{"path":"/pages/mine/help/index","meta":{},"window":{"navigationBarTitleText":"常见问题"}},{"path":"/pages/mine/about/index","meta":{},"window":{"navigationBarTitleText":"关于我们"}},{"path":"/pages/login","meta":{},"window":{"navigationBarTitleText":"登录","navigationStyle":"custom"}},{"path":"/pages/common/webview/index","meta":{},"window":{"navigationBarTitleText":"浏览网页"}},{"path":"/pages/common/textview/index","meta":{},"window":{"navigationBarTitleText":"浏览文本"}},{"path":"/pages/work/archive/worklog/index","meta":{},"window":{"navigationBarTitleText":"工作量列表"}},{"path":"/pages/work/archive/worklog/edit","meta":{},"window":{"navigationBarTitleText":"添加-计量","navigationStyle":"custom"}},{"path":"/pages/work/archive/worklog/edit-no","meta":{},"window":{"navigationBarTitleText":"添加-不计量","navigationStyle":"custom"}},{"path":"/pages/work/archive/project/index","meta":{},"window":{"navigationBarTitleText":"项目列表"}},{"path":"/pages/work/archive/project/edit","meta":{},"window":{"navigationBarTitleText":"项目编辑","navigationStyle":"custom"}},{"path":"/pages/work/business/project/index","meta":{},"window":{"navigationBarTitleText":"项目列表","navigationStyle":"custom"}},{"path":"/pages/work/business/project/edit","meta":{},"window":{"navigationBarTitleText":"项目编辑","navigationStyle":"custom"}},{"path":"/pages/work/business/projectTask/index","meta":{},"window":{"navigationBarTitleText":"任务管理","navigationStyle":"custom"}},{"path":"/pages/work/business/projectTask/history","meta":{},"window":{"navigationBarTitleText":"办理历史","navigationStyle":"custom"}},{"path":"/pages/work/daily/message/index","meta":{},"window":{"navigationBarTitleText":"消息列表","navigationStyle":"custom"}},{"path":"/pages/work/daily/message/history","meta":{},"window":{"navigationBarTitleText":"历史消息","navigationStyle":"custom"}}];
__uniConfig.onReady=function(callback){if(__uniConfig.ready){callback()}else{onReadyCallbacks.push(callback)}};Object.defineProperty(__uniConfig,"ready",{get:function(){return isReady},set:function(val){isReady=val;if(!isReady){return}const callbacks=onReadyCallbacks.slice(0);onReadyCallbacks.length=0;callbacks.forEach(function(callback){callback()})}});
__uniConfig.onServiceReady=function(callback){if(__uniConfig.serviceReady){callback()}else{onServiceReadyCallbacks.push(callback)}};Object.defineProperty(__uniConfig,"serviceReady",{get:function(){return isServiceReady},set:function(val){isServiceReady=val;if(!isServiceReady){return}const callbacks=onServiceReadyCallbacks.slice(0);onServiceReadyCallbacks.length=0;callbacks.forEach(function(callback){callback()})}});
service.register("uni-app-config",{create(a,b,c){if(!__uniConfig.viewport){var d=b.weex.config.env.scale,e=b.weex.config.env.deviceWidth,f=Math.ceil(e/d);Object.assign(__uniConfig,{viewport:f,defaultFontSize:Math.round(f/20)})}return{instance:{__uniConfig:__uniConfig,__uniRoutes:__uniRoutes,global:void 0,window:void 0,document:void 0,frames:void 0,self:void 0,location:void 0,navigator:void 0,localStorage:void 0,history:void 0,Caches:void 0,screen:void 0,alert:void 0,confirm:void 0,prompt:void 0,fetch:void 0,XMLHttpRequest:void 0,WebSocket:void 0,webkit:void 0,print:void 0}}}});
