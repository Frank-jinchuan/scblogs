package cn.sticki.resource.controller;

import cn.sticki.common.result.RestResult;
import cn.sticki.resource.service.ImageService;
import cn.sticki.resource.type.FileType;
import cn.sticki.resource.utils.FileUtils;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * 上传图片相关接口
 *
 * @author 阿杆
 */
@Slf4j
@RestController
@RequestMapping("/private/resource")
@Validated
public class UploadController {

	@Resource
	private ImageService imageService;

	/**
	 * 上传图片
	 *
	 * @param file 图片文件
	 * @return 图片链接
	 */
	@PostMapping("/image")
	public String uploadBlogImage(@NotNull MultipartFile file) throws MinioException, IOException {
		log.debug("uploadBlogImage, fileName->{}", file.getOriginalFilename());
		FileUtils.checkFile(file, 1024 * 1024L, FileType.JPEG, FileType.PNG);
		return imageService.uploadBlogImage(file);
	}

	/**
	 * 上传头像
	 *
	 * @param file 头像文件
	 * @return 图片链接
	 */
	@PostMapping("/avatar")
	public RestResult<String> uploadAvatar(@NotNull MultipartFile file, @NotNull String name) {
		log.debug("uploadAvatar, fileName->{}", file.getOriginalFilename());
		log.info("上传了头像{}", name);
		FileUtils.checkFile(file, 1024 * 1024L, FileType.JPEG, FileType.PNG);
		String s = imageService.uploadAvatar(file, name);
		if (s == null) {
			return RestResult.fail("上传头像失败");
		}
		return RestResult.ok(s);
	}

}
