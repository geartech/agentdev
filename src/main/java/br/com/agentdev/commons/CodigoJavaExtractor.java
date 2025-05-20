package br.com.agentdev.commons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;

import br.com.agentdev.enums.TipoCodigoJava;



public class CodigoJavaExtractor {

    // Classe POJO que representa cada item do JSON
    public static class CodigoJavaItem {
        private TipoCodigoJava tipo;
        private String codigoJava;

        // Getters e Setters
        public TipoCodigoJava getTipo() {
            return tipo;
        }

        public void setTipo(TipoCodigoJava tipo) {
            this.tipo = tipo;
        }

        public String getCodigoJava() {
            return codigoJava;
        }

        public void setCodigoJava(String codigoJava) {
            this.codigoJava = codigoJava;
        }
    }
    
    public static List<String> getCodigoJavaByTipo(String jsonRespApi, TipoCodigoJava tipoBuscado) {
    	List<String> codigoJavaList = new ArrayList<String>(); 
    	JsonMapper jsonMapper = JsonMapper.builder().build();
        // Permite caracteres de controle não escapados (por exemplo, quebras de linha)
        JsonMapper.builder().configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS , true);
        JsonMapper.builder().configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER , true);

        try {
            // Desserializa a string JSON para uma lista de objetos CodigoJavaItem
            List<CodigoJavaItem> itens = jsonMapper.readValue(jsonRespApi, new TypeReference<List<CodigoJavaItem>>() {});
            // Itera pela lista procurando o item com o tipo desejado
            for (CodigoJavaItem item : itens) {
                if (tipoBuscado.equals(item.getTipo())) {
                	codigoJavaList.add(item.getCodigoJava());
                }
            }
            
            return codigoJavaList;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retorna null caso não encontre um objeto com o tipo informado
        return null;
    }
    
}
