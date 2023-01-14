package com.techelevator;

import com.techelevator.application.VendingMachine;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class VendingMachineTest
{
    VendingMachine vendingMachine = new VendingMachine();

    @Test
    public void isValidMoneyAmount_should_return_if_amount_is_positive_or_has_no_pennies()
    {
        //Arrange
        BigDecimal test1 = new BigDecimal("-10.00");
        BigDecimal test2 = new BigDecimal("0.01");
        BigDecimal test3 = new BigDecimal("3.95");

        //Act
        boolean actual1 = vendingMachine.isValidMoneyAmount(test1);
        boolean actual2 = vendingMachine.isValidMoneyAmount(test2);
        boolean actual3 = vendingMachine.isValidMoneyAmount(test3);

        //Assert
        Assert.assertFalse("Because the amount is negative", actual1);
        Assert.assertFalse("Because the amount has pennies", actual2);
        Assert.assertTrue("Because the amount is valid", actual3);
    }

    @Test
    public void isNumberic_should_return_true_only_if_amount_is_numeric()
    {
        //Arrange
        String test1 = "abcd";
        String test2 = "-10";
        String test3 = "3.50";

        //Act
        boolean actual1 = vendingMachine.isNumeric(test1);
        boolean actual2 = vendingMachine.isNumeric(test2);
        boolean actual3 = vendingMachine.isNumeric(test3);

        //Assert
        Assert.assertFalse("Because the string is not numberic", actual1);
        Assert.assertTrue("Because the amount is numeric", actual2);
        Assert.assertTrue("Because the amount is numeric", actual3);
    }

    @Test
    public void giveChange_should_return_formatted_string_of_change()
    {
        //Arrange
        vendingMachine.insertMoney(new BigDecimal("1.65"));
        String expected = "6 Quarters | 1 Dimes | 1 Nickels";

        //Act
        String actual = vendingMachine.giveChange();

        //Assert
        Assert.assertEquals("Because $1.65 in change is 6 quarters, a dime, and a nickel", expected, actual);
    }
}
