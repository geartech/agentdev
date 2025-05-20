package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class CriarService {

	public static void execute(Diretorios diretorios, String codigoJava) throws IOException {
	    
        // Obter o diretório do projeto
        String diretorio = diretorios.getService();
        
        // Obter o package
        String packageService = Utils.extrairPackage(diretorios.getService());
        codigoJava = Utils.atualizarPackage(codigoJava, packageService);
        
        // Cria a arquivo no projeto
        criarService(diretorio, codigoJava);

	}
	
    private static void criarService(String diretorio, String codigoJava) throws IOException {
        
        // Cria o diretório
        File classDir = new File(diretorio);
        classDir.mkdirs();

        // Extrai o nome da classe
        String nomeClasse = Utils.extrairNomeClass(codigoJava);
        if (nomeClasse == null) {
            nomeClasse = "Servico"; // Nome padrão caso não encontre
        }

        // Cria o arquivo com o nome correto
        File classFile = new File(classDir, nomeClasse + ".java");
        classFile.createNewFile();

        // Escreve o conteúdo no arquivo
        Files.write(classFile.toPath(), codigoJava.getBytes());
        System.out.println("✅ Service criado com sucesso: " + classFile.getAbsolutePath());
        
    }
}
