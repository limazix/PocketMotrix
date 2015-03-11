package br.ufrj.pee.pocketmotrix.service;

public enum Command {
	UNKNOWN("unknown"), GO_HOME("gohome"), GO_BACK("goback"), WAKE_OR_KEEP_WAKE("startlistening"), RELEASE_KEEP_WAKE("stoplistening"),
	GO_UP("goup"), GO_LEFT("goleft"), GO_DOWN("godown"), GO_RIGHT("goright"), QUIETER("quieter"), LOUDER("louder"), REFRESH("refresh"), CLEAR("clear");
	

    private final String label;
    private Command(final String label) {
        this.label = label;
    }

    public static Command get(String label){
        for (Command c: Command.values()) {
            if (c.label.equalsIgnoreCase(label))
                return c;
        }
        return UNKNOWN;
    }
}
