package com.earn.earnmoney.Service;

import com.earn.earnmoney.model.Agent;
import com.earn.earnmoney.model.Agent;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.repo.AgentRepo;
import com.earn.earnmoney.util.ImageUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.List;

@Service
public class AgentServiceImplementation implements AgentService {

    @Autowired
    private AgentRepo agentRepo;

    @Override
    public List<Agent> getAllAgents() {
        List<Agent> agents = agentRepo.findAll();
        // Clear image binary data to reduce load
        agents.forEach(a -> {
            if (a.getImage() != null) {
                a.getImage().setImage(null);
            }
        });
        return agents;
    }

    @Override
    public Agent registerAgent(String name, String phone, String whatsapp, String telegram, String facebook,
            String instagram, MultipartFile file) throws IOException {
        Agent agent = new Agent();
        agent.setName(name);
        agent.setPhone(phone);
        agent.setWhatsapp(whatsapp);
        agent.setTelegram(telegram);
        agent.setFacebook(facebook);
        agent.setInstagram(instagram);

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setName(file.getOriginalFilename());
            image.setType(file.getContentType());
            image.setImage(ImageUtilities.compressImage(file.getBytes()));
            agent.setImage(image);
        }

        return agentRepo.save(agent);
    }

    @Override
    public Agent updateAgent(Long id, String name, String phone, String whatsapp, String telegram, String facebook,
            String instagram, MultipartFile file) throws IOException {
        Agent agent = agentRepo.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));
        agent.setName(name);
        agent.setPhone(phone);
        agent.setWhatsapp(whatsapp);
        agent.setTelegram(telegram);
        agent.setFacebook(facebook);
        agent.setInstagram(instagram);

        if (file != null && !file.isEmpty()) {
            Image image = new Image();
            image.setName(file.getOriginalFilename());
            image.setType(file.getContentType());
            image.setImage(ImageUtilities.compressImage(file.getBytes()));
            agent.setImage(image);
        }

        return agentRepo.save(agent);
    }

    @Override
    public Agent registerAgent(Agent agent) {
        return agentRepo.save(agent);
    }

    @Override
    public void deleteAgent(Long id) {
        agentRepo.deleteById(id);
    }

    @Override
    public Agent getAgentById(Long id) {
        return agentRepo.findById(id).orElse(null);
    }
}
