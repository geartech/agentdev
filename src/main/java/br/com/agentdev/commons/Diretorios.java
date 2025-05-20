package br.com.agentdev.commons;

public class Diretorios {

    private String path;
    private String nomeProjeto;
    private String entity;
    private String repository;
    private String service;
    private String mapper;
    private String mapperXML;
    private String dto;
    private String exceptions;
    private String enumerator;
    private String controller;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNomeProjeto() {
		return nomeProjeto;
	}

	public void setNomeProjeto(String nomeProjeto) {
		this.nomeProjeto = nomeProjeto;
	}

	public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMapper() {
        return mapper;
    }

    public void setMapper(String mapper) {
        this.mapper = mapper;
    }

    public String getMapperXML() {
		return mapperXML;
	}

	public void setMapperXML(String mapperXML) {
		this.mapperXML = mapperXML;
	}

	public String getDto() {
        return dto;
    }

    public void setDto(String dto) {
        this.dto = dto;
    }

    public String getExceptions() {
        return exceptions;
    }

    public void setExceptions(String exceptions) {
        this.exceptions = exceptions;
    }

    public String getEnumerator() {
        return enumerator;
    }

    public void setEnumerator(String enumerator) {
        this.enumerator = enumerator;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

}
