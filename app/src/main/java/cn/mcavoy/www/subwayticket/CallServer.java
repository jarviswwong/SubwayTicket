package cn.mcavoy.www.subwayticket;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;

//封装单例队列

public class CallServer {
    private static CallServer instance;

    /**
     * 请求队列。
     */
    private RequestQueue requestQueue;

    private CallServer() {
        requestQueue = NoHttp.newRequestQueue(3);
    }

    /**
     * 请求队列。
     */
    public synchronized static CallServer getInstance() {
        if (instance == null)
            synchronized (CallServer.class) {
                if (instance == null)
                    instance = new CallServer();
            }
        return instance;
    }

    /**
     * 添加一个请求到请求队列。
     *
     * @param what     用来标志请求, 当多个请求使用同一个Listener时, 在回调方法中会返回这个what。
     * @param request  请求对象。
     * @param listener 结果回调对象。
     */
    public <T> void add(int what, Request<T> request, OnResponseListener listener) {
        requestQueue.add(what, request, listener);
    }

    /**
     * 取消这个sign标记的所有请求。
     *
     * @param sign 请求的取消标志。
     */
    public void cancelBySign(Object sign) {
        requestQueue.cancelBySign(sign);
    }

    /**
     * 取消队列中所有请求。
     */
    public void cancelAll() {
        requestQueue.cancelAll();
    }
}
