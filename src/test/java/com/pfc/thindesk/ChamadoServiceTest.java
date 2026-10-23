package com.pfc.thindesk;

import com.pfc.thindesk.dto.ChamadoRequest;
import com.pfc.thindesk.dto.SolucaoRequest;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.entity.StatusChamado;
import com.pfc.thindesk.repository.ChamadoRepository;
import com.pfc.thindesk.service.ChamadoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes das regras de negócio. O banco é simulado com Mockito, então rodam sem MongoDB. */
class ChamadoServiceTest {

    private final ChamadoRepository repository = mock(ChamadoRepository.class);
    private final ChamadoService service = new ChamadoService(repository);

    private final Jwt ana = token("u1", "ana@thindesk.com", "USUARIO");
    private final Jwt bruno = token("u2", "bruno@thindesk.com", "USUARIO");
    private final Jwt tecnico = token("t1", "tecnico@thindesk.com", "TECNICO");

    private static Jwt token(String id, String email, String perfil) {
        return Jwt.withTokenValue("teste").header("alg", "HS256")
                .subject(id).claim("email", email).claim("perfil", perfil).build();
    }

    private Chamado chamadoDaAna() {
        Chamado chamado = new Chamado();
        chamado.setId("c1");
        chamado.setSolicitanteId("u1");
        chamado.setStatus(StatusChamado.ABERTO);
        return chamado;
    }

    @Test
    void abrirPreencheSolicitanteDataEStatus() {
        when(repository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));

        Chamado chamado = service.abrir(new ChamadoRequest("Sem internet", "O computador não conecta"), ana);

        assertThat(chamado.getStatus()).isEqualTo(StatusChamado.ABERTO);
        assertThat(chamado.getSolicitanteId()).isEqualTo("u1");
        assertThat(chamado.getSolicitanteEmail()).isEqualTo("ana@thindesk.com");
        assertThat(chamado.getDataAbertura()).isNotNull();
    }

    @Test
    void usuarioNaoVeChamadoDeOutro() {
        when(repository.findById("c1")).thenReturn(Optional.of(chamadoDaAna()));

        assertThatThrownBy(() -> service.buscar("c1", bruno))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void chamadoInexistenteDa404() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar("x", ana))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void tecnicoResolveChamado() {
        when(repository.findById("c1")).thenReturn(Optional.of(chamadoDaAna()));
        when(repository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));

        Chamado chamado = service.resolver("c1", new SolucaoRequest("Cabo de rede trocado"), tecnico);

        assertThat(chamado.getStatus()).isEqualTo(StatusChamado.RESOLVIDO);
        assertThat(chamado.getSolucao()).isEqualTo("Cabo de rede trocado");
        assertThat(chamado.getDataResolucao()).isNotNull();
    }

    @Test
    void chamadoResolvidoNaoPodeSerAlterado() {
        Chamado resolvido = chamadoDaAna();
        resolvido.setStatus(StatusChamado.RESOLVIDO);
        when(repository.findById("c1")).thenReturn(Optional.of(resolvido));

        assertThatThrownBy(() -> service.resolver("c1", new SolucaoRequest("Outra solução"), tecnico))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(repository, never()).save(any());
    }

    @Test
    void usuarioComumNaoResolve() {
        assertThatThrownBy(() -> service.resolver("c1", new SolucaoRequest("Resolvi sozinho"), ana))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void moderadorNaoResolve() {
        Jwt moderador = token("m1", "moderador@thindesk.com", "MODERADOR");

        assertThatThrownBy(() -> service.resolver("c1", new SolucaoRequest("Tentativa do moderador"), moderador))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }
}
