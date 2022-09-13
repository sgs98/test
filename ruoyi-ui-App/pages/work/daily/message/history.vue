<template>
  <view class="work-container">
	<view class="uni-padding-wrap uni-common-mt">
		<uni-segmented-control :current="current" :values="tabs" style-type="button"
			active-color="#007aff" @clickItem="onClickItem" />
	</view>
	<view class="content" v-if="current==0">
		<view v-for="(item, index) in list" :key="index">
			<uni-card :title="item.name" :extra="item.nickName" margin="10rpx">
				<uni-row class="font-9">
					办理状态：{{item.status}}
				</uni-row>
				<uni-row class="font-9">
					办理意见：{{item.comment}}
				</uni-row>
				<uni-row class="font-9">
					开始时间：{{item.startTime}}
				</uni-row>
				<uni-row class="font-9">
					结束时间：{{item.endTime}}
				</uni-row>
			</uni-card>
		</view>
		<!-- <uni-tr>
			<uni-th width="20" align="center">序号</uni-th>
			<uni-th align="center">任务名称</uni-th>
			<uni-th align="center">办理人</uni-th>
			<uni-th align="center">状态</uni-th>
			<uni-th width="100" align="center">办理意见</uni-th>
			<uni-th width="100" align="center">开始时间</uni-th>
			<uni-th width="100" align="center">结束时间</uni-th>
		</uni-tr>
		<uni-tr v-for="(item, index) in list" :key="index">
			<uni-td align="center">{{index + 1}}</uni-td>
			<uni-td align="center">
				{{item.name}}
			</uni-td>
			<uni-td align="center">
				{{item.nickName}}
			</uni-td>
			<uni-td align="center">
				{{item.status}}
			</uni-td>
			<uni-td align="center">
				{{item.comment}}
			</uni-td>
			<uni-td align="center">
				{{item.startTime}}
			</uni-td>
			<uni-td align="center">
				{{item.endTime}}
			</uni-td>
		</uni-tr> -->
	</view>
	<view class="content" v-if="current==1">
		<view v-for="(item, index) in list" :key="index">
			<step-bar :textTitle="item.name" :textContent="'<b>办理人:</b>'+item.nickName + '<br><b>办理意见:</b>'+item.comment + '<br> <b>状态:</b>'+ item.status + '<br> <b>起止时间:</b>'+item.startTime +'~'+ (item.endTime == null ? '' : item.endTime )" :status="item.status"></step-bar>
		</view>
	</view>
  </view>
</template>

<script>
import apiProcessInst from '@/api/work/workflow/processInst'
import stepBar from "@/components/stepBar/stepBar.vue"
export default {
	components: {
		stepBar,
	},
	
  data () {
    return {
		// 遮罩层
		processInstanceId:'',
		loading : false,
		tabs: ['流程进度', '流程图'],
		current: 0,
		// table 数据
		list: [],
    }
  },
  onLoad (params) {
    this.processInstanceId = params.id
    this.loadData()
  },
  methods: {
	onClickItem(e) {
		if (this.current !== e.currentIndex) {
			this.current = e.currentIndex
		}
	},
	// 查询审批历史记录
	async getHistoryInfoList() {
	  const { data } = await apiProcessInst.getHistoryInfoList(this.processInstanceId)
	  this.list = data
	  this.loading = false
	  console.log(data)
	},
	// 加载数据
	loadData () {
	  this.loading =true
	  this.getHistoryInfoList()
	},
  }
}
</script>

<style lang="scss" scoped>
.list-item {
  padding: 20rpx 0;
  border-bottom: 0.5px solid #ccc;
}
.content{
  margin: 10rpx 0;
}
</style>