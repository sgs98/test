<template>
  <view>
    <Navbar :hideBtn="false" bgColor="#f3f4f6"></Navbar>
    <view style="background-color: #2b85e4; padding: 40rpx;">
      <view style="width: 140rpx; height: 140rpx; border: 1px solid #fff; border-radius: 50%; margin: 0 auto;">
        <u-avatar src="/static/img/avatar.png" size="120rpx" style="margin: 10rpx;"></u-avatar>
      </view>
    </view>
    <view style="padding: 40rpx;">
      <u--form :model="userInfo" ref="uForm" labelWidth="160rpx" labelAlign="left">
        <u-form-item label="姓名" prop="nickName" class="u-border-bottom">
          <u--input
            placeholder="请输入内容"
            border="none"
            v-model="userInfo.nickName"
          ></u--input>
        </u-form-item>
        <u-form-item label="性别" prop="sex" class="u-border-bottom">
          <u-radio-group v-model="userInfo.sex" size="36rpx">
            <u-radio shape="circle" label="男" name="1" labelSize="32rpx"></u-radio>
            <u-radio shape="circle" label="女" name="2" labelSize="32rpx" style="margin-left: 36rpx;"></u-radio>
          </u-radio-group>
        </u-form-item>
        <u-form-item label="手机号码" prop="phonenumber" class="u-border-bottom">
          <u--input
            placeholder="请输入内容"
            border="none"
            v-model="userInfo.phonenumber"
          ></u--input>
        </u-form-item>
        <u-form-item label="邮箱" prop="email" class="u-border-bottom">
          <u--input
            placeholder="请输入内容"
            border="none"
            v-model="userInfo.email"
          ></u--input>
        </u-form-item>
      </u--form>
    </view>
    <view style="padding: 40rpx;">
      <u-row gutter="32">
        <u-col span="6">
          <u-button icon="arrow-left" text="返回" plain @click="goBack()"></u-button>
        </u-col>
        <u-col span="6">
		    <u-button icon="checkmark-circle" text="保存" type="primary" @click="updateUserInfo()"></u-button>
        </u-col>
      </u-row>
    </view>
  </view>
</template>

<script>
import Navbar from '@/components/navbar/Navbar'
import * as api from '@/api/user'
import { isEmpty, isMobile,isEmail} from '@/utils/verify.js'
export default {
  components: {
    Navbar,
  },
  data () {
    return {
      userInfo: {}
    }
  },
  created(){
	this.loadData();
  },
  methods: {
	// 加载数据
	loadData () {
		const app = this
		// 首先获取当前登录账号信息
		app.$store.dispatch('Info').then(res => {
		  if (res) {
			// 只查询当前用户的操作日志
			app.userInfo = res
		  }
		})
	},
    goBack () {
      uni.navigateBack({ delta: 1});
    },
	// 验证表单内容
	validItem() {
		const app = this
		if (isEmpty(app.userInfo.nickName)) {
			uni.$u.toast('请输入姓名')
			return false
		}
		if (isEmpty(app.userInfo.sex)) {
			uni.$u.toast('请选择性别')
			return false
		}
		if (isEmpty(app.userInfo.phonenumber)) {
			uni.$u.toast('请输入正确手机号')
			return false
		}
		if (isEmpty(app.userInfo.email)) {
			uni.$u.toast('请输入邮箱')
			return false
		}
		return true
	},
	updateUserInfo(){
		const app = this
		let valid = app.validItem();
		if(valid){
			api.updateUserProfile(this.userInfo).then(response => {
				console.log(app.userInfo)
				uni.$u.toast('修改成功')
			});
		}
		
	}
  }
}
</script>

<style lang="sass" scoped>

</style>