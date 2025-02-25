package com.liucc.cli.pattern;

public class Client {
    public static void main(String[] args) {
        // 创建接收者对象
        Device TV = new Device("TV");
        Device Stereo = new Device("Stereo");
        // 创建命令对象
        Command turnON = new TurnOnCommand(TV);
        Command turnOff = new TurnOffCommand(Stereo);
        // 执行命令
        RemoteControl remoteControl = new RemoteControl();
        remoteControl.setCommand(turnON);
        remoteControl.pressButton();

        remoteControl.setCommand(turnOff);
        remoteControl.pressButton();
    }
}
