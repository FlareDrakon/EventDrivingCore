package ru.flare.event.core.dao;


import java.util.List;

/**
 * this dao can be replaced to rabbitmq/kafka if need
 * but we does not need rabbit only for load/save so I use JPA storage
 */
public abstract class Dao<T> {

    public abstract void save(T abstractTask);

    public abstract List<T> findAll(String sortProperty);

    public abstract void delete(T abstractTask);
}
