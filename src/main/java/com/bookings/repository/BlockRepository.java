package com.bookings.repository;

import com.bookings.models.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    @Query("""
            select case when count(b)> 0 then true else false end from Block b where
                :blockDate >= b.startDate and
                :blockDate <= b.endDate  and
                b.property.id = :propertyId""")
    Boolean existsByBlockDateAndPropertyId(@Param("blockDate") LocalDate blockDate, @Param("propertyId") Long propertyId);
}
