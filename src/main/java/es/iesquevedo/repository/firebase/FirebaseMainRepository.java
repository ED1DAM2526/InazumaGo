package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Implementación de producción del repositorio basada en Firebase Realtime Database (SDK).
 * Utiliza Firebase Admin SDK para operaciones en tiempo real, incluyendo lectura, escritura atómica y listeners.
 */
public class FirebaseMainRepository implements FirebaseGameRepository {
    private final FirebaseDatabase database;
    @SuppressWarnings({"FieldCanBeLocal", "CollectionWithoutInitialCapacity", "MismatchedQueryAndUpdateOfCollection"})
    private final Map<String, ValueEventListener> listeners = new HashMap<>();

    public FirebaseMainRepository(String firebaseUrl) {
        this.database = FirebaseDatabase.getInstance(firebaseUrl);
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        DatabaseReference ref = database.getReference("games/" + gameId);
        CompletableFuture<GameDto> future = new CompletableFuture<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GameDto game = snapshot.getValue(GameDto.class);
                future.complete(game);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new Exception(error.getMessage()));
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        DatabaseReference ref = database.getReference("games/" + gameId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("moves", payload.getMoves());
        updates.put("timestamp", payload.getTimestamp());
        updates.put("gameVersion", payload.getGameVersion());
        CompletableFuture<Void> future = new CompletableFuture<>();
        ref.updateChildren(updates, (error, ref1) -> {
            if (error == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(new Exception(error.getMessage()));
            }
        });
        return future;
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        DatabaseReference ref = database.getReference("games/" + gameId + "/moves");
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GenericTypeIndicator<List<MoveData>> t = new GenericTypeIndicator<>() {};
                List<MoveData> moves = snapshot.getValue(t);
                listener.accept(moves);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error, perhaps log or notify
            }
        };
        ref.addValueEventListener(vel);
        String id = "listener-" + gameId + "-" + System.currentTimeMillis();
        listeners.put(id, vel);
        return id;
    }

    @Override
    public String findDefaultName() {
        DatabaseReference ref = database.getReference("config/defaultName");
        final String[] result = new String[1];
        CountDownLatch latch = new CountDownLatch(1);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                result[0] = snapshot.getValue(String.class);
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                result[0] = "DefaultPlayer";
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "DefaultPlayer";
        }
        return result[0] != null ? result[0] : "DefaultPlayer";
    }

    /**
     * Remueve un listener previamente registrado por su ID.
     *
     * @param listenerId ID del listener retornado por addMovesListener
     */
    @SuppressWarnings("unused")
    public void removeMovesListener(String listenerId) {
        listeners.remove(listenerId);
    }
}
