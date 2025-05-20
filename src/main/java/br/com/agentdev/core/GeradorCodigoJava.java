package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.agentdev.client.ModeloIAClient;
import br.com.agentdev.commons.AppProperties;
import br.com.agentdev.commons.CodigoJavaExtractor;
import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;
import br.com.agentdev.enums.TipoCodigoJava;


public class GeradorCodigoJava {

    public static void execute(Diretorios diretorios, String prompt) throws Exception {
        
    	// Cria a branch com base na checkout atual
    	String pathGit = diretorios.getPath().replace(diretorios.getNomeProjeto(), ".git");
    	
    	Git git = abrirRepositorio(pathGit);
    	if (git != null) {
    		try {
				criarBranch(git);
			} catch (GitAPIException e) {
				System.out.println("⚠️ Falha na criação da branch: " + e.getMessage());
			}
    	}
    	
    	
        String implementacaoEntidade = implementarEntidade(diretorios, prompt);
        String implementacaoRepositorio = implementarRepositorio(diretorios, implementacaoEntidade);
//        String implementacaoService = implementarService(diretorios, implementacaoEntidade, implementacaoRepositorio);
//        implementarMapper(diretorios, prompt);
//        implementarController(diretorios, prompt);
//        implementarTestes(diretorios, prompt);
        
    }

    private static Git abrirRepositorio(String pathGit) {
    	
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
       
		try {
			 Repository repository = builder.setGitDir(new File(pathGit))
			        .readEnvironment()
			        .findGitDir()
			        .build();
			 return new Git(repository);
		} catch (IOException e) {
			 System.out.println("⚠️ Nenhum repositório clonado para criação da branch");
			 return null;
		}
    }
    
    public static String criarBranch(Git git) throws GitAPIException {
        String nomeBranch = "feature/AGENTDEV_CRUD_"+ LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();

        try {
            git.branchCreate()
                    .setName(nomeBranch)
                    .call();
            System.out.println("✅ Branch criada: " + nomeBranch);
        } catch (RefAlreadyExistsException e) {
            System.out.println("⚠️ Branch já existe: " + nomeBranch);
        }

        // Faz checkout no novo branch
        git.checkout()
                .setName(nomeBranch)
                .call();
        System.out.println("🚀 Checkout realizado para o branch: " + nomeBranch);

        return nomeBranch;
    }
    
	private static String implementarEntidade(Diretorios diretorios, String prompt) throws Exception {
		
		String respostaAPI = null;
		String modeloIA = AppProperties.get("app.modeloia.nome");
	    String urlModeloIA = AppProperties.get("app.modeloia.server");
	        
		try {
            // Lê o conteúdo do template do arquivo templateEntidade.txt
            String promptEntidade = Utils.obterTemplate("templateEntidade.txt");

            if (promptEntidade != null) {
            	// Solicita a IA para implementar a entidade
                ModeloIAClient client = new ModeloIAClient(
                		urlModeloIA,
                		modeloIA,
                        promptEntidade
                );

                respostaAPI = client.execute(prompt);
                
                int tentativas = 0;
                ObjectMapper objectMapper = new ObjectMapper();
                boolean jsonValido = false;
                
                String respostaAPICorrigida = respostaAPI;
         
                while (tentativas < 3 && !jsonValido) {
                    try {
                        objectMapper.readTree(respostaAPICorrigida); // Valida JSON
                        respostaAPI = respostaAPICorrigida;
                        jsonValido = true;
                    } catch (Exception e) {
                        tentativas++;
                        System.out.println("❌ JSON inválido na tentativa " + tentativas + ": " + e.getMessage());
                        respostaAPICorrigida = client.execute(prompt);
                    }
                }

                if (!jsonValido) {
                    throw new RuntimeException("❌ Falha ao validar a resposta do Agent após 3 tentativas.");
                }

            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao conectar à API de IA: " + e.getMessage());
            System.out.println("❌ Sistema Encerrado! Erro ao conectar à API de IA.");
            throw e;
        }

        try {

            List<String> codigoJavaEntidade = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.ENTITY);
            List<String> codigoJavaDTO = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.DTO);
            List<String> codigoJavaEnum = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.ENUM);
            
