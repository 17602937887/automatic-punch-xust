/*
 * Copyright (c) 2021 hangcc.cn
 * All rights reserved.
 *
 */
package cn.hangcc.automaticclockinxust.biz.mq;

import cn.hangcc.automaticclockinxust.biz.AutomaticClockIn.AliSmsBiz;
import cn.hangcc.automaticclockinxust.domain.model.AutomaticClockIn.ClockInMsgModel;
import cn.hangcc.automaticclockinxust.domain.model.AutomaticClockIn.UserInfoModel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import static cn.hangcc.automaticclockinxust.common.constant.AutomaticClockInConstants.*;

/**
 * 发送短信消费者
 *
 * @author chenhang
 * @created 2021/2/10
 */
@Slf4j
@Component
public class SmsMsgConsumer {

    @Resource
    private AliSmsBiz aliSmsBiz;

    @KafkaListener(topics = KAFKA_SEND_REGISTER_SUCCESS_SMS_TOPIC)
    public void listenRegisterSuccess(ConsumerRecord<?, ?> record) {
        try {
            UserInfoModel value = JSON.parseObject(record.value().toString(), UserInfoModel.class);
            log.info("接到kafka生产的发送注册成功消息userInfoModel:{}", value);
            // 发送注册成功的短信消息
            aliSmsBiz.sendRegisterSuccessMsg(value);
        } catch (Exception e) {
            log.error("调用Aliyun短信接口出现异常: e = ", e);
        }
    }

    @KafkaListener(topics = KAFKA_SEND_CLOCK_IN_FAILED_SMS_TOPIC)
    public void listenClockInFailed(ConsumerRecord<?, ?> record) {
        try {
            ClockInMsgModel value = JSON.parseObject(record.value().toString(), ClockInMsgModel.class);
            // 发送打卡失败的短信消息
            aliSmsBiz.sendClockInFailedMsg(value);
        } catch (Exception e) {
            log.error("调用Aliyun短信接口出现异常: e = ", e);
        }
    }

    @KafkaListener(topics = KAFKA_SEND_CLOCK_IN_EXCEPTION_SMS_TOPIC)
    public void listenExceptionSms(ConsumerRecord<?, ?> record) {
        try {
            JSONObject jsonObject = JSON.parseObject(record.value().toString());
            String time = jsonObject.getString("time");
            String msg = jsonObject.getString("errorMsg");
            aliSmsBiz.sendExceptionToDev(time, msg);
        } catch (Exception e) {
            log.error("调用Aliyun短信接口出现异常: e = ", e);
        }
    }
}