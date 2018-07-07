package com.proper.enterprise.platform.core.jpa.repository;

import com.proper.enterprise.platform.core.jpa.util.JPAUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.Serializable;

public class BaseJpaRepositoryImpl<T, IDT extends Serializable> extends SimpleJpaRepository<T, IDT> implements BaseJpaRepository<T, IDT> {

    private final JpaEntityInformation<T, ?> entityInformation;

    public BaseJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
    }

    @Override
    public T updateForSelective(T var1) {
        if (entityInformation.isNew(var1)) {
            throw new PersistenceException("entity not persist");
        }
        IDT id = (IDT) entityInformation.getId(var1);
        if (id == null) {
            throw new PersistenceException("Could not get id from " + var1);
        }
        T oldEntity = this.findById(id).orElseThrow(() -> new PersistenceException("entity not persist"));
        BeanUtils.copyProperties(oldEntity, var1, JPAUtil.getNotNullColumnNames(var1));
        return this.save(var1);
    }
}

