<!-- 步骤条-->
<template>
  <view class="step-line-one">
    <view class="step-bar">
		<view :class="clazzLine"></view>
		<view :class="clazzDot"></view>
    </view>
    <view class="step-content" >
      <view class="step-content-title">{{ textTitle }}</view>
      <view class="step-content-content" v-html="textContent">{{ textContent }}</view>
    </view>
  </view>
</template>
 
<script>
export default {
  name: "stepBar",
  props: {
    textTitle: {
		type: String,
		default: ''
    },
	textContent: {
		type: String,
		default: ''
	},
	status:{
		type: String,
		default: ''
	}
  },
  data () {
    return {
  		clazzLine:'step-bar-line',
		clazzDot:'step-bar-dot'
    }
  },
  watch:{
	status:{
		handler(newVal,oldVal){
			switch(newVal){
				case '待处理':
					this.clazzLine = 'step-bar-line-1'
					this.clazzDot = 'step-bar-dot-1'
					break;
				case '已处理':
					this.clazzLine = 'step-bar-line'
					this.clazzDot = 'step-bar-dot'
					break;
			}
		},
		immediate: true,
		deep:true
	}
  }
}
</script>
 
<style scoped lang="scss">
	.step-content {
	  margin-left: 20px;
	  &-title {
		font-weight: 700;
	  }
	  &-content {
		margin: 10px 0;
		color: #b0b0b0;
		font-size: 8px;
	  }
	}
	.step-bar {
	  position: relative;
	}
	.step-bar-dot {
	  width: 14px;
	  height: 15px;
	  border-radius: 50%;
	  background-color: #17bc05;
	  position: absolute;
	  left: 0;
	  top: 3px;
	}
	.step-bar-line {
	  width: 4px;
	  height: 85px;
	  background-color: #17bc05;
	  position: absolute;
	  left: 6px;
	  top: 7px;
	}
	.step-bar-dot-1 {
	  width: 14px;
	  height: 15px;
	  border-radius: 50%;
	  background-color: #ffaa00;
	  position: absolute;
	  left: 0;
	  top: 3px;
	}
	.step-bar-line-1 {
	  width: 3px;
	  height: 85px;
	  background-color: #ffaa00;
	  position: absolute;
	  left: 6px;
	  top: 7px;
	}
</style>