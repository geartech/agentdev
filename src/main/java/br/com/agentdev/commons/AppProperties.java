package br.com.agentdev.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {

	private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("⚠️ Configuração do servidor do Modelo IA, não encontrado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String chave) {
        return properties.getProperty(chave);
    }

    public static boolean getAsBoolean(String chave) {
        return Boolean.parseBoolean(properties.getProperty(chave));
    }

    public static int getAsInt(String chave) {
        return Integer.parseInt(properties.getProperty(chave));
    }
}
