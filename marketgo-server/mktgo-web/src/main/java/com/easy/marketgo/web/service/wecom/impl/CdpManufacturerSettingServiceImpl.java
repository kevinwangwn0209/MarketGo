package com.easy.marketgo.web.service.wecom.impl;

import com.easy.marketgo.common.enums.CdpManufacturerTypeEnum;
import com.easy.marketgo.common.enums.ErrorCodeEnum;
import com.easy.marketgo.core.entity.CdpConfigEntity;
import com.easy.marketgo.core.repository.CdpConfigRepository;
import com.easy.marketgo.web.model.request.CdpManufacturerMessageRequest;
import com.easy.marketgo.web.model.response.BaseResponse;
import com.easy.marketgo.web.model.response.CdpManufactureListResponse;
import com.easy.marketgo.web.model.response.CdpManufacturerMessageResponse;
import com.easy.marketgo.web.service.wecom.CdpManufacturerSettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : kevinwang
 * @version : 1.0
 * @data : 11/27/22 4:58 PM
 * Describe:
 */
@Slf4j
@Service
public class CdpManufacturerSettingServiceImpl implements CdpManufacturerSettingService {

    @Resource
    private CdpConfigRepository cdpConfigRepository;

    @Override
    public BaseResponse queryCdpList(String projectId, String corpId) {
        log.info("start to query cdp list. projectUuid={}, corpId={}", projectId, corpId);
        CdpManufactureListResponse response = new CdpManufactureListResponse();

        List<CdpManufactureListResponse.CdpManufactureMessage> manufactureMessages = new ArrayList<>();

        for (CdpManufacturerTypeEnum item : CdpManufacturerTypeEnum.list()) {
            CdpManufactureListResponse.CdpManufactureMessage message =
                    new CdpManufactureListResponse.CdpManufactureMessage();
            message.setCdpName(item.getName());
            message.setCdpType(item.getValue());
            CdpConfigEntity entity = cdpConfigRepository.getCdpConfigByCorpIdAndCdpType(projectId, corpId,
                    item.getValue());
            if (entity == null) {
                message.setConfigStatus(Boolean.FALSE);
                message.setSwitchStatus(Boolean.FALSE);
            } else {
                message.setConfigStatus(Boolean.TRUE);
                message.setSwitchStatus(entity.getStatus());
            }

            manufactureMessages.add(message);
        }
        response.setCdpList(manufactureMessages);
        return BaseResponse.success(response);
    }

    @Override
    public BaseResponse saveOrUpdateCdpMessage(String projectId, String corpId, String cdpType,
                                               CdpManufacturerMessageRequest request) {
        log.info("start to save cdp config to db. projectUuid={}, corpId={}, cdpType={}, request={}", projectId, corpId,
                cdpType, request);
        CdpConfigEntity entity = new CdpConfigEntity();
        if (request.getId() != null) {
            entity.setId(request.getId());
        } else {
            entity.setStatus(Boolean.FALSE);
        }
        entity.setProjectUuid(projectId);
        entity.setCdpType(cdpType);
        entity.setApiUrl(request.getApiUrl());
        if (StringUtils.isNotBlank(request.getApiSecret())) {
            entity.setApiSecret(request.getApiSecret());
        }

        if (StringUtils.isNotBlank(request.getAppKey())) {
            entity.setAppKey(request.getAppKey());
        }

        if (StringUtils.isNotBlank(request.getDataUrl())) {
            entity.setDataUrl(request.getDataUrl());
        }

        if (StringUtils.isNotBlank(request.getProjectName())) {
            entity.setProjectName(request.getProjectName());
        }

        cdpConfigRepository.save(entity);
        return BaseResponse.success();
    }

    @Override
    public BaseResponse queryCdpMessage(String projectId, String corpId, String cdpType) {
        log.info("start to query cdp config from db. projectUuid={}, corpId={}, cdpType={}", projectId, corpId,
                cdpType);

        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(corpId) || StringUtils.isBlank(cdpType)) {
            return BaseResponse.failure(ErrorCodeEnum.ERROR_WEB_PARAM_IS_ILLEGAL);
        }
        CdpConfigEntity entity = cdpConfigRepository.getCdpConfigByCorpIdAndCdpType(projectId, corpId, cdpType);
        CdpManufacturerMessageResponse response = new CdpManufacturerMessageResponse();
        if (entity != null) {
            BeanUtils.copyProperties(entity, response);
        }

        return BaseResponse.success(response);
    }

    @Override
    @Transient
    public BaseResponse changeCdpStatus(String projectId, String corpId, String cdpType, Boolean status) {
        log.info("start to change cdp status. projectUuid={}, corpId={}, cdpType={}, status={}", projectId, corpId,
                cdpType, status);
        if (StringUtils.isBlank(projectId) || StringUtils.isBlank(corpId) || StringUtils.isBlank(cdpType)) {
            return BaseResponse.failure(ErrorCodeEnum.ERROR_WEB_PARAM_IS_ILLEGAL);
        }

        if (status.equals(Boolean.TRUE)) {
            cdpConfigRepository.updateCdpStatusByCorpId(projectId, corpId);
        }
        cdpConfigRepository.updateCdpStatusByCorpIdAndCdpType(projectId, corpId, cdpType, status);
        return BaseResponse.success();
    }

    @Override
    public BaseResponse deleteCdpMessage(String projectId, String corpId, String cdpType) {
        cdpConfigRepository.deleteByCorpIdAndCdpType(projectId, corpId, cdpType);
        return BaseResponse.success();
    }

    @Override
    public BaseResponse CdpSettingTest(String projectId, String corpId, String cdpType,
                                       CdpManufacturerMessageRequest request) {
        return null;
    }
}
