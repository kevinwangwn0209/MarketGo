package com.easy.marketgo.biz.service.wecom.masstask;

import com.easy.marketgo.api.model.request.masstask.WeComMassTaskClientRequest;
import com.easy.marketgo.common.enums.WeComMassTaskSendStatusEnum;
import com.easy.marketgo.common.enums.WeComMassTaskStatus;
import com.easy.marketgo.common.enums.WeComMassTaskTypeEnum;
import com.easy.marketgo.common.utils.JsonUtils;
import com.easy.marketgo.core.entity.WeComAgentMessageEntity;
import com.easy.marketgo.core.entity.masstask.WeComMassTaskEntity;
import com.easy.marketgo.core.entity.masstask.WeComMassTaskSendQueueEntity;
import com.easy.marketgo.core.repository.wecom.WeComAgentMessageRepository;
import com.easy.marketgo.core.repository.wecom.masstask.WeComMassTaskSendQueueRepository;
import com.easy.marketgo.core.repository.wecom.masstask.WeComMassTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.easy.marketgo.common.constants.RabbitMqConstants.EXCHANGE_NAME_DEFAULT_WECOM_MASS_TASK_GROUP;
import static com.easy.marketgo.common.constants.RabbitMqConstants.ROUTING_KEY_WECOM_MASS_TASK_GROUP;

/**
 * @author : kevinwang
 * @version : 1.0
 * @data : 7/8/22 9:25 PM
 * Describe:
 */
@Slf4j
@Component
public class SendGroupMassTaskProducer extends SendBaseMassTaskProducer {
    @Autowired
    private WeComMassTaskRepository weComMassTaskRepository;

    @Autowired
    private WeComMassTaskSendQueueRepository weComMassTaskSendQueueRepository;

    @Autowired
    private RabbitTemplate weComGroupMassTaskAmqpTemplate;

    @Autowired
    private WeComAgentMessageRepository weComAgentMessageRepository;

    public void sendGroupMassTask() {
        List<WeComMassTaskEntity> entities =
                weComMassTaskRepository.getWeComMassTaskByScheduleTime(SEND_USER_GROUP_TIME_BEFORE,
                        WeComMassTaskTypeEnum.GROUP.name(), WeComMassTaskStatus.COMPUTED.getValue());
        if (CollectionUtils.isEmpty(entities)) {
            log.info("query group mass task is empty.");
            return;
        }
        log.info("start query user group send queue for group mass task. entities={}", entities);
        for (WeComMassTaskEntity entity : entities) {
            try {
                WeComAgentMessageEntity weComAgentMessageEntity =
                        weComAgentMessageRepository.getWeComAgentByCorp(entity.getProjectUuid(), entity.getCorpId());
                String agentId = (weComAgentMessageEntity == null) ? "" : weComAgentMessageEntity.getAgentId();
                String content = entity.getContent();
                if (StringUtils.isNotBlank(content)) {
                    WeComMassTaskClientRequest request =
                            buildSendMassTaskContent(WeComMassTaskTypeEnum.GROUP.name().toLowerCase(), content);
                    request.setProjectUuid(entity.getProjectUuid());
                    List<WeComMassTaskSendQueueEntity> weComMassTaskSendQueueEntities =
                            weComMassTaskSendQueueRepository.queryByTaskUuid(entity.getUuid(),
                                    WeComMassTaskSendStatusEnum.UNSEND.name());
                    weComMassTaskRepository.updateTaskStatusByUUID(entity.getUuid(),
                            WeComMassTaskStatus.SENDING.getValue());
                    for (WeComMassTaskSendQueueEntity weComMassTaskSendQueueEntity : weComMassTaskSendQueueEntities) {
                        request.setSender(weComMassTaskSendQueueEntity.getMemberId());
                        request.setTaskUuid(entity.getUuid());
                        request.setCorpId(entity.getCorpId());
                        request.setAgentId(agentId);
                        log.info("send request to queue for group mass task. sendMessage={}",
                                JsonUtils.toJSONString(request));
                        produceRabbitMqMessage(request);
                    }
                    weComMassTaskRepository.updateTaskStatusByUUID(entity.getUuid(),
                            WeComMassTaskStatus.SENT.getValue());
                }
            } catch (Exception e) {
                weComMassTaskRepository.updateTaskStatusByUUID(entity.getUuid(),
                        WeComMassTaskStatus.SEND_FAILED.getValue());
                log.error("failed to send single mass task message to queue.", e);
            }
        }
    }

    private void produceRabbitMqMessage(Object values) {
        weComGroupMassTaskAmqpTemplate.convertAndSend(EXCHANGE_NAME_DEFAULT_WECOM_MASS_TASK_GROUP,
                ROUTING_KEY_WECOM_MASS_TASK_GROUP, values);
    }
}
