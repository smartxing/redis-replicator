# redis-replicator
 redis 数据迁移，双机房同步
# redis 主重复制原理 熟悉一下概念
## RDB
### 原理
## AOF
### 原理

## redis 主从复制
![Alt text](https://github.com/smartxing/imageflod/blob/master/redisfullsync.png)
1 当启动一个slave node的时候，它会发送一个PSYNC命令给master node    
2 如果这是slave node重新连接master node，那么master node仅仅会复制给slave部分缺少的数据; 否则如果是slave node第一次连接master node，那么会触发一次full resynchronization    
3 开始full resynchronization的时候，master会启动一个后台线程，开始生成一份RDB快照文件，同时还会将从客户端收到的所有写命令缓存在内存中。RDB文件生成完毕之后，master会将这个RDB发送给slave，slave会先写入本地磁盘，然后再从本地磁盘加载到内存中。然后master会将内存中缓存的写命令发送给slave，slave也会同步这些数据。    
4 slave node如果跟master node有网络故障，断开了连接，会自动重连。master如果发现有多个slave node都来重新连接，仅仅会启动一个rdb save操作，用一份数据服务所有slave node。    
## 什么时候发生全量同步，什么时候发生增量同步。 比如做数据迁移的时候需要考虑一下，如果在数据迁移中发生了重连，全量rdb重新会同步一份    
1.redis什么时候会发生全量复制     
>> a) redis slave首启动或者重启后，连接到master时    
>> b) redis slave进程没重启，但是掉线了，重连后不满足部分复制条件    

2.部分复制需要的条件    
>> a) 主从的redis版本>=2.8     
>> b) redis slave进程没有重启，但是掉线了，重连了master(因为slave进程重启的话，run id就没有了)    
>> c) redis slave保存的run id与master当前run id一致 (注：run id并不是pid，slave把它保存在内存中，重启就消失)    
>> d) redis slave掉线期间，master保存在内存的offset可用，也就是master变化不大，被更改的指令都保存在内存    

## 读写分离




