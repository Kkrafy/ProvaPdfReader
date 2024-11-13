package starterpackage.businesslayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.lowagie.text.pdf.PdfReader;
import starterpackage.Gabarito;
import starterpackage.dataacesslayer.dto.TextObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Será um algoritmo para detectar questões e suas respostas em pdf de gabaritos reconhecendo padroes nas text matrices
 */
public class QuestoesService {
    float cordenadaQuestaoX;
    float cordenadaAlX;
    int quantidadeQuestoes;
    HashMap<Integer,List<TextObject>> bancoX;
    HashMap<Integer,List<TextObject>> bancoY;
    HashMap<String,String> pares;
    List<List<TextObject>> streamsList;
    enum XY{X,Y}
    Logger logger = LogManager.getLogger();

    /**
     *
     * @param arquivoDir diretorio do arquivo pdf para scanear
     * @param quantidadeQuestoes quantidade de questões no gabarito que sera interpretado
     * @throws FileNotFoundException Se o arquivo não for encontrado
     * @throws IOException Se o {@link PdfReader} não aceitar o arquivo(provavelmente o arquivo é invalido se a exception for lançada)
     */
    public QuestoesService(String arquivoDir,int quantidadeQuestoes) throws IOException, FileNotFoundException {
        this.streamsList = new GabaritoScanner(arquivoDir).scanGabarito();
        this.quantidadeQuestoes = quantidadeQuestoes;
    }

    public Gabarito gerarGabarito(){
        setupBancos();
        Gabarito gabarito = new Gabarito(quantidadeQuestoes);
        for(Map.Entry<String,String> entry:pares.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            int alternativa;
            char resposta;
            try{
                alternativa = Integer.parseInt(key);
                resposta = value.toCharArray()[0];
            }
            catch (NumberFormatException e){
                try {
                    alternativa = Integer.parseInt(value);
                    resposta = key.toCharArray()[0];
                }catch (NumberFormatException e2){
                   continue;
                }
            }
            if(alternativa <= gabarito.getQuantidadeQuestoes()){
                gabarito.put(alternativa,resposta);
            }
        }
        return gabarito;
    }

    /**
     * Seta o bancoX e o bancoY,Maps que ligam os TextObjects com tm aproximadamente igual e o Map pares que contem os tj dos TextObjects
     * que so coincidem tm 2 vezes(pois é provavel que sejam alternativa e resposta), todos os setados acima sao variaveis desta classe
     */
    private void setupBancos(){
        faseContagemSetupBanco();
        System.out.println("contagem");
        faseFiltragemSetupBanco(XY.X);
        faseFiltragemSetupBanco(XY.Y);
        System.out.println("bah");
    }

    private void faseContagemSetupBanco(){
        bancoX = new HashMap<>();
        bancoY = new HashMap<>();

        int index = 0;
        while(streamsList.size() != index){
            List<TextObject> currentStream = streamsList.get(index);
            boolean debugger = true;
            for(TextObject t:currentStream){
                int tCordenadaX =(int) t.getTm()[4]; //aproximação pq nem sempre vai ser exato, mas parecido
                int tCordenadaY = (int)t.getTm()[5]; //aproximação pq nem sempre vai ser exato, mas parecido
                List<TextObject> listaX = bancoX.get(tCordenadaX) == null?new ArrayList<TextObject>():bancoX.get(tCordenadaX);
                listaX.add(t);
                List<TextObject> listaY = bancoY.get(tCordenadaY) == null?new ArrayList<TextObject>():bancoY.get(tCordenadaY);
                listaY.add(t);
                bancoX.put(tCordenadaX,listaX);
                bancoY.put(tCordenadaY,listaY);
            }
            index++;
        }
    }

    private void faseFiltragemSetupBanco(XY xy) {
        HashMap<Integer,List<TextObject>> banco = xy==XY.X?bancoX:bancoY;
        HashMap<Integer,List<TextObject>> bancoFuturo = (HashMap<Integer, List<TextObject>>) banco.clone();
        for(Map.Entry<Integer,List<TextObject>> entry:banco.entrySet()){
            List<TextObject> value = entry.getValue();
            if(value.size() < 2){
                bancoFuturo.remove(entry.getKey());
            }else if(value.size() == 2){
                String tj0 = value.get(0).getTj().isPresent()?value.get(0).getTj().get():"N/A,TJ Array nao suportados ainda";
                String tj1 = value.get(1).getTj().isPresent()?value.get(1).getTj().get():"N/A,TJ Array nao suportados ainda";
                if(pares == null){pares = new HashMap<>();}
                pares.put(tj0,tj1);
            }
        }
        if(xy == XY.X){bancoX = bancoFuturo;}
        else{bancoY = bancoFuturo;}
    }
}
