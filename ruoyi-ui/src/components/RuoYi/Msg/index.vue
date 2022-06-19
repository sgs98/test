<template>
<div >
  <el-badge :value="badgeValue" :max="99">
    <el-popover
      placement="right"
      width="400"
      trigger="click">
      <div class="msg">
        <el-card class="box-card">
          <div class="read">
            <div><i class="el-icon-chat-dot-square icon"></i></div><div style="cursor: pointer;" @click="batchReadMessage()">全部已读</div>
          </div>
        </el-card>
        <el-card class="box-card" style="cursor: pointer;" v-for="(data,index) in messageList" :key="data.id" >
          <span style="cursor: pointer;" v-if="data.status === 0" @click="readMessage(data,index)"><i class="el-icon-message icon"></i>{{data.messageContent}}</span>
          <span v-else-if="data.status === 1" style="color: #ccc;"><i class="el-icon-message icon"></i>{{data.messageContent}}</span>
        </el-card>
        <el-card class="box-card" style="cursor: pointer;">
          <center @click="clickMessage()">查看更多消息<i class="el-icon-d-arrow-right"></i></center>
        </el-card>
      </div>
      <svg-icon slot="reference" icon-class="message"/>
    </el-popover>
  </el-badge>
</div>
</template>

<script>
import { readMessage, batchReadMessage} from "@/api/workflow/message";
export default {
    name: 'RuoYiMsg',
    data() {
    return {
      badgeValue: 0, // 消息总条数
      ws: null, // websocket实例
      wsUrl: process.env.VUE_APP_WEBSOCKET_URL+'/'+this.$store.state.user.name, // websocket连结url
      timeoutObj: null,
      serverTimeoutObj: null,
      timer: null, // 定时器
      lockReconnect: false,
      messageList:[]
    }
    },
    created() {
      // 连接webSocket
      this.initWebSocket();
    },
    methods: {
      //查看更多消息
      clickMessage(){
         this.$router.push("/workflow/message");
      },
      //阅读通知
      readMessage(data,index){
        readMessage(data.id)
        this.messageList[index].status = 1
      },
      //批量阅读通知
      batchReadMessage(){
        batchReadMessage()
        this.messageList = this.messageList.forEach(e=>{
          e.status = 1
        })
      },
      // 页面获取用户信息
      initWebSocket() {
        window.clearTimeout(this.timer);
        window.clearTimeout(this.timeoutObj)
        this.createWebSocket();
      },
      // 创建websocket
      createWebSocket() {
        try {
          this.ws = new WebSocket(this.wsUrl);
          this.initWebScoketFun();
        } catch (e) {
          this.reconnect(this.wsUrl);
        }
      },
      // websocket消息提醒
      initWebScoketFun() {
        const timeout = 15000;
        this.timer = setTimeout(() => {
             this.ws.send("发送心跳");
             this.serverTimeoutObj = setTimeout(() => {
                this.ws.close();
             }, timeout);
        }, timeout)

        this.ws.onclose = () => {
          this.reconnect(this.wsUrl);
        };

        this.ws.onerror = () => {
          this.reconnect(this.wsUrl);
        };

        this.ws.onopen = () => {
          // 心跳检测重置
          this.timer = setTimeout(() => {
             this.ws.send("发送心跳");
          }, timeout)
        };

        this.ws.onmessage = (event) => {
          if(event.data){
            let data = JSON.parse(event.data)
            if(data.page){
              this.badgeValue = data.page.total
              this.messageList = data.page.rows
            }
          }
        };
      },
      // 重新链接websocket
      reconnect(url) {
        if (this.lockReconnect) {
          return;
        }
        this.lockReconnect = true;
        // 没连接上会一直重连，设置延迟避免请求过多
        window.clearTimeout(this.timer);
        window.clearTimeout(this.serverTimeoutObj);
        this.timeoutObj = setTimeout(() => {
          this.createWebSocket(url);
          this.lockReconnect = false;
        }, 15000);
      }
    }
}
</script>
<style scoped>
 .msg{
  font-size: 10px;
 }
 .icon{
  font-size:20px;
  vertical-align: middle;
  padding-right: 10px;
 }
 .read{
  display: flex;
  justify-content: space-between;
 }
</style>
