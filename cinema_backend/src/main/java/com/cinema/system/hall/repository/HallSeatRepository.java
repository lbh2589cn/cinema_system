package com.cinema.system.hall.repository;

import com.cinema.system.hall.entity.HallSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface HallSeatRepository extends JpaRepository<HallSeat, Long> {
    List<HallSeat> findByHallId(Long hallId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from HallSeat s where s.hallId = :hallId")
    void deleteByHallId(Long hallId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from HallSeat s where s.id in :ids")
    void deleteBySeatIdIn(@Param("ids") Set<Long> ids);
}
