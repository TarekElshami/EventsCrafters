package com.EventCrafters.EventCrafters.service;

import com.EventCrafters.EventCrafters.model.Category;
import com.EventCrafters.EventCrafters.repository.CategoryRepository;
import com.EventCrafters.EventCrafters.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

	@Autowired
	private CategoryRepository repository;

	private int pageSize = 1;
	private int maxPageNum;

	public Optional<Category> findById(long id) {
		return repository.findById(id);
	}

	public List<Category> findAll(int page) {
		Pageable pageable = PageRequest.of(page, pageSize);
		return repository.findCategories(pageable).getContent();
	}

	public boolean exist(long id) {
		return repository.existsById(id);
	}

	public List<Category> findAll() {
		return repository.findAll();
	}

	public List<String> findAllNames() {return repository.findAllNames();}

	public List<Category> findAjax(){
		// this is so that the result is rounded up
		Page<Category> catPage = repository.findCategories(PageRequest.of(0, pageSize));
		this.maxPageNum = catPage.getTotalPages();
		return catPage.getContent();

	}

	public Category save(Category category) {
		return repository.save(category);
	}

	@Transactional
	public void delete(long id) {
		if (id == 1) {
			throw new RuntimeException("No se puede eliminar la categoría predeterminada");
		}

		repository.reassignEventsToDefaultCategory(id);
		repository.deleteById(id);
	}


	public int getMaxPageNum() { return this.maxPageNum; }




	public List<Integer> categoriesNumbers(){
		List<Integer> count = repository.findAllCategoriesUsedCount();
		List<Integer> finalList= new ArrayList<>();
		int size = count.size();
		int aux = this.findAll().size();
		int i;
		for (i = 0; i< aux;i++){
			finalList.add(0);
		}
		for (i = 0; i<size;i++) {
			finalList.set(i,count.get(i));
		}

		return finalList;
	}
}
