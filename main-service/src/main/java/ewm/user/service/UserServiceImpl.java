package ewm.user.service;

import ewm.exception.EntityNotFoundException;
import ewm.user.dto.UserDto;
import ewm.user.mapper.UserMapper; // No need for UserMapper to be injected
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        if (ids != null && !ids.isEmpty()) {
            return UserMapper.toUserDtoList(userRepository.findByIdIn(ids, pageable));
        } else {
            return UserMapper.toUserDtoList(userRepository.findAll(pageable).getContent());
        }
    }

    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Пользователь c ID - " + id + ", не найден."));
        userRepository.deleteById(id);
    }
}
