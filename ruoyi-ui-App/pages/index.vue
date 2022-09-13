<template>
	<view>
		<uni-notice-bar show-icon scrollable
			text="uni-app 版正式发布，开发一次，同时发布iOS、Android、H5、微信小程序、支付宝小程序、百度小程序、头条小程序等7大平台。" />
		<view class="work-container">
			<!-- 轮播图 -->
			<uni-swiper-dot class="uni-swiper-dot-box" :info="list" :current="current" field="content">
			<swiper class="swiper-box" :current="swiperDotIndex" @change="changeSwiper">
			  <swiper-item v-for="(item, index) in list" :key="index">
				<view class="swiper-item" @click="clickBannerItem(item)">
				  <image :src="item.image" mode="aspectFill" :draggable="false" />
				</view>
			  </swiper-item>
			</swiper>
			</uni-swiper-dot>
			
			<uni-list-chat title="消息通知" avatar="https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png" note="您收到一条新的消息" time="2020-02-02 20:20" badge-positon="left" badge-text="99"></uni-list-chat>
			<!-- 自定义右侧内容 -->
			<uni-list-chat title="uni-app" :avatar-list="avatarList" note="您收到一条新的消息" time="2020-02-02 20:20" badge-positon="left" badge-text="dot">
				<view class="chat-custom-right">
					<text class="chat-custom-text">2020-02-02 20:20</text>
					<!-- 需要使用 uni-icons 请自行引入 -->
					<!-- <uni-icons type="star-filled" color="#999" size="18"></uni-icons> -->
				</view>
			</uni-list-chat>
		</view>
	</view>
	
</template>
<script>
export default {
	data() {
		return {
			current: 0,
			swiperDotIndex: 0,
			list: [{
				image: '/static/images/banner/banner01.jpg'
			  },
			  {
				image: '/static/images/banner/banner02.jpg'
			  },
			  {
				image: '/static/images/banner/banner03.jpg'
			  }
			],
			avatarList: [{
				url: 'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png'
			}, {
				url: 'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png'
			}, {
				url: 'https://vkceyugu.cdn.bspapp.com/VKCEYUGU-dc-site/460d46d0-4fcc-11eb-8ff1-d5dcf8779628.png'
			}]
		};
	},
	onLoad() {
		
	},
	methods: {
		clickBannerItem(item) {
		  console.info(item)
		},
		changeSwiper(e) {
		  this.current = e.detail.current
		},
	}
};
</script>
<style lang="scss">
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
