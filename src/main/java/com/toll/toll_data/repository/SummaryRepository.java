package com.toll.toll_data.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.toll.toll_data.model.Summary;

public interface SummaryRepository extends JpaRepository<Summary, Long> {}
