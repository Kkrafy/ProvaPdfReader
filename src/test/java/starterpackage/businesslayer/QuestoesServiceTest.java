package starterpackage.businesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import starterpackage.Gabarito;

public class QuestoesServiceTest {

    QuestoesService questoesService;

    @BeforeEach
    public void beforeEach(){
        try {
            questoesService = new QuestoesService("/home/kkraft/Downloads/2023_GB_impresso_D1_CD1.pdf", 40);
        }catch (Exception e){
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Test
    public void gerarGabaritoTest(){
        Gabarito gabarito = questoesService.gerarGabarito();
        System.out.println("debuggerlineyay");
    }
}
