package com.liangbo.xing.redisreplicator.listener;

import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;

/**
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午8:24 xingliangbo Exp $
 */
public interface RdbParserListener {
    public void process(Replicator replicator,Command command);
}
