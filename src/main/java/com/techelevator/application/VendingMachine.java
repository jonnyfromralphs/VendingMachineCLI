package com.techelevator.application;

import com.techelevator.file_io.InventoryLoader;
import com.techelevator.file_io.Logger;
import com.techelevator.models.exceptions.InsufficientFundsException;
import com.techelevator.models.exceptions.InvalidChoiceException;
import com.techelevator.models.exceptions.InvalidPasswordException;
import com.techelevator.models.exceptions.OutOfStockException;
import com.techelevator.models.products.Product;
import com.techelevator.view.Input;
import com.techelevator.view.Output;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

public class VendingMachine
{
    //instance variables
    //Linked hash map to keep order of insertion
    private LinkedHashMap<String, Product> inventory = InventoryLoader.stockInventory();
    private LinkedHashMap<String, BigDecimal> sales = new LinkedHashMap<>();
    private BigDecimal moneyProvided = BigDecimal.ZERO;
    private BigDecimal totalSales;
    private String input;
    private Logger logger = new Logger("logs/");
    private final String PATH = "data/salesreport.csv";
    private final File SALES_REPORT_CSV = new File(PATH);
    private final String TOTAL_SALES_KEY = "**TOTAL SALES**";

    //Getters and setters
    public LinkedHashMap<String, Product> getInventory()
    {
        return inventory;
    }
    public BigDecimal getMoneyProvided()
    {
        return moneyProvided;
    }
    public void insertMoney(BigDecimal amount)
    {
        moneyProvided = moneyProvided.add(amount);
    }

    public void run()
    {
        //Load previous sales from csv file, display initial welcome screen, and get user input to continue
        startup();

        //Program's initial running loop
        while (true)
        {
            try
            {
                //Print home screen and get user's option. If user's choice valid, throw exception
                Output.printHomeScreen();
                input = Input.getInput();

                //constant variables to clean up switch
                final String LIST_INVENTORY = "1";
                final String PURCHASE = "2";
                final String EXIT = "3";
                final String ADMIN = "4";

                //call helper functions if choice is valid
                switch (input)
                {
                    case LIST_INVENTORY:
                        //List id, name, price, and quantity of each item
                        listInventory();
                        continue;
                    case PURCHASE:
                        //Allow user to feed money, purchase an item, and get change
                        purchaseScreen();
                        continue;
                    case EXIT:
                        //Upon exiting the program, write to csv file
                        writeSales();
                        return;
                    case ADMIN:
                        //Allow user to view or clear sales log
                        adminScreen();
                        continue;
                    default:
                        throw new InvalidChoiceException();
                }
            }
            //catch exceptions and display error message
            catch (InvalidChoiceException e)
            {
                Output.printErrorMessage(e);
            }
        }
    }

    private void startup()
    {
        getPreviousSales();
        Output.printWelcomeScreen();
        input = Input.getInput();
    }

    private void listInventory()
    {
        Output.printItems(inventory);
        logger.logMessage("INVENTORY LISTED");
        input = Input.getInput();
    }

    private void purchaseScreen()
    {
        final String FEED_MONEY = "1";
        final String SELECT_PRODUCT = "2";
        final String FINISH_TRANSACTION = "3";

        while (true)
        {
            try
            {
                Output.printPurchaseScreen(moneyProvided);
                input = Input.getInput();

                switch (input)
                {
                    case FEED_MONEY:
                        feedMoney();
                        continue;
                    case SELECT_PRODUCT:
                        selectProductToPurchase();
                        continue;
                    case FINISH_TRANSACTION:
                        finishTransaction();
                        return;
                    default:
                        throw new InvalidChoiceException();
                }
            }
            catch (InvalidChoiceException e)
            {
                Output.printErrorMessage(e);
            }
        }
    }

    private void feedMoney()
    {
        BigDecimal amount;
        //While money amount is invalid
        while (true)
        {
            Output.printFeedMoneyScreen(moneyProvided);
            try
            {
                input = Input.getInput();

                //Checks if input is numeric first, then for any pennies or negative amounts
                if (!isNumeric(input))
                {
                    throw new InvalidChoiceException();
                }
                amount = new BigDecimal(input);
                if (!isValidMoneyAmount(amount))
                {
                    throw new InvalidChoiceException();
                }
                else
                {
                    break;
                }
            } catch (InvalidChoiceException e)
            {
                Output.printErrorMessage(e);
            }

        }
        //Adds amount to instance variable if all is successful
        insertMoney(amount);
        logger.logMessage("FEED MONEY $" + amount + " $" + moneyProvided);
    }

