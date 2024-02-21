package site.duqian.spring.manager;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * ThreadManager:简易的线程池管理类
 *
 * @author Dusan-杜乾 Created on 2017/6/13 - 13:46.
 * E-mail:duqian2010@gmail.com
 */
public class ThreadManager {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ThreadManager.class);// slf4j日志记录器

    private static ThreadPoolProxy mBackgroundPool = null;
    private static final Object mBackgroundLock = new Object();
    private static ThreadPoolProxy mDownloadPool = null;
    private static final Object mDownloadLock = new Object();
    private static Map<String, ThreadPoolProxy> mMap = new HashMap<>();
    private static final Object mSingleLock = new Object();
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();//可用的CPU数

    /**
     * 获取后台线程池,核心线程会一直存活。
     * CPU密集型任务配置尽可能少的线程数量：CPU核数+1个线程的线程池
     */
    public static ThreadPoolProxy getBackgroundPool() {
        synchronized (mBackgroundLock) {
            if (mBackgroundPool == null) {
                int corePoolSize = CPU_COUNT > 2 ? CPU_COUNT : 2;
                logger.debug("dq-corePoolSize1=" + corePoolSize);
                mBackgroundPool = new ThreadPoolProxy(corePoolSize + 1, Integer.MAX_VALUE, 60L, false);
            }
            return mBackgroundPool;
        }
    }

    /**
     * 获取一个用于文件并发下载的线程池
     * 修改核心线程数和最大线程数:
     * IO密集型任务线程并不是一直在执行任务，则应配置尽可能多的线程，CPU核数*2
     */
    public static ThreadPoolProxy getDownloadPool() {
        synchronized (mDownloadLock) {
            if (mDownloadPool == null) {
                int cpu = Math.max(CPU_COUNT, 1);
                int corePoolSize = cpu * 2;
                logger.debug("dq-corePoolSize2 cpu=" + cpu + "，corePoolSize=" + corePoolSize);
                mDownloadPool = new ThreadPoolProxy(corePoolSize, corePoolSize * 2, 60L, true);
            }
            return mDownloadPool;
        }
    }

    /**
     * 获取一个单线程池，所有任务将会被按照加入的顺序执行，免除了同步开销的问题
     */
    public static ThreadPoolProxy getSinglePool(String name) {
        synchronized (mSingleLock) {
            ThreadPoolProxy singlePool = mMap.get(name);
            if (singlePool == null) {
                singlePool = new ThreadPoolProxy(0, 1, 60L, false);
                mMap.put(name, singlePool);
            }
            return singlePool;
        }
    }

    public static class ThreadPoolProxy {
        private ThreadPoolExecutor mPool;
        private int mCorePoolSize;
        private int mMaximumPoolSize;
        private long mKeepAliveTime;
        private boolean mIsPriority;

        /**
         * @param corePoolSize    核心线程数量
         * @param maximumPoolSize 最大线程数量
         * @param keepAliveTime   空闲线程存活时间，秒
         */
        private ThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime, boolean isPriority) {
            mCorePoolSize = corePoolSize;
            mMaximumPoolSize = maximumPoolSize;
            mKeepAliveTime = keepAliveTime;
            mIsPriority = isPriority;
        }

        /**
         * 执行任务，当线程池处于关闭，将会重新创建新的线程池
         */
        public synchronized void execute(Runnable run) {
            if (run == null) {
                return;
            }
            if (mPool == null || mPool.isShutdown()) {
                //ThreadFactory是每次创建新的线程工厂
                if (mIsPriority) {//使用优先级队列
                    mPool = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAliveTime, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new AbortPolicy());
                } else {//队列任务
                    mPool = new ThreadPoolExecutor(mCorePoolSize, mMaximumPoolSize, mKeepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(), new AbortPolicy());
                }
            }
            mPool.execute(run);
        }

        /**
         * 取消线程池中某个还未执行的任务
         */
        public synchronized void remove(Runnable run) {
            if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
                mPool.getQueue().remove(run);
            }
        }

        /**
         * 是否包含某个任务
         */
        public synchronized boolean contains(Runnable run) {
            if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
                return mPool.getQueue().contains(run);
            } else {
                return false;
            }
        }

        /**
         * 关闭线程池，
         *
         * @param isNow if true 立即终止线程池，并尝试打断正在执行的任务，清空任务缓存队列，返回尚未执行的任务。
         *              if false ,确保所有已经加入的任务都将会被执行完毕才关闭,后面不接受任务
         **/
        public synchronized void shutdown(boolean isNow) {
            if (mPool != null && (!mPool.isShutdown() || mPool.isTerminating())) {
                if (isNow) {
                    mPool.shutdownNow();
                } else {
                    mPool.shutdown();
                }
            }
        }
    }
}