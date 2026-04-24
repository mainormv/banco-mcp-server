package com.banco.mcp.controller;

import com.banco.mcp.service.BankingToolsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class BankingController {

    private final BankingToolsService bankingService;

    public BankingController(BankingToolsService bankingService) {
        this.bankingService = bankingService;
    }

    record PagoRequest(String numeroCuenta, double monto) {}

    @GetMapping("/{cedula}")
    public ResponseEntity<Map<String, Object>> verificarCliente(@PathVariable String cedula) {
        return ResponseEntity.ok(bankingService.verificarCliente(cedula));
    }

    @GetMapping("/{cedula}/cuentas")
    public ResponseEntity<Map<String, Object>> obtenerCuentas(@PathVariable String cedula) {
        return ResponseEntity.ok(bankingService.obtenerCuentas(cedula));
    }

    @GetMapping("/{cedula}/servicios")
    public ResponseEntity<Map<String, Object>> obtenerServiciosPublicos(@PathVariable String cedula) {
        return ResponseEntity.ok(bankingService.obtenerServiciosPublicos(cedula));
    }

    @GetMapping("/{cedula}/servicios/{codigo}/deuda")
    public ResponseEntity<Map<String, Object>> consultarDeuda(@PathVariable String cedula,
                                                               @PathVariable String codigo) {
        return ResponseEntity.ok(bankingService.consultarDeudaServicio(cedula, codigo));
    }

    @PostMapping("/{cedula}/servicios/{codigo}/pagar")
    public ResponseEntity<Map<String, Object>> pagarServicio(@PathVariable String cedula,
                                                              @PathVariable String codigo,
                                                              @RequestBody PagoRequest request) {
        return ResponseEntity.ok(bankingService.pagarServicioPublico(cedula, codigo, request.numeroCuenta(), request.monto()));
    }
}
