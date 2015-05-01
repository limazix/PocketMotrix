package br.ufrj.pee.pocketmotrix.notifier;

public abstract class AbstractNotifier {

	private boolean isReady = false; 
	
	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	public abstract void setupNotifier();

	public abstract void notifyUser(String message);
}
