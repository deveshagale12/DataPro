package com.DataPro.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SupabaseStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Upload encrypted bytes to Supabase bucket ────────────────────
 // ── Upload encrypted bytes to Supabase bucket ────────────────────
    public String uploadFile(String filePath, byte[] encryptedData, String contentType) throws Exception {
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        
        // Dynamic content type evaluation to pass Supabase's restriction filters
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("x-upsert", "false"); // reject if already exists

        HttpEntity<byte[]> entity = new HttpEntity<>(encryptedData, headers);
        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("File uploaded to Supabase: {}", filePath);
            return buildPublicUrl(filePath);
        } else {
            throw new RuntimeException("Supabase upload failed: " + response.getStatusCode());
        }
    }

    // ── Download encrypted bytes from Supabase bucket ────────────────
    public byte[] downloadFile(String filePath) throws Exception {
        String downloadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, entity, byte[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Supabase download failed: " + response.getStatusCode());
        }
    }

    // ── Delete file from Supabase bucket ─────────────────────────────
    public void deleteFile(String filePath) {
        try {
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);
            log.info("Deleted from Supabase: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to delete from Supabase: {}", e.getMessage());
        }
    }

    // ── Build public URL ──────────────────────────────────────────────
    public String buildPublicUrl(String filePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filePath;
    }
}