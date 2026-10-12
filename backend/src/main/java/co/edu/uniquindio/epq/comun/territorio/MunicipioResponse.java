package co.edu.uniquindio.epq.comun.territorio;

public record MunicipioResponse(Integer id, String nombre, String codigoDane) {

    public static MunicipioResponse de(Municipio municipio) {
        return new MunicipioResponse(municipio.getId(), municipio.getNombre(), municipio.getCodigoDane());
    }
}
