package com.liucc.marker;

import com.liucc.marker.cli.CommandExecutor;

public class Main {
    public static void main(String[] args) {
        CommandExecutor executor = new CommandExecutor();
        executor.doExecute(args);
    }
}
