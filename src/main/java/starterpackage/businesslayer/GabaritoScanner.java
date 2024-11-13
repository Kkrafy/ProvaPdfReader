package starterpackage.businesslayer;

import org.apache.logging.log4j.LogManager;
import com.lowagie.text.pdf.*;
import org.apache.logging.log4j.Logger;
import starterpackage.Gabarito;
import starterpackage.dataacesslayer.dto.TextObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *Classe capaz de ler todos os Text Objects de um pdf(somente os Tj e Tm deles)
 */
public class GabaritoScanner {
    Gabarito gabarito;
    PdfReader reader;
    PdfDictionary catalog;
    PdfArray contents;

    PRStream stream;
    byte[] streamBytes;
    String streamString;
    char[] streamCharArray;
    Pattern tmPattern;
    Matcher tmMatcher;
    Pattern tjPattern;
    Matcher tjMatcher;
    List<List<TextObject>> streamsList = new ArrayList<>();

    Logger logger = LogManager.getLogger();


    /**
     *
     * @param arquivoDir diretorio do arquivo pdf para scanear
     * @throws FileNotFoundException Se o arquivo não for encontrado
     * @throws IOException Se o {@link PdfReader} não aceitar o arquivo(provavelmente o arquivo é invalido se a exception for lançada)
     */
    public GabaritoScanner(String arquivoDir) throws FileNotFoundException, IOException{
        reader = new PdfReader(new FileInputStream(arquivoDir));
        catalog = reader.getCatalog();
        contents = catalog.getAsDict(new PdfName("Pages")).getAsArray(new PdfName("Kids")).getAsDict(0).getAsArray(new PdfName("Contents"));
    }

    /**
     * Le os Text Objects gabarito
     * @return Retorna uma List com todos os Text Objects de todas as streams(cada stream sendo uma lista de {@link TextObject} dentro da lista principal),
     * contendo seus Tj e Tm(veja a documentação do {@link TextObject})
     */
    public List<List<TextObject>> scanGabarito() {
        lerTodosTextObjects();
        return streamsList;
    }

