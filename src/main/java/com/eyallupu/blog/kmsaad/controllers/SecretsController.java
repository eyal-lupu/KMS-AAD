package com.eyallupu.blog.kmsaad.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecretsController {

	private Map<String, String> secrets = new HashMap<>();

	@RequestMapping(path = "/put/{name}", method = RequestMethod.POST)
	public ResponseEntity<String> put(@PathVariable("name") String name, @RequestBody String value) {
		if (null == value) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		if (null == secrets.put(name, value)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}
}