package ong.aurora.commons.command;

public class CommandValidationException extends Throwable {

    public String errorCode;

    public CommandValidationException(String errorCode) {
            this.errorCode = errorCode;
    }
}
