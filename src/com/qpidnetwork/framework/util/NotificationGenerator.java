package com.qpidnetwork.framework.util;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationGenerator {

	private static AtomicInteger mNotificationRequestId = new AtomicInteger();
	
	public static int getNotificationRequestId(){
		return mNotificationRequestId.getAndIncrement();
	}
}
