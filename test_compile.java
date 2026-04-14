// Test simple para verificar que las clases se compilaron correctamente
import es.iesquevedo.repository.firebase.FirebaseGameRepository;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;

public class test_compile {
    public static void main(String[] args) {
        // Verificar que todas las clases existen y pueden ser instanciadas
        FirebaseGameRepository inMemory = new InMemoryMainRepository();
        System.out.println("InMemory repo: " + inMemory.findDefaultName());

        FirebaseGameRepository firebase = new FirebaseMainRepository("http://example.com");
        System.out.println("Firebase repo: " + firebase.findDefaultName());

        MainServiceImpl service = new MainServiceImpl(inMemory);
        System.out.println("Service greeting: " + service.greet());

        System.out.println("\n✅ Compilación exitosa - todos los métodos existen");
    }
}

