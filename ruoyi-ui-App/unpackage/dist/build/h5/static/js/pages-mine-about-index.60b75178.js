(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["pages-mine-about-index"],{"0270":function(t,n,i){"use strict";i.r(n);var e=i("a504"),a=i("2543");for(var s in a)"default"!==s&&function(t){i.d(n,t,(function(){return a[t]}))}(s);i("9afe");var r,o=i("f0c5"),u=Object(o["a"])(a["default"],e["b"],e["c"],!1,null,"24b1c60f",null,!1,e["a"],r);n["default"]=u.exports},1551:function(t,n,i){var e=i("b267");"string"===typeof e&&(e=[[t.i,e,""]]),e.locals&&(t.exports=e.locals);var a=i("4f06").default;a("7aa42960",e,!0,{sourceMap:!1,shadowMode:!1})},2543:function(t,n,i){"use strict";i.r(n);var e=i("6c12"),a=i.n(e);for(var s in e)"default"!==s&&function(t){i.d(n,t,(function(){return e[t]}))}(s);n["default"]=a.a},"40f4":function(t,n,i){"use strict";Object.defineProperty(n,"__esModule",{value:!0}),n.default=void 0;var e={data:function(){return{version:getApp().globalData.config.appInfo.version}}};n.default=e},"6c12":function(t,n,i){"use strict";i("d3b7"),i("25f0"),Object.defineProperty(n,"__esModule",{value:!0}),n.default=void 0;var e={name:"UniTitle",props:{type:{type:String,default:""},title:{type:String,default:""},align:{type:String,default:"left"},color:{type:String,default:"#333333"},stat:{type:[Boolean,String],default:""}},data:function(){return{}},computed:{textAlign:function(){var t="center";switch(this.align){case"left":t="flex-start";break;case"center":t="center";break;case"right":t="flex-end";break}return t}},watch:{title:function(t){this.isOpenStat()&&uni.report&&uni.report("title",this.title)}},mounted:function(){this.isOpenStat()&&uni.report&&uni.report("title",this.title)},methods:{isOpenStat:function(){""===this.stat&&(this.isStat=!1);var t="boolean"===typeof this.stat&&this.stat||"string"===typeof this.stat&&""!==this.stat;return""===this.type&&(this.isStat=!0,"false"===this.stat.toString()&&(this.isStat=!1)),""!==this.type&&(this.isStat=!0,this.isStat=!!t),this.isStat}}};n.default=e},7869:function(t,n,i){var e=i("d914");"string"===typeof e&&(e=[[t.i,e,""]]),e.locals&&(t.exports=e.locals);var a=i("4f06").default;a("394704a0",e,!0,{sourceMap:!1,shadowMode:!1})},"9afe":function(t,n,i){"use strict";var e=i("1551"),a=i.n(e);a.a},a504:function(t,n,i){"use strict";var e;i.d(n,"b",(function(){return a})),i.d(n,"c",(function(){return s})),i.d(n,"a",(function(){return e}));var a=function(){var t=this,n=t.$createElement,i=t._self._c||n;return i("v-uni-view",{staticClass:"uni-title__box",style:{"align-items":t.textAlign}},[i("v-uni-text",{staticClass:"uni-title__base",class:["uni-"+t.type],style:{color:t.color}},[t._v(t._s(t.title))])],1)},s=[]},ac2b:function(t,n,i){"use strict";var e=i("7869"),a=i.n(e);a.a},ac30:function(t,n,i){"use strict";i.r(n);var e=i("f0b5"),a=i("d2d7");for(var s in a)"default"!==s&&function(t){i.d(n,t,(function(){return a[t]}))}(s);i("ac2b");var r,o=i("f0c5"),u=Object(o["a"])(a["default"],e["b"],e["c"],!1,null,"285b465a",null,!1,e["a"],r);n["default"]=u.exports},b267:function(t,n,i){var e=i("24fb");n=e(!1),n.push([t.i,"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n/* .uni-title {\n\n} */.uni-title__box[data-v-24b1c60f]{\ndisplay:flex;\nflex-direction:column;align-items:flex-start;justify-content:center;padding:8px 0;flex:1}.uni-title__base[data-v-24b1c60f]{font-size:15px;color:#333;font-weight:500}.uni-h1[data-v-24b1c60f]{font-size:20px;color:#333;font-weight:700}.uni-h2[data-v-24b1c60f]{font-size:18px;color:#333;font-weight:700}.uni-h3[data-v-24b1c60f]{font-size:16px;color:#333;font-weight:700\n\t/* font-weight: 400; */}.uni-h4[data-v-24b1c60f]{font-size:14px;color:#333;font-weight:700\n\t/* font-weight: 300; */}.uni-h5[data-v-24b1c60f]{font-size:12px;color:#333;font-weight:700\n\t/* font-weight: 200; */}",""]),t.exports=n},d2d7:function(t,n,i){"use strict";i.r(n);var e=i("40f4"),a=i.n(e);for(var s in e)"default"!==s&&function(t){i.d(n,t,(function(){return e[t]}))}(s);n["default"]=a.a},d914:function(t,n,i){var e=i("24fb");n=e(!1),n.push([t.i,'@charset "UTF-8";\r\n/**\r\n * uni-app内置的常用样式变量\r\n */\r\n/* 行为相关颜色 */\r\n/* 文字基本颜色 */\r\n/* 背景颜色 */\r\n/* 边框颜色 */\r\n/* 尺寸变量 */\r\n/* 文字尺寸 */\r\n/* 图片尺寸 */\r\n/* Border Radius */\r\n/* 水平间距 */\r\n/* 垂直间距 */\r\n/* 透明度 */\r\n/* 文章场景相关 */uni-page-body[data-v-285b465a]{background-color:#f8f8f8}.copyright[data-v-285b465a]{margin-top:%?50?%;text-align:center;line-height:%?60?%;color:#999}.header-section[data-v-285b465a]{display:flex;padding:%?30?% 0 0;flex-direction:column;align-items:center}body.?%PAGE?%[data-v-285b465a]{background-color:#f8f8f8}',""]),t.exports=n},f0b5:function(t,n,i){"use strict";i.d(n,"b",(function(){return a})),i.d(n,"c",(function(){return s})),i.d(n,"a",(function(){return e}));var e={uniTitle:i("0270").default},a=function(){var t=this,n=t.$createElement,i=t._self._c||n;return i("v-uni-view",{staticClass:"about-container"},[i("v-uni-view",{staticClass:"header-section text-center"},[i("v-uni-image",{staticStyle:{width:"150rpx",height:"150rpx"},attrs:{src:"/static/logo200.png",mode:"widthFix"}}),i("uni-title",{attrs:{type:"h2",title:"东一移动端"}})],1),i("v-uni-view",{staticClass:"content-section"},[i("v-uni-view",{staticClass:"menu-list"},[i("v-uni-view",{staticClass:"list-cell list-cell-arrow"},[i("v-uni-view",{staticClass:"menu-item-box"},[i("v-uni-view",[t._v("版本信息")]),i("v-uni-view",{staticClass:"text-right"},[t._v("v"+t._s(t.version))])],1)],1),i("v-uni-view",{staticClass:"list-cell list-cell-arrow"},[i("v-uni-view",{staticClass:"menu-item-box"},[i("v-uni-view",[t._v("官方邮箱")]),i("v-uni-view",{staticClass:"text-right"},[t._v("dongyi_xx@163.com")])],1)],1),i("v-uni-view",{staticClass:"list-cell list-cell-arrow"},[i("v-uni-view",{staticClass:"menu-item-box"},[i("v-uni-view",[t._v("联系热线")]),i("v-uni-view",{staticClass:"text-right"},[t._v("021-38762820")])],1)],1),i("v-uni-view",{staticClass:"list-cell list-cell-arrow"},[i("v-uni-view",{staticClass:"menu-item-box"},[i("v-uni-view",[t._v("公司地址")]),i("v-uni-view",{staticClass:"text-right"},[t._v("浦东新区金高路2388号105室")])],1)],1)],1)],1),i("v-uni-view",{staticClass:"copyright"},[i("v-uni-view",[t._v("Copyright © 2022 dongyi All Rights Reserved.")])],1)],1)},s=[]}}]);