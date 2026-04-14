package es.iesquevedo.repository;

import es.iesquevedo.repository.firebase.FirebaseGameRepository;

/**
 * Alias para FirebaseGameRepository.
 * Mantiene compatibilidad con código existente.
 * Usar FirebaseGameRepository directamente en código nuevo.
 */
public interface MainRepository extends FirebaseGameRepository {
}

