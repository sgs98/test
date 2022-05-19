
package com.ruoyi.workflow.flowable.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.flowable.common.engine.impl.cfg.IdGenerator;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;
/**
 * @program: ruoyi-vue-plus
 * @description: 配置
 * @author: gssong
 * @created: 2021/10/03 19:31
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {


    /**
     * 解決工作流生成图片乱码问题
     * @param processEngineConfiguration
     */

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setProcessDiagramGenerator(new CustomDefaultProcessDiagramGenerator());

        /**
         * 自定义id
         */
        processEngineConfiguration.setIdGenerator(() -> {
            Snowflake snowflake = IdUtil.getSnowflake(1, 1);
            return snowflake.nextIdStr();
        });
    }
}

