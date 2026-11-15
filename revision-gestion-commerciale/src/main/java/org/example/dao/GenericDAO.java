package org.example.dao;

import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class GenericDAO<T, ID extends Serializable> {
    private final Class<T> entityClass;
    private final SessionFactory sessionFactory;

    public GenericDAO(Class<T> entityClass) {
        this(entityClass, HibernateUtil.getSessionFactory());
    }

    public GenericDAO(Class<T> entityClass, SessionFactory sessionFactory) {
        if (entityClass == null || sessionFactory == null) {
            throw new IllegalArgumentException("Le type d'entité et la SessionFactory sont obligatoires.");
        }
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }

    public T find(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.get(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM " + entityClass.getName(), entityClass)
                    .getResultList();
        }
    }

    public T create(T entity) {
        validateEntity(entity);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.save(entity);
                transaction.commit();
                return entity;
            } catch (RuntimeException exception) {
                rollback(transaction);
                throw exception;
            }
        }
    }

    public T update(T entity) {
        validateEntity(entity);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                T updated = (T) session.merge(entity);
                transaction.commit();
                return updated;
            } catch (RuntimeException exception) {
                rollback(transaction);
                throw exception;
            }
        }
    }

    public void delete(T entity) {
        validateEntity(entity);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.delete(session.contains(entity) ? entity : session.merge(entity));
                transaction.commit();
            } catch (RuntimeException exception) {
                rollback(transaction);
                throw exception;
            }
        }
    }

    private void validateEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle.");
        }
    }

    private void rollback(Transaction transaction) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }
}
