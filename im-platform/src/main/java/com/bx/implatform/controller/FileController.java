package com.bx.implatform.controller;

import cn.hutool.core.util.RandomUtil;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.thirdparty.FileService;
import com.bx.implatform.vo.UploadImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Slf4j
@RestController
@Tag(name = "文件上传")
public class FileController {

    @CrossOrigin
    @Operation(summary = "上传文件", description = "上传文件，上传后返回文件url")
    @PostMapping("/file/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(handleImageUpload(file).getData().getOriginUrl());
    }


    private final Path uploadDir = Paths.get("uploads");

    public FileController() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @PostMapping("/image/upload")
    @ResponseBody
    public Result<UploadImageVO>  handleImageUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResultUtils.error(-1, "no upload file");
        }

        LocalDate now = LocalDate.now();
        String today = now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth();

        try {
            // 获取文件名
            String fileName = file.getOriginalFilename();
            String newName = today+"/" + System.currentTimeMillis()+ RandomUtil.randomString(6) + fileName.substring(fileName.lastIndexOf("."));
            // 构建文件保存路径
            Path filePath = uploadDir.resolve(newName);
            // 保存文件到指定路径
            File newFile = filePath.getParent().toFile();
            if(!newFile.exists()){
                newFile.mkdirs();
            }
            Files.copy(file.getInputStream(), filePath);

            // 构建文件访问URL
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("download/")
                    .path(newName)
                    .toUriString();
            UploadImageVO vo = new UploadImageVO();
            vo.setOriginUrl(fileDownloadUri);
            vo.setThumbUrl(fileDownloadUri);
            return ResultUtils.success(vo);
        } catch (IOException e) {
            e.printStackTrace();
            return ResultUtils.error(-1,"上传失败");
        }
    }

    @GetMapping("/download/{filePath}/{fileName}")
    @SneakyThrows
    public ResponseEntity<Resource> downloadFile(@PathVariable("filePath") String filePath , @PathVariable("fileName") String fileName , HttpServletResponse response) {

        response.reset();
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader(
                "Content-disposition",
                "attachment; filename=" + fileName);
        Path fileAllPath = uploadDir.resolve(filePath + File.separator +fileName);
        Resource resource = new UrlResource(fileAllPath.toUri());
        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(resource.getFile()));
                // 输出流
                BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        ) {
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = bis.read(buff)) > 0) {
                bos.write(buff, 0, len);
            }
        }
        return null;
    }

}