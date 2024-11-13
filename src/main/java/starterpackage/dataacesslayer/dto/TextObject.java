package starterpackage.dataacesslayer.dto;

import java.util.Optional;

/**
 * Classe que representa bem simplificadamente um TextObject de Streams pdf com Tm e Tj[Sem suporte para TJ(de array)]
 */
public class TextObject {

    private float[] tm;
    private Optional<String> tj;


    public TextObject(float[] tm, Optional<String> tj){
        this.tm = tm;
        this.tj = tj;
    }

    /**
     * Retorna a text matrix do jeito que aparece na definição
     * @return Retorna a text matrix do jeito que aparece na definição
     */
    public float[] getTm() {
        return tm;
    }

    /**
     * Se o Text Object lido tiver um Tj(de string) a optional vai estar presente, se tiver um array nao
     * @return Se o Text Object lido tiver um Tj(de string) a optional vai estar presente, se tiver um array nao
     */
    public Optional<String> getTj() {
        return tj;
    }

    /**
     * Seta a textMatrix
     * @param tm Seta a textMatrix
     */
    public void setTm(float[] tm) {
        this.tm = tm;
    }

    public void setTj(Optional<String> tj) {
        this.tj = tj;
    }

}
