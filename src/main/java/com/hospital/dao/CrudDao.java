package com.hospital.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface representing standard CRUD operations.
 * Demonstrates Abstraction by decoupling business logic from underlying relational storage.
 *
 * @param <T> The entity type
 */
public interface CrudDao<T> {

    /**
     * Inserts a new entity record into the database.
     * The generated primary key should be set on the entity.
     *
     * @param entity The entity to insert
     * @return The entity with its generated ID populated
     */
    T add(T entity);

    /**
     * Updates an existing entity record in the database.
     *
     * @param entity The entity to update
     * @return true if an existing record was updated, false otherwise
     */
    boolean update(T entity);

    /**
     * Deletes an entity record by its unique primary key.
     *
     * @param id The primary key ID
     * @return true if a record was removed, false otherwise
     */
    boolean delete(int id);

    /**
     * Retrieves an entity by its unique primary key.
     *
     * @param id The primary key ID
     * @return Optional containing the entity if found, or empty Optional
     */
    Optional<T> getById(int id);

    /**
     * Retrieves all entity records.
     *
     * @return List of all entities, or empty list if no records exist
     */
    List<T> getAll();
}
