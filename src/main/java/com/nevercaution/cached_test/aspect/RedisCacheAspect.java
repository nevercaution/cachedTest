package com.nevercaution.cached_test.aspect;

import com.google.common.base.Joiner;
import com.nevercaution.cached_test.annotation.RedisCached;
import com.nevercaution.cached_test.annotation.RedisCachedKeyParam;
import com.nevercaution.cached_test.config.RedisDB;
import com.nevercaution.cached_test.model.RedisCacheParameterMethodInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Component
@Aspect
public class RedisCacheAspect {
    private static Map<String, RedisCacheParameterMethodInfo> cacheParameterMethodInfoMap = new HashMap<>();

    @Autowired
    private RedisDB redisDB;

    @Around(value = "execution(* *(..)) && @annotation(redisCached)")
    public Object aroundAspect(ProceedingJoinPoint joinPoint, RedisCached redisCached) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();

        Class returnType = methodSignature.getReturnType();

        String key = redisCached.key();
        int expire = redisCached.expire();
        boolean replace = redisCached.replace();


        // reflect caching
        List<String> parameterKeyList = new ArrayList<>();
        Object[] args = joinPoint.getArgs();
        RedisCacheParameterMethodInfo methodInfo = cacheParameterMethodInfoMap.get(methodName);
        if (methodInfo != null) {
            List<RedisCacheParameterMethodInfo.IndexInfo> indexInfoList = methodInfo.getIndexInfoList();
            indexInfoList.forEach(info ->
                    parameterKeyList.add(makeCacheKey(info.getAnnotation(), args[info.getIndex()].toString())));

        } else {
            methodInfo = new RedisCacheParameterMethodInfo();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof RedisCachedKeyParam) {
                        RedisCachedKeyParam keyParam = (RedisCachedKeyParam)annotation;
                        parameterKeyList.add(makeCacheKey(keyParam, args[i].toString()));
                        methodInfo.addInfo(keyParam, i);
                    }
                }
            }
            cacheParameterMethodInfoMap.put(methodName, methodInfo);
        }

        // make cache key
        StringBuilder cacheKeyBuilder = new StringBuilder()
                .append(key).append("/").append(methodName).append("/");

        if (!CollectionUtils.isEmpty(parameterKeyList)) {
            cacheKeyBuilder.append(Joiner.on(",").join(parameterKeyList));
        }
        final String cacheKey = cacheKeyBuilder.toString();

        try {
            Object result;
            if (!replace) {
                Long ttl = redisDB.ttl(cacheKey);
                if (ttl > 0) {

                    result = redisDB.get(cacheKey, returnType);
                    return result;
                }
            }

            result = joinPoint.proceed();
            redisDB.set(cacheKey, result, returnType);
            redisDB.expire(cacheKey, expire);

            return result;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

    private String makeCacheKey(RedisCachedKeyParam keyParam, String value ) {
        return String.format("%s=%s", keyParam.key(), value);
    }
}
