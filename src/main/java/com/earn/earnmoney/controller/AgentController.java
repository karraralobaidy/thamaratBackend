package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.AgentService;
import com.earn.earnmoney.model.Agent;
import com.earn.earnmoney.model.Image;
import com.earn.earnmoney.util.ImageUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @GetMapping
    public ResponseEntity<List<Agent>> getAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/getimage/{id}")
    public ResponseEntity<byte[]> getAgentImage(@PathVariable Long id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        Agent agent = agentService.getAgentById(id);
        if (agent != null && agent.getImage() != null && agent.getImage().getImage() != null) {
            Image image = agent.getImage();
            byte[] imageData = ImageUtilities.decompressImage(image.getImage());
            String contentType = image.getType();
            return ResponseEntity.ok()
                    .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG)
                    .body(imageData);
        }
        return ResponseEntity.notFound().build();
    }
}
