package org.jcvalram.test.springboot.app.services;

import org.jcvalram.test.springboot.app.models.Cuenta;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CuentaService {

    List<Cuenta> findAll();

    Cuenta findById(Long id);

    Cuenta save(Cuenta cuenta);

    void deleteById(Long id);

    int revisarTotalTransferencias(Long bancoId);

    BigDecimal revisarSaldo(Long cuentaId);

    void transferir(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto, Long bancoId);
}
