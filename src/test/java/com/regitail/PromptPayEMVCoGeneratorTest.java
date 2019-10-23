package com.regitail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptPayEMVCoGeneratorTest {

    @DisplayName("Test Telephone Number with 50.23")
    @Test
    void generate01() {
        String target = "0812345678";
        Double amount = 50.23D;
        String expected = "00020101021229370016A000000677010111011300668123456785802TH5303764540550.236304FCDA";

        try {
            PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
            assertEquals(expected, promptpay.generate());
        } catch (TargetMismatchException ex) {
            ex.printStackTrace();
        } catch (NegativeAmountException ex) {
            ex.printStackTrace();
        }
    }

    @DisplayName("Test Telephone Number with 9999999.99")
    @Test
    void generate02() {
        String target = "0000000000";
        Double amount = 9999999.99D;
        String expected = "00020101021229370016A000000677010111011300660000000005802TH530376454109999999.99630479D1";

        try {
            PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
            assertEquals(expected, promptpay.generate());
        } catch (TargetMismatchException ex) {
            ex.printStackTrace();
        } catch (NegativeAmountException ex) {
            ex.printStackTrace();
        }
    }

    @DisplayName("Test Citizen Number with 9999999.99")
    @Test
    void generate03() {
        String target = "0000000000000";
        Double amount = 9999999.99D;
        String expected = "00020101021229370016A000000677010111021300000000000005802TH530376454109999999.9963042B5D";

        try {
            PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
            assertEquals(expected, promptpay.generate());
        } catch (TargetMismatchException ex) {
            ex.printStackTrace();
        } catch (NegativeAmountException ex) {
            ex.printStackTrace();
        }
    }

    @DisplayName("Test invalid target with 9999999.99")
    @Test
    void generate04() {
        String target = "08123456789";
        Double amount = 9999999.99D;
        String expected = "00020101021229370016A000000677010111021300000000000005802TH530376454109999999.9963042B5D";

        assertThrows(TargetMismatchException.class, () -> {
            PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
            assertEquals(expected, promptpay.generate());
        });
    }

    @DisplayName("Test telephone with -9999999.99")
    @Test
    void generate05() {
        String target = "0812345678";
        Double amount = -9999999.99D;
        String expected = "00020101021229370016A000000677010111021300000000000005802TH530376454109999999.9963042B5D";

        assertThrows(NegativeAmountException.class, () -> {
            PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
            assertEquals(expected, promptpay.generate());
        });
    }
}