package com.demo.cdmall1.util;

import java.io.*;

import org.springframework.http.*;
import org.springframework.web.multipart.*;

public class ZmallUtil {
	// 파일 업로드할 때 확장자 잘라내는 함수
	public static String getMultipartExtension(MultipartFile sajin) {
		int lastPositionOfDot = sajin.getOriginalFilename().lastIndexOf("."); 
		return sajin.getOriginalFilename().substring(lastPositionOfDot);
	}
	
	// 프사 저장하는 함수
	public static String saveProfile(MultipartFile sajin, String username) {
		if(sajin!=null && sajin.isEmpty()==false) {
			File file = new File(ZmallConstant.PROFILE_FOLDER, username + ZmallUtil.getMultipartExtension(sajin));
			try {
				// MultipartFile을 파일로 이동하는 함수. 이동하고 나면 MultipartFile은 사용이 불가능해진다
				sajin.transferTo(file);
				return file.getName();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ZmallConstant.DEFAULT_PROFILE_NAME;
	}
	
	// 이미지를 받아서 이미지의 종류를 리턴
	public static MediaType getMediaType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".")).toUpperCase();
		MediaType type = MediaType.IMAGE_JPEG;
		if(extension.equals("PNG"))
			type = MediaType.IMAGE_PNG;
		else if(extension.equals("GIF"))
			type=MediaType.IMAGE_GIF;
		return type;
	}
}
