package ong.aurora.aan.command;

public class CommandValidationException extends Throwable {

    public String errorCode;

    public CommandValidationException(String errorCode) {
            this.errorCode = errorCode;
    }
}
