package com.rockranger.analyzer.resume.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.rockranger.analyzer.resume.exception.CloudinaryUploadException;
import com.rockranger.analyzer.resume.service.CloudinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public Map uploadFile(MultipartFile file, String folder) {
        try {
            Map uploadParams = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto",
                    "use_filename", true,
                    "unique_filename", true
            );
            return cloudinary.uploader().upload(file.getBytes(), uploadParams);
        } catch (IOException e) {
            logger.error("Cloudinary upload failed for file: {}", file.getOriginalFilename(), e);
            throw new CloudinaryUploadException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            logger.info("Cloudinary delete result for {}: {}", publicId, result);
        } catch (Exception e) {
            try {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
                logger.info("Cloudinary raw delete result for {}: {}", publicId, result);
            } catch (Exception ex) {
                logger.error("Failed to delete asset from Cloudinary: {}", publicId, ex);
            }
        }
    }
}
