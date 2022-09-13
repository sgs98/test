<template>
  <view class="work-container">
	<!-- 日常办公 -->
	<uni-section title="日常办公" type="line"></uni-section>
	<view class="grid-body">
		<uni-grid :column="4" :showBorder="false">
			<uni-grid-item v-if="checkPermi(['system:message:list'])" >
				<view class="grid-item-box" @click="navTo('/pages/work/daily/message/index')">
				  <uni-icons type="email" size="30"></uni-icons>
				  <text class="text">消息</text>
				</view>
			</uni-grid-item>
		</uni-grid>
	</view>
    <uni-section title="系统管理" type="line"></uni-section>
    <view class="grid-body">
      <uni-grid :column="4" :showBorder="false" @change="changeGrid">
        <uni-grid-item v-if="checkPermi(['system:user:add'])">
          <view class="grid-item-box">
            <uni-icons type="person-filled" size="30"></uni-icons>
            <text class="text">用户管理</text>
          </view>
        </uni-grid-item>
        <uni-grid-item v-if="checkPermi(['system:role:add'])">
          <view class="grid-item-box">
            <uni-icons type="staff-filled" size="30"></uni-icons>
            <text class="text">角色管理</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="color" size="30"></uni-icons>
            <text class="text">菜单管理</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="settings-filled" size="30"></uni-icons>
            <text class="text">部门管理</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="heart-filled" size="30"></uni-icons>
            <text class="text">岗位管理</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="bars" size="30"></uni-icons>
            <text class="text">字典管理</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="gear-filled" size="30"></uni-icons>
            <text class="text">参数设置</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="chat-filled" size="30"></uni-icons>
            <text class="text">通知公告</text>
          </view>
        </uni-grid-item>
        <uni-grid-item>
          <view class="grid-item-box">
            <uni-icons type="wallet-filled" size="30"></uni-icons>
            <text class="text">日志管理</text>
          </view>
        </uni-grid-item>
      </uni-grid>
    </view>
  </view>
</template>

<script>
  export default {
    data() {
      return {
        
      }
    },
    methods: {
      navTo(url) {
      	uni.navigateTo({
      		url: url
      	});
      },
      changeGrid(e) {
        this.$modal.showToast('模块建设中~')
      }
    }
  }
</script>

<style lang="scss">
  /* #ifndef APP-NVUE */
  page {
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    background-color: #fff;
    min-height: 100%;
    height: auto;
  }

  view {
    font-size: 14px;
    line-height: inherit;
  }

  /* #endif */

  .text {
    text-align: center;
    font-size: 26rpx;
    margin-top: 10rpx;
  }

  .grid-item-box {
    flex: 1;
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: column;
    align-items: center ;
    justify-content: center;
    padding: 15rpx 0;
  }

  .uni-margin-wrap {
    width: 690rpx;
    width: 100%;
  }

  .swiper {
    height: 300rpx;
  }

  .swiper-box {
    height: 150px;
  }

  .swiper-item {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #fff;
    height: 300rpx;
    line-height: 300rpx;
  }

  @media screen and (min-width: 500px) {
    .uni-swiper-dot-box {
      width: 400px;
      /* #ifndef APP-NVUE */
      margin: 0 auto;
      /* #endif */
      margin-top: 8px;
    }

    .image {
      width: 100%;
    }
  }
</style>
