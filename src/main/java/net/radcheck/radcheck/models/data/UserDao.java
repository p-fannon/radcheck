package net.radcheck.radcheck.models.data;

import net.radcheck.radcheck.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository("userRepository")
@Transactional
public interface UserDao extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
