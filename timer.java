import java.util.*;
public class timer{
    public static void main(String[] args)throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the begining of countdown: ");
        int s = sc.nextInt();
        for (int i=s;i>0;i--){
            System.out.println(i);
            Thread.sleep(1000);
            }
        System.out.println("Time's up!");

    }
}
    
