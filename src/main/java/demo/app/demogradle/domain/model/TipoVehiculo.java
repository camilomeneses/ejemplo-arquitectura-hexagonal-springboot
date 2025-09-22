package demo.app.demogradle.domain.model;

public enum TipoVehiculo {
    CARRO(1000),
    MOTO(500);

    private final int tarifaPorHora;

    TipoVehiculo(int tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public int getTarifaPorHora() {
        return tarifaPorHora;
    }
}
