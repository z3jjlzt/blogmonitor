package com.kkk.blogmonitor.utils;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis 相关工具类
 * Created by z3jjlzt on 2018/7/23.
 */
@Slf4j
public class RedisUtil {

    private static JedisPool jedisPool;

    static {
        // init a jedispool，which is threadsafe.
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(poolConfig, "localhost");
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static void set(String k, String v) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set(k, v);
        } catch (Exception e) {
            log.error("set key {} value {} fail {}",k,v,e);
        }
    }

    public static void del(String k) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(k);
        } catch (Exception e) {
            log.error("set key {} fail {}",k,e);
        }
    }

    public static boolean exists(String k) {
        boolean result = false;
        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.exists(k);
        } catch (Exception e) {
            log.error("exists key {} fail {}",k,e);
        }
        return result;
    }

    public static void lPush(String k, String v) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(k, v);
        } catch (Exception e) {
            log.error("lpush key {} value {} fail {}",k,v,e);
        }
    }

    public static String lPop(String k) {
        String result = null;
        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.lpop(k);
        } catch (Exception e) {
            log.error("lpush key {} fail {}",k,e);
        }
        return result;
    }

    /**
     * 获取redis某个list列表的长度
     * @param k 列表名称
     * @return 列表长度，发生异常返回-1
     */
    public static long lLen(String k) {
        long llen;
        try (Jedis jedis = jedisPool.getResource()) {
            llen = jedis.llen(k);
        } catch (Exception e) {
            log.error("lLen key {} fail {}",k,e);
            return -1;
        }
        return llen;
    }

    public static String get(String k) {
        String result = null;
        try (Jedis jedis = jedisPool.getResource()) {
            result = jedis.get(k);
        } catch (Exception e) {
            log.error("get key {} fail {}",k,e);
        }
        return result;
    }

    public static void close() {
        try {
            jedisPool.close();
        } catch (Exception e) {
            log.error("jedis pool close fail {}",e);
        }
    }

}