            List<String> nomesEnum = null;
            if (codigoJavaEnum != null && codigoJavaEnum.size() > 0) {
            	nomesEnum = new ArrayList<String>();
            	for (String javaEnum : codigoJavaEnum) {
        			nomesEnum.add(Utils.extrairNomeEnum(javaEnum));
        			CriarEnum.execute(diretorios, javaEnum);
				}
        	}
             
            List<String> nomesDto = null;
            if (codigoJavaDTO != null && codigoJavaDTO.size() > 0) {
            	nomesDto = new ArrayList<String>();
            	for (String javaDTO : codigoJavaDTO) {
            		nomesDto.add(Utils.extrairNomeClass(javaDTO));
            		
            		if (nomesEnum != null && nomesEnum.size() > 0) {
                		for (String nomeEnum : nomesEnum) {
                			javaDTO = Utils.incluirImport(diretorios, javaDTO, TipoCodigoJava.ENUM, nomeEnum);	
    					}
                	}
            		CriarDTO.execute(diretorios, javaDTO);	
				}
            }
            
            if (codigoJavaEntidade != null && codigoJavaEntidade.size() > 0) {
            	for (String javaEntidade : codigoJavaEntidade) {
            		
            		if (nomesEnum != null && nomesEnum != null) {
            			for (String nomeEnum : nomesEnum) {
            				javaEntidade = Utils.incluirImport(diretorios, javaEntidade, TipoCodigoJava.ENUM, nomeEnum);	
    					}
                	}	
            		
            		if (nomesDto != null && nomesDto != null) {
            			for (String nomeDto : nomesDto) {
            				javaEntidade = Utils.incluirImport(diretorios, javaEntidade, TipoCodigoJava.DTO, nomeDto);	
    					}
                	}
            		CriarEntidade.execute(diretorios, javaEntidade);
				}
            	
            }

