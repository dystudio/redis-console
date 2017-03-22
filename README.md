# redis-console
Demo地址:http://www.wanghe.space/redis/

redis web数据管理,使用jdk8开发,支持集群,哨兵,单机等模式<br/>
有bug和意见邮件请联系 178070584@qq.com

修改redis.properties <br/>
单机->stand.alone=ip:port  密码: stand.pass=密码 没有密码为空<br/>
哨兵->master.name=masterName sentinel=哨兵ip:哨兵port;哨兵ip:哨兵port  密码:sentinel.pass<br/>
集群->cluster=ip:port;ip:port 密码:redis.pass

打包 <br/>
进入项目根目录，使用 mvn clean package 命令打包<br/>
mvn clean package <br/>
打包后的文件存放于项目下的target目录中，如：redis-console-1.0-SNAPSHOT.jar<br/>
启动<br/>
通过命令java -jar target/redis-console-1.0-SNAPSHOT.jar运行程序<br/>
java -jar target/redis-console-1.0-SNAPSHOT.jar  --name="Spring" --server.port=8080 
