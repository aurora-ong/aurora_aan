package ong.aurora.aan.command;

public class CommandNotFoundException extends Throwable {

    public String errorCode;

    public CommandNotFoundException(String errorCode) {
            this.errorCode = errorCode;
    }
}
