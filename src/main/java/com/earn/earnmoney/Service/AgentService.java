package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.Agent;
import java.util.List;

public interface AgentService {
        List<Agent> getAllAgents();

        Agent registerAgent(String name, String phone, String whatsapp, String telegram, String facebook,
                        String instagram,
                        String notes, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;

        Agent updateAgent(Long id, String name, String phone, String whatsapp, String telegram, String facebook,
                        String instagram, String notes, org.springframework.web.multipart.MultipartFile file)
                        throws java.io.IOException;

        Agent registerAgent(Agent agent);

        void deleteAgent(Long id);

        Agent getAgentById(Long id);
}
