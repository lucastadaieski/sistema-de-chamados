package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.ChamadoRequest;
import com.pfc.thindesk.dto.SolucaoRequest;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.service.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Chamado abrir(@Valid @RequestBody ChamadoRequest request, @AuthenticationPrincipal Jwt usuario) {
        return service.abrir(request, usuario);
    }

    @GetMapping("/usuario/{id}")
    public List<Chamado> listarDoUsuario(@PathVariable String id, @AuthenticationPrincipal Jwt usuario) {
        return service.listarDoUsuario(id, usuario);
    }

    @GetMapping("/{id}")
    public Chamado buscar(@PathVariable String id, @AuthenticationPrincipal Jwt usuario) {
        return service.buscar(id, usuario);
    }

    @PutMapping("/{id}/resolver")
    public Chamado resolver(@PathVariable String id, @Valid @RequestBody SolucaoRequest request,
                            @AuthenticationPrincipal Jwt tecnico) {
        return service.resolver(id, request, tecnico);
    }
}
