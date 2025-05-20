package br.com.agentdev.commons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.agentdev.enums.TipoCodigoJava;


public class Utils {

    public static String extrairMensagemClient(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "❌ Erro ao processar a resposta da API: " + e.getMessage();
        }
    }

    public static String toJsonString(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter objeto para JSON", e);
        }
    }

    public static String getNomeProjeto(String projectPath) {
        File gradleFile = new File(Paths.get(projectPath, "settings.gradle").toString());

        if (!gradleFile.exists()) {
            gradleFile = new File(Paths.get(projectPath, "build.gradle").toString());
        }

        if (!gradleFile.exists()) {
            return "Desconhecido";
        }

        try (Stream<String> stream = Files.lines(gradleFile.toPath())) {
            return stream.filter(line -> line.trim().startsWith("rootProject.name"))
                    .map(line -> line.replace("rootProject.name", "").replace("=", "")
                            .replace("\"", "").replace("'", "").trim())
                    .findFirst().orElse("Desconhecido");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Desconhecido";
    }
    

    // Lê o conteúdo do arquivo de template.
    public static String lerTemplate(String caminhoArquivo) throws Exception {
        // System.out.println(Paths.get(caminhoArquivo));
        return new String(Files.readAllBytes(Paths.get(caminhoArquivo)), StandardCharsets.UTF_8);
    }

    public static String obterTemplate(String nomeTemplate) {
    	try (InputStream input = Utils.class.getClassLoader().getResourceAsStream("templates/" + nomeTemplate)) {
            if (input == null) {
                System.out.println("❌ Arquivo NÃO encontrado.");
            } else {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
        	System.out.println("❌ Arquivo NÃO encontrado. FALHA: "+ e.getMessage());
        }
    	return null;
    }

    // Método recursivo para buscar um arquivo pelo nome em um diretório e seus subdiretórios.
    public static void procurarArquivo(File directory, String fileName, List<File> foundFiles) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Busca recursivamente em subdiretórios
                procurarArquivo(file, fileName, foundFiles);
            } else if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) {
                // Adiciona o arquivo encontrado à lista
                foundFiles.add(file);
            }
        }
    }

    public static Diretorios obterDiretorios() throws Exception {
        String templatePacotes = obterTemplate("pacotes.json");
        // System.out.println(templatePacotes);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(templatePacotes, Diretorios.class);
    }

    public static void criarArquivo(String diretorio, String conteudo) throws IOException {
        // Tenta encontrar a declaração "public class NomeClasse"
        Pattern pattern = Pattern.compile("\\bpublic\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(conteudo);
        String nomeClasse = null;

        if (matcher.find()) {
            nomeClasse = matcher.group(1);
        } else {
            // Se não encontrou, tenta buscar por "class NomeClasse"
            pattern = Pattern.compile("\\bclass\\s+(\\w+)");
            matcher = pattern.matcher(conteudo);
            if (matcher.find()) {
                nomeClasse = matcher.group(1);
            } else {
                throw new IllegalArgumentException(
                        "Não foi possível identificar o nome da classe no conteúdo fornecido.");
            }
        }

        // Define o nome do arquivo a partir do nome da classe
        String nomeArquivo = nomeClasse + ".java";
        File diretorioFile = new File(diretorio);

        // Cria o diretório se ele não existir
        if (!diretorioFile.exists()) {
            diretorioFile.mkdirs();
        }

        // Cria o arquivo no diretório informado
        File arquivo = new File(diretorioFile, nomeArquivo);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            writer.write(conteudo);
        }

        System.out.println("Arquivo criado: " + arquivo.getAbsolutePath());
    }
    
    public static String atualizarPackage(String conteudoClasse, String packageEntidade) {
        if (packageEntidade == null || packageEntidade.trim().isEmpty()) {
            throw new IllegalArgumentException("O novo pacote não pode ser nulo ou vazio.");
        }

        // Formata a nova declaração de package com duas quebras de linha (pode ser ajustado conforme a necessidade)
        String pacoteJava = packageEntidade.replace(File.separator, ".");
        String novaDeclaracao = "package " + pacoteJava + ";\n\n";

        // Expressão regular para identificar a declaração de package.
        // O (?m) ativa o modo MULTILINE para que ^ se refira ao início de cada linha.
        Pattern pattern = Pattern.compile("(?m)^\\s*package\\s+[\\w\\.]+\\s*;\\s*\\n?");
        Matcher matcher = pattern.matcher(conteudoClasse);

        if (matcher.find()) {
            // Se a declaração de package existir, substitui-a pela nova.
            return matcher.replaceFirst(Matcher.quoteReplacement(novaDeclaracao));
        } else {
            // Se não existir, insere a nova declaração no início do conteúdo.
            return novaDeclaracao + conteudoClasse;
        }
    }
    
    public static String extrairPackage(String caminhoCompleto) {
        String marcador = "src" + File.separator + "main" + File.separator + "java" + File.separator;
        
        int indice = caminhoCompleto.indexOf(marcador);
        if (indice == -1) {
            return ""; // marcador não encontrado
        }

        return caminhoCompleto.substring(indice + marcador.length());
    }
    
    public static String extrairNomeClass(String conteudoJava) {
        // Regex para encontrar o nome da classe após a palavra-chave "class"
        Pattern pattern = Pattern.compile("\\bclass\\s+(\\w+)\\b");
        Matcher matcher = pattern.matcher(conteudoJava);
        if (matcher.find()) {
            return matcher.group(1); // Retorna o nome da classe encontrado
        }
        return null; // Retorna null caso não encontre
    }
    
    public static String extrairNomeInterface(String conteudoJava) {
        // Regex para encontrar o nome da classe após a palavra-chave "class"
        Pattern pattern = Pattern.compile("\\binterface\\s+(\\w+)\\b");
        Matcher matcher = pattern.matcher(conteudoJava);
        if (matcher.find()) {
            return matcher.group(1); // Retorna o nome da classe encontrado
        }
        return null; // Retorna null caso não encontre
    }
    
    public static String extrairNomeEnum(String conteudoJava) {
        // Regex para encontrar o nome da classe após a palavra-chave "class"
        Pattern pattern = Pattern.compile("\\benum\\s+(\\w+)\\b");
        Matcher matcher = pattern.matcher(conteudoJava);
        if (matcher.find()) {
            return matcher.group(1); // Retorna o nome da classe encontrado
        }
        return null; // Retorna null caso não encontre
    }
    
    public static String incluirImport(Diretorios diretorios, String conteudoJava, TipoCodigoJava tipo, String nomeObjeto) {
        String packageJava = "";

        // Define o pacote com base no tipo
        switch (tipo) {
            case ENUM:
                packageJava = extrairPackage(diretorios.getEnumerator());
                break;
            case DTO:
                packageJava = extrairPackage(diretorios.getDto());
                break;
            default:
                return conteudoJava; // Tipo não tratado
        }

        // Monta o import necessário
        packageJava = packageJava.replace(File.separator, ".");
        String novoImport = "import " + packageJava + "." + nomeObjeto + ";";

        // Verifica se o import já existe
        if (conteudoJava.contains(novoImport)) {
            return conteudoJava; // Já está presente, não insere
        }

        // Regex para encontrar todos os imports
        Pattern patternImports = Pattern.compile("(?m)^import\\s+.*?;");
        Matcher matcher = patternImports.matcher(conteudoJava);

        int ultimaPosicaoImport = -1;

        // Percorre todos os imports e encontra a última posição
        while (matcher.find()) {
            ultimaPosicaoImport = matcher.end();
        }

        // Se encontrou imports, adiciona logo após o último
        if (ultimaPosicaoImport != -1) {
            StringBuilder sb = new StringBuilder();
            sb.append(conteudoJava, 0, ultimaPosicaoImport);
            sb.append("\n").append(novoImport).append("\n");
            sb.append(conteudoJava.substring(ultimaPosicaoImport));
            return sb.toString();
        } else {
            // Se não houver nenhum import, tenta inserir após o package
            Pattern patternPackage = Pattern.compile("(?m)^package\\s+.*?;");
            Matcher matcherPackage = patternPackage.matcher(conteudoJava);

            if (matcherPackage.find()) {
                int fimPackage = matcherPackage.end();
                return conteudoJava.substring(0, fimPackage)
                        + "\n\n" + novoImport + "\n"
                        + conteudoJava.substring(fimPackage);
            } else {
                return conteudoJava;
            }
        }
    }

}
