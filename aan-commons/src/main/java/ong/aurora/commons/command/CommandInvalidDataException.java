package ong.aurora.commons.command;

public class CommandInvalidDataException extends Throwable {

    public String errorCode;

    public CommandInvalidDataException(String errorCode) {
            this.errorCode = errorCode;
    }
}
