package com.autod.locshare.dao;

import com.autod.locshare.model.BrowseUser;
import com.autod.locshare.model.Location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;


public interface UserRepository extends JpaRepository<BrowseUser, String> {
}

