package com.liangbo.xing.redisreplicator.schedule;

import com.liangbo.xing.redisreplicator.back.CommandBackup;
import com.liangbo.xing.redisreplicator.back.RdbBackup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author xingliangbo
 * @version $Id: v 0.1 17/12/27 下午9:00 xingliangbo Exp $
 */
@EnableScheduling
@Configuration
public class ScheduleBackup {

    private Logger logger = LoggerFactory.getLogger(ScheduleBackup.class);

    @Autowired
    private Environment env;

    @Scheduled(cron = "0 0 12 * * ?")
    public void rdbBackUp() {
        RdbBackup rdbBackup = new RdbBackup();
        try {
            rdbBackup.backUPRdb(env.getProperty("", ""), env.getProperty("", ""));
        } catch (Exception e) {
            logger.error("back up error", e);
        }
    }


    @Scheduled(cron = "0 0 12 * * ?")
    public void aofBackUp() {
        CommandBackup rdbBackup = new CommandBackup();
        try {
            rdbBackup.backupAof(env.getProperty("", ""), env.getProperty("", ""));
        } catch (Exception e) {
            logger.error("back up error", e);
        }
    }

}
