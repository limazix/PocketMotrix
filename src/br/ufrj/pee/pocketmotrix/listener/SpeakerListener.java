package br.ufrj.pee.pocketmotrix.listener;

public interface SpeakerListener {
	
	public void onSpeakerReady();
	
	public void onSpeakerError(String errorMessage);

}
