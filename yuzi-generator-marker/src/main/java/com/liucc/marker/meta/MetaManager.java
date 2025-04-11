package com.liucc.marker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

/**
 * 双检查锁单例
 */
public class MetaManager {
    private static volatile Meta meta;
    // 构造器私有化，防止外部实例化
    private MetaManager(){

    }
    public static Meta getMetaObject(){
        if(meta == null){
            synchronized (MetaManager.class){
                if(meta == null){
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    /**
     * 通过双检查锁机制初始化 Meta 对象
     * @return
     */
    public static Meta initMeta(){
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        Meta newMeta = JSONUtil.toBean(metaJson, Meta.class);
        return newMeta;
    }
}
