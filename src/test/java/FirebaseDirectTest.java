import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Test directo a Firebase RTDB sin autenticación.
 * Si devuelve 200 con datos o null, las reglas están abiertas.
 * Si devuelve 401, las reglas NO están públicas.
 */
public class FirebaseDirectTest {
    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();
        
        // URL SIN token
        String url = "https://inazumago-default-rtdb.firebaseio.com/games.json";
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            System.out.println("=== FIREBASE RTDB TEST ===");
            System.out.println("URL: " + url);
            System.out.println("Status: " + response.code());
            System.out.println("Body: " + (response.body() != null ? response.body().string() : "NULL"));
            
            if (response.code() == 200) {
                System.out.println("\n✅ SUCCESS - Reglas están públicas!");
            } else if (response.code() == 401) {
                System.out.println("\n❌ PERMISSION DENIED - Reglas NO están públicas!");
            } else {
                System.out.println("\n⚠️ UNEXPECTED: " + response.code());
            }
        }
    }
}

