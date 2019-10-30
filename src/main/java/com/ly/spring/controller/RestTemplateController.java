package com.ly.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
*@author: dt-ly
*@email:379944104@qq.com
*@version: V1.0
*@Date 2019年10月29日下午5:18:01
*
*/
@RestController
public class RestTemplateController {

	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/add")
	public String add(Integer a, Integer b) {
		String result = restTemplate.getForObject("http://spring-eureka-producer/produce/hello", String.class);
		return result;
	}
}
