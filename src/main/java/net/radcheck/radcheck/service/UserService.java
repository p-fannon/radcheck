package net.radcheck.radcheck.service;

import net.radcheck.radcheck.models.User;

public interface UserService {
    public User findUserByEmail(String email);
    public void saveUser(User user);
}
