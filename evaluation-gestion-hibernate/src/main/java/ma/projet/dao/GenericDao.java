package ma.projet.dao;

import ma.projet.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class GenericDao<T, ID extends Serializable> implements IDao<T, ID> {
    private final Class<T> entityClass;
    protected final SessionFactory sessionFactory;

    public GenericDao(Class<T> entityClass) {
        this(entityClass, HibernateUtil.getSessionFactory());
    }

    public GenericDao(Class<T> entityClass, SessionFactory sessionFactory) {
        if (entityClass == null || sessionFactory == null) {
            throw new IllegalArgumentException("Le type et la SessionFactory sont obligatoires.");
        }
        this.entityClass = entityClass;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public T findById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.get(entityClass, id);
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM " + entityClass.getName(), entityClass).getResultList();
        }
    }

    @Override
    public T create(T entity) {
        requireEntity(entity);
        return inTransaction(session -> {
            session.save(entity);
            return entity;
        });
    }

    @Override
    public T update(T entity) {
        requireEntity(entity);
        return inTransaction(session -> entityClass.cast(session.merge(entity)));
    }

    @Override
    public void delete(T entity) {
        requireEntity(entity);
        inTransaction(session -> {
            session.delete(session.contains(entity) ? entity : session.merge(entity));
            return null;
        });
    }

    protected <R> R inTransaction(HibernateWork<R> work) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                R result = work.execute(session);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    private void requireEntity(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne peut pas être nulle.");
        }
    }

    @FunctionalInterface
    protected interface HibernateWork<R> {
        R execute(Session session);
    }
}
