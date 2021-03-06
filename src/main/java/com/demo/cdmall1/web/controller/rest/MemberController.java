package com.demo.cdmall1.web.controller.rest;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;

import javax.servlet.http.*;
import javax.validation.*;
import javax.validation.constraints.*;

import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.*;
import org.springframework.security.web.authentication.logout.*;
import org.springframework.stereotype.*;
import org.springframework.validation.*;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.*;

import com.demo.cdmall1.domain.member.service.*;
import com.demo.cdmall1.util.*;
import com.demo.cdmall1.util.validation.annotation.*;
import com.demo.cdmall1.web.dto.*;

import lombok.*;

@RequiredArgsConstructor
@RestController
// @RequestParam에서 검증 활성화
@Validated
public class MemberController {
	private final MemberService service;
	
	
	// Java Generic, Java Reflection
	// Java Reflection : 자바 클래스를 풀어헤쳐서 필드, 메서드 등의 정보를 끄집어내거나 추가하는 기술 -> annotation은 모두 reflection 기반으로 처리 -> proxy 
	/*
	public void test() {
		// 자바 클래스를 읽어 온다(2번까지 실행)
		// 객체 초기화 순서 : 1. 인스턴스 초기화 -> 2. 정적 초기화 -> 3.생성자
		Class clz = Class.forName("java.lang.Integer");
		
		// 클래스를 조작 
		// 클래스에 들어있는 메소드의 배열을 읽어온다
		Method[] methods = clz.getMethods();
		for(Method m:methods) {
			if(m.getAnnotation(Service.class)!=null) {
				// 처리해라
			}
		}
		
		// 개조한 클래스의 객체를 생성한다
		Integer i = (Integer)clz.newInstance();
	}
	*/
	
	
	// 아이디 사용 여부
	@GetMapping(path="/members/username/check", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> idAvailableCheck(@Username String username) {
		service.idAvailabelCheck(username);
		return ResponseEntity.ok(ZmallResponseConstant.ID_AVAILABLE_MSG);
	}
	
	// 이메일 사용 여부
	@GetMapping(path="/members/email/check", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> emailAvailableCheck(@Email String email) {
		service.emailAvailabelCheck(email);
		return ResponseEntity.ok(ZmallResponseConstant.EMAIL_AVAILABLE_MSG);
	}
	
	// 회원 가입
	@PostMapping(path="/members/new", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> join(@Valid MemberDto.Join dto, BindingResult bindingResult) throws BindException {
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		service.join(dto);
		return ResponseEntity.ok(ZmallResponseConstant.JOIN_MSG);
	}
	
	// 회원 가입 확인 : 체크 코드를 확인한 다음 REST로 화면 이동을 시키자
	// 300대 상태 코드는 redirect를 의미한다
	@GetMapping("/members/join/check")
	public ResponseEntity<?> joinCheck(String checkcode) {
		service.joinCheck(checkcode);
		URI uri = UriComponentsBuilder.newInstance().path("/member/login").build().toUri();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(uri);
		return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
	}
	
	// 아이디 찾기
	@GetMapping(path="/members/member/email", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> findId(@Email String email) {
		URI uri = UriComponentsBuilder.newInstance().path("/member/login").build().toUri();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(uri);
		return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
	}
	
	// 비밀번호 리셋
	@PatchMapping(path="/members/member/password/temp", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> resetPassword(@Valid MemberDto.ResetPwd dto, BindingResult bindingResult) throws BindException {
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		service.resetPassword(dto);
		return ResponseEntity.ok(ZmallResponseConstant.RESET_PASSWORD_MSG);
	}
	
	// 임시 비밀번호로 로그인했을 때 비밀번호 변경
	@PreAuthorize("isAuthenticated()")
	@PatchMapping(path="/members/member/password", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> changePassword(@Valid MemberDto.ChangePwd dto, BindingResult bindingResult, Principal principal) throws BindException {
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		service.changePassword(dto, principal.getName());
		return ResponseEntity.ok(ZmallResponseConstant.CHANGE_PASSWORD_MSG);
	}
	
	// 비밀번호 확인
	@PreAuthorize("isAuthenticated()")
	@GetMapping(path="/members/member/password/check", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> checkPassword(@Password String password, Principal principal, HttpSession session) {
		service.checkPassword(password, principal.getName());
		session.setAttribute("passwordCheck", true);
		return ResponseEntity.ok(null);
	}
	
	// 내 정보 보기
	@PreAuthorize("isAuthenticated()")
	@GetMapping(path="/members/member", produces = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity<?> read(Principal principal, HttpSession session) {
		return ResponseEntity.ok(service.read(principal.getName()));
	}
	
	// 내 정보 변경
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/members/member")
	public ResponseEntity<?> update(@Valid MemberDto.Update dto, BindingResult bindingResult, Principal principal) throws BindException {
		if(bindingResult.hasErrors())
			throw new BindException(bindingResult);
		service.update(dto, principal.getName());
		return ResponseEntity.ok(null);
	}
	
	// 회원 탈퇴 후 루트 페이지로 이동(회원 탈퇴 버튼이 내정보에 있다. 따라서 루트로 강제이동 시키자)
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/members/member")
	public ResponseEntity<?> resign(SecurityContextLogoutHandler handler, HttpServletRequest req, HttpServletResponse res, Authentication authentication )  {
		service.resign(authentication.getName());
		handler.logout(req, res, authentication);
		return ResponseEntity.ok(null);
	}
	
	// 프사 출력
	@GetMapping(path="/display", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<?> showProfile(String imagename) {
		File file = new File(ZmallConstant.PROFILE_FOLDER + imagename);
		// 헤더를 만들어 MediaType을 이미지로 지정하고 Content-Disposition을 inline으로 주면 브라우저가 처리한다
		// Content-Disposition를 attachment로 브라우저가 다운로드한다
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(ZmallUtil.getMediaType(imagename));
		headers.add("Content-Disposition", "inline;filename="  + imagename);
		try {
			// 파일을 바이트 배열로 바꾸는 방법들이 여러가지 나오는데...
			return ResponseEntity.ok().headers(headers).body(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// 프사 이름를 리턴
	// @PreAuthorize("isAuthenticated()") -> RestTemplate은 MVC 로그인을 지원하지 않는다
	@GetMapping("/members/profile")
	public ResponseEntity<?> getProfile(@Username String username) {
		return ResponseEntity.ok(service.getProfile(username));
	}
}
