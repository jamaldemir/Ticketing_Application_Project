package com.cydeo.service.impl;

import com.cydeo.dto.ProjectDTO;
import com.cydeo.dto.TaskDTO;
import com.cydeo.dto.UserDTO;
import com.cydeo.entity.User;
import com.cydeo.mapper.UserMapper;
import com.cydeo.repository.UserRepository;
import com.cydeo.service.ProjectService;
import com.cydeo.service.TaskService;
import com.cydeo.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TaskService taskService;
    private final ProjectService projectService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, TaskService taskService, ProjectService projectService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.taskService = taskService;
        this.projectService = projectService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> listAllUsers() {
        List<User> userList=userRepository.findAll(Sort.by("firstName"));
        return userList.stream().map(userMapper::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO findByUserName(String username) {
        User user = userRepository.findByUserName(username);
        return userMapper.convertToDTO(user);
    }

    @Override
    public void save(UserDTO dto) {

        dto.setEnabled(true);
        User obj=userMapper.convertToEntity(dto);
        obj.setPassword(passwordEncoder.encode(obj.getPassword()));
        userRepository.save(obj);
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
        User user = userRepository.findByUserName(username);

        if (checkIfUserCanBeDeleted(user)) {
            user.setIsDeleted(true);
            user.setUserName(user.getUserName() + "-" + user.getId());
            userRepository.save(user);
        }
    }

    private boolean checkIfUserCanBeDeleted(User user) {

        switch (user.getRole().getDescription()) {
            case "Manager":
                List<ProjectDTO> projectDTOList = projectService.readAllByAssignedManager(user);
                return projectDTOList.size() == 0;
            case "Employee":
                List<TaskDTO> taskDTOList = taskService.readAllByAssignedEmployee(user);
                return taskDTOList.size() == 0;
            default:
                return true;
        }
    }
    @Override
    public List<UserDTO> listAllByRole(String role) {
        List<User> users=userRepository.findAllByRoleDescriptionIgnoreCase(role);
        return users.stream().map(userMapper::convertToDTO).collect(Collectors.toList());
    }


}
