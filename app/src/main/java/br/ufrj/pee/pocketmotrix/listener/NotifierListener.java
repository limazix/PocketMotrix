package br.ufrj.pee.pocketmotrix.listener;

import br.ufrj.pee.pocketmotrix.notifier.AbstractNotifier;

public interface NotifierListener {
	
	public void onNotifierReady(AbstractNotifier notifier);

	public void onNotifierError(String errorMessage);

}
