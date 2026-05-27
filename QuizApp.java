import java.util.*;
import java.net.URI;
import java.net.http.*;
import java.util.concurrent.*;
public class QuizApp {
    public static void main(String[] args){
        System.out.println("-----Welcome to the QUIZ-----");
        QuizEngine engine = new QuizEngine();
        engine.startQuiz();

    }
}
class QuizEngine {
    private final QuizApi apiClient = new QuizApi();
    private Timecount timer;
    private boolean isQuizActive;
    public void startQuiz(){
        Scanner scanner = new Scanner(System.in);
        int s=0;
        try{
            System.out.println("Fetching quiz questions...");
            String rawJson = apiClient.fetchRawQuestionsJson();
            String[] questionBlocks = rawJson.split("\"question\":\"");
            for (int i=0;i<5;i++){
                String block = questionBlocks[i];
                String questionText = block.substring(0,block.indexOf("\""));
                int answerStart = block.indexOf("\"correct_answer\":\"") + 18;
                String remainingBlock = block.substring(answerStart);
                String correctAnswer = remainingBlock.substring(0,remainingBlock.indexOf("\""));
                questionText = questionText.replace("&quot;","\"").replace("&#039;","'").replace("&amp;","&");
                correctAnswer = correctAnswer.replace("&quot;","\"").replace("&#039;","'").replace("&amp;","&");
                System.out.println("\n---------------------------------");
                System.out.println("Question"+i+":"+questionText);
                System.out.println("---------------------------------");
                isQuizActive = true;
                timer = new Timecount(() -> {
                    System.out.println("\nTime's Up! Press Enter to continue...");
                    isQuizActive = false;

                });
                timer.start(10);
                String userAnswer = "";
                while (isQuizActive){
                   if(System.in.available()>0){
                       userAnswer = scanner.nextLine().trim();
                       isQuizActive = false;
                       timer.stop();
                   }
                   Thread.sleep(100);
                }
                if (userAnswer.equalsIgnoreCase(correctAnswer)){
                    System.out.println("Correct!");
                    s++;
                }
                else if (userAnswer.equals("")){
                    System.out.println("Skipped! The correct answer was: "+correctAnswer);
                }
                else {
                    System.out.println("Wrong! The correct answer was: "+correctAnswer);
                }
                        
            }
            System.out.println("\n---------------------");
            System.out.println("Quiz Completed! Your Score: "+s+"/5");
            System.out.println("---------------------");
        }
        catch(Exception e){
            System.out.println("Error in exceution: "+ e.getMessage());

        }
        finally{
            scanner.close();
        }
    }
}
class Timecount{
    private ScheduledExecutorService scheduler;
    private int secondsLeft;
    private final Runnable onTimeoutCallback;
    public Timecount(Runnable onTimeoutCallback){
        this.onTimeoutCallback = onTimeoutCallback;
    }
    public void start(int seconds){
        this.secondsLeft =seconds;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() ->{
            if (secondsLeft>0){
                System.out.println("\r[Time remaining:"+ secondsLeft +"s]   ");
                secondsLeft--;
            }
            else{
                stop();
                onTimeoutCallback.run();
            }
        },0,1,TimeUnit.SECONDS);
    }
    public void stop(){
        if (scheduler != null && !scheduler.isShutdown()){
            scheduler.shutdownNow();
        }
    }
}
class QuizApi {
    private final HttpClient client = HttpClient.newHttpClient();
    public String fetchRawQuestionsJson() throws Exception {
        String url = "https://opentdb.com/api.php?amount=5&type=multiple";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();

    }
}

