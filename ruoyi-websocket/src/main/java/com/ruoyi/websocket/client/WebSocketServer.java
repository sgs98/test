package com.ruoyi.websocket.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xycq
 * @version 1.0.0
 * @ClassName WebSocketServer.java
 * @Description TODO
 * @createTime 2022年06月16日
 */
@ServerEndpoint("/webSocketServer/{userName}")
@Component
public class WebSocketServer {
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
    public void onOpen(Session session,@PathParam("userName") String userName) throws IOException {
        this.session = session;
        this.userName=userName;
        if(webSocketMap.containsKey(userName)){
            webSocketMap.remove(userName);
            webSocketMap.put(userName,this);
            //加入set中
        }else{
            webSocketMap.put(userName,this);
            //加入set中
            addOnlineCount();
            //在线数加1
        }
        sendMessageTo("连接成功用户:"+ userName,userName);
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
        //可以群发消息
        //消息保存到数据库、redis
        if(StringUtils.isNotBlank(message)){
            try {
                //解析发送的报文
                JSONObject jsonObject = JSONUtil.parseObj(message);
                //追加发送人(防止串改)
                jsonObject.set("fromUserName",this.userName);
                String toUserId=jsonObject.getStr("toUserName");
                //传送给对应toUserId用户的websocket
                if(StringUtils.isNotBlank(toUserId)&&webSocketMap.containsKey(toUserId)){
                    webSocketMap.get(toUserId).sendMessage(jsonObject.toString());
                }else{
                    //System.out.println("请求的userId:"+toUserId+"不在该服务器上");
                    //否则不在这个服务器上，发送到mysql或者redis
                }
            }catch (Exception e){
            }
        }
    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("发生错误");
        throwable.printStackTrace();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 批量发送
     * @param message
     * @throws IOException
     */
    public static void sendMessageAll(String message) throws IOException {
        for (WebSocketServer item : webSocketMap.values()) {
            item.session.getBasicRemote().sendText(message);
        }
    }

    /**
     * 批量发送Object
     * @param msg
     * @throws IOException
     */
    public static void sendObjMessageAll(R<Object> msg) throws IOException, EncodeException {
        for (WebSocketServer item : webSocketMap.values()) {
            item.session.getBasicRemote().sendObject(msg);
        }
    }
    /**
     * 定向发送
     * @param msg
     * @param to 指定对象id
     * @throws IOException
     */
    public static void sendObjMessageTo(R<Object> msg, String to) throws IOException, EncodeException {
        if(StringUtils.isNotBlank(to)&&webSocketMap.containsKey(to)){
            WebSocketServer client = webSocketMap.get(to);
            client.session.getBasicRemote().sendObject(msg);
        }else{
        }
    }
    /**
     * 定向发送
     * @param message
     * @param to 指定对象id
     * @throws IOException
     */
    public static void sendMessageTo(String message, String to) throws IOException{
        if(StringUtils.isNotBlank(to)&&webSocketMap.containsKey(to)){
            WebSocketServer client = webSocketMap.get(to);
            client.session.getBasicRemote().sendText(message);
        }else{
        }
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
