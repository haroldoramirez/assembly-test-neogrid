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
    * Metodo principal onde esta sendo recebido o arquivo do front-end e aplicado a logica de linha de montagem
    *
    * Logica
    * Verificar cada linha que contem os minutos, somar na jornada e adicionar no array de saida
    * Enquanto nao chegar ao meio dia ou na ginastica vai somando o tempo
    * Adicionar o texto almoco quando chegar meio dia e adicionar ginatica quando passar das 16:00
    * */
    public Result read(Http.Request request) throws IOException {

        //Receber o arquivo da requisicao
        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<Files.TemporaryFile> arquivo = body.getFile("file");

        //verificar se o arquivo nao esta nulo
        if (arquivo != null) {

            String linha;
            LocalTime jornadaManha = LocalTime.parse("09:00:00");
            LocalTime jornadaTarde = LocalTime.parse("13:00:00");
            LocalTime almoco = LocalTime.parse("12:00:00");
            LocalTime ginastica = LocalTime.parse("16:00:00");
            DateTimeFormatter formatador = DateTimeFormatter.ISO_LOCAL_TIME;

            List<String> saida = new ArrayList<>();

            Files.TemporaryFile tempFile = arquivo.getRef();

            BufferedReader bReader = new BufferedReader(new FileReader(tempFile.path().toFile()));

            while ((linha = bReader.readLine()) != null) {
                if (jornadaManha.isBefore(almoco)) {
                    saida.add(jornadaManha.format(formatador) + " " + linha);
                    if (linha.contains("30min")) {
                        jornadaManha = jornadaManha.plusMinutes(30);
                    } else if (linha.contains("45min")) {
                        jornadaManha = jornadaManha.plusMinutes(45);
                    } else {
                        jornadaManha = jornadaManha.plusMinutes(60);
                    }
                } else if (jornadaManha.isAfter(almoco)) {
                    saida.add(jornadaManha.format(formatador) + " Almoço");
                    jornadaManha = jornadaTarde;
                }
            }

            System.out.println("Fim da Jornada da manha " + jornadaManha.format(formatador));

            return ok(views.html.resultado.render(bReader, "", saida));
        } else {
            return badRequest().flashing("error", "Missing file");
        }

    }

}
