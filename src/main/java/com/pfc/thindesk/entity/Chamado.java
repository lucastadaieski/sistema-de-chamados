package com.pfc.thindesk.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chamados")
public class Chamado {

    @Id
    private String id;
    private String titulo;
    private String descricao;
    private StatusChamado status;

    // Preenchidos automaticamente na abertura
    private String solicitanteId;
    private String solicitanteEmail;
    private Instant dataAbertura;

    // Preenchidos na resolução
    private String tecnicoEmail;
    private String solucao;
    private Instant dataResolucao;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusChamado getStatus() { return status; }
    public void setStatus(StatusChamado status) { this.status = status; }

    public String getSolicitanteId() { return solicitanteId; }
    public void setSolicitanteId(String solicitanteId) { this.solicitanteId = solicitanteId; }

    public String getSolicitanteEmail() { return solicitanteEmail; }
    public void setSolicitanteEmail(String solicitanteEmail) { this.solicitanteEmail = solicitanteEmail; }

    public Instant getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(Instant dataAbertura) { this.dataAbertura = dataAbertura; }

    public String getTecnicoEmail() { return tecnicoEmail; }
    public void setTecnicoEmail(String tecnicoEmail) { this.tecnicoEmail = tecnicoEmail; }

    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }

    public Instant getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(Instant dataResolucao) { this.dataResolucao = dataResolucao; }
}
