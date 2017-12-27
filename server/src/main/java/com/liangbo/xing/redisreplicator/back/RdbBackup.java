package com.liangbo.xing.redisreplicator.back;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.io.RawByteListener;
import com.moilioncircle.redis.replicator.rdb.RdbListener;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.skip.SkipRdbVisitor;

import java.io.*;
import java.net.URISyntaxException;

/**
 * rdb备份
 *
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午5:32 xingliangbo Exp $
 */
public class RdbBackup {

    public void backUPRdb(String redisPath, String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        Replicator replicator;
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            RawByteListener rawByteListener = new RawByteListener() {
                @Override
                public void handle(byte... rawBytes) {
                    try {
                        out.write(rawBytes);
                    } catch (IOException ignore) {
                    }
                }
            };
            replicator = new RedisReplicator(redisPath);
            replicator.setRdbVisitor(new SkipRdbVisitor(replicator));
            replicator.addRdbListener(new RdbListener() {
                @Override
                public void preFullSync(Replicator replicator) {
                    replicator.addRawByteListener(rawByteListener);
                }

                @Override
                public void handle(Replicator replicator, KeyValuePair<?> kv) {
                }

                @Override
                public void postFullSync(Replicator replicator, long checksum) {
                    replicator.removeRawByteListener(rawByteListener);
                    try {
                        out.close();
                        replicator.close();
                    } catch (IOException ignore) {
                    }
                }
            });
            replicator.open();
        }

    }

}
