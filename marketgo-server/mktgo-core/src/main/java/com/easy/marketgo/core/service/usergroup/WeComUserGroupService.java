package com.easy.marketgo.core.service.usergroup;

import com.easy.marketgo.core.model.bo.WeComUserGroupAudienceRule;

/**
 * @author : kevinwang
 * @version : 1.0
 * @data : 6/25/22 5:43 PM
 * Describe:
 */
public interface WeComUserGroupService {
    void userGroupEstimate(String projectId, String corpId, String requestId,
                           WeComUserGroupAudienceRule weComUserGroupAudienceRule);
}
