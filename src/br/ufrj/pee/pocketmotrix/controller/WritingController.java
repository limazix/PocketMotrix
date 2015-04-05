package br.ufrj.pee.pocketmotrix.controller;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import br.ufrj.pee.pocketmotrix.R;
import br.ufrj.pee.pocketmotrix.engine.WriterEngine;
import br.ufrj.pee.pocketmotrix.listener.WriterListener;

@EBean
public class WritingController extends AbstractController implements WriterListener {

	@Bean
	WriterEngine writerEngine;
	
	@Override
	public void setup() {
		super.setup();
		writerEngine.setWriterListener(this);
	}
	
	@Override
	public void startEngine() {
		if(!writerEngine.isActive())
			writerEngine.startWriter();
	}
	
	@Override
	public void stopEngine() {
		if(writerEngine.isActive())
			writerEngine.stopWriter();
	}
	
	@Override
	public void onWriterReady() {
		service.writeText(app.getString(R.string.writer_is_ready));
	}

	@Override
	public void onWriterStoped() {
		service.writeText(app.getString(R.string.writer_has_stoped));
	}

	@Override
	public void onWriteText(String text) {
		service.writeText(text);
	}

	@Override
	public void onWriterError(String errorMessage) {
		app.showNotification(errorMessage);
	}

}
