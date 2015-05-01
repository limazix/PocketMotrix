package br.ufrj.pee.pocketmotrix.listener;

public interface WriterListener {
	
	public void onWriterReady();
	
	public void onWriterStoped();
	
	public void onWriteText(String text);
	
	public void onWriterError(String errorMessage);

}
