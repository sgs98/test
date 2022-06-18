<template>
<div >
  <el-badge :value="badgeValue">
    <svg-icon icon-class="message" />
  </el-badge>
</div>
</template>

<script>
export default {
    name: 'RuoYiMsg',
    data() {
    return {
      badgeValue: 0, // 消息总条数
      // websocket消息推送
      ws: null, // websocket实例
      wsUrl: process.env.VUE_APP_WEBSOCKET_URL+'/'+this.$store.state.user.name, // websocket连结url
      timeoutObj: null,
      serverTimeoutObj: null,
      timer: null, // 定时器
      lockReconnect: false,
    }
    },
    created() {
        // 连接webSocket
        this.initWebSocket();
    },
    methods: {
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
