package br.com.agentdev.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import br.com.agentdev.commons.Utils;

public class ModeloIAClient {
    private final String apiUrl;
    private final String model;
    private final String systemPrompt;

    public ModeloIAClient(String apiUrl, String model, String systemPrompt) {
        this.apiUrl = apiUrl;
        this.model = model;
        this.systemPrompt = systemPrompt;
    }

    public String execute(String prompt) throws IOException {
        
        HttpURLConnection conn = createConnection();
        String requestBody = buildRequestBody(prompt);

        writeRequestBody(conn, requestBody);
        String response = readResponse(conn);
        conn.disconnect();
        
        String jsonResponse = Utils.extrairMensagemClient(response);
        jsonResponse = removerPrefixoJson(jsonResponse);
        
        return jsonResponse;
    }

    private HttpURLConnection createConnection() throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        return conn;
    }

    private String buildRequestBody(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "temperature", 0.2, // menos criativo, mais determinístico e obediente
            "top_p", 1.0,
            "repeat_penalty", 1.1,
            "max_tokens", 4096,
            "seed", 42, // resposta sempre igual para mesmo prompt
            "messages", List.of(
                Map.of("role", "system", "content", "Você é um programador java sênior. "
                        + "Gere a implementação exata conforme solicitado, seguindo rigorosamente o padrão fornecido. "
                		+ "Ignore todas as mensagens anteriores. Esta é uma nova requisição independente. "
                		+ systemPrompt),
                Map.of("role", "user", "content", prompt)
            )
        );
        return Utils.toJsonString(requestBody);
    }

    private void writeRequestBody(HttpURLConnection conn, String requestBody) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }
    
    public static String removerPrefixoJson(String jsonString) {
        // Remove ```json no início, se existir
        jsonString = jsonString.replaceFirst("^```json\\s*", "");

        // Remove ``` no final, se existir
        jsonString = jsonString.replaceFirst("\\s*```$", "");

        jsonString = jsonString.replaceFirst("^.*?(\\[)", "$1").replaceFirst("(\\]).*$", "$1");
        return jsonString.trim(); // Remove espaços extras no começo e fim
    }
    
}
