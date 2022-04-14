package io.github.jihch.model;

import java.net.URI;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class M3U8 {

	private URI keyURI;
	
	private Collection<URI> tsURIs;

}