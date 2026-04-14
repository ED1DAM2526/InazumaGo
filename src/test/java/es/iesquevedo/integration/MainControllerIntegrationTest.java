package es.iesquevedo.integration;

import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.MainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MainControllerIntegrationTest {

    @Mock
    private MainService mainService;

    private MainController mainController;

    @BeforeEach
    void setUp() {
        mainController = new MainController();
        mainController.setService(mainService);
    }

    @Test
    void testGreetDelegatesToService() {
        String expectedGreeting = "Hello, TestPlayer!";
        when(mainService.greet()).thenReturn(expectedGreeting);

        String actualGreeting = mainController.greet();

        assertEquals(expectedGreeting, actualGreeting);
        verify(mainService, times(1)).greet();
    }

    @Test
    void testGreetWhenServiceIsNull() {
        MainController emptyController = new MainController();
        String result = emptyController.greet();
        assertEquals("Servicio no disponible", result);
    }

    @Test
    void testSetServiceAndInitialize() {
        MainController newController = new MainController();
        newController.setService(mainService);
        assertNotNull(newController);
    }
}