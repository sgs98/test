package com.ruoyi.websocket.client;

import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.workflow.domain.vo.SysMessageVo;
import com.ruoyi.workflow.service.ISysMessageService;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
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
@Component
@ServerEndpoint(value = "/webSocketServer/{userName}",encoders = {ServerEncoder.class})
public class WebSocketServer {

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的对象。
     */
    private static ConcurrentHashMap<String,Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * @Description: 连接建立成功调用的方法
     * @param: session 
     * @param: userName
     * @return: void
     * @author: xycq
     * @Date: 2022/6/20 20:11
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("userName") String userName){
        if(!sessionPool.containsKey(userName)){
            sessionPool.put(userName,session);
        }
    }

    /**
     * @Description: 连接关闭调用的方法
     * @param: closeSession
     * @return: void
     * @author: gssong
     * @Date: 2022/6/20 20:11
     */
    @OnClose
    public void onClose(Session closeSession) {
        for (String userName : sessionPool.keySet()) {
            Session session = sessionPool.get(userName);
            if(session.getId().equals(closeSession.getId())){
                sessionPool.remove(userName);
                break;
            }
        }
    }


    /**
     * @Description: 客户端发送过来的消息
     * @param: message
     * @param: onSession
     * @return: void
     * @author: gssong
     * @Date: 2022/6/20 20:09
     */
    @OnMessage
    public void onMessage(String message, Session onSession) {
        /*Map<String, Object> map = new HashMap<>();
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(5);
        ISysMessageService bean = SpringUtils.getBean(ISysMessageService.class);
        for (String userName : sessionPool.keySet()) {
            Session session = sessionPool.get(userName);
            if(session.getId().equals(onSession.getId())){
                TableDataInfo<SysMessageVo> page = bean.queryPage(pageQuery,userName);
                map.put("page",page);
                map.put("message",message);
                sessionPool.get(userName).getAsyncRemote().sendObject(map);
                break;
            }
        }*/
    }

    /**
     * @Description: 错误时调用
     * @param: session
     * @param: throwable
     * @return: void
     * @author: xycq
     * @Date: 2022/6/20 20:12
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

}
