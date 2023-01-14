package com.techelevator.models.exceptions;

public class InvalidPasswordException extends Exception
{
    public InvalidPasswordException () {
        super("Invalid Password. Try again or exit if you aren't the owner of" +
                " this vending machine.");
    }
}