            return respostaAPI;
            
        } catch (Exception e) {
            System.out.println("❌ Erro ao criar Entidade: " + e.getMessage());
            System.out.println("❌ Erro ao criar Entidade!");
            throw e;
        }
       
	}

	private static String implementarRepositorio(Diretorios diretorios, String implementacaoEntidade) throws Exception {
		
		String respostaAPI = null;
		String modeloIA = AppProperties.get("app.modeloia.nome");
	    String urlModeloIA = AppProperties.get("app.modeloia.server");
	    List<String> codigoJavaEntidade = CodigoJavaExtractor.getCodigoJavaByTipo(implementacaoEntidade, TipoCodigoJava.ENTITY);
	    
		try {
            // Lê o conteúdo do template
            String templateRepositorio = Utils.obterTemplate("templateRepositorio.txt");
            
            if (templateRepositorio != null) {
            	String prompt = String.format(templateRepositorio, codigoJavaEntidade.get(0));
            	// Solicita a IA para implementar
                ModeloIAClient client = new ModeloIAClient(
                		urlModeloIA,
                		modeloIA,
                        "Você é um programador sênior. "
                        + "Gere a implementação exata conforme solicitado, seguindo rigorosamente o padrão fornecido. "
                );
                respostaAPI = client.execute(prompt);
            }
            
        } catch (Exception e) {
        	 System.out.println("❌ Erro ao conectar à API de IA: " + e.getMessage());
             System.out.println("❌ Sistema Encerrado! Erro ao conectar à API de IA.");
             throw e;
        }
		
		try {

			List<String> codigoJavaRepository = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.REPOSITORY);
			List<String> codigoJavaException = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.EXCEPTION);
           
            if (codigoJavaRepository != null && codigoJavaRepository.size() > 0) {
            	for (String javaRepository : codigoJavaRepository) {
            		CriarRepository.execute(diretorios, javaRepository);	
				}	
            }
            
            if (codigoJavaException != null && codigoJavaException.size() > 0) {
            	for (String javaException : codigoJavaException) {
            		CriarException.execute(diretorios, javaException);	
				}
            }

            List<String> nomesException = null;
            if (codigoJavaException != null && codigoJavaException.size() > 0) {
            	nomesException = new ArrayList<String>();
            	for (String javaException : codigoJavaException) {
            		nomesException.add(Utils.extrairNomeClass(javaException));
            		
            		if (nomesException != null && nomesException.size() > 0) {
                		for (String nomeException : nomesException) {
                			javaException = Utils.incluirImport(diretorios, javaException, TipoCodigoJava.EXCEPTION, nomeException);	
    					}
                		CriarException.execute(diretorios, javaException);			
                	}	
				}
            }
            
            return respostaAPI;
            
        } catch (Exception e) {
        	 System.out.println("❌ Erro ao criar Repositorio: " + e.getMessage());
             System.out.println("❌ Erro ao criar Repositorio!");
             throw e;
        }
		
	}
	
	/*
	private static String implementarService(Diretorios diretorios, String implementacaoEntidade, String implementacaoRepositorio) throws Exception {
		
		String respostaAPI = null;
		String modeloIA = AppProperties.get("app.modeloia.nome");
	    String urlModeloIA = AppProperties.get("app.modeloia.server");
	    String codigoJavaEntidade = CodigoJavaExtractor.getCodigoJavaByTipo(implementacaoEntidade, TipoCodigoJava.ENTITY);
	    String codigoJavaRepositorio = CodigoJavaExtractor.getCodigoJavaByTipo(implementacaoRepositorio, TipoCodigoJava.REPOSITORY);
	    
		try {
            // Lê o conteúdo do template
            String templateService = Utils.obterTemplate("templateService.txt");
            
            if (templateService != null) {
            	String prompt = String.format(templateService, codigoJavaEntidade, codigoJavaRepositorio);
            	// Solicita a IA para implementar
                ModeloIAClient client = new ModeloIAClient(
                		urlModeloIA,
                		modeloIA,
                        "Você é um programador sênior. "
                        + "Gere a implementação exata conforme solicitado, seguindo rigorosamente o padrão fornecido. "
                );
                respostaAPI = client.execute(prompt);
            }
            
        } catch (Exception e) {
        	 System.out.println("❌ Erro ao conectar à API de IA: " + e.getMessage());
             System.out.println("❌ Sistema Encerrado! Erro ao conectar à API de IA.");
             throw e;
        }
		
		try {

            String codigoJavaService = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.SERVICE);
            String codigoJavaExceptions = CodigoJavaExtractor.getCodigoJavaByTipo(respostaAPI, TipoCodigoJava.EXCEPTIONS);
           
            if (codigoJavaService != null) {
                CriarService.execute(diretorios, codigoJavaService);
            } else {
                System.out.println("Nenhum objeto com o tipo '" + TipoCodigoJava.SERVICE
                        + "' foi definido.");
            }
            
            if (codigoJavaExceptions != null) {
            	CriarException.execute(diretorios, codigoJavaExceptions);
            } else {
                System.out.println("Nenhum objeto com o tipo '" + TipoCodigoJava.EXCEPTION
                        + "' foi definido.");
            }

            return respostaAPI;
            
        } catch (Exception e) {
        	 System.out.println("❌ Erro ao criar Repositorio: " + e.getMessage());
             System.out.println("❌ Erro ao criar Repositorio!");
             throw e;
        }
		
	}
	*/
	
	private static void implementarMapper(Diretorios diretorios, String prompt) {
		// TODO Auto-generated method stub
		
	}

	private static void implementarController(Diretorios diretorios, String prompt) {
		// TODO Auto-generated method stub
		
	}

	private static void implementarTestes(Diretorios diretorios, String prompt) {
		// TODO Auto-generated method stub
		
	}

}


