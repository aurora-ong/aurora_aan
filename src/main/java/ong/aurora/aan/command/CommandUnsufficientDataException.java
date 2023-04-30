package ong.aurora.aan.command;

public class CommandUnsufficientDataException extends Throwable {

    public String errorCode;

    public CommandUnsufficientDataException(String errorCode) {
            this.errorCode = errorCode;
    }
}
