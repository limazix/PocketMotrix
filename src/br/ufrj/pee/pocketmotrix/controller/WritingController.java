package br.ufrj.pee.pocketmotrix.controller;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import br.ufrj.pee.pocketmotrix.app.PocketMotrixApp;
import br.ufrj.pee.pocketmotrix.engine.WriterEngine;
import br.ufrj.pee.pocketmotrix.listener.WriterListener;
import br.ufrj.pee.pocketmotrix.service.PocketMotrixService;

@EBean
public class WritingController implements WriterListener {

	private PocketMotrixService service;

	@App
	PocketMotrixApp app;

	@Bean
	WriterEngine writerEngine;
	
	public void setup() {
		writerEngine.setWriterListener(this);
		service = app.getPocketMotrixService();
	}
	
	public void startEngine() {
		if(!writerEngine.isActive())
			writerEngine.startWriter();
	}
	
	public void stopEngine() {
		if(writerEngine.isActive())
			writerEngine.stopWriter();
	}
	
	@Override
	public void onWriterReady() {
		service.writeText("Writer is Ready");
	}

	@Override
	public void onWriterStoped() {
		service.writeText("Writer has stoped");
	}

	@Override
	public void onWriteText(String text) {
		service.writeText(text);
	}

	@Override
	public void onWriterError(String errorMessage) {
		service.showNotification(errorMessage);
	}

}
