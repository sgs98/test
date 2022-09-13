<template>
  <view class="container">
	
	<uni-search-bar @confirm="search" placeholder="请输入搜索内容" v-model="params.messageContent" radius="100" @clear="clear">
	</uni-search-bar>
	<view class="uni-padding-wrap uni-common-mt">
		<uni-segmented-control :current="current" :values="tabs" style-type="button"
			active-color="#007aff" @clickItem="onClickItem" />
	</view>
	<view class="content" v-if="list.length == 0">
		<uni-load-more color="#d5d5d5" status="noMore" />
	</view>
	<view class="content" v-if="list.length != 0">
		<view v-for="(item, index) in list" :key="index">
			<uni-card :title="`${item.title}`" :extra="item.createBy" margin="10rpx" padding="10rpx" spacing="10rpx" is-full>
				<uni-row class="font-9">
					标题：{{item.title}}
				</uni-row>
				<uni-row class="font-9">
					内容：{{item.messageContent}}
				</uni-row>
				<uni-row class="font-9">
					发送人：{{item.createBy}}
				</uni-row>
				<uni-row class="font-9">
					<uni-tag :text="item.status == 0 ? '未读' : '已读'" :type="item.status == 0 ? 'warning' : 'success'" v-if="checkPermi(['system:message:list'])" v-on:click="handleRead(item)" />
					<!-- <uni-tag text="办理" type="primary" v-if="checkPermi(['business:project:edit'])" v-on:click="handleUpdate(item.processInstanceId)" /> -->
				</uni-row>
			</uni-card>
		</view>
		<uni-pagination :total="total" :current="params.pageNum" :pageSize="params.pageSize" :showIcon="true" @change="pageChange" />
		
		<!-- 提示窗 -->
		<uni-popup ref="alertDialog" type="dialog">
			<uni-popup-dialog :type="alertType" cancelText="取消" confirmText="确认" title="提示" :content="alertContent" @confirm="dialogConfirm"
				@close="dialogClose"></uni-popup-dialog>
		</uni-popup>
		<!-- 提示信息弹窗 -->
		<uni-popup ref="message" type="message">
			<uni-popup-message :type="msgType" :message="messageText" :duration="2000"></uni-popup-message>
		</uni-popup>
	</view>
  </view>
</template>

<script>
import { listMessage, getMessage, delMessage, addMessage, updateMessage, readMessage} from "@/api/work/daily/message";
export default {
  data () {
    return {
		// 遮罩层
		loading : false,
		tabs: ['未读', '已读'],
		current: 0,
		// 总条数
		total: 0,
		// 查询参数
		params: {
			pageNum: 0,
			pageSize: 6,
			messageContent: '',
			status:0
		},
		// table 数据
		list: [],
		
		// 弹出框
		alertType:'',
		alertContent:'',
		// 消息提示窗口、提示框
		msgType: '',
		messageText: '',
		
		tagText:'未读',
		tagType:'warn'
    }
  },
  created () {
  	this.getList();
  },
  onShow(){
	this.getList();
  },
  methods: {
	onClickItem(e) {
		if (this.current !== e.currentIndex) {
			this.current = e.currentIndex
			// 0 未读 1 已读
			this.params.status = e.currentIndex
			this.params.pageNum = 0;
			this.getList();
		}
	},
	// 搜索框
	search() {
		this.params.pageNum = 0;
		this.getList()
	},
	// 翻页
	pageChange(e){
		this.params.pageNum = e.current;
		this.getList()
	},
	// 类型选项变更事件
	typeChange(){
		this.getList()
	},
	// 加载数据
	loadData () {
	  this.params.pageNum += 1;
	  this.getList();
	},
	clear() {
		this.params.pageNum = 0;
		this.params.messageContent = "";
		this.getList();
	},
	// 查询列表
	getList () {
		const app = this
		app.loading =true
	
		listMessage(this.params).then(res => {
			app.list = res.rows;
			console.log(res)
			app.total = res.total;
			app.loading =false
		})
	},
	 //已读
	handleRead(row){
	  const ids = row.id || this.ids;
	  this.$modal.confirm('是否确认已读消息通知？').then(() => {
		this.loading = true;
		return readMessage(ids);
	  }).then(() => {
		this.loading = false;
		this.getList();
		this.$modal.msgSuccess("操作成功");
	  }).finally(() => {
		this.loading = false;
	  });
	},
	handleUpdate(id){
		uni.navigateTo({ url: '/pages/work/business/project/edit?id=' + id })
	},
	handleView(id){
		uni.navigateTo({ url: '/pages/work/business/projectTask/history?id=' + id })
	},
	handleDelete(id){
		// this.alertType = 'warn'
		// this.alertContent ='是否确认删除该条数据?'
		// this.$refs.alertDialog.open()
		// this.tempId = id;
		// 禁止删除
		this.msgType = 'error'
		this.messageText = '请联系管理员进行删除！'
		this.$refs.message.open()
	},
	// 确认删除
	dialogConfirm(){
		const app = this
		if(app.tempId){
			delProject(app.tempId).then(() => {
				app.getList();
			});
		}
	},
	// 取消删除
	dialogClose(){
		this.tempId ="";
	},
  }
}
</script>

<style lang="scss" scoped>
.list-item {
  padding: 20rpx 0;
  border-bottom: 0.5px solid #ccc;
}
</style>