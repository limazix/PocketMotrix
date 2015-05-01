package br.ufrj.pee.pocketmotrix.service;

public enum Command {
	UNKNOWN("unknown"), GO_HOME("gohome"), GO_BACK("goback"), WAKE_OR_KEEP_WAKE("startlistening"), RELEASE_KEEP_WAKE("stoplistening"),
	GO_FORWARD("goforward"), GO_BACKWARD("gobackward"), QUIETER("quieter"), LOUDER("louder"), REFRESH("refresh"), CLEAR("clear"),
	NOTIFICATION("notification"), POWER("power"), HISTORY("history"), SETTINGS("settings"), WRITE("write");
	

    private final String label;
    private Command(final String label) {
        this.label = label;
    }
    
    public String getLabel() {
    	return label;
    }

    public static Command get(String label){
        for (Command c: Command.values()) {
            if (c.label.equalsIgnoreCase(label))
                return c;
        }
        return UNKNOWN;
    }
}
