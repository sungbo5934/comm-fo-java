package com.ssb.comm.helper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedisHelper {

	@Autowired
	private RedisTemplate<String, Object>  redisTemplate;
	
	public void setString(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}
	
	public String getString(String key, String value) {
		return (String) redisTemplate.opsForValue().get(key);
	}
	
	public void setList(String key, String value) {
		redisTemplate.opsForList().rightPush(key, value);
	}
	
	public List<Object> getList(String key, int start, int end) {
		return (List<Object>) redisTemplate.opsForList().range(key, start, end);
	}
	
	public void setHash(String key, String hashKey, Object value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
		
	}
	
	public Map<String, Object> getHash(String key, String hashKey) {
		return (Map<String, Object>) redisTemplate.opsForHash().get(key, hashKey);
	}
}
