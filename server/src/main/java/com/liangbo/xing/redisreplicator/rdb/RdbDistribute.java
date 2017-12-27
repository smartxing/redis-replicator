package com.liangbo.xing.redisreplicator.rdb;

import com.liangbo.xing.redisreplicator.listener.CommonParserListener;
import com.liangbo.xing.redisreplicator.listener.RdbParserListener;
import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandListener;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import org.springframework.stereotype.Component;
import org.testng.collections.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午3:54 xingliangbo Exp $
 */
public class RdbDistribute {


    List<CommonParserListener> commonParserListeners = Lists.newArrayList();

    List<RdbParserListener> rdbParserListeners = Lists.newArrayList();


    /**
     * redisPath : redis://127.0.0.1:6379
     *
     * @param redisPath
     * @throws Exception
     */
    public void registerRedisSync(String redisPath) throws Exception {
        try (Replicator replicator = new RedisReplicator(redisPath)) {
            replicator.addRdbListener(new RdbListener.Adaptor() {
                @Override
                public void handle(Replicator replicator, KeyValuePair<?> kv) {
                    commonParserListeners.stream().forEach(commonParserListener -> {
                        commonParserListener.process(replicator, kv);
                    });
                }
            });
            replicator.addCommandListener(new CommandListener() {
                @Override
                public void handle(Replicator replicator, Command command) {
                    rdbParserListeners.stream().forEach(rdbParserListener -> rdbParserListener.process(replicator, command));
                }
            });
            replicator.open();
        }
    }


    public void addCommonParserListener(CommonParserListener commonParserListener) {
        this.commonParserListeners.add(commonParserListener);
    }

    public void addRdbParserListeners(RdbParserListener rdbParserListener) {
        this.rdbParserListeners.add(rdbParserListener);
    }
}
