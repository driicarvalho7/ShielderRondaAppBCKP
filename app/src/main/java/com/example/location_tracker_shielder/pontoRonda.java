package com.example.location_tracker_shielder;

public class pontoRonda extends Object {

    private String pontoPassado;
    String idPontoRonda;
    String idRonda;
    String nomePontoRonda;
    String ordemPontoRonda;
    String coordenadasPontoRonda;


    public pontoRonda(String idRonda, String idPontoRonda, String coordenadasPontoRonda, String ordemPontoRonda, String nomePontoRonda, String pontoPassado) {

        this.idPontoRonda = idPontoRonda;
        this.idRonda = idRonda;
        this.ordemPontoRonda = ordemPontoRonda;
        this.coordenadasPontoRonda = coordenadasPontoRonda;
        this.nomePontoRonda = nomePontoRonda;
        this.pontoPassado = pontoPassado;
    }


    public String getNomePontoRonda() {
        return nomePontoRonda;
    }

    public void setNomePontoRonda(String nomePontoRonda) {
        this.nomePontoRonda = nomePontoRonda;
    }

    public String getIdPontoRonda() {
        return idPontoRonda;
    }

    public void setIdPontoRonda(String idPontoRonda) {
        this.idPontoRonda = idPontoRonda;
    }

    public String getIdRonda() {
        return idRonda;
    }

    public Integer getOrdemPontoRonda(){return new Integer(this.ordemPontoRonda);}

    public void setIdMorador(String idRonda) {
        this.idRonda = idRonda;
    }

    public String getCoordenadasPontoRonda() {
        return coordenadasPontoRonda;
    }

    public String getPontoPassado() {
        return pontoPassado;
    }

    public void setCoordenadasPontoRonda(String coordenadasPontoRonda) {
        this.coordenadasPontoRonda = coordenadasPontoRonda;
    }

    public Double getLatitudePontoRonda() {
        return new Double(this.coordenadasPontoRonda.split(",")[0].split("°")[0]);
    }

    public Double getLongitudePontoRonda() {
        return new Double(this.coordenadasPontoRonda.split(",")[1].split("°")[0]);
    }

    public void setPontoPassado() {
        this.pontoPassado = "1";
    }
}
