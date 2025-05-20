package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class CriarRepository {

	public static void execute(Diretorios diretorios, String codigoJava) throws IOException {
	    
        // Obter o diretório do projeto
        String diretorio = diretorios.getRepository();
        
        // Obter o package
        String packageRepository = Utils.extrairPackage(diretorios.getRepository());
        codigoJava = Utils.atualizarPackage(codigoJava, packageRepository);
        
        // Cria a arquivo no projeto
        criarRepository(diretorio, codigoJava);

	}
	
    private static void criarRepository(String diretorio, String codigoJava) throws IOException {
        
        // Cria o diretório
        File classDir = new File(diretorio);
        classDir.mkdirs();

        // Extrai o nome da classe
        String nomeClasse = Utils.extrairNomeInterface(codigoJava);
        if (nomeClasse == null) {
            nomeClasse = "Repositorio"; // Nome padrão caso não encontre
        }

        // Cria o arquivo com o nome correto
        File classFile = new File(classDir, nomeClasse + ".java");
        classFile.createNewFile();

        // Escreve o conteúdo no arquivo
        Files.write(classFile.toPath(), codigoJava.getBytes());
        System.out.println("✅ Repository criado com sucesso: " + classFile.getAbsolutePath());
        
    }
}
