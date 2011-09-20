package example.selenium.facebook.pokes;

import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.internal.FindsByXPath;

import com.thoughtworks.selenium.Wait;

/**
 * こんにちわこんにちわ
 */
public class PokesEraser 
{
	private static boolean DEBUG = false; 
    private static final int WAIT_SECOND = 5;

    public void pokesErase(String email, String password,
    		AccountLaungage accountLaungage,
    		       DriverStorategy driverStorategy 
    		       ) throws PokeException {
    	WebDriver driver = null;
    	try { 
    		driver = driverStorategy.getDriver();
    		driver.get("http://www.facebook.com/");

    		//ログインしまっせ
    		login(driver, email, password, accountLaungage);
    		
    		//あいさつしまくる
    		returnPokes(driver, accountLaungage);
    		
    	} finally  {
    		if (driver != null) {
    			driver.close();
    		}
    	}
		
	}
    
	private void returnPokes(WebDriver driver, AccountLaungage accountLaungage) {
		if (false == exists(driver, By.id("pagelet_pokes"))) {
			//挨拶のpageletがない場合は何もしない
			return;
		}
		
		WebElement pageletPokes = driver.findElement(By.id("pagelet_pokes"));
		
		//すべて表示があったら押す
		showAllIfNeed(pageletPokes);
		
		List<WebElement> pokeElems = pageletPokes.findElements(By.xpath("//a[contains(@ajaxify, 'poke_dialog.php')]"));
		for (WebElement pokeElem : pokeElems) {
			poke(driver, pokeElem, accountLaungage);
		}
	}

	private void poke(WebDriver driver, WebElement pokeElem, AccountLaungage accountLaungage) {
		//あいさつを返す要素をクリックする
		pokeElem.click(); 
		
		//あいさつをするボタンが出るのを待つ
		By pokeButtonBy = new ByValue(accountLaungage.getPokeButtonValue());
		if (DEBUG) {
			pokeButtonBy = By.name("cancel");
		}
		waitPresent(driver, pokeButtonBy);

		WebElement button = driver.findElement(pokeButtonBy);
		button.click();
		//ボタンがなくなってる（Ajaxで処理が終わったことを確認)
		waitNotPresent(driver, pokeButtonBy);
	}

	private void showAllIfNeed(WebElement pageletPokes) {
		if (exists(pageletPokes, By.className("showAll"))) {
			WebElement elem = pageletPokes.findElement(By.className("showAll"));
			elem.click();
			//すべて表示を押して、Ajaxですべて表示がなくなるまで待つ
			waitInvisible(pageletPokes, By.className("showAll"));
		}
		
	}

	private void login(WebDriver driver, String email, String password, AccountLaungage accountLaungage) throws PokeException {
		typeEmail(driver, email);
		typePassword(driver, password);
		doLogin(driver, accountLaungage);
	}

	private void doLogin(WebDriver driver, AccountLaungage accountLaungage) throws PokeException  {
		WebElement elem = driver.findElement(By.xpath("//input[@value='"+accountLaungage.gtLoginButtonValue()+"']"));
		elem.click();
	}



	private void typePassword(WebDriver driver, String password) {
		WebElement elem = driver.findElement(By.id("pass"));
		elem.clear();
		elem.sendKeys(password);
	}


	private void typeEmail(WebDriver driver, String email) {
		WebElement elem = driver.findElement(By.id("email"));
		elem.clear();
		elem.sendKeys(email);
	}

	private void waitPresent(final SearchContext context, final By by) {
		Wait wait = new Wait() {
		    @Override
		    public boolean until() {
		    	try {
		    		WebElement elem = context.findElement(by);
		    		return elem.isDisplayed();
		    	} catch (NoSuchElementException e) {
		    		return false;
		    	}
		    }
		};
		
		wait.wait("Element not exists", WAIT_SECOND * 1000); 
	}
	
	private void waitNotPresent(final SearchContext context, final By by) {
		Wait wait = new Wait() {
		    @Override
		    public boolean until() {
		    	try {
		    		context.findElement(by);
		    		return false;
		    	} catch (NoSuchElementException e) {
		    		return true;
		    	}
		    }
		};
		
		wait.wait("Element not exists", WAIT_SECOND * 1000); 
	}
	
	//非表示になるまでまつ
	private void waitInvisible(final SearchContext context, final By by) {
		Wait wait = new Wait() {
		    @Override
		    public boolean until() {
		    	WebElement elem = context.findElement(by);
		    	return false == elem.isDisplayed();
		    }
		};
		
		wait.wait("Element exists", WAIT_SECOND * 1000);
    }
	
	
    private boolean exists(SearchContext searchContext, By by) {
    	try {
    		searchContext.findElement(by);
    		return true;
    	} catch (NoSuchElementException e) {
    		return false;
    	}
    }
    
