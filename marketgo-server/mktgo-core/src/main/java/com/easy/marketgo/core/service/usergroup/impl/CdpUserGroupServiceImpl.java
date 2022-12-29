package com.easy.marketgo.core.service.usergroup.impl;

import com.easy.marketgo.common.enums.UserGroupAudienceStatusEnum;
import com.easy.marketgo.common.utils.JsonUtils;
import com.easy.marketgo.core.model.bo.CdpUserGroupAudienceRule;
import com.easy.marketgo.core.model.bo.UserGroupEstimateResultBO;
import com.easy.marketgo.core.repository.wecom.WeComUserGroupAudienceRepository;
import com.easy.marketgo.core.service.usergroup.UserGroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author : kevinwang
 * @version : 1.0
 * @data : 12/29/22 4:17 PM
 * Describe:
 */
@Slf4j
@Component
public class CdpUserGroupServiceImpl implements UserGroupService {

    @Autowired
    private WeComUserGroupAudienceRepository weComUserGroupAudienceRepository;

    @Async
    @Override
    public void userGroupEstimate(String projectId, String corpId, String requestId, String taskType, String userGroupRules) {

        CdpUserGroupAudienceRule cdpUserGroupAudienceRule = JsonUtils.toObject(userGroupRules,
                CdpUserGroupAudienceRule.class);
        Integer memberCount = 0;
        Integer externalUserCount = 0;
        try {
            if (CollectionUtils.isNotEmpty(cdpUserGroupAudienceRule.getCrowds())) {
                for (CdpUserGroupAudienceRule.CrowdMessage message : cdpUserGroupAudienceRule.getCrowds()) {
                    memberCount += message.getUserCount();
                    externalUserCount += message.getUserCount();
                }
            }
        } catch (Exception e) {
            log.error("failed to user group for cdp estimate result. requestId={}", requestId, e);
        }
        UserGroupEstimateResultBO userGroupEstimateResultBO = new UserGroupEstimateResultBO();
        userGroupEstimateResultBO.setExternalUserCount(externalUserCount);
        userGroupEstimateResultBO.setMemberCount(memberCount);

        weComUserGroupAudienceRepository.updateResultByRequestId(requestId, projectId,
                JsonUtils.toJSONString(userGroupEstimateResultBO),
                UserGroupAudienceStatusEnum.SUCCEED.getValue());
    }
}
