package net.radcheck.radcheck.models.data;

import net.radcheck.radcheck.models.LatLon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository("locationRepository")
@Transactional
public interface LatLonDao extends JpaRepository<LatLon, Integer> {
    @Query("SELECT a FROM LatLon a WHERE a.lat < :north AND a.lat > :south AND a.lon < :east AND a.lon > :west")
    List<LatLon> findDuplicates(@Param("north") double north, @Param("south") double south,
                                @Param("east") double east, @Param("west") double west);
}
