package com.example.NotsHub.payload;

import java.util.HashSet;
import java.util.Set;

import com.example.NotsHub.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
	
	private Long userId;
	private String username;
	private String email;
	private String password;
	private Set<Role> roles = new HashSet<>();

}
