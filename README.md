# redis-replicator
 redis 数据迁移，双机房同步
# redis 主重复制原理 熟悉一下概念
## RDB
### 原理
2、RDB
     
     A、RDB方式是Redis默认采用的持久化方式，通过快照（snapshotting）完成的。当符合一定条件时Redis会自动将内存中的所有数据进行快照并存储在整个硬盘上。进行快照的条件：时间和改动的键的个数。当在指定的时间内被更改的键的个数大于指定的数值时就会进行快照。
     
     B、配置文件中已经预先设置了3个条件：        
          save 900 1        
          save 300 10       
          save 60 10000

     C、save参数指定了快照条件，可以存多个条件，条件之间是“或”的关系。 如上，在900秒内至少一个键被更改就进行快照。时间单位是秒

     D、Redis默认会将快照文件存储在当前目录的dump.rdb文件中，也可以通过 dir 和 dbfilename 两个参数分别指定快照文件的存储路径和文件名。

     E、Redis实现快照的过程：
          1）Redis使用fork函数复制一份当前进程（父进程）的副本（子进程）
     
          2）父进程继续接受并处理客户端发来的命令，而子进程开始将内存中的数据写入硬盘中的临时文件

          3）当子进程写入完所有数据后，会用该临时文件替换旧的RDB文件，至此一次快照操作完成。

     F、在执行fork的时候操作系统会使用写时复制（copy-on-write）策略，即fork()函数发生的那一刻父子进程共享同一内存空间，当父进程要更改某一片的数据时（如执行写命令），操作系统会将该片数据复制一份以保证子进程的数据不受影响，所以新的RDB文件存储的是fork()那一刻的内存数据。

     G、Redis是在快照结束后才会将旧的RDB文件替换成新的，也就是说任何时候RDB文件都是完整的。这时我们就可以通过备份RDB文件来实现Redis数据的备份。
     
     H、RDB文件是经过压缩的二进制格式，所以占用的空间小于内存中的数据大小，更加利于传输。也可以设置redcompression参数以禁用压缩。

     I、手动Redis执行快照命令：SAVE \ BGSAVE ,区别是：前者是由主进程进行快照操作，会阻塞住其他请求，后者会通过fork子进程进行快照操作。
     
     J、Redis启动后会读取RDB快照文件，将数据从硬盘载入内存。

     K、RDB方式持久化数据，一旦Redis异常退出，就会失去最后一次快照以后更改的数据。如果数据很重要以至于无法承受任何损失，可以使用AOF进行持久化。    
## AOF
### 原理

    AOF方式(Append Only File)     
    当使用redis存储非临时数据时，一般都需要打开AOF持久化来降低进程中导致的数据丢失。aof可以将redis执行的每一条命令追加到硬盘文件中，这一过程会降低redis的性能。
    开启AOF
    redis默认情况下没有开启AOF方式的持久化，可以通过appendonly参数启用：
    appendonly yes
    开启AOF持久化后每执行一条会更改redis 中的数据的命令，redis就会将该命令写入到硬盘中的AOF文件。AOF文件的保存位置和RDB文件的位置相同，都是通过dir参数设置的，默认的文件名是appendonly.aof，可以通过appendfilename参数修改：appendfilename  appendonly.aof
    AOF的实现  
    AOF文件以纯文本的形式记录了redis执行的写命令。
    AOF文件的内容正是redis客户端向redis 发送的原始通信协议的内容。每当达到一定条件时redis就会自动重写AOF文件，这个条件可以在配置文件中设置：
    auto-aof-rewrite-percentage 100
    auto-aof-rewrite-min-size      64mb
    auto-aof-rewrite-percentage 当目前超过的AOF文件上一次重写时的AOF文件大小的百分之多少时会再次进行重写。auto-aof-rewrite-min-size 限制了允许重写的最小AOF文件大小。
    BGREWRITEAOF命令手动执行AOF重写。
    同步硬盘数据
    虽然每次执行更改数据库内容的操作时，AOF都会将命令记录在AOF文件中，但由于操作系统的缓存机制，数据并没有真正的写入硬盘，而是进入了系统的硬盘缓存。默认情况下系统每30秒会执行一次同步操作，以便将硬盘缓存中的内容真正的写入硬盘。
    redis在写入AOF文件后主动要求系统将缓存内容同步到硬盘中
    #appendfsync always    ​每次执行写入 都会执行同步
    appendfsync everysec 每秒执行一次同步操作
    #appendfsync no  不主动进行同步操作，完全交由操作系统来做(即每30秒一次​)



