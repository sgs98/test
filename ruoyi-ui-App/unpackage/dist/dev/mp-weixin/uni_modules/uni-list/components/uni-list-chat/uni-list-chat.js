(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["uni_modules/uni-list/components/uni-list-chat/uni-list-chat"],{249:function(t,e,n){"use strict";n.r(e);var i=n(250),r=n(252);for(var a in r)"default"!==a&&function(t){n.d(e,t,(function(){return r[t]}))}(a);n(254);var u,o=n(24),s=Object(o["default"])(r["default"],i["render"],i["staticRenderFns"],!1,null,null,null,!1,i["components"],u);s.options.__file="uni_modules/uni-list/components/uni-list-chat/uni-list-chat.vue",e["default"]=s.exports},250:function(t,e,n){"use strict";n.r(e);var i=n(251);n.d(e,"render",(function(){return i["render"]})),n.d(e,"staticRenderFns",(function(){return i["staticRenderFns"]})),n.d(e,"recyclableRender",(function(){return i["recyclableRender"]})),n.d(e,"components",(function(){return i["components"]}))},251:function(t,e,n){"use strict";var i;n.r(e),n.d(e,"render",(function(){return r})),n.d(e,"staticRenderFns",(function(){return u})),n.d(e,"recyclableRender",(function(){return a})),n.d(e,"components",(function(){return i}));var r=function(){var t=this,e=t.$createElement;t._self._c},a=!1,u=[];r._withStripped=!0},252:function(t,e,n){"use strict";n.r(e);var i=n(253),r=n.n(i);for(var a in i)"default"!==a&&function(t){n.d(e,t,(function(){return i[t]}))}(a);e["default"]=r.a},253:function(t,e,n){"use strict";(function(t){Object.defineProperty(e,"__esModule",{value:!0}),e.default=void 0;var n=45,i={name:"UniListChat",emits:["click"],props:{title:{type:String,default:""},note:{type:String,default:""},clickable:{type:Boolean,default:!1},link:{type:[Boolean,String],default:!1},to:{type:String,default:""},badgeText:{type:[String,Number],default:""},badgePositon:{type:String,default:"right"},time:{type:String,default:""},avatarCircle:{type:Boolean,default:!1},avatar:{type:String,default:""},avatarList:{type:Array,default:function(){return[]}}},computed:{isSingle:function(){if("dot"===this.badgeText)return"uni-badge--dot";var t=this.badgeText.toString();return t.length>1?"uni-badge--complex":"uni-badge--single"},computedAvatar:function(){return this.avatarList.length>4?(this.imageWidth=.31*n,"avatarItem--3"):this.avatarList.length>1?(this.imageWidth=.47*n,"avatarItem--2"):(this.imageWidth=n,"avatarItem--1")}},data:function(){return{isFirstChild:!1,border:!0,imageWidth:50}},mounted:function(){this.list=this.getForm(),this.list&&(this.list.firstChildAppend||(this.list.firstChildAppend=!0,this.isFirstChild=!0),this.border=this.list.border)},methods:{getForm:function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"uniList",e=this.$parent,n=e.$options.name;while(n!==t){if(e=e.$parent,!e)return!1;n=e.$options.name}return e},onClick:function(){""===this.to?(this.clickable||this.link)&&this.$emit("click",{data:{}}):this.openPage()},openPage:function(){-1!==["navigateTo","redirectTo","reLaunch","switchTab"].indexOf(this.link)?this.pageApi(this.link):this.pageApi("navigateTo")},pageApi:function(e){var n=this;t[e]({url:this.to,success:function(t){n.$emit("click",{data:t})},fail:function(t){n.$emit("click",{data:t}),console.error(t.errMsg)}})}}};e.default=i}).call(this,n(1)["default"])},254:function(t,e,n){"use strict";n.r(e);var i=n(255),r=n.n(i);for(var a in i)"default"!==a&&function(t){n.d(e,t,(function(){return i[t]}))}(a);e["default"]=r.a},255:function(t,e,n){}}]);
//# sourceMappingURL=../../../../../.sourcemap/mp-weixin/uni_modules/uni-list/components/uni-list-chat/uni-list-chat.js.map
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'uni_modules/uni-list/components/uni-list-chat/uni-list-chat-create-component',
    {
        'uni_modules/uni-list/components/uni-list-chat/uni-list-chat-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('1')['createComponent'](__webpack_require__(249))
        })
    },
    [['uni_modules/uni-list/components/uni-list-chat/uni-list-chat-create-component']]
]);
