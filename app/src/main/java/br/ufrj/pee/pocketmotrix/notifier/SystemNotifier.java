package br.ufrj.pee.pocketmotrix.notifier;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import br.ufrj.pee.pocketmotrix.R;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

@EBean
public class SystemNotifier extends AbstractNotifier {

	private static final int NOTIFICATION_DEFAULT_ID = 0;

	@RootContext
	Context context;
	
	private NotificationManager notificationManager;

	private NotificationCompat.Builder notificationBuilder;
	
	private String notificationTitle;
	private String notificationContentText;
	private String notificationTicker;
	
	@Override
	public void setupNotifier() {
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		setupNotificationManager();	
	}
	
	private void setupNotificationManager() {
		notificationBuilder = new NotificationCompat.Builder(context)
		.setSmallIcon(R.mipmap.ic_launcher);
		notifyUser(context.getResources().getString(R.string.app_name));
	}

	public String getNotificationTitle() {
		return notificationTitle;
	}

	public void setNotificationTitle(String notificationTitle) {
		this.notificationTitle = notificationTitle;
		notificationBuilder.setContentTitle(notificationTitle);
	}

	public String getNotificationContentText() {
		return notificationContentText;
	}

	public void setNotificationContentText(String notificationContentText) {
		this.notificationContentText = notificationContentText;
		notificationBuilder.setContentText(notificationContentText);
	}

	public String getNotificationTicker() {
		return notificationTicker;
	}

	public void setNotificationTicker(String notificationTicker) {
		this.notificationTicker = notificationTicker;
		notificationBuilder.setTicker(notificationTicker);
	}

	@Override
	public void notifyUser(String message) {
		setNotificationContentText(message);
		setNotificationTicker(message);
		notificationManager.notify(NOTIFICATION_DEFAULT_ID, notificationBuilder.build());		
	}

}
