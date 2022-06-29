package com.demo.cdmall1.web.controller.mvc;

import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductMvcController {
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/product/insert")
	public void insert() {
	}
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/product/read")
	public void read() {
	}
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/product/list")
	public void list() {
	}
	
	
}
