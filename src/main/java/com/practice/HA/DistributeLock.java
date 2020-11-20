package com.practice.HA;

import com.practice.common.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DistributedLock implements Watcher,Runnable{

    private static final Logger logger = LoggerFactory.getLogger(DistributedLock.class);
    private int threadId;
    private ZKConnector zkClient;
    private String selfPath;
    private String waitPath;
    private String LOG_PREFIX_OF_THREAD;
    private AbstractApplicationContext ctx;
    private static boolean hascreated = false;

    //确保连接zookeeper成功
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);
    //确保每个进程运行结束
    private static final CountDownLatch threadSemaphore = new CountDownLatch(Constant.THREAD_NUM);


    public ZKConnector getZkClient() {
        return zkClient;
    }
    public void setZkClient(ZKConnector zkClient) {
        this.zkClient = zkClient;
    }

    public DistributedLock(int id,AbstractApplicationContext context,ZKConnector zkClient){
        this.threadId = id;
        this.zkClient = zkClient;
        LOG_PREFIX_OF_THREAD = Thread.currentThread().getName().concat("_").concat(String.valueOf(Thread.currentThread().getId()));

        try{
            zkClient.createConnection(Constant.ZKSERVER, Constant.SESSION_TIMEOUT);
            //GROUP_PATH 不存在的话，由一个线程创建即可
            synchronized (threadSemaphore) {
                if(!zkClient.exist(Constant.GROUP_PATH)){
                    zkClient.createPersistNode(Constant.GROUP_PATH, "该节点由线程"+threadId+"创建");
                }
            }
            ctx = context;
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        getLock();
    }

    @Override
    public void process(WatchedEvent event) {
        if(event ==null){
            return;
        }
        if(Event.KeeperState.SyncConnected ==event.getState()){
            if(Event.EventType.None == event.getType()){
                connectedSemaphore.countDown();
            }else if(event.getType()== Event.EventType.NodeDeleted && event.getPath().equals(waitPath)){
                if(checkMinPath()){
                    getLockSuccess();
                }
            }

        }
    }

    /**
     * 获取锁逻辑：
     * 首先是上来先在zookeeper上注册一把属于自己的锁，然后修改状态为已创建
     * 第二步，检查自己是否是最小id的锁，若是则获取锁，不是则继续等待
     */
    private void getLock(){
        if(!hascreated){
            selfPath = this.getZkClient().createEsquentialNode(Constant.SUB_PATH, "");
            hascreated = true;
        }
        if(checkMinPath()){
            getLockSuccess();
        }else{
            Executor.run(this, 1, 1, TimeUnit.SECONDS);
        }

    }

    /**
     * 检查自己是不是最小路径
     * @return
     */
    public boolean checkMinPath(){
        List<String> subNodes = this.getZkClient().getChildren(Constant.GROUP_PATH);
        Collections.sort(subNodes);
        //查找"/syncLocks"后面的路径
        int index = subNodes.indexOf(selfPath.substring(Constant.GROUP_PATH.length()+1));
        switch(index){
            case -1:{
                return false;
            }
            case 0:{
                return true;
            }
            default:{
                this.waitPath = Constant.GROUP_PATH+"/"+subNodes.get(index-1);
                //Logger.info("waitPath: "+waitPath);
                this.getZkClient().readData(waitPath);
                if(!this.getZkClient().exist(waitPath)){
                    return checkMinPath();
                }
            }
        }
        return false;
    }

    /**
     * 获取锁成功
     */
    public void getLockSuccess(){
        if(!this.getZkClient().exist(selfPath)){
            logger.error(LOG_PREFIX_OF_THREAD+"本节点已不存在.");
            return;
        }
        logger.info(LOG_PREFIX_OF_THREAD + "获取锁成功,进行同步工作！");

        try{
            new Worker(ctx).doWork();
        }catch(Exception ex){
            logger.info(ex.getMessage());
            Executor.run(this, 1, 1, TimeUnit.SECONDS);
            return;
        }

        logger.info(LOG_PREFIX_OF_THREAD+"删除本节点："+selfPath);
        this.getZkClient().deleteNode(selfPath);
        this.getZkClient().releaseConnection();
        threadSemaphore.countDown();
    }


}