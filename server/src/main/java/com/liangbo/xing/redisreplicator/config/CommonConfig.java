package com.liangbo.xing.redisreplicator.config;

import com.liangbo.xing.redisreplicator.listener.CommonParserListener;
import com.liangbo.xing.redisreplicator.listener.RdbParserListener;
import com.liangbo.xing.redisreplicator.rdb.RdbDistribute;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

/**
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午9:05 xingliangbo Exp $
 */
@Configuration
public class CommonConfig {

    @Bean
    public RdbDistribute rdbDistribute() {
        RdbDistribute rdbDistribute = new RdbDistribute();
        rdbDistribute.addCommonParserListener(new CommonParserListener() {
            @Override
            public void process(Replicator replicator, KeyValuePair keyValuePair) {

            }
        });
        rdbDistribute.addRdbParserListeners(new RdbParserListener() {
            @Override
            public void process(Replicator replicator, Command command) {

            }
        });
        return rdbDistribute;
    }
}
