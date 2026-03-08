package com.example.NotsHub.security.services;

import com.example.NotsHub.Repository.UserRepository;
import com.example.NotsHub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String rawIdentifier = usernameOrEmail.trim();
        String identifier = rawIdentifier.contains("@")
                ? rawIdentifier.toLowerCase()
                : rawIdentifier;

        User user = rawIdentifier.contains("@")
                ? userRepository.findByEmail(identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with email: " + identifier))
                : userRepository.findByUserName(identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with username: " + identifier));

        return UserDetailsImpl.build(user);
    }
}