package starterpackage.businesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import starterpackage.dataacesslayer.dto.TextObject;

import java.util.List;

public class GabaritoScannerTest {

    GabaritoScanner gabaritoScanner;
    @BeforeEach
    public void beforeEach(){
        try {
            gabaritoScanner = new GabaritoScanner("/home/kkraft/Downloads/2023_GB_impresso_D1_CD1.pdf");
        }catch (Exception e){
            e.printStackTrace();
            throw new Error(e);
        }
    }

    @Test
    public void scanEUpdateGabaritoTest(){
        List<List<TextObject>> resultado = gabaritoScanner.scanGabarito();
        assert resultado.size() == 8;

    }
}
