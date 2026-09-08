package br.com.fiap.streamfiap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleArgumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonInvalido(HttpMessageNotReadableException e) {
        Throwable causa = e.getMostSpecificCause();
        String mensagem = causa instanceof IllegalArgumentException
                ? causa.getMessage()
                : "Corpo JSON inválido. Verifique os campos enviados.";
        return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(ConteudoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleConteudoNaoEncontrado(ConteudoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(CreditosInsuficientesException.class)
    public ResponseEntity<Map<String, String>> handleCreditosInsuficientes(CreditosInsuficientesException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(ConteudoIndisponivelException.class)
    public ResponseEntity<Map<String, String>> handleConteudoIndisponivel(ConteudoIndisponivelException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }
}
