package com.pfc.thindesk.service;

import com.pfc.thindesk.dto.ChamadoRequest;
import com.pfc.thindesk.dto.SolucaoRequest;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.entity.StatusChamado;
import com.pfc.thindesk.repository.ChamadoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Set;


@Service
public class ChamadoService {

    private static final Set<String> PERFIS_TECNICOS = Set.of("TECNICO", "ADMIN");

    private final ChamadoRepository repository;

    public ChamadoService(ChamadoRepository repository) {
        this.repository = repository;
    }

    /** Abre um chamado: solicitante, data e status ABERTO são definidos aqui, nunca pelo cliente. */
    public Chamado abrir(ChamadoRequest request, Jwt usuario) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(request.titulo().trim());
        chamado.setDescricao(request.descricao().trim());
        chamado.setSolicitanteId(usuario.getSubject());
        chamado.setSolicitanteEmail(usuario.getClaimAsString("email"));
        chamado.setDataAbertura(Instant.now());
        chamado.setStatus(StatusChamado.ABERTO);
        return repository.save(chamado);
    }

    /** Lista os chamados de um usuário. Usuário comum só vê os próprios. */
    public List<Chamado> listarDoUsuario(String usuarioId, Jwt usuario) {
        if (!usuarioId.equals(usuario.getSubject()) && !isTecnico(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode ver os seus próprios chamados");
        }
        return repository.findBySolicitanteIdOrderByDataAberturaDesc(usuarioId);
    }

    /** Consulta um chamado. Usuário comum só vê os próprios. */
    public Chamado buscar(String id, Jwt usuario) {
        Chamado chamado = buscarOuFalhar(id);
        if (!chamado.getSolicitanteId().equals(usuario.getSubject()) && !isTecnico(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode ver os seus próprios chamados");
        }
        return chamado;
    }

    /** O técnico registra a solução. Chamado já resolvido não pode ser alterado. */
    public Chamado resolver(String id, SolucaoRequest request, Jwt tecnico) {
        if (!isTecnico(tecnico)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas técnicos podem resolver chamados");
        }
        Chamado chamado = buscarOuFalhar(id);
        if (chamado.getStatus() == StatusChamado.RESOLVIDO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este chamado já foi resolvido e não pode ser alterado novamente");
        }
        chamado.setSolucao(request.solucao().trim());
        chamado.setTecnicoEmail(tecnico.getClaimAsString("email"));
        chamado.setDataResolucao(Instant.now());
        chamado.setStatus(StatusChamado.RESOLVIDO);
        return repository.save(chamado);
    }

    private Chamado buscarOuFalhar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
    }

    private boolean isTecnico(Jwt usuario) {
        return PERFIS_TECNICOS.contains(usuario.getClaimAsString("perfil"));
    }
}
