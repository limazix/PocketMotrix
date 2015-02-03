package br.ufrj.pee.pocketmotrix.service;

public enum Command {
	UNKNOWN("unknown"), GO_HOME("gohome"), GO_BACK("goback"), GO_RIGHT("goright"),
	GO_NEXT("gonext"), QUIETER("quieter"), LOUDER("louder");

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
