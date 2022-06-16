<template>
<div >
  <el-badge :value="waitCount">
    <svg-icon icon-class="message" @click="clickMsg" />
  </el-badge>
</div>
</template>

<script>
import task from '@/api/workflow/task'
export default {
    name: 'RuoYiMsg',
    data() {
    return {
        msg:'',
        websock:null,
        waitCount:this.$store.waitCount,
    }
    },
    created() {
        // 连接webSocket
        this.initWebSocket();
    },
    destroyed() {
        // 离开路由之后断开websocket连接
        this.websocketclose()
    },
    methods: {
        clickMsg(){
            // if(this.$store.waitCount){
            //     this.$message({
            //         showClose: true,
            //         message: '您有'+this.$store.waitCount+'个待办任务！'
            //     });
            // }
            this.$confirm('您有'+this.$store.waitCount+'个待办任务！', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
            }).then(() => {
                this.$router.push("/workflowPersonal/personalWaiting");
            })
        },
        // ---- WebSocket连接start ----
        initWebSocket() {
            //初始化weosocket
            const wsuri = process.env.VUE_APP_WEBSOCKET_URL+'/'+this.$store.state.user.name
            this.websock = new WebSocket(wsuri)
            this.websock.onmessage = this.websocketonmessage
            this.websock.onopen = this.websocketonopen
            this.websock.onerror = this.websocketonerror
            this.websock.onclose = this.websocketclose
        },
        // 连接建立之后执行send方法发送数据
        websocketonopen() {
        },
        //连接建立失败重连
        websocketonerror() {
            console.log('WebSocket连接失败，重新连接')
            this.initWebSocket()
        },
        //数据接收
        websocketonmessage(e) {
            console.log(e)
            if (e.isTrusted) {
                this.msg = e.data
                //this.msgShow = true // 提示信息的弹框展示，弹框样式本文不再赘述
                this.$notify({
                    title: '提示',
                    message: '连接websocket成功：'+this.msg,
                    type: 'success',
                    position: 'bottom-right',
                    //duration: 0
                });

                setTimeout(() => {
                    task.getTaskWaitCount().then(response => {
                        this.waitCount=response.data.count;
                        //设置到全局变量
                        this.$store.waitCount=response.data.count;
                    })
                    console.log('收到数据执行xxxx方法正在。。。')
                    console.log(this.msg)
                }, 3000)
            }
        },
        //数据发送
        websocketsend(Data) {
            this.websock.send(Data)
        },
        //关闭 websocket
        websocketclose(e) {
            console.log('WebSocket断开连接,重新连接', e)
        }
        // ---- WebSocket连接end ----
    }
}
</script>
