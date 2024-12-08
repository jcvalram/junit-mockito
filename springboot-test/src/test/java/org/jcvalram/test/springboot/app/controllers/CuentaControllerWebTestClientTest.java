package org.jcvalram.test.springboot.app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jcvalram.test.springboot.app.models.Cuenta;
import org.jcvalram.test.springboot.app.models.TransaccionDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integracion_wc")
// Anotación que permite dar un order de ejecución de los test
// de integración, para evitar errores, si se modifican datos en otros test
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class CuentaControllerWebTestClientTest {

    @Autowired
    private WebTestClient client;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    void testTransferir() throws JsonProcessingException {
        // Given
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setCuentadestinoId(2L);
        dto.setBancoId(1L);
        dto.setMonto(new BigDecimal("100"));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Tranferencia realizada con éxito");
        response.put("transaccion", dto);

        // When
        client.post().uri("/api/cuentas/transferir")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dto)
            .exchange()
        // Then
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            // Si no se indica ningún tipo de dato por defecto es byte[]
            .expectBody()
            .consumeWith(respuesta -> {
                try {
                    // Transformamos la respuesta a Json
                    JsonNode json = objectMapper.readTree(respuesta.getResponseBody());
                    // Comprobamos atributos del json con su valor esperado dentro de una función lambda
                    assertEquals("Tranferencia realizada con éxito", json.path("mensaje").asText());
                    assertEquals(1L, json.path("transaccion").path("cuentaOrigenId").asLong());
                    assertEquals(LocalDate.now().toString(), json.path("date").asText());
                    assertEquals("100", json.path("transaccion").path("monto").asText());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            })
            .jsonPath("$.mensaje").isNotEmpty()
            .jsonPath("$.mensaje").value(is("Tranferencia realizada con éxito"))
            // Comprobaciones equivalentes
            .jsonPath("$.mensaje").value(valor ->
                assertEquals("Tranferencia realizada con éxito", valor))
            .jsonPath("$.mensaje").isEqualTo("Tranferencia realizada con éxito")
            .jsonPath("$.transaccion.cuentaOrigenId").isEqualTo(1)
            .jsonPath("$.date").isEqualTo(LocalDate.now().toString())
            .json(objectMapper.writeValueAsString(response));
    }

    @Test
    @Order(2)
    void testDetalle() throws JsonProcessingException {
        Cuenta cuenta = new Cuenta(1L, "Andrés", new BigDecimal("900"));

        client.get().uri("/api/cuentas/1").exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.persona").isEqualTo("Andrés")
            .jsonPath("$.saldo").isEqualTo(900)
            .json(objectMapper.writeValueAsString(cuenta));
    }

    @Test
    @Order(3)
    void testDetalle2() {
        client.get().uri("/api/cuentas/2").exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Cuenta.class)
            .consumeWith(respuesta -> {
                Cuenta cuenta = respuesta.getResponseBody();
                assertNotNull(cuenta);
                assertEquals("Jhon", cuenta.getPersona());
                assertEquals("2100.00", cuenta.getSaldo().toPlainString());
            });
    }

    @Test
    @Order(4)
    void testListar() {
        client.get().uri("/api/cuentas").exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$[0].persona").isEqualTo("Andrés")
            .jsonPath("$[0].id").isEqualTo(1)
            .jsonPath("$[0].saldo").isEqualTo(900)
            .jsonPath("$[1].persona").isEqualTo("Jhon")
            .jsonPath("$[1].id").isEqualTo(2)
            .jsonPath("$[1].saldo").isEqualTo(2100)
            .jsonPath("$").isArray()
            .jsonPath("$").value(hasSize(2));
    }

    @Test
    @Order(5)
    void testListar2() {
        client.get().uri("/api/cuentas").exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Cuenta.class)
            .consumeWith(respuesta -> {
                List<Cuenta> cuentas = respuesta.getResponseBody();
                assertNotNull(cuentas);
                assertEquals(2, cuentas.size());
                assertEquals(1L, cuentas.get(0).getId());
                assertEquals("Andrés", cuentas.get(0).getPersona());
                assertEquals("900.0", cuentas.get(0).getSaldo().toPlainString());
                assertEquals(2L, cuentas.get(1).getId());
                assertEquals("Jhon", cuentas.get(1).getPersona());
                assertEquals("2100.0", cuentas.get(1).getSaldo().toPlainString());
            })
            .hasSize(2)
            .value(hasSize(2));
    }

    @Test
    @Order(6)
    void testGuardar() {
        // Given
        Cuenta cuentaPepe = new Cuenta(null, "Pepe", new BigDecimal("3000"));

        // When
        client.post().uri("/api/cuentas")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(cuentaPepe)
            .exchange()
        // Then
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo(3)
            .jsonPath("$.persona").isEqualTo("Pepe")
            .jsonPath("$.persona").value(is("Pepe"))
            .jsonPath("$.saldo").isEqualTo(3000);
    }

    @Test
    @Order(7)
    void testGuardar2() {
        // Given
        Cuenta cuentaPepa = new Cuenta(null, "Pepa", new BigDecimal("3500"));

        // When
        client.post().uri("/api/cuentas")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(cuentaPepa)
            .exchange()
        // Then
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Cuenta.class)
            .consumeWith(respuesta -> {
                Cuenta c = respuesta.getResponseBody();
                assertNotNull(c);
                assertEquals(4, c.getId());
                assertEquals("Pepa", c.getPersona());
                assertEquals("3500", c.getSaldo().toPlainString());
            });
    }

    @Test
    @Order(8)
    void testEliminar() {
        client.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(4);

        client.delete().uri("/api/cuentas/3").exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();

        client.get().uri("/api/cuentas").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Cuenta.class)
                .hasSize(3);

        client.get().uri("/api/cuentas/3").exchange()
//                .expectStatus().is5xxServerError();
            .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }
}