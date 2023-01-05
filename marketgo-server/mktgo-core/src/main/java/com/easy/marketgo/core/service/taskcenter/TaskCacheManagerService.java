package com.easy.marketgo.core.service.taskcenter;

import cn.hutool.core.codec.Base64;
import com.easy.marketgo.common.enums.WeComMassTaskExternalUserStatusEnum;
import com.easy.marketgo.core.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : kevinwang
 * @version : 1.0
 * @data : 6/28/22 5:58 PM
 * Describe:
 */
@Slf4j
@Service
public class TaskCacheManagerService {

    private final static String CACHE_CONTENT_KEY_NAME = "task_center_content_%s";

    private final static String CACHE_MEMBER_KEY_NAME = "task_center_member_%s##%s##%s##%s";

    private final static String CACHE_CUSTOMER_KEY_NAME = "task_center_customer_%s##%s##%s##%s##%s##%s";
    private final static long CACHE_SAVE_TIME = 30 * 24 * 60 * 60;

    private final static long CACHE_CONTENT_SAVE_TIME = 3 * 24 * 60 * 60;

    @Autowired
    private RedisService redisService;

    public void setCacheContent(String taskUuid, String content) {
        redisService.set(String.format(CACHE_CONTENT_KEY_NAME, taskUuid), content, CACHE_CONTENT_SAVE_TIME);
    }

    public String getCacheContent(String taskUuid) {
        String content = redisService.get(String.format(CACHE_CONTENT_KEY_NAME, taskUuid));
        return (StringUtils.isNotBlank(content)) ? content : null;
    }

    public Long getCacheContentExpireTime(String taskUuid) {
        Long ExpireTime = redisService.getExpire(String.format(CACHE_CONTENT_KEY_NAME, taskUuid));
        return (ExpireTime != null) ? ExpireTime : null;
    }

    public void delCacheContent(String taskUuid) {
        redisService.delete(String.format(CACHE_CONTENT_KEY_NAME, taskUuid));
    }

    public void setCustomerCache(String corpId, String memberId, String taskUuid, String uuid, String externalUserId,
                                 String externalUserName) {
        String key = String.format(CACHE_CUSTOMER_KEY_NAME, corpId, memberId, taskUuid, uuid,
                externalUserId, Base64.encode(externalUserName));
        log.info("save external user message for single task center to cache . key={}", key);
        redisService.set(key, WeComMassTaskExternalUserStatusEnum.UNDELIVERED.getValue(), CACHE_SAVE_TIME);
    }

    public void setMemberCache(String corpId, String memberId, String taskUuid, String uuid) {
        redisService.set(String.format(CACHE_MEMBER_KEY_NAME, corpId, memberId, taskUuid, uuid),
                WeComMassTaskExternalUserStatusEnum.UNDELIVERED.getValue(), CACHE_SAVE_TIME);
    }

    public void delCustomerCache(String corpId, String memberId, String taskUuid, String uuid) {
        String key = String.format(CACHE_CUSTOMER_KEY_NAME, corpId, memberId, taskUuid, uuid);
        List<String> keys = redisService.cursorPatternKeys(key);
        log.info("save external user message for single task center to cache . keys={}", keys);
        if (CollectionUtils.isNotEmpty(keys)) {
            redisService.deleteKeys(keys);
        }
    }

    public void delMemberCache(String corpId, String memberId, String taskUuid, String uuid) {
        redisService.delete(String.format(CACHE_MEMBER_KEY_NAME, corpId, memberId, taskUuid, uuid));
    }
}
