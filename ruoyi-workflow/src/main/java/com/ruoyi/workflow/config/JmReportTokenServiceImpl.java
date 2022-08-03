package com.ruoyi.workflow.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.ruoyi.common.utils.StringUtils;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;

/**
 * @author xycq
 * @version 1.0.0
 * @ClassName JmReportTokenServiceImpl.java
 * @Description 积木报表Token验证
 * @createTime 2022年08月03日 15:12:00
 */
public class JmReportTokenServiceImpl implements JmReportTokenServiceI  {
    /**
     * 获得用户id
     * @param token  token
     * @return 用户id
     */
    @Override
    public String getUsername(String token) {
        String loginId = StpUtil.stpLogic.getLoginIdNotHandle(token);
        if(StringUtils.isEmpty(loginId)){
            throw new RuntimeException("没有认证");
        }
        return loginId;
    }

    /**
     * 校验 Token
     *
     * @param token JmReport token
     * @return 是否认证通过
     */
    @Override
    public Boolean verifyToken(String token) {
        if (StrUtil.isEmpty(token)) {
            return false;
        }

        String loginId = StpUtil.stpLogic.getLoginIdNotHandle(token);
        return !StringUtils.isEmpty(loginId);
    }
}
