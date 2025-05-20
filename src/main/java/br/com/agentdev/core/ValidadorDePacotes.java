package br.com.agentdev.core;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class ValidadorDePacotes {


    private final Scanner scanner = new Scanner(System.in);

    public Diretorios validarDiretorios(String caminhoProjeto, String nomeProjeto) throws Exception {
    	Diretorios diretorios = Utils.obterDiretorios();
    	
    	Diretorios dto = new Diretorios();

    	dto.setPath(caminhoProjeto);
    	dto.setNomeProjeto(nomeProjeto);
        dto.setEntity(validarOuSolicitar("entity", caminhoProjeto, diretorios.getEntity()));
        dto.setRepository(validarOuSolicitar("repository", caminhoProjeto, diretorios.getRepository()));
        dto.setService(validarOuSolicitar("service", caminhoProjeto, diretorios.getService()));
        dto.setMapper(validarOuSolicitar("mapper", caminhoProjeto, diretorios.getMapper()));
        dto.setMapperXML(validarOuSolicitar("mapperXML", caminhoProjeto, diretorios.getMapperXML()));
        dto.setDto(validarOuSolicitar("dto", caminhoProjeto, diretorios.getDto()));
        dto.setExceptions(validarOuSolicitar("exceptions", caminhoProjeto, diretorios.getExceptions()));
        dto.setEnumerator(validarOuSolicitar("enumerator", caminhoProjeto, diretorios.getEnumerator()));
        dto.setController(validarOuSolicitar("controller", caminhoProjeto, diretorios.getController()));

        return dto;
    }

    private String validarOuSolicitar(String nomePacote, String basePath, String caminhoRelativoEsperado) {
        String caminhoRelativo = caminhoRelativoEsperado;
        File dir = montarDiretorio(nomePacote, basePath, caminhoRelativo);

        if (dir != null && dir.exists() && dir.isDirectory()) {
            System.out.println("✅ Pacote \"" + nomePacote + "\" encontrado em: " + dir.getPath());
        } else {
            System.out.println("❌ Pacote \"" + nomePacote + "\" não encontrado em: " + basePath +" *** "+ caminhoRelativoEsperado);
            boolean valido = false;
            while (!valido) {
                System.out.print("Informe o caminho relativo correto para o pacote \"" + nomePacote + "\" (ex: \"" + caminhoRelativoEsperado + "\"): ");
                caminhoRelativo = scanner.nextLine().trim();
                dir = montarDiretorio(nomePacote, basePath, caminhoRelativo);

                if (dir != null && dir.exists() && dir.isDirectory()) {
                    valido = true;
                    System.out.println("✅ Novo caminho validado para \"" + nomePacote + "\": " + dir.getAbsolutePath());
                } else {
                    System.out.println("❌ Caminho informado não é válido. Tente novamente.");
                }
            }
        }

        return dir.getPath();
    }

    private File montarDiretorio(String nomePacote, String basePath, String caminhoRelativo) {
    	
    	File raiz = null;
    	if (nomePacote == "mapperXML") {
    		raiz = Paths.get(basePath, "src", "main", "resources").toFile();
    	} else {   	
    		raiz = Paths.get(basePath, "src", "main", "java").toFile();
    	}
    	
    	Optional<File> encontrado = encontrarPacote(raiz, caminhoRelativo);
    	if (encontrado.isPresent()) {
    	    return encontrado.get();
    	}
        return null;
    }
    
    private static Optional<File> encontrarPacote(File raiz, String caminhoRelativoAlvo) {
        if (raiz == null || !raiz.exists()) {
            return Optional.empty();
        }

        // Padroniza o caminho alvo com separadores do sistema operacional
        String caminhoNormalizado = caminhoRelativoAlvo.replace("/", File.separator).replace("\\", File.separator);

        File[] arquivos = raiz.listFiles();
        if (arquivos == null) {
            return Optional.empty();
        }

        for (File file : arquivos) {
            if (file.isDirectory()) {
                // Verifica se termina com o caminho alvo
                if (file.getPath().endsWith(caminhoNormalizado)) {
                    return Optional.of(file);
                }

                // Busca recursiva
                Optional<File> encontrado = encontrarPacote(file, caminhoRelativoAlvo);
                if (encontrado.isPresent()) {
                    return encontrado;
                }
            }
        }

        return Optional.empty();
    }


    
}
