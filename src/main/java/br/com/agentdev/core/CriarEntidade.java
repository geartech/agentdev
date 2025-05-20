package br.com.agentdev.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;

public class CriarEntidade {

	public static void execute(Diretorios diretorios, String codigoJavaEntidade) throws IOException {
		    
        // Obter o diretório `entities` do projeto
        String diretorioEntidade = diretorios.getEntity();
        
        // Obter o package para armazenar entidades
        String packageEntidade = Utils.extrairPackage(diretorios.getEntity());
        codigoJavaEntidade = Utils.atualizarPackage(codigoJavaEntidade, packageEntidade);
        
        // Cria a entidade no projeto
        criarEntidade(diretorioEntidade, codigoJavaEntidade);

	}
	
    private static void criarEntidade(String diretorio, String codigoJavaEntidade) throws IOException {
        
        // Cria o diretório da entidade
        File entidadeDir = new File(diretorio);
        entidadeDir.mkdirs();

        // Extrai o nome da classe
        String nomeClasse = Utils.extrairNomeClass(codigoJavaEntidade);
        if (nomeClasse == null) {
            nomeClasse = "Entidade"; // Nome padrão caso não encontre
        }

        // Cria o arquivo da entidade com o nome correto
        File entidadeFile = new File(entidadeDir, nomeClasse + ".java");
        entidadeFile.createNewFile();

        // Escreve o conteúdo da entidade no arquivo
        Files.write(entidadeFile.toPath(), codigoJavaEntidade.getBytes());
        System.out.println("✅ Entidade criada com sucesso: " + entidadeFile.getAbsolutePath());
        
    }
    
}
