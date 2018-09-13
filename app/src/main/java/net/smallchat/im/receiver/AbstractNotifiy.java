package net.smallchat.im.receiver;

import android.app.NotificationManager;
import android.content.Context;

import net.smallchat.im.service.IMService;

public abstract class AbstractNotifiy implements Notifiy{
	private NotificationManager notificationManager;
	private IMService service;
	public AbstractNotifiy(IMService context) {
		super();
		service = context;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public IMService getService() {
		return service;
	}
	
	
}
