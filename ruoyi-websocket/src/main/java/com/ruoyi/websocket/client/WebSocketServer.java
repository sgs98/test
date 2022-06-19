package com.ruoyi.websocket.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.JsonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.workflow.domain.vo.SysMessageVo;
import com.ruoyi.workflow.service.ISysMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xycq
 * @version 1.0.0
 * @ClassName WebSocketServer.java
 * @Description 即时通讯
 * @createTime 2022年06月16日
 */
@ServerEndpoint(value = "/webSocketServer/{userName}",encoders = {ServerEncoder.class})
@Component
public class WebSocketServer {

    protected static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    //记录当前在线连接数
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentHashMap<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收userName
    private String userName="";

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session,@PathParam("userName") String userName) throws IOException, EncodeException {
        this.session = session;
        this.userName=userName;
        if(webSocketMap.containsKey(userName)){
            webSocketMap.remove(userName);
            webSocketMap.put(userName,this);
        }else{
            webSocketMap.put(userName,this);
            //加入set中  在线数加1
            addOnlineCount();
        }
        sendMessageTo(userName);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(userName)){
            webSocketMap.remove(userName);
            //从set中删除
            subOnlineCount();
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        if(StringUtils.isNotBlank(message)){
            try {
                if(StringUtils.isNotBlank(userName)&&webSocketMap.containsKey(userName)){
                    webSocketMap.get(userName).sendMessage();
                }else{
                    webSocketMap.put(userName,this);
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.info("请求失败:"+e.getMessage());
            }
        }
    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage() throws IOException, EncodeException {
        HashMap<String, Object> map = new HashMap<>();
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(5);
        ISysMessageService bean = SpringUtils.getBean(ISysMessageService.class);
        TableDataInfo<SysMessageVo> page = bean.queryPage(pageQuery,userName);
        map.put("page",page);
        this.session.getBasicRemote().sendObject(map);
    }
    /**
     * @Description:  message
     * @param: userName
     * @return: void
     * @author: gssong
     * @Date: 2022/6/18 16:10
     */
    public static void sendMessageTo(String userName) throws IOException, EncodeException {
        WebSocketServer client = webSocketMap.get(userName);
        Map<String, Object> map = new HashMap<>();
        map.put("userName",userName);
        client.session.getBasicRemote().sendObject(map);
    }
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

}
