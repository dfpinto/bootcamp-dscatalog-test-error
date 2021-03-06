package com.devsuperior.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {

	@Autowired
	ProductRepository repositoryProduct;

	@Autowired
	CategoryRepository repositoryCategory;

	@Transactional(readOnly = true)
	public List<ProductDTO> findAll() {
		return repositoryProduct.findAll().stream().map(x -> new ProductDTO(x)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> entity = repositoryProduct.findById(id);
		return (new ProductDTO(entity.orElseThrow(() -> new ResourceNotFoundException("Entity not found")), entity.get().getCategories()));
	}

	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyProductDtoToProduct(dto, entity);
		entity = repositoryProduct.save(entity);
		return new ProductDTO(entity);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repositoryProduct.getById(id);
			copyProductDtoToProduct(dto, entity);
			entity = repositoryProduct.save(entity);			
			return new ProductDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}


	public void delete(Long id) {
		try {
			repositoryProduct.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DataBaseException("Integrity violation. Id " + id);
		}
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Pageable pageable) {
		Page<Product> list = repositoryProduct.findAll(pageable);
		
		return list.map(x -> new ProductDTO(x, x.getCategories()));
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPagedFilter(Pageable pageable, Integer categoryId, String name) {
		//Category category = (categoryId == 0) ? null : repositoryCategory.getById(categoryId);
		Page<Product> list = repositoryProduct.findAllPagedFilter(pageable, categoryId, name);
		
		return list.map(x -> new ProductDTO(x, x.getCategories()));
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPagedFilter2(Pageable pageable, Long categoryId, String name) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(repositoryCategory.getById(categoryId));
		Page<Product> list = repositoryProduct.findAllPagedFilter2(pageable, categories, name);
		
		return list.map(x -> new ProductDTO(x));
	}

	@Transactional(readOnly = true)
	public Page<ProductDTO> findNativeQuery(Pageable pageable) {
		Page<Product> list = repositoryProduct.findNativeQuery(pageable);
		
		return list.map(x -> new ProductDTO(x, x.getCategories()));
	}

	private void copyProductDtoToProduct(ProductDTO dto, Product entity) {
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setPrice(dto.getPrice());
		entity.setImgUrl(dto.getImgUrl());
		entity.setDate(dto.getDate());
		
		entity.getCategories().clear();
		dto.getCategories().forEach(cat-> entity.getCategories().add(repositoryCategory.getById(cat.getId())));
	}
}
