package net.radcheck.radcheck.models.data;

import net.radcheck.radcheck.models.LatLon;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface LatLonDao extends CrudRepository<LatLon, Integer> {
}
