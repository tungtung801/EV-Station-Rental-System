package spring_boot.project_swp.exception.Print_Exception;

public class NotFoundException extends  RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
