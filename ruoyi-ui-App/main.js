import Vue from 'vue'
import App from './App'
import store from './store' // store
import plugins from './plugins' // plugins
import './permission' // permission
import { checkPermi, checkRole } from './utils/permission'

import { getDicts } from "@/api/system/dict/data";
Vue.prototype.getDicts = getDicts

Vue.use(plugins)

Vue.config.productionTip = false
Vue.prototype.$store = store
// 权限挂载
Vue.prototype.checkPermi = checkPermi
Vue.prototype.checkRole = checkRole

App.mpType = 'app'

const app = new Vue({
  ...App
})

app.$mount()
