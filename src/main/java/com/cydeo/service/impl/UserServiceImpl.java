package com.cydeo.service.impl;

import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDTO> listAllUsers() {
        List<User> userList=userRepository.findAll(Sort.by("firstName"));
        return userList.stream().map(userMapper::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO findByUserName(String username) {
       return userMapper.convertToDTO(userRepository.findByUserName(username));
    }

    @Override
    public void save(UserDTO dto) {

        userRepository.save(userMapper.convertToEntity(dto));
    }

    @Override
    public UserDTO update(UserDTO dto) {
        User user=userRepository.findByUserName(dto.getUserName());
        User convertedUser=userMapper.convertToEntity(dto);
        convertedUser.setId(user.getId());
        userRepository.save(convertedUser);
        return findByUserName(dto.getUserName());
    }

    @Override
    public void deleteByUserName(String username) {
        userRepository.deleteByUserName(username);
    }

    @Override
    public void delete(String username) {
        User user=userRepository.findByUserName(username);
        user.setIsDeleted(true);
        userRepository.save(user);
    }
}
