package ${basePackage};

import ${basePackage}.cli.CommandExecutor;

public class Main {
    public static void main(String[] args) {
        CommandExecutor executor = new CommandExecutor();
        executor.doExecute(args);
    }
}
