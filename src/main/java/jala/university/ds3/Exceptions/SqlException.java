package jala.university.ds3.Exceptions;

import java.sql.SQLException;

public class SqlException extends RuntimeException {
    public SqlException(String message) {
        System.out.println("SQL Exception: " + message);
        //super(message);
    }
}
