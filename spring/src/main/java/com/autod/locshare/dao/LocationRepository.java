package com.autod.locshare.dao;

import com.autod.locshare.model.Location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;


public interface LocationRepository extends JpaRepository<Location, Integer> {
    @Query("from Location l  join BrowseUser u on l.time>=:time and l.userId=u.id and u.groupId=:groupName and not l.userId=:userId")
    List<Location> findAfterTime(@Param("time") Timestamp time, @Param("groupName") String groupName,@Param("userId") String userId);
    @Query("from Location l  join BrowseUser u on l.time>=:time and l.userId=u.id and ( u.groupId is null or u.groupId = '' ) and not l.userId=:userId")
    List<Location> findAfterTime(@Param("time") Timestamp time,@Param("userId") String userId);
}

