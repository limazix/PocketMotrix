package br.ufrj.pee.pocketmotrix.notifier;

import br.ufrj.pee.pocketmotrix.listener.NotifierListener;

public abstract class AbstractNotifier {

	private boolean isReady = false;

    private NotifierListener listener;

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

    public NotifierListener getListener() {
        return listener;
    }

    public void setListener(NotifierListener listener) {
        this.listener = listener;
    }

	public abstract void setupNotifier();

	public abstract void notifyUser(String message);
}
