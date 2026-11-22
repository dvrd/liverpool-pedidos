package mx.com.liverpool.pedidos.comun;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextosTest {

    @Test
    void quitaAcentosYMayusculas() {
        assertThat(Textos.normalizar("José Núñez")).isEqualTo("jose nunez");
        assertThat(Textos.normalizar("  Camila   Sofía ")).isEqualTo("camila sofia");
    }

    @Test
    void escapaMetacaracteresParaMongo() {
        assertThat(Textos.regexLiteral("LP-ELC")).isEqualTo("lp-elc");
        assertThat(Textos.regexLiteral("a.b")).isEqualTo("a\\.b");
        assertThat(Textos.regexLiteral("10+2")).isEqualTo("10\\+2");
    }

    @Test
    void correoSeNormaliza() {
        assertThat(Textos.correo("  Ana@Correo.COM ")).isEqualTo("ana@correo.com");
    }
}
