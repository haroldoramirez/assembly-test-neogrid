package controllers;

import play.libs.Files;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
    * Metodo principal onde recebe o arquivo do front-end e aplicado a logica de linha de montagem
    *
    * Logica
    * Verificar cada linha que contem os minutos, somar na jornada e adicionar no array de saida
    * Enquanto nao chegar ao meio dia ou na ginastica vai somando o tempo
    * Adicionar o texto almoco quando chegar meio dia e adicionar ginatica quando passar das 16:00
    * */
    public Result read(Http.Request request) throws IOException {

        //Recebe o arquivo da requisicao e preparamos para trabalhar com linhas
        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<Files.TemporaryFile> arquivo = body.getFile("file");
        Files.TemporaryFile tempFile = arquivo.getRef();

        //Variaveis auxiliares
        String linha;
        LocalTime jornada = LocalTime.parse("09:00:00");
        LocalTime almoco = LocalTime.parse("12:00:00");
        LocalTime ginastica = LocalTime.parse("16:00:00");
        DateTimeFormatter formatador = DateTimeFormatter.ISO_LOCAL_TIME;

        //Lista da linha de montagem - output
        List<String> saida = new ArrayList<>();

        //Flags utilizadas
       boolean horarioAlmoco = false;
       boolean horarioGinastica = false;

        //verificar se o arquivo nao esta nulo
        if (arquivo != null) {

            BufferedReader bReader = new BufferedReader(new FileReader(tempFile.path().toFile()));

            //Verificar se nao tem nenhuma linha vazia
            while ((linha = bReader.readLine()) != null) {
                if (jornada.isBefore(almoco)) {
                    System.out.println("Precisa passar apenas da manha!");
                    saida.add(jornada.format(formatador) + " " + linha);
                    if (linha.contains("30min")) {
                        jornada = jornada.plusMinutes(30);
                    } else if (linha.contains("45min")) {
                        jornada = jornada.plusMinutes(45);
                    } else if (linha.contains("60min")) {
                        jornada = jornada.plusMinutes(60);
                    } else {
                        //Manutencao adiciona apenas 5 minutos
                        jornada = jornada.plusMinutes(5);
                    }
                }
                if ((jornada.isAfter(almoco) || jornada.equals(almoco)) && !horarioAlmoco) {
                    horarioAlmoco = true;
                    saida.add(jornada.format(formatador) + " Almoço");
                    System.out.println("Precisa passar apenas no Almoço!");
                    jornada = LocalTime.parse("13:00:00");
                }
                if (jornada.isBefore(ginastica) && horarioAlmoco) {
                    System.out.println("Precisa passar apenas da tarde!");
                    saida.add(jornada.format(formatador) + " " + linha);
                    if (linha.contains("30min")) {
                        jornada = jornada.plusMinutes(30);
                    } else if (linha.contains("45min")) {
                        jornada = jornada.plusMinutes(45);
                    } else if (linha.contains("60min")) {
                        jornada = jornada.plusMinutes(60);
                    } else {
                        //Manutencao adiciona apenas 5 minutos
                        jornada = jornada.plusMinutes(5);
                    }
                }
                if ((jornada.isAfter(ginastica) || jornada.equals(ginastica)) && !horarioGinastica) {
                    horarioGinastica = true;
                    saida.add(jornada.format(formatador) + " Ginástica laboral");
                    System.out.println("Precisa passar apenas na Ginástica!");
                }
            }

            System.out.println("Fim da Jornada da manha " + jornada.format(formatador));
            System.out.println("Almocando " + horarioAlmoco);
            System.out.println("--------------------------------------------------------");
            //System.out.println("Jornada da Tarde " + jornadaTarde.format(formatador));
            System.out.println("Ginastica " + horarioGinastica);

            return ok(views.html.resultado.render(bReader, "", saida, arquivo.getFilename()));
        } else {
            return badRequest().flashing("error", "Missing file");
        }

    }

}
