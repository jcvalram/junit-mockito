package org.jcvalram.test.springboot.app.controllers;

import static org.hamcrest.Matchers.*;
import static org.jcvalram.test.springboot.app.Datos.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jcvalram.test.springboot.app.models.Cuenta;
import org.jcvalram.test.springboot.app.models.TransaccionDto;
import org.jcvalram.test.springboot.app.services.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CuentaService cuentaService;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testDetalle() throws Exception {
        // Given
        when(cuentaService.findById(1L)).thenReturn(crearCuenta001().orElseThrow());

        // When
        mvc.perform(get("/api/cuentas/1").contentType(MediaType.APPLICATION_JSON))
        // Then
                .andExpect(status().isOk()) // Prueba que la respuesta sea 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Prueba que el content type de la respuesta sea json
                .andExpect(jsonPath("$.persona").value("Andrés")) // Prueba algún atributo del json devuelva un valor
                .andExpect(jsonPath("$.saldo").value("1000"));

        verify(cuentaService).findById(1L);
    }

    @Test
    void testTransferir() throws Exception, JsonProcessingException {
        // Given
        TransaccionDto dto = new TransaccionDto();
        dto.setCuentaOrigenId(1L);
        dto.setMonto(new BigDecimal("100"));
        dto.setCuentadestinoId(2L);
        dto.setBancoId(1L);

        //Opcional, mostrar dto por consola
        System.out.println(objectMapper.writeValueAsString(dto));

        Map<String, Object> response = new HashMap<>();
        response.put("date", LocalDate.now().toString());
        response.put("status", "OK");
        response.put("mensaje", "Tranferencia realizada con éxito");
        response.put("transaccion", dto);

        //Opcional, mostrar response por consola
        System.out.println(objectMapper.writeValueAsString(response));

        // When
        mvc.perform(post("/api/cuentas/transferir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        // Then
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.mensaje").value("Tranferencia realizada con éxito"))
                .andExpect(jsonPath("$.transaccion.cuentaOrigenId").value(1L))
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void testListar() throws Exception {
        // Given
        List<Cuenta> cuentas = Arrays.asList(crearCuenta001().orElseThrow(),
                crearCuenta002().orElseThrow());
        when(cuentaService.findAll()).thenReturn(cuentas);

        // When
        mvc.perform(get("/api/cuentas").contentType(MediaType.APPLICATION_JSON))

        // Then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].persona").value("Andrés"))
            .andExpect(jsonPath("$[1].persona").value("Jhon"))
            .andExpect(jsonPath("$[0].saldo").value("1000"))
            .andExpect(jsonPath("$[1].saldo").value("2000"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(content().json(objectMapper.writeValueAsString(cuentas)));

        verify(cuentaService).findAll();
    }

    @Test
    void testGuardar() throws Exception {
        // Given
        Cuenta cuenta = new Cuenta(null, "Pepe", new BigDecimal("3000"));
        when(cuentaService.save(any())).then(invocation -> {
            Cuenta c = invocation.getArgument(0);
            c.setId(3L);
            return c;
        });

        // When
        mvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(cuenta)))
        // Then
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(3)))
            .andExpect(jsonPath("$.persona", is("Pepe")))
            .andExpect(jsonPath("$.saldo", is(3000)));

        verify(cuentaService).save(any());
    }
}