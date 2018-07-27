package net.radcheck.radcheck.models.data;

import net.radcheck.radcheck.models.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface QueryDao extends CrudRepository<Query, Integer> {
}