    private void selectProductToPurchase()
    {
        final String EXIT_PURCHASE = "3";
        while (true)
        {
            Output.printSelectProductScreen();
            input = Input.getInput();
            String id = input.toUpperCase();
            try
            {
                //If valid id for vending machine
                if (inventory.containsKey(id))
                {
                    Product product = inventory.get(id);
                    String name = product.getName();
                    int stock = product.getRemainingQuantity();
                    BigDecimal price = product.getPrice();

                    //Check if enough money is in machine and if that item is in stock
                    if (moneyProvided.compareTo(price) >= 0)
                    {
                        if (stock > 0)
                        {
                            //Increase quantity sold in product, display message, and update sales map and total sales
                            product.itemsSold();
                            moneyProvided = moneyProvided.subtract(price);
                            Output.printSoldProduct(product, moneyProvided);
                            inventory.put(id, product);
                            sales.put(name, sales.get(name).add(BigDecimal.ONE));
                            sales.put(TOTAL_SALES_KEY, sales.get(TOTAL_SALES_KEY).add(price));
                            logger.logMessage(name + " " + id + " " + price + " " + moneyProvided);
                        }
                        else
                        {
                            throw new OutOfStockException();
                        }

                    }
                    else
                    {
                        throw new InsufficientFundsException();
                    }
                }
                else if (input.equals(EXIT_PURCHASE))
                {
                    return;
                }
                else
                {
                    throw new InvalidChoiceException();
                }
            }
            catch (Exception e)
            {
                Output.printErrorMessage(e);
            }
        }
    }

    //Calls helper function to determine change and display it to user
    private void finishTransaction()
    {
        String change = giveChange();
        Output.printChange(moneyProvided, change);
        logger.logMessage("GIVE CHANGE $" + moneyProvided);
        moneyProvided = BigDecimal.ZERO;
    }

    private void adminScreen()
    {
        while (true)
        {
            try
            {
                Output.printHiddenScreen();
                input = Input.getInput();

                final String EXIT_ADMIN_MENU = "3";
                final String PASSWORD = "lightship";

                //If input is equal to the password, enter admin menu. If input is 3 go back, else throw an error
                if (input.equals(PASSWORD))
                {
                    final String VIEW_SALES = "1";
                    final String CLEAR_SALES = "2";

                    Output.printAdminMenu();
                    input = Input.getInput();

                    //If admin wants to view sales, update csv file with values in sales and display
                    if (input.equals(VIEW_SALES))
                    {
                        writeSales();
                        Output.printLogs(SALES_REPORT_CSV);
                    }
                    else if (input.equals(CLEAR_SALES))
                    {
                        clearSales();
                    }
                    else
                    {
                        throw new InvalidChoiceException();
                    }

                }
                else if (input.equals(EXIT_ADMIN_MENU))
                {
                    return;
                }
                else
                {
                    throw new InvalidPasswordException();
                }


            }
            catch (Exception e)
            {
                Output.printErrorMessage(e);
            }
        }
    }

    //Returns true if amount inserted is non negative and if no pennies were inserted
    public boolean isValidMoneyAmount(BigDecimal amount)
    {
        double moneyToBeInserted = amount.doubleValue();
        int moneyInPennies = (int) (moneyToBeInserted * 100);
        if (moneyInPennies < 0)
        {
            return false;
        }
        else if (moneyInPennies % 5 != 0)
        {
            return false;
        }
        return true;
    }

    //Checks if given string is numeric
    public boolean isNumeric(String str)
    {
        try
        {
            Double.parseDouble(str);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }

    //Converts money left in machine to a double, then a whole number to do modulo math on to get quarters, dimes, and nickels
    public String giveChange()
    {
        double moneyInMachine = moneyProvided.doubleValue();
        int totalChange = (int) (moneyInMachine * 100);
        int quarters = totalChange / 25;
        totalChange = totalChange % 25;
        int dimes = totalChange / 10;
        totalChange = totalChange % 10;
        int nickels = totalChange / 5;
        return quarters + " Quarters | " + dimes + " Dimes | " + nickels + " Nickels";
    }

    //Read from sales report csv and populate hashmap with values retrieved from file
    private void getPreviousSales()
    {
        try (Scanner reader = new Scanner(SALES_REPORT_CSV))
        {
            while (reader.hasNextLine())
            {
                String line = reader.nextLine();
                String[] lineSplit = line.split("\\|");
                String snack = lineSplit[0];
                BigDecimal quantity = new BigDecimal(lineSplit[1]);
                sales.put(snack, quantity);
            }
        }
        catch (IOException e)
        {
            Output.printErrorMessage(e);
        }
    }

    //Write to csv file with current values in hashmap
    private void writeSales()
    {
        try (PrintWriter writer = new PrintWriter(SALES_REPORT_CSV))
        {
            for (String key : sales.keySet())
            {
                writer.println(key + "|" + sales.get(key));
            }
        }
        catch ( IOException e)
        {
            Output.printErrorMessage(e);
        }
    }

    //Set all values to 0
    private void clearSales()
    {
        try (PrintWriter writer = new PrintWriter(SALES_REPORT_CSV))
        {
            for (String key : sales.keySet())
            {
                sales.put(key, BigDecimal.ZERO);
                writer.println(key + "|0");
            }
        }
        catch ( IOException e)
        {
            Output.printErrorMessage(e);
        }
    }
}
