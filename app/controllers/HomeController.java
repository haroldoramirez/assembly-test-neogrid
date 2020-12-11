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

        //Variaveis auxiliares
        String linha;
        LocalTime jornada = LocalTime.parse("09:00:00");
        LocalTime jornada2 = LocalTime.parse("09:00:00");
        LocalTime almoco = LocalTime.parse("12:00:00");
        LocalTime ginastica = LocalTime.parse("16:00:00");
        DateTimeFormatter formatador = DateTimeFormatter.ISO_LOCAL_TIME;

        //Lista da linha de montagem - output
        List<String> saida = new ArrayList<>();
        List<String> saida2 = new ArrayList<>();

        //Flags utilizadas
        boolean horarioAlmoco = false;
        boolean horarioAlmoco2 = false;
        boolean horarioGinastica = false;
        boolean horarioGinastica2 = false;
        boolean jornada1Concluida = false;
        boolean flag = true;

        //verificar se o arquivo nao esta nulo
        if (arquivo != null) {

            Files.TemporaryFile tempFile = arquivo.getRef();

            BufferedReader bReader = new BufferedReader(new FileReader(tempFile.path().toFile()));

            /**
             * Atividades
             * 1 - Buscar uma forma de nao repetir a ultima tarefa no inicio da tarde - Verificar no laco while!
             * 2 - Criar o output2 - ok
             * 3 - Equilibrar as tarefas do output 1 e output 2 para respeitar o horário de almoco e ginastica como?
             * 4 - Criar um verificador de horas das tarefas pra nao passar das 12:00hs(almoco) ou das 16:00(ginastica laboral), se passar do horário, enviar para o output2
             */
            while ((linha = bReader.readLine()) != null) {

                //Jornada 1 - Linha de montagem 1
                //Jornada antes do almoco
                if (jornada.isBefore(almoco) || jornada.equals(almoco)) {
                    saida.add(jornada.format(formatador) + " " + linha);
                    System.out.println("Jornada a manha: " + jornada.format(formatador) + " " + linha);
                    if (linha.contains("30min")) {
                        jornada = jornada.plusMinutes(30);
                    } else if (linha.contains("45min")) {
                        jornada = jornada.plusMinutes(45);
                    } else if (linha.contains("60min")) {
                        jornada = jornada.plusMinutes(60);
                    } else {
                        //Maintenance - Manutencao adiciona apenas 5 minutos
                        jornada = jornada.plusMinutes(5);
                    }
                }
                //Jornada depois de almoco ou igual ao almoco - para destacar a linha do almoco
                if ((jornada.isAfter(almoco) || jornada.equals(almoco)) && !horarioAlmoco) {
                    System.out.println("Vetor Saida Manhã: " + saida);
                    horarioAlmoco = true;
                    saida.add(jornada.format(formatador) + " Almoço");
                    jornada = LocalTime.parse("13:00:00");
                }
                //Jornada antes da ginastica
                if (jornada.isBefore(ginastica) && horarioAlmoco) {
                    //precisa ser ignorada para nao repetir no inicio da tarde - isolar
                    System.out.println("Jornada a tarde: " + jornada.format(formatador) + " " + linha);
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
                //Jornada depois da ginastica ou antes da ginastica -  para destacar a linha da ginastica laboral
                if ((jornada.isAfter(ginastica) || jornada.equals(ginastica)) && !horarioGinastica) {
                    System.out.println("Vetor Saida Tarde: " + saida);
                    horarioGinastica = true;
                    saida.add(jornada.format(formatador) + " Ginástica laboral");
                    //System.out.println("Jornada 1 - Precisa passar apenas na Ginástica!");
                    jornada1Concluida = true;
                    //System.out.println("--------------------------------------------------------");
                    //System.out.println("Jornada 1 - Fim da Jornada " + jornada.format(formatador));
                }

                //Jornada 2 - Linha de montagem 2
                //A Jornada 1 precisa estar concluida
                if (jornada1Concluida) {
                    if (jornada2.isBefore(almoco)) {
                        //System.out.println("Jornada 2 - Precisa passar apenas da manha!");
                        saida2.add(jornada2.format(formatador) + " " + linha);
                        if (linha.contains("30min")) {
                            jornada2 = jornada2.plusMinutes(30);
                        } else if (linha.contains("45min")) {
                            jornada2 = jornada2.plusMinutes(45);
                        } else if (linha.contains("60min")) {
                            jornada2 = jornada2.plusMinutes(60);
                        } else {
                            //Manutencao adiciona apenas 5 minutos
                            jornada2 = jornada2.plusMinutes(5);
                        }
                    }
                    if ((jornada2.isAfter(almoco) || jornada2.equals(almoco)) && !horarioAlmoco2) {
                        horarioAlmoco2 = true;
                        saida2.add(jornada2.format(formatador) + " Almoço");
                        //System.out.println("Jornada 2 - Precisa passar apenas no Almoço!");
                        jornada2 = LocalTime.parse("13:00:00");
                    }
                    if (jornada2.isBefore(ginastica) && horarioAlmoco2) {
                        //System.out.println("Jornada 2 - Precisa passar apenas de tarde!");
                        saida2.add(jornada2.format(formatador) + " " + linha);
                        if (linha.contains("30min")) {
                            jornada2 = jornada2.plusMinutes(30);
                        } else if (linha.contains("45min")) {
                            jornada2 = jornada2.plusMinutes(45);
                        } else if (linha.contains("60min")) {
                            jornada2 = jornada2.plusMinutes(60);
                        } else {
                            //Manutencao adiciona apenas 5 minutos
                            jornada2 = jornada2.plusMinutes(5);
                        }
                    }
                    if ((jornada2.isAfter(ginastica) || jornada2.equals(ginastica)) && !horarioGinastica2) {
                        horarioGinastica2 = true;
                        saida2.add(jornada2.format(formatador) + " Ginástica laboral");
                        //System.out.println("Jornada 2 - Precisa passar apenas na Ginástica!");
                        //System.out.println("--------------------------------------------------------");
                        //System.out.println("Jornada 2 - Fim da Jornada " + jornada.format(formatador));
                    }
                }
            }

            //System.out.println("--------------------------------------------------------");
            //System.out.println("Jornada 2 - Fim da Jornada da manha  " + jornada2.format(formatador));

            return ok(views.html.resultado.render(bReader, "", saida, saida2, arquivo.getFilename()));
        } else {
            return badRequest().flashing("error", "Missing file");
        }

    }

}
