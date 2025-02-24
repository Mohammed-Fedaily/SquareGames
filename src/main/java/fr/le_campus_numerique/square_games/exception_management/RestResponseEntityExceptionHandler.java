package fr.le_campus_numerique.square_games.exception_management;

import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { InvalidPositionException.class, Exception.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected String handleConflict(Exception ex, WebRequest request) {
        ex.printStackTrace();
        return ex.getMessage();
    }
}