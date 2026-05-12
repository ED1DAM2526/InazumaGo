package es.iesquevedo.controller;

import es.iesquevedo.service.MainService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class MainControllerTest {
    @Test
    public void status_shouldReturnGreetingFromService() {
        MainService mockService = Mockito.mock(MainService.class);
        when(mockService.status()).thenReturn("Hello, test!");

        MainController controller = new MainController(mockService);

        String result = controller.status();

        assertEquals("Hello, test!", result);
    }
}
