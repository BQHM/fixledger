package com.fixledger.infrastructure.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fixledger.infrastructure.notification.NotificationDeliveryService;
import com.fixledger.infrastructure.notification.NotificationDispatchResult;
import com.fixledger.infrastructure.notification.NotificationProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationDeliverySchedulerTest {

  @Test
  @DisplayName("外部渠道全部关闭时不查询待投递记录")
  void skipDispatchWhenExternalChannelsDisabled() {
    NotificationDeliveryService service = mock(NotificationDeliveryService.class);
    NotificationDeliveryScheduler scheduler = new NotificationDeliveryScheduler(
        service,
        new NotificationProperties()
    );

    scheduler.dispatchNotifications();

    verifyNoInteractions(service);
  }

  @Test
  @DisplayName("任一外部渠道启用时执行通知投递")
  void dispatchWhenEmailChannelEnabled() {
    NotificationDeliveryService service = mock(NotificationDeliveryService.class);
    NotificationProperties properties = new NotificationProperties();
    properties.getEmail().setEnabled(true);
    when(service.dispatchPending()).thenReturn(new NotificationDispatchResult(2, 1, 1, 0));
    NotificationDeliveryScheduler scheduler = new NotificationDeliveryScheduler(
        service,
        properties
    );

    scheduler.dispatchNotifications();

    verify(service).dispatchPending();
  }
}
