package com.example.location_tracker_shielder;

public class Ronda {

    protected String id_ronda_condominio, nome_ronda, descricao, inicio, fim, habilitado, funcao, nome_cond, nome_funcionario;

    public Ronda(String id_ronda_condominio, String nome_ronda, String descricao, String inicio, String fim, String habilitado, String funcao, String nome_funcionario, String nome_cond) {
        this.id_ronda_condominio = id_ronda_condominio;
        this.nome_ronda = nome_ronda;
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
        this.habilitado = habilitado;
        this.funcao = funcao;
        this.nome_cond = nome_cond;
        this.nome_funcionario = nome_funcionario;
    }

    public String getId_ronda_condominio() {
        return id_ronda_condominio;
    }

    public void setId_ronda_condominio(String id_ronda_condominio) {
        this.id_ronda_condominio = id_ronda_condominio;
    }

    public String getNome_ronda() {
        return nome_ronda;
    }

    public void setNome_ronda(String nome_ronda) {
        this.nome_ronda = nome_ronda;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFim() {
        return fim;
    }

    public void setFim(String fim) {
        this.fim = fim;
    }

    public String getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(String habilitado) {
        this.habilitado = habilitado;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getNome_cond() {
        return nome_cond;
    }

    public void setNome_cond(String nome_cond) {
        this.nome_cond = nome_cond;
    }

    public String getNomeFuncionario() {
        return nome_funcionario;
    }
}
