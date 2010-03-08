package org.workin.core.persistence.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaCallback;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.stereotype.Repository;
import org.workin.core.constant.Constants;
import org.workin.core.persistence.support.PaginationSupport;
import org.workin.util.Assert;
import org.workin.util.CollectionUtils;

/**
 * 
 * @author <a href="mailto:goingmm@gmail.com">G.Lee</a>
 *
 * @param <T>
 * @param <PK>
 */

@Repository
@SuppressWarnings("unchecked")
public class JpaPersistenceImpl<T, PK extends Serializable> extends JpaDaoSupport implements
		JpaPersistence<T, PK> {

	/**
	 * ===Public methods================================================= 
	 * 
	 * Some public method for: 
	 * 			implements interface EntityPersistence and support service
	 * 
	 * =======================================================================
	 */

	/**
	 * 
	 * Make an objectToSave instance managed and persistent.
	 * 
	 * Throws:
     *		EntityExistsException 		 
     *			- if the entity already exists. (The EntityExistsException may be thrown when the persist operation is invoked, or the EntityExistsException or another PersistenceException may be thrown at flush or commit time.) 
     *		IllegalStateException 		 
     *			- if this EntityManager has been closed. 
     *		IllegalArgumentException 	 
     *			- if not an entity 
     *		TransactionRequiredException 
     *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param  objectToSave
	 * @return objectToSave
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public T persist(final T objectToSave) {
		this.getJpaTemplate().persist(objectToSave);
		return objectToSave;
	}

	/**
	 * 
	 * Make an objectsToSave(batch) instance managed and persistent.
	 * 
	 * Throws:
     *		EntityExistsException 		 
     *			- if the entity already exists. (The EntityExistsException may be thrown when the persist operation is invoked, or the EntityExistsException or another PersistenceException may be thrown at flush or commit time.) 
     *		IllegalStateException 		 
     *			- if this EntityManager has been closed. 
     *		IllegalArgumentException 	 
     *			- if not an entity 
     *		TransactionRequiredException 
     *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param  objectsToSave
	 * @return void
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public void batchPersist(final List objectsToSave) {
		Assert.isTrue(!CollectionUtils.isEmpty(objectsToSave),
				"List objectToSave canot null, when batchPersist...");

		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				int max = objectsToSave.size();
				for (int i = 0; i < max; i++) {
					em.persist(objectsToSave.get(i));
					if ((i != 0 && i % DEFAULT_BATCH_SIZE == 0) || i == max - 1) {
						em.flush();
					}
				}
				return null;
			}

		});
	}

	/**
	 * 
	 * Merge the state of the given entity into the current persistence context.
	 * 
	 * Throws:
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed. 
	 *		IllegalArgumentException 
	 *			- if instance is not an entity or is a removed entity. 
	 *		TransactionRequiredException 
	 *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.	
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 *  
	 * @param  objectToMerge
	 * @return objectToMerge
	 * 		   	- the instance that the state was merged to
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public T merge(final T objectToMerge) {
		return this.getJpaTemplate().merge(objectToMerge);
	}

	/**
	 * 
	 * Merge(Batch) the state of the given entitys into the current persistence context.
	 * 
	 * Throws:
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed. 
	 *		IllegalArgumentException 
	 *			- if instance is not an entity or is a removed entity. 
	 *		TransactionRequiredException 
	 *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.	
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 *  
	 * @param  objectsToMerge
	 * @return void
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public void batchMerge(final List objectsToMerge) {
		Assert.isTrue(!CollectionUtils.isEmpty(objectsToMerge),
				"List objectsToMerge canot null, when batchMerge...");

		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				int max = objectsToMerge.size();
				for (int i = 0; i < max; i++) {
					em.merge(objectsToMerge.get(i));
					if ((i != 0 && i % DEFAULT_BATCH_SIZE == 0) || i == max - 1) {
						em.flush();
					}
				}
				return null;
			}

		});
	}

   /**
    * 
    * Refresh the state of the instance from the database, overwriting changes made to the entity, if any.
    * 
    * Throws:
    * 	   	IllegalStateException 
    * 			- if this EntityManager has been closed. 
    * 		IllegalArgumentException 
    * 			- if not an entity or entity is not managed. 
    * 		TransactionRequiredException 
    * 			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction. 
    * 		EntityNotFoundException 
    * 			- if the entity no longer exists in the database.
    * 
    * Note:
	* 		All exceptions Will be converted to DataAccessException's subclass and thow
	* 
    * @param   objectToRefresh
    * @return  void
    * 
    * @throws org.springframework.dao.DataAccessException
	* 		   - If an error occurs.but usually throws DataAccessException's subclass
	* 
    */
	public void refresh(final T objectToRefresh) {
		this.getJpaTemplate().refresh(objectToRefresh);
	}

	/**
	 * 
	 * Remove the entity instance.
	 * 
	 *	Throws:
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed. 
	 *		IllegalArgumentException 
	 *			- if not an entity or if a detached entity 
	 *		TransactionRequiredException 
	 *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.
	 *
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param objectToRemove
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public void remove(final T objectToRemove) {
		this.getJpaTemplate().remove(objectToRemove);
	}

	/**
	 * 
	 * 1) Find entity by Object's PK. 
	 * 2) Remove the entity instance.
	 * 
	 * Throws:
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed. 
	 *		IllegalArgumentException 
	 *			- if not an entity or if a detached entity 
	 *			- if the first argument does not denote an entity type or the second argument is not a valid type for that entity's primary key
	 *		TransactionRequiredException 
	 *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param id
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 *  
	 */
	public void remove(final Class<T> entityClass, final PK id) {
		this.remove(this.findById(entityClass, id));
	}

	/**
	 * 
	 * Remove(Batch) the entitys instance.
	 *
	 * Throws:
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed. 
	 *		IllegalArgumentException 
	 *			- if not an entity or if a detached entity 
	 *		TransactionRequiredException 
	 *			- if invoked on a container-managed entity manager of type PersistenceContextType.TRANSACTION and there is no transaction.
	 *
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 *  
	 * @param objectsToRemove
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 */
	public void batchRemove(final List objectsToRemove) {
		Assert.isTrue(!CollectionUtils.isEmpty(objectsToRemove),
				"List ObjectsToRemove canot null, when batchRemove...");

		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				int max = objectsToRemove.size();
				for (int i = 0; i < max; i++) {
					em.refresh(objectsToRemove.get(i));
					em.remove(objectsToRemove.get(i));
					if ((i != 0 && i % DEFAULT_BATCH_SIZE == 0) || i == max - 1) {
						em.flush();
					}
				}
				return null;
			}
		});
	}

	/**
	 * 
	 * Synchronize the persistence context to the underlying database.
	 * 
	 * Throws:
     *  	IllegalStateException 
     *  		- if this EntityManager has been closed. 
     *   	TransactionRequiredException 
     *   		- if there is no transaction 
     *   	PersistenceException 
     *   		- if the flush fails
     *
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 */
	public void flush() {
		this.getJpaTemplate().flush();
	}
	
	/**
	 * 
	 * Clear the persistence context, causing all managed entities to become detached. 
	 * Changes made to entities that have not been flushed to the database will not be persisted.
	 * 
	 * Throws:
     *  	IllegalStateException 
     *  		- if this EntityManager has been closed.
     *
     *
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 */
	public void clear() {
		getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				em.clear();
				return null;
			}
		});
	}
	
	/**
	 * 
	 * Check if the instance belongs to the current persistence context.
	 * 
	 * Throws:
	 * 		IllegalStateException 
	 * 			- if this EntityManager has been closed. 
     *		IllegalArgumentException 
     *			- if not an entity		
	 *
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	entity
	 * @return 	boolean
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 *  
	 */
	public boolean contains(final T entity) {
		return this.getJpaTemplate().contains(entity);
	}

	/**
	 * 
	 * 1) Find entity by Object's PK. 
	 * 2) Check if the instance belongs to the current persistence context.
	 * 
	 * Throws:
	 * 		IllegalStateException 
	 * 			- if this EntityManager has been closed. 
     *		IllegalArgumentException 
     *			- if not an entity	
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
     *
	 * @param 	entityClass
	 * @param 	id
	 * @return 	boolean
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public boolean contains(final Class<T> entityClass, final PK id) {
		T entity = this.findById(entityClass, id);
		return this.contains(entity);
	}

	/**
	 * 
	 * Find entity by Object's PK. 
	 * 
	 * Throws:
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed. 
	 *		IllegalArgumentException 
	 *			- if the first argument does not denote an entity type or the second argument is not a valid type for that entity's primary key
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param id
	 * 
	 * @return entity
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public T findById(final Class<T> entityClass, final PK id) {
		return getJpaTemplate().find(entityClass, id);
	}

	/**
	 * 
	 * Execute a SELECT query that returns a single result.
	 * 	
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
	 *		NoResultException 		 
	 *			- if there is no result 
     *		NonUniqueResultException 
     *			- if more than one result 
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * @param values
	 * 
	 * @return T
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public T findUniqueByNamedOfQuery(final String queryName, final Object... values) {

		return (T) this.getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (values != null) {
					for (int i = 0,len = values.length; i < len; i++) {
						queryObject.setParameter(i + 1, values[i]);
					}
				}
				return queryObject.getSingleResult();
			}
		});
	}

	/**
	 * 
	 * Execute a SELECT query that returns a single result.
	 * 	
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
	 *		NoResultException 		 
	 *			- if there is no result 
     *		NonUniqueResultException 
     *			- if more than one result 
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * @param params
	 * 
	 * @return T
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public T findUniqueByNamedOfQuery(final String queryName, final Map<String, ?> params) {

		return (T) this.getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (!CollectionUtils.isEmpty(params)) {
					for (Map.Entry<String, ?> entry : params.entrySet()) {
						queryObject.setParameter(entry.getKey(), entry.getValue());
					}
				}
				return queryObject.getSingleResult();
			}
		});
	}

	/**
	 * 
	 * Execute a SELECT query that returns a single result.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * 
	 * @return T
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public T findUniqueByProperty(final Class<T> entityClass, final String propertyName, final Object value) {

		return (T) getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(buildQueryString(false, entityClass, propertyName));

				query.setParameter(1, value);

				return query.getSingleResult();
			}

		});
	}

	/**
	 * 
	 * Execute a SELECT query that returns a single result.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param params
	 * 
	 * @return T
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public T findUniqueByPropertys(final Class<T> entityClass, final Map<String, ?> params) {

		return (T) getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(buildQueryStringWithNamedParams(false, entityClass, params));

				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}

				return query.getSingleResult();
			}

		});
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * @param values
	 * 
	 * @return List
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List findByNamedOfQuery(final String queryName, final Object... values) {
		return this.getJpaTemplate().findByNamedQuery(queryName, values);
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * 
	 * @return List
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List findByNamedOfQuery(final String queryName) {
		return this.getJpaTemplate().findByNamedQuery(queryName, (Object[]) null);
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * @param nameAndValue
	 * 
	 * @return List
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List findByNamedOfQuery(final String queryName, final Map params) {
		return this.getJpaTemplate().findByNamedQueryAndNamedParams(queryName, params);
	}
	
	/**
	 * 
	 * Execute a save,update,delete query and return the query results as a int.
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	queryName
	 * @param 	values
	 * @return 	int
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public int executeNamedOfQuery(final String queryName, final Object... values) {
		return (Integer)getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				
				if (values != null) {
					for (int i = 0,len = values.length; i < len; i++) {
						queryObject.setParameter(i + 1, values[i]);
					}
				}
				
				return queryObject.executeUpdate();
			}
		});
	}
	
	/**
	 * 
	 * Execute a save,update,delete query and return the query results as a int.
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	queryName
	 * @param 	nameAndValue
	 * @return 	int
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public int executeNamedOfQuery(final String queryName, final Map<String, ?> nameAndValue) {
		return (Integer)getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (!CollectionUtils.isEmpty(nameAndValue)) {
					for (Map.Entry<String, ?> entry : nameAndValue.entrySet()) {
						queryObject.setParameter(entry.getKey(), entry.getValue());
					}
				}
				
				return queryObject.executeUpdate();
			}
		});
	}
	
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * 
	 * @return List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public List<T> findByProperty(final Class<T> entityClass, final String propertyName, final Object value) {
		return (List<T>) getJpaTemplate().find(buildQueryString(false, entityClass, propertyName), value);
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * 
	 * @param start
	 * @param maxRows
	 * 
	 * @return List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public List<T> findByProperty(final Class<T> entityClass, final String propertyName, final Object value,
			final int start, final int maxRows) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findByProperty()");
		
		return getJpaTemplate().executeFind(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				String queryString = buildQueryString(false, entityClass, propertyName).toString();
				Query query = em.createQuery(queryString);

				query.setParameter(1, value);

				if (maxRows >= 0) {
					query.setMaxResults(maxRows);
				}
				if (start >= 0) {
					query.setFirstResult(start);
				}

				return query.getResultList();
			}

		});
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a PaginationSupport.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * 
	 * @param start
	 * @param maxRows
	 * 
	 * @return PaginationSupport<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public PaginationSupport<T> findPaginatedByProperty(final Class<T> entityClass,
			final String propertyName, final Object value, final int start, final int maxRows) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findPaginatedByProperty()");
		int tmpMaxRows = maxRows > 0 ? maxRows : 1;
		int tmpStart = start > 0 ? start : 0;

		Integer count = countByProperty(entityClass, propertyName, value);
		
		if (count == null || count <= 0) {
			return new PaginationSupport<T>(new ArrayList<T>(0), 0, tmpMaxRows, tmpStart);
		}

		List<T> result = this.findByProperty(entityClass, propertyName, value, start, maxRows);

		return new PaginationSupport<T>(result, count, tmpMaxRows, tmpStart);
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	entityClass
	 * @param 	params
	 * 
	 * @return 	List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public List<T> findByPropertys(final Class<T> entityClass, final Map<String, ?> params) {

		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createQuery(buildQueryStringWithNamedParams(false, entityClass, params));

				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}

				return query.getResultList();
			}

		});
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	entityClass
	 * @param 	params
	 * 
	 * @param 	start
	 * @param 	maxRows
	 * 
	 * @return 	List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public List<T> findByPropertys(final Class<T> entityClass, final Map<String, ?> params, final int start,
			final int maxRows) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findByPropertys()");
		
		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				String queryString = buildQueryStringWithNamedParams(false, entityClass, params).toString();
				Query query = em.createQuery(queryString);

				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}

				if (maxRows >= 0) {
					query.setMaxResults(maxRows);
				}
				if (start >= 0) {
					query.setFirstResult(start);
				}

				return query.getResultList();
			}

		});
	}
	

	/**
	 * 
	 * Execute a SELECT query and return the query results as a PaginationSupport.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	entityClass
	 * @param 	params
	 * 
	 * @param 	start
	 * @param 	maxRows
	 * 
	 * @return 	PaginationSupport<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public PaginationSupport<T> findPaginatedByPropertys(final Class<T> entityClass,
			final Map<String, ?> params, final int start, final int maxRows) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findPaginatedByPropertys()");
		
		int tmpMaxRows = maxRows > 0 ? maxRows : 1;
		int tmpStart = start > 0 ? start : 0;

		Integer count = countByPropertys(entityClass, params);
		if (count == null || count <= 0) {
			return new PaginationSupport<T>(new ArrayList<T>(0), 0, tmpMaxRows, tmpStart);
		}

		List<T> result = findByPropertys(entityClass, params, start, maxRows);

		return new PaginationSupport<T>(result, count, tmpMaxRows, tmpStart);
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 
     *
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	entityClass
	 * @return 	List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<T> getAll(final Class<T> entityClass) {
		return this.getJpaTemplate().find(buildQueryString(false, entityClass).toString());
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 
     *
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param 	entityClass
	 * @return 	List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<T> getAllDistinct(final Class<T> entityClass) {
		Collection result = new LinkedHashSet(getAll(entityClass));
		return new ArrayList(result);
	}

	/**
	 * 
	 * Execute an update or delete statement.
	 * 
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed.
	 *			- if called for a Java Persistence query language SELECT statement 
	 *		TransactionRequiredException 
	 *			- if there is no transaction
	 *
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * @param values
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public void updateOrDelByNamedOfQuery(final String queryName, final Object... values) {

		this.getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (values != null) {
					for (int i = 0; i < values.length; i++) {
						queryObject.setParameter(i + 1, values[i]);
					}
				}
				return queryObject.executeUpdate();
			}
		});
	}

	/**
	 * 
	 * Execute an update or delete statement.
	 * 
	 * Throws:
	 * 		IllegalArgumentException 
	 * 			- if a query has not been defined with the given name
	 *		IllegalStateException 
	 *			- if this EntityManager has been closed.
	 *			- if called for a Java Persistence query language SELECT statement 
	 *		TransactionRequiredException 
	 *			- if there is no transaction
	 *
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryName
	 * @param params
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public void updateOrDelByNamedOfQuery(final String queryName, final Map<String, ?> params) {

		this.getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query queryObject = em.createNamedQuery(queryName);
				if (!CollectionUtils.isEmpty(params)) {
					for (Map.Entry<String, ?> entry : params.entrySet()) {
						queryObject.setParameter(entry.getKey(), entry.getValue());
					}
				}
				return queryObject.executeUpdate();
			}
		});
	}

	/**
	 * 
	 * Execute a SELECT query and return the count.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * 
	 * @return int
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public int countByProperty(final Class<?> entityClass, final String propertyName, final Object value) {
		
		return (Integer) getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				
				String queryString = buildQueryString(true, entityClass, propertyName).toString();
				
				Query query = em.createQuery(queryString);
				query.setParameter(1, value);
				
				return Integer.valueOf(String.valueOf(query.getSingleResult()));
			}
		});
	}

	
	/**
	 * 
	 * Execute a SELECT query and return the count.
	 * 
     * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param entityClass
	 * @param params
	 * 
	 * @return int
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public int countByPropertys(final Class<?> entityClass, final Map<String, ?> params) {

		return (Integer) getJpaTemplate().execute(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				String queryString = buildQueryStringWithNamedParams(true, entityClass, params).toString();
				Query query = em.createQuery(queryString);

				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}

				return Integer.valueOf(String.valueOf(query.getSingleResult()));
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * 
	 * @return List<?>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<?> findByNativeQuery(final String queryString) {
		
		return getJpaTemplate().executeFind(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				return em.createNativeQuery(queryString).getResultList();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param values
	 * 
	 * @return List<?>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<?> findByNativeQuery(final String queryString, final Object... values) {

		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString);

				for (int i = 0; i < values.length; i++) {
					query.setParameter(i + 1, values[i]);
				}
				return query.getResultList();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param start
	 * @param maxRows
	 * @param values
	 * 
	 * @return List<?>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<?> findByNativeQuery(final String queryString, final int start, final int maxRows,
			final Object... values) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findByNativeQuery()");
		
		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString);

				for (int i = 0; i < values.length; i++) {
					query.setParameter(i + 1, values[i]);
				}

				if (maxRows >= 0) {
					query.setMaxResults(maxRows);
				}
				if (start >= 0) {
					query.setFirstResult(start);
				}

				return query.getResultList();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param returnClass
	 * @param values
	 * 
	 * @return List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<T> findByNativeQuery(final Class<T> returnClass, final String queryString,
			final Object... values) {

		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString, returnClass);

				for (int i = 0; i < values.length; i++) {
					query.setParameter(i + 1, values[i]);
				}

				return query.getResultList();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a List.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param returnClass
	 * @param start
	 * @param maxRows
	 * @param values
	 * 
	 * @return List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<T> findByNativeQuery(final Class<T> returnClass, final String queryString, final int start,
			final int maxRows, final Object... values) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findByNativeQuery()");
		
		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString, returnClass);

				for (int i = 0; i < values.length; i++) {
					query.setParameter(i + 1, values[i]);
				}

				return query.getResultList();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a Entity.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param returnClass
	 * @param queryString
	 * @param values
	 * 
	 * @return T
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public T findUniqueByNativeQuery(final Class<T> returnClass, final String queryString,
			final Object... values) {

		return (T) getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString, returnClass);

				for (int i = 0,len=values.length; i < len; i++) {
					query.setParameter(i + 1, values[i]);
				}

				return query.getSingleResult();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a list.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param params
	 * 
	 * @return List<?>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<?> findByNativeQuery(final String queryString, final Map<String, ?> params) {

		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString);

				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}

				return query.getResultList();
			}

		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a list.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param start
	 * @param maxRows
	 * @param params
	 * 
	 * @return List<?>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<?> findByNativeQuery(final String queryString, final int start, final int maxRows,
			final Map<String, ?> params) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findByNativeQuery()");
		
		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString);
				
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
				
				if (maxRows >= 0) {
					query.setMaxResults(maxRows);
				}
				if (start >= 0) {
					query.setFirstResult(start);
				}
				return query.getResultList();
			}
			
		});
	}

	/**
	 * 
	 * Execute a SELECT query and return the query results as a list.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param returnClass
	 * @param queryString
	 * @param params
	 * 
	 * @return List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<T> findByNativeQuery(final Class<T> returnClass, final String queryString,
			final Map<String, ?> params) {
		
		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString, returnClass);
				
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
				return query.getResultList();
			}
			
		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a list.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param returnClass
	 * @param queryString
	 * @param start
	 * @param maxRows
	 * @param params
	 * 
	 * @return List<T>
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public List<T> findByNativeQuery(final Class<T> returnClass, final String queryString, final int start,
			final int maxRows, final Map<String, ?> params) {
		
		Assert.isTrue(maxRows != 0, "maxRows cannot be 0, in JpaPersistenceImpl.findByNativeQuery()");
		
		return getJpaTemplate().executeFind(new JpaCallback() {
			
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString, returnClass);
				
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
				
				if (maxRows >= 0) {
					query.setMaxResults(maxRows);
				}
				if (start >= 0) {
					query.setFirstResult(start);
				}
				return query.getResultList();
			}
			
		});
	}
	
	/**
	 * 
	 * Execute a SELECT query and return the query results as a Entity.
	 * 	
	 * Throws:
     *	    IllegalStateException 	 
     *			- if this EntityManager has been closed
     *			- if called for a Java Persistence query language UPDATE or DELETE statement
     *			
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param returnClass
	 * @param queryString
	 * @param params
	 * 
	 * @return T
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 * 
	 */
	public T findUniqueByNativeQuery(final Class<T> returnClass, final String queryString,
			final Map<String, ?> params) {
		
		return (T)getJpaTemplate().execute(new JpaCallback() {

			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString, returnClass);
				
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
			
				return query.getSingleResult();
			}
			
		});
	}
	
	/**
	 * 
	 * Execute an update or delete statement.
	 * 
	 * Throws:
	 *	IllegalStateException 
	 *		- if called for a Java Persistence query language SELECT statement or for a criteria query
	 *	TransactionRequiredException 
	 *		- if there is no transaction
	 *	QueryTimeoutException 
	 *		- if the statement execution exceeds the query timeout value set and only the statement is rolled back
	 *	PersistenceException 
	 *		- if the query execution exceeds the query timeout value set and the transaction is rolled back
	 * 
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @return
	 * 		The number of entities updated or deleted.
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public int persistByNativeQuery(final String queryString) {
		
		return (Integer) getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				return em.createNativeQuery(queryString).executeUpdate();
			}
		});
	}
	
	/**
	 * 
	 * Execute an update or delete statement.
	 * 
	 * Throws:
	 *	IllegalStateException 
	 *		- if called for a Java Persistence query language SELECT statement or for a criteria query
	 *	TransactionRequiredException 
	 *		- if there is no transaction
	 *	QueryTimeoutException 
	 *		- if the statement execution exceeds the query timeout value set and only the statement is rolled back
	 *	PersistenceException 
	 *		- if the query execution exceeds the query timeout value set and the transaction is rolled back
	 * 
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param params
	 * @return
	 * 		The number of entities updated or deleted.
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public int persistByNativeQuery(final String queryString, final Map<String, ?> params) {
		
		return (Integer) getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString);
				
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
				return query.executeUpdate();
			}
		});
	}
	
	/**
	 * 
	 * Execute an update or delete statement.
	 * 
	 * Throws:
	 *	IllegalStateException 
	 *		- if called for a Java Persistence query language SELECT statement or for a criteria query
	 *	TransactionRequiredException 
	 *		- if there is no transaction
	 *	QueryTimeoutException 
	 *		- if the statement execution exceeds the query timeout value set and only the statement is rolled back
	 *	PersistenceException 
	 *		- if the query execution exceeds the query timeout value set and the transaction is rolled back
	 * 
	 * 
	 * Note:
	 * 		All exceptions Will be converted to DataAccessException's subclass and thow
	 * 
	 * @param queryString
	 * @param values
	 * @return
	 * 		The number of entities updated or deleted.
	 * 
	 * @throws org.springframework.dao.DataAccessException
	 * 		   	- If an error occurs.but usually throws DataAccessException's subclass
	 * 
	 */
	public int persistByNativeQuery(final String queryString, final Object... values) {
		
		return (Integer) getJpaTemplate().execute(new JpaCallback() {
			public Object doInJpa(EntityManager em) throws PersistenceException {
				Query query = em.createNativeQuery(queryString);

				for (int i = 0,len = values.length; i < len; i++) {
					query.setParameter(i + 1, values[i]);
				}

				return query.executeUpdate();
			}
		});
	}
	
	/**
	 * ===Private================================================= Some private
	 * method for: -support public method
	 * ================================================================
	 */

	/**
	 * 
	 * Build Query String by class only.
	 * 
	 * @param clazz
	 * 
	 * @return string
	 * 
	 */
	private StringBuilder buildQueryString(final Class<?> clazz, final boolean isCount) {
		StringBuilder queryBuilder = new StringBuilder();
		
		if(isCount)
			queryBuilder.append("SELECT COUNT(*) as totalCount FROM ");
		else
			queryBuilder.append("SELECT obj FROM ");
		
		queryBuilder.append(clazz.getName());
		queryBuilder.append(" obj");

		return queryBuilder;
	}

	/**
	 * 
	 * Build Query String by class and values of parameters.
	 * 
	 * @param clazz
	 * @param values
	 * 
	 * @return string
	 * 
	 */
	private String buildQueryString(final boolean isCount, final Class<?> clazz, final String... values) {
		StringBuilder queryBuilder = buildQueryString(clazz, isCount);

		if (values != null && values.length > 0) {
			queryBuilder.append(" where ");
			for (String value : values) {
				queryBuilder.append(value).append(" = ? and ");
			}

			if (queryBuilder.lastIndexOf(" and ") == (queryBuilder.length() - 5)) {
				queryBuilder.delete(queryBuilder.length() - 5, queryBuilder.length());
			}
		}

		return queryBuilder.toString();
	}

	/**
	 * 
	 * Build Query String by class and values of parameters.
	 * 
	 * @param clazz
	 * @param params
	 * 
	 * @return string
	 * 
	 */
	private String buildQueryStringWithNamedParams(final boolean isCount, final Class<?> clazz, final Map<String, ?> params) {
		StringBuilder queryBuilder = buildQueryString(clazz, isCount);

		if (!CollectionUtils.isEmpty(params)) {
			queryBuilder.append(" where ");

			for (Map.Entry<String, ?> entry : params.entrySet()) {
				queryBuilder.append(entry.getKey()).append(" = :").append(entry.getKey()).append(" and ");
			}

			if (queryBuilder.lastIndexOf(" and ") == (queryBuilder.length() - 5)) {
				queryBuilder.delete(queryBuilder.length() - 5, queryBuilder.length());
			}
		}
		
		logger.info(" Build Query String With NamedParams: {}", queryBuilder.toString());
		return queryBuilder.toString();
	}
	
	// JpaPersistenceImpl logger
	private static final transient Logger logger = LoggerFactory.getLogger(JpaPersistenceImpl.class);
	
	// Allowed batch objects record size
	protected static final int DEFAULT_BATCH_SIZE = Constants.DEFAULT_BATCH_SIZE;
}
