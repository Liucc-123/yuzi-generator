package com.liucc.cli.example;

import picocli.CommandLine;

@CommandLine.Command
public class Main implements Runnable {
    @CommandLine.Option(names = "--interactive", interactive = true)
    String value;

    public void run() {
        if (value == null && System.console() != null) {
            // alternatively, use Console::readPassword
            value = System.console().readLine("Enter value for --interactive: ");
        }
        System.out.println("You provided value '" + value + "'");
    }

    public static void main(String[] args) {
        new CommandLine(new Main()).execute("--interactive");
    }
}
