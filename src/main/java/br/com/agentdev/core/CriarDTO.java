package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class CriarDTO {

	public static void execute(Diretorios diretorios, String codigoJavaDTO) throws IOException {
		 
		// Obter o diretório `dto` do projeto
        String diretorioDTO = diretorios.getDto();
        
        // Obter o package para armazenar DTOS
        String packageDTO = Utils.extrairPackage(diretorios.getDto());
        
        //System.out.println(pacoteDTO);
        codigoJavaDTO = Utils.atualizarPackage(codigoJavaDTO, packageDTO);
        
        // Cria a entidade no projeto
        criarDTO(diretorioDTO, codigoJavaDTO);
        
	}
	
    private static void criarDTO(String diretorio, String codigoJavaDTO) throws IOException {

        // Cria o diretório do DTO
        File dtoDir = new File(diretorio);
        dtoDir.mkdirs();

        // Extrai o nome da classe
        String nomeClasse = Utils.extrairNomeClass(codigoJavaDTO);
        if (nomeClasse == null) {
            nomeClasse = "DadosEntidade"; // Nome padrão caso não encontre
        }

        // Cria o arquivo da entidade com o nome correto
        File dtoFile = new File(dtoDir, nomeClasse + ".java");
        dtoFile.createNewFile();

        // Escreve o conteúdo da entidade no arquivo
        Files.write(dtoFile.toPath(), codigoJavaDTO.getBytes());
        System.out.println("✅ DTO criado com sucesso: " + dtoFile.getAbsolutePath());

    }
    
}
