(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["pages/mine/info/edit"],{74:function(e,n,r){"use strict";(function(e){r(5);t(r(4));var n=t(r(75));function t(e){return e&&e.__esModule?e:{default:e}}wx.__webpack_require_UNI_MP_PLUGIN__=r,e(n.default)}).call(this,r(1)["createPage"])},75:function(e,n,r){"use strict";r.r(n);var t=r(76),o=r(78);for(var u in o)"default"!==u&&function(e){r.d(n,e,(function(){return o[e]}))}(u);r(80);var i,s=r(24),c=Object(s["default"])(o["default"],t["render"],t["staticRenderFns"],!1,null,null,null,!1,t["components"],i);c.options.__file="pages/mine/info/edit.vue",n["default"]=c.exports},76:function(e,n,r){"use strict";r.r(n);var t=r(77);r.d(n,"render",(function(){return t["render"]})),r.d(n,"staticRenderFns",(function(){return t["staticRenderFns"]})),r.d(n,"recyclableRender",(function(){return t["recyclableRender"]})),r.d(n,"components",(function(){return t["components"]}))},77:function(e,n,r){"use strict";var t;r.r(n),r.d(n,"render",(function(){return o})),r.d(n,"staticRenderFns",(function(){return i})),r.d(n,"recyclableRender",(function(){return u})),r.d(n,"components",(function(){return t}));try{t={uniForms:function(){return Promise.all([r.e("common/vendor"),r.e("uni_modules/uni-forms/components/uni-forms/uni-forms")]).then(r.bind(null,299))},uniFormsItem:function(){return Promise.all([r.e("common/vendor"),r.e("uni_modules/uni-forms/components/uni-forms-item/uni-forms-item")]).then(r.bind(null,308))},uniEasyinput:function(){return r.e("uni_modules/uni-easyinput/components/uni-easyinput/uni-easyinput").then(r.bind(null,315))},uniDataCheckbox:function(){return Promise.all([r.e("common/vendor"),r.e("uni_modules/uni-data-checkbox/components/uni-data-checkbox/uni-data-checkbox")]).then(r.bind(null,322))}}}catch(s){if(-1===s.message.indexOf("Cannot find module")||-1===s.message.indexOf(".vue"))throw s;console.error(s.message),console.error("1. 排查组件名称拼写是否正确"),console.error("2. 排查组件是否符合 easycom 规范，文档：https://uniapp.dcloud.net.cn/collocation/pages?id=easycom"),console.error("3. 若组件不符合 easycom 规范，需手动引入，并在 components 中注册该组件")}var o=function(){var e=this,n=e.$createElement;e._self._c},u=!1,i=[];o._withStripped=!0},78:function(e,n,r){"use strict";r.r(n);var t=r(79),o=r.n(t);for(var u in t)"default"!==u&&function(e){r.d(n,e,(function(){return t[e]}))}(u);n["default"]=o.a},79:function(e,n,r){"use strict";Object.defineProperty(n,"__esModule",{value:!0}),n.default=void 0;var t=r(62),o={data:function(){return{user:{nickName:"",phonenumber:"",email:"",sex:""},sexs:[{text:"男",value:"0"},{text:"女",value:"1"}],rules:{nickName:{rules:[{required:!0,errorMessage:"用户昵称不能为空"}]},phonenumber:{rules:[{required:!0,errorMessage:"手机号码不能为空"},{pattern:/^1[3|4|5|6|7|8|9][0-9]\d{8}$/,errorMessage:"请输入正确的手机号码"}]},email:{rules:[{required:!0,errorMessage:"邮箱地址不能为空"},{format:"email",errorMessage:"请输入正确的邮箱地址"}]}}}},onLoad:function(){this.getUser()},onReady:function(){this.$refs.form.setRules(this.rules)},methods:{getUser:function(){var e=this;(0,t.getUserProfile)().then((function(n){e.user=n.data.user}))},submit:function(e){var n=this;this.$refs.form.validate().then((function(e){(0,t.updateUserProfile)(n.user).then((function(e){n.$modal.msgSuccess("修改成功")}))}))}}};n.default=o},80:function(e,n,r){"use strict";r.r(n);var t=r(81),o=r.n(t);for(var u in t)"default"!==u&&function(e){r.d(n,e,(function(){return t[e]}))}(u);n["default"]=o.a},81:function(e,n,r){}},[[74,"common/runtime","common/vendor"]]]);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/mine/info/edit.js.map