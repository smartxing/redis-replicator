package redisreplicator;

import com.liangbo.xing.redisreplicator.back.RdbBackup;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午7:56 xingliangbo Exp $
 */
public class RdbBackupTest {

    @Test
    public void test() throws Exception {
        RdbBackup rdbBackup = new RdbBackup();
        rdbBackup.backUPRdb("redis://127.0.0.1:6379", "");
    }
}
