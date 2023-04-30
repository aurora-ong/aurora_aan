package ong.aurora.aan.command;

public class CommandInvalidDataException extends Throwable {

    public String errorCode;

    public CommandInvalidDataException(String errorCode) {
            this.errorCode = errorCode;
    }
}