    /**
     * Le todos os text objects de todas as streams e salva eles na variavel streamsList da classe
     */
    private void lerTodosTextObjects(){
        int index = 0;
        Optional<TextObject> tjPendente = Optional.empty();
        Optional<TextObject> tmPendente = Optional.empty();

        while(contents.size() != index) {
            stream = (PRStream) PdfReader.getPdfObject(contents.getPdfObject(index));
            try {
                streamBytes = PdfReader.getStreamBytes(stream);
                streamString = new String(streamBytes, Charset.forName("UTF-8"));
                streamCharArray = streamString.toCharArray();
                tmPattern = Pattern.compile("Tm");
                tmMatcher = tmPattern.matcher(streamString);
                tjPattern = Pattern.compile("Tj|TJ");
                tjMatcher = tjPattern.matcher(streamString);
            }catch (IOException e){
                throw new IOError(e);
            }
            List<TextObject> streamAtual = new ArrayList<>();

            while (true) {
                if(tjPendente.isPresent()){
                    tjMatcher.find();
                    tjPendente.get().setTj(getProximoTj(true));
                    logger.trace("tjpendente");
                    logger.trace("Fim do Text Object");
                    tjPendente = Optional.empty();
                    continue;
                }else if(tmPendente.isPresent()){ //TODO:Adaptar o metodo pra esse caso(provavel que vou precisar de unit tests melhores)
                    tmMatcher.find();
                    tmPendente.get().setTm(getProximoTm());
                    logger.trace("tmpendente");
                    logger.trace("Fim do Text Object");
                    continue;
                }

                boolean tjFound = false;
                boolean tmFound = false;
                if(tjMatcher.find()){
                    tjFound = true;
                }
                if(tmMatcher.find()){
                    tmFound = true;
                }
                if(tmFound && !tjFound){ // Se o text object continuar na stream seguinte pode acontecer isso, nao contei outras possibilidades
                    logger.trace("Inicio do Text Object");
                    tjPendente = Optional.of(new TextObject(getProximoTm(), null));
                    break;
                }else if (!tmFound && tjFound){// Se o text object continuar na stream seguinte pode acontecer isso, nao contei outras possibilidades
                    logger.trace("Inicio do Text Object");
                    tmPendente = Optional.of(new TextObject(null, getProximoTj(false)));
                    break;
                }else if(!tmFound && !tjFound){
                    break;
                }
                logger.trace("Inicio do Text Object");
                float[] tm = getProximoTm();
                Optional<String> tj = getProximoTj(false);
                streamAtual.add(new TextObject(tm, tj));
                logger.trace("Fim do Text Object");
            }
            logger.debug("Stream " + contents.getPdfObject(index) .toString() + " parseada");
            index++;
            streamsList.add(streamAtual);
        }
    }
    /**
     * Retorna a proxima text matrix encontrada pelo matcher em forma de float[]
     * @return Retorna a proxima text matrix encontrada pelo matcher em forma de float[]
     */
    private float[] getProximoTm(){
        float[] tm = new float[6];
        int currentIndex = 5;
        int numeroEndIndex = tmMatcher.start() - 2;
        String numero = null;
        while(true){
            if(streamCharArray[numeroEndIndex - 1] == ' ' || streamCharArray[numeroEndIndex - 1] == '\n' ){
                if(numero != null){
                    numero = streamCharArray[numeroEndIndex] + numero;
                }else {
                    numero = String.valueOf(streamCharArray[numeroEndIndex]);
                }
                tm[currentIndex] = Float.valueOf(numero);
                currentIndex--;
                numeroEndIndex--;//no fim do loop diminui mais um, assim pula o espaço e o proximo loop é o proximo numero
                if(currentIndex == -1){
                    break;
                }
                numero = null;
            }else{
                if(numero != null){
                    numero = streamCharArray[numeroEndIndex] + numero;
                }else {
                    numero = String.valueOf(streamCharArray[numeroEndIndex]);
                }
            }
            numeroEndIndex--;
        }
        logger.trace("Tm:" + Arrays.toString(tm));
        return tm;
    }

    /**
     * Retorna o proximo Tj encontrado pelo matcher em forma de String[Se for TJ com jota maiusculo(veja a referencia do pdf) ele retorna optional vazia]
     * @param tjPendente true se o tj estiver no inicio da file e nao tiver \n antes(pendente pq ele  so pode pertencer a um elemento de outra stream se ele estiver na primeira linha da stream)
     * @return Retorna o proximo Tj encontrado pelo matcher em forma de String[Se for TJ com jota maiusculo(veja a referencia do pdf) ele retorna optional vazia]
     */
    private Optional<String> getProximoTj(boolean tjPendente){
        if(streamCharArray[tjMatcher.end() - 1] == 'J'){
            logger.trace("Esse Object tem um TJ(array)");
            return Optional.empty();
        }
        String toReturn = "";
        int stringEndIndex = tjMatcher.start() - 2; // a sigla Tj n tem espaço da string apesar do Rups colocar
        boolean condicao = tjPendente?stringEndIndex >= 0:streamCharArray[stringEndIndex] != '\n';
         while(condicao){
            toReturn = streamCharArray[stringEndIndex] + toReturn;
            stringEndIndex --;
            condicao = tjPendente?stringEndIndex >= 0:streamCharArray[stringEndIndex] != '\n';
        }
        toReturn = toReturn.substring(1,toReturn.length()); // tirar o primeiro parentese
        toReturn = toReturn.translateEscapes(); //Pra processar as backslashs(pelo que eu entendi elas estão na mesma encoding da string e pra os bytes ate 255 a PDFDocEncoding é igual ao UTF-16
        logger.trace("Tj:" + toReturn);
        return Optional.of(toReturn);
    }

}
