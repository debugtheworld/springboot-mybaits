package com.xw.test.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class PictureController {

	@RequestMapping(value = "/pic/add", method = RequestMethod.POST)
	@ResponseBody
	public String uploadPictures(@RequestParam MultipartFile[] pics, HttpServletRequest request) {
		uploadFileList(pics, request);
		return "";
	}

	private void uploadFileList(MultipartFile[] pics, HttpServletRequest request) {
		for (MultipartFile pic : pics) {
			// 文件的原始名称
			String originalFilename = pic.getOriginalFilename();
			String newFileName = null;
			if (pic != null && originalFilename != null && originalFilename.length() > 0) {

				newFileName = System.currentTimeMillis() + originalFilename;
				// 存储图片的物理路径
				String pic_path = request.getSession().getServletContext().getRealPath("/upload/");
				// 新图片路径
				File targetFile = new File(pic_path, newFileName);
				System.out.println(pic_path + newFileName);
				try {
					// 转存文件到指定的路径
					pic.transferTo(targetFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
