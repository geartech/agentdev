package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class CriarException {

	public static void execute(Diretorios diretorios, String codigoJava) throws IOException {
	    
        // Obter o diretório do projeto
        String diretorio = diretorios.getExceptions();
        
        // Obter o package
        String packageException = Utils.extrairPackage(diretorios.getExceptions());
        codigoJava = Utils.atualizarPackage(codigoJava, packageException);
        
        // Cria a arquivo no projeto
        criarException(diretorio, codigoJava);

	}
	
    private static void criarException(String diretorio, String codigoJava) throws IOException {
        
        // Cria o diretório
        File classDir = new File(diretorio);
        classDir.mkdirs();

        // Extrai o nome da classe
        String nomeClasse = Utils.extrairNomeClass(codigoJava);
        if (nomeClasse == null) {
            nomeClasse = "Excecao"; // Nome padrão caso não encontre
        }

        // Cria o arquivo com o nome correto
        File classFile = new File(classDir, nomeClasse + ".java");
        classFile.createNewFile();

        // Escreve o conteúdo no arquivo
        Files.write(classFile.toPath(), codigoJava.getBytes());
        System.out.println("✅ Exception criado com sucesso: " + classFile.getAbsolutePath());
        
    }
}
