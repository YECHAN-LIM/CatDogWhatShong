package com.demo.cdmall1.web.controller.rest;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;

import javax.validation.*;

import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.*;

import com.demo.cdmall1.domain.imageboard.entity.*;
import com.demo.cdmall1.domain.product.entity.*;
import com.demo.cdmall1.domain.product.service.*;
import com.demo.cdmall1.util.*;
import com.demo.cdmall1.web.dto.*;

import lombok.*;

@RequiredArgsConstructor
@RestController
public class ProductController {
	private final ProductService service;
	// 이미지 첨부파일 보기
	@GetMapping(path={"/products/image", "/temp/image"}, produces=MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<?> showImage(@RequestParam String imagename) throws IOException {
		File file = new File(ZmallConstant.IMAGE_FOLDER + imagename);
		System.out.println(file);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(ZmallUtil.getMediaType(imagename));
		headers.add("Content-Disposition", "inline;filename="  + imagename);
		try {
			return ResponseEntity.ok().headers(headers).body(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	@PostMapping(path="/products/new", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> insert(@Valid ProductDto.Write dto, BindingResult bindingResult, Principal principal) throws BindException {
		System.out.println("***************dto: "+dto);
		
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		Product product= service.insert(dto, principal.getName());
		URI uri = UriComponentsBuilder.newInstance().path("/product/read").queryParam("pno", product.getPno()).build().toUri();
		return ResponseEntity.created(uri).body(product);
		
//		return ResponseEntity.ok(service.insert(dto));
	}
	
	
	
	
	
	
	@GetMapping("/products/{pno}")
	public ResponseEntity<?> read(@PathVariable Integer pno) {
		return ResponseEntity.ok(service.read(pno));
	}
	
	@GetMapping(path="/products/all", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> list(@RequestParam(defaultValue="1") Integer pageno, String writer) {
		System.out.println("===============================" + writer);
		return ResponseEntity.ok(service.list(pageno, writer));
	}
}
