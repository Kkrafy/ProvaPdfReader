package starterpackage;

import java.util.HashMap;

/**
 * Entity que representa um gabarito, extende um {@link HashMap} que guarda o numero da questão e sua alternativa
 */
public class Gabarito extends HashMap<Integer,Character>{

    int quantidadeQuestoes;

    public Gabarito(int quantidadeQuestoes){
        this.quantidadeQuestoes = quantidadeQuestoes;
    }
    public int getQuantidadeQuestoes(){
        return quantidadeQuestoes;
    }


}
