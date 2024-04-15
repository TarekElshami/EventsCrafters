package com.EventCrafters.EventCrafters.repository;

import java.util.List;
import java.util.Optional;

import com.EventCrafters.EventCrafters.model.Category;
import com.EventCrafters.EventCrafters.model.Event;
import com.EventCrafters.EventCrafters.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = "SELECT name FROM eventcrafters.categories",
            nativeQuery = true)
    List<String> findAllNames();

    @Query(value = "SELECT COUNT(eventcrafters.events.id) AS event_count FROM eventcrafters.categories LEFT JOIN eventcrafters.events ON eventcrafters.categories.id = eventcrafters.events.category_id GROUP BY eventcrafters.categories.id, eventcrafters.categories.name;",
            nativeQuery = true)
    List<Integer> findAllCategoriesUsedCount();

    @Query(value = "SELECT eventcrafters.categories.id FROM eventcrafters.categories LEFT JOIN eventcrafters.events ON eventcrafters.categories.id = eventcrafters.events.category_id GROUP BY eventcrafters.categories.id, eventcrafters.categories.name;",
            nativeQuery = true)
    List<Integer> findAllCategoriesUsedId();

    @Query(value="SELECT * FROM eventcrafters.categories c where c.name <> 'Sin categoría'", nativeQuery = true)
    Page<Category> findCategories(Pageable pageable);


    @Modifying
    @Transactional
    @Query("update Event e set e.category.id = 1 where e.category.id = :categoryId")
    void reassignEventsToDefaultCategory(Long categoryId);


}
