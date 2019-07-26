package com.xw.test.util;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

/**
 * Created on 2019/7/25 14:20
 * 单台redis  不分片，没集群
 */
@Component
public class RedisUtil {

    private static final Logger log = Logger.getLogger(RedisUtil.class);

    private static JedisPool jedisPool = null;

    private static final String LOCK_SUCCESS = "OK";

    private static final String SET_IF_NOT_EXIST = "NX";

    private static final String SET_WITH_EXPIRE_TIME = "PX";

    private static final Long RELEASE_SUCCESS = 1L;

    /**
     * 初始化Redis连接池
     */
    static {
        try {
            // 加载redis配置文件
            ResourceBundle bundle = ResourceBundle.getBundle("redis");
            if (bundle == null) {
                throw new IllegalArgumentException("配置文件加载失败");
            }
            int maxActivity = Integer.valueOf(bundle.getString("redis.pool.maxActive"));
            int maxIdle = Integer.valueOf(bundle.getString("redis.pool.maxIdle"));
            long maxWait = Long.valueOf(bundle.getString("redis.pool.maxWait"));
            boolean testOnBorrow = Boolean.valueOf(bundle.getString("redis.pool.testOnBorrow"));
            boolean onreturn = Boolean.valueOf(bundle.getString("redis.pool.testOnReturn"));

            // 创建jedis池配置实例
            JedisPoolConfig config = new JedisPoolConfig();
            // 设置池配置项值
            config.setMaxTotal(maxActivity);
            config.setMaxIdle(maxIdle);
            config.setMaxWaitMillis(maxWait);
            config.setTestOnBorrow(testOnBorrow);
            config.setTestOnReturn(onreturn);

            jedisPool = new JedisPool(config, bundle.getString("redis.ip"), Integer.valueOf(bundle.getString("redis.port")), 10000, bundle.getString("redis.password"));

            log.info("初始化Redis连接池success");

        } catch (Exception e) {
            log.error("初始化Redis连接池 出错！", e);
        }
    }

