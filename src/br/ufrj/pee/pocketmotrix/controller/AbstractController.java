package br.ufrj.pee.pocketmotrix.controller;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EBean;

import br.ufrj.pee.pocketmotrix.app.PocketMotrixApp;
import br.ufrj.pee.pocketmotrix.service.PocketMotrixService;

@EBean
public abstract class AbstractController {

	public PocketMotrixService service;

	@App
	PocketMotrixApp app;
	
	public void setup() {
		service = app.getPocketMotrixService();		
	}
	
	public abstract void startEngine();
	
	public abstract void stopEngine();
}
