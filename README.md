# redis-console
redis web数据管理,使用jdk8开发,支持集群,哨兵,单机等模式<br/>
有bug和意见邮件请联系 178070584@qq.com

修改redis.properties <br/>
单机->stand.alone=ip:port  密码: stand.pass=密码 没有密码为空<br/>
哨兵->master.name=masterName sentinel=哨兵ip:哨兵port;哨兵ip:哨兵port  密码:sentinel.pass<br/>
集群->cluster=ip:port;ip:port 密码:redis.pass
