package br.com.agentdev;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;

import br.com.agentdev.commons.Diretorios;
import br.com.agentdev.commons.Utils;
import br.com.agentdev.core.GeradorCodigoJava;
import br.com.agentdev.core.ValidadorDePacotes;

public class Main {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		 
		System.out.println("🧠 Agente Gerador de Código Java!");
        System.out.println("📁 Deseja Informar o diretório do projeto Java? (default: diretório local) (S/N):");
       
        try {
        	// Localiza o projeto
        	String srcPath = obtemEnderecoProjeto(scanner);
            // Valida  se é um projeto Java Gradle
        	Optional<String> projetoBackendValido = validarProjeto(srcPath);
        	
            if (!projetoBackendValido.isEmpty()) {
            	
            	srcPath = projetoBackendValido.get();
             	String nomeProjeto = Utils.getNomeProjeto(srcPath);
             	System.out.println("✅ Diretório selecionado: " + srcPath);
             	System.out.println("✅ Nome do Projeto: " + nomeProjeto);
             	
             	// Valida os pacotes onde serão criados os arquivos java
             	ValidadorDePacotes validador = new ValidadorDePacotes();
             	Diretorios dir = validador.validarDiretorios(srcPath, nomeProjeto);

             	// Obtem o prompt do usuario
             	System.out.println("\n✍️ Informe os requisitos para criação do CRUD:");
                String prompt = scanner.nextLine();
                
                // Gera a implementação
                GeradorCodigoJava.execute(dir, prompt);
                System.out.println("✅ Código gerado com sucesso!");
                 
             } else {
             	System.err.println("⚠️ Projeto não foi identificado! Arquivo build.gradle não encontrado no diretório informado.");
             }
             
		} catch (Exception e) {
			System.err.println("⚠️ Erro ao processar dados, sistema encerrado! " + e.getMessage());
		} finally {
			scanner.close();
		}
        
    }

	private static String obtemEnderecoProjeto(Scanner scanner) {
		String srcPath = null;
		String resposta = scanner.nextLine().trim().toUpperCase();
        if ("N".equals(resposta)) {
        	String projectPath = System.getProperty("user.dir");
        	srcPath = Paths.get(projectPath, "src").toString();
        } else {
        	System.out.println("📁 Informe o diretório do projeto Java (uma janela será aberta para seleção):");
         	
         	// Pegando o diretório via JFileChooser
        	UIManager.put("FileChooser.useSystemExtensionHiding", Boolean.FALSE);
            File diretorio = selecionarDiretorio();
            if (diretorio == null) {
            	System.out.println("❌ Nenhum diretório foi selecionado. Encerrando.");
                scanner.close();
                return null;
            }
            
            srcPath = diretorio.getAbsolutePath();
        }
		return srcPath;
	}

	private static File selecionarDiretorio() {
        // Cria um frame invisível para garantir que o JFileChooser fique em foco
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setVisible(true);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecione o diretório do projeto");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int resultado = chooser.showOpenDialog(frame);
        frame.dispose(); // fecha o frame invisível

        if (resultado == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }
	 
    
    private static Optional<String> validarProjeto(String backendPath) {
    	
    	Optional<String> backendFolder = encontrarPastaComBuildGradle(new File(backendPath));
    	if (!backendPath.isBlank() && backendFolder.isEmpty()) {
    		return backendFolder;
    	}
    	
    	return backendFolder;
    }
    
    private static Optional<String> encontrarPastaComBuildGradle(File raiz) {
        if (!raiz.exists() || !raiz.isDirectory()) {
            return Optional.empty();
        }

        return buscarArquivo(raiz);
    }
    
    private static Optional<String> buscarArquivo(File pasta) {
    	 File[] arquivos = pasta.listFiles();
    	    if (arquivos == null) {
    	        return Optional.empty();
    	    }

    	    for (File arquivo : arquivos) {
    	        if (arquivo.isFile() && "build.gradle".equalsIgnoreCase(arquivo.getName())) {
    	            return Optional.of(arquivo.getParent());
    	        } else if (arquivo.isDirectory()) {
    	            Optional<String> encontrada = buscarArquivo(arquivo);
    	            if (encontrada.isPresent()) {
    	                return encontrada;
    	            }
    	        }
    	    }

    	    return Optional.empty();
    }
    
}
