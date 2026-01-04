package com.earn.earnmoney.repo;

import com.earn.earnmoney.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepo extends JpaRepository<Agent, Long> {
}
