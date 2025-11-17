package Licenta.Licenta.Model;

public enum StatusProgramare {
    PROGRAMAT("Programat"),
    CONFIRMAT("Confirmat"),
    IN_DESFASURARE("În desfășurare"),
    FINALIZAT("Finalizat"),
    ANULAT("Anulat");

    private final String displayName;

    StatusProgramare(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
