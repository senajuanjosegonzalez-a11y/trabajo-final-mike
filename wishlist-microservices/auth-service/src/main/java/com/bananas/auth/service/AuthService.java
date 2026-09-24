package com.bananas.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bananas.auth.dto.GlobalMessageResponseDTO;
import com.bananas.auth.dto.LoginRequestDTO;
import com.bananas.auth.dto.LoginResponseDTO;
import com.bananas.auth.dto.RegisterRequestDTO;
import com.bananas.auth.dto.UserDTO;
import com.bananas.auth.entity.Users;
import com.bananas.auth.repository.RolesRepository;
import com.bananas.auth.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final JwtService jwtService;

    public GlobalMessageResponseDTO<UserDTO> register(RegisterRequestDTO request) {
        GlobalMessageResponseDTO<UserDTO> response = new GlobalMessageResponseDTO<>();

        if (usersRepository.findFirstByEmail(request.getEmail()).isPresent()) {
            response.setMessage("El correo ya se encuentra en uso");
            return response;
        }

        if (usersRepository.findFirstByUsername(request.getUsername()).isPresent()) {
            response.setMessage("El nombre de usuario ya se encuentra en uso");
            return response;
        }

        if (rolesRepository.findById(request.getRolId()).isEmpty()) {
            response.setMessage("El rol no existe");
            return response;
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRolId(request.getRolId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        usersRepository.save(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRolId(user.getRolId());

        response.setData(userDTO);
        response.setMessage("Usuario registrado correctamente");
        return response;
    }

    public GlobalMessageResponseDTO<LoginResponseDTO> login(LoginRequestDTO request) {
        GlobalMessageResponseDTO<LoginResponseDTO> response = new GlobalMessageResponseDTO<>();

        Optional<Users> byUsername = usersRepository.findFirstByUsername(request.getUser());
        Optional<Users> byEmail = usersRepository.findFirstByEmail(request.getUser());

        Optional<Users> userFoundOpt = byUsername.isPresent() ? byUsername : byEmail;

        if (userFoundOpt.isEmpty()) {
            response.setMessage("El usuario o contraseña no son correctos");
            return response;
        }

        Users userFound = userFoundOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), userFound.getPassword())) {
            response.setMessage("El usuario o contraseña no son correctos");
            return response;
        }

        String jwt = jwtService.generateToken(userFound.getEmail(), userFound.getId(), userFound.getRolId());

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setJwt(jwt);
        response.setData(loginResponseDTO);
        response.setMessage("Inicio de sesión exitoso");
        return response;
    }

    public GlobalMessageResponseDTO<LoginResponseDTO> refreshToken(String token) throws Exception {
        GlobalMessageResponseDTO<LoginResponseDTO> response = new GlobalMessageResponseDTO<>();
        LoginResponseDTO refresh = new LoginResponseDTO();
        String jwt = jwtService.refreshToken(token);
        refresh.setJwt(jwt);
        response.setData(refresh);
        response.setMessage("Token renovado correctamente");
        return response;
    }
}
