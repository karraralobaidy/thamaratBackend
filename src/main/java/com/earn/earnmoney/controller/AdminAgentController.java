package com.earn.earnmoney.controller;

import com.earn.earnmoney.Service.AgentService;
import com.earn.earnmoney.model.Agent;
import com.earn.earnmoney.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/agents")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAgentController {

    @Autowired
    private AgentService agentService;

    @GetMapping
    public ResponseEntity<List<Agent>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addAgent(
            @RequestParam("name") String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "whatsapp", required = false) String whatsapp,
            @RequestParam(value = "telegram", required = false) String telegram,
            @RequestParam(value = "facebook", required = false) String facebook,
            @RequestParam(value = "instagram", required = false) String instagram,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        agentService.registerAgent(name, phone, whatsapp, telegram, facebook, instagram, notes, image);
        return ResponseEntity.ok(new MessageResponse("تمت إضافة الوكيل بنجاح"));
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAgent(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "whatsapp", required = false) String whatsapp,
            @RequestParam(value = "telegram", required = false) String telegram,
            @RequestParam(value = "facebook", required = false) String facebook,
            @RequestParam(value = "instagram", required = false) String instagram,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        agentService.updateAgent(id, name, phone, whatsapp, telegram, facebook, instagram, notes, image);
        return ResponseEntity.ok(new MessageResponse("تم تحديث بيانات الوكيل بنجاح"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.ok(new MessageResponse("تم حذف الوكيل بنجاح"));
    }
}
