package dk.ronin;

public class TooManyRequestException extends Exception{

    public TooManyRequestException(String message) {
        super(message);
    }
}
