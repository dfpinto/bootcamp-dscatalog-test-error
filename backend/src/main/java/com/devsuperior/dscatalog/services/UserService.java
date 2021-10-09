package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserPasswordDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DataBaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class UserService {

	@Autowired
	UserRepository repositoryUser;

	@Autowired
	RoleRepository repositoryRole;
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public List<UserDTO> findAll() {
		return repositoryUser.findAll().stream().map(x -> new UserDTO(x)).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> entity = repositoryUser.findById(id);
		return (new UserDTO(entity.orElseThrow(() -> new ResourceNotFoundException("Entity not found"))));
	}

	@Transactional
	public UserDTO insert(UserPasswordDTO dto) {
		User entity = new User();
		copyUserDtoToUser(dto, entity);
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity = repositoryUser.save(entity);
		return new UserDTO(entity);
	}

	@Transactional
	public UserDTO update(Long id, UserDTO dto) {
		try {
			User entity = repositoryUser.getById(id);
			copyUserDtoToUser(dto, entity);
			entity = repositoryUser.save(entity);			
			return new UserDTO(entity);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}


	public void delete(Long id) {
		try {
			repositoryUser.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		} catch (DataIntegrityViolationException e) {
			throw new DataBaseException("Integrity violation. Id " + id);
		}
	}

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable) {
		Page<User> list = repositoryUser.findAll(pageable);
		
		return list.map(x -> new UserDTO(x));
	}
	
	private void copyUserDtoToUser(UserDTO dto, User entity) {
		entity.setEmail(dto.getEmail());
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		
		entity.getRoles().clear();
		dto.getRoles().forEach(role -> entity.getRoles().add(repositoryRole.getById(role.getId())));
	}
}
