import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * Demo program for mining large pizza prizes from Papa John's Pizza Rewards (2016).
 *
 * This is a good demonstration of why captcha is important for high-volume requests coming from a single IP address.
 */
public class Program
{

    private static final String emailUrl = "https://www.moakt.com/en/mail";
    private static final String emailUrlChange = "https://www.moakt.com/en/mail/change";
    private static final String rewwardsUrl = "http://www.papajohns.co.uk/paparewards";

    private WebDriver papa = create();
    private WebDriver email = create();
    private String emailAddress;
    private boolean won = false;

    // Target number of pizzas to win
    private static final int PIZZA_TARGET = 2;

    // Number of individual threads to attempt winning pizzas
    private static final int WORKERS = 4;

    public static void main(String[] args) throws Exception
    {
        // Test alarms before starting...
        alert(true);
        alert(false);

        // start pool
        List<Thread> pool = new LinkedList<>();

        Thread worker;

        for (int i = 0; i < WORKERS; i++)
        {
            worker = new Thread(() -> {
                Program program = new Program();
                program.papaThread();
            });
            worker.start();
        }

        System.out.println("thread pool loaded");

        // join workers
        for (Thread thread : pool)
        {
            thread.join();
        }
    }

    private void papaThread()
    {
        while (!won && !isTargetPizzasMet())
        {
            try
            {
                generateEmail();
                roll();
            }
            catch (Throwable e)
            {
                System.err.println("something went wrong, next cycle...");
            }
        }

        // just keep the worker idle if it won a pizza...
        while (won)
        {
            try { Thread.sleep(1000); } catch (Exception e) { }
        }

        // dispose browser windows
        email.close();
        papa.close();
    }

    private void roll()
    {
        // Enter e-mail
        papa.get(rewwardsUrl);

        WebElement emailTextbox = papa.findElement(By.xpath("//*[@placeholder=\"Email\"]"));
        emailTextbox.sendKeys(emailAddress);
        emailTextbox.sendKeys(Keys.RETURN);

        // determine if email confirm needed
        boolean emailConfirm = papa.findElements(By.cssSelector(".prError")).size() > 0;
        if (emailConfirm)
        {
            confirmEmail();
        }
        else
        {
            // lets check our prize...
            String jsContent = findPrizeHtml();

            if (jsContent.contains("spin(0,"))
            {
                printResult("0");
            }
            else if (jsContent.contains("spin(8,"))
            {
                printResult("8");
                alert(false);
            }
            else if (jsContent.contains("spin(12,"))
            {
                printResult("12");
                alert(false);
            }
            else if (jsContent.contains("spin(25,"))
            {
                printResult("25");
                alert(true);
                won = true;
                incrementPizzasWon();
            }
            else
            {
                System.err.println("no script element found");
            }
        }
    }

    private void printResult(String points)
    {
        int id = getId();
        System.out.println(id + " - " + emailAddress + " - " + points + " points");
    }

    private String findPrizeHtml()
    {
        List<WebElement> scriptElements = papa.findElements(By.tagName("script"));
        String html;

        for (WebElement script : scriptElements)
        {
            html = script.getAttribute("innerHTML");
            if (html != null && html.contains("PAPAREWARDSWHEEL.spin"))
            {
                return html;
            }
        }
        return "";
    }

    private void generateEmail()
    {
        // Fetch emails page
        email.get(emailUrlChange);

        // Check if cloudflare warning...
        while (email.getTitle().contains("CloudFlare"))
        {
            try { Thread.sleep(100); } catch ( Exception e){}
        }

        //System.out.println("viewing email...");

        // Check if it has initial random/enter page
        boolean initialEmailPage = email.findElements(By.cssSelector(".mail_butt")).size() > 0;
        if (initialEmailPage)
        {
            //System.out.println("on initial page...");

            // Click random address button
            WebElement button = email.findElement(By.cssSelector(".mail_butt"));
            button.click();
        }
        else
        {
            //System.out.println("in existing inbox...");

            // Click change button
            //WebElement button = email.findElement(By.cssSelector(".butt_02"));
            //button.click();
        }

        // Read new email
        WebElement emailContainer = email.findElement(By.cssSelector(".viewaddedmail-btn"));
        emailAddress = emailContainer.getText();

        //System.out.println("new email: " + emailAddress);
    }

    private void confirmEmail()
    {
        // fetch inbox
        email.get(emailUrl);

        // find newest email
        WebElement emailEntry = email.findElement(By.xpath("//*[@id=\"email_message_list\"]/div/table/tbody/tr[2]/td[1]/a"));
        emailEntry.click();

        // find confirm link
        WebElement confirmLink = email.findElement(By.xpath("//*[@id=\"page_2\"]/div[1]/div[2]/div/div[4]/div[2]/p/a[7]"));
        confirmLink.click();

        // Roll again...
        roll();
    }

    private static WebDriver create()
    {
        String profile = "/home/limpygnome/.mozilla/firefox/i5hak9hs.default";

        WebDriver driver = new FirefoxDriver(new FirefoxBinary(new File("/home/limpygnome/firefox/firefox-46/firefox")), new FirefoxProfile(new File(profile)));
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        return driver;
    }

    private static void alert(boolean isLargePizza)
    {
        try
        {
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();

            MidiChannel[] channels = synthesizer.getChannels();

            if (isLargePizza)
            {
                for (int i = 0; i < 10; i++)
                {
                    channels[0].noteOn(80, 9999);
                    Thread.sleep(100);
                    channels[0].noteOn(90, 9999);
                    Thread.sleep(100);
                    channels[0].noteOn(100, 9999);
                    Thread.sleep(100);
                    channels[0].noteOn(110, 9999);
                    Thread.sleep(100);
                    channels[0].noteOn(120, 9999);
                    Thread.sleep(100);
                }

                Thread.sleep(5000);
            }
            else
            {
                channels[0].noteOn(60, 9999);
                channels[0].noteOn(56, 9999);
                Thread.sleep(3000);

                channels[0].noteOn(54, 9999);
                channels[0].noteOn(52, 9999);
                Thread.sleep(3000);

                channels[0].noteOn(58, 9999);
                channels[0].noteOn(56, 9999);
                Thread.sleep(3000);
            }

            synthesizer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Used to count attempts
    private static int counter = 0;

    // Fetches new attempt identifier for logging
    private static synchronized int getId()
    {
        return ++counter;
    }

    // Used to count large pizzas won
    private static int pizzasWon = 0;

    // Increments large pizzas won
    private static synchronized void incrementPizzasWon()
    {
        int pizzas = ++pizzasWon;
        System.out.println(pizzas + " / " + PIZZA_TARGET + " have been won");
    }

    // Used to determine if to continue mining...
    private static synchronized boolean isTargetPizzasMet()
    {
        return pizzasWon >= PIZZA_TARGET;
    }

}
