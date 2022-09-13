(global["webpackJsonp"]=global["webpackJsonp"]||[]).push([["uni_modules/uni-forms/components/uni-forms/uni-forms"],{299:function(e,t,n){"use strict";n.r(t);var r=n(300),a=n(302);for(var i in a)"default"!==i&&function(e){n.d(t,e,(function(){return a[e]}))}(i);n(306);var u,o=n(24),l=Object(o["default"])(a["default"],r["render"],r["staticRenderFns"],!1,null,null,null,!1,r["components"],u);l.options.__file="uni_modules/uni-forms/components/uni-forms/uni-forms.vue",t["default"]=l.exports},300:function(e,t,n){"use strict";n.r(t);var r=n(301);n.d(t,"render",(function(){return r["render"]})),n.d(t,"staticRenderFns",(function(){return r["staticRenderFns"]})),n.d(t,"recyclableRender",(function(){return r["recyclableRender"]})),n.d(t,"components",(function(){return r["components"]}))},301:function(e,t,n){"use strict";var r;n.r(t),n.d(t,"render",(function(){return a})),n.d(t,"staticRenderFns",(function(){return u})),n.d(t,"recyclableRender",(function(){return i})),n.d(t,"components",(function(){return r}));var a=function(){var e=this,t=e.$createElement;e._self._c},i=!1,u=[];a._withStripped=!0},302:function(e,t,n){"use strict";n.r(t);var r=n(303),a=n.n(r);for(var i in r)"default"!==i&&function(e){n.d(t,e,(function(){return r[e]}))}(i);t["default"]=a.a},303:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.default=void 0;var r=o(n(120)),a=o(n(304)),i=n(305),u=o(n(4));function o(e){return e&&e.__esModule?e:{default:e}}function l(e,t,n,r,a,i,u){try{var o=e[i](u),l=o.value}catch(s){return void n(s)}o.done?t(l):Promise.resolve(l).then(r,a)}function s(e){return function(){var t=this,n=arguments;return new Promise((function(r,a){var i=e.apply(t,n);function u(e){l(i,r,a,u,o,"next",e)}function o(e){l(i,r,a,u,o,"throw",e)}u(void 0)}))}}function f(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}u.default.prototype.binddata=function(e,t,n){if(n)this.$refs[n].setValue(e,t);else{var r;for(var a in this.$refs){var i=this.$refs[a];if(i&&i.$options&&"uniForms"===i.$options.name){r=i;break}}if(!r)return console.error("当前 uni-froms 组件缺少 ref 属性");r.setValue(e,t)}};var c={name:"uniForms",emits:["validate","submit"],options:{virtualHost:!0},props:{value:{type:Object,default:function(){return null}},modelValue:{type:Object,default:function(){return null}},model:{type:Object,default:function(){return null}},rules:{type:Object,default:function(){return{}}},errShowType:{type:String,default:"undertext"},validateTrigger:{type:String,default:"submit"},labelPosition:{type:String,default:"left"},labelWidth:{type:[String,Number],default:""},labelAlign:{type:String,default:"left"},border:{type:Boolean,default:!1}},provide:function(){return{uniForm:this}},data:function(){return{formData:{},formRules:{}}},computed:{localData:function(){var e=this.model||this.modelValue||this.value;return e?(0,i.deepCopy)(e):{}}},watch:{rules:{handler:function(e,t){this.setRules(e)},deep:!0,immediate:!0}},created:function(){this.childrens=[],this.inputChildrens=[],this.setRules(this.rules)},methods:{setRules:function(e){this.formRules=Object.assign({},this.formRules,e),this.validator=new a.default(e)},setValue:function(e,t){var n=this.childrens.find((function(t){return t.name===e}));return n?(this.formData[e]=(0,i.getValue)(e,t,this.formRules[e]&&this.formRules[e].rules||[]),n.onFieldChange(this.formData[e])):null},validate:function(e,t){return this.checkAll(this.formData,e,t)},validateField:function(){var e=this,t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:[],n=arguments.length>1?arguments[1]:void 0;t=[].concat(t);var r={};return this.childrens.forEach((function(n){var a=(0,i.realName)(n.name);-1!==t.indexOf(a)&&(r=Object.assign({},r,f({},a,e.formData[a])))})),this.checkAll(r,[],n)},clearValidate:function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:[];e=[].concat(e),this.childrens.forEach((function(t){if(0===e.length)t.errMsg="";else{var n=(0,i.realName)(t.name);-1!==e.indexOf(n)&&(t.errMsg="")}}))},submit:function(e,t,n){var r=this,a=function(e){var t=r.childrens.find((function(t){return t.name===e}));t&&void 0===r.formData[e]&&(r.formData[e]=r._getValue(e,r.dataValue[e]))};for(var i in this.dataValue)a(i);return n||console.warn("submit 方法即将废弃，请使用validate方法代替！"),this.checkAll(this.formData,e,t,"submit")},checkAll:function(e,t,n,a){var u=this;return s(r.default.mark((function o(){var l,s,f,c,d,m,h,v,p,b,y;return r.default.wrap((function(o){while(1)switch(o.prev=o.next){case 0:if(u.validator){o.next=2;break}return o.abrupt("return");case 2:for(f in l=[],s=function(e){var t=u.childrens.find((function(t){return(0,i.realName)(t.name)===e}));t&&l.push(t)},e)s(f);n||"function"!==typeof t||(n=t),!n&&"function"!==typeof n&&Promise&&(c=new Promise((function(e,t){n=function(n,r){n?t(n):e(r)}}))),d=[],m=JSON.parse(JSON.stringify(e)),o.t0=r.default.keys(l);case 10:if((o.t1=o.t0()).done){o.next=23;break}return h=o.t1.value,v=l[h],p=(0,i.realName)(v.name),o.next=16,v.onFieldChange(m[p]);case 16:if(b=o.sent,!b){o.next=21;break}if(d.push(b),"toast"!==u.errShowType&&"modal"!==u.errShowType){o.next=21;break}return o.abrupt("break",23);case 21:o.next=10;break;case 23:if(Array.isArray(d)&&0===d.length&&(d=null),Array.isArray(t)&&t.forEach((function(e){var t=(0,i.realName)(e),n=(0,i.getDataValue)(e,u.localData);void 0!==n&&(m[t]=n)})),"submit"===a?u.$emit("submit",{detail:{value:m,errors:d}}):u.$emit("validate",d),y={},y=(0,i.rawData)(m,u.name),n&&"function"===typeof n&&n(d,y),!c||!n){o.next=33;break}return o.abrupt("return",c);case 33:return o.abrupt("return",null);case 34:case"end":return o.stop()}}),o)})))()},validateCheck:function(e){this.$emit("validate",e)},_getValue:i.getValue,_isRequiredField:i.isRequiredField,_setDataValue:i.setDataValue,_getDataValue:i.getDataValue,_realName:i.realName,_isRealName:i.isRealName,_isEqual:i.isEqual}};t.default=c},306:function(e,t,n){"use strict";n.r(t);var r=n(307),a=n.n(r);for(var i in r)"default"!==i&&function(e){n.d(t,e,(function(){return r[e]}))}(i);t["default"]=a.a},307:function(e,t,n){}}]);
//# sourceMappingURL=../../../../../.sourcemap/mp-weixin/uni_modules/uni-forms/components/uni-forms/uni-forms.js.map
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'uni_modules/uni-forms/components/uni-forms/uni-forms-create-component',
    {
        'uni_modules/uni-forms/components/uni-forms/uni-forms-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('1')['createComponent'](__webpack_require__(299))
        })
    },
    [['uni_modules/uni-forms/components/uni-forms/uni-forms-create-component']]
]);
