package org.clarkproject.aioapi.api.tool;

import org.clarkproject.aioapi.api.obj.RequestAccessLog;
import org.clarkproject.aioapi.api.service.AccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

@Component
public class TenMinuteJob {

    @Autowired
    private AccessLogService accessLogService;

    TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");

    // 每天早上8點執行一次
    @Scheduled(cron = "0 10 * * * ?", zone = "Asia/Taipei")
    public void throwRecordIntoDB() {

        List<RequestAccessLog> accessLogList = accessLogService.getAccessWithInTenMinutes(LocalDateTime.now().minusMinutes(10),LocalDateTime.now());
        accessLogService.backUpAccessLog(accessLogList);
        System.out.println("每日任務執行中: " + System.currentTimeMillis() / 1000);
    }
}
