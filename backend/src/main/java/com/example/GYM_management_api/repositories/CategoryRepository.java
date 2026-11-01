package com.example.GYM_management_api.repositories;

import com.example.GYM_management_api.entities.Category;
import com.example.GYM_management_api.entities.enums.CommonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByCode(String code);

    Optional<Category> findByName(String name);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Category> findByStatus(CommonStatus status);
}
