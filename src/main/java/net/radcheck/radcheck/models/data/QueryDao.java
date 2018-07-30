package net.radcheck.radcheck.models.data;

import net.radcheck.radcheck.models.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository("queryRepository")
@Transactional
public interface QueryDao extends JpaRepository<Query, Integer> {
}
