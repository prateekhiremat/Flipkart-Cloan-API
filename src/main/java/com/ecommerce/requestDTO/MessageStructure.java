package com.ecommerce.requestDTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageStructure {
	private String to;
	private String subject;
	private Date sentDate;
	private String text;
}
