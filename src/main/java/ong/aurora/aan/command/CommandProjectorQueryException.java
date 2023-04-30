package ong.aurora.aan.command;

public class CommandProjectorQueryException extends Throwable {

    public String errorCode;

    public CommandProjectorQueryException(String errorCode) {
            this.errorCode = errorCode;
    }
}
