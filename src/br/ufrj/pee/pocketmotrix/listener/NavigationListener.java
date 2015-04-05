package br.ufrj.pee.pocketmotrix.listener;

import br.ufrj.pee.pocketmotrix.service.Command;

public interface NavigationListener {
	
	public void onNavigationCommand(Command cmd);
	
	public void onNavigationNumber(String number);
	
	public void onNavigationError(String errorMessage);

}