## redis 主从复制
### 基本流程
![Alt text](https://github.com/smartxing/imageflod/blob/master/redisfullsync.png)

### 3次握手
![Alt text](https://github.com/smartxing/imageflod/blob/master/redissync2.png)
>> 1 当启动一个slave node的时候，它会发送一个PSYNC命令给master node    
>> 2 如果这是slave node重新连接master node，那么master node仅仅会复制给slave部分缺少的数据; 否则如果是slave node第一次连接master node，那么会触发一次full resynchronization    
>> 3 开始full resynchronization的时候，master会启动一个后台线程，开始生成一份RDB快照文件，同时还会将从客户端收到的所有写命令缓存在内存中。RDB文件生成完毕之后，master会将这个RDB发送给slave，slave会先写入本地磁盘，然后再从本地磁盘加载到内存中。然后master会将内存中缓存的写命令发送给slave，slave也会同步这些数据。    
>> 4 slave node如果跟master node有网络故障，断开了连接，会自动重连。master如果发现有多个slave node都来重新连接，仅仅会启动一个rdb save操作，用一份数据服务所有slave node。    
## 什么时候发生全量同步，什么时候发生增量同步。 比如做数据迁移的时候需要考虑一下，如果在数据迁移中发生了重连，全量rdb重新会同步一份    
1.redis什么时候会发生全量复制     
>> a) redis slave首启动或者重启后，连接到master时    
>> b) redis slave进程没重启，但是掉线了，重连后不满足部分复制条件    

2.部分复制需要的条件    
>> a) 主从的redis版本>=2.8     
>> b) redis slave进程没有重启，但是掉线了，重连了master(因为slave进程重启的话，run id就没有了)    
>> c) redis slave保存的run id与master当前run id一致 (注：run id并不是pid，slave把它保存在内存中，重启就消失)    
>> d) redis slave掉线期间，master保存在内存的offset可用，也就是master变化不大，被更改的指令都保存在内存    
#### 实践
##### 读写分离，redis通常用来做缓存，所以通过redis 复制策略，可以写主读从，减少master压力
##### 主数据库禁止备份，从数据库备份，提升master性能， master挂掉后从slave提升为主  
##### 注意 不要直接操作master,不然master会把空的文件同步到slave，那样可能会造成数据全部丢失

#### 上述是原理部分，下面开始设计下功能
# 系统功能设计
## 整体架构

![Alt text](https://github.com/smartxing/imageflod/blob/master/dcsysnc.png)


## 主要难点就是 解析RDB，解析命令 这个有大牛网上已经开源了，所以说这就没有什么难度了
## 大牛地址：https://github.com/leonchen83/redis-replicator  基本都能满足需求了 感谢大牛
### 全量RDB同步,增量commond同步
```java

        RdbDistribute rdbDistribute = new RdbDistribute();
        rdbDistribute.addCommonParserListener(new CommonParserListener() {
            @Override
            public void process(Replicator replicator, KeyValuePair keyValuePair) {
                //commond 解析
            }
        });
        
        rdbDistribute.addRdbParserListeners(new RdbParserListener() {
            @Override
            public void process(Replicator replicator, Command command) {
                //rdb 解析
            }
        });
```

### 定期备份
```java
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
```

