package com.demo.cdmall1.web.controller.rest;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;

import javax.servlet.http.*;
import javax.validation.*;

import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.validation.*;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;
import org.springframework.web.util.*;

import com.demo.cdmall1.domain.board.entity.*;
import com.demo.cdmall1.domain.board.service.*;
import com.demo.cdmall1.util.*;
import com.demo.cdmall1.web.dto.*;

import lombok.*;

@RequiredArgsConstructor
@RestController
public class BoardController {
	private final BoardService service;
	// 이미지 첨부파일 보기
	@GetMapping(path={"/board/image", "/temp/image"}, produces=MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<?> showImage(@RequestParam String imagename, HttpServletRequest req) throws IOException {
		
		// 호출한 주소에 따라 폴더명을 계산하자
		String command = req.getRequestURI().substring(1, req.getRequestURI().lastIndexOf("/"));
		File file = new File(ZmallConstant.TEMP_FOLDER + imagename);
		if(command.equals("board"))
			file = new File(ZmallConstant.IMAGE_FOLDER + imagename);
		
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
	
	// ck 이미지 업로드
	@PostMapping(value="/board/image", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> ckImageUpload(MultipartFile upload) {
		return ResponseEntity.ok(service.ckImageUpload(upload));
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping(path="/board/new", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> write(@Valid BoardDto.Write dto, BindingResult bindingResult, Principal principal) throws BindException {
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		System.out.println(dto.getContent());
		Board board = service.write(dto, principal.getName());
		URI uri = UriComponentsBuilder.newInstance().path("/board/read").queryParam("bno", board.getBno()).build().toUri();
		return ResponseEntity.created(uri).body(board);
	}
	
	@GetMapping(path="/board/{bno}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> read(@PathVariable Integer bno, Principal principal) {
		String username = (principal==null)? null : principal.getName();
		return ResponseEntity.ok(service.read(bno, username));
	}
	
	@GetMapping(path="/board/all", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> list(@RequestParam(defaultValue="1") Integer pageno, String writer) {
		return ResponseEntity.ok(service.list(pageno, writer));
	}
	
	
	@PutMapping(path="/board/{bno}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> update(@Valid BoardDto.Update dto, BindingResult bindingResult, Principal principal) throws BindException {
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		return ResponseEntity.ok(service.update(dto, principal.getName()));
	}
	
	@DeleteMapping(path="/board/{bno}")
	public ResponseEntity<?> delete(@PathVariable Integer bno, Principal principal) throws BindException {
		return ResponseEntity.ok(service.delete(bno, principal.getName()));
	}
		
	@PostMapping("/board/comments")
	public ResponseEntity<?> CommentCnt(@RequestParam Integer bno) {
		Integer cnt = service.updateCommentCnt(bno);
		return ResponseEntity.ok(cnt);
	}
	
	@GetMapping("/board/good_or_bad")
	public ResponseEntity<?> GoodOrBadCnt(@RequestParam Integer bno, @RequestParam Integer state) {
		Integer cnt = service.goodOrBad(bno, state);
		return ResponseEntity.ok(cnt);
	}
	
	@PostMapping("/board/attachment")
	public ResponseEntity<?> updateAttachmentCnt(Integer bno) {
		Integer cnt = service.updateAttachment(bno);
		return ResponseEntity.ok(cnt);
	}
	
}





