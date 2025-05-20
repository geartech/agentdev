package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class CriarEnum {

	public static void execute(Diretorios diretorios, String codigoJavaEnum) throws IOException {
		
		// Obter o diretório `enums` do projeto
        String diretorioEnum = diretorios.getEnumerator();
        
        // Obter o package para armazenar o enum
        String packageEnum = Utils.extrairPackage(diretorios.getEnumerator());
        
        //System.out.println(pacoteEnum);
        codigoJavaEnum = Utils.atualizarPackage(codigoJavaEnum, packageEnum);
        
        // Cria o enum no projeto
        criarEnum(diretorioEnum, codigoJavaEnum);
        
	}
	
    private static void criarEnum(String diretorio, String codigoJavaEnum) throws IOException {

        // Cria o diretório do DTO
        File enumDir = new File(diretorio);
        enumDir.mkdirs();

        // Extrai o nome da classe
        String nomeEnum = extrairNomeEnum(codigoJavaEnum);
        if (nomeEnum == null) {
            nomeEnum = "Enumerador"; // Nome padrão caso não encontre
        }

        // Cria o arquivo da entidade com o nome correto
        File enumFile = new File(enumDir, nomeEnum + ".java");
        enumFile.createNewFile();

        // Escreve o conteúdo da entidade no arquivo
        Files.write(enumFile.toPath(), codigoJavaEnum.getBytes());
        System.out.println("✅ Enum criado com sucesso: " + enumFile.getAbsolutePath());

    }
    
    private static String extrairNomeEnum(String implementacao) {
        // Regex para encontrar o nome do enum após a palavra-chave "enum"
        Pattern pattern = Pattern.compile("\\benum\\s+(\\w+)\\b");
        Matcher matcher = pattern.matcher(implementacao);
        if (matcher.find()) {
            return matcher.group(1); // Retorna o nome do enum encontrado
        }
        return null; // Retorna null caso não encontre
    }
    
}
