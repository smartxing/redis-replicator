package com.liangbo.xing.redisreplicator.back;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandListener;
import com.moilioncircle.redis.replicator.io.RawByteListener;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * commond备份
 *
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午5:55 xingliangbo Exp $
 */
public class CommandBackup {

    private Logger LOGGER = LoggerFactory.getLogger(CommandBackup.class);


    public void backupAof(String redisPath, String aofPath) throws Exception {
        File file = new File(aofPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        final RawByteListener rawByteListener = new RawByteListener() {
            @Override
            public void handle(byte... rawBytes) {
                try {
                    out.write(rawBytes);
                } catch (IOException ignore) {
                }
            }
        };

        Replicator replicator = new RedisReplicator(redisPath);
        replicator.addRdbListener(new RdbListener() {
            @Override
            public void preFullSync(Replicator replicator) {
            }

            @Override
            public void handle(Replicator replicator, KeyValuePair<?> kv) {
            }

            @Override
            public void postFullSync(Replicator replicator, long checksum) {
                replicator.addRawByteListener(rawByteListener);
            }
        });

        final AtomicInteger acc = new AtomicInteger(0);
        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                if (acc.incrementAndGet() == 1000) {
                    try {
                        out.close();
                        replicator.close();
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
            }
        });
        replicator.open();
    }


}
