package br.com.agentdev;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TesteRecurso {

	 public static void main(String[] args) {
	        try (InputStream input = TesteRecurso.class.getClassLoader().getResourceAsStream("templates/templateEntidade.txt")) {
	            if (input == null) {
	                System.out.println("❌ Arquivo NÃO encontrado.");
	            } else {
	                String conteudo = new String(input.readAllBytes(), StandardCharsets.UTF_8);
	                System.out.println("✅ Arquivo encontrado! Conteúdo:");
	                System.out.println(conteudo);
	            }
	        } catch (Exception e) {
	            System.out.println("Erro: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
}
