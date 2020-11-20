package com.practice.HA;

import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Random;

public class StartRcpSync {

    private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(StartRcpSync.class);
    private static AbstractApplicationContext appContext = null;
    private static String confPath = null;

    static{
        //后续来读取命令中的conf 例如 java -Dconf=conf/*.xml -classpath .:lib/*
        if(System.getProperty("conf") !=null){
            System.out.println(System.getProperty("user.dir"));
            confPath = System.getProperty("conf");
            System.out.println("读取配置路径conf目录："+confPath);
            appContext = new FileSystemXmlApplicationContext(confPath.concat("/applicationContext*.xml"));
        }else{
            confPath = "E:/aa/bb/src/main/resources/conf";
            appContext = new FileSystemXmlApplicationContext(confPath.concat("/applicationContext*.xml"));
        }
    }

    public static void main(String[] args) {
        Logger.info("Sync will starting ...");
        //加载配置文件
        appContext.registerShutdownHook();
        appContext.start();
        Logger.info("Sync has been started successfully.");
        //获取zookeeper的连接
        ZKConnector zkClient = new ZKConnector();
        DistributedLock dl = new DistributedLock(new Random().nextInt(),appContext,zkClient);
        dl.run();
    }

    //just for Test
    public static void DoTask(){
        Worker w =new Worker(appContext);
        w.doWork();
    }


}
