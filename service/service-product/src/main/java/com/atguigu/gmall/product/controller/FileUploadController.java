package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/product")
public class FileUploadController {


    @Value("${minio.endpointUrl}")
    private String endpointUrl;

    @Value("${minio.accessKey}")
    private String accessKey;


    @Value("${minio.secreKey}")
    private String secreKey;

    @Value("${minio.bucketName}")
    private String bucketName;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file){
        String url="";

        try {
            // 创建minIo客户端对象
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpointUrl)
                            .credentials(accessKey, secreKey)
                            .build();

            // 查询bucket是否存在
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                // 不存在，创建存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                System.out.println("Bucket 'asiatrip' already exists.");
            }


            //文件名称
            String fileName= UUID.randomUUID().toString().replaceAll("-","")+file.getOriginalFilename();

            //文件流上传实现
            // Upload known sized input stream.
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                            file.getInputStream(),file.getSize() , -1)
                            .contentType(file.getContentType())
                            .build());
         //访问路径
          url=endpointUrl+"/"+bucketName+"/"+fileName;



        } catch (Exception e) {
            e.printStackTrace();
        }


        return Result.ok(url);
    }




}
