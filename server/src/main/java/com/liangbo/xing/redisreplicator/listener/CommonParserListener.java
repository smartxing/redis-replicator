package com.liangbo.xing.redisreplicator.listener;

import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;

/**
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午8:17 xingliangbo Exp $
 */
public interface CommonParserListener {

    public void process(Replicator replicator, KeyValuePair keyValuePair);

}
