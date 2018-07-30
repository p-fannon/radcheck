package net.radcheck.radcheck.models.data;

import net.radcheck.radcheck.models.LatLon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository("locationRepository")
@Transactional
public interface LatLonDao extends JpaRepository<LatLon, Integer> {
}
