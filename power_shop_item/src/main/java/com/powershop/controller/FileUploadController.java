package com.powershop.controller;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.powershop.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FastFileStorageClient storageClient;

     private List<String> typeList = Arrays.asList("image/jpeg", "image/gif", "image/png");

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        try {
            // 校验文件的类型
            String originalFilename = file.getOriginalFilename();
            if(!typeList.contains(file.getContentType())){
                return Result.error("文件类型不合法：" + originalFilename);
            }

            // 校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage == null){
                return Result.error("文件内容不合法：" + originalFilename);
            }

            // 保存到服务器
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            String fullPath = storePath.getFullPath();

            // 生成url地址，返回
            return Result.ok("http://image.powershop.com/"+fullPath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}