    /**
     * 获取Jedis实例
     *
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                //todo 可以抛个异常出来
                return null;
            }
        } catch (Exception e) {
            log.error("Redis缓存获取Jedis实例 出错！", e);
            return null;
        }
    }


    /**
     * 向缓存中设置字符串内容
     *
     * @param key   key
     * @param value value
     * @return
     * @throws Exception
     */
    public static boolean set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis缓存设置key值 出错！", e);
            return false;
        }
    }

    /**
     * 判断key是否存在
     */
    public static boolean exists(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(key);
        } catch (Exception e) {
            log.error("Redis缓存判断key是否存在 出错！", e);
            return false;
        }
    }

    /**
     * 删除缓存中的对象，根据key
     *
     * @param key
     * @return
     */
    public static boolean del(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.del(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    //*******************key-value****************start

    /**
     * 向缓存中设置对象
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean set(String key, Object value) {
        try (Jedis jedis = getJedis()) {
            String objectJson = JSON.toJSONString(value);
            jedis.set(key, objectJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取内容
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        try (Jedis jedis = getJedis()) {
            Object value = jedis.get(key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取对象
     *
     * @param key
     * @return
     */
    public static <T> T get(String key, Class<T> clazz) {
        try (Jedis jedis = getJedis()) {
            return JSON.parseObject(jedis.get(key), clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //*******************key-value****************end

    //*************** 操作list****************start

    /**
     * 向缓存中设置对象
     *
     * @param key
     * @param list T string calss
     * @return
     */
    public static <T> boolean setList(String key, List<T> list) {
        try (Jedis jedis = getJedis()) {
            for (T vz : list) {
                if (vz instanceof String) {
                    jedis.lpush(key, (String) vz);
                } else {
                    String objectJson = JSON.toJSONString(vz);
                    jedis.lpush(key, objectJson);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static <T> List<T> getListEntity(String key, Class<T> entityClass) {
        try (Jedis jedis = getJedis()) {
            List<String> valueJson = jedis.lrange(key, 0, -1);
            String json = JSON.toJSONString(valueJson);
            return JSON.parseArray(json, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getListString(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.lrange(key, 0, -1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //*************** 操作list****************end

    //*************** 操作map****************start
    public static <K, V> boolean setMap(String key, Map<String, V> map) {
        try (Jedis jedis = getJedis()) {
            Set<Map.Entry<String, V>> entry = map.entrySet();
            for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext(); ) {
                Map.Entry<String, V> kv = ite.next();
                if (kv.getValue() instanceof String) {
                    jedis.hset(key, kv.getKey(), (String) kv.getValue());
                } else if (kv.getValue() instanceof List) {
                    jedis.hset(key, kv.getKey(), JSON.toJSONString(kv.getValue()));
                } else {
                    jedis.hset(key, kv.getKey(), JSON.toJSONString(kv.getValue()));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setMapKey(String key, String mapKey, Object value) {
        try (Jedis jedis = getJedis()) {
            if (value instanceof String) {
                jedis.hset(key, mapKey, String.valueOf(value));
            } else {
                jedis.hset(key, mapKey, JSON.toJSONString(value));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * seconds key和value 保存的有效时间（单位：秒）
     *
     * @return
     */
    public static boolean setMapKeyExpire(String key, String mapKey, Object value, int seconds) {
        try (Jedis jedis = getJedis()) {
            if (value instanceof String) {
                jedis.hset(key, mapKey, String.valueOf(value));
            } else {
                jedis.hset(key, mapKey, JSON.toJSONString(value));
            }
            jedis.expire(key, seconds);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <K, V> Map<String, V> getMap(String key) {
        try (Jedis jedis = getJedis()) {
            Map<String, V> map = (Map<String, V>) jedis.hgetAll(key);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <K, V> Map<String, V> getMapEntityClass(String key, Class<V> clazz) {
        try (Jedis jedis = getJedis()) {
            Map<String, V> map = (Map<String, V>) jedis.hgetAll(key);
            Set<Map.Entry<String, V>> entry = map.entrySet();
            for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext(); ) {
                Map.Entry<String, V> kv = ite.next();
                map.put(kv.getKey(), JSON.parseObject(JSON.toJSONString(kv.getValue()), clazz));
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <K, V> Map<String, List<V>> getMapList(String key, Class<V> clazz) {
        try (Jedis jedis = getJedis()) {
            Map<String, V> map = (Map<String, V>) jedis.hgetAll(key);
            Set<Map.Entry<String, V>> entry = map.entrySet();
            for (Iterator<Map.Entry<String, V>> ite = entry.iterator(); ite.hasNext(); ) {
                Map.Entry<String, V> kv = ite.next();
                String json = JSON.toJSONString(kv.getValue());
                map.put(kv.getKey(), (V) JSON.parseArray(json, clazz));
            }
            return (Map<String, List<V>>) map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> getMapKeyListEntity(String key, String mapKey,
                                                  Class<T> entityClass) {
        try (Jedis jedis = getJedis()) {
            String valueJson = jedis.hget(key, mapKey);
            return JSON.parseArray(valueJson, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T getMapKeyEntity(String key, String mapKey,
                                        Class<T> entityClass) {
        try (Jedis jedis = getJedis()) {
            String valueJson = jedis.hget(key, mapKey);
            return JSON.parseObject(valueJson, entityClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getMapKey(String key, String mapKey) {
        try (Jedis jedis = getJedis()) {
            return jedis.hget(key, mapKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean delMapKey(String key, String mapKey) {
        try (Jedis jedis = getJedis()) {
            jedis.hdel(key, mapKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hexists(String key, String mapKey) {
        try (Jedis jedis = getJedis()) {
            return jedis.hexists(key, mapKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //*************** 操作map****************end

    //***************计数器应用INCR,DECR****************begin
    //Redis的命令都是原子性的，你可以轻松地利用INCR，DECR命令来构建计数器系统

    /**
     * incr(key)：名称为key的string增1操作
     */
    public static boolean incr(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.incr(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * incrby(key, integer)：名称为key的string增加integer
     */
    public static boolean incrBy(String key, int value) {
        try (Jedis jedis = getJedis()) {
            jedis.incrBy(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * decr(key)：名称为key的string减1操作
     */
    public static boolean decr(String key) {
        try (Jedis jedis = getJedis()) {
            jedis.decr(key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * decrby(key, integer)：名称为key的string减少integer
     */
    public static boolean decrBy(String key, int value) {
        try (Jedis jedis = getJedis()) {
            jedis.decrBy(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //***************计数器应用INCR,DECR****************end

    //***************使用sorted set(zset)甚至可以构建有优先级的队列系统***************begin

    /**
     * 向名称为key的zset中添加元素member，score用于排序。
     * 如果该元素已经存在，则根据score更新该元素的顺序
     */
    public static boolean zadd(String key, double score, String member) {
        try (Jedis jedis = getJedis()) {
            jedis.zadd(key, score, member);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除名称为key的zset中的元素member
     */
    public static boolean zrem(String key, String... members) {
        try (Jedis jedis = getJedis()) {
            jedis.zrem(key, members);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 返回集合中score在给定区间的元素
     */
    public static Set<String> zrangeByScore(String key, double min, double max) {
        try (Jedis jedis = getJedis()) {
            return jedis.zrangeByScore(key, min, max);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //***************使用sorted set(zset)甚至可以构建有优先级的队列系统***************end

    //***************sorted set 处理***************************************begin
    //zset 处理
    public static boolean zaddObject(String key, double score, Object value) {
        try (Jedis jedis = getJedis()) {
            String objectJson = JSON.toJSONString(value);
            jedis.zadd(key, score, objectJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * score值递减(从大到小)次序排列。
     *
     * @param key
     * @param max         score
     * @param min         score
     * @param entityClass
     * @return
     */
    public static <T> List<T> zrevrangeByScore(String key, double max, double min,
                                               Class<T> entityClass) {
        try (Jedis jedis = getJedis()) {
            Set<String> set = jedis.zrevrangeByScore(key, max, min);
            List<T> list = new ArrayList();
            for (String str : set) {
                list.add(JSON.parseObject(str, entityClass));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * score值递减(从大到小)次序排列。
     *
     * @param key
     * @param max         score
     * @param min         score
     * @param offset      count (类似mysql的 LIMIT)
     * @param entityClass
     * @return
     */
    public static <T> List<T> zrevrangeByScore(String key, double max, double min,
                                               int offset, int count, Class<T> entityClass) {
        try (Jedis jedis = getJedis()) {
            Set<String> set = jedis.zrevrangeByScore(key, max, min, offset, count);
            List<T> list = new ArrayList();
            for (String str : set) {
                list.add(JSON.parseObject(str, entityClass));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //得到总记录数
    public static long zcard(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.zcard(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //删除 元素
    public static boolean zremObject(String key, Object value) {
        try (Jedis jedis = getJedis()) {
            String objectJson = JSON.toJSONString(value);
            jedis.zrem(key, objectJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //统计zset集合中score某个范围内（1-5），元素的个数
    public static long zcount(String key, double min, double max) {
        try (Jedis jedis = getJedis()) {
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //查看zset集合中元素的score
    public static double zscore(String key, Object value) {
        try (Jedis jedis = getJedis()) {
            String objectJson = JSON.toJSONString(value);
            return jedis.zscore(key, objectJson);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    //**************sorted set******************************************end

    //***********************Redis Set集合操作**************************begin

    /**
     * sadd:向名称为Key的set中添加元素,同一集合中不能出现相同的元素值。（用法：sadd set集合名称 元素值）
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean sadd(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.sadd(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * srem:删除名称为key的set中的元素。（用法：srem set集合名称 要删除的元素值）
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean srem(String key, String value) {
        try (Jedis jedis = getJedis()) {
            jedis.srem(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * sdiff:返回所有给定key与第一个key的差集。（用法：sdiff set集合1 set集合2）
     *
     * @param key1
     * @param key2
     * @return
     */
    public static Set<String> sdiff(String key1, String key2) {
        Set<String> diffList = new HashSet<>();
        try (Jedis jedis = getJedis()) {
            diffList = jedis.sdiff(key1, key2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diffList;
    }

    /**
     * sismember:判断某个值是否是集合的元素。（用法：sismember 集合1 指定的元素值）
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean sismember(String key, String value) {
        try (Jedis jedis = getJedis()) {
            return jedis.sismember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * smembers(key) ：返回名称为key的set的所有元素
     *
     * @param key
     * @return
     */
    public static Set<String> smembers(String key) {
        Set<String> list = new HashSet<>();
        try (Jedis jedis = getJedis()) {
            list = jedis.smembers(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //***********************Redis Set集合操作****************************end

    //***********************Redis 分布式锁****************************start

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁
     * @param requestId  请求标识
     * @param expireTime 过期时间
     * @return 是否获取成功
     */
    public static boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {

        try (Jedis jedis = getJedis()) {
            String script = "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then redis.call('pexpire', KEYS[1], ARGV[2]) return 1 else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Arrays.asList(requestId, String.valueOf(expireTime)));
            //jedis 3.x不支持五个参数的set
            //String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * 释放分布式锁
     *
     * @param lockKey   锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(String lockKey, String requestId) {

        try (Jedis jedis = getJedis()) {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //***********************Redis 分布式锁****************************end
}