    static class ByValue extends By {
        private final String value;

        public ByValue(String className) {
          this.value = className;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
          return ((FindsByXPath) context).findElementsByXPath(".//*["
              + containingWord("value", value) + "]");
        }

        @Override
        public WebElement findElement(SearchContext context) {
          return ((FindsByXPath) context).findElementByXPath(".//*["
              + containingWord("value", value) + "]");
        }
        
        private String containingWord(String attribute, String word) {
            return "contains(concat(' ',normalize-space(@" + attribute + "),' '),' "
                + word + " ')";
          }

    }
	
	public static void main(String[] args) throws Exception {
		
		Options options = getCommandLineOptions();
		CommandLine cl = getCommandLine(args, options);
		
		PokesEraser man = new PokesEraser();
    	AccountLaungage accountLaungage = getAccountLaungage(cl, options);
    	
    	DriverStorategy driverStorategy = getDriverStorategy(options, cl);
    	if (cl.hasOption("debug")) {
    		DEBUG = true;
    	}
    	
    	System.out.println("DEBUG = " + DEBUG);
    	
    	man.pokesErase(cl.getOptionValue("email"), 
    			      cl.getOptionValue("password"), 
    			      accountLaungage, driverStorategy);
    }

	private static DriverStorategy getDriverStorategy(Options options, CommandLine cl) {
		DriverStorategy driverStorategy = DriverStorategy.firefox;
    	if (cl.hasOption("browser")) {
    		try {
    			driverStorategy = DriverStorategy.valueOf(cl.getOptionValue("browser"));
    		} catch (IllegalArgumentException e) {
    			help(options);
    		}
    	}
		return driverStorategy;
	}

	private static Options getCommandLineOptions() {
		Options opt = new Options();
		Option userOption = new Option("email", true, "email。必須です。");
		userOption.setRequired(true);
		opt.addOption(userOption);
		Option passwordOption = new Option("password", true, "パスワードです。必須です。");
		passwordOption.setRequired(true);
		opt.addOption(passwordOption);
		opt.addOption("help", false, "ヘルプが表示されます");
		opt.addOption("debug", false, "デバッグモードで実行します。ポイントの付与の直前まで実行します。");
		String browserNames = StringUtils.join(DriverStorategy.values(), ",");
		opt.addOption("browser", true, "ブラウザを指定します。デフォルトは firefox です。"  + browserNames);
	
		String langages = StringUtils.join(AccountLaungage.values(), ",");
		opt.addOption("laungage", true, "言語を指定します。デフォルトは japanese です。"  + langages);
		
	
		return opt;
	}

	private static CommandLine getCommandLine(String[] args, Options options) {
		BasicParser parser = new BasicParser();
		CommandLine cl = null;
		try {
			cl = parser.parse(options, args);
		} catch (ParseException e) {
			help(options);
		}
		
		if (cl.hasOption("help")) {
			help(options);
		}
		return cl;
	}

	private static void help(Options options) {
		HelpFormatter f = new HelpFormatter();
		f.printHelp("OptionsTip", options);
		System.exit(1);
	}

	private static AccountLaungage getAccountLaungage(CommandLine cl, Options options) {
		AccountLaungage langage = AccountLaungage.japanese;
    	if (cl.hasOption("laungage")) {
    		String laungageStr = cl.getOptionValue("laungage");
    		try {
    			langage = AccountLaungage.valueOf(laungageStr);
    		} catch (IllegalArgumentException illegalArgumentException) {
    			help(options);
    		}
    	}
		return langage;
	}
}

enum DriverStorategy {
	firefox, ie; 
	
	public WebDriver getDriver() {
		if (this == firefox) {
			return new FirefoxDriver();
		} else if (this == ie) {
			return new InternetExplorerDriver();
		} else {
			throw new IllegalStateException("hoge");
		}
	}
	
}

enum AccountLaungage {
//	japanese("ログイン", "あいさつを返す", "すべて表示"), english("Log In", "Poke Back", "Show all");
	japanese("ログイン", "あいさつする"), english("Log In", "Poke");
	
	private String loginButtonValue;
	private String pokeButtonValue;
	
	private AccountLaungage(String loginButtonValue, String pokeButtonValue) {
		this.loginButtonValue = loginButtonValue;
		this.pokeButtonValue = pokeButtonValue;
	}
	public String gtLoginButtonValue() {
		return loginButtonValue;
	}
	public String getPokeButtonValue() {
		return pokeButtonValue;
	}
}

class PokeException extends Exception {
	private static final long serialVersionUID = 1L;

	public PokeException(String msg) {
		super(msg);
	}
	
	public PokeException(String msg, Exception cause) {
		super(msg, cause);
	}
	
}

